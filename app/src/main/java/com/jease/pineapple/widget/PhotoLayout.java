package com.jease.pineapple.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.jease.pineapple.R;

public class PhotoLayout extends FrameLayout {

    private FrameLayout mContainer;

    private ImageView mPhoto;


    public PhotoLayout(@NonNull Context context) {
        this(context, null);
    }

    public PhotoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContainer = (FrameLayout) inflate(context, R.layout.layout_album_item, this);
        mPhoto = mContainer.findViewById(R.id.iv_item);
    }

    public ImageView getPhoto() {
        return mPhoto;
    }
}
