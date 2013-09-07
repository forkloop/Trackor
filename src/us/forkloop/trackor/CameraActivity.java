package us.forkloop.trackor;

import us.forkloop.trackor.camera.TrackorCamera;
import us.forkloop.trackor.camera.TrackorCameraPreview;
import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

public class CameraActivity extends Activity {
    private Camera camera;
    private TrackorCameraPreview preview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        camera = TrackorCamera.getCamera();

        preview = new TrackorCameraPreview(this, camera);
        RelativeLayout previewLayout = (RelativeLayout) findViewById(R.id.camera_preview);
        previewLayout.addView(preview);

        View focusRect = findViewById(R.id.focus_rect);
        focusRect.bringToFront();

        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(null, null, new TrackorPictureCallback());
            }
        });
    }

    @Override
    protected void onPause() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
        super.onPause();
    }

    private class TrackorPictureCallback implements Camera.PictureCallback {

        private final String TAG = getClass().getSimpleName();

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "");
        }
    }
}