package fr.yoannbarrallon.runtrack.plan;

import fr.yoannbarrallon.runtrack.auth.User;
import fr.yoannbarrallon.runtrack.plan.dto.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plans")
@SecurityRequirement(name = "bearerAuth")
public class TrainingPlanController {

    private final TrainingPlanService trainingPlanService;

    public TrainingPlanController(TrainingPlanService trainingPlanService) {
        this.trainingPlanService = trainingPlanService;
    }

    @PostMapping
    public ResponseEntity<TrainingPlanResponse> create(
            @Valid @RequestBody CreateTrainingPlanRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trainingPlanService.create(request, user));
    }

    @GetMapping("/active")
    public TrainingPlanResponse active(@AuthenticationPrincipal User user) {
        return trainingPlanService.findActive(user);
    }

    @PatchMapping("/{id}/status")
    public TrainingPlanResponse updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTrainingPlanStatusRequest request,
            @AuthenticationPrincipal User user
    ) {
        return trainingPlanService.updateStatus(id, request, user);
    }
}
