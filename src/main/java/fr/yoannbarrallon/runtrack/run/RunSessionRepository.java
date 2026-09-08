package fr.yoannbarrallon.runtrack.run;

import fr.yoannbarrallon.runtrack.auth.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RunSessionRepository extends JpaRepository<RunSession, UUID> {

    Page<RunSession> findAllByUserOrderByStartTimeDesc(User user, Pageable pageable);

    Optional<RunSession> findByIdAndUser(UUID id, User user);
}
