package fr.yoannbarrallon.runtrack.run.fit;

public record FitSplitData(
        long durationSeconds,
        long distanceMeters,
        int elevationGainMeters
) {
}
