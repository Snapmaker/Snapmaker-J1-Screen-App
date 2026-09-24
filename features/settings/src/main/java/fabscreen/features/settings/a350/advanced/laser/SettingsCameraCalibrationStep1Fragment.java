package fabscreen.features.settings.a350.advanced.laser;

import android.widget.Button;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.features.settings.a350.index.SettingsActivity;
import fabscreen.platform.base.Constants;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class SettingsCameraCalibrationStep1Fragment extends BaseFragment {
    @BindView(R2.id.btn_camera_calibration_next)
    Button mBtnNext;

    public static SettingsCameraCalibrationStep1Fragment newInstance() {
        return new SettingsCameraCalibrationStep1Fragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_settings_camera_calibration_step1;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void ensureHomed() {
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(coordinateSystem -> {
                    boolean homed = coordinateSystem.isHomed;
                    if (homed) {
                        next();
                    } else {
                        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().home(0)
                                .observeOn(AndroidSchedulers.mainThread())
                                .as(bindToLifecycle())
                                .subscribe(success -> checkHome());
                    }
                }, LogHelper::log);
    }

    private void checkHome() {
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(coordinateSystem -> {
                    boolean homed = coordinateSystem.isHomed;
                    if (homed) {
                        next();
                    } else {
                        AndroidSchedulers.mainThread().scheduleDirect(this::checkHome, 2000, Constants.TIME_UNIT);
                    }
                }, LogHelper::log);
    }

    private void next() {
        mBtnNext.setEnabled(true);
        if (getActivity() == null) return;
        ((SettingsActivity) getActivity()).gotoCameraCalibrationStep2();
    }

    @OnClick(R2.id.btn_camera_calibration_next)
    void onClickStart() {
        playNormalClickSound();
        mBtnNext.setEnabled(false);
        ensureHomed();
    }
}
