package fabscreen.features.guide.s20.laser.camearcalibration;

import android.widget.Button;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.features.guide.s20.laser.GuideLaserActivity;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class GuideLaserCameraCalibrationStep1Fragment extends BaseFragment {
    @BindView(R2.id.btn_camera_calibration_next)
    Button mBtnNext;

    public static GuideLaserCameraCalibrationStep1Fragment newInstance() {
        return new GuideLaserCameraCalibrationStep1Fragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_settings_camera_calibration_step1;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_camera_calibration_next)
    void onClickNext() {
        playNormalClickSound();
        mBtnNext.setEnabled(false);
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(coordinateSystem -> {
                    mBtnNext.setEnabled(true);
                    if (getActivity() != null) {
                        ((GuideLaserActivity) getActivity()).startCameraCalibrationStep2Fragment();
                    }
                }, LogHelper::log);
    }

    @Override
    protected void back() {
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(coordinateSystem -> super.back());
    }
}
