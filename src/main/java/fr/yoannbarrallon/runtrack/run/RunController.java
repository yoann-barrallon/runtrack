package fr.yoannbarrallon.runtrack.run;

import fr.yoannbarrallon.runtrack.auth.User;
import fr.yoannbarrallon.runtrack.run.dto.CreateRunRequest;
import fr.yoannbarrallon.runtrack.run.dto.RunResponse;
import fr.yoannbarrallon.runtrack.run.dto.UpdateRunRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/runs")
public class RunController {

    private final RunService runService;

    public RunController(RunService runService) {
        this.runService = runService;
    }

    @PostMapping
    public ResponseEntity<RunResponse> create(
            @Valid @RequestBody CreateRunRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(runService.create(request, user));
    }

    @PostMapping(value = "/import/fit", consumes = "multipart/form-data")
    public ResponseEntity<RunResponse> importFit(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(runService.importFit(file, user));
    }

    @GetMapping
    public Page<RunResponse> findAll(
            @AuthenticationPrincipal User user,
            Pageable pageable
    ) {
        return runService.findAll(user, pageable);
    }

    @GetMapping("/{id}")
    public RunResponse findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        return runService.findById(id, user);
    }

    @PutMapping("/{id}")
    public RunResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRunRequest request,
            @AuthenticationPrincipal User user
    ) {
        return runService.update(id, request, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        runService.delete(id, user);
    }
}
