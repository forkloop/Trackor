package us.forkloop.trackor.view;

import android.content.Context;
import android.widget.TextView;

public class TrackorLabelFactory {

    public static TextView getLabel(Context context, String content) {
        TextView label = new TextView(context);
        label.setText(content);
        return label;
    }
}
