package fr.yoannbarrallon.runtrack.plan.dto;

import fr.yoannbarrallon.runtrack.plan.PlannedSessionType;

import java.time.LocalDate;
import java.util.UUID;

public record PlannedSessionResponse(
        UUID id,
        LocalDate targetDate,
        PlannedSessionType sessionType,
        Integer targetDistanceMeters,
        Integer targetDurationSeconds,
        String description
) {
}
