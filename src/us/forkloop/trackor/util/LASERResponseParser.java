package us.forkloop.trackor.util;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LASERResponseParser implements ResponseParser {

    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
    private List<Event> events;
    private String estimatedDeliveryDate;
    private String destination;

    @Override
    public void parse(final String response) throws JSONException {
        JSONObject json = new JSONObject(response);

        estimatedDeliveryDate = json.getString("EstimatedDeliveryDate");

        JSONObject jDestination = json.getJSONObject("Destination");
        destination = jDestination.getString("City") + ", " + jDestination.getString("State");

        JSONArray jEvents = json.getJSONArray("Events");
        events = new ArrayList<Event>();
        for (int n = 0; n < jEvents.length(); n++) {
            JSONObject jEvent = jEvents.getJSONObject(n);
            String location = jEvent.getString("City") + ", " + jEvent.getString("State");
            String zipcode = jEvent.getString("PostalCode");
            String info = jEvent.getString("EventShortText");
            DateTime time = FORMATTER.parseDateTime(jEvent.getString("DateTime"));
            Event event = new Event(time, location, zipcode, info);
            events.add(event);
        }
    }

    @Override
    public List<Event> getEvents() {
        return events;
    }

    @Override
    public String getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    @Override
    public String getDestination() {
        return destination;
    }

}
