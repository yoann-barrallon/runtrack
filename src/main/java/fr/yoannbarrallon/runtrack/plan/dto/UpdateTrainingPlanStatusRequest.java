package fr.yoannbarrallon.runtrack.plan.dto;

import fr.yoannbarrallon.runtrack.plan.TrainingPlanStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTrainingPlanStatusRequest(@NotNull TrainingPlanStatus status) {
}
