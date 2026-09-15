package fr.yoannbarrallon.runtrack.run.fit;

import com.garmin.fit.LapMesg;
import com.garmin.fit.LapMesgListener;
import com.garmin.fit.SessionMesg;
import com.garmin.fit.SessionMesgListener;

import java.util.ArrayList;
import java.util.List;

final class FitMessageCollector implements SessionMesgListener, LapMesgListener {

    private final List<LapMesg> laps = new ArrayList<>();
    private SessionMesg session;

    @Override
    public void onMesg(SessionMesg session) {
        if (this.session == null) {
            this.session = session;
        }
    }

    @Override
    public void onMesg(LapMesg lap) {
        laps.add(lap);
    }

    SessionMesg session() {
        return session;
    }

    List<LapMesg> laps() {
        return laps;
    }
}
