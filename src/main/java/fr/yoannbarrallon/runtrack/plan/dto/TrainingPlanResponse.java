package fr.yoannbarrallon.runtrack.plan.dto;

import fr.yoannbarrallon.runtrack.plan.GoalDistanceType;
import fr.yoannbarrallon.runtrack.plan.TrainingPlanStatus;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TrainingPlanResponse(
        UUID id,
        String title,
        GoalDistanceType goalDistanceType,
        LocalDate startDate,
        LocalDate endDate,
        TrainingPlanStatus status,
        Instant createdAt,
        List<PlannedSessionResponse> plannedSessions
) {
}
