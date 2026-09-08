package fr.yoannbarrallon.runtrack.run;

import fr.yoannbarrallon.runtrack.auth.User;
import fr.yoannbarrallon.runtrack.run.dto.CreateRunRequest;
import fr.yoannbarrallon.runtrack.run.dto.RunResponse;
import fr.yoannbarrallon.runtrack.run.dto.UpdateRunRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RunServiceTest {

    @Mock
    private RunSessionRepository runSessionRepository;

    private RunService runService;

    @BeforeEach
    void setUp() {
        runService = new RunService(runSessionRepository);
    }

    @Test
    void shouldCalculateAveragePaceInSecondsPerKilometer() {
        assertThat(RunService.calculateAveragePaceSecondsPerKm(1800, 5000))
                .isEqualTo(360);
    }

    @Test
    void shouldRoundAveragePaceToNearestSecond() {
        assertThat(RunService.calculateAveragePaceSecondsPerKm(1000, 3000))
                .isEqualTo(333);
    }

    @Test
    void shouldCalculatePaceWhenCreatingRun() {
        User user = User.builder().id(UUID.randomUUID()).email("runner@example.com").build();
        CreateRunRequest request = new CreateRunRequest(
                "Morning run",
                Instant.parse("2026-09-08T06:30:00Z"),
                1800,
                5000,
                42
        );
        when(runSessionRepository.save(any(RunSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RunResponse response = runService.create(request, user);
        assertThat(response.averagePaceSecondsPerKm()).isEqualTo(360);
        verify(runSessionRepository).save(any(RunSession.class));
    }

    @Test
    void shouldUpdateOwnedRunAndRecalculatePace() {
        UUID runId = UUID.randomUUID();
        User user = User.builder().id(UUID.randomUUID()).email("runner@example.com").build();
        RunSession runSession = RunSession.builder()
                .id(runId)
                .user(user)
                .title("Old title")
                .startTime(Instant.parse("2026-09-08T06:30:00Z"))
                .durationSeconds(1800)
                .distanceMeters(5000)
                .elevationGainMeters(10)
                .averagePaceSecondsPerKm(360)
                .build();
        UpdateRunRequest request = new UpdateRunRequest(
                "Updated title",
                Instant.parse("2026-09-08T07:00:00Z"),
                2400,
                6000,
                25
        );
        when(runSessionRepository.findByIdAndUser(runId, user)).thenReturn(java.util.Optional.of(runSession));
        when(runSessionRepository.save(runSession)).thenReturn(runSession);

        RunResponse response = runService.update(runId, request, user);

        assertThat(response.title()).isEqualTo("Updated title");
        assertThat(response.averagePaceSecondsPerKm()).isEqualTo(400);
        verify(runSessionRepository).save(runSession);
    }

    @Test
    void shouldDeleteOwnedRun() {
        UUID runId = UUID.randomUUID();
        User user = User.builder().id(UUID.randomUUID()).email("runner@example.com").build();
        RunSession runSession = RunSession.builder().id(runId).user(user).build();
        when(runSessionRepository.findByIdAndUser(runId, user)).thenReturn(java.util.Optional.of(runSession));

        runService.delete(runId, user);

        verify(runSessionRepository).delete(runSession);
    }
}
