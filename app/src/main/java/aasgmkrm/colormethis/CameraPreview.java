package aasgmkrm.colormethis;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private Camera mCamera;
    private Camera.Parameters params;
    private float mDist;
    public SurfaceHolder mSurfaceHolder;
    private boolean isPreviewRunning;

    private static final String SURFACE_TAG = "Surface";
    private static final String CAM_TAG = "CAMERA";

    // Constructor that obtains context and camera
    public CameraPreview(Context context, Camera camera) {
        super(context);
        setMCamera(camera);
        params = mCamera.getParameters();
        this.mSurfaceHolder = this.getHolder();
        this.mSurfaceHolder.addCallback(this);
        this.mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.setDisplayOrientation(90);

            /** Camera.Parameters used to save picture in correct orientation:
             *  http://stackoverflow.com/questions/17782806/camera-app-rotates-images-by-90-degrees
             */
            params.set("jpeg-quality", 72);
            params.set("rotation", 90);
            params.set("orientation", "portrait");
            params.setPictureFormat(PixelFormat.JPEG);
            mCamera.setParameters(params);

            previewStart();
        } catch (NullPointerException e) {
            Log.d(SURFACE_TAG, "SurfaceCreated: NPE " + e.getMessage());
        } catch (IOException e) {
            Log.d(SURFACE_TAG, "SurfaceCreated: IOE " + e.getMessage());
        } catch (RuntimeException e) {
            Log.d(SURFACE_TAG, "SurfaceCreated: RTE " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if(mCamera != null) {
            previewStop();
            Log.d(CAM_TAG, "Release in surfaceDestroyed");
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
        } catch (NullPointerException e) {
            Log.d(SURFACE_TAG, "SurfaceChanged: NPE " + e.getMessage());
        } catch (IOException e) {
            Log.d(SURFACE_TAG, "SurfaceChanged: IOE " + e.getMessage());
        } catch (RuntimeException e) {
            Log.d(SURFACE_TAG, "SurfaceChanged: RTE " + e.getMessage());
        }
    }


    /** Camera pinch-to-zoom
     *  Source: http://stackoverflow.com/questions/8120753/android-camera-preview-zoom-using-double-finger-touch
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Get the pointer ID
        int action = event.getAction();

        if (event.getPointerCount() > 1) {
            if (action == MotionEvent.ACTION_POINTER_DOWN) { mDist = spacing(event); }

            else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
                int maxZoom = params.getMaxZoom();
                int zoom = params.getZoom();
                float newDist = spacing(event);

                if (newDist > mDist) {
                    if (zoom < maxZoom - (maxZoom % 2))
                        zoom += 2;
                }

                else if (newDist < mDist) {
                    if (zoom > 0)
                        zoom -= 2;
                }

                mDist = newDist;
                params.setZoom(zoom);
                mCamera.setParameters(params);
            }
        }

        return true;
    }


    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }


    public void setMCamera(Camera camera){
        mCamera = camera;
        this.mSurfaceHolder = this.getHolder();
        this.mSurfaceHolder.addCallback(this);
    }

    public void previewStart() {
        if (!isPreviewRunning && (mCamera != null)) {
            mCamera.startPreview();
            isPreviewRunning = true;
        }
    }

    public void previewStop () {
        if (isPreviewRunning && (mCamera != null)) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            isPreviewRunning = false;
        }
    }
}