package aasgmkrm.colormethis;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

/**
 * Created by smercier91 on 4/6/15.
 */
public class Workspace extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workspace);

        Intent intent = getIntent();
        String message = intent.getStringExtra(ColorMeThis.WORKSPACE_MESSAGE);

        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, message, duration);
        toast.show();

        File imgFile = new File(message);
        if(imgFile.exists()) {

            Toast toast2 = Toast.makeText(context, "SUCCESS!", duration);
            toast2.show();

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            ImageView myImage = (ImageView) findViewById(R.id.selection);
            myImage.setImageBitmap(myBitmap);
        }


        //ImageView jpgView = (ImageView)findViewById(R.id.workspace);
        //Bitmap bitmap = BitmapFactory.decodeFile(message);
        //jpgView.setImageBitmap(bitmap);
    }

}
