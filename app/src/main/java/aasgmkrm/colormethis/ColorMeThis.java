package aasgmkrm.colormethis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ColorMeThis extends Activity {

    private String TAG = "Color Me This!";

    /** Camera initializations */
    private Camera mCamera;
    private CameraPreview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Create an instance of Camera.
        mCamera = getCameraInstance();

        /** Metering and focus areas. */

        // Set Camera parameters.
        Camera.Parameters params = mCamera.getParameters();

        if (params.getMaxNumMeteringAreas() > 0) {
            // Check that metering areas are supported.
            List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();

            //Specify an area in the center of the image.
            Rect areaRect1 = new Rect(-100, -100, 100, 100);

            // Set weight to 60%.
            meteringAreas.add(new Camera.Area(areaRect1, 600));

            // Specify area in the upper right corner of the image.
            Rect areaRect2 = new Rect(800, -1000, 1000, 800);

            // Set weight to 40%.
            meteringAreas.add(new Camera.Area(areaRect2, 400));

            params.setMeteringAreas(meteringAreas);
        }

        mCamera.setParameters(params);

        /** Placing preview in a layout. */

        // Create the Preview view and set it as the content of the activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        findViewById(R.id.button_capture).setOnClickListener(mCaptureClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.about:
                return true;
            case R.id.settings:
                startActivityForResult(new Intent(this, Settings.class), 0);
                return true;
            /**
            case R.id.palette:
                return true;
            */
        }
        return false;
    }

    /** For SETTINGS: adjustments to be made at a later time.
     *  Files to check: Settings.java, preferences.xml
     *
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_CANCELED) {
            ...
        }
    }
    */

    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);

            if (pictureFile == null) {
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    // Add a listener to the Capture button.
    View.OnClickListener mCaptureClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Get an image from the camera.
            mCamera.takePicture(null, null, mPicture);
        }
    };

    /** Camera components:
     * http://developer.android.com/guide/topics/media/camera.html#custom-camera
    */

    /** Detecting camera hardware: check if this device has a camera. */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
            return true;
        else
            return false;
    }

    /** Accessing cameras: a safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance() {
        Camera c = null;

        try {
            c = Camera.open();  // Attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }

        return c; // Returns null if camera is not available
    }

    /** Checking camera features:
     *  Camera.getParameters()
     *  Camera.getCameraInfo()
     */

    /** Creating a preview class: a basic camera preview class. */
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the underlying surface is
            // created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);

            // Deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            //The Surface has been created, now tell the camera where to draw the preview.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // Empty.  Take care of releasing the Camera preview in the activity.
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If the preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null) {
                // Preview surface does not exist.
                return;
            }

            // Stop preview before making changes.
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                // Ignore: tried to stop a non-existent preview.
            }

            // Set preview size and make any resize, rotate, or reformatting changes here.
            // When setting preview size, values from getSupportedPreviewSizes() MUST be used.

            // Start preview with new settings.
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            } catch(Exception e) {
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }
    }

    /** Saving media files */
    public static final int MEDIA_TYPE_IMAGE = 1;

    /** Create a file Uri for saving an image. */
    public static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image. */
    private static File getOutputMediaFile(int type) {
        // To be safe, check that the SD Card is mounted using
        // Environment.getExternalStorageState() before doing this.

        // This location works best if you want the created images to be shared between
        // applications and persist after the app has been uninstalled.

        File mediaStorageDir = new File
                (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "ColorMeThis");

        // Create the storage directory if it does not exist.
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("ColorMeThis", "failed to create directory.");
                return null;
            }
        }

        // Create a media file name.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" +
                    timeStamp + ".jpg");
        }

        else
            return null;

        return mediaFile;
    }
}
