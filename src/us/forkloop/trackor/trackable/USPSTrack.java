package us.forkloop.trackor.trackable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import us.forkloop.trackor.util.Event;
import android.util.Log;

public class USPSTrack implements Trackable {

    private final String TAG = getClass().getSimpleName();
    private final String TEMPLATE = "http://production.shippingapis.com/ShippingAPITest.dll?API=TrackV2&XML=%3CTrackRequest%20USERID=%22092TRACK3843%22%3E%3CTrackID%20ID=%22EJ958083578US%22%3E%3C/TrackID%3E%3C/TrackRequest%3E";
    @Override
    public List<Event> track(String trackingNumber) {
        HttpURLConnection conn = null;
        try {
            Log.d(TAG, "fetching status for " + trackingNumber);
            URL url = new URL(TEMPLATE);
            conn = (HttpURLConnection) url.openConnection();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream in = conn.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = buffer.readLine()) != null) {
                    sb.append(line);
                }
                buffer.close();
                return null;//sb.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            conn.disconnect();
        }
        return null;
    }
}
