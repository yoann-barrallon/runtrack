package fr.yoannbarrallon.runtrack.stat.projection;

import java.time.Instant;

public interface WeeklyStatsProjection {

    Instant getWeekStart();

    long getRunCount();

    long getTotalDistanceMeters();

    long getTotalDurationSeconds();

    long getTotalElevationGainMeters();
}
