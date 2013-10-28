package us.forkloop.trackor.trackable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import us.forkloop.trackor.util.Event;
import android.util.Log;

public class UPSTrack implements Trackable {

    private final String TAG = getClass().getSimpleName();

    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyyMMddHHmmss");
    private final String ENDPOINT = "https://wwwcie.ups.com/ups.app/xml/Track";
    private final String TEMPLATE = "<?xml version=\"1.0\" ?><AccessRequest xml:lang='en-US'><AccessLicenseNumber>%s</AccessLicenseNumber>"
                                    + "<UserId>%s</UserId><Password>%s</Password></AccessRequest>"
                                    + "<TrackRequest><Request><TransactionReference></TransactionReference>"
                                    + "<RequestAction>Track</RequestAction><RequestOption>Activity</RequestOption></Request>"
                                    + "<TrackingNumber>%s</TrackingNumber></TrackRequest>";
    private final String UPS_TOKEN = "ACBC1CC31858BAE6";
    private final String USER_ID = "forkloop";
    private final String PASSWORD = "Trackor4UPS";
    @Override
    public List<Event> track(final String trackingNumber) {
        String body = String.format(TEMPLATE, UPS_TOKEN, USER_ID, PASSWORD, trackingNumber);
        Log.d(TAG, "UPS request body: " + body);
        HttpURLConnection conn = null;
        try {
            URL url = new URL(ENDPOINT);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(0);
            conn.setRequestMethod("POST");
            OutputStream out = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(body);
            writer.flush();
            writer.close();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                return parse(sb.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            conn.disconnect();
        }
        return null;
    }

    private List<Event> parse(final String response) {
        List<Event> events = new ArrayList<Event>();
        Log.d(TAG, response);
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document dom = builder.parse(new InputSource(new StringReader(response)));
            NodeList status = dom.getElementsByTagName("ResponseStatusCode");
            if (status.getLength() > 0 && "1".equals(status.item(0).getTextContent())) {
                NodeList activities = dom.getElementsByTagName("Activity");
                for (int n = 0; n < activities.getLength(); n++) {
                    try {
                        Element activity = (Element) activities.item(n);
                        StringBuilder location = new StringBuilder();
                        NodeList city = activity.getElementsByTagName("City");
                        if (city.getLength() > 0) {
                            location.append(city.item(0).getTextContent());
                        }
                        NodeList state = activity.getElementsByTagName("StateProvinceCode");
                        if (state.getLength() > 0) {
                            location.append(" ").append(state.item(0).getTextContent());
                        }
                        String zipcode = "";
                        NodeList postalCode = activity.getElementsByTagName("PostalCode");
                        if (postalCode.getLength() > 0) {
                            zipcode = postalCode.item(0).getTextContent();
                        }
                        String info = "";
                        NodeList description = activity.getElementsByTagName("Description");
                        if (description.getLength() > 0) {
                            info = description.item(0).getTextContent();
                        }
                        StringBuilder datetime = new StringBuilder();
                        NodeList date = activity.getElementsByTagName("Date");
                        if (date.getLength() > 0) {
                            datetime.append(date.item(0).getTextContent());
                        }
                        NodeList time = activity.getElementsByTagName("Time");
                        if (time.getLength() > 0) {
                            datetime.append(time.item(0).getTextContent());
                        }
                        Event event = new Event(DateTime.parse(datetime.toString(), FORMATTER), location.toString(), zipcode, info);
                        Log.d(TAG, event.toString());
                        events.add(event);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
            }
        } catch (Exception e) {
        }
        return events;
    }
}