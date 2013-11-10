package us.forkloop.trackor;

import us.forkloop.trackor.util.SwipeReturnGesture;
import us.forkloop.trackor.view.SettingsFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.MotionEvent;

public class SettingsActivity extends FragmentActivity implements SwipeReturnGesture.SwipeReturnGestureListener {

    public static final String KEY_AUTO_FOCUS = "auto";
    public static final String KEY_DISABLE_CONTINUOUS_FOCUS = "continuous";
    public static final String KEY_INVERT_SCAN = "invert";
    public static final String KEY_FRONT_LIGHT_MODE = "front";

    private SwipeReturnGesture gesture;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        gesture = new SwipeReturnGesture(this, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            onBackPressed();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gesture.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        // gesture.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public void onSwipe() {
        onBackPressed();
    }

}