package us.forkloop.trackor.view;

import us.forkloop.trackor.R;
import us.forkloop.trackor.TrackorApp;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class TypefacedTextView extends TextView {

    private static final String TAG = "TypefacedTextView";
    private final TrackorApp app = TrackorApp.getInstance(getContext());

    public TypefacedTextView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TypefacedTextView);
        if (array.length() > 0) {
            final String typeface = array.getString(0);
            if (typeface != null) {
                Log.d(TAG, "use typeface " + typeface);
                setTypeface(app.getTypeface(typeface));
            }
        }
        array.recycle();
    }
}
