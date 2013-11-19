package us.forkloop.trackor.db;

public class TrackingWithAction {

    private final Tracking tracking;
    private final Action action;

    public TrackingWithAction(Tracking tracking, Action action) {
        this.tracking = tracking;
        this.action = action;
    }

    public Tracking getTracking() {
        return this.tracking;
    }

    public Action getAction() {
        return this.action;
    }

    public static enum Action {
        Add, Archive, Update, Switch;
    }
}
