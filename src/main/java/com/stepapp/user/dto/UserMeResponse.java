package com.stepapp.user.dto;

import com.stepapp.user.User;

public record UserMeResponse(
        Long id,
        String email,
        String username,
        String fullName,
        String avatarUrl,
        String timezone
) {
    public static UserMeResponse from(User u) {
        return new UserMeResponse(
                u.getId(),
                u.getEmail(),
                u.getUsername(),
                u.getFullName(),
                u.getAvatarUrl(),
                u.getTimezone()
        );
    }
}
