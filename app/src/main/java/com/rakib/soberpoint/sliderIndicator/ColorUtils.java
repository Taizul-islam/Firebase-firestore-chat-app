package com.rakib.soberpoint.sliderIndicator;

import android.graphics.Color;

class ColorUtils {
    static int getScaleRadioColor(float radio, int startColor, int endColor) {
        if (radio < 0) {
            radio = 0;
        }
        int alphaStart = Color.alpha(startColor);
        int redStart = Color.red(startColor);
        int blueStart = Color.blue(startColor);
        int greenStart = Color.green(startColor);
        int alphaEnd = Color.alpha(endColor);
        int redEnd = Color.red(endColor);
        int blueEnd = Color.blue(endColor);
        int greenEnd = Color.green(endColor);

        int alpha = (int) (alphaEnd + ((alphaStart - alphaEnd) * radio));
        int red = (int) (redEnd + ((redStart - redEnd) * radio));
        int greed = (int) (greenEnd + ((greenStart - greenEnd) * radio));
        int blue = (int) (blueEnd + ((blueStart - blueEnd) * radio));
        return Color.argb(alpha, red, greed, blue);
    }
}
