package com.snapmaker.j1.modules.home;

import androidx.fragment.app.Fragment;

import com.snapmaker.j1.R;

import fabscreen.platform.base.view.BaseFragment;

public class J1MainFragment extends BaseFragment {
    public static Fragment newInstance() {
        return new J1MainFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_main;
    }
}
