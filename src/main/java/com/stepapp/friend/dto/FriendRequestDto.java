package com.stepapp.friend.dto;

import com.stepapp.friend.FriendRequest;

public record FriendRequestDto(
        Long id,
        String senderUsername,
        String receiverUsername,
        String status
) {
    public static FriendRequestDto from(FriendRequest fr) {
        return new FriendRequestDto(
                fr.getId(),
                fr.getSender().getUsername(),
                fr.getReceiver().getUsername(),
                fr.getStatus().name()
        );
    }
}
