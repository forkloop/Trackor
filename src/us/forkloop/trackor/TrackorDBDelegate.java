package us.forkloop.trackor;

import us.forkloop.trackor.db.Tracking;

public interface TrackorDBDelegate {

    public void addTracking(final Tracking tracking);
    public void updateTracking(final long id, final String newTag);
}
