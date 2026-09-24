package fabscreen.features.machinetools.calibration.a400platform.laser.w_1_6;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.BaseCalibrationProgressFragment;
import fabscreen.platform.core.ui.view.RulerView;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class CalibrationChooseLineFragment extends BaseCalibrationProgressFragment {
    public static Fragment newInstance() {
        return new CalibrationChooseLineFragment();
    }


    @BindView(R2.id.rv_line_chooser)
    RulerView mRvLineChooser;

    private A400LaserCalibrationViewModel mViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
    }

    private void initView() {
        setMainTitle("4-1 Manual Focus Calibration");
        setSubTitle("Calibrate  (4/4)");
        setProgress(4, 4);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_manual_focus_calibration_choose_line;
    }

    @Override
    protected A400LaserCalibrationViewModel getViewModel() {
        return getViewModelProvider().get(A400LaserCalibrationViewModel.class);
    }

    @OnClick(R2.id.btn_save)
    void onSaveClicked() {
        playNormalClickSound();
        mViewModel.saveFocalLenAndQuit(mRvLineChooser.getCurrentValue())
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> ((A400LaserManualFocusCalibrationActivity) requireActivity()).goToCalibrationComplete(), LogHelper::log);
    }

    @OnClick(R2.id.btn_quit)
    void onQuitClicked() {
        playNormalClickSound();
        onCloseClicked();
//        mViewModel.quitCalibration(false)
//                .observeOn(AndroidSchedulers.mainThread())
//                .as(bindToLifecycle())
//                .subscribe(success -> back());
    }
}
