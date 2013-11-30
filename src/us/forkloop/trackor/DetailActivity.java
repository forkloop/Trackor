package us.forkloop.trackor;

import java.util.List;

import us.forkloop.trackor.db.DatabaseHelper;
import us.forkloop.trackor.db.Tracking;
import us.forkloop.trackor.trackable.FedExTrack;
import us.forkloop.trackor.trackable.LASERSHIPTrack;
import us.forkloop.trackor.trackable.Trackable;
import us.forkloop.trackor.trackable.UPSTrack;
import us.forkloop.trackor.trackable.USPSTrack;
import us.forkloop.trackor.util.DetailTrackingAdapter;
import us.forkloop.trackor.util.Event;
import us.forkloop.trackor.util.SwipeReturnGesture;
import us.forkloop.trackor.util.TrackorNetworking;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DetailActivity extends Activity implements SwipeReturnGesture.SwipeReturnGestureListener {

    public static final String TRACKING = "tracking";
    private static final String TAG = "DetailActivity";
    private static final String ARCHIVE = "Archive";
    // FIXME width & height
    private static final String MAP_ENDPOINT = "https://maps.googleapis.com/maps/api/staticmap?center=%s&zoom=15&size=1000x300&sensor=false";
    private static final String UPS_WEB_URL = "http://wwwapps.ups.com/etracking/tracking.cgi?TypeOfInquiryNumber=T&InquiryNumber1=";
    private static final String LASERSHIP_WEB_URL = "http://www.lasership.com/track/";
    private static final String USPS_WEB_URL = "https://tools.usps.com/go/TrackConfirmAction_input?origTrackNum=";
    private static final String FEDEX_WEB_URL = "https://www.fedex.com/fedextrack/?tracknumbers=";

    private DatabaseHelper dbHelper;
    private SwipeReturnGesture gesture;
    private TrackorApp app;
    private ImageView map;
    private ProgressBar progressBar;
    private View defaultView;

    Trackable trackable;
    private String webUrl;
    private String carrier;
    private String trackingNumber;
    private boolean isDelivered;
    private boolean isChecking;
    private Tracking tracking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        dbHelper = DatabaseHelper.getInstance(getApplicationContext());
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
        gesture = new SwipeReturnGesture(this, this);

        Intent intent = getIntent();
        tracking = (Tracking) intent.getParcelableExtra(TRACKING);
        carrier = tracking.getCarrier();
        trackingNumber = tracking.getTrackingNumber();
        isDelivered = tracking.isDelivered();
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
            onBackPressed();
            return true;
        case R.id.action_refresh:
            check();
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gesture.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
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
        if (carrier != null && trackingNumber != null && !isChecking) {
            isChecking = true;
            progressBar.setVisibility(View.VISIBLE);
            if (app.isConnected()) {
                (new CheckStatusAsyncTask()).execute();
            } else {
                Toast.makeText(this, "Network disconnected!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class CheckStatusAsyncTask extends AsyncTask<Void, Void, List<Event>> {
        @Override
        protected void onPostExecute(List<Event> events) {
            if (events != null && events.size() > 0) {
                renderSuccess(events);
                // TODO separate thread?
                updateDatabase();
            } else {
                renderFailure();
            }
            progressBar.setVisibility(View.GONE);
            isChecking = false;
        }

        @Override
        protected List<Event> doInBackground(Void... args) {
            Log.d(TAG, "Start to check status from " + carrier + " " + trackingNumber);
            if ("USPS".equals(carrier)) {
                // trackable = new USPSHTMLTrack();
                trackable = new USPSTrack();
                webUrl = USPS_WEB_URL + trackingNumber;
            } else if ("UPS".equals(carrier)) {
                trackable = new UPSTrack();
                webUrl = UPS_WEB_URL + trackingNumber;
            } else if ("FedEx".equals(carrier)) {
                trackable = new FedExTrack();
                webUrl = FEDEX_WEB_URL + trackingNumber;
            } else if ("LASERSHIP".equals(carrier)) {
                webUrl = LASERSHIP_WEB_URL + trackingNumber;
                trackable = new LASERSHIPTrack();
            } else {
                Log.e(TAG, "Unknown carrier " + carrier);
                return null;
            }
            List<Event> events;
            if (isDelivered) {
                Log.i(TAG, "Already delivered, so read status from db.");
                events = trackable.parse(dbHelper.getTrackingStatus(trackingNumber));
            } else {
                Log.i(TAG, "Not delivered yet, fetch status from some remote TCP/IP device.");
                events = trackable.track(trackingNumber);
            }
            return events;
        }
    }

    private void renderFailure() {
        if (defaultView == null) {
            defaultView = findViewById(R.id.default_detail);
            if (webUrl != null) {
                TextView defaultText = (TextView) findViewById(R.id.default_text);
                String info = defaultText.getText().toString();
                String html = info + "<a href=\"" + webUrl + "\"> Touch to view the detail tracking information here.";
                defaultText.setText(Html.fromHtml(html));
                defaultText.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
        defaultView.setVisibility(View.VISIBLE);
    }

    private void renderSuccess(List<Event> events) {
        if (defaultView != null) {
            defaultView.setVisibility(View.GONE);
        }

        // FIXME fix if zipcode not exists
        String url = String.format(MAP_ENDPOINT, events.get(0).getZipcode());
        Log.d(TAG, "get map with " + url);
        (new GetMapTask()).execute(url);

        ListView listView = (ListView) findViewById(R.id.detail_tracking_list);
        TextView tv = (TextView) getLayoutInflater().inflate(R.layout.typefaced_textview, null);
        tv.setBackgroundColor(Color.parseColor("#c0392b"));
        tv.setTextColor(Color.WHITE);
        tv.setText(ARCHIVE);
        listView.addFooterView(tv);
        listView.setAdapter(new DetailTrackingAdapter(this, R.layout.detail_tracking_record, events));
    }

    private void updateDatabase() {
        if (!isDelivered) { // if it is already delivered before, don't update status.
            dbHelper.updateTrackingStatus(trackingNumber, trackable.isDelivered(), trackable.rawStatus());
        }
    }

    @Override
    public void onSwipe() {
        onBackPressed();
    }
}