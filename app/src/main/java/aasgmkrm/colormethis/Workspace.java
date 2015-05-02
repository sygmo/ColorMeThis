package aasgmkrm.colormethis;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.ClipboardManager;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    private static final float MAX_ZOOM = 3.0f;

    private float density;
    RectF displayRect = new RectF();

    // For displayer:
    GradientDrawable colorDisplayer;
    TextView colorNameDisplayer;
    TextView hexDisplayer;
    TextView rgbDisplayer;

    // For toasts:
    private Context context;
    private CharSequence text;
    private int duration;
    private Toast toast;

    // For settings:
    private SharedPreferences mPrefs;
    private boolean mColorNameOn = true;
    private boolean mColorRGBOn = true;
    private boolean mColorHexOn = true;
    private ImageButton copyOrSave;

    // For database:
    MySQLiteHelper db;
    int colorBox;
    String colorName;
    int colorRed;
    int colorGreen;
    int colorBlue;
    String colorHex;

    TextView red_text;
    TextView green_text;
    TextView blue_text;

    private ImageView mGrabPhotoView;
    private static final int SELECT_PICTURE = 1;
    private String selectedImagePath;

    private ImageButton returnToCamera;
    private ImageButton settingsBtn;

    private Bitmap mBitmap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the action bar.
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();

        setContentView(R.layout.workspace);

        mPrefs = getSharedPreferences("cmt_prefs", MODE_PRIVATE);
        getSettings();

        Intent intent = getIntent();
        String message = intent.getStringExtra(ColorMeThis.WORKSPACE_MESSAGE);

        colorDisplayer = (GradientDrawable) findViewById(R.id.color_display_box).getBackground();
        colorNameDisplayer = (TextView) findViewById(R.id.color_name);
        hexDisplayer = (TextView) findViewById(R.id.hex_displayer);
        rgbDisplayer = (TextView) findViewById(R.id.rgb_displayer);
        red_text = (TextView) findViewById(R.id.red_value_text);
        green_text = (TextView) findViewById(R.id.green_value_text);
        blue_text = (TextView) findViewById(R.id.blue_value_text);

        mGrabPhotoView = (ImageView) findViewById(R.id.grab_photo);

        setGrabPhotoImage();
        mGrabPhotoView.setOnClickListener(new GrabClickListener());

        settingsBtn = (ImageButton) findViewById(R.id.settingsbtn);
        settingsBtn.setOnClickListener(new SettingsClickListener());

        returnToCamera = (ImageButton) findViewById(R.id.camera_back);
        returnToCamera.setOnClickListener(new goBackToCameraListener());

        db = new MySQLiteHelper(this);
        copyOrSave = (ImageButton) findViewById(R.id.add_to_library);

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

            copyOrSave.setEnabled(true);
            copyOrSave.setOnLongClickListener(new CopyOrSaveListener());
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Workspace activity = Workspace.this;
                selectedImagePath = getImagePath(data, activity);
                mBitmap = BitmapFactory.decodeFile(selectedImagePath);
                myImage.setImageBitmap(mBitmap);

                /*
                if (selectedImagePath != null) {
                    // open image view (workspace)
                    myImage.setImageBitmap(null);
                }

                else
                    Log.d(TAG, "The selected image path is NULL.");
            }*/
        }
    }
    }

    private class goBackToCameraListener implements View.OnClickListener{
        public void onClick(View view){
            finish();
        }
    }

    private class GrabClickListener implements View.OnClickListener {
        public void onClick (View view) {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, SELECT_PICTURE);
        }
    }

    private class SettingsClickListener implements View.OnClickListener{
        public void onClick (View view){
            startActivity(new Intent(getApplicationContext(), Settings.class));
        }
    }

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
                Bitmap bm = BitmapFactory.decodeFile(imageLocation);
                mGrabPhotoView.setImageBitmap(bm);
            }
        }
    }

    // Immersive full-screen mode: for API level 19 and above.
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            myImage.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }


    private void setColorDisplayer(int color){
        colorName = getClosestColor(color);
        colorRed = Color.red(color);
        colorGreen = Color.green(color);
        colorBlue = Color.blue(color);
        colorHex = String.format("#%06X", (0xFFFFFF & color));

        colorDisplayer.setColor(color);
        if (mColorNameOn) { colorNameDisplayer.setText(colorName); }
        if (mColorHexOn) { hexDisplayer.setText("Hex: "+ colorHex); }
        if (mColorRGBOn) {
            red_text.setText("" + colorRed);
            green_text.setText("" + colorGreen);
            blue_text.setText("" + colorBlue);
        }
        Log.d("ColorHex", colorHex);
    }


    /** Save to palette button:
     *  Store the color, color name, color RGB and color hex to the database.
     *  */
    private class CopyOrSaveListener implements View.OnLongClickListener {
        public boolean onLongClick(View v) {
            showDialog(0);
            return true;
        }
    }




    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String copy_hex = "Copy hex to clipboard";
        String save_palette = "Save color to palette";
        CharSequence[] options = new CharSequence[] { copy_hex, save_palette };

        builder.setTitle("Choose an option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == 0) {
                    ClipboardManager clipboard = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(colorHex);

                    context = getApplicationContext();
                    text = "Copied " + colorHex + " to the clipboard.";
                    duration = Toast.LENGTH_SHORT;
                    toast = Toast.makeText(context, text, duration);
                    toast.show();
                }

                else {
                    db.addPaletteColor(new PaletteColor(colorBox,
                            colorName,
                            colorRed,
                            colorGreen,
                            colorBlue,
                            colorHex));

                    Log.d(TAG, "Added to database: " +
                            colorBox + ", " +
                            colorName + ", " +
                            colorRed + ", " +
                            colorGreen + ", " +
                            colorBlue + ", " +
                            colorHex);

                    context = getApplicationContext();
                    text = "Saved " + colorName + " (" + colorHex + ") to the Palette!";
                    duration = Toast.LENGTH_SHORT;
                    toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        });

        return builder.show();
    }


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
        density = getResources().getDisplayMetrics().density;
        float scale;
        dumpEvent(event);

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

                colorBox = getColor(x, y);

/*               try {
                    color = Utils.findColor(myImage, x, y);
                } catch (ArithmeticException e) {
                    Log.d(TAG, "Divide by zero.");
                }*/

                setColorDisplayer(colorBox);

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

    public void getSettings() {
        mColorNameOn = mPrefs.getBoolean("color_name", true);
        mColorHexOn = mPrefs.getBoolean("color_hex", true);
        mColorRGBOn = mPrefs.getBoolean("color_rgb", true);
    }

   public String getClosestColor(int color) {
       Resources r = getApplicationContext().getResources();
       TypedArray all_colors = r.obtainTypedArray(R.array.all_colors);
       String[] all_color_names = r.getStringArray(R.array.all_color_names);
       double closest = Integer.MAX_VALUE;
       String closestColor = "";

       for (int i = 0; i < all_colors.length(); i++) {
           int currentColor = all_colors.getColor(i, 0);
           if (color == currentColor) {
               closestColor = all_color_names[i];
               break;
           } else {

                double d = distance(color, currentColor);

               if (d < closest) {
                   closest = d;
                   closestColor = all_color_names[i];
               }
           }
       }

       return closestColor;
   }

    public double distance(int a, int b) {
        return Math.abs(Color.red(a) - Color.red(b)) +
                Math.abs(Color.green(a) - Color.green(b)) +
                Math.abs(Color.blue(a) - Color.blue(b));
    }
}