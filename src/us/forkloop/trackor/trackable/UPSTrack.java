package us.forkloop.trackor.trackable;

public class UPSTrack implements Trackable {

    @Override
    public String track(String trackingNumber) {
        String status = "Delivered";
        return status;
    }

}
