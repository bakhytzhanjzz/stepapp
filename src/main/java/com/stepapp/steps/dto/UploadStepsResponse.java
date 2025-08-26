package com.stepapp.steps.dto;

import java.util.Map;

public record UploadStepsResponse(
        int accepted,
        int skipped,
        Map<String, Long> perDate
) {}
