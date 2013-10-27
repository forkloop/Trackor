package us.forkloop.trackor.view;

import us.forkloop.trackor.MainActivity;
import us.forkloop.trackor.R;
import us.forkloop.trackor.util.QuickReturn;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.TextView;

public class PullableListView extends ListView implements OnScrollListener, OnItemLongClickListener {

    public static final String TRACKING_NUMBER_KEY = "tnumber";
    public static final String TRACKING_TAG_KEY = "tag";
    private final String TAG = getClass().getSimpleName();
    private final int MAX_OVER_SCROLL = 200;

    private LayoutInflater inflater;
    private MainActivity parent;

    // long click state
    private TextView archiveView;
    private TextView tagView;
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
        this.parent = (MainActivity) context;
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
            View overlay = view.findViewById(R.id.tracking_action);
            overlay.bringToFront();
            view.invalidate();
            TextView tv = (TextView) view.findViewById(R.id.tracking_number);
            String trackingNumber = tv.getText().toString();
            archiveView = (TextView) view.findViewById(R.id.archive);
            archiveView.setOnClickListener(new ArchiveClickListener(trackingNumber));
            tagView = (TextView) view.findViewById(R.id.add_label);
            tagView.setOnClickListener(new TagClickListener(trackingNumber));
            longClickedView = view;
            isLongClicked = true;
            return true;
        }
        return false;
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
            hideKeyboard();
        }
        if (isLongClicked) {
            View v = longClickedView.findViewById(R.id.tracking_info);
            v.bringToFront();
            //pass null as click listener will still consume the click event.
            //overlay.setOnClickListener(null);
            archiveView.setClickable(false);
            tagView.setClickable(false);
            longClickedView.invalidate();
            isLongClicked = false;
        }
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) parent.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private class TagClickListener implements OnClickListener {
        private String trackingNumber;

        public TagClickListener(final String trackingNumber) {
            this.trackingNumber = trackingNumber;
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "clicked " + v);
            Bundle bundle = new Bundle();
            bundle.putString(TRACKING_NUMBER_KEY, trackingNumber);
            String tag = ((TextView) longClickedView.findViewById(R.id.tracking_tag)).getText().toString();
            bundle.putString(TRACKING_TAG_KEY, tag);
            bundle.putString("action", "update");
            DialogFragment dialog = new TrackorAddTagDialogFragment();
            dialog.setArguments(bundle);
            dialog.show(parent.getFragmentManager(), "");
        }
    }

    private class ArchiveClickListener implements OnClickListener {
        private String trackingNumber;

        public ArchiveClickListener(final String trackingNumber) {
            this.trackingNumber = trackingNumber;
        }

        @Override
        public void onClick(View v) {
            Bundle bundle = new Bundle();
            bundle.putString(TRACKING_NUMBER_KEY, trackingNumber);
            DialogFragment dialog = new TrackorArchiveDialogFragment();
            dialog.setArguments(bundle);
            dialog.show(parent.getFragmentManager(), "");

        }
    }
}
