package aasgmkrm.colormethis;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Kevin on 5/2/2015.
 */

public class PaletteAdapter extends BaseAdapter {
    Context context;

    protected List<PaletteColor> listPalettes;
    LayoutInflater inflater;

    public PaletteAdapter(Context context, List<PaletteColor> listPalettes) {
        this.listPalettes = listPalettes;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
    }

    public int getCount() {
        return listPalettes.size();
    }

    public PaletteColor getItem(int position) {
        return listPalettes.get(getCount() - position - 1);
    }

    public long getItemId(int position) {
        return listPalettes.get(getCount() - position - 1).getId();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {

            holder = new ViewHolder();
            convertView = this.inflater.inflate(R.layout.palette_item,
                    parent, false);

            holder.colorDisplayer = (GradientDrawable) convertView
                    .findViewById(R.id.color_display_box).getBackground();
            holder.textColorName = (TextView) convertView
                    .findViewById(R.id.text_color_name);
            holder.textColorHex = (TextView) convertView
                    .findViewById(R.id.text_color_hex);
            holder.textColorRed = (TextView) convertView
                    .findViewById(R.id.text_color_red);
            holder.textColorGreen = (TextView) convertView
                    .findViewById(R.id.text_color_green);
            holder.textColorBlue = (TextView) convertView
                    .findViewById((R.id.text_color_blue));

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PaletteColor palette = listPalettes.get(getCount() - position - 1);

        holder.colorDisplayer.setColor(palette.getColor());
        holder.textColorName.setText(palette.getColorName());
        holder.textColorHex.setText(palette.getColorHex());
        holder.textColorRed.setText(Integer.toString(palette.getColorRed()));
        holder.textColorGreen.setText(Integer.toString(palette.getColorGreen()));
        holder.textColorBlue.setText(Integer.toString(palette.getColorBlue()));

        return convertView;
    }

    private class ViewHolder {
        GradientDrawable colorDisplayer;
        TextView textColorName;
        TextView textColorHex;
        TextView textColorRed;
        TextView textColorGreen;
        TextView textColorBlue;
    }

}