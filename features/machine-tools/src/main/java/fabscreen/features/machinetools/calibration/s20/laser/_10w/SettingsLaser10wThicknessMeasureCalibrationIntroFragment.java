package fabscreen.features.machinetools.calibration.s20.laser._10w;

import static fabscreen.platform.core.ui.common.Laser10wThicknessCalibrationViewModel.FIRST_CAPTURE;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.common.Laser10wThicknessCalibrationViewModel;
import fabscreen.platform.core.ui.presenter.CoordinateSystemPresenter;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

@SuppressLint("NonConstantResourceId")
public class SettingsLaser10wThicknessMeasureCalibrationIntroFragment extends BaseFragment {

    @BindView(R2.id.top_bar_back)
    Button mBtnBack;
    @BindView(R2.id.btn_laser_10w_thickness_measure_calibration_start)
    Button mBtnStart;
    private Laser10wThicknessCalibrationViewModel mViewModel;

    public static Fragment getInstance() {
        return new SettingsLaser10wThicknessMeasureCalibrationIntroFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(R.string.settings_laser_thickness_measure_calibration_title);
        mViewModel = getViewModel();
        // We can only go ahead after finishing switching coordinate.
        CoordinateSystemPresenter coordinateSystemPresenter = new CoordinateSystemPresenter(disposables);
        coordinateSystemPresenter.setOnCoordinateSwitchListener(this::prepareHead);
        coordinateSystemPresenter.ensureCoordinate(1);
        setButtonsEnabled(false);
    }

    /**
     * Set proper exposeTime(now we set 1) and init camera position for distance measuring.
     * Laser point won't be on the material surface if we don't init the camera position.
     */
    private void prepareHead() {
        mViewModel.initCameraPosition(FIRST_CAPTURE)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    if (success) {
                        setButtonsEnabled(true);
                    } else {
                        // If fail, we can't start but should have ability to back.
                        mBtnBack.setEnabled(true);
                    }
                }, LogHelper::log);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_laser_10w_thickness_measure_calibration;
    }

    @Override
    protected Laser10wThicknessCalibrationViewModel getViewModel() {
        return getViewModelProvider().get(Laser10wThicknessCalibrationViewModel.class);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R2.id.btn_laser_10w_thickness_measure_calibration_start)
    void onStartClicked() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getHeaderSecurityStatus().observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(headerSecurity -> {
                    if (headerSecurity.status == 0) {
                        mViewModel.switchAFAssistLight(true);
                        Laser10wThicknessMeasureCalibrationActivity activity = (Laser10wThicknessMeasureCalibrationActivity) requireActivity();
                        activity.gotoLaser10wThicknessMeasureCalibrationPointsFragment();
                    }
                });

    }

    private void setButtonsEnabled(boolean enabled) {
        mBtnBack.setEnabled(enabled);
        mBtnStart.setEnabled(enabled);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            prepareHead();
        }
    }
}
