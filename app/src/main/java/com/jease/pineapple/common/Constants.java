package com.jease.pineapple.common;

import com.jease.pineapple.R;

public interface Constants {

    String BUNDLE_KEY_FILTERS = "filters";

    interface Video {
        int WIDTH = 1080;
        int HEIGHT = 1920;
        int FRAME_RATE = 30;
        int SAMPLE_RATE = 44100;
        int CHANNELS = 2;
        int AUDIO_BIT_RATE = 96000;
    }

    interface Filter {
        int[] FILTER_RES_IDS = {
                R.drawable.filter_normal,
                R.drawable.filter_baby_1,
                R.drawable.filter_baby_2,
                R.drawable.filter_baby_3,
                R.drawable.filter_baby_4,
                R.drawable.filter_baby_5,
                R.drawable.filter_gray,
                R.drawable.filter_food_1,
                R.drawable.filter_food_2,
                R.drawable.filter_food_3,
                R.drawable.filter_landscape_1,
                R.drawable.filter_landscape_2,
                R.drawable.filter_landscape_3,
                R.drawable.filter_plant_1,
                R.drawable.filter_plant_2,
                R.drawable.filter_recomend_1,
                R.drawable.filter_recomend_2,
                R.drawable.filter_street_1,
                R.drawable.filter_street_2,
                R.drawable.filter_street_3
        };

        String[] FILTER_ASSETS_PATH = {
                "lut/res_original.png",
                "lut/res_baby_1.png",
                "lut/res_baby_2.png",
                "lut/res_baby_3.png",
                "lut/res_baby_4.png",
                "lut/res_baby_5.png",
                "filter_gray",
                "lut/res_food_1.png",
                "lut/res_food_2.png",
                "lut/res_food_3.png",
                "lut/res_landscape_1.png",
                "lut/res_landscape_2.png",
                "lut/res_landscape_3.png",
                "lut/res_plant_1.png",
                "lut/res_plant_2.png",
                "lut/res_recomend_1.png",
                "lut/res_recomend_2.png",
                "lut/res_street_1.png",
                "lut/res_street_2.png",
                "lut/res_street_3.png"
        };
    }
}
