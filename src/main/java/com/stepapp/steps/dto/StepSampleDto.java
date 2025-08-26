package com.stepapp.steps.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record StepSampleDto(
        String externalId,
        @NotNull OffsetDateTime startedAt,
        @NotNull OffsetDateTime endedAt,
        @NotNull @Min(0) Integer steps,
        String source
) {}
