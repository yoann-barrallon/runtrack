package fr.yoannbarrallon.runtrack.stat.dto;

import java.io.Serializable;
import java.util.List;

public record OverallStatsResponse(
        long runCount,
        long totalDistanceMeters,
        long totalDurationSeconds,
        long totalElevationGainMeters,
        List<PersonalRecordResponse> personalRecords
) implements Serializable {
}
