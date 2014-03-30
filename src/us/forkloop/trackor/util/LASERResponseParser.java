package us.forkloop.trackor.util;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class LASERResponseParser implements ResponseParser {

    private static final String TAG = "LASERResponseParser";
    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
    private List<Event> events;
    private String estimatedDeliveryDate;
    private String destination;
    private boolean isDelivered;

    @Override
    public void parse(final String response) throws JSONException {
        Log.d(TAG, response);
        JSONObject json = new JSONObject(response);

        estimatedDeliveryDate = json.getString("EstimatedDeliveryDate");

        JSONObject jDestination = json.getJSONObject("Destination");
        destination = jDestination.getString("City") + ", " + jDestination.getString("State");

        JSONArray jEvents = json.getJSONArray("Events");
        events = new ArrayList<Event>();
        for (int n = 0; n < jEvents.length(); n++) {
            JSONObject jEvent = jEvents.optJSONObject(n);
            String eventType = jEvent.optString("EventType");
            String location = "";
            String info = "";
            if ("Exception".equals(eventType)) {
                location = jEvent.optString("Country");
                info = "Package not received from sender";
            } else {
                location = jEvent.optString("City") + ", " + jEvent.optString("State") + " " + jEvent.optString("Country");
                info = jEvent.optString("EventShortText") + " " + jEvent.optString("Location");
            }
            String zipcode = jEvent.optString("PostalCode");
            DateTime time = FORMATTER.parseDateTime(jEvent.getString("DateTime"));
            Event event = new Event(time, location, zipcode, info);
            events.add(event);
            // check deliver status
            if ("Released".equals(eventType) || "Delivered".equals(eventType)) {
                isDelivered = true;
            }
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

    @Override
    public boolean isDelivered() {
        return this.isDelivered;
    }
}
