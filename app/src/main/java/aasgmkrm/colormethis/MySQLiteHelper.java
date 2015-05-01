package aasgmkrm.colormethis;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Kevin on 4/28/2015.
 */


public class MySQLiteHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "MyDatabase";

    /** Constants for table & column names */
    private static final String TABLE_PALETTE = "palette";
    private static final String KEY_ID = "id";
    private static final String KEY_COLOR = "color";
    private static final String KEY_COLOR_NAME = "color_name";
    private static final String KEY_COLOR_RED = "color_red";
    private static final String KEY_COLOR_GREEN = "color_green";
    private static final String KEY_COLOR_BLUE = "color_blue";
    private static final String KEY_COLOR_HEX = "color_hex";

    private static final String[] COLUMNS = {KEY_ID,
            KEY_COLOR,
            KEY_COLOR_NAME,
            KEY_COLOR_RED,
            KEY_COLOR_GREEN,
            KEY_COLOR_BLUE,
            KEY_COLOR_HEX};

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create book table
        String CREATE_PALETTE_TABLE = "CREATE TABLE palette ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "color INTEGER, "+
                "color_name TEXT, " +
                "color_red INTEGER " +
                "color_green INTEGER, " +
                "color_blue INTEGER, " +
                "color_hex TEXT )";

        // create books table
        db.execSQL(CREATE_PALETTE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older books table if existed
        db.execSQL("DROP TABLE IF EXISTS palette");

        // create fresh books table
        this.onCreate(db);
    }


    // add color to palette
    public void addPaletteColor(PaletteColor pc){
        // for logging
        Log.d("addColor", pc.toString());

        // get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_COLOR, pc.getColor());
        values.put(KEY_COLOR_NAME, pc.getColorName());
        values.put(KEY_COLOR_RED, pc.getColorRed());
        values.put(KEY_COLOR_GREEN, pc.getColorGreen());
        values.put(KEY_COLOR_BLUE, pc.getColorBlue());
        values.put(KEY_COLOR_HEX, pc.getColorHex());

        // insert
        db.insert(TABLE_PALETTE, null, values);

        // close
        db.close();
    }


    // get color from palette
    public PaletteColor getPaletteColor(int id){

        // get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // build query
        Cursor cursor =
                db.query(TABLE_PALETTE, // table
                COLUMNS, // column names
                " id = ?", // selections
                new String[] { String.valueOf(id) }, // selections args
                null, // group by
                null, // having
                null, // order by
                null); // limit

        // if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();

        // build paletteColor object
        PaletteColor pc = new PaletteColor();
        pc.setId(Integer.parseInt(cursor.getString(0)));
        pc.setColor(Integer.parseInt(cursor.getString(1)));
        pc.setColorName(cursor.getString(2));
        pc.setColorRed(Integer.parseInt(cursor.getString(3)));
        pc.setColorGreen(Integer.parseInt(cursor.getString(4)));
        pc.setColorBlue(Integer.parseInt(cursor.getString(5)));
        pc.setColorHex(cursor.getString(6));

        // log
        Log.d("getPaletteColor("+id+")", pc.toString());

        // return paletteColor
        return pc;
    }

    public List<PaletteColor> getAllPaletteColors() {
        List<PaletteColor> colorsList = new LinkedList<PaletteColor>();

        // build the query
        String query = "SELECT * FROM " + TABLE_PALETTE;

        // get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // go over each row, build color and add it to list
        PaletteColor pc = null;
        if (cursor.moveToFirst()) {
            do {
                pc = new PaletteColor();
                pc.setId(Integer.parseInt(cursor.getString(0)));
                pc.setColor(Integer.parseInt(cursor.getString(1)));
                pc.setColorName(cursor.getString(2));
                pc.setColorRed(Integer.parseInt(cursor.getString(3)));
                pc.setColorGreen(Integer.parseInt(cursor.getString(4)));
                pc.setColorBlue(Integer.parseInt(cursor.getString(5)));
                pc.setColorHex(cursor.getString(6));

                // add color to colorsList
                colorsList.add(pc);
            } while (cursor.moveToNext());
        }

        Log.d("getAllPaletteColors()", colorsList.toString());

        // return colorsList
        return colorsList;
    }


    public int updatePaletteColor(PaletteColor pc) {

        // get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_COLOR, pc.getColor());
        values.put(KEY_COLOR_NAME, pc.getColorName());
        values.put(KEY_COLOR_RED, pc.getColorRed());
        values.put(KEY_COLOR_GREEN, pc.getColorGreen());
        values.put(KEY_COLOR_BLUE, pc.getColorBlue());
        values.put(KEY_COLOR_HEX, pc.getColorHex());

        // updating row
        int i = db.update(TABLE_PALETTE, // table
                values, // values
                KEY_ID+" = ?", // selections
                new String[] { String.valueOf(pc.getId()) }); // selection args

        // close
        db.close();

        return i;
    }


    // delete color entry
    public void deletePaletteColor(PaletteColor pc) {

        // get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // delete
        db.delete(TABLE_PALETTE, KEY_ID+" = ?", new String[] { String.valueOf(pc.getId()) });

        // close
        db.close();

        // log
        Log.d("deletePaletteColor", pc.toString());
    }
}
