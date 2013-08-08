package us.forkloop.trackor;

import us.forkloop.trackor.db.DatabaseHelper;
import us.forkloop.trackor.db.Tracking.TrackingColumn;
import us.forkloop.trackor.view.PullableListView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MainActivity extends Activity {

    final String TAG = getClass().getSimpleName();
    final int TRACKING_NAME_COLUMN_INDEX = 2;
    private DatabaseHelper dbHelper;
    
    private Context context;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        
        PullableListView listView = getListView();
        listView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        View header = getLayoutInflater().inflate(R.layout.fillin_view, null);
        listView.addHeaderView(header);

        dbHelper = new DatabaseHelper(this);
        Cursor cursor = dbHelper.getTrackings();
        String[] from = {TrackingColumn.COLUMN_CARRIER};
        int[] to = { R.id.carrier };
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.tracking_record_layout, cursor, from, to, 0);
        adapter.setViewBinder(new TrackingViewBinder());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new TrackingClickListener());
        listView.setOnItemLongClickListener(new TrackingLongClickListener());
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
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
        case R.id.action_add:
            Log.d(TAG, "add new tracking.");
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private PullableListView getListView() {
        return (PullableListView)findViewById(R.id.list);
    }



    private class TrackingClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            startActivity(new Intent(context, DetailActivity.class));
            overridePendingTransition(R.anim.slide_in_right, android.R.anim.slide_out_right);
        }
    }
    
    private class TrackingLongClickListener implements OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Log.d(TAG, String.format("Long click position: %d id: %d", position, id));
            return true;
        }
        
    }
    
    private class TrackingViewBinder implements SimpleCursorAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            Log.d(TAG, "" + columnIndex);
            String carrier = cursor.getString(columnIndex);
            if (columnIndex == TRACKING_NAME_COLUMN_INDEX) {
                if (carrier.equals("UPS")) {
                    view.setBackgroundColor(Color.parseColor("#d35400"));
                } else if (carrier.equals("Fedex")) {
                    view.setBackgroundColor(Color.parseColor("#34495e"));
                } else if (carrier.equals("USPS")) {
                    view.setBackgroundColor(Color.parseColor("#3498db"));
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
}
