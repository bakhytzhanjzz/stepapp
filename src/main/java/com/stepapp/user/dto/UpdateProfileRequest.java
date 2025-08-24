package com.stepapp.user.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Nullable @Size(max = 100) String fullName,
        @Nullable @Size(max = 500) String avatarUrl,
        @Nullable @Size(min = 2, max = 50) String timezone
) {}
