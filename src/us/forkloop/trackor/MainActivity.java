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
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends Activity implements QuickReturn, TrackorDBDelegate {

    final String TAG = getClass().getSimpleName();
    final int TRACKING_NAME_COLUMN_INDEX = 1;
    final int TRACKING_CARRIER_COLUMN_INDEX = 2;
    private DatabaseHelper dbHelper;
    private Cursor cursor;
    SimpleCursorAdapter adapter;
    private TrackorApp app;

    private Context context;
    private Spinner spinner;
    private PullableListView listView;
    private ActionBar actionBar;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FIXME
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main);

        context = this;

        app = TrackorApp.getInstance(getApplicationContext());

        setupDrawer();

        IntentFilter filter = new IntentFilter();
        filter.addAction("ArchiveTracking");

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

        EditText editText = (EditText) findViewById(R.id.fillin_tnumber);
        editText.setTypeface(app.getTypeface("Gotham-Book.otf"));
        editText.clearFocus();
        editText.setOnTouchListener(new RightDrawableOnTouchListener(editText) {
            @Override
            public boolean onDrawableTouch(MotionEvent event) {
                Intent cameraIntent = new Intent(context, CameraActivity.class);
                startActivity(cameraIntent);
                return true;
            }
        });

        //FIXME
        editText.setImeActionLabel("Add", KeyEvent.KEYCODE_ENTER);
        editText.setOnEditorActionListener(new AddTrackingEvent());

        dbHelper = new DatabaseHelper(this);
        cursor = dbHelper.getAllTrackings();
        String[] from = {TrackingColumn.COLUMN_CARRIER, TrackingColumn.COLUMN_NAME, TrackingColumn.COLUMN_TRACKING_NUMBER};
        int[] to = { R.id.carrier, R.id.tracking_tag, R.id.tracking_number };
        adapter = new SimpleCursorAdapter(this, R.layout.action_overlay, cursor, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        adapter.setViewBinder(new TrackingViewBinder());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new TrackingClickListener());
        listView.setOnItemLongClickListener(listView);
        listView.setOnScrollListener(listView);

        // started by widget
        Log.d(TAG, "Started by --> " + getIntent().getAction());
        if (TrackorActions.CAMERA_ACTION.getAction().equals(getIntent().getAction())) {
            startActivity(new Intent(context, CameraActivity.class));
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
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);

        MenuItem showTypeItem = menu.findItem(R.id.action_showtype);
        Spinner spinner = (Spinner)showTypeItem.getActionView();
        String[] showType = getResources().getStringArray(R.array.show_type);
        ArrayAdapter<String> showTypeAdapter = new ArrayAdapter<String>(this, R.layout.showtype_spinner_row, showType) {
            @Override
            public View getView (int position, View convertView, ViewGroup parent) {
                return customizeView(position, convertView, parent);
            }

            @Override
            public View getDropDownView (int position, View convertView, ViewGroup parent) {
                return customizeView(position, convertView, parent);
            }

            private View customizeView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.showtype_spinner_row, null);
                }
                TextView tv = (TextView) convertView.findViewById(R.id.showtype_spinner_entry);
                tv.setText(getItem(position));
                tv.setTypeface(app.getTypeface("Gotham-Book.otf"));
                return convertView;
            }
        };
        spinner.setAdapter(showTypeAdapter);

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
            if (drawerToggle.onOptionsItemSelected(item)) return true; //handle touch on action bar icon
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void addTracking(final Tracking tracking) {
        TrackingWithAction trackingWithAction = new TrackingWithAction(tracking, Action.Add);
        (new TrackingDBAsyncTask()).execute(new TrackingWithAction[]{trackingWithAction});
    }

    @Override
    public void updateTracking(final String trackingNumber, final String newTag) {
        Log.d(TAG, "update tracking " + trackingNumber + ": " + newTag);
        Tracking tracking = new Tracking(null, trackingNumber, newTag);
        TrackingWithAction trackingWithAction = new TrackingWithAction(tracking, Action.Update);
        (new TrackingDBAsyncTask()).execute(new TrackingWithAction[]{trackingWithAction});
    }

    @Override
    public void archiveTracking(final String trackingNumber) {
        final Tracking tracking = new Tracking(null, trackingNumber, null);
        TrackingWithAction trackingWithAction = new TrackingWithAction(tracking, Action.Archive);
        (new TrackingDBAsyncTask()).execute(new TrackingWithAction[]{trackingWithAction});
    }

    private class TrackingDBAsyncTask extends AsyncTask<TrackingWithAction, String, Cursor> {

        @Override
        protected void onPostExecute (final Cursor cursor) {
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
                }
                cursor = dbHelper.getAllTrackings();
                return cursor;
            }
            return null;
        }
    }

    private class TrackingClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(context, DetailActivity.class);
            TextView tv = (TextView) view.findViewById(R.id.carrier);
            intent.putExtra("carrier", tv.getText());
            tv = (TextView) view.findViewById(R.id.tracking_number);
            intent.putExtra("tnumber", tv.getText());
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
            if ( view instanceof TextView ) {
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

    private class DrawerClickListener implements ListView.OnItemClickListener {

        private final String TAG = getClass().getSimpleName();
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.d(TAG, "Clicked on " + position);
            loadFragment(position);
            drawerLayout.closeDrawer(drawerListView);
        }

        private void loadFragment(int position) {
            Fragment fragment = null;

            if (position == 2) {
                fragment = new HelpFragment();
            } else if (position == 3) {
                fragment = new FeedbackFragment();
            } else {
                Log.e(TAG, "Clicked unknown fragment: " + position);
            }

            if (fragment != null) {
                fragment.setArguments(new Bundle());
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.drawer, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
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

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                                    R.drawable.ic_drawer, R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }

    private void setupDrawer() {
        // The first empty string is accommodating the action bar
        String[] drawerEntries = new String[] {"", "Settings", "Help", "Feedback", ""};
        drawerListView = (ListView)findViewById(R.id.drawer_list);
        try {
            TextView label = (TextView) getLayoutInflater().inflate(R.layout.typefaced_textview, null);
            String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            label.setText("Version " + version);
            label.setTypeface(app.getTypeface("Gotham-Book.otf"));
            label.setClickable(false);
            drawerListView.addFooterView(label);
        } catch (NameNotFoundException nfe) {
            Log.e(TAG, "Can not get package version, ", nfe);
        }
        drawerListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, drawerEntries));
        drawerListView.setOnItemClickListener(new DrawerClickListener());
    }
}
