package com.stepapp.steps.dto;

import java.time.LocalDate;

public record DailyStepsDto(
        LocalDate date,
        Long stepsTotal
) {}
