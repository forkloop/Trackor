package us.forkloop.trackor.trackable;

import java.util.List;

import us.forkloop.trackor.util.Event;

public interface Trackable {

    public List<Event> track(String trackingNumber);
}
