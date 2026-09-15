package fr.yoannbarrallon.runtrack.stat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface PersonalRecordRepository extends JpaRepository<PersonalRecord, UUID> {

    Optional<PersonalRecord> findByUserIdAndDistanceType(UUID userId, RecordDistanceType distanceType);

    List<PersonalRecord> findAllByUserId(UUID userId);
}
