package fr.yoannbarrallon.runtrack.stat.dto;

import java.io.Serializable;
import java.time.Instant;

public record WeeklyStatsResponse(
        Instant weekStart,
        long runCount,
        long totalDistanceMeters,
        long totalDurationSeconds,
        long totalElevationGainMeters
) implements Serializable {
}
