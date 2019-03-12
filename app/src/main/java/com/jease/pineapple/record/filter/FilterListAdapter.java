package com.jease.pineapple.record.filter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jease.pineapple.R;
import com.jease.pineapple.base.GlideApp;

import java.util.ArrayList;
import java.util.List;

public class FilterListAdapter extends RecyclerView.Adapter<FilterListAdapter.ViewHolder> {

    private List<Filter> mFilters;

    public FilterListAdapter() {
        mFilters = new ArrayList<>();
    }

    public void addAll(List<Filter> filters) {
        if (filters == null || filters.isEmpty())
            return;
        mFilters.clear();
        mFilters.addAll(filters);
        notifyDataSetChanged();
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
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        GlideApp.with(viewHolder.itemView)
                .load(mFilters.get(position).getCoverResId())
                .circleCrop()
                .into(viewHolder.mFilterImage);
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
}
