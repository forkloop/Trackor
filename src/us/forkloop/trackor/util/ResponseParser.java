package us.forkloop.trackor.util;

import java.util.List;

public interface ResponseParser {

    public void parse(final String response) throws Exception;

    public List<Event> getEvents();

    public String getEstimatedDeliveryDate();

    public String getDestination();

    public boolean isDelivered();
}