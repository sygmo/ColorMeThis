package aasgmkrm.colormethis;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.ClipboardManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Kevin on 5/2/2015.
 */

public class PaletteActivity extends ActionBarActivity {
    MySQLiteHelper db;
    ListView lvPalettes;
    List<PaletteColor> savedPalettes;
    PaletteColor selectedPalette;
    PaletteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_palette);

        db = new MySQLiteHelper(this);

        lvPalettes = (ListView) findViewById(R.id.paletteList);
        savedPalettes = db.getAllPaletteColors();
        adapter = new PaletteAdapter(this, savedPalettes);
        lvPalettes.setAdapter(adapter);

        lvPalettes.setOnItemLongClickListener(new PaletteItemListener());
    }

    private class PaletteItemListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            selectedPalette = savedPalettes.get(savedPalettes.size() - position - 1);
            showDialog(0);
            return true;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String copy_hex = "Copy hex to clipboard";
        String delete_palette = "Delete this color from palette";
        CharSequence[] options = new CharSequence[] { copy_hex, delete_palette };

        builder.setTitle(R.string.option);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == 0) {
                    ClipboardManager clipboard = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(selectedPalette.getColorHex());

                    Context context = getApplicationContext();
                    CharSequence text = "Copied " +
                            selectedPalette.getColorHex() +
                            " to the clipboard.";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }

                else {
                    builder.setMessage(R.string.delete_question).setCancelable(false)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface st, int id) {
                                    db.deletePaletteColor(selectedPalette);
                                    savedPalettes.clear();
                                    savedPalettes.addAll(db.getAllPaletteColors());
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton(R.string.no, null);
                    builder.show();
                }
            }
        });

        return builder.show();
    }
}
