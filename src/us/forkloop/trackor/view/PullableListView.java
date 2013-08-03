package us.forkloop.trackor.view;

import us.forkloop.trackor.R;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

public class PullableListView extends ListView {

    private final String TAG = getClass().getSimpleName();
    private final int MAX_OVER_SCROLL = 200;

    private LayoutInflater inflater;
    private boolean hasHeader;
    
    public PullableListView(Context context) {
        super(context);
    }

    public PullableListView(Context context, AttributeSet attributes) {
        super(context, attributes);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    protected boolean overScrollBy (int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, 
            int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(0, deltaY, 0, scrollY, 0, scrollRangeY, 0, MAX_OVER_SCROLL, isTouchEvent);
    }

    @Override
    protected void onOverScrolled (int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        /*
        if ( !hasHeader && scrollY > MAX_OVER_SCROLL - 20 ) {
            Log.d(TAG, "Adding a header. " + inflater);
            hasHeader = true;
            View header = inflater.inflate(R.layout.fillin_view, null);
            addHeaderView(header);
            setAdapter(getAdapter());
        }
        */
    }
}
