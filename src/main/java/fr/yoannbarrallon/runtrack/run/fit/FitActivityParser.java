package fr.yoannbarrallon.runtrack.run.fit;

import com.garmin.fit.Decode;
import com.garmin.fit.FitRuntimeException;
import com.garmin.fit.LapMesg;
import com.garmin.fit.LapMesgListener;
import com.garmin.fit.MesgBroadcaster;
import com.garmin.fit.SessionMesg;
import com.garmin.fit.SessionMesgListener;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
            FitMessageCollector collector = new FitMessageCollector();
            broadcaster.addListener((SessionMesgListener) collector);
            broadcaster.addListener((LapMesgListener) collector);

            if (!decode.read(inputStream, broadcaster, broadcaster) || collector.session() == null) {
                throw new FitImportException("The FIT file does not contain a session");
            }

            return toActivityData(collector.session(), collector.laps());
        } catch (IOException exception) {
            throw new FitImportException("Unable to read the FIT file", exception);
        } catch (FitRuntimeException exception) {
            throw new FitImportException("The FIT file could not be decoded", exception);
        }
    }

    private FitActivityData toActivityData(SessionMesg session, List<LapMesg> laps) {
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
                session.getAvgHeartRate() == null ? null : session.getAvgHeartRate().intValue(),
                laps.stream()
                        .filter(lap -> lap.getTotalTimerTime() != null
                                && lap.getTotalDistance() != null
                                && lap.getTotalTimerTime() > 0
                                && lap.getTotalDistance() > 0)
                        .map(lap -> new FitSplitData(
                                Math.round(lap.getTotalTimerTime()),
                                Math.round(lap.getTotalDistance()),
                                lap.getTotalAscent() == null ? 0 : lap.getTotalAscent()
                        ))
                        .toList()
        );
    }

}
