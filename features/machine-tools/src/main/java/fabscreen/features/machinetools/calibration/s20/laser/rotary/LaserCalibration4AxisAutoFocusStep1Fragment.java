package fabscreen.features.machinetools.calibration.s20.laser.rotary;

import android.widget.Button;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.s20.laser.CalibrationLaserActivity;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class LaserCalibration4AxisAutoFocusStep1Fragment extends BaseFragment {
    @BindView(R2.id.btn_guide_next)
    Button mBtnNext;

    public static LaserCalibration4AxisAutoFocusStep1Fragment newInstance() {
        return new LaserCalibration4AxisAutoFocusStep1Fragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_guide_laser_auto_focus_step1;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private Observable<Boolean> gotoInitialPosition() {
        // Assume we are at CS#1
        return ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 X0 Y0 F3000")
                .flatMap(response -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 B0 F3000"))
                .flatMap(response -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 Z0 F1800"))
                .map(coordinateSystem -> true);
    }

    @OnClick(R2.id.btn_guide_next)
    void onClickNext() {
        playNormalClickSound();
        mBtnNext.setEnabled(false);

        gotoInitialPosition()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    mBtnNext.setEnabled(true);

                    if (getActivity() != null) {
                        ((CalibrationLaserActivity) getActivity()).start4AxisAutoFocusStep2Fragment();
                    }
                }, e -> {
                    mBtnNext.setEnabled(true);
                    LogHelper.log(e);
                });
    }
}
