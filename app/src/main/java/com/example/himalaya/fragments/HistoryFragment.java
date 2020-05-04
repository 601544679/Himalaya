package com.example.himalaya.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentActivity;

import com.example.himalaya.R;
import com.example.himalaya.base.BaseFragment;

public class HistoryFragment extends BaseFragment {
    @Override
    protected View onSubViewLoaded(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        return view;
    }
}
