package fabscreen.features.machinetools.calibration;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.OnClick;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.a400platform.laser.w_1_6.A400LaserCalibrationViewModel;
import fabscreen.platform.core.ui.base.BaseProgressFragment;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public abstract class BaseCalibrationProgressFragment extends BaseProgressFragment {
    protected A400LaserCalibrationViewModel mViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
    }

    @Override
    protected abstract int getLayoutResID();

    @OnClick(R2.id.iv_close)
    public void onCloseClicked() {
        playNormalClickSound();
        new AlertDialog.Builder(requireContext())
                .setMessage("校准未完成，确认退出？")
                .setPositiveButton("退出", (dialog, which) -> quitCalibration())
                .setNegativeButton("不退出", (dialog, which) -> {
                })
                .create()
                .show();
    }

    private void quitCalibration() {
        mViewModel.quitCalibration(false)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> back(), LogHelper::log);
    }

    @Override
    protected A400LaserCalibrationViewModel getViewModel() {
        return getViewModelProvider().get(A400LaserCalibrationViewModel.class);
    }
}
