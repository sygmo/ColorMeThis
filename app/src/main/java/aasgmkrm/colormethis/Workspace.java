package aasgmkrm.colormethis;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by smercier91 on 4/6/15.
 */
public class Workspace extends ActionBarActivity implements View.OnTouchListener {

    ImageView myImage;
    private static final String TAG = "Gestures";

    // touch coordinates
    private int x;
    private int y;
    private int color;

    @SuppressWarnings("unused")
    private static final float MIN_ZOOM = 0.5f, MAX_ZOOM = 2.5f;

    // These matrices will be used to scale points of the image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix(); // also the min matrix

    // The 3 states (events) which the user is trying to perform
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // these PointF objects are used to record the point(s) the user is touching
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;


    GradientDrawable colorDisplayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workspace);

        Intent intent = getIntent();
        String message = intent.getStringExtra(ColorMeThis.WORKSPACE_MESSAGE);


        colorDisplayer = (GradientDrawable) findViewById(R.id.color_display_box).getBackground();


        File imgFile = new File(message);
        if(imgFile.exists()) {

            // get size of display
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int reqWidth = size.x;
            int reqHeight = size.y;

            // new code
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
            //int imageHeight = options.outHeight;
            //int imageWidth = options.outWidth;
            // String imageType = options.outMimeType;

            Bitmap myBitmap = decodeSampledBitmapFromResource(imgFile, reqWidth, reqHeight);
            //Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            myImage = (ImageView) findViewById(R.id.selection);
            Log.d(TAG, "SIZE OF IMAGE: " + myBitmap.getWidth() + " x " + myBitmap.getHeight());
            myImage.setImageBitmap(myBitmap);
            myImage.setOnTouchListener(this);
        }
    }


    private void setColorDisplayer(int color){
        colorDisplayer.setColor(color);
    }



    // new code
    public static Bitmap decodeSampledBitmapFromResource(File imgFile,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
    }

    // new code
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    /** MotionEvent: Zoom and Scroll
     *  Source:
     *  http://stackoverflow.com/questions/6650398/android-imageview-zoom-in-and-zoom-out
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);
        float scale;
        dumpEvent(event);
        // Handle touch events here...

        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:   // first finger down only
                matrix.set(view.getImageMatrix());
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                Log.d(TAG, "mode=DRAG"); // write to LogCat
                mode = DRAG;
                break;

            case MotionEvent.ACTION_UP: // first finger lifted
                x = (int) event.getX();
                y = (int) event.getY();
                float[] xy = new float[] {x, y};

                Matrix invertMatrix = new Matrix();
                myImage.getImageMatrix().invert(invertMatrix);

                invertMatrix.mapPoints(xy);
                int ex = Integer.valueOf((int)xy[0]);
                int ey = Integer.valueOf((int)xy[1]);

                Drawable imgDrawable = myImage.getDrawable();
                Bitmap bm = ((BitmapDrawable)imgDrawable).getBitmap();

                if (ex < 0)
                    ex = 0;
                else if (ex > bm.getWidth()-1) ex = bm.getWidth()-1;

                if (ey < 0) ey = 0;
                else if (ey > bm.getHeight()-1) ey = bm.getHeight()-1;

                int color = bm.getPixel(ex, ey);

/*                try {
                    color = Utils.findColor(myImage, x, y);
                } catch (ArithmeticException e) {
                    Log.d(TAG, "Divide by zero.");
                }*/

                setColorDisplayer(color);

/*              Context context = getApplicationContext();
                CharSequence text = "R: " + Color.red(color) + "  G: " + Color.green(color) + "  B: "
                        + Color.blue(color);
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();*/

                Log.d(TAG, "x: " + x + ", y: " + y + ", onSingleTapUp: " + event.toString());
                break;

            case MotionEvent.ACTION_POINTER_UP: // second finger lifted
                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;

            case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down
                oldDist = spacing(event);
                Log.d(TAG, "oldDist=" + oldDist);

                if (oldDist > 5f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }

                break;

            case MotionEvent.ACTION_MOVE:

                if (mode == DRAG){
                    matrix.set(savedMatrix);
                    // create the transformation in the matrix  of points
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                }

                else if (mode == ZOOM) {
                    float[] f = new float[9];

                    // pinch zooming
                    float newDist = spacing(event);
                    Log.d(TAG, "newDist=" + newDist);

                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        scale = newDist / oldDist; // setting the scaling of the

                        // matrix...if scale > 1 means
                        // zoom in...if scale < 1 means
                        // zoom out

                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }

                    /** Max and min zoom
                     *  Source:
                     *  http://stackoverflow.com/questions/3881187/
                     *  imageview-pinch-zoom-scale-limits-and-pan-bounds */

                    matrix.getValues(f);
                    float scaleX = f[Matrix.MSCALE_X];
                    float scaleY = f[Matrix.MSCALE_Y];

                    if (scaleX <= MIN_ZOOM) {
                        matrix.postScale((MIN_ZOOM) / scaleX, (MIN_ZOOM) / scaleY, mid.x, mid.y);
                    }

                    else if (scaleX >= 2.5f) {
                        matrix.postScale((MAX_ZOOM) / scaleX, (MAX_ZOOM) / scaleY, mid.x, mid.y);
                    }
                }

                break;
        }

        view.setImageMatrix(matrix); // display the transformation on screen
        return true; // indicate event was handled
    }

    /*
     * --------------------------------------------------------------------------
     * Method: spacing Parameters: MotionEvent Returns: float Description:
     * checks the spacing between the two fingers on touch
     * ----------------------------------------------------
     */

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event) {
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE","POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);

        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP)
        {
            sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }

        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++)
        {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }

        sb.append("]");
        Log.d(TAG, sb.toString());
    }
}