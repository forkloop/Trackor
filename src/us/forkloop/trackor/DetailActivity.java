package us.forkloop.trackor;

import us.forkloop.trackor.trackable.Trackable;
import us.forkloop.trackor.trackable.USPSTrack;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

public class DetailActivity extends Activity {

    private final String TAG = getClass().getSimpleName();
    private GestureDetectorCompat detector;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        
        detector = new GestureDetectorCompat(this, new SwipeGestureListener());
        
        //(new CheckStatusAsyncTask()).execute(new String[]{""});
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    
    
    private class CheckStatusAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute (String result) {
            TextView tv = (TextView) findViewById(R.id.detail);
            tv.setText(result);
        }
        
        @Override
        protected String doInBackground(String... args) {
            Trackable usps = new USPSTrack();
            String result = usps.track("");
            return result;
        }
        
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        
        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
            Log.d(TAG, "onFling: " + e1 + ", " + e2);
            onBackPressed();
            return true;
        }
    }
}
