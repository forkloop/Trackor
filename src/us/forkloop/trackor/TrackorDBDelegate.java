package us.forkloop.trackor;

import us.forkloop.trackor.db.Tracking;

public interface TrackorDBDelegate {

    public void addTracking(final Tracking tracking);
    public void updateTracking(final String trackingNumber, final String newTag);
    public void archiveTracking(final String trackingNumber);
}
