package us.forkloop.trackor;

import us.forkloop.trackor.db.DatabaseHelper;
import us.forkloop.trackor.db.Tracking;
import us.forkloop.trackor.db.Tracking.TrackingColumn;
import us.forkloop.trackor.util.ImageTextAdapter;
import us.forkloop.trackor.util.QuickReturn;
import us.forkloop.trackor.util.TypefaceSpan;
import us.forkloop.trackor.view.PullableListView;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MainActivity extends Activity implements QuickReturn {

    final String TAG = getClass().getSimpleName();
    final int TRACKING_NAME_COLUMN_INDEX = 2;
    private DatabaseHelper dbHelper;
    private Cursor cursor;
    SimpleCursorAdapter adapter;
    private TrackorApp app;

    private BroadcastReceiver receiver;
    private Context context;
    private Spinner spinner;
    private PullableListView listView;
    private ActionBar actionBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FIXME
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main);

        context = this;
        Log.d(TAG, "context: " + context);
        Log.d(TAG, "intent start me:" + getIntent().getAction());

        app = TrackorApp.getInstance(context);

        receiver = new TrackorBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("ArchiveTracking");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        actionBar = getActionBar();
        customizeActionBar();

        listView = getListView();
        // delegate to toggle actionBar
        listView.setDelegate(this);

        listView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        View header = getLayoutInflater().inflate(R.layout.fillin_view, null);
        listView.addHeaderView(header);

        spinner = (Spinner) findViewById(R.id.fillin_spinner);
        String[] carriers = getResources().getStringArray(R.array.carriers);
        ImageTextAdapter carrierAdapter = new ImageTextAdapter(context, R.layout.carrier_spinner_row, carriers);
        spinner.setAdapter(carrierAdapter);

        EditText editText = (EditText) findViewById(R.id.fillin_tnumber);
        editText.setTypeface(app.getTypeface("Gotham-Book.otf"));
        editText.clearFocus();

        //FIXME
        editText.setImeActionLabel("Add", KeyEvent.KEYCODE_ENTER);
        editText.setOnEditorActionListener(new AddTrackingEvent());

        dbHelper = new DatabaseHelper(this);
        cursor = dbHelper.getTrackings();
        String[] from = {TrackingColumn.COLUMN_CARRIER};
        int[] to = { R.id.carrier };
        adapter = new SimpleCursorAdapter(this, R.layout.action_overlay, cursor, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        adapter.setViewBinder(new TrackingViewBinder());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new TrackingClickListener());
        listView.setOnItemLongClickListener(listView);
        listView.setOnScrollListener(listView);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        dbHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //LocalBroadcastManager.getInstance(this).registerReceiver(receiver, null);
    }

    @Override
    protected void onStop() {
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private PullableListView getListView() {
        return (PullableListView)findViewById(R.id.list);
    }


    private class TrackorBroadcastReceiver extends BroadcastReceiver {

        final String TAG = getClass().getSimpleName();
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Receiving " + action);
            if ("ArchiveTracking".equals(action)) {
                Toast.makeText(context, "Archiving...", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "" + intent.getIntExtra("id", -1));
            }
        }
    }

    private class TrackingClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(context, DetailActivity.class);
            TextView tv = (TextView) view.findViewById(R.id.carrier);
            intent.putExtra("carrier", tv.getText());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, android.R.anim.slide_out_right);
        }
    }
    
    private class TrackingViewBinder implements SimpleCursorAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            String carrier = cursor.getString(columnIndex);
            if (columnIndex == TRACKING_NAME_COLUMN_INDEX) {
                if (carrier.equals("UPS")) {
                    view.setBackgroundColor(Color.parseColor("#d35400"));
                } else if (carrier.equals("FedEx")) {
                    view.setBackgroundColor(Color.parseColor("#34495e"));
                } else if (carrier.equals("USPS")) {
                    view.setBackgroundColor(Color.parseColor("#3498db"));
                } else {
                    view.setBackgroundColor(Color.parseColor("#2ecc71"));
                }
                if ( view instanceof TextView ) {
                    ((TextView) view).setText(carrier);
                    Typeface font = Typeface.createFromAsset(getAssets(), "Lato-Reg.ttf");
                    ((TextView) view).setTypeface(font);
                }
                return true;
            }
            return false;
        }
    }

    private class AddTrackingAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute (String result) {
            Log.d(TAG, "refreshing...");
            adapter.changeCursor(cursor);
            adapter.notifyDataSetChanged();
        }

        @Override
        protected String doInBackground(String... args) {
            cursor = dbHelper.getTrackings();
            return null;
        }

    }

    private class AddTrackingEvent implements OnEditorActionListener {

        private void requery() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    cursor = dbHelper.getTrackings();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "refreshing...");
                            adapter.notifyDataSetChanged();
                            listView.invalidate();
                        }
                    });
                }
            }).start();
        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String tNumber = v.getText().toString();
                String carrier = spinner.getSelectedItem().toString();
                Log.d(TAG, "Adding " + carrier + ": " + tNumber);
                Tracking t = new Tracking(carrier, tNumber);
                dbHelper.addTracking(t);
                v.clearFocus();
                v.setText("");
                //requery();
                (new AddTrackingAsyncTask()).execute(new String[]{""});
                return true;
            }
            return false;
        }
    }

    @Override
    public void toggleActionBar(boolean hide) {
        if (hide) {
            actionBar.show();
        } else {
            actionBar.hide();
        }
    }

    private void customizeActionBar() {
        SpannableString s = new SpannableString(getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(this, "Gotham-Book.otf"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        actionBar.setTitle(s);
    }
}
