package aasgmkrm.colormethis;

/**
 * Created by Kevin on 4/28/2015.
 */

/** Creating a simple database
 *  Source: http://hmkcode.com/android-simple-sqlite-database-tutorial/
 */

public class PaletteColor {
    private int id;
    private int color;
    private String color_name;
    private String color_rgb;
    private String color_hex;

    public PaletteColor() {}

    public PaletteColor(int color, String color_name, String color_rgb, String color_hex) {
        super();
        this.color = color;
        this.color_name = color_name;
        this.color_rgb = color_rgb;
        this.color_hex = color_hex;
    }

    // Getters & setters

    @Override
    public String toString() {
        return "PaletteColor [id=" + id + ", " +
                "color="      + color      + ", " +
                "color_name=" + color_name + ", " +
                "color_rgb="  + color_rgb  + ", " +
                "color_hex="  + color_hex  + ", " +
                "]";
    }

    public int getId() { return id; }
    public int getColor() {
        return color;
    }
    public String getColorName() {
        return color_name;
    }
    public String getColorRGB() { return color_rgb; }
    public String getColorHex() { return color_hex; }

    public void setId(int new_id) { id = new_id; }
    public void setColor(int new_color) { color = new_color; }
    public void setColorName(String new_color_name) { color_name = new_color_name; }
    public void setColorRGB(String new_color_rgb) { color_rgb = new_color_rgb; }
    public void setColorHex(String new_color_hex) { color_hex = new_color_hex; }
}