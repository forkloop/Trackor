package us.forkloop.trackor.db;

import us.forkloop.trackor.db.Tracking.TrackingColumn;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String[] DEFAULT_LIST_COLUMNS =
    { TrackingColumn._ID, TrackingColumn.COLUMN_NAME, TrackingColumn.COLUMN_CARRIER,
            TrackingColumn.COLUMN_TRACKING_NUMBER, TrackingColumn.COLUMN_IS_DELIVERED };
    private static final int DB_VERSION = 3;
    private static final String DB_NAME = "trackings.db";
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TrackingColumn.TABLE_NAME + " ( "
                    + TrackingColumn._ID + " INTEGER PRIMARY KEY,"
                    + TrackingColumn.COLUMN_NAME + " TEXT NOT NULL,"
                    + TrackingColumn.COLUMN_CARRIER + " TEXT NOT NULL,"
                    + TrackingColumn.COLUMN_TRACKING_NUMBER + " TEXT NOT NULL UNIQUE,"
                    + TrackingColumn.COLUMN_IS_DELETED + " INTEGER NOT NULL,"
                    + TrackingColumn.COLUMN_IS_DELIVERED + " INTEGER NOT NULL,"
                    + TrackingColumn.COLUMN_STATUS_BLOB + " TEXT );";
    private static final String CREATE_INDEX_SQL =
            "CREATE INDEX " + TrackingColumn.INDEX_NAME + " ON " + TrackingColumn.TABLE_NAME + " ( " + TrackingColumn.COLUMN_TRACKING_NUMBER + " );";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TrackingColumn.TABLE_NAME;
    private static final String DROP_INDEX_SQL = "DROP INDEX IF EXISTS " + TrackingColumn.INDEX_NAME;

    private static DatabaseHelper helper;

    public synchronized static DatabaseHelper getInstance(Context context) {
        if (helper == null) {
            helper = new DatabaseHelper(context);
        }
        return helper;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL(CREATE_TABLE);
            db.execSQL(CREATE_INDEX_SQL);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // @Override
    // public void onOpen(SQLiteDatabase db) {
    // db.execSQL("DELETE FROM tracking WHERE carrier is null;");
    // }

    @Override
    public void onUpgrade(SQLiteDatabase db, int v1, int v2) {
        Log.d(TAG, "Upgrade table from " + v1 + " to " + v2);
        // Cursor cursor = getActiveTrackings();
        db.beginTransaction();
        try {
            db.execSQL(DROP_TABLE);
            db.execSQL(DROP_INDEX_SQL);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        onCreate(db);
        // migrate(cursor);
    }

    public long addTracking(Tracking t) {
        Log.v(TAG, "add tracking " + t.toString());

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TrackingColumn.COLUMN_CARRIER, t.getCarrier());
        values.put(TrackingColumn.COLUMN_NAME, t.getName());
        values.put(TrackingColumn.COLUMN_TRACKING_NUMBER, t.getTrackingNumber());
        values.put(TrackingColumn.COLUMN_IS_DELETED, 0);
        values.put(TrackingColumn.COLUMN_IS_DELIVERED, 0);
        long id = -1;
        try {
            id = db.insertOrThrow(TrackingColumn.TABLE_NAME, null, values);
        } catch (SQLiteException se) {
            Log.e(TAG, "Error while inserting " + t + ": " + se);
            values.remove(TrackingColumn.COLUMN_TRACKING_NUMBER);
            db.update(TrackingColumn.TABLE_NAME, values, TrackingColumn.COLUMN_TRACKING_NUMBER + " = ?", new String[] { t.getTrackingNumber() });
        }
        return id;
    }

    public Cursor getArchiveTrackings() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TrackingColumn.TABLE_NAME, DEFAULT_LIST_COLUMNS, TrackingColumn.COLUMN_IS_DELETED + " = 1", null, null, null, TrackingColumn._ID + " DESC");
        return c;
    }

    public Cursor getActiveTrackings() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TrackingColumn.TABLE_NAME, DEFAULT_LIST_COLUMNS, TrackingColumn.COLUMN_IS_DELETED + " = 0", null, null, null, TrackingColumn._ID + " DESC");
        return c;
    }

    public Cursor getActiveOnTheWayTrackings() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TrackingColumn.TABLE_NAME, DEFAULT_LIST_COLUMNS, TrackingColumn.COLUMN_IS_DELETED + " = 0 AND " + TrackingColumn.COLUMN_IS_DELIVERED + " = 0",
                null, null, null, TrackingColumn._ID + " DESC");
        return c;
    }

    public String getTrackingStatus(String trackingNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TrackingColumn.TABLE_NAME, new String[] { TrackingColumn.COLUMN_STATUS_BLOB }, TrackingColumn.COLUMN_TRACKING_NUMBER + " = ?",
                new String[] { trackingNumber }, null, null, null);
        if (c != null && c.moveToFirst()) {
            return c.getString(c.getColumnIndex(TrackingColumn.COLUMN_STATUS_BLOB));
        }
        return null;
    }

    public int archiveTracking(String trackingNumber) {
        Log.d(TAG, "archive tracking " + trackingNumber);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TrackingColumn.COLUMN_IS_DELETED, 1);
        return db.update(TrackingColumn.TABLE_NAME, values, TrackingColumn.COLUMN_TRACKING_NUMBER + " = ?", new String[] { trackingNumber });
    }

    public int updateTrackingTag(String trackingNumber, String tag) {
        Log.d(TAG, "update tracking " + trackingNumber + ", new tag: " + tag);
        ContentValues values = new ContentValues();
        values.put(TrackingColumn.COLUMN_NAME, tag);
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(TrackingColumn.TABLE_NAME, values, TrackingColumn.COLUMN_TRACKING_NUMBER + " = ?", new String[] { trackingNumber });
    }

    public int updateTrackingStatus(String trackingNumber, boolean isDelivered, String status) {
        Log.d(TAG, "update tracking " + trackingNumber + ", isDelivered " + isDelivered);
        ContentValues values = new ContentValues();
        values.put(TrackingColumn.COLUMN_IS_DELIVERED, isDelivered ? 1 : 0);
        if (status != null) {
            values.put(TrackingColumn.COLUMN_STATUS_BLOB, status);
        }
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(TrackingColumn.TABLE_NAME, values, TrackingColumn.COLUMN_TRACKING_NUMBER + " = ?", new String[] { trackingNumber });
    }

    private void migrate(Cursor cursor) {
        while (cursor.moveToNext()) {
            String trackingNumber = cursor.getString(cursor.getColumnIndex(TrackingColumn.COLUMN_TRACKING_NUMBER));
            String carrier = cursor.getString(cursor.getColumnIndex(TrackingColumn.COLUMN_CARRIER));
            String name = cursor.getString(cursor.getColumnIndex(TrackingColumn.COLUMN_NAME));
            addTracking(new Tracking(trackingNumber, carrier, name, false));
        }
    }
}