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

public class CameraActivity extends FullScreenActivity {

    private static final String CAMERA_JAVA = "java";

    private static final String CAMERA_CPP = "c++";

    private boolean isJava = false;

    public static Intent getCallingIntent(Context context, boolean isJava) {
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra("isJava", isJava);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_root);
        isJava = getIntent().getBooleanExtra("isJava", true);
        loadCameraFragment();
    }

    private void loadCameraFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment;
        if (isJava)
            fragment = Camera1Fragment.newInstance();
        else
            fragment = Camera2Fragment.newInstance();
        addFragment(fm, fragment, R.id.frame_root);
    }
}
