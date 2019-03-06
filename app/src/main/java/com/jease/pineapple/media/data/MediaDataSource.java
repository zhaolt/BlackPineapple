package com.jease.pineapple.media.data;

import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.Observable;

public interface MediaDataSource {
    Observable<List<Media>> getMedias();
    void save(@NonNull List<Media> medias);
}
