package com.stepapp.auth.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        String refreshToken
) {
    public static final String BEARER = "Bearer";
}
