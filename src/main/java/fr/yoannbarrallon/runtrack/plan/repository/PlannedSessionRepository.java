package fr.yoannbarrallon.runtrack.plan.repository;

import fr.yoannbarrallon.runtrack.auth.User;
import fr.yoannbarrallon.runtrack.plan.entity.PlannedSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlannedSessionRepository extends JpaRepository<PlannedSession, UUID> {

    Optional<PlannedSession> findByIdAndPlan_User(UUID id, User user);
}
