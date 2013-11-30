package us.forkloop.trackor.util;

import us.forkloop.trackor.db.DatabaseHelper;
import us.forkloop.trackor.db.Tracking.TrackingColumn;
import us.forkloop.trackor.trackable.FedExTrack;
import us.forkloop.trackor.trackable.LASERSHIPTrack;
import us.forkloop.trackor.trackable.Trackable;
import us.forkloop.trackor.trackable.UPSTrack;
import us.forkloop.trackor.trackable.USPSTrack;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

public class TrackorSyncReceiver extends BroadcastReceiver {

    public static final String ACTION = "us.forkloop.trackor.UPDATE_TRACKINGS";
    private static final String TAG = "TrackorSyncReceiver";
    private int deliveredCount = 0;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d(TAG, "Receive action " + intent.getAction());
        new Thread(new Runnable() {
            @Override
            public void run() {
                update(context);
                if (deliveredCount > 0) {
                    TrackorNotificationManager manager = new TrackorNotificationManager(context);
                    manager.notify("Trackor", "Horay, you have " + deliveredCount + " packages delivered!");
                }
            }
        }).start();
    }

    private void update(Context context) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        Cursor cursor = dbHelper.getActiveOnTheWayTrackings();
        Log.i(TAG, "Ready to update status for " + cursor.getCount() + " trackings.");
        while (cursor.moveToNext()) {
            String carrier = cursor.getString(cursor.getColumnIndex(TrackingColumn.COLUMN_CARRIER));
            String trackingNumber = cursor.getString(cursor.getColumnIndex(TrackingColumn.COLUMN_TRACKING_NUMBER));
            Trackable trackable = null;
            if ("LASERSHIP".equals(carrier)) {
                trackable = new LASERSHIPTrack();
            } else if ("UPS".equals(carrier)) {
                trackable = new UPSTrack();
            } else if ("FedEx".equals(carrier)) {
                trackable = new FedExTrack();
            } else if ("USPS".equals(carrier)) {
                trackable = new USPSTrack();
            }
            trackable.track(trackingNumber);
            dbHelper.updateTrackingStatus(trackingNumber, trackable.isDelivered(), trackable.rawStatus());
            if (trackable.isDelivered()) {
                deliveredCount++;
            }
        }
    }
}