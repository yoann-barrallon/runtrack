package fr.yoannbarrallon.runtrack.run.dto;

import java.util.UUID;

public record RunSplitResponse(
        UUID id,
        int splitNumber,
        int durationSeconds,
        int averagePaceSecondsPerKm,
        int elevationGainMeters
) {
}
