package us.forkloop.trackor.db;

import us.forkloop.trackor.db.Tracking.TrackingColumn;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final int DB_VERSION    = 2;
    private static final String DB_NAME    = "trackings.db";
    private static final String CREATE_TABLE = 
            "CREATE TABLE " + TrackingColumn.TABLE_NAME + " ( "
            + TrackingColumn._ID + " INTEGER PRIMARY KEY,"
            + TrackingColumn.COLUMN_NAME + " TEXT NOT NULL,"
            + TrackingColumn.COLUMN_CARRIER + " TEXT NOT NULL,"
            + TrackingColumn.COLUMN_TRACKING_NUMBER + " TEXT NOT NULL UNIQUE,"
            + TrackingColumn.COLUMN_IS_DELETED + " INTEGER NOT NULL );";
    private static final String CREATE_INDEX_SQL =
            "CREATE INDEX " + TrackingColumn.INDEX_NAME + " ON " + TrackingColumn.TABLE_NAME + " ( " + TrackingColumn.COLUMN_TRACKING_NUMBER + " );";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TrackingColumn.TABLE_NAME;
    private static final String DROP_INDEX_SQL = "DROP INDEX IF EXISTS " + TrackingColumn.INDEX_NAME;
    
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        db.execSQL(CREATE_TABLE);
        db.execSQL(CREATE_INDEX_SQL);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

//    @Override
//    public void onOpen(SQLiteDatabase db) {
//        db.execSQL("DELETE FROM tracking WHERE carrier is null;");
//   }

    @Override
    public void onUpgrade(SQLiteDatabase db, int v1, int v2) {
        Log.d(TAG, "Upgrade table from " + v1 + " to " + v2);
        db.beginTransaction();
        db.execSQL(DROP_TABLE);
        db.execSQL(DROP_INDEX_SQL);
        db.setTransactionSuccessful();
        db.endTransaction();

        onCreate(db);
    }

    public long addTracking(Tracking t) {
        Log.v(TAG, "add tracking " + t.toString());

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TrackingColumn.COLUMN_CARRIER, t.getCarrier());
        values.put(TrackingColumn.COLUMN_NAME, t.getName());
        values.put(TrackingColumn.COLUMN_TRACKING_NUMBER, t.getTrackingNumber());
        values.put(TrackingColumn.COLUMN_IS_DELETED, 0);
        long id = db.insert(TrackingColumn.TABLE_NAME, null, values);
        return id;
    }

    public Cursor getAllTrackings() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TrackingColumn.TABLE_NAME, null, null, null, null, null, TrackingColumn._ID + " DESC");
        return c;
    }

    public Cursor getActiveTrackings() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TrackingColumn.TABLE_NAME, null, TrackingColumn.COLUMN_IS_DELETED + " = 0", null, null, null, TrackingColumn._ID + " DESC");
        return c;
    }

    public int archiveTracking(String trackingNumber) {
        Log.d(TAG, "archive tracking " + trackingNumber);
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TrackingColumn.TABLE_NAME, TrackingColumn.COLUMN_TRACKING_NUMBER + " = ?", new String[] {trackingNumber});
    }

    public int updateTrackingTag(String trackingNumber, String tag) {
        Log.d(TAG, "update tracking " + trackingNumber + ", new tag: " + tag);
        ContentValues values = new ContentValues();
        values.put(TrackingColumn.COLUMN_NAME, tag);
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(TrackingColumn.TABLE_NAME, values, TrackingColumn.COLUMN_TRACKING_NUMBER + " = ?", new String[] {trackingNumber});
    }
}
