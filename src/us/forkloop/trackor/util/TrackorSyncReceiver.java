package us.forkloop.trackor.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
    private static final int THREADS_NUM = 2;
    private static final int POLL_TIMEOUT = 60;
    private static final String TAG = "TrackorSyncReceiver";
    DatabaseHelper dbHelper;
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
        dbHelper = DatabaseHelper.getInstance(context);
        Cursor cursor = dbHelper.getActiveOnTheWayTrackings();
        Log.i(TAG, "Ready to update status for " + cursor.getCount() + " trackings.");
        ExecutorService executors = Executors.newFixedThreadPool(THREADS_NUM);
        CompletionService<Boolean> service = new ExecutorCompletionService<Boolean>(executors);
        int count = 0;
        while (cursor.moveToNext()) {
            String carrier = cursor.getString(cursor.getColumnIndex(TrackingColumn.COLUMN_CARRIER));
            String trackingNumber = cursor.getString(cursor.getColumnIndex(TrackingColumn.COLUMN_TRACKING_NUMBER));
            service.submit(new UpdateCallable(carrier, trackingNumber));
            count++;
        }
        for (int n = 0; n < count; n++) {
            try {
                Future<Boolean> result = service.poll(POLL_TIMEOUT, TimeUnit.SECONDS);
                if (result.get()) {
                    deliveredCount++;
                }
            } catch (InterruptedException ie) {
                Log.e(TAG, "interrputed while updating " + ie);
            } catch (ExecutionException ee) {
                Log.e(TAG, "error while getting update status " + ee);
            }
        }
        executors.shutdown();
    }

    private class UpdateCallable implements Callable<Boolean> {
        private final String carrier;
        private final String trackingNumber;

        public UpdateCallable(String carrier, String trackingNumber) {
            this.carrier = carrier;
            this.trackingNumber = trackingNumber;
        }

        @Override
        public Boolean call() throws Exception {
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
            return trackable.isDelivered();
        }
    }
}