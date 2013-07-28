package us.forkloop.trackor.db;

import us.forkloop.trackor.db.Tracking.TrackingColumn;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private final String TAG = getClass().getSimpleName();
    
    private static final int DB_VERSION    = 1;
    private static final String DB_NAME    = "trackings.db";
    private static final String CREATE_TABLE = 
            "CREATE TABLE " + TrackingColumn.TABLE_NAME + " ("
            + TrackingColumn._ID + " INTEGER PRIMARY KEY,"
            + TrackingColumn.COLUMN_NAME + " TEXT,"
            + TrackingColumn.COLUMN_CARRIER + " TEXT,"
            + TrackingColumn.COLUMN_TRACKING_NUMBER + " TEXT );";
    private static final String DROP_TABLE = 
            "DROP TABLE IF EXISTS " + TrackingColumn.TABLE_NAME;
    
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int v1, int v2) {
        Log.d(TAG, "Upgrade table from " + v1 + " to " + v2);
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

    public long addTracking(Tracking t) {
        Log.v(TAG, t.toString());
        
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TrackingColumn.COLUMN_CARRIER, t.getCarrier());
        values.put(TrackingColumn.COLUMN_NAME, t.getName());
        values.put(TrackingColumn.COLUMN_TRACKING_NUMBER, t.getTrackingNumber());
        
        long id = db.insert(TrackingColumn.TABLE_NAME, null, values);
        return id;
    }
    
    public Cursor getTrackings() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TrackingColumn.TABLE_NAME, null, null, null, null, null, null);
        return c;
    }
}
