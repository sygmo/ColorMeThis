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
    private int color_red;
    private int color_green;
    private int color_blue;
    private String color_hex;

    public PaletteColor() {}

    public PaletteColor(int color,
                        String color_name,
                        int color_red,
                        int color_green,
                        int color_blue,
                        String color_hex) {
        super();
        this.color = color;
        this.color_name = color_name;
        this.color_red = color_red;
        this.color_green = color_green;
        this.color_blue = color_blue;
        this.color_hex = color_hex;
    }

    // Getters & setters

    @Override
    public String toString() {
        return "PaletteColor [id=" + id + ", " +
                "color="      + color      + ", " +
                "color_name=" + color_name + ", " +
                "color_red="  + color_red  + ", " +
                "color_green="  + color_green  + ", " +
                "color_blue="  + color_blue  + ", " +
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
    public int getColorRed() { return color_red; }
    public int getColorGreen() { return color_green; }
    public int getColorBlue() { return color_blue; }
    public String getColorHex() { return color_hex; }

    public void setId(int new_id) { id = new_id; }
    public void setColor(int new_color) { color = new_color; }
    public void setColorName(String new_color_name) { color_name = new_color_name; }
    public void setColorRed(int new_color_red) { color_red = new_color_red; }
    public void setColorGreen(int new_color_green) { color_green = new_color_green; }
    public void setColorBlue(int new_color_blue) { color_blue = new_color_blue; }
    public void setColorHex(String new_color_hex) { color_hex = new_color_hex; }
}