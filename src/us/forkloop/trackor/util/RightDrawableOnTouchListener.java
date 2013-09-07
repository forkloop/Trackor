package us.forkloop.trackor.util;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;

public abstract class RightDrawableOnTouchListener implements OnTouchListener {

    private int fuzz = 10;
    private Drawable drawable;

    public RightDrawableOnTouchListener(EditText view) {
        super();
        drawable = view.getCompoundDrawables()[2];
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (drawable != null && event.getAction() == MotionEvent.ACTION_UP) {
            final int x = (int) event.getX();
            final int y = (int) event.getY();
            final Rect bounds = drawable.getBounds();
            if (x >= (v.getRight() - bounds.width() - fuzz) && x <= (v.getRight() - v.getPaddingRight() + fuzz)
                    && y >= (v.getPaddingTop() - fuzz) && y <= (v.getHeight() - v.getPaddingBottom()) + fuzz) {
                return onDrawableTouch(event);
            }
        }
        return false;
    }

    public abstract boolean onDrawableTouch(MotionEvent event);
}
