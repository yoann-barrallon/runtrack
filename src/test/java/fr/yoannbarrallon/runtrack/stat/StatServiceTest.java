package fr.yoannbarrallon.runtrack.stat;

import fr.yoannbarrallon.runtrack.stat.dto.OverallStatsResponse;
import fr.yoannbarrallon.runtrack.stat.dto.WeeklyStatsResponse;
import fr.yoannbarrallon.runtrack.stat.projection.OverallStatsProjection;
import fr.yoannbarrallon.runtrack.stat.projection.WeeklyStatsProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatServiceTest {

    @Mock
    private StatRepository statRepository;

    @Mock
    private PersonalRecordRepository personalRecordRepository;

    @Mock
    private OverallStatsProjection overallStats;

    @Mock
    private WeeklyStatsProjection weeklyStats;

    private StatService statService;

    @BeforeEach
    void setUp() {
        statService = new StatService(statRepository, personalRecordRepository);
    }

    @Test
    void shouldAggregateOverallStatsAndPersonalRecords() {
        UUID userId = UUID.randomUUID();
        PersonalRecord record = PersonalRecord.builder()
                .distanceType(RecordDistanceType.FIVE_KM)
                .timeSeconds(1_500)
                .achievedAt(Instant.parse("2026-09-15T06:30:00Z"))
                .build();
        when(statRepository.findOverallStats(userId)).thenReturn(overallStats);
        when(overallStats.getRunCount()).thenReturn(12L);
        when(overallStats.getTotalDistanceMeters()).thenReturn(75_000L);
        when(overallStats.getTotalDurationSeconds()).thenReturn(27_000L);
        when(overallStats.getTotalElevationGainMeters()).thenReturn(900L);
        when(personalRecordRepository.findAllByUserId(userId)).thenReturn(List.of(record));

        OverallStatsResponse response = statService.overall(userId);

        assertThat(response.runCount()).isEqualTo(12);
        assertThat(response.totalDistanceMeters()).isEqualTo(75_000);
        assertThat(response.personalRecords()).singleElement()
                .satisfies(item -> assertThat(item.timeSeconds()).isEqualTo(1_500));
    }

    @Test
    void shouldMapWeeklyStats() {
        UUID userId = UUID.randomUUID();
        Instant weekStart = Instant.parse("2026-09-14T00:00:00Z");
        when(statRepository.findWeeklyStats(userId)).thenReturn(List.of(weeklyStats));
        when(weeklyStats.getWeekStart()).thenReturn(weekStart);
        when(weeklyStats.getRunCount()).thenReturn(3L);
        when(weeklyStats.getTotalDistanceMeters()).thenReturn(18_000L);
        when(weeklyStats.getTotalDurationSeconds()).thenReturn(5_400L);
        when(weeklyStats.getTotalElevationGainMeters()).thenReturn(120L);

        List<WeeklyStatsResponse> response = statService.weekly(userId);

        assertThat(response).singleElement().satisfies(item -> {
            assertThat(item.weekStart()).isEqualTo(weekStart);
            assertThat(item.runCount()).isEqualTo(3);
            assertThat(item.totalDistanceMeters()).isEqualTo(18_000);
        });
    }
}
