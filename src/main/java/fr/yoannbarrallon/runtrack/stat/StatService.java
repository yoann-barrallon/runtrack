package fr.yoannbarrallon.runtrack.stat;

import fr.yoannbarrallon.runtrack.stat.projection.OverallStatsProjection;
import fr.yoannbarrallon.runtrack.stat.dto.OverallStatsResponse;
import fr.yoannbarrallon.runtrack.stat.dto.PersonalRecordResponse;
import fr.yoannbarrallon.runtrack.stat.dto.WeeklyStatsResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class StatService {

    private final StatRepository statRepository;
    private final PersonalRecordRepository personalRecordRepository;

    public StatService(
            StatRepository statRepository,
            PersonalRecordRepository personalRecordRepository
    ) {
        this.statRepository = statRepository;
        this.personalRecordRepository = personalRecordRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "user_stats", key = "#userId")
    public OverallStatsResponse overall(UUID userId) {
        OverallStatsProjection stats =
                statRepository.findOverallStats(userId);
        List<PersonalRecordResponse> records = personalRecordRepository
                .findAllByUserId(userId)
                .stream()
                .map(record -> new PersonalRecordResponse(
                        record.getDistanceType(),
                        record.getTimeSeconds(),
                        record.getAchievedAt()
                ))
                .toList();

        return new OverallStatsResponse(
                stats.getRunCount(),
                stats.getTotalDistanceMeters(),
                stats.getTotalDurationSeconds(),
                stats.getTotalElevationGainMeters(),
                records
        );
    }

    @Transactional(readOnly = true)
    public List<WeeklyStatsResponse> weekly(UUID userId) {
        return statRepository.findWeeklyStats(userId)
                .stream()
                .map(stats -> new WeeklyStatsResponse(
                        stats.getWeekStart(),
                        stats.getRunCount(),
                        stats.getTotalDistanceMeters(),
                        stats.getTotalDurationSeconds(),
                        stats.getTotalElevationGainMeters()
                ))
                .toList();
    }
}
