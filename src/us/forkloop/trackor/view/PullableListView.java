package us.forkloop.trackor.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ListView;

public class PullableListView extends ListView {

    private final String TAG = getClass().getSimpleName();
    
    public PullableListView(Context context) {
        super(context);
    }

    public PullableListView(Context context, AttributeSet attributes) {
        super(context, attributes);
    }
    
    @Override
    protected void onOverScrolled (int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        Log.d(TAG, "overscroll " + scrollX);
    }
}
