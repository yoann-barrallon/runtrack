package fr.yoannbarrallon.runtrack.run.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RunResponse(
        UUID id,
        String title,
        Instant startTime,
        int durationSeconds,
        int distanceMeters,
        int elevationGainMeters,
        int averagePaceSecondsPerKm,
        Integer averageHeartRate,
        String sourceType,
        Instant createdAt,
        List<RunSplitResponse> splits
) {
}
