package com.jease.pineapple.record.filter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.jease.pineapple.R;
import com.jease.pineapple.base.GlideApp;
import com.jease.pineapple.utils.DensityUtils;
import com.jease.pineapple.widget.GlideCircleBorderTransform;

import java.util.ArrayList;
import java.util.List;

public class FilterListAdapter extends RecyclerView.Adapter<FilterListAdapter.ViewHolder> {

    private static final String TAG = FilterListAdapter.class.getSimpleName();

    private List<Filter> mFilters;

    private OnItemClickListener mOnItemClickListener;

    private RecyclerView mRecyclerView;

    public FilterListAdapter(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mFilters = new ArrayList<>();
    }

    public void addAll(List<Filter> filters) {
        if (filters == null || filters.isEmpty())
            return;
        mFilters.clear();
        mFilters.addAll(filters);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.layout_filter_list_item, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        RequestOptions options = null;
        if (mFilters.get(position).isSelected()) {
            float borderWidth = DensityUtils.dp2px(2f);
            options = new RequestOptions()
                    .centerCrop()
                    .bitmapTransform(new GlideCircleBorderTransform(borderWidth,
                            Color.parseColor("#FD415F")))
                    .diskCacheStrategy(DiskCacheStrategy.DATA);
        } else {
            options = new RequestOptions()
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.DATA);
        }
        GlideApp.with(viewHolder.itemView)
                .load(mFilters.get(position).getCoverResId())
                .apply(options)
                .into(viewHolder.mFilterImage);
        viewHolder.mFilterImage.setSelected(mFilters.get(position).isSelected());
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0, len = mFilters.size(); i < len; i++) {
                    mFilters.get(i).setSelected(false);
                }
                Filter f = mFilters.get(position);
                f.setSelected(true);
                mRecyclerView.smoothScrollToPosition(position);
                notifyDataSetChanged();
                if (null != mOnItemClickListener)
                    mOnItemClickListener.onItemClicked(f);
            }
        });
    }

    public void setCurrentFilter(Filter filter) {
        int index = -1;
        if (null == filter) {
            index = 0;
        } else {
            index = mFilters.indexOf(filter);
        }
        for (Filter f : mFilters) {
            f.setSelected(false);
        }
        mFilters.get(index).setSelected(true);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mFilters.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView mFilterImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mFilterImage = itemView.findViewById(R.id.iv_filter);
        }
    }

    public interface OnItemClickListener {
        void onItemClicked(Filter f);
    }
}
