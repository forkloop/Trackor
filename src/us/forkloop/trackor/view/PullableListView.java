package us.forkloop.trackor.view;

import us.forkloop.trackor.R;
import android.content.Context;
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

    public PullableListView(Context context) {
        super(context);
    }

    public PullableListView(Context context, AttributeSet attributes) {
        super(context, attributes);
        this.context = context;
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
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (!isLongClicked) {
            Log.d(TAG, String.format("Long click position: %d id: %d", position, id));
            overlay = view.findViewById(R.id.archive);
            //overlay.setOnClickListener(new ArchiveClickListener());
            overlay.bringToFront();
            view.invalidate();
            longClickedView = view;
            isLongClicked = true;
        }
        return true;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (isLongClicked) {
            View v = longClickedView.findViewById(R.id.carrier);
            v.bringToFront();
            //overlay.setOnClickListener(null);
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
        }
    }
}
