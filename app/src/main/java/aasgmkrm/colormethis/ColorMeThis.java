package aasgmkrm.colormethis;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ColorMeThis extends Activity {
    private static final String TAG = "Color Me This!";
    private static final String PIC_TAG = "Picture";

    /** Camera declarations. */
    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private ImageButton mCaptureButton;
    private FrameLayout preview;
    private Bitmap mBitmap;
    private ImageView mImageView;

    // touch coordinates
    private float x;
    private float y;

    /** Grab photo declarations. */
    private static final int SELECT_PICTURE = 1;
    private String selectedImagePath;
    private ImageView mGrabPhotoView;

    // Others
    private final int DIALOG_QUIT_ID = 0;
    public final static String WORKSPACE_MESSAGE = "aasgmkrm.colormethis.MESSAGE";

    // Screen width and height
    int reqWidth;
    int reqHeight;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mCamera = getCameraInstance();
        mCameraPreview = new CameraPreview(this, mCamera);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mCameraPreview);

        mImageView = (ImageView) findViewById(R.id.photo_selected);

        mCaptureButton = (ImageButton) findViewById(R.id.button_capture);
        mCaptureButton.setOnClickListener(new CaptureClickListener());

        mGrabPhotoView = (ImageView) findViewById(R.id.grab_photo);

        setGrabPhotoImage();
        mGrabPhotoView.setOnClickListener(new GrabClickListener());
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
            releaseCameraAndPreview();
        } catch(Exception e){}
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(PIC_TAG, "Inside onResume");
        mCamera = getCameraInstance();
        mCameraPreview.setMCamera(mCamera);
        try {
            mCamera.setPreviewCallback(null);
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

            case R.id.palette:
                startActivityForResult(new Intent(this, PaletteActivity.class), 0);
                return true;
        }
        return false;
    }


    /** Dialog for quitting */
    @Override
    public void onBackPressed() {
        showDialog(DIALOG_QUIT_ID);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (id == DIALOG_QUIT_ID)
            dialog = this.createQuitDialog(builder);

        if (dialog == null)
            Log.d(TAG, "Dialog is null.");

        return dialog;
    }

    public Dialog createQuitDialog(AlertDialog.Builder builder) {
        builder.setMessage(R.string.quit_question).setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface st, int id) {
                        ColorMeThis.this.finish();
                    }
                })
                .setNegativeButton(R.string.no, null);
        return builder.create();
    }


    /** Grab the most recent image for the grab photo button */
    private void setGrabPhotoImage() {
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };

        final Cursor cursor = getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                        null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

        if (cursor.moveToFirst()) {

            String imageLocation = cursor.getString(1);
            File imageFile = new File(imageLocation);
            if (imageFile.exists()) {
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                reqWidth = size.x;
                reqHeight = size.y;
                Bitmap bm = Workspace.decodeSampledBitmapFromResource(imageFile, reqWidth, reqHeight);

                mGrabPhotoView.setImageBitmap(bm);
            }
        }
    }


    /** Camera methods */
    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            releaseCameraAndPreview();
            camera = Camera.open(0);
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
        public void onClick (View view) { mCamera.takePicture(null, null, mPicture); }
    }


    /** Saving images to file */
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

                mCamera.startPreview();

                // open image view (workspace)
                Intent intent = new Intent(ColorMeThis.this, Workspace.class);
                intent.putExtra(WORKSPACE_MESSAGE, pictureFile.getPath());
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
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, SELECT_PICTURE);
        }
    }

    /** Taking the image and setting it to workspace */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                ColorMeThis activity = ColorMeThis.this;
                selectedImagePath = getImagePath(data, activity);

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                reqWidth = size.x;
                reqHeight = size.y;
                String message = data.getStringExtra(ColorMeThis.WORKSPACE_MESSAGE);
                if (message != null) {
                    File imgFile = new File(message);
                    mBitmap = Workspace.decodeSampledBitmapFromResource(imgFile, reqWidth, reqHeight);
                }
                mImageView.setImageBitmap(mBitmap);

                if (selectedImagePath != null) {
                    mImageView.setImageBitmap(null);
                    Intent intent = new Intent(ColorMeThis.this, Workspace.class);
                    intent.putExtra(WORKSPACE_MESSAGE, selectedImagePath);
                    startActivity(intent);
                }

                else
                    Log.d(TAG, "The selected image path is NULL.");
            }
        }
    }

    public static String getImagePath(Intent data, Context context) {
        Uri selectedImage = data.getData();
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver()
                .query(selectedImage,filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String selectedImagePath = cursor.getString(columnIndex);
        cursor.close();
        return selectedImagePath;
    }
}