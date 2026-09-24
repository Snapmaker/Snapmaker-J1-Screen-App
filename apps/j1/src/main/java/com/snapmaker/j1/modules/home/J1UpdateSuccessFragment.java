package com.snapmaker.j1.modules.home;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;
import com.snapmaker.j1.R;
import com.snapmaker.j1.R2;

import butterknife.OnClick;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;

public class J1UpdateSuccessFragment extends BaseFragment {

    private MainViewModel mViewModel;

    public static Fragment newInstance() {
        return new J1UpdateSuccessFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_update_success;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = getActivityScopeViewModel(MainViewModel.class);
        if (ServiceContainer.getInstance() != null &&
                ServiceContainer.getInstance().getService(IPreferences.class).getHelper() != null) {
            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMockEnabled(false);
            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setDebugFlag(false);
            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setUSBFactoryModeOn(false);
            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMachineDeveloper(false);
        } else {
            Logger.e("Preferences was not ready yet!");
        }
    }

    @OnClick(R2.id.btn_complete)
    void onUpdateConfirmed() {
        mViewModel.confirmUpdate();
        back();
    }
}
