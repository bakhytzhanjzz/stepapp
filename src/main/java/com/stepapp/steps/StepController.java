package com.stepapp.steps;

import com.stepapp.config.SecurityUtils;
import com.stepapp.steps.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/steps")
@RequiredArgsConstructor
public class StepController {

    private final StepService stepService;

    /**
     * Upload batch of step samples.
     */
    @PostMapping("/upload")
    public ResponseEntity<UploadStepsResponse> upload(
            @Valid @RequestBody UploadStepsRequest req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        String username = SecurityUtils.getCurrentUsernameOrNull();
        var resp = stepService.ingest(username, req);
        return ResponseEntity.ok(resp);
    }

    /**
     * Daily totals for current user between dates (inclusive)
     */
    @GetMapping("/daily")
    public ResponseEntity<List<DailyStepsDto>> daily(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        String username = SecurityUtils.getCurrentUsernameOrNull();
        return ResponseEntity.ok(stepService.getDaily(username, from, to));
    }

    /**
     * History for 'days' days up to today.
     */
    @GetMapping("/history")
    public ResponseEntity<List<DailyStepsDto>> history(
            @RequestParam(defaultValue = "30") int days
    ) {
        String username = SecurityUtils.getCurrentUsernameOrNull();
        return ResponseEntity.ok(stepService.getHistory(username, days));
    }

    /**
     * Leaderboard among friends for a given date
     */
    @GetMapping("/leaderboard/friends")
    public ResponseEntity<List<LeaderboardEntryDto>> friendsLeaderboard(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "10") int top
    ) {
        String username = SecurityUtils.getCurrentUsernameOrNull();
        return ResponseEntity.ok(stepService.friendsLeaderboard(username, date, top));
    }

    /**
     * Leaderboard for a group
     */
    @GetMapping("/leaderboard/groups/{groupId}")
    public ResponseEntity<List<LeaderboardEntryDto>> groupLeaderboard(
            @PathVariable Long groupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "10") int top
    ) {
        return ResponseEntity.ok(stepService.groupLeaderboard(groupId, date, top));
    }
}
