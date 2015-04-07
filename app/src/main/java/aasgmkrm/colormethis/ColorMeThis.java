package aasgmkrm.colormethis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ColorMeThis extends Activity implements
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    private static final String DEBUG_TAG = "Gestures";
    private GestureDetectorCompat mDetector;

    private static final String TAG = "Color Me This!";
    private static final String PIC_TAG = "Picture";

    /** Camera declarations. */
    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private Button mCaptureButton;
    private FrameLayout preview;
    Bitmap mBitmap;

    // touch coordinates
    private float x;
    private float y;

    /** Grab photo declarations. */
    private static final int SELECT_PICTURE = 1;
    private String selectedImagePath;
    private Button mGrabPhotoButton;

    public final static String WORKSPACE_MESSAGE = "aasgmkrm.colormethis.MESSAGE";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Instantiate gesture detector
        mDetector = new GestureDetectorCompat(this, this);
        mDetector.setOnDoubleTapListener(this);

        mCamera = getCameraInstance();
        mCameraPreview = new CameraPreview(this, mCamera);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mCameraPreview);

        mCaptureButton = (Button) findViewById(R.id.button_capture);
        mCaptureButton.setOnClickListener(new CaptureClickListener());

        mGrabPhotoButton = (Button) findViewById(R.id.grab_photo);
        mGrabPhotoButton.setOnClickListener(new GrabClickListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent event) {
        Log.d(DEBUG_TAG,"onDown: " + event.toString());
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
        Log.d(DEBUG_TAG, "onFling: " + event1.toString()+event2.toString());
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        Log.d(DEBUG_TAG, "onLongPress: " + event.toString());
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                float distanceY) {
        Log.d(DEBUG_TAG, "onScroll: " + e1.toString()+e2.toString());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        Log.d(DEBUG_TAG, "onShowPress: " + event.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        x = event.getX();
        y = event.getY();

        Log.d(DEBUG_TAG, "x: " + x + ", y: " + y + ", onSingleTapUp: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        Log.d(DEBUG_TAG, "onDoubleTapEvent: " + event.toString());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + event.toString());
        return true;
    }

    /** Release the camera in onPause(). */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(PIC_TAG, "inside onPause");

        try{
            mCameraPreview.previewStop();
            mCamera.setPreviewCallback(null);
            Log.d(PIC_TAG, "release in onPause");
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        } catch(Exception e){}
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d(PIC_TAG, "Inside onResume");
        mCamera = getCameraInstance();
        mCameraPreview.setMCamera(mCamera);
        try {
            mCamera.setPreviewCallback(null);

            //mCamera.setPreviewCallback(null);
            //mCameraPreview = new CameraPreview(this, mCamera);//set preview
            //preview.addView(mCameraPreview);

            mCameraPreview.previewStart();
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate our menu which can gather user input for switching camera
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
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

    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            releaseCameraAndPreview();
            camera = Camera.open();
        } catch (Exception e) {
            Log.d(TAG, "Camera in use or does not exist: " + e.getMessage());
        }
        return camera;
    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            Log.d(PIC_TAG, "release in releaseCameraAndPreview");
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }
    }

    private class CaptureClickListener implements View.OnClickListener {
        public void onClick (View view) {
            mCamera.takePicture(null, null, mPicture);
        }
    }

    PictureCallback mPicture = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

                Context context = getApplicationContext();
                CharSequence text = "Photo saved to " + pictureFile.getPath();
                int duration = Toast.LENGTH_SHORT;
                Log.d(PIC_TAG, pictureFile.getPath());

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                mCamera.startPreview();

                // open image view (workspace)
                Intent intent = new Intent(ColorMeThis.this, Workspace.class);
                intent.putExtra(WORKSPACE_MESSAGE, pictureFile.getPath().toString());
                startActivity(intent);



            } catch (FileNotFoundException e) {
                Log.d(PIC_TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(PIC_TAG, "IOException: " + e.getMessage());
            }
        }
    };

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES),
                        "ColorMeThis");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"
                + timeStamp + ".jpg");

        return mediaFile;
    }

    private class GrabClickListener implements View.OnClickListener {
        public void onClick (View view) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
            }
        }

    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if(cursor != null){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }

        else
            return null;
    }
}
