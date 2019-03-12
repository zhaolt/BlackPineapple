package com.jease.pineapple.record.filter;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.jease.pineapple.R;
import com.jease.pineapple.common.Constants;

import java.util.ArrayList;
import java.util.List;

public class FilterMenuFragment extends Fragment {

    private RecyclerView mFilterList;

    private List<Filter> mFilters;

    private FilterListAdapter mFilterListAdapter;

    public static FilterMenuFragment newInstance(List<Filter> filters) {
        FilterMenuFragment fragment = new FilterMenuFragment();
        Bundle data = new Bundle();
        data.putParcelableArrayList(Constants.BUNDLE_KEY_FILTERS,
                (ArrayList<? extends Parcelable>) filters);
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        return enter ? AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_up)
                : AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_down);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_menu, container, false);
        mFilterList = view.findViewById(R.id.rv_filter_list);
        Bundle data = getArguments();
        if (data != null)
            mFilters = data.getParcelableArrayList(Constants.BUNDLE_KEY_FILTERS);
        setupFilterList();
        return view;
    }

    private void setupFilterList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false);
        mFilterList.setLayoutManager(layoutManager);
        mFilterListAdapter = new FilterListAdapter();
        mFilterList.setAdapter(mFilterListAdapter);
        mFilterListAdapter.addAll(mFilters);
    }

}
