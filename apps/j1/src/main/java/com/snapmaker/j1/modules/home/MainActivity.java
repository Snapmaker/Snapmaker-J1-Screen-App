package com.snapmaker.j1.modules.home;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;
import com.snapmaker.j1.R;

import fabscreen.platform.base.BaseMainActivity;
import fabscreen.platform.base.BaseMainViewModel;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.DecisionDialog;

public class MainActivity extends BaseMainActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addFragment(J1MainFragment.newInstance());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsFirstTimeIn) {
            dispatchRoutes();
        }
    }

    @Override
    protected void modifyOrientation() {
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
    }

    @Override
    protected BaseMainViewModel getViewModelByChild() {
        return getViewModel(MainViewModel.class);
    }

    @Override
    protected void onInitFinished() {
        dispatchRoutes();
    }

    private void dispatchRoutes() {
        Logger.d("dispatching route...");
        // welcome, guide, or home.
        boolean machineSetupFlag = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineSetupFlag();
        boolean fdmSetup = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineSetup3DP();

        if (!machineSetupFlag) {
            mRouter.routeToWelcome(false).start(this);
            return;
        }

        if (!fdmSetup) {
            mRouter.routeToGuide3DP(false).start(this);
            return;
        }

        mRouter.routeToHome().start(this);

        // MainActivity is now useless.
        // finish();
    }

    @Override
    protected void onInitTimeout() {
        DecisionDialog.create(this)
                .setContent(R.string.all_j1_can_not_connect)
                .setType(DecisionDialog.WARMING_TYPE)
                .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
                .setFirstTv(R.string.all_ok, fabscreen.platform.base.R.color.select_dialog_orange_txt, ((dialog, which) -> {
                    dialog.dismiss();
                    mRouter.routeToHome().start(this);
                })).show();
    }

    @Override
    protected void onUpdateFinished() {
        // show update finish
        addFragment(R.id.fcv_main, J1UpdateSuccessFragment.newInstance());
    }
}

