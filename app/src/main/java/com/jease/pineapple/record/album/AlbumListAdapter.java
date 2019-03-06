package com.jease.pineapple.record.album;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.jease.pineapple.R;
import com.jease.pineapple.base.GlideApp;
import com.jease.pineapple.media.data.Media;
import com.jease.pineapple.utils.DensityUtils;
import com.jease.pineapple.utils.DeviceInfoUtils;
import com.jease.pineapple.widget.PhotoLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.ViewHolder> {

    private List<Media> mMedias = new ArrayList<>();

    private int mCellSize;

    public AlbumListAdapter() {
        mCellSize = (int) ((DeviceInfoUtils.getScreenWidth() - DensityUtils.dp2px(14f)) / 3);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        PhotoLayout item = new PhotoLayout(viewGroup.getContext());
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(-1, mCellSize);
        item.setLayoutParams(params);
        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Media media = mMedias.get(position);
        PhotoLayout item = viewHolder.content;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mCellSize, mCellSize);
        item.getPhoto().setLayoutParams(params);
        File file = new File(media.getFilePath());
        GlideApp.with(item.getPhoto())
                .load(file)
                .override(mCellSize, mCellSize)
                .placeholder(R.drawable.album_plachholder_shape)
                .centerCrop()
                .into(item.getPhoto());
    }

    public void addAll(List<Media> medias) {
        mMedias.clear();
        mMedias.addAll(medias);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mMedias.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private PhotoLayout content;

        public ViewHolder(@NonNull PhotoLayout itemView) {
            super(itemView);
            content = itemView;
        }
    }
}
