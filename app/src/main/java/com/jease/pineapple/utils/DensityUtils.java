package com.jease.pineapple.utils;

import android.content.res.Resources;
import android.util.DisplayMetrics;

public class DensityUtils {

    public static float sp2px(float sp) {
        float scale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

    public static float px2sp(float px) {
        return px / Resources.getSystem().getDisplayMetrics().scaledDensity;
    }

    public static float px2dp(float px) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float dp2px(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
