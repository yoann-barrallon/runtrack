package fr.yoannbarrallon.runtrack.run;

import fr.yoannbarrallon.runtrack.auth.User;
import fr.yoannbarrallon.runtrack.run.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class RunService {

    private final RunSessionRepository runSessionRepository;
    private final FitActivityParser fitActivityParser;

    public RunService(RunSessionRepository runSessionRepository, FitActivityParser fitActivityParser) {
        this.runSessionRepository = runSessionRepository;
        this.fitActivityParser = fitActivityParser;
    }

    @Transactional
    public RunResponse create(CreateRunRequest request, User user) {
        RunSession runSession = RunSession.builder()
                .user(user)
                .title(request.title())
                .startTime(request.startTime())
                .durationSeconds(request.durationSeconds())
                .distanceMeters(request.distanceMeters())
                .elevationGainMeters(request.elevationGainMeters())
                .averagePaceSecondsPerKm(calculateAveragePaceSecondsPerKm(
                        request.durationSeconds(),
                        request.distanceMeters()
                ))
                .sourceType("MANUAL")
                .build();

        return toResponse(runSessionRepository.save(runSession));
    }

    @Transactional
    public RunResponse importFit(MultipartFile file, User user) {
        FitActivityParser.FitActivityData activity = fitActivityParser.parse(file);
        String title = resolveTitle(file);

        RunSession runSession = RunSession.builder()
                .user(user)
                .title(title)
                .startTime(activity.startTime())
                .durationSeconds(toInt(activity.durationSeconds(), "duration"))
                .distanceMeters(toInt(activity.distanceMeters(), "distance"))
                .elevationGainMeters(activity.elevationGainMeters())
                .averagePaceSecondsPerKm(calculateAveragePaceSecondsPerKm(
                        toInt(activity.durationSeconds(), "duration"),
                        toInt(activity.distanceMeters(), "distance")
                ))
                .averageHeartRate(activity.averageHeartRate())
                .sourceType("FIT")
                .build();

        int splitNumber = 1;
        for (FitActivityParser.FitSplitData split : activity.splits()) {
            int durationSeconds = toInt(split.durationSeconds(), "split duration");
            int distanceMeters = toInt(split.distanceMeters(), "split distance");
            runSession.addSplit(RunSplit.builder()
                    .splitNumber(splitNumber++)
                    .durationSeconds(durationSeconds)
                    .averagePaceSecondsPerKm(calculateAveragePaceSecondsPerKm(
                            durationSeconds,
                            distanceMeters
                    ))
                    .elevationGainMeters(split.elevationGainMeters())
                    .build());
        }

        return toResponse(runSessionRepository.save(runSession));
    }

    @Transactional(readOnly = true)
    public Page<RunResponse> findAll(User user, Pageable pageable) {
        return runSessionRepository.findAllByUserOrderByStartTimeDesc(user, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public RunResponse findById(UUID id, User user) {
        return runSessionRepository.findByIdAndUser(id, user)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Run not found"));
    }

    @Transactional
    public RunResponse update(UUID id, UpdateRunRequest request, User user) {
        RunSession runSession = getOwnedRun(id, user);
        runSession.setTitle(request.title());
        runSession.setStartTime(request.startTime());
        runSession.setDurationSeconds(request.durationSeconds());
        runSession.setDistanceMeters(request.distanceMeters());
        runSession.setElevationGainMeters(request.elevationGainMeters());
        runSession.setAveragePaceSecondsPerKm(calculateAveragePaceSecondsPerKm(
                request.durationSeconds(),
                request.distanceMeters()
        ));

        return toResponse(runSessionRepository.save(runSession));
    }

    @Transactional
    public void delete(UUID id, User user) {
        RunSession runSession = getOwnedRun(id, user);
        runSessionRepository.delete(runSession);
    }

    static int calculateAveragePaceSecondsPerKm(int durationSeconds, int distanceMeters) {
        return (int) Math.round((double) durationSeconds * 1000 / distanceMeters);
    }

    private RunSession getOwnedRun(UUID id, User user) {
        return runSessionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Run not found"));
    }

    private String resolveTitle(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            return "Imported FIT activity";
        }

        int extensionIndex = filename.lastIndexOf('.');
        return extensionIndex > 0 ? filename.substring(0, extensionIndex) : filename;
    }

    private int toInt(long value, String fieldName) {
        if (value <= 0 || value > Integer.MAX_VALUE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "FIT " + fieldName + " is outside the supported range"
            );
        }
        return (int) value;
    }

    private RunResponse toResponse(RunSession runSession) {
        List<RunSplitResponse> splits = runSession.getSplits().stream()
                .map(split -> new RunSplitResponse(
                        split.getId(),
                        split.getSplitNumber(),
                        split.getDurationSeconds(),
                        split.getAveragePaceSecondsPerKm(),
                        split.getElevationGainMeters()
                ))
                .toList();

        return new RunResponse(
                runSession.getId(),
                runSession.getTitle(),
                runSession.getStartTime(),
                runSession.getDurationSeconds(),
                runSession.getDistanceMeters(),
                runSession.getElevationGainMeters(),
                runSession.getAveragePaceSecondsPerKm(),
                runSession.getAverageHeartRate(),
                runSession.getSourceType(),
                runSession.getCreatedAt(),
                splits
        );
    }
}
