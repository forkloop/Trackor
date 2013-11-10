package us.forkloop.trackor.util;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class SwipeReturnGesture extends SimpleOnGestureListener {

    private static final float FLING_DIFF_THRESHOLD = 100;
    private static final String TAG = "SwipeReturnGesture";

    private final SwipeReturnGestureListener listener;
    private final GestureDetector detector;

    public SwipeReturnGesture(final Context context, final SwipeReturnGestureListener listener) {
        this.detector = new GestureDetector(context, this);
        this.listener = listener;
    }

    public boolean onTouchEvent(final MotionEvent event) {
        return detector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(final MotionEvent event) {
        return true;
    }

    @Override
    public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
        final float ydiff = Math.abs(e2.getY() - e1.getY());
        // check if swipe from left to right
        final float xdiff = e2.getX() - e1.getX();
        Log.d(TAG, "fling ydiff " + ydiff);
        if (xdiff > 0 && ydiff < FLING_DIFF_THRESHOLD) {
            listener.onSwipe();
            return true;
        }
        return false;
    }

    public static interface SwipeReturnGestureListener {
        public void onSwipe();
    }
}