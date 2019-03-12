package com.jease.pineapple.record.camera;

import io.reactivex.Observable;

public interface TakePictureCallback {
    void onTakePicture(Observable<byte[]> observable);
    void onError(int errorCode);
}
