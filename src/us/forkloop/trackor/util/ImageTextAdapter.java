package us.forkloop.trackor.util;

import us.forkloop.trackor.R;
import us.forkloop.trackor.TrackorApp;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ImageTextAdapter extends ArrayAdapter<String> {

    private TrackorApp app;

    public ImageTextAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
        app = TrackorApp.getInstance(context);
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        return customzieView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return customzieView(position, convertView, parent);
    }

    private View customzieView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.carrier_spinner_row, null);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.carrier_spinner_tv);
        tv.setText(getItem(position));
        tv.setTypeface(app.getTypeface("Gotham-Book.otf"));
        return convertView;        
    }
}