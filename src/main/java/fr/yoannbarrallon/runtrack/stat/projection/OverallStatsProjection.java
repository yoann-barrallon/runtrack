package fr.yoannbarrallon.runtrack.stat.projection;

public interface OverallStatsProjection {

    long getRunCount();

    long getTotalDistanceMeters();

    long getTotalDurationSeconds();

    long getTotalElevationGainMeters();
}
