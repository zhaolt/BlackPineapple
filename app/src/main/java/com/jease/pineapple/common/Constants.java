package com.jease.pineapple.common;

import com.jease.pineapple.R;

public interface Constants {

    String BUNDLE_KEY_FILTERS = "filters";
    String BUNDLE_KEY_FILTER = "filter";

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
                R.drawable.filter_0,
                R.drawable.filter_i_1,
                R.drawable.filter_i_2,
                R.drawable.filter_i_3,
                R.drawable.filter_i_4,
                R.drawable.filter_i_5,
                R.drawable.filter_gray,
                R.drawable.filter_j_1,
                R.drawable.filter_j_2,
                R.drawable.filter_j_3,
                R.drawable.filter_k_1,
                R.drawable.filter_k_2,
                R.drawable.filter_k_3,
                R.drawable.filter_l_1,
                R.drawable.filter_l_2,
                R.drawable.filter_m_1,
                R.drawable.filter_m_2,
                R.drawable.filter_n_1,
                R.drawable.filter_n_2,
                R.drawable.filter_n_3
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
