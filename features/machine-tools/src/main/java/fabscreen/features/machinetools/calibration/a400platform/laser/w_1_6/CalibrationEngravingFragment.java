package fabscreen.features.machinetools.calibration.a400platform.laser.w_1_6;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.calibration.BaseCalibrationProgressFragment;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class CalibrationEngravingFragment extends BaseCalibrationProgressFragment {

    public static Fragment newInstance() {
        return new CalibrationEngravingFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        setMainTitle("3-1 Manual Focus Calibration");
        setSubTitle("Engrave Calibration Pattern (3/4)");
        setProgress(3, 4);
        getViewModel().doEngraving()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> ((A400LaserManualFocusCalibrationActivity)requireActivity()).goToChooseLine(), LogHelper::log);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_manual_focus_calibration_engraving;
    }

    @Override
    protected A400LaserCalibrationViewModel getViewModel() {
        return getViewModelProvider().get(A400LaserCalibrationViewModel.class);
    }
}
