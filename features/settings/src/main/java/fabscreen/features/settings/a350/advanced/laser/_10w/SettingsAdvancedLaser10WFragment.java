package fabscreen.features.settings.a350.advanced.laser._10w;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.features.settings.a350.index.SettingsActivity;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.view.FabAlert;

@SuppressLint("NonConstantResourceId")
public class SettingsAdvancedLaser10WFragment extends BaseFragment {

    @BindView(R2.id.ll_settings_laser_camera_calibration)
    View mCameraCalibration;
    @BindView(R2.id.ll_settings_laser_thickness_measure_calibration)
    View mThicknessMeasureCalibration;
    @BindView(R2.id.ll_settings_laser_toolhead_focus_calibration)
    View mToolHeadFocusCalibration;
    @BindView(R2.id.btn_settings_advanced_light)
    Button mBtnLight;

    private SettingsActivity mActivity;

    public static Fragment getInstance() {
        return new SettingsAdvancedLaser10WFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(R.string.settings_advance_laser_10w);
        mActivity = (SettingsActivity) requireActivity();

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_settings_advanced_laser_10w;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        boolean isRotaryAvailable = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isRotaryAvailable;
        mCameraCalibration.setVisibility(isRotaryAvailable ? View.GONE : View.VISIBLE);
        mThicknessMeasureCalibration.setVisibility(isRotaryAvailable ? View.GONE : View.VISIBLE);
        mToolHeadFocusCalibration.setVisibility(isRotaryAvailable ? View.GONE : View.VISIBLE);
        boolean cameraLightOn = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserCameraLightOn();
        mBtnLight.setActivated(cameraLightOn);
    }


    @OnClick(R2.id.btn_settings_advanced_light)
    void onCameraLightClicked(View view) {
        playNormalClickSound();
        boolean cameraLightOn = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserCameraLightOn();
        cameraLightOn = !cameraLightOn;

        Logger.i("Setting camera light mode " + cameraLightOn);

        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLaserCameraLightOn(cameraLightOn);
        mBtnLight.setActivated(cameraLightOn);
    }

    @OnClick(R2.id.ll_settings_laser_camera_calibration)
    void onCameraCalibrationClicked() {
        playNormalClickSound();
        calibrateCamera();
    }

    @OnClick(R2.id.ll_settings_laser_thickness_measure_calibration)
    void onThicknessMeasureCalibration() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class).routeTo10wThicknessCalibrationPage().start(requireContext());
    }

    @OnClick(R2.id.ll_settings_laser_toolhead_focus_calibration)
    void onToolheadFocusCalibrationClicked() {
        playNormalClickSound();
        mActivity.gotoLaser10wToolheadFocusCalibration();
    }

    private void calibrateCamera() {
        // open camera calibration
        if (ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().isConnected()) {
            SettingsActivity activity = (SettingsActivity) getContext();
            if (activity != null) {
                activity.goto10wCameraCalibrationIntroFragment();
            }
        } else {
            FabAlert.alert(getContext(), R.string.laser_camera_alert_camera_not_connected);
        }
    }
}
