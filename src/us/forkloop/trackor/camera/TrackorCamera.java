package us.forkloop.trackor.camera;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;

public class TrackorCamera {

    private static final String TAG = "TrackorCamera";

    public static Camera getCamera() {
        Camera camera = null;
        try {
            camera = Camera.open();
            camera.setDisplayOrientation(90);
            Camera.Parameters params = camera.getParameters();
            if (params.getMaxNumMeteringAreas() > 0) {
                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                Rect focusRect = new Rect(-100, -40, 100, 40);
                meteringAreas.add(new Camera.Area(focusRect, 1000));
                params.setMeteringAreas(meteringAreas);
            }
            Log.d(TAG, Arrays.toString(params.getSupportedSceneModes().toArray()));
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            camera.setParameters(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return camera;
    }
}