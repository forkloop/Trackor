package us.forkloop.trackor.scan;

import android.app.Activity;

import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;

public abstract class ResultHandler {

    public ResultHandler(Activity activity, ParsedResult result, Result rawResult) {
    }

}
