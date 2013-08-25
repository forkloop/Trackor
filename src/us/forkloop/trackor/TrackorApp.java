package us.forkloop.trackor;

import android.content.Context;
import android.graphics.Typeface;
import android.util.LruCache;

public final class TrackorApp {

    private static Context context;
    private static TrackorApp app;
    private LruCache<String, Typeface> sTypefaceCache =
            new LruCache<String, Typeface>(12);

    synchronized public static TrackorApp getInstance(Context context) {
        if (app == null) {
            TrackorApp.context = context;
            app = new TrackorApp();
        }
        return app;
    }

    synchronized public Typeface getTypeface(String typeface) {
        Typeface mTypeface = sTypefaceCache.get(typeface);
        
        if (mTypeface == null) {
            mTypeface = Typeface.createFromAsset(context.getApplicationContext()
                    .getAssets(), String.format("%s", typeface));
            sTypefaceCache.put(typeface, mTypeface);
        }
        return mTypeface;
    }

}
