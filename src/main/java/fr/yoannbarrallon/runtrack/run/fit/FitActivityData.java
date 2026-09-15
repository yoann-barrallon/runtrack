package fr.yoannbarrallon.runtrack.run.fit;

import java.time.Instant;
import java.util.List;

public record FitActivityData(
        Instant startTime,
        long durationSeconds,
        long distanceMeters,
        int elevationGainMeters,
        Integer averageHeartRate,
        List<FitSplitData> splits
) {
}
