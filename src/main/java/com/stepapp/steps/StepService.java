package com.stepapp.steps;

import com.stepapp.friend.FriendService;
import com.stepapp.group.Group;
import com.stepapp.group.GroupMemberRepository;
import com.stepapp.group.GroupRepository;
import com.stepapp.group.GroupService;
import com.stepapp.steps.dto.*;
import com.stepapp.user.User;
import com.stepapp.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StepService {

    private final StepSampleRepository sampleRepo;
    private final DailyStepsRepository dailyRepo;
    private final UserService userService;
    private final FriendService friendService; // used for friends leaderboard
    private final GroupRepository groupRepository; // used for group leaderboard

    /**
     * Ingests a batch of samples for a user.
     * Returns summary: accepted / skipped / perDate increments.
     */
    @Transactional
    public UploadStepsResponse ingest(String username, UploadStepsRequest req) {
        if (req == null || req.samples() == null || req.samples().isEmpty()) {
            throw new IllegalArgumentException("Empty payload");
        }

        User user = userService.getByUsernameOrThrow(username);

        StepSample.Provider provider;
        try {
            provider = StepSample.Provider.valueOf(req.provider().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown provider: " + req.provider());
        }

        Map<LocalDate, Long> addedPerDay = new HashMap<>();
        int accepted = 0, skipped = 0;

        ZoneId userZone = ZoneId.of(Optional.ofNullable(user.getTimezone()).orElse("UTC"));

        for (var dto : req.samples()) {
            // basic validation
            if (dto.steps() == null || dto.steps() < 0) {
                skipped++;
                continue;
            }
            if (dto.startedAt().isAfter(dto.endedAt())) {
                skipped++;
                continue;
            }
            // Prevent future insane timestamps (allow small skew of 5 minutes)
            if (dto.endedAt().isAfter(OffsetDateTime.now().plusMinutes(5))) {
                skipped++;
                continue;
            }

            // Dedup by externalId when provided
            if (dto.externalId() != null && !dto.externalId().isBlank()) {
                var exists = sampleRepo.findByUserAndProviderAndExternalId(user, provider, dto.externalId());
                if (exists.isPresent()) {
                    // duplicate
                    skipped++;
                    continue;
                }
            }

            // Persist sample
            StepSample s = StepSample.builder()
                    .user(user)
                    .provider(provider)
                    .externalId(dto.externalId())
                    .startedAt(dto.startedAt())
                    .endedAt(dto.endedAt())
                    .steps(dto.steps())
                    .source(dto.source())
                    .build();
            sampleRepo.save(s);
            accepted++;

            // Aggregate by local date — use the sample's start time to pick date
            Instant instant = dto.startedAt().toInstant();
            LocalDate localDate = instant.atZone(userZone).toLocalDate();
            addedPerDay.merge(localDate, dto.steps().longValue(), Long::sum);
        }

        // Update daily aggregates (atomic per-user transaction)
        for (var e : addedPerDay.entrySet()) {
            LocalDate date = e.getKey();
            long toAdd = e.getValue();

            DailySteps ds = dailyRepo.findByUserAndDate(user, date)
                    .map(d -> {
                        d.setStepsTotal(d.getStepsTotal() + toAdd);
                        return d;
                    })
                    .orElseGet(() -> DailySteps.builder()
                            .user(user)
                            .date(date)
                            .stepsTotal(toAdd)
                            .build());

            dailyRepo.save(ds);
        }

        // Prepare response
        Map<String, Long> perDateStr = addedPerDay.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));

        log.debug("Ingest result for user={} accepted={} skipped={} perDate={}", username, accepted, skipped, perDateStr);
        return new UploadStepsResponse(accepted, skipped, perDateStr);
    }

    @Transactional(readOnly = true)
    public List<DailyStepsDto> getDaily(String username, LocalDate from, LocalDate to) {
        User user = userService.getByUsernameOrThrow(username);
        if (from == null || to == null) {
            throw new IllegalArgumentException("from/to required");
        }
        var list = dailyRepo.findAllByUserAndDateBetween(user, from, to);
        return list.stream()
                .map(d -> new DailyStepsDto(d.getDate(), d.getStepsTotal()))
                .sorted(Comparator.comparing(DailyStepsDto::date))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DailyStepsDto> getHistory(String username, int days) {
        if (days <= 0) throw new IllegalArgumentException("days must be > 0");
        User user = userService.getByUsernameOrThrow(username);
        LocalDate to = LocalDate.now(ZoneId.of(Optional.ofNullable(user.getTimezone()).orElse("UTC")));
        LocalDate from = to.minusDays(days - 1);
        return getDaily(username, from, to);
    }

    /**
     * Leaderboard among friends for a given date.
     */
    @Transactional(readOnly = true)
    public List<LeaderboardEntryDto> friendsLeaderboard(String username, LocalDate date, int limit) {
        User user = userService.getByUsernameOrThrow(username);
        // friendService.listFriends returns List<String> usernames
        List<String> friendsUsernames = friendService.listFriends(username);
        // include self too
        List<String> allUsernames = new ArrayList<>(friendsUsernames);
        allUsernames.add(user.getUsername());

        // load User entities
        var users = allUsernames.stream()
                .map(userService::findByUsername)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (users.isEmpty()) return Collections.emptyList();

        var top = dailyRepo.findTopByDateAndUsers(date, users, PageRequest.of(0, Math.max(1, limit)));
        return top.stream()
                .map(d -> new LeaderboardEntryDto(d.getUser().getUsername(), d.getStepsTotal()))
                .collect(Collectors.toList());
    }

    /**
     * Leaderboard for a group (by groupId) for a given date.
     */
    @Transactional(readOnly = true)
    public List<LeaderboardEntryDto> groupLeaderboard(Long groupId, LocalDate date, int limit) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("Group not found"));

        // extract members
        var members = group.getMembers().stream()
                .map(m -> m.getUser())
                .collect(Collectors.toList());

        if (members.isEmpty()) return Collections.emptyList();

        var top = dailyRepo.findTopByDateAndUsers(date, members, PageRequest.of(0, Math.max(1, limit)));
        return top.stream()
                .map(d -> new LeaderboardEntryDto(d.getUser().getUsername(), d.getStepsTotal()))
                .collect(Collectors.toList());
    }
}
