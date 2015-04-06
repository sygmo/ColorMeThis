/**
 *  Color Picker by Juan Mart?
 *  Copyright (C) 2010 nauj27.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

//package com.nauj27.android.colorpicker;
package aasgmkrm.colormethis;

import java.util.Iterator;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.view.View;
import android.widget.ImageView;

/**
 * @author nauj27
 *
 */
class Utils {



    /**
     * Fill hex string with "0" when hexString minor than F.
     * @param hexString
     * @return
     */
    public static String beautyHexString(String hexString) {
        if (hexString.length() < 2) {
            return "0".concat(hexString);
        } else {
            return hexString;
        }
    }

    /**
     * Find components of color of the bitmap at x, y.
     * @param x Distance from left border of the View
     * @param y Distance from top of the View
     * @param view Touched surface on screen
     */
    public static int findColor(View view, int x, int y)
            throws NullPointerException {

        int red = 0;
        int green = 0;
        int blue = 0;
        int color = 0;

        int offset = 1; // 3x3 Matrix
        int pixelsNumber = 0;

        int xImage = 0;
        int yImage = 0;

        // Get the bitmap from the view.
        ImageView imageView = (ImageView)view;
        BitmapDrawable bitmapDrawable = (BitmapDrawable)imageView.getDrawable();
        Bitmap imageBitmap = bitmapDrawable.getBitmap();

        // Calculate the target in the bitmap.
        xImage = (int)(x * ((double)imageBitmap.getWidth() / (double)imageView.getWidth()));
        yImage = (int)(y * ((double)imageBitmap.getHeight() / (double)imageView.getHeight()));

        // Average of pixels color around the center of the touch.
        for (int i = xImage - offset; i <= xImage + offset; i++) {
            for (int j = yImage - offset; j <= yImage + offset; j++) {
                try {
                    color = imageBitmap.getPixel(i, j);
                    red += Color.red(color);
                    green += Color.green(color);
                    blue += Color.blue(color);
                    pixelsNumber += 1;
                } catch(Exception e) {
                    //Log.w(TAG, "Error picking color!");
                }
            }
        }
        red = red / pixelsNumber;
        green = green / pixelsNumber;
        blue = blue / pixelsNumber;

        return Color.rgb(red, green, blue);
    }

}