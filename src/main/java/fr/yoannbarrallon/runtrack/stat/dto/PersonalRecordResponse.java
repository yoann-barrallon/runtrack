package fr.yoannbarrallon.runtrack.stat.dto;

import fr.yoannbarrallon.runtrack.stat.RecordDistanceType;

import java.io.Serializable;
import java.time.Instant;

public record PersonalRecordResponse(
        RecordDistanceType distanceType,
        int timeSeconds,
        Instant achievedAt
) implements Serializable {
}
