package us.forkloop.trackor;

import java.util.List;

import us.forkloop.trackor.trackable.FedExTrack;
import us.forkloop.trackor.trackable.LASERSHIPTrack;
import us.forkloop.trackor.trackable.Trackable;
import us.forkloop.trackor.trackable.UPSTrack;
import us.forkloop.trackor.trackable.USPSTrack;
import us.forkloop.trackor.util.DetailTrackingAdapter;
import us.forkloop.trackor.util.Event;
import us.forkloop.trackor.util.TrackorNetworking;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class DetailActivity extends Activity {

    private final String TAG = getClass().getSimpleName();
    private static final float FLING_THRESHOLD = 100;
    // TODO width & height
    private final String MAP_ENDPOINT = "https://maps.googleapis.com/maps/api/staticmap?center=%s&zoom=15&size=1000x300&sensor=false";
    private GestureDetectorCompat detector;
    private TrackorApp app;
    private ImageView map;
    private ProgressBar progressBar;
    private View defaultView;

    private String carrier;
    private String tNumber;
    private boolean isChecking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.progress);

        map = (ImageView) findViewById(R.id.map);
        map.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // FIXME
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=New+York,NY&z=18"));
                startActivity(intent);
            }
        });

        app = TrackorApp.getInstance(getApplicationContext());
        detector = new GestureDetectorCompat(this, new SwipeGestureListener());

        Intent intent = getIntent();
        carrier = intent.getStringExtra("carrier");
        tNumber = intent.getStringExtra("tnumber");
        check();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        case R.id.action_refresh:
            check();
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

    private class GetMapTask extends AsyncTask<String, Void, Void> {
        private Bitmap bitmap;

        @Override
        protected void onPostExecute(Void v) {
            map.setImageBitmap(bitmap);
        }

        @Override
        protected Void doInBackground(String... args) {
            if (args.length == 0) {
                return null;
            }

            bitmap = new TrackorNetworking().downloadImage(args[0]);
            return null;
        }
    }

    private void check() {
        if (carrier != null && tNumber != null && !isChecking) {
            isChecking = true;
            progressBar.setVisibility(View.VISIBLE);
            if (app.isConnected()) {
                (new CheckStatusAsyncTask()).execute(new String[]{carrier, tNumber});
            } else {
                Toast.makeText(this, "Network disconnected!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class CheckStatusAsyncTask extends AsyncTask<String, Void, List<Event>> {

        @Override
        protected void onPostExecute(List<Event> events) {
            if (events != null && events.size() > 0) {
                render(events);
            } else {
                defaultView = findViewById(R.id.default_detail);
                defaultView.setVisibility(View.VISIBLE);
            }
            progressBar.setVisibility(View.GONE);
            isChecking = false;
        }
        
        @Override
        protected List<Event> doInBackground(String... args) {
            String carrier = args[0];
            String tNumber = args[1];
            Log.d(TAG, "Start to request status from " + carrier + " " + tNumber);
            Trackable trackable = null;
            if ("USPS".equals(carrier)) {
                trackable = new USPSTrack();
            } else if ("UPS".equals(carrier)) {
                trackable = new UPSTrack();
            } else if ("FedEx".equals(carrier)) {
                // need context to load template from asset folder
                trackable = new FedExTrack(getApplicationContext());
            } else if ("LASERSHIP".equals(carrier)) {
                trackable = new LASERSHIPTrack();
            } else {
                Log.e(TAG, "Unknown carrier " + carrier);
                return null;
            }
            List<Event> events = trackable.track(tNumber);
            return events;
        }
    }

    // TODO fix archive button gone
    private void render(List<Event> events) {
        if (defaultView != null) {
            defaultView.setVisibility(View.GONE);
        }
        // TODO fix if zipcode not exists
        String url = String.format(MAP_ENDPOINT, events.get(0).getZipcode());
        Log.d(TAG, "get map with " + url);
        (new GetMapTask()).execute(url);

        ListView listView = (ListView) findViewById(R.id.detail_tracking_list);
        listView.setAdapter(new DetailTrackingAdapter(this, R.layout.detail_tracking_record, events));
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        
        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
            Log.d(TAG, "onFling: " + e1 + ", " + e2);
            double diff = Math.abs(e1.getY() - e2.getY());
            if (diff < FLING_THRESHOLD) {
                onBackPressed();
                return true;
            }
            return false;
        }
    }
}
