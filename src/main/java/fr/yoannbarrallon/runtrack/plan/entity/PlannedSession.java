package fr.yoannbarrallon.runtrack.plan.entity;

import fr.yoannbarrallon.runtrack.plan.PlannedSessionType;
import fr.yoannbarrallon.runtrack.run.RunSession;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "planned_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlannedSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private TrainingPlan plan;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false, length = 50)
    private PlannedSessionType sessionType;

    @Column(name = "target_distance_meters")
    private Integer targetDistanceMeters;

    @Column(name = "target_duration_seconds")
    private Integer targetDurationSeconds;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "plannedSession")
    @Builder.Default
    private List<RunSession> runs = new ArrayList<>();
}
