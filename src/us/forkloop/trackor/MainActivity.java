package us.forkloop.trackor;

import us.forkloop.trackor.db.DatabaseHelper;
import us.forkloop.trackor.db.Tracking;
import us.forkloop.trackor.db.Tracking.TrackingColumn;
import us.forkloop.trackor.db.TrackingWithAction;
import us.forkloop.trackor.db.TrackingWithAction.Action;
import us.forkloop.trackor.util.ImageTextAdapter;
import us.forkloop.trackor.util.QuickReturn;
import us.forkloop.trackor.util.RightDrawableOnTouchListener;
import us.forkloop.trackor.util.TrackorActions;
import us.forkloop.trackor.util.TypefaceSpan;
import us.forkloop.trackor.view.PullableListView;
import us.forkloop.trackor.view.TrackorAddTagDialogFragment;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends Activity implements QuickReturn, TrackorDBDelegate {

    private static final String TAG = "MainActivity";
    private static final int CAPTURE_REQUEST_CODE = 10;
    static final String CODE = "code";

    final int TRACKING_NAME_COLUMN_INDEX = 1;
    final int TRACKING_CARRIER_COLUMN_INDEX = 2;
    final int TRACKING_STATUS_COLUMN_INDEX = 4;
    private DatabaseHelper dbHelper;
    private Cursor cursor;
    SimpleCursorAdapter adapter;
    private TrackorApp app;

    private Context context;
    private Spinner spinner;
    private PullableListView listView;
    private ActionBar actionBar;
    private EditText editText;

    private int showType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // FIXME
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main);

        context = this;

        app = TrackorApp.getInstance(getApplicationContext());

        actionBar = getActionBar();
        customizeActionBar();

        listView = (PullableListView) findViewById(R.id.list);
        // delegate to toggle actionBar
        listView.setDelegate(this);

        listView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        View header = getLayoutInflater().inflate(R.layout.fillin_view, null);
        listView.addHeaderView(header);

        spinner = (Spinner) findViewById(R.id.fillin_spinner);
        String[] carriers = getResources().getStringArray(R.array.carriers);
        ImageTextAdapter carrierAdapter = new ImageTextAdapter(this, R.layout.carrier_spinner_row, carriers);
        spinner.setAdapter(carrierAdapter);

        editText = (EditText) findViewById(R.id.fillin_tnumber);
        editText.setTypeface(app.getTypeface("Gotham-Book.otf"));
        editText.clearFocus();
        editText.setOnTouchListener(new RightDrawableOnTouchListener(editText) {
            @Override
            public boolean onDrawableTouch(MotionEvent event) {
                Intent captureIntent = new Intent(context, CaptureActivity.class);
                startActivityForResult(captureIntent, CAPTURE_REQUEST_CODE);
                return true;
            }
        });

        // FIXME
        editText.setImeActionLabel("Add", KeyEvent.KEYCODE_ENTER);
        editText.setOnEditorActionListener(new AddTrackingEvent());

        dbHelper = DatabaseHelper.getInstance(getApplicationContext());
        cursor = dbHelper.getActiveTrackings();
        showType = 0;
        String[] from = { TrackingColumn.COLUMN_CARRIER, TrackingColumn.COLUMN_NAME, TrackingColumn.COLUMN_TRACKING_NUMBER, TrackingColumn.COLUMN_IS_DELIVERED };
        int[] to = { R.id.carrier, R.id.tracking_tag, R.id.tracking_number, R.id.tracking_status };
        adapter = new SimpleCursorAdapter(this, R.layout.action_overlay, cursor, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        adapter.setViewBinder(new TrackingViewBinder());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new TrackingClickListener());
        listView.setOnItemLongClickListener(listView);
        listView.setOnScrollListener(listView);

        // started by widget
        Log.d(TAG, "Started by --> " + getIntent().getAction());
        if (TrackorActions.CAMERA_ACTION.getAction().equals(getIntent().getAction())) {
            startActivity(new Intent(this, CaptureActivity.class));
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);

        MenuItem showTypeItem = menu.findItem(R.id.action_showtype);
        Spinner spinner = (Spinner) showTypeItem.getActionView();
        String[] showType = getResources().getStringArray(R.array.show_type);
        ArrayAdapter<String> showTypeAdapter = new ArrayAdapter<String>(this, R.layout.showtype_spinner_row, showType) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return customizeView(position, convertView, parent);
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = customizeView(position, convertView, parent);
                view.setBackgroundColor(getResources().getColor(R.color.wet_asphalt));
                return view;
            }

            private View customizeView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.showtype_spinner_row, parent, false);
                }
                TextView tv = (TextView) convertView.findViewById(R.id.showtype_spinner_entry);
                tv.setText(getItem(position));
                tv.setTypeface(app.getTypeface("Gotham-Book.otf"));
                return convertView;
            }
        };
        spinner.setAdapter(showTypeAdapter);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            private boolean isFirst = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isFirst) {
                    isFirst = false;
                    return;
                }
                (new TrackingDBAsyncTask()).execute(new TrackingWithAction(null, Action.Switch));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_settings:
            startActivity(new Intent(this, SettingsActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String code = data.getStringExtra(CODE);
                if (editText != null) {
                    editText.setText(code);
                }
            } else {
                Log.d(TAG, "manually cancelled.");
            }
        }
    }

    @Override
    public void addTracking(final Tracking tracking) {
        TrackingWithAction trackingWithAction = new TrackingWithAction(tracking, Action.Add);
        (new TrackingDBAsyncTask()).execute(new TrackingWithAction[] { trackingWithAction });
    }

    @Override
    public void updateTracking(final String trackingNumber, final String newTag) {
        Log.d(TAG, "update tracking " + trackingNumber + ": " + newTag);
        Tracking tracking = new Tracking(null, trackingNumber, newTag, false);
        TrackingWithAction trackingWithAction = new TrackingWithAction(tracking, Action.Update);
        (new TrackingDBAsyncTask()).execute(new TrackingWithAction[] { trackingWithAction });
    }

    @Override
    public void archiveTracking(final String trackingNumber) {
        final Tracking tracking = new Tracking(null, trackingNumber, null, false);
        TrackingWithAction trackingWithAction = new TrackingWithAction(tracking, Action.Archive);
        (new TrackingDBAsyncTask()).execute(new TrackingWithAction[] { trackingWithAction });
    }

    private class TrackingDBAsyncTask extends AsyncTask<TrackingWithAction, String, Cursor> {

        @Override
        protected void onPostExecute(final Cursor cursor) {
            if (cursor != null) {
                Log.d(TAG, "refreshing trackings list...");
                adapter.changeCursor(cursor);
                adapter.notifyDataSetChanged();
                listView.onScrollStateChanged(listView, OnScrollListener.SCROLL_STATE_IDLE);
            }
        }

        @Override
        protected Cursor doInBackground(final TrackingWithAction... args) {
            if (args.length > 0) {
                TrackingWithAction trackingWithAction = args[0];
                Action action = trackingWithAction.getAction();
                Tracking tracking = trackingWithAction.getTracking();
                if (action == Action.Add) {
                    dbHelper.addTracking(tracking);
                } else if (action == Action.Archive) {
                    dbHelper.archiveTracking(tracking.getTrackingNumber());
                } else if (action == Action.Update) {
                    dbHelper.updateTrackingTag(tracking.getTrackingNumber(), tracking.getName());
                } else if (action == Action.Switch) {
                    Log.d(TAG, "switch show type " + showType);
                    if (showType == 0) {
                        showType = 1;
                        return dbHelper.getArchiveTrackings();
                    } else {
                        showType = 0;
                    }
                }
                cursor = dbHelper.getActiveTrackings();
                return cursor;
            }
            return null;
        }
    }

    private class TrackingClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(context, DetailActivity.class);
            Cursor cursor = (Cursor) (adapter.getItem(position - 1));
            String trackingNumber = cursor.getString(cursor.getColumnIndex(TrackingColumn.COLUMN_TRACKING_NUMBER));
            String carrier = cursor.getString(cursor.getColumnIndex(TrackingColumn.COLUMN_CARRIER));
            String name = cursor.getString(cursor.getColumnIndex(TrackingColumn.COLUMN_NAME));
            boolean isDelivered = cursor.getInt(cursor.getColumnIndex(TrackingColumn.COLUMN_IS_DELIVERED)) == 1 ? true : false;
            Tracking tracking = new Tracking(carrier, trackingNumber, name, isDelivered);
            intent.putExtra(DetailActivity.TRACKING, tracking);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, android.R.anim.slide_out_right);
        }
    }

    private class TrackingViewBinder implements SimpleCursorAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            String text = cursor.getString(columnIndex);
            if (columnIndex == TRACKING_NAME_COLUMN_INDEX) {
                text = text.toUpperCase();
            }
            if (view instanceof TextView) {
                ((TextView) view).setText(text);
                ((TextView) view).setTypeface(app.getTypeface("Gotham-Book.otf"));
            }
            if (columnIndex == TRACKING_CARRIER_COLUMN_INDEX) {
                if (text.equals("UPS")) {
                    view.setBackgroundColor(Color.parseColor("#d35400"));
                } else if (text.equals("FedEx")) {
                    view.setBackgroundColor(Color.parseColor("#34495e"));
                } else if (text.equals("USPS")) {
                    view.setBackgroundColor(Color.parseColor("#3498db"));
                } else if (text.equals("LASERSHIP")) {
                    view.setBackgroundColor(Color.parseColor("#e74c3c"));
                } else {
                    view.setBackgroundColor(Color.parseColor("#9b59b6"));
                }
            }
            if (columnIndex == TRACKING_STATUS_COLUMN_INDEX) {
                boolean isDelivered = cursor.getInt(columnIndex) == 1 ? true : false;
                if (isDelivered) {
                    view.setBackgroundColor(getResources().getColor(R.color.emerald));
                } else {
                    view.setBackgroundColor(getResources().getColor(R.color.carrot));
                }
            }
            return true;
        }
    }

    private class AddTrackingEvent implements OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                DialogFragment dialog = new TrackorAddTagDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString("carrier", spinner.getSelectedItem().toString());
                bundle.putString("tnumber", v.getText().toString());
                bundle.putString("action", "add");
                dialog.setArguments(bundle);
                dialog.show(getFragmentManager(), "");
                v.clearFocus();
                v.setText("");
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
