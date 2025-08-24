package com.stepapp.auth.dto;

public record LoginResponse(
        String accessToken,
        String tokenType
) {
    public static final String BEARER = "Bearer";
}
