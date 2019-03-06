package com.jease.pineapple.media.data;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.jease.pineapple.base.App;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Function;

public class MediaSystemDataSource implements MediaDataSource {

    private static MediaSystemDataSource sInstance;

    public static MediaSystemDataSource getInstance() {
        if (null == sInstance) {
            synchronized (MediaSystemDataSource.class) {
                if (null == sInstance)
                    sInstance = new MediaSystemDataSource();
            }
        }
        return sInstance;
    }

    private MediaSystemDataSource() {
    }


    @Override
    public Observable<List<Media>> getMedias() {
        return rawQuery();
    }

    @Override
    public void save(@NonNull List<Media> medias) {

    }

    private Observable<List<Media>> rawQuery() {
        final String[] imageProjection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.MIME_TYPE
        };
        return Observable.create(new ObservableOnSubscribe<Cursor>() {
            @Override
            public void subscribe(ObservableEmitter<Cursor> emitter) {
                Context context = App.sInstance;
                Cursor cursor = null;
                try {
                    cursor = MediaStore.Images.Media.query(context.getContentResolver(),
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageProjection);
                    if (!emitter.isDisposed())
                        emitter.onNext(cursor);
                    emitter.onComplete();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (null != cursor) {
                        cursor.close();
                    }
                }
            }
        }).map(new Function<Cursor, List<Media>>() {
            @Override
            public List<Media> apply(Cursor cursor) {
                List<Media> mediaList = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String id = cursor.getString(cursor.getColumnIndex(
                            MediaStore.Images.Media._ID));
                    String path = cursor.getString(cursor.getColumnIndex(
                            MediaStore.Images.Media.DATA));
                    int dateAdded = cursor.getInt(cursor.getColumnIndex(
                            MediaStore.Images.Media.DATE_ADDED));
                    if (ignoreSpecificTypeFile(path))
                        continue;
                    Image media = new Image();
                    media.setId(id);
                    media.setDate(dateAdded);
                    media.setFilePath(path);
                    mediaList.add(media);
                }
                return mediaList;
            }
        });
    }

    private boolean ignoreSpecificTypeFile(String path) {
        if (path != null) {
            if (path.endsWith(".jpeg") ||
                    path.endsWith(".JPEG") ||
                    path.endsWith(".jpg") ||
                    path.endsWith(".JPG"))
                return false;
        }
        return true;
    }
}
