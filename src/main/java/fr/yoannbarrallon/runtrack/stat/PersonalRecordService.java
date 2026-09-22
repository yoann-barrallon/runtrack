package fr.yoannbarrallon.runtrack.stat;

import fr.yoannbarrallon.runtrack.run.RunSession;
import fr.yoannbarrallon.runtrack.run.RunRepository;
import fr.yoannbarrallon.runtrack.auth.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonalRecordService {

    private final PersonalRecordRepository personalRecordRepository;
    private final RunRepository runRepository;

    public PersonalRecordService(
            PersonalRecordRepository personalRecordRepository,
            RunRepository runRepository
    ) {
        this.personalRecordRepository = personalRecordRepository;
        this.runRepository = runRepository;
    }
    @Transactional
    public void recalculateRecords(User user) {
        personalRecordRepository.deleteAllByUserId(user.getId());
        runRepository.findAllByUserOrderByStartTimeAsc(user)
                .forEach(this::detectRecords);
    }

    @Transactional
    public void detectRecords(RunSession runSession) {
        for (RecordDistanceType distanceType : RecordDistanceType.values()) {
            if (runSession.getDistanceMeters() < distanceType.distanceMeters()) {
                continue;
            }
            int timeSeconds = (int) Math.round(
                    (double) runSession.getDurationSeconds() * distanceType.distanceMeters()
                            / runSession.getDistanceMeters()
            );
            personalRecordRepository.findByUserIdAndDistanceType(runSession.getUser().getId(), distanceType)
                    .ifPresentOrElse(
                            record -> updateIfFaster(record, runSession, timeSeconds),
                            () -> createRecord(runSession, distanceType, timeSeconds)
                    );
        }
    }

    private void updateIfFaster(PersonalRecord record, RunSession runSession, int timeSeconds) {
        if (timeSeconds < record.getTimeSeconds()) {
            record.setRunSession(runSession);
            record.setTimeSeconds(timeSeconds);
            record.setAchievedAt(runSession.getStartTime());
            personalRecordRepository.save(record);
        }
    }

    private void createRecord(RunSession runSession, RecordDistanceType distanceType, int timeSeconds) {
        personalRecordRepository.save(PersonalRecord.builder()
                .user(runSession.getUser())
                .runSession(runSession)
                .distanceType(distanceType)
                .timeSeconds(timeSeconds)
                .achievedAt(runSession.getStartTime())
                .build());
    }
}
