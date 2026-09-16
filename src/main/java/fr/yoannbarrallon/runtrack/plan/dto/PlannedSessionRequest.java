package fr.yoannbarrallon.runtrack.plan.dto;

import fr.yoannbarrallon.runtrack.plan.PlannedSessionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record PlannedSessionRequest(
        @NotNull LocalDate targetDate,
        @NotNull PlannedSessionType sessionType,
        @Positive Integer targetDistanceMeters,
        @Positive Integer targetDurationSeconds,
        String description
) {
}
