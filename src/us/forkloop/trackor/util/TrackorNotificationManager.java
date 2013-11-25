package us.forkloop.trackor.util;

import us.forkloop.trackor.MainActivity;
import us.forkloop.trackor.R;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class TrackorNotificationManager {
    private static final int SMALL_ICON = R.drawable.ic_launcher;
    private static final int NOTIFICATION_ID = 1;
    private static final int NOTIFICATION_REQUEST_CODE = 12;
    private final Context context;

    public TrackorNotificationManager(Context context) {
        this.context = context;
    }

    public void notify(String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(SMALL_ICON)
                .setContentTitle(title)
                .setContentText(text);
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, NOTIFICATION_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, builder.build());
    }

}
