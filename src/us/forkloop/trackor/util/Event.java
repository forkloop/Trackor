package us.forkloop.trackor.util;

import org.joda.time.DateTime;

/**
 * A Event object contains the detail of each
 * tracking history, e.g., time, location, reason.
 *
 */
public class Event {

    final private DateTime time;
    final private String zipcode;
    final private String location;
    final private String info;

    public Event(DateTime time, String location, String zipcode, String info) {
        this.time = time;
        this.location = location;
        this.zipcode = zipcode;
        this.info = info;
    }

    @Override
    public String toString() {
        return "Event [time=" + time + ", zipcode=" + zipcode + ", location=" + location + ", info=" + info + "]";
    }

    public DateTime getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }

    public String getZipcode() {
        return zipcode;
    }

    public String getInfo() {
        return info;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((info == null) ? 0 : info.hashCode());
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((time == null) ? 0 : time.hashCode());
        result = prime * result + ((zipcode == null) ? 0 : zipcode.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Event other = (Event) obj;
        if (info == null) {
            if (other.info != null)
                return false;
        } else if (!info.equals(other.info))
            return false;
        if (location == null) {
            if (other.location != null)
                return false;
        } else if (!location.equals(other.location))
            return false;
        if (time == null) {
            if (other.time != null)
                return false;
        } else if (!time.equals(other.time))
            return false;
        if (zipcode == null) {
            if (other.zipcode != null)
                return false;
        } else if (!zipcode.equals(other.zipcode))
            return false;
        return true;
    }

}