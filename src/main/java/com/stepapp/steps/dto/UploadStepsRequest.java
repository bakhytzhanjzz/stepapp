package com.stepapp.steps.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UploadStepsRequest(
        @NotNull String provider,
        String deviceId,
        @NotEmpty List<StepSampleDto> samples
) {}
