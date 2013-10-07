package us.forkloop.trackor;

import us.forkloop.trackor.db.Tracking;

public interface TrackorDBDelegate {

    public void addTracking(Tracking tracking);
    public void updateTracking(Tracking tracking);
}
