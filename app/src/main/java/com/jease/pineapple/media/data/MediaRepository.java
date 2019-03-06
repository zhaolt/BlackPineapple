package com.jease.pineapple.media.data;

import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.Observable;

public class MediaRepository implements MediaDataSource {

    private final MediaDataSource mSystemDataSource;

    private static MediaRepository sInstance;

    public static MediaRepository getInstance(MediaDataSource systemDataSource) {
        if (null == sInstance) {
            synchronized (MediaRepository.class) {
                if (null == sInstance)
                    sInstance = new MediaRepository(systemDataSource);
            }
        }
        return sInstance;
    }

    private MediaRepository(MediaDataSource systemDataSource) {
        mSystemDataSource = systemDataSource;
    }

    @Override
    public Observable<List<Media>> getMedias() {
        return mSystemDataSource.getMedias();
    }

    @Override
    public void save(@NonNull List<Media> medias) {

    }
}
