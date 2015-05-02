package aasgmkrm.colormethis;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Kevin on 5/2/2015.
 */

public class PaletteActivity extends Activity implements AdapterView.OnItemClickListener {
    MySQLiteHelper db;
    ListView lvPalettes;
    List<PaletteColor> savedPalettes;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_palette);

        db = new MySQLiteHelper(this);

        lvPalettes = (ListView) findViewById(R.id.paletteList);
        savedPalettes = db.getAllPaletteColors();
        PaletteAdapter adapter = new PaletteAdapter(this, savedPalettes);
        lvPalettes.setAdapter(adapter);

        lvPalettes.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PaletteColor selectedPalette = savedPalettes.get(position);
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(selectedPalette.getColorHex());

        Context context = getApplicationContext();
        CharSequence text = "Copied " + selectedPalette.getColorHex() + " to the clipboard.";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
