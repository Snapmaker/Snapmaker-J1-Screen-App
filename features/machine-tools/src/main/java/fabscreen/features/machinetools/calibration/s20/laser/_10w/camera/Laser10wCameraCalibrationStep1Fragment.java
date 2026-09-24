package fabscreen.features.machinetools.calibration.s20.laser._10w.camera;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.Constants;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class Laser10wCameraCalibrationStep1Fragment extends BaseFragment {
    @BindView(R2.id.btn_camera_calibration_next)
    Button mBtnNext;
    @BindView(R2.id.tv_settings_camera_calibration_content)
    TextView mTvDesc;
    @BindView(R2.id.iv_settings_camera_calibration)
    ImageView mIvCover;
    private MachineController mMachineController;

    public static Laser10wCameraCalibrationStep1Fragment newInstance() {
        return new Laser10wCameraCalibrationStep1Fragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        mMachineController = ServiceContainer.getInstance().getService(IMachine.class).getMachineController();
    }

    private void initView() {
        mIvCover.setImageResource(R.drawable.pic_10w_laser_camera_calibration_with_goggles_240x160);
        mTvDesc.setText(R.string.laser_10w_camera_calibnration_step1_desc);
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
        mMachineController.updateCoordinateSystem(0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(coordinateSystem -> {
                    if (coordinateSystem.isHomed) {
                        next();
                    } else {
                        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().home(1)
                                .observeOn(AndroidSchedulers.mainThread())
                                .as(bindToLifecycle())
                                .subscribe(success -> checkHome());
                    }
                }, LogHelper::log);
    }

    private void checkHome() {
        mMachineController.updateCoordinateSystem()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(coordinateSystem -> {
                    boolean homed = true;
                    if (homed) {
                        next();
                    } else {
                        AndroidSchedulers.mainThread().scheduleDirect(this::checkHome, 2000, Constants.TIME_UNIT);
                    }
                }, LogHelper::log);
    }

    private void next() {
        if (getActivity() == null) return;
        ((Laser10wCameraCalibrationActivity) getActivity()).gotoCameraCalibrationStep2();
    }

    @OnClick(R2.id.btn_camera_calibration_next)
    void onClickStart() {
        playNormalClickSound();
//        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getHeaderSecurityStatus()
//                .observeOn(AndroidSchedulers.mainThread())
//                .as(bindToLifecycle())
//                .subscribe(headerSecurity -> {
//                    // When status is 0, the execution header is normal
//                    if (headerSecurity.status == 0) {
//                        mBtnNext.setEnabled(false);
//                        ensureHomed();
//                    }
//                }, LogHelper::log);
        next();
    }
}
