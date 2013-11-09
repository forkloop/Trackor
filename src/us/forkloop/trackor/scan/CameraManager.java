package us.forkloop.trackor.scan;

import java.io.IOException;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

import com.google.zxing.PlanarYUVLuminanceSource;

public class CameraManager {

    private static final String TAG = "CameraManager";

    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;
    private static final int MAX_FRAME_WIDTH = 960; // = 1920/2
    private static final int MAX_FRAME_HEIGHT = 540; // = 1080/2

    private Camera camera;
    private final Context context;
    private final CameraConfigurationManager configManager;

    private Rect framingRect;
    private Rect framingRectInPreview;
    private boolean previewing;

    private final PreviewCallback previewCallback;

    public CameraManager(Context context) {
        this.context = context;
        this.configManager = new CameraConfigurationManager(context);
        this.previewCallback = new PreviewCallback(configManager);
    }

    public synchronized boolean isOpen() {
        return camera != null;
    }

    public synchronized void openDriver(SurfaceHolder holder) throws IOException {
    }

    /**
     * Closes the camera driver if still in use.
     */
    public synchronized void closeDriver() {
        if (camera != null) {
            camera.release();
            camera = null;
            // Make sure to clear these each time we close the camera, so that any scanning rect
            // requested by intent is forgotten.
            framingRect = null;
            framingRectInPreview = null;
        }
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    public synchronized void startPreview() {
        Camera theCamera = camera;
        if (theCamera != null && !previewing) {
            theCamera.startPreview();
            previewing = true;
        }
    }

    /**
     * Tells the camera to stop drawing preview frames.
     */
    public synchronized void stopPreview() {
        if (camera != null && previewing) {
            camera.stopPreview();
            previewCallback.setHandler(null, 0);
            previewing = false;
        }
    }

    /**
     * Calculates the framing rect which the UI should draw to show the user where to place the barcode. This target helps with alignment as well as forces the user to hold the device far enough away
     * to ensure the image will be in focus.
     * 
     * @return The rectangle to draw on screen in window coordinates.
     */
    public synchronized Rect getFramingRect() {
        if (framingRect == null) {
            if (camera == null) {
                return null;
            }
            Point screenResolution = configManager.getScreenResolution();
            if (screenResolution == null) {
                // Called early, before init even finished
                return null;
            }

            int width = findDesiredDimensionInRange(screenResolution.x, MIN_FRAME_WIDTH, MAX_FRAME_WIDTH);
            int height = findDesiredDimensionInRange(screenResolution.y, MIN_FRAME_HEIGHT, MAX_FRAME_HEIGHT);

            int leftOffset = (screenResolution.x - width) / 2;
            int topOffset = (screenResolution.y - height) / 2;
            framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
            Log.d(TAG, "Calculated framing rect: " + framingRect);
        }
        return framingRect;
    }

    /**
     * A single preview frame will be returned to the handler supplied. The data will arrive as byte[] in the message.obj field, with width and height encoded as message.arg1 and message.arg2,
     * respectively.
     * 
     * @param handler
     *            The handler to send the message to.
     * @param message
     *            The what field of the message to be sent.
     */
    public synchronized void requestPreviewFrame(Handler handler, int message) {
        Camera theCamera = camera;
        if (theCamera != null && previewing) {
            previewCallback.setHandler(handler, message);
            theCamera.setOneShotPreviewCallback(previewCallback);
        }
    }

    /**
     * Like {@link #getFramingRect} but coordinates are in terms of the preview frame, not UI / screen.
     */
    public synchronized Rect getFramingRectInPreview() {
        if (framingRectInPreview == null) {
            Rect framingRect = getFramingRect();
            if (framingRect == null) {
                return null;
            }
            Rect rect = new Rect(framingRect);
            Point cameraResolution = configManager.getCameraResolution();
            Point screenResolution = configManager.getScreenResolution();
            if (cameraResolution == null || screenResolution == null) {
                // Called early, before init even finished
                return null;
            }
            rect.left = rect.left * cameraResolution.x / screenResolution.x;
            rect.right = rect.right * cameraResolution.x / screenResolution.x;
            rect.top = rect.top * cameraResolution.y / screenResolution.y;
            rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;
            framingRectInPreview = rect;
        }
        return framingRectInPreview;
    }

    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        Rect rect = getFramingRectInPreview();
        if (rect == null) {
            return null;
        }
        // Go ahead and assume it's YUV rather than die.
        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                rect.width(), rect.height(), false);
    }

    private int findDesiredDimensionInRange(int resolution, int hardMin, int hardMax) {
        int dim = resolution / 2; // Target 50% of each dimension
        if (dim < hardMin) {
            return hardMin;
        }
        if (dim > hardMax) {
            return hardMax;
        }
        return dim;
    }

    final class PreviewCallback implements Camera.PreviewCallback {
        private static final String TAG = "CameraManager.PreviewCallback";

        private final CameraConfigurationManager configManager;
        private Handler previewHandler;
        private int previewMessage;

        PreviewCallback(CameraConfigurationManager configManager) {
            this.configManager = configManager;
        }

        void setHandler(Handler previewHandler, int previewMessage) {
            this.previewHandler = previewHandler;
            this.previewMessage = previewMessage;
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
        }
    }
}