package com.jease.pineapple.record.album;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jease.pineapple.R;
import com.jease.pineapple.media.data.Media;
import com.jease.pineapple.media.data.MediaRepository;
import com.jease.pineapple.media.data.MediaSystemDataSource;
import com.jease.pineapple.utils.DensityUtils;
import com.jease.pineapple.widget.GridSpacingItemDecoration;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AlbumFragment extends Fragment {

    private RecyclerView mAlbumList;

    private AlbumListAdapter mAdapter;

    private MediaRepository mRepository;

    private GridLayoutManager mLayoutManager;

    public static AlbumFragment newInstance() {
        return new AlbumFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new AlbumListAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_album, container, false);
        mAlbumList = root.findViewById(R.id.rv_album_list);
        mRepository = MediaRepository.getInstance(MediaSystemDataSource.getInstance());
        setupRecyclerView();
        importPhotos();
        return root;
    }

    private void setupRecyclerView() {
        int offsetPadding = (int) DensityUtils.dp2px(0.5f);
        mAlbumList.setPadding(offsetPadding, offsetPadding, offsetPadding, offsetPadding);
        int gridPadding = (int) DensityUtils.dp2px(3.5f);
        mAlbumList.addItemDecoration(new GridSpacingItemDecoration(gridPadding, 3));
        mLayoutManager = new GridLayoutManager(getActivity(), 3);
        mAlbumList.setHasFixedSize(true);
        mAlbumList.setAdapter(mAdapter);
    }

    private void showPhotos(List<Media> medias) {
        mAdapter.addAll(medias);
        mAlbumList.setLayoutManager(mLayoutManager);
    }

    private void importPhotos() {
        mRepository.getMedias()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Media>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(List<Media> media) {
                        if (null == media)
                            return;
                        showPhotos(media);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }
}
