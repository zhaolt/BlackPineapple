package com.jease.pineapple.record;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.WindowManager;

import com.jease.pineapple.R;
import com.jease.pineapple.base.FullScreenActivity;

public class Camera1Activity extends FullScreenActivity {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, Camera1Activity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_root);
        loadCameraFragment();
    }

    private void loadCameraFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.frame_root);
        if (null == fragment) {
            fragment = Camera1Fragment.newInstance();
        }
        addFragment(fm, fragment, R.id.frame_root);
    }
}
