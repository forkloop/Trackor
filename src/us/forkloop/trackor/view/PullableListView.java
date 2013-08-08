package us.forkloop.trackor.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

public class PullableListView extends ListView implements OnScrollListener, OnItemLongClickListener {

    private final String TAG = getClass().getSimpleName();
    private final int MAX_OVER_SCROLL = 200;

    private LayoutInflater inflater;
    private boolean hasHeader;
    
    // long click state
    private boolean isLongClicked;
    private int position;
    private Drawable background;

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


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, String.format("Long click position: %d id: %d", position, id));
        this.background = view.getBackground();
        view.setBackground(new ColorDrawable(Color.CYAN));
        this.position = position;
        this.isLongClicked = true;
        return true;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (this.isLongClicked) {
            View v = getChildAt(this.position);
            v.setBackground(this.background);
            this.isLongClicked = false;
        }
    }
}
