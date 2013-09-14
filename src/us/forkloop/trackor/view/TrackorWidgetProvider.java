package us.forkloop.trackor.view;

import us.forkloop.trackor.MainActivity;
import us.forkloop.trackor.R;
import us.forkloop.trackor.util.TrackorActions;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class TrackorWidgetProvider extends AppWidgetProvider {

    private final String TAG = getClass().getSimpleName();

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setAction(TrackorActions.WIDGET_ACTION.getAction());
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            Intent cameraIntent = new Intent(context, MainActivity.class);
            cameraIntent.setAction(TrackorActions.CAMERA_ACTION.getAction());
            // intent to open camera after start the activity
            PendingIntent cameraPendingIntent = PendingIntent.getActivity(context, 1, cameraIntent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_layout);
            views.setOnClickPendingIntent(R.id.appwidget_tv, pendingIntent);
            views.setOnClickPendingIntent(R.id.appwidget_ib, cameraPendingIntent);

            appWidgetManager.updateAppWidget(id, views);
        }
    }

    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received " + intent.getAction());
        super.onReceive(context, intent);
    }
}