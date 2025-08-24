package com.stepapp.auth.dto;

public record RegisterResponse(
        Long id,
        String email,
        String username,
        String fullName
) {}
