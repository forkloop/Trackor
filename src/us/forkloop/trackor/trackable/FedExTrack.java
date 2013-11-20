package us.forkloop.trackor.trackable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import us.forkloop.trackor.util.Event;
import android.util.Log;

/**
 * {@link http://www.hurl.it/}
 */
public class FedExTrack implements Trackable {

    private static final String TAG = "FedExTrack";
    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-ddHH:mm:ss");
    private final String ENDPOINT = "https://www.fedex.com/trackingCal/track";
    private final String PREFIX = "format=json&action=trackpackages&data=";
    private final String TEMPLATE = "{\"TrackPackagesRequest\":{\"appType\":\"wtrk\",\"uniqueKey\":\"\""
            + ",\"processingParameters\":{\"anonymousTransaction\":false,\"clientId\":\"WTRK\",\"returnDetailedErrors\":true,"
            + "\"returnLocalizedDateTime\":false},\"trackingInfoList\":[{\"trackNumberInfo\":{\"trackingNumber\":\"%s\""
            + ",\"trackingQualifier\":\"\",\"trackingCarrier\":\"\"}}]}}";

    @Override
    public List<Event> track(final String trackingNumber) {
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            final URL url = new URL(ENDPOINT);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            OutputStream out = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            String data = URLEncoder.encode(String.format(TEMPLATE, trackingNumber), "utf-8");
            writer.write(PREFIX + data);
            writer.flush();
            writer.close();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                in = conn.getInputStream();
                String response = IOUtils.toString(in);
                return parse(response);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            }
            conn.disconnect();
        }
        return null;
    }

    private List<Event> parse(final String response) {
        try {
            JSONObject json = new JSONObject(response).getJSONObject("TrackPackagesResponse");
            boolean isSuccess = json.getBoolean("successful");
            if (isSuccess) {
                JSONArray trackings = json.getJSONArray("packageList");
                if (trackings.length() > 0) {
                    JSONObject tracking = trackings.getJSONObject(0);
                    JSONArray updates = tracking.getJSONArray("scanEventList");
                    List<Event> events = new ArrayList<Event>();
                    for (int n = 0; n < updates.length(); n++) {
                        JSONObject scan = updates.getJSONObject(n);
                        Log.d(TAG, "" + scan);
                        String info = scan.optString("status");
                        String location = scan.optString("scanLocation");
                        String dateTime = scan.optString("date") + scan.optString("time");
                        DateTime date = null;
                        try {
                            date = DateTime.parse(dateTime, FORMATTER);
                        } catch (IllegalArgumentException iae) {
                            Log.e(TAG, "Error while parse " + dateTime + ": " + date);
                            continue;
                        }
                        Event event = new Event(date, location, null, info);
                        events.add(event);
                    }
                    return events;
                }
            }
        } catch (JSONException je) {
            Log.e(TAG, "Error while parsing response " + je);
        }
        return null;
    }
}