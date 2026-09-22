package fr.yoannbarrallon.runtrack.plan;

import fr.yoannbarrallon.runtrack.auth.User;
import fr.yoannbarrallon.runtrack.plan.dto.*;
import fr.yoannbarrallon.runtrack.plan.entity.TrainingPlan;
import fr.yoannbarrallon.runtrack.plan.repository.TrainingPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingPlanServiceTest {

    @Mock
    private TrainingPlanRepository trainingPlanRepository;

    private TrainingPlanService trainingPlanService;

    @BeforeEach
    void setUp() {
        trainingPlanService = new TrainingPlanService(trainingPlanRepository);
    }

    @Test
    void shouldCreatePlanWithPlannedSessions() {
        User user = User.builder().id(UUID.randomUUID()).email("runner@example.com").build();
        CreateTrainingPlanRequest request = new CreateTrainingPlanRequest(
                "10K preparation",
                GoalDistanceType.TEN_K,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 15),
                List.of(new PlannedSessionRequest(
                        LocalDate.of(2026, 9, 2),
                        PlannedSessionType.EASY_RUN,
                        5000,
                        1800,
                        "Easy effort"
                ))
        );
        when(trainingPlanRepository.save(any(TrainingPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TrainingPlanResponse response = trainingPlanService.create(request, user);

        assertThat(response.title()).isEqualTo("10K preparation");
        assertThat(response.status()).isEqualTo(TrainingPlanStatus.ACTIVE);
        assertThat(response.plannedSessions()).hasSize(1);
        verify(trainingPlanRepository).save(argThat(plan ->
                plan.getUser() == user && plan.getPlannedSessions().size() == 1
        ));
    }

    @Test
    void shouldReturnOnlyUpcomingSessionsForActivePlan() {
        User user = User.builder().id(UUID.randomUUID()).email("runner@example.com").build();
        TrainingPlan plan = TrainingPlan.builder()
                .id(UUID.randomUUID())
                .user(user)
                .title("Current plan")
                .startDate(LocalDate.now().minusDays(7))
                .endDate(LocalDate.now().plusDays(14))
                .status(TrainingPlanStatus.ACTIVE)
                .build();
        plan.addPlannedSession(fr.yoannbarrallon.runtrack.plan.entity.PlannedSession.builder()
                .targetDate(LocalDate.now().minusDays(1))
                .sessionType(PlannedSessionType.EASY_RUN)
                .build());
        plan.addPlannedSession(fr.yoannbarrallon.runtrack.plan.entity.PlannedSession.builder()
                .targetDate(LocalDate.now().plusDays(1))
                .sessionType(PlannedSessionType.LONG_RUN)
                .build());
        when(trainingPlanRepository.findByUserAndStatus(user, TrainingPlanStatus.ACTIVE))
                .thenReturn(Optional.of(plan));

        TrainingPlanResponse response = trainingPlanService.findActive(user);

        assertThat(response.plannedSessions()).hasSize(1);
        assertThat(response.plannedSessions().getFirst().sessionType())
                .isEqualTo(PlannedSessionType.LONG_RUN);
    }

    @Test
    void shouldClosePlan() {
        UUID planId = UUID.randomUUID();
        User user = User.builder().id(UUID.randomUUID()).email("runner@example.com").build();
        TrainingPlan plan = TrainingPlan.builder()
                .id(planId)
                .user(user)
                .title("Current plan")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(14))
                .status(TrainingPlanStatus.ACTIVE)
                .build();
        when(trainingPlanRepository.findByIdAndUser(planId, user)).thenReturn(Optional.of(plan));
        when(trainingPlanRepository.save(plan)).thenReturn(plan);

        TrainingPlanResponse response = trainingPlanService.updateStatus(
                planId,
                new UpdateTrainingPlanStatusRequest(TrainingPlanStatus.COMPLETED),
                user
        );

        assertThat(response.status()).isEqualTo(TrainingPlanStatus.COMPLETED);
        verify(trainingPlanRepository).save(plan);
    }

    @Test
    void shouldRejectPlanWithEndDateBeforeStartDate() {
        CreateTrainingPlanRequest request = new CreateTrainingPlanRequest(
                "Invalid plan",
                GoalDistanceType.FIVE_K,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 9, 30),
                List.of(new PlannedSessionRequest(
                        LocalDate.of(2026, 10, 1),
                        PlannedSessionType.EASY_RUN,
                        5000,
                        1800,
                        null
                ))
        );

        assertThatThrownBy(() -> trainingPlanService.create(request, User.builder().build()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("End date must not be before start date");
        verifyNoInteractions(trainingPlanRepository);
    }

    @Test
    void shouldRejectPlannedSessionBeforePlanStartDate() {
        CreateTrainingPlanRequest request = validPlanRequest(LocalDate.of(2026, 8, 31));

        assertThatThrownBy(() -> trainingPlanService.create(request, User.builder().build()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Planned session date must be within the training plan dates");
        verifyNoInteractions(trainingPlanRepository);
    }

    @Test
    void shouldRejectPlannedSessionAfterPlanEndDate() {
        CreateTrainingPlanRequest request = validPlanRequest(LocalDate.of(2026, 10, 16));

        assertThatThrownBy(() -> trainingPlanService.create(request, User.builder().build()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Planned session date must be within the training plan dates");
        verifyNoInteractions(trainingPlanRepository);
    }

    @Test
    void shouldReturnNotFoundWhenThereIsNoActivePlan() {
        User user = User.builder().id(UUID.randomUUID()).build();
        when(trainingPlanRepository.findByUserAndStatus(user, TrainingPlanStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingPlanService.findActive(user))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Active training plan not found");
    }

    @Test
    void shouldRejectTryingToReactivateAPlanThroughStatusEndpoint() {
        assertThatThrownBy(() -> trainingPlanService.updateStatus(
                UUID.randomUUID(),
                new UpdateTrainingPlanStatusRequest(TrainingPlanStatus.ACTIVE),
                User.builder().build()
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("A plan can only be closed");
        verifyNoInteractions(trainingPlanRepository);
    }

    @Test
    void shouldReturnNotFoundWhenClosingAnUnknownPlan() {
        UUID planId = UUID.randomUUID();
        User user = User.builder().id(UUID.randomUUID()).build();
        when(trainingPlanRepository.findByIdAndUser(planId, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingPlanService.updateStatus(
                planId,
                new UpdateTrainingPlanStatusRequest(TrainingPlanStatus.COMPLETED),
                user
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Training plan not found");
    }

    private CreateTrainingPlanRequest validPlanRequest(LocalDate targetDate) {
        return new CreateTrainingPlanRequest(
                "10K preparation",
                GoalDistanceType.TEN_K,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 10, 15),
                List.of(new PlannedSessionRequest(
                        targetDate,
                        PlannedSessionType.EASY_RUN,
                        5000,
                        1800,
                        null
                ))
        );
    }
}
