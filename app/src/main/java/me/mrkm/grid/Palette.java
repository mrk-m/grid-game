package me.mrkm.grid;

import android.graphics.Color;

public class Palette {

    public static int DARK = Color.rgb(58, 60, 62);
    public static int BACKGROUND = Color.rgb(58, 60, 62);
    public static int FOREGROUND = Color.LTGRAY;
    public static int TEXT = Color.LTGRAY;
    public static int MIDGROUND = Color.GRAY;
    public static int BLACK = Color.BLACK;

    public static int[] colors = {
            Color.rgb(77, 79, 81),
            Color.rgb(0, 160, 220),
            Color.rgb(141, 108, 171),
            Color.rgb(221, 81, 67),
            Color.rgb(230, 133, 35),
            Color.rgb(0, 174, 179),
            Color.rgb(237, 178, 32),
            Color.rgb(220, 75, 137),
            Color.rgb(124, 184, 47)
    };

    public static int getColor(int id) {
        return colors[id];
    }

    public static void setLight() {
        FOREGROUND = Color.rgb(58, 60, 62);
        BACKGROUND = Color.WHITE;
        MIDGROUND = Color.GRAY;
        colors[0] = Color.LTGRAY;


    }

    public static void setDark() {
        FOREGROUND = Color.LTGRAY;
        BACKGROUND = Color.rgb(58, 60, 62);
        MIDGROUND = Color.GRAY;
        colors[0] = Color.rgb(77, 79, 81);
    }
}
