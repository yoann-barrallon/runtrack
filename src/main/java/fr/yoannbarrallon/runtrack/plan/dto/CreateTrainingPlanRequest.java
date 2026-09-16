package fr.yoannbarrallon.runtrack.plan.dto;

import fr.yoannbarrallon.runtrack.plan.GoalDistanceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CreateTrainingPlanRequest(
        @NotBlank String title,
        GoalDistanceType goalDistanceType,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotEmpty List<@Valid PlannedSessionRequest> plannedSessions
) {
}
