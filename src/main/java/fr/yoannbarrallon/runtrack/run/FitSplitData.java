package fr.yoannbarrallon.runtrack.run;

public record FitSplitData(
        long durationSeconds,
        long distanceMeters,
        int elevationGainMeters
) {
}
