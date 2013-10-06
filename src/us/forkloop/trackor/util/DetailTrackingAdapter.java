package us.forkloop.trackor.util;

import java.util.List;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import us.forkloop.trackor.R;
import us.forkloop.trackor.TrackorApp;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailTrackingAdapter extends ArrayAdapter<Event> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("HH:mm:ss / MM-dd");
    private TrackorApp app;

    public DetailTrackingAdapter(Context context, int resource, List<Event> objects) {
        super(context, resource, objects);
        app = TrackorApp.getInstance(context);
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent)  {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.detail_tracking_record, null);
        }
        Event record = getItem(position);
        TextView locationView = (TextView) convertView.findViewById(R.id.tracking_record_location);
        locationView.setText(record.getLocation());
        locationView.setTypeface(app.getTypeface("Gotham-Book.otf"));
        TextView infoView = (TextView) convertView.findViewById(R.id.tracking_record_info);
        infoView.setText(record.getInfo());
        infoView.setTypeface(app.getTypeface("Gotham-Book.otf"));

        TextView dateView = (TextView) convertView.findViewById(R.id.tracking_record_date);
        dateView.setText(record.getTime().toString(FORMATTER));
        dateView.setTypeface(app.getTypeface("Gotham-Book.otf"));

        ImageView imageView = (ImageView) convertView.findViewById(R.id.tracking_record_image);
        imageView.setImageResource(R.drawable.transparent_green_bit);
        return convertView;
    }
}