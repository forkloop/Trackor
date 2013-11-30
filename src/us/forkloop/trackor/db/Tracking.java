package us.forkloop.trackor.db;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

public final class Tracking implements Parcelable {

    private static final String DEFAULT_NAME = "package";
    private final String carrier;
    private final String trackingNumber;
    private String name;
    private final boolean isDelivered;

    // private boolean isArchived;

    public static final Parcelable.Creator<Tracking> CREATOR =
            new Parcelable.Creator<Tracking>() {
                @Override
                public Tracking createFromParcel(Parcel source) {
                    return new Tracking(source);
                }

                @Override
                public Tracking[] newArray(int size) {
                    return new Tracking[size];
                }
            };

    public Tracking(Parcel source) {
        carrier = source.readString();
        trackingNumber = source.readString();
        name = source.readString();
        isDelivered = (source.readByte() != 0);
    }

    public Tracking(final String carrier, final String trackingNumber, final String name, final boolean isDelivered) {
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
        this.name = name;
        if (name == null || name.isEmpty()) {
            this.name = DEFAULT_NAME;
        }
        this.isDelivered = isDelivered;
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

    public boolean isDelivered() {
        return this.isDelivered;
    }

    @Override
    public String toString() {
        return this.carrier + ": " + this.trackingNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flag) {
        dest.writeString(carrier);
        dest.writeString(trackingNumber);
        dest.writeString(name);
        dest.writeByte((byte) (isDelivered ? 1 : 0));
    }

    public static abstract class TrackingColumn implements BaseColumns {
        public static final String TABLE_NAME = "tracking";
        public static final String INDEX_NAME = "tindex";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CARRIER = "carrier";
        public static final String COLUMN_TRACKING_NUMBER = "tnumber";
        public static final String COLUMN_IS_DELETED = "is_deleted";
        public static final String COLUMN_IS_DELIVERED = "is_delivered";
        /*
         * store as a blob instead of another model (which may have event, time, location, etc)
         * so that we can preserve all information. In case later we want to extract
         * more information, the data would still be available.
         * 
         * the tradeoff between having a structural model vs storing unorder detailed information,
         * the later one is winning.
         */
        public static final String COLUMN_STATUS_BLOB = "status";
    }
}
