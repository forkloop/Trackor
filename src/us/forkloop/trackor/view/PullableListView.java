package us.forkloop.trackor.view;

import us.forkloop.trackor.MainActivity;
import us.forkloop.trackor.R;
import us.forkloop.trackor.util.QuickReturn;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

public class PullableListView extends ListView implements OnScrollListener, OnItemLongClickListener {

    private final String TAG = getClass().getSimpleName();
    private final int MAX_OVER_SCROLL = 200;

    private LayoutInflater inflater;
    private Context context;

    // long click state
    private View overlay;
    private View longClickedView;
    private boolean isLongClicked;

    // scroll state
    private QuickReturn delegate;
    private int sPosition;
    private int sOffset;

    public PullableListView(Context context) {
        super(context);
    }

    public PullableListView(Context context, AttributeSet attributes) {
        super(context, attributes);
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setDelegate(QuickReturn delegate) {
        this.delegate = delegate;
    }

    @Override
    protected boolean overScrollBy (int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, 
            int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(0, deltaY, 0, scrollY, 0, scrollRangeY, 0, MAX_OVER_SCROLL, isTouchEvent);
    }

    @Override
    protected void onOverScrolled (int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        // position > 0 is to no-op the long click on header
        if (!isLongClicked && position > 0) {
            Log.d(TAG, String.format("Long click position: %d id: %d", position, id));
            overlay = view.findViewById(R.id.archive);
            overlay.setOnClickListener(new ArchiveClickListener());
            overlay.bringToFront();
            view.invalidate();
            longClickedView = view;
            isLongClicked = true;
        }
        return true;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Log.d(TAG, "" + firstVisibleItem + ":" + visibleItemCount + ":" + totalItemCount);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        Log.d(TAG, "scrollState: " + scrollState + " position: " + view.getFirstVisiblePosition() + " top: " + view.getChildAt(0).getTop());
        if (scrollState != OnScrollListener.SCROLL_STATE_IDLE) {
            int position = view.getFirstVisiblePosition();
            View v = view.getChildAt(0);
            int offset = v == null ? 0 : v.getTop();
            if (sPosition < position || (sPosition == position && offset > sOffset)) {
                // show the actionbar
                delegate.toggleActionBar(false);
            } else {
                delegate.toggleActionBar(true);
            }
        }
        if (isLongClicked) {
            View v = longClickedView.findViewById(R.id.carrier);
            v.bringToFront();
            //pass null as click listener will still consume the click event.
            //overlay.setOnClickListener(null);
            overlay.setClickable(false);
            longClickedView.invalidate();
            isLongClicked = false;
        }
        hideKeyboard();
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private class ArchiveClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            Log.d(TAG, "clicked " + v);
            Intent intent = new Intent();
            intent.setAction("ArchiveTracking");
            intent.putExtra("id", 1);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }
}
