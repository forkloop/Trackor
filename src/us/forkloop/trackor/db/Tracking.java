package us.forkloop.trackor.db;

import android.provider.BaseColumns;

public final class Tracking {

    private static final String DEFAULT_NAME = "package";
    private final String carrier;
    private final String trackingNumber;
    private String name;
    
    //private boolean isArchived;
    
    public Tracking(final String carrier, final String trackingNumber, final String name) {
        this.carrier        = carrier;
        this.trackingNumber = trackingNumber;
        this.name           = name;
        if (name == null || name.isEmpty()) {
            this.name = DEFAULT_NAME;
        }
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
