package us.forkloop.trackor.trackable;

import java.io.BufferedReader;
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

// TODO contact USPS service
public class USPSTrack implements Trackable {

    private final String TAG = getClass().getSimpleName();
    private final String TEMPLATE = "http://production.shippingapis.com/ShippingAPITest.dll?API=TrackV2&XML=%3CTrackRequest%20USERID=%22092TRACK3843%22%3E%3CTrackID%20ID=%22EJ958083578US%22%3E%3C/TrackID%3E%3C/TrackRequest%3E";
    private static final Pattern PATTERN = Pattern.compile("^(.*am|pm)\\s(.*)\\s(\\d{5})\\.$");
    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("MMM dd KK:mm aa");

    @Override
    public List<Event> track(final String trackingNumber) {
        HttpURLConnection conn = null;
        try {
            Log.d(TAG, "fetching status for " + trackingNumber);
            URL url = new URL(TEMPLATE);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream in = conn.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = buffer.readLine()) != null) {
                    sb.append(line);
                }
                buffer.close();
                return parse(sb.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            conn.disconnect();
        }
        return null;
    }

    private List<Event> parse(String response) throws Exception {
        List<Event> events = new ArrayList<Event>();

        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(response));
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() == XmlPullParser.START_TAG
                    && parser.getName().equals("TrackDetail")) {
                String detail = parser.nextText();
                Log.d(TAG, detail);
                Matcher matcher = PATTERN.matcher(detail);
                String zipcode = "";
                DateTime time = DateTime.now();
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
                    Event event = new Event(time, "", zipcode, info);
                    events.add(event);
                }
            }
        }
        return events;
    }
}