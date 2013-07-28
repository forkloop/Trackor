package us.forkloop.trackor;

import us.forkloop.trackor.db.DatabaseHelper;
import us.forkloop.trackor.db.Tracking.TrackingColumn;
import us.forkloop.trackor.view.PullableListView;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends Activity {

    final String TAG = getClass().getSimpleName();

    private DatabaseHelper dbHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        PullableListView listView = getListView();
        listView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        View header = getLayoutInflater().inflate(R.layout.fillin_view, null);
        listView.addHeaderView(header);

        dbHelper = new DatabaseHelper(this);
        Cursor cursor = dbHelper.getTrackings();
        String[] from = {TrackingColumn.COLUMN_CARRIER};
        int[] to = {android.R.id.text1};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, from, to, 0);
        listView.setAdapter(adapter);
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
}
