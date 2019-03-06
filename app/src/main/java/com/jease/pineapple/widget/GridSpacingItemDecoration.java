package com.jease.pineapple.widget;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private int gridSpace;
    private int spanCount;

    public GridSpacingItemDecoration(int gridSpace, int spanCount) {
        this.gridSpace = gridSpace;
        this.spanCount = spanCount;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        int itemSpanIndex = layoutParams.getSpanIndex();
        int itemSpanSize = layoutParams.getSpanSize();
        if (itemSpanSize == 1) {
            outRect.left = gridSpace - itemSpanIndex * gridSpace / spanCount;
            outRect.right = (itemSpanIndex + 1) * gridSpace / spanCount;
            outRect.bottom = gridSpace; // item bottom
        }
    }
}
