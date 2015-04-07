package aasgmkrm.colormethis;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

/**
 * Created by smercier91 on 4/6/15.
 */
public class Workspace extends ActionBarActivity implements
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener{

    ImageView myImage;
    private GestureDetectorCompat mDetector;

    private static final String DEBUG_TAG = "Gestures";

    // touch coordinates
    private int x;
    private int y;
    private int color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workspace);


        // Instantiate gesture detector
        mDetector = new GestureDetectorCompat(this, this);
        mDetector.setOnDoubleTapListener(this);

        Intent intent = getIntent();
        String message = intent.getStringExtra(ColorMeThis.WORKSPACE_MESSAGE);

        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, message, duration);
        toast.show();

        File imgFile = new File(message);
        if(imgFile.exists()) {

            //Toast toast2 = Toast.makeText(context, "SUCCESS!", duration);
            //toast2.show();

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            myImage = (ImageView) findViewById(R.id.selection);
            myImage.setImageBitmap(myBitmap);
        }

        //ImageView jpgView = (ImageView)findViewById(R.id.workspace);
        //Bitmap bitmap = BitmapFactory.decodeFile(message);
        //jpgView.setImageBitmap(bitmap);
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
        x = (int) event.getX();
        y = (int) event.getY();

        color = Utils.findColor(myImage, x, y);
        Context context = getApplicationContext();
        CharSequence text = "R: " + Color.red(color) + "  G: " + Color.green(color) + "  B: "
                + Color.blue(color);
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

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


}
