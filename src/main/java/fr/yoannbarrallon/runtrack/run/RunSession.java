package fr.yoannbarrallon.runtrack.run;

import fr.yoannbarrallon.runtrack.auth.User;
import fr.yoannbarrallon.runtrack.plan.entity.PlannedSession;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "run_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planned_session_id")
    private PlannedSession plannedSession;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(name = "distance_meters", nullable = false)
    private int distanceMeters;

    @Column(name = "elevation_gain_meters", nullable = false)
    @Builder.Default
    private int elevationGainMeters = 0;

    @Column(name = "avg_pace_seconds_per_km", nullable = false)
    private int averagePaceSecondsPerKm;

    @Column(name = "avg_heart_rate")
    private Integer averageHeartRate;

    @Column(name = "source_type", nullable = false, length = 30)
    @Builder.Default
    private String sourceType = "MANUAL";

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "runSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("splitNumber ASC")
    @Builder.Default
    private List<RunSplit> splits = new ArrayList<>();

    public void addSplit(RunSplit split) {
        splits.add(split);
        split.setRunSession(this);
    }
}
