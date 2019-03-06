package com.jease.pineapple;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.jease.pineapple.record.album.AlbumActivity;
import com.jease.pineapple.utils.PermissionUtils;

public class MainFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = MainFragment.class.getSimpleName();

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    private Runnable mCamera1InitViewTask = new Runnable() {
        @Override
        public void run() {
            Intent intent = AlbumActivity.getCallingIntent(getActivity());
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.slide_in_up, 0);
        }
    };

    private ImageView mCameraBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mCameraBtn = view.findViewById(R.id.iv_camera_btn);
        mCameraBtn.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_camera_btn:
                PermissionUtils.askPermission(this,
                        new String[]{
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        },
                        10,
                        mCamera1InitViewTask);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(requestCode == 10, grantResults,
                mCamera1InitViewTask, new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), R.string.request_permission_failed,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
