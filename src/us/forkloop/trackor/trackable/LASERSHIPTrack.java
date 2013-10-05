package us.forkloop.trackor.trackable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class LASERSHIPTrack implements Trackable {

    private static final String ENDPOINT = "http://www.lasership.com/track/%s/json";
    private static final String TAG = "LASERSHIP";

    @Override
    public String track(final String trackingNumber) {
        HttpURLConnection conn = null;
        String mockTrackingNumber = "Q26181478";
        final String endpoint = String.format(ENDPOINT, mockTrackingNumber);
        try {
            Log.d(TAG, "fetching status for " + trackingNumber);
            final URL url = new URL(endpoint);
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
                return sb.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            conn.disconnect();
        }
        return null;
    }

}
