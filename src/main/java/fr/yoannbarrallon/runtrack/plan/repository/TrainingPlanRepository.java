package fr.yoannbarrallon.runtrack.plan.repository;

import fr.yoannbarrallon.runtrack.auth.User;
import fr.yoannbarrallon.runtrack.plan.TrainingPlanStatus;
import fr.yoannbarrallon.runtrack.plan.entity.TrainingPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, UUID> {

    Optional<TrainingPlan> findByIdAndUser(UUID id, User user);

    Optional<TrainingPlan> findByUserAndStatus(User user, TrainingPlanStatus status);
}
