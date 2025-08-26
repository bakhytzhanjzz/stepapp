package com.stepapp.group;

import com.stepapp.group.dto.GroupDto;
import com.stepapp.group.dto.GroupInvitationDto;
import com.stepapp.user.User;
import com.stepapp.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepo;
    private final GroupMemberRepository memberRepo;
    private final GroupInvitationRepository invitationRepo;
    private final UserService userService;

    // --- Создание группы ---
    @Transactional
    public GroupDto createGroup(String ownerUsername, String name, String description, boolean isPrivate) {
        User owner = userService.getByUsernameOrThrow(ownerUsername);

        Group group = Group.builder()
                .name(name)
                .description(description)
                .isPrivate(isPrivate)
                .build();

        group = groupRepo.save(group);

        memberRepo.save(GroupMember.builder()
                .group(group)
                .user(owner)
                .role(GroupMember.Role.OWNER)
                .build());

        return GroupDto.from(group);
    }

    // --- Приглашение пользователя в группу ---
    @Transactional
    public GroupInvitationDto inviteUser(Long groupId, String inviterUsername, String invitedUsername) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("Group not found"));
        User inviter = userService.getByUsernameOrThrow(inviterUsername);
        User invited = userService.getByUsernameOrThrow(invitedUsername);

        memberRepo.findByGroupAndUser(group, inviter)
                .orElseThrow(() -> new IllegalArgumentException("You are not a member of this group"));

        if (invitationRepo.findByGroupAndInvited(group, invited).isPresent()) {
            throw new IllegalStateException("User already invited");
        }

        GroupInvitation gi = invitationRepo.save(GroupInvitation.builder()
                .group(group)
                .inviter(inviter)
                .invited(invited)
                .status(GroupInvitation.Status.INVITED)
                .build());

        return GroupInvitationDto.from(gi);
    }

    // --- Ответ на приглашение ---
    @Transactional
    public GroupInvitationDto respondToInvite(Long inviteId, String invitedUsername, boolean accept) {
        GroupInvitation gi = invitationRepo.findById(inviteId)
                .orElseThrow(() -> new NoSuchElementException("Invite not found"));

        if (!gi.getInvited().getUsername().equals(invitedUsername)) {
            throw new IllegalArgumentException("You are not the invited user");
        }

        gi.setStatus(accept ? GroupInvitation.Status.ACCEPTED : GroupInvitation.Status.REJECTED);
        invitationRepo.save(gi);

        if (accept) {
            memberRepo.save(GroupMember.builder()
                    .group(gi.getGroup())
                    .user(gi.getInvited())
                    .role(GroupMember.Role.MEMBER)
                    .build());
        }

        return GroupInvitationDto.from(gi);
    }

    // --- Получение всех активных приглашений пользователя ---
    @Transactional(readOnly = true)
    public List<GroupInvitationDto> listPendingInvitations(String username) {
        User user = userService.getByUsernameOrThrow(username);

        return invitationRepo.findAllByInvitedAndStatus(user, GroupInvitation.Status.INVITED)
                .stream()
                .map(GroupInvitationDto::from)
                .toList();
    }

    // --- Получение групп пользователя ---
    @Transactional(readOnly = true)
    public List<GroupDto> listUserGroups(String username) {
        User u = userService.getByUsernameOrThrow(username);
        return memberRepo.findAllByUser(u).stream()
                .map(m -> GroupDto.from(m.getGroup()))
                .toList();
    }

    // --- Покинуть группу ---
    @Transactional
    public void leaveGroup(Long groupId, String username) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("Group not found"));
        User user = userService.getByUsernameOrThrow(username);

        GroupMember gm = memberRepo.findByGroupAndUser(group, user)
                .orElseThrow(() -> new IllegalArgumentException("Not a member"));

        if (gm.getRole() == GroupMember.Role.OWNER) {
            throw new IllegalStateException("Owner cannot leave group without deleting it");
        }

        memberRepo.delete(gm);
    }

    // --- Удалить группу ---
    @Transactional
    public void deleteGroup(Long groupId, String username) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("Group not found"));
        User user = userService.getByUsernameOrThrow(username);

        GroupMember gm = memberRepo.findByGroupAndUser(group, user)
                .orElseThrow(() -> new IllegalArgumentException("Not a member"));

        if (gm.getRole() != GroupMember.Role.OWNER) {
            throw new IllegalArgumentException("Only owner can delete group");
        }

        groupRepo.delete(group);
    }
}
