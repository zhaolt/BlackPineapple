package com.jease.pineapple.record;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.WindowManager;

import com.jease.pineapple.MainActivity;
import com.jease.pineapple.R;
import com.jease.pineapple.base.FullScreenActivity;
import com.jease.pineapple.record.filter.Filter;

public class CameraActivity extends FullScreenActivity {


    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, CameraActivity.class);
        return intent;
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
        if (fragment == null)
            fragment = CameraFragment.newInstance();
        addFragment(fm, fragment, R.id.frame_root);
    }

    @Override
    public void onBackPressed() {
        CameraFragment fragment = (CameraFragment) getSupportFragmentManager()
                .findFragmentById(R.id.frame_root);
        if (fragment != null && fragment.dismissPopMenu()) return;
        // 回主页 清栈
        Intent intent = MainActivity.getCallingIntent(getApplicationContext());
        startActivity(intent);
        finish();
        overridePendingTransition(0, R.anim.slide_out_down);
    }

    public void updateCameraFilter(Filter filter) {
        CameraFragment fragment = (CameraFragment) getSupportFragmentManager()
                .findFragmentById(R.id.frame_root);
        if (fragment != null)
            fragment.updateFilter(filter);
    }
}
