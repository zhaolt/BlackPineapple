package com.jease.pineapple.base;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateFontScale();
    }

    private void updateFontScale() {
        Resources res = getResources();
        Configuration configuration = new Configuration();
        if (res.getConfiguration().fontScale != configuration.fontScale) { //非默认值
            configuration.fontScale = res.getDisplayMetrics() != null
                    && res.getDisplayMetrics().densityDpi > DisplayMetrics.DENSITY_XXHIGH
                    ? 1.1f : 1.0f;
            res.updateConfiguration(configuration, res.getDisplayMetrics());
        }
    }

    public void addFragment(@NonNull FragmentManager fragmentManager,
                            @NonNull Fragment fragment,
                            int frameId) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(frameId, fragment);
        transaction.commitAllowingStateLoss();
    }

    public void removeFragment(@NonNull FragmentManager fragmentManager,
                               @NonNull Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(fragment);
        transaction.commitAllowingStateLoss();
    }

    public void addFragment(@NonNull FragmentManager fragmentManager,
                            @NonNull Fragment fragment,
                            int frameId,
                            String tag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(frameId, fragment, tag);
        transaction.commitAllowingStateLoss();
    }
}
