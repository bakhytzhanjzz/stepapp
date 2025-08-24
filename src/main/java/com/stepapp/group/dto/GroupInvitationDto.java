package com.stepapp.group.dto;

import com.stepapp.group.GroupInvitation;

public record GroupInvitationDto(Long id, String groupName, String inviter, String invited, String status) {
    public static GroupInvitationDto from(GroupInvitation gi) {
        return new GroupInvitationDto(
                gi.getId(),
                gi.getGroup().getName(),
                gi.getInviter().getUsername(),
                gi.getInvited().getUsername(),
                gi.getStatus().name()
        );
    }
}
