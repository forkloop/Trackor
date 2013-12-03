package us.forkloop.trackor.trackable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xmlpull.v1.XmlPullParser;

import us.forkloop.trackor.util.Event;
import android.util.Log;
import android.util.Xml;

public class USPSTrack implements Trackable {

    private final String TAG = getClass().getSimpleName();
    private final String TEMPLATE_BEFORE = "http://production.shippingapis.com/ShippingAPI.dll?API=TrackV2&XML=%3CTrackRequest%20USERID=%22092TRACK3843%22%3E%3CTrackID%20ID=%22";
    private final String TEMPLATE_AFTER = "%22%3E%3C/TrackID%3E%3C/TrackRequest%3E";
    private static final Pattern PATTERN = Pattern.compile("^(.*am|pm)\\s(.*)\\s(\\d{5})\\.$");
    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("MMM dd KK:mm aa");
    private static final String DELIVERED = "Delivered";
    private String response;
    private boolean isDelivered;

    @Override
    public List<Event> track(final String trackingNumber) {
        HttpURLConnection conn = null;
        InputStream in = null;
        try {
            Log.d(TAG, "fetching status for " + trackingNumber);
            URL url = new URL(TEMPLATE_BEFORE + trackingNumber + TEMPLATE_AFTER);
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
                response = sb.toString();
                buffer.close();
                return parse(response);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
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

    @Override
    public String rawStatus() {
        return response;
    }

    @Override
    public boolean isDelivered() {
        return this.isDelivered;
    }

    @Override
    public List<Event> parse(String response) {
        List<Event> events = new ArrayList<Event>();

        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(new StringReader(response));
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG
                        && parser.getName().equals("TrackDetail")) {
                    String detail = parser.nextText();
                    Log.d(TAG, detail);
                    Matcher matcher = PATTERN.matcher(detail);
                    String zipcode = "";
                    DateTime time = null;
                    String location = "";
                    String info = "";
                    if (matcher.find()) {
                        try {
                            time = FORMATTER.parseDateTime(matcher.group(1));
                        } catch (IllegalArgumentException e) {
                            Log.e(TAG, "error while parsing date", e);
                            continue;
                        }
                        info = matcher.group(2);
                        zipcode = matcher.group(3);
                    } else {
                        info = detail;
                    }
                    if (info.contains(DELIVERED)) {
                        isDelivered = true;
                    }
                    Event event = new Event(time, location, zipcode, info);
                    events.add(event);
                }
            }
        } catch (Exception e) {
        }
        return events;
    }
}