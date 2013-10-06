package us.forkloop.trackor.trackable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import us.forkloop.trackor.util.Event;
import android.util.Log;

public class UPSTrack implements Trackable {

    private final String TAG = getClass().getSimpleName();

    private final String ENDPOINT = "https://wwwcie.ups.com/ups.app/xml/Track";
    private final String TEMPLATE = "<?xml version=\"1.0\" ?><AccessRequest xml:lang='en-US'><AccessLicenseNumber>%s</AccessLicenseNumber>"
                                    + "<UserId>%s</UserId><Password>%s</Password></AccessRequest>"
                                    + "<TrackRequest><Request><TransactionReference></TransactionReference><RequestAction>Track</RequestAction></Request>"
                                    + "<TrackingNumber>%s</TrackingNumber></TrackRequest>";
    private final String UPS_TOKEN = "ACBC1CC31858BAE6";
    private final String USER_ID = "forkloop";
    private final String PASSWORD = "";
    @Override
    public List<Event> track(String trackingNumber) {
        String body = String.format(TEMPLATE, UPS_TOKEN, USER_ID, PASSWORD, "1Z06R89V9006422981");
        Log.d(TAG, "UPS request body: " + body);
        HttpURLConnection conn = null;
        try {
            URL url = new URL(ENDPOINT);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(0);
            conn.setRequestMethod("POST");
            OutputStream out = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(body);
            writer.flush();
            writer.close();
            //
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
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
