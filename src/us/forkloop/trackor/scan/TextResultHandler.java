package us.forkloop.trackor.scan;

import android.app.Activity;

import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;

public final class TextResultHandler extends ResultHandler {

    public TextResultHandler(Activity activity, ParsedResult result, Result rawResult) {
        super(activity, result, rawResult);
    }

}
