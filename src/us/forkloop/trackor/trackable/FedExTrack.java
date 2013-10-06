package us.forkloop.trackor.trackable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import us.forkloop.trackor.util.Event;
import android.content.Context;
import android.util.Log;

public class FedExTrack implements Trackable {

    private final String TAG = getClass().getSimpleName();
    private final String ENDPOINT = "https://wsbeta.fedex.com/xml";

    private Context context;

    public FedExTrack(Context context) {
        this.context = context;
    }

    @Override
    public List<Event> track(final String trackingNumber) {
        String template = loadTemplate();
        String mockTrackingNumber = "9612804882227374518306";
        if (template != null) {
            String body = String.format(template, mockTrackingNumber);
            Log.d(TAG, body);

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
        }
        return null;
    }

    private String loadTemplate() {
        try {
            InputStream in = context.getAssets().open("fedex.xml");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            return sb.toString();
        } catch (IOException ioe) {
            Log.e(TAG, ioe.toString());
        }
        return null;
    }
}
