package fr.yoannbarrallon.runtrack.stat;

import fr.yoannbarrallon.runtrack.run.RunSession;
import fr.yoannbarrallon.runtrack.stat.projection.OverallStatsProjection;
import fr.yoannbarrallon.runtrack.stat.projection.WeeklyStatsProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface StatRepository extends Repository<RunSession, UUID> {

    @Query(value = """
            SELECT COUNT(*) AS runCount,
                   COALESCE(SUM(distance_meters), 0) AS totalDistanceMeters,
                   COALESCE(SUM(duration_seconds), 0) AS totalDurationSeconds,
                   COALESCE(SUM(elevation_gain_meters), 0) AS totalElevationGainMeters
            FROM run_sessions
            WHERE user_id = :userId
            """, nativeQuery = true)
    OverallStatsProjection findOverallStats(@Param("userId") UUID userId);

    @Query(value = """
            SELECT DATE_TRUNC('week', start_time) AS weekStart,
                   COUNT(*) AS runCount,
                   COALESCE(SUM(distance_meters), 0) AS totalDistanceMeters,
                   COALESCE(SUM(duration_seconds), 0) AS totalDurationSeconds,
                   COALESCE(SUM(elevation_gain_meters), 0) AS totalElevationGainMeters
            FROM run_sessions
            WHERE user_id = :userId
            GROUP BY DATE_TRUNC('week', start_time)
            ORDER BY weekStart DESC
            """, nativeQuery = true)
    List<WeeklyStatsProjection> findWeeklyStats(@Param("userId") UUID userId);
}
