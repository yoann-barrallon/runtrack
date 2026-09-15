package fr.yoannbarrallon.runtrack.stat;

import fr.yoannbarrallon.runtrack.auth.User;
import fr.yoannbarrallon.runtrack.run.RunSession;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "personal_records",
        uniqueConstraints = @UniqueConstraint(name = "uq_user_distance_record", columnNames = {"user_id", "distance_type"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_session_id")
    private RunSession runSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "distance_type", nullable = false, length = 50)
    private RecordDistanceType distanceType;

    @Column(name = "time_seconds", nullable = false)
    private int timeSeconds;

    @Column(name = "achieved_at", nullable = false)
    private Instant achievedAt;
}
