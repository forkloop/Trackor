package us.forkloop.trackor;

import us.forkloop.trackor.view.SettingsFragment;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

public class SettingsActivity extends Activity {

    public static final String KEY_AUTO_FOCUS = "auto";
    public static final String KEY_DISABLE_CONTINUOUS_FOCUS = "continuous";
    public static final String KEY_INVERT_SCAN = "invert";
    public static final String KEY_FRONT_LIGHT_MODE = "front";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

}