package fr.yoannbarrallon.runtrack.stat;

import fr.yoannbarrallon.runtrack.auth.User;
import fr.yoannbarrallon.runtrack.run.RunSession;
import fr.yoannbarrallon.runtrack.run.RunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonalRecordServiceTest {

    @Mock
    private PersonalRecordRepository personalRecordRepository;

    @Mock
    private RunRepository runRepository;

    private PersonalRecordService personalRecordService;

    @BeforeEach
    void setUp() {
        personalRecordService = new PersonalRecordService(personalRecordRepository, runRepository);
    }

    @Test
    void shouldCreateRecordsForEverySupportedDistanceCovered() {
        User user = user();
        RunSession run = run(user, 21_100, 7_200);

        personalRecordService.detectRecords(run);

        verify(personalRecordRepository, times(4))
                .save(argThat(record -> record.getUser() == user && record.getRunSession() == run));
        verify(personalRecordRepository)
                .findByUserIdAndDistanceType(user.getId(), RecordDistanceType.ONE_KM);
        verify(personalRecordRepository)
                .findByUserIdAndDistanceType(user.getId(), RecordDistanceType.HALF_MARATHON);
    }

    @Test
    void shouldUpdateAnExistingRecordOnlyWhenTheNewTimeIsFaster() {
        User user = user();
        RunSession run = run(user, 5_000, 1_500);
        PersonalRecord record = PersonalRecord.builder()
                .user(user)
                .distanceType(RecordDistanceType.FIVE_KM)
                .timeSeconds(1_600)
                .build();
        when(personalRecordRepository.findByUserIdAndDistanceType(
                eq(user.getId()),
                any(RecordDistanceType.class)
        )).thenReturn(Optional.empty());
        when(personalRecordRepository.findByUserIdAndDistanceType(user.getId(), RecordDistanceType.FIVE_KM))
                .thenReturn(Optional.of(record));

        personalRecordService.detectRecords(run);

        assertThat(record.getTimeSeconds()).isEqualTo(1_500);
        assertThat(record.getRunSession()).isSameAs(run);
        verify(personalRecordRepository).save(record);
    }

    @Test
    void shouldNotSaveAnExistingRecordWhenTheNewTimeIsSlower() {
        User user = user();
        RunSession run = run(user, 5_000, 1_700);
        PersonalRecord record = PersonalRecord.builder()
                .user(user)
                .distanceType(RecordDistanceType.FIVE_KM)
                .timeSeconds(1_600)
                .build();
        when(personalRecordRepository.findByUserIdAndDistanceType(
                eq(user.getId()),
                any(RecordDistanceType.class)
        )).thenReturn(Optional.empty());
        when(personalRecordRepository.findByUserIdAndDistanceType(user.getId(), RecordDistanceType.FIVE_KM))
                .thenReturn(Optional.of(record));

        personalRecordService.detectRecords(run);

        assertThat(record.getTimeSeconds()).isEqualTo(1_600);
        verify(personalRecordRepository, never()).save(record);
    }

    @Test
    void shouldRecalculateRecordsFromAllRemainingRuns() {
        User user = user();
        RunSession run = run(user, 5_000, 1_500);
        when(runRepository.findAllByUserOrderByStartTimeAsc(user)).thenReturn(List.of(run));
        when(personalRecordRepository.findByUserIdAndDistanceType(
                eq(user.getId()),
                any(RecordDistanceType.class)
        )).thenReturn(Optional.empty());

        personalRecordService.recalculateRecords(user);

        verify(personalRecordRepository).deleteAllByUserId(user.getId());
        verify(runRepository).findAllByUserOrderByStartTimeAsc(user);
        verify(personalRecordRepository, times(2))
                .save(argThat(record -> record.getRunSession() == run));
    }

    private User user() {
        return User.builder().id(UUID.randomUUID()).email("runner@example.com").build();
    }

    private RunSession run(User user, int distanceMeters, int durationSeconds) {
        return RunSession.builder()
                .user(user)
                .startTime(Instant.parse("2026-09-16T06:30:00Z"))
                .distanceMeters(distanceMeters)
                .durationSeconds(durationSeconds)
                .build();
    }
}
