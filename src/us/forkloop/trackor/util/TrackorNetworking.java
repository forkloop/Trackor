package us.forkloop.trackor.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class TrackorNetworking {

    private static final String TAG = "TrackorNetworking";

    public Bitmap downloadImage(String sURL) {
        URL url = null;
        try {
            url = new URL(sURL);
        } catch (MalformedURLException mue) {
            Log.d(TAG, "Invalid url: " + sURL);
            throw new TrackorException(mue);
        }

        Bitmap bitmap = null;
        InputStream in = null;
        try {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            in = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(in);
        }
        } catch (Exception e) {
            Log.d(TAG, "Error while downloading image from " + sURL);
            throw new TrackorException(e);
        } finally {
            try {
                in.close();
            } catch (IOException ioe) {
                Log.e(TAG, "Fail to close input stream.", ioe);
            }
        }
        return bitmap;
    }
}