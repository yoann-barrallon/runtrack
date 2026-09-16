package fr.yoannbarrallon.runtrack.plan.entity;

import fr.yoannbarrallon.runtrack.auth.User;
import fr.yoannbarrallon.runtrack.plan.GoalDistanceType;
import fr.yoannbarrallon.runtrack.plan.TrainingPlanStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "training_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_distance_type", length = 50)
    private GoalDistanceType goalDistanceType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private TrainingPlanStatus status = TrainingPlanStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("targetDate ASC")
    @Builder.Default
    private List<PlannedSession> plannedSessions = new ArrayList<>();

    public void addPlannedSession(PlannedSession plannedSession) {
        plannedSessions.add(plannedSession);
        plannedSession.setPlan(this);
    }
}
