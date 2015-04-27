package aasgmkrm.colormethis;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
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
import android.view.Window;
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

    @SuppressWarnings("unused")
    private static final float MIN_ZOOM = 0.4f, MAX_ZOOM = 2.0f;

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

    // Widths and heights
    int bitmapWidth;
    int bitmapHeight;
    int reqWidth;
    int reqHeight;

    private float density;
    RectF displayRect = new RectF();

    GradientDrawable colorDisplayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the action bar.
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();

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
            reqWidth = size.x;
            reqHeight = size.y;

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

            // Used for scroll limit
            bitmapWidth = myBitmap.getWidth();
            bitmapHeight = myBitmap.getHeight();

            Log.d(TAG, "SIZE OF IMAGE: " + bitmapWidth + " x " + bitmapHeight);
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

        density = getResources().getDisplayMetrics().density;

        float dx; // postTranslate X distance
        float dy; // postTranslate Y distance
        float[] matrixValues = new float[9];
        float matrixX = 0; // X coordinate of matrix inside the ImageView
        float matrixY = 0; // Y coordinate of matrix inside the ImageView
        float width = 0; // width of drawable
        float height = 0; // height of drawable

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

                int color = getColor(x, y);

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

                /** Restricting panning and zooming
                 *  Source:
                 *  http://stackoverflow.com/questions/5385071/restricting-panning-and-zooming
                 *  */
                if (mode == DRAG){
                    matrix.set(savedMatrix);
                    // create the transformation in the matrix  of points
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                }

                else if (mode == ZOOM) {
                    // pinch zooming
                    float newDist = spacing(event);
                    Log.d(TAG, "newDist=" + newDist);

                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        scale = newDist / oldDist; // setting the scaling of the

                        // matrix...if scale > 1 means
                        // zoom in...if scale < 1 means
                        // zoom out

                        float[] f = new float[9];
                        matrix.getValues(f);
                        float currentScale = f[Matrix.MSCALE_X];
                        displayRect.set(0, 0, reqWidth, reqHeight);
                        float minZoom = Math.min(displayRect.width() / bitmapWidth,
                                displayRect.height() / bitmapHeight);
                        float maxZoom = MAX_ZOOM * density;

                        if (scale * currentScale > maxZoom)
                            scale = maxZoom / currentScale;

                        else if (scale * currentScale < minZoom)
                            scale = minZoom / currentScale;

                        adjustPan();
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }

        adjustPan();
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

    private int getColor(int x, int y) {
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
        return color;
    }

    private void adjustPan() {
        displayRect.set(0, 0, reqWidth, reqHeight);
        float[] matrixValues = new float[9];
        matrix.getValues(matrixValues);
        float currentY = matrixValues[Matrix.MTRANS_Y];
        float currentX = matrixValues[Matrix.MTRANS_X];
        float currentScale = matrixValues[Matrix.MSCALE_X];
        float currentHeight = bitmapHeight * currentScale;
        float currentWidth =  bitmapWidth * currentScale;
        float newX = currentX;
        float newY = currentY;

        RectF drawingRect = new RectF(newX, newY, newX + currentWidth,
                newY + currentHeight);
        float diffUp = Math.min(displayRect.bottom - drawingRect.bottom,
                displayRect.top - drawingRect.top);
        float diffDown = Math.max(displayRect.bottom - drawingRect.bottom,
                displayRect.top - drawingRect.top);
        float diffLeft = Math.min(displayRect.left - drawingRect.left,
                displayRect.right - drawingRect.right);
        float diffRight = Math.max(displayRect.left - drawingRect.left,
                displayRect.right - drawingRect.right);

        float x = 0, y = 0;

        if (diffUp > 0)
            y += diffUp;
        if (diffDown < 0)
            y += diffDown;
        if (diffLeft > 0)
            x += diffLeft;
        if (diffRight < 0)
            x += diffRight;

        if(currentWidth < displayRect.width())
            x = -currentX + (displayRect.width() - currentWidth) / 2;
        if(currentHeight<displayRect.height())
            y = -currentY + (displayRect.height() - currentHeight) / 2;

        matrix.postTranslate(x, y);
    }
}