package com.example.himalaya.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.himalaya.R;
import com.example.himalaya.utils.LogUtil;


public abstract class BaseFragment extends Fragment {

    private static final String TAG = "BaseFragment";
    private View mView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogUtil.d(TAG, "onCreateView");
        mView = onSubViewLoaded(inflater, container);
        return mView;
    }

    protected abstract View onSubViewLoaded(LayoutInflater inflater, ViewGroup container);


}
