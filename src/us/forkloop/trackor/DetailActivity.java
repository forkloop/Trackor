package us.forkloop.trackor;

import java.util.ArrayList;
import java.util.List;

import us.forkloop.trackor.db.TrackRecord;
import us.forkloop.trackor.trackable.FedExTrack;
import us.forkloop.trackor.trackable.Trackable;
import us.forkloop.trackor.trackable.UPSTrack;
import us.forkloop.trackor.trackable.USPSTrack;
import us.forkloop.trackor.util.DetailTrackingAdapter;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DetailActivity extends Activity {

    private final String TAG = getClass().getSimpleName();
    private GestureDetectorCompat detector;
    private TrackorApp app;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        context = this;

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        app = TrackorApp.getInstance(getApplicationContext());
        detector = new GestureDetectorCompat(this, new SwipeGestureListener());

        mockTrackingDetail();

        Intent intent = getIntent();
        String carrier = intent.getStringExtra("carrier");
        if (carrier != null) {
            if (app.isConnected()) {
        //        (new CheckStatusAsyncTask()).execute(new String[]{carrier});
            } else {
                Toast.makeText(this, "Network disconnected!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    
    private class CheckStatusAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute (String result) {
            if (isTrackingAvailable()) {

            } else {
                TextView tv = (TextView) findViewById(R.id.detail);
                tv.setText(result);
                tv.setVisibility(View.VISIBLE);
            }
        }
        
        @Override
        protected String doInBackground(String... args) {
            String carrier = args[0];
            Log.d(TAG, "Start to request status from " + carrier);
            Trackable trackable = null;
            if ("USPS".equals(carrier)) {
                trackable = new USPSTrack();
            } else if ("UPS".equals(carrier)) {
                trackable = new UPSTrack();
            } else if ("FedEx".equals(carrier)) {
                // need context to load template from asset folder
                trackable = new FedExTrack(getApplicationContext());
            } else {
                return "Unknown carrier: " + carrier;
            }
            String result = trackable.track("");
            return result;
        }
        
    }

    private boolean isTrackingAvailable() {
        return false;
    }

    private void mockTrackingDetail() {
        int n = 5;
        ListView listView = (ListView) findViewById(R.id.detail_tracking_list);
        List<TrackRecord> records = new ArrayList<TrackRecord>(n);
        for (int i = 0; i < n; i++) {
            records.add(new TrackRecord());
        }
        listView.setAdapter(new DetailTrackingAdapter(this, R.layout.detail_tracking_record, records));
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
