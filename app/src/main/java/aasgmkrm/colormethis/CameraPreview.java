package aasgmkrm.colormethis;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;

    private static final String CAM_PREVIEW_TAG = "CameraPreview";
    private static final String SURFACE_TAG = "Surface";

    // Constructor that obtains context and camera
    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.mCamera = camera;
        this.mSurfaceHolder = this.getHolder();
        this.mSurfaceHolder.addCallback(this);
        this.mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(CAM_PREVIEW_TAG, "IOException: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.stopPreview();
        mCamera.release();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        mCamera.setDisplayOrientation(90);
        try {
            mCamera.setPreviewDisplay(surfaceHolder);

            /** Camera.Parameters used to save picture in correct orientation:
             *  http://stackoverflow.com/questions/17782806/camera-app-rotates-images-by-90-degrees
             */
            Camera.Parameters params = mCamera.getParameters();
            params.set("jpeg-quality", 72);
            params.set("rotation", 90);
            params.set("orientation", "portrait");
            params.setPictureFormat(PixelFormat.JPEG);
            mCamera.setParameters(params);

            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(SURFACE_TAG, "Exception: " + e.getMessage());
        }
    }
}