package us.forkloop.trackor.trackable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import us.forkloop.trackor.util.Event;
import android.util.Log;
import android.util.Pair;

/**
 * sample tracking number: 9261290100130056892383
 *
 */
public class USPSHTMLTrack implements Trackable {

    private static final String TAG = "USPSHTMLTrack";
    private static final String ENDPOINT = "https://tools.usps.com/go/TrackConfirmAction_input?origTrackNum=";
    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("MMMM d, yyyy , KK:mm aa");
    private static final DateTimeFormatter SIMPLE_FORMATTER = DateTimeFormat.forPattern("MMMM d, yyyy");
    private static final Pattern PATTERN = Pattern.compile("^(.*)\\s(\\d{5})");

    @Override
    public List<Event> track(String trackingNumber) {
        try {
            Document doc = Jsoup.connect(ENDPOINT + trackingNumber).get();
            Log.d(TAG, doc.toString());
            Elements elements = doc.select(".detail-wrapper");
            if (elements != null && elements.size() > 0) {
                List<Event> events = new ArrayList<Event>();
                for (Element element : elements) {
                    Elements dateElements = element.select(".date-time > p");
                    DateTime date = extractDate(dateElements);
                    Elements statusElements = element.select(".info-text");
                    String status = extractStatus(statusElements);
                    Elements locationElements = element.select(".location > p");
                    Pair<String, String> geo = extractLocation(locationElements);
                    Event event;
                    if (geo != null) {
                        event = new Event(date, geo.first, geo.second, status);
                    } else {
                        event = new Event(date, null, null, status);
                    }
                    Log.d(TAG, "Add event " + event);
                    events.add(event);
                }
                return events;
            }
        } catch (IOException ioe) {
            Log.e(TAG, "Error while getting info for " + trackingNumber + ": " + ioe.getMessage());
        }
        return null;
    }

    private DateTime extractDate(Elements dateElements) {
        if (dateElements != null && dateElements.size() > 0) {
            String text = dateElements.get(0).text();
            DateTime date = null;
            try {
                date = FORMATTER.parseDateTime(text.trim());
            } catch (Exception e) {
                Log.e(TAG, "Error while parsing date " + text + ": " + e.getMessage());
            }
            if (date == null) {
                try {
                    date = SIMPLE_FORMATTER.parseDateTime(text.trim());
                } catch(Exception e) {
                    Log.e(TAG, "Error while parsing simple date " + text + ": " + e.getMessage());
                }
            }
            return date;
        }
        return null;
    }

    private String extractStatus(Elements statusElements) {
        if (statusElements != null && statusElements.size() > 0) {
            Element status = statusElements.get(0);
            return status.text();
        }
        return null;
    }

    private Pair<String, String> extractLocation(Elements locationElements) {
        if (locationElements != null && locationElements.size() > 0) {
            Element element = locationElements.get(0);
            String text = element.text();
            try {
                //FIXME still have whitespace after trim()
                Log.d(TAG, "test ^" + text.trim() + "$");
                Matcher matcher = PATTERN.matcher(text.trim());
                if (matcher.find()) {
                    return new Pair<String, String>(matcher.group(1), matcher.group(2));
                }
            } catch (Exception e) {
                Log.d(TAG, "Error while extracting location " + text + ": " + e.getMessage());
            }
        }
        return null;
    }
}
