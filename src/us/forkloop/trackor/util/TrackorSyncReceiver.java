package us.forkloop.trackor.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TrackorSyncReceiver extends BroadcastReceiver {

    private static final String TAG = "TrackorSyncReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Receive action " + intent.getAction());
        TrackorNotificationManager manager = new TrackorNotificationManager(context);
        manager.notify("Trackor", intent.getAction());
    }

}
