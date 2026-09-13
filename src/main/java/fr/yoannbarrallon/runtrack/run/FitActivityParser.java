package fr.yoannbarrallon.runtrack.run;

import com.garmin.fit.Decode;
import com.garmin.fit.FitRuntimeException;
import com.garmin.fit.MesgBroadcaster;
import com.garmin.fit.SessionMesg;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

import org.springframework.stereotype.Component;

@Component
public class FitActivityParser {

    public FitActivityData parse(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FitImportException("The FIT file is empty");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Decode decode = new Decode();
            MesgBroadcaster broadcaster = new MesgBroadcaster(decode);
            SessionCollector sessionCollector = new SessionCollector();
            broadcaster.addListener(sessionCollector::collect);

            if (!decode.read(inputStream, broadcaster, broadcaster) || sessionCollector.session() == null) {
                throw new FitImportException("The FIT file does not contain a session");
            }

            return toActivityData(sessionCollector.session());
        } catch (IOException exception) {
            throw new FitImportException("Unable to read the FIT file", exception);
        } catch (FitRuntimeException exception) {
            throw new FitImportException("The FIT file could not be decoded", exception);
        }
    }

    private FitActivityData toActivityData(SessionMesg session) {
        if (session.getStartTime() == null
                || session.getTotalTimerTime() == null
                || session.getTotalDistance() == null
                || session.getTotalTimerTime() <= 0
                || session.getTotalDistance() <= 0) {
            throw new FitImportException("The FIT session is missing duration, distance, or start time");
        }

        return new FitActivityData(
                session.getStartTime().getDate().toInstant(),
                Math.round(session.getTotalTimerTime()),
                Math.round(session.getTotalDistance()),
                session.getTotalAscent() == null ? 0 : session.getTotalAscent(),
                session.getAvgHeartRate() == null ? null : session.getAvgHeartRate().intValue()
        );
    }

    private static final class SessionCollector {
        private SessionMesg session;

        private void collect(SessionMesg session) {
            if (this.session == null) {
                this.session = session;
            }
        }

        private SessionMesg session() {
            return session;
        }
    }

    public record FitActivityData(
            Instant startTime,
            long durationSeconds,
            long distanceMeters,
            int elevationGainMeters,
            Integer averageHeartRate
    ) {
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class FitImportException extends RuntimeException {

        public FitImportException(String message) {
            super(message);
        }

        public FitImportException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
