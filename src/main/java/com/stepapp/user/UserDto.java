package com.stepapp.user;

import lombok.Builder;

@Builder
public record UserDto(
        Long id,
        String username,
        String email,
        String fullName,
        String avatarUrl
) {
    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
