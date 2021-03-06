package us.forkloop.trackor;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.LruCache;
import android.util.TypedValue;

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

    public int convertDpToPiexl(int dp) {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    synchronized public boolean isConnected() {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
}
