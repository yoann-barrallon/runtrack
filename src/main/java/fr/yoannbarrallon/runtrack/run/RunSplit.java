package fr.yoannbarrallon.runtrack.run;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "run_splits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_session_id", nullable = false)
    private RunSession runSession;

    @Column(name = "split_number", nullable = false)
    private int splitNumber;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(name = "avg_pace_seconds_per_km", nullable = false)
    private int averagePaceSecondsPerKm;

    @Column(name = "elevation_gain_meters", nullable = false)
    @Builder.Default
    private int elevationGainMeters = 0;
}
