package fr.yoannbarrallon.runtrack.plan;

import fr.yoannbarrallon.runtrack.auth.User;
import fr.yoannbarrallon.runtrack.plan.dto.*;
import fr.yoannbarrallon.runtrack.plan.entity.PlannedSession;
import fr.yoannbarrallon.runtrack.plan.entity.TrainingPlan;
import fr.yoannbarrallon.runtrack.plan.repository.TrainingPlanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class TrainingPlanService {

    private final TrainingPlanRepository trainingPlanRepository;

    public TrainingPlanService(TrainingPlanRepository trainingPlanRepository) {
        this.trainingPlanRepository = trainingPlanRepository;
    }

    @Transactional
    public TrainingPlanResponse create(CreateTrainingPlanRequest request, User user) {
        validateDates(request.startDate(), request.endDate());
        TrainingPlan plan = TrainingPlan.builder()
                .user(user)
                .title(request.title())
                .goalDistanceType(request.goalDistanceType())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(TrainingPlanStatus.ACTIVE)
                .build();

        request.plannedSessions().forEach(sessionRequest -> {
            validateSessionDate(sessionRequest.targetDate(), request.startDate(), request.endDate());
            plan.addPlannedSession(PlannedSession.builder()
                    .targetDate(sessionRequest.targetDate())
                    .sessionType(sessionRequest.sessionType())
                    .targetDistanceMeters(sessionRequest.targetDistanceMeters())
                    .targetDurationSeconds(sessionRequest.targetDurationSeconds())
                    .description(sessionRequest.description())
                    .build());
        });

        return toResponse(trainingPlanRepository.save(plan), false);
    }

    @Transactional(readOnly = true)
    public TrainingPlanResponse findActive(User user) {
        TrainingPlan plan = trainingPlanRepository.findByUserAndStatus(user, TrainingPlanStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active training plan not found"));
        return toResponse(plan, true);
    }

    @Transactional
    public TrainingPlanResponse updateStatus(
            UUID id,
            UpdateTrainingPlanStatusRequest request,
            User user
    ) {
        if (request.status() == TrainingPlanStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A plan can only be closed");
        }
        TrainingPlan plan = trainingPlanRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training plan not found"));
        plan.setStatus(request.status());
        return toResponse(trainingPlanRepository.save(plan), false);
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must not be before start date");
        }
    }

    private void validateSessionDate(LocalDate targetDate, LocalDate startDate, LocalDate endDate) {
        if (targetDate.isBefore(startDate) || targetDate.isAfter(endDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Planned session date must be within the training plan dates"
            );
        }
    }

    private TrainingPlanResponse toResponse(TrainingPlan plan, boolean upcomingOnly) {
        LocalDate today = LocalDate.now();
        List<PlannedSessionResponse> sessions = plan.getPlannedSessions().stream()
                .filter(session -> !upcomingOnly || !session.getTargetDate().isBefore(today))
                .map(session -> new PlannedSessionResponse(
                        session.getId(),
                        session.getTargetDate(),
                        session.getSessionType(),
                        session.getTargetDistanceMeters(),
                        session.getTargetDurationSeconds(),
                        session.getDescription()
                ))
                .toList();
        return new TrainingPlanResponse(
                plan.getId(),
                plan.getTitle(),
                plan.getGoalDistanceType(),
                plan.getStartDate(),
                plan.getEndDate(),
                plan.getStatus(),
                plan.getCreatedAt(),
                sessions
        );
    }
}
