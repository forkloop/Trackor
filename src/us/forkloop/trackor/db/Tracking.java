package us.forkloop.trackor.db;

import android.provider.BaseColumns;

public final class Tracking {

    private static final String DEFAULT_NAME = "package";
    private String carrier;
    private String trackingNumber;
    private String name;
    
    //private boolean isArchived;
    
    public Tracking(String carrier, String trackingNumber) {
        this(carrier, trackingNumber, DEFAULT_NAME);
    }

    public Tracking(String carrier, String trackingNumber, String name) {
        this.carrier        = carrier;
        this.trackingNumber = trackingNumber;
        this.name           = name;
    }

    public String getCarrier() {
        return this.carrier;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getTrackingNumber() {
        return this.trackingNumber;
    }

    @Override
    public String toString() {
        return this.carrier + ": " + this.trackingNumber;
    }
    
    public static abstract class TrackingColumn implements BaseColumns {
        public static final String TABLE_NAME = "tracking";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CARRIER = "carrier";
        public static final String COLUMN_TRACKING_NUMBER = "tnumber";
    }
}
