package us.forkloop.trackor.trackable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import us.forkloop.trackor.util.Event;
import us.forkloop.trackor.util.LASERResponseParser;
import android.util.Log;

public class LASERSHIPTrack implements Trackable {

    private static final String ENDPOINT = "http://www.lasership.com/track/%s/json";
    private static final String TAG = "LASERSHIP";

    private LASERResponseParser parser;
    private String response;

    @Override
    public List<Event> track(final String trackingNumber) {
        HttpURLConnection conn = null;
        String mockTrackingNumber = "Q26181478";
        InputStream in = null;
        final String endpoint = String.format(ENDPOINT, trackingNumber);
        try {
            Log.d(TAG, "fetching status for " + trackingNumber);
            final URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                in = conn.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = buffer.readLine()) != null) {
                    sb.append(line);
                }
                buffer.close();
                response = sb.toString();
                if (response != null) {
                    return parse(response);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            conn.disconnect();
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            }
        }
        return null;
    }

    @Override
    public String rawStatus() {
        return response;
    }

    @Override
    public boolean isDelivered() {
        return parser.isDelivered();
    }

    @Override
    public List<Event> parse(final String response) {
        parser = new LASERResponseParser();
        try {
            parser.parse(response);
        } catch (Exception e) {
        }
        return parser.getEvents();
    }
}