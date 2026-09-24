package fabscreen.features.machinetools.calibration.s20.laser.autofocus;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.s20.laser.CalibrationLaserActivity;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class LaserCalibrationAutoFocusStep1Fragment extends BaseFragment {
    MachineController machineController;
    @BindView(R2.id.btn_guide_next)
    Button mBtnNext;

    public static LaserCalibrationAutoFocusStep1Fragment newInstance() {
        return new LaserCalibrationAutoFocusStep1Fragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_guide_laser_auto_focus_step1;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBtnNext.setText(R.string.all_start);
        machineController = ServiceContainer.getInstance().getService(IMachine.class).getMachineController();
    }

    private Observable<Boolean> gotoInitialPosition() {
        // Assume we are at CS#1
        Vector vector = new Vector();
        vector.setX(0);
        vector.setY(0);
        return machineController.gotoAbsolutePosition(vector, 3000)//sendGcode("G0 X0 Y0 F3000")
                .flatMap(response -> {
                    Vector vector1 = new Vector();
                    vector.setZ(0);
                    return machineController.gotoAbsolutePosition(vector1, 1800);
                })//.sendGcode("G0 Z0 F1800"))
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
                        ((CalibrationLaserActivity) getActivity()).startAutoFocusStep2Fragment();
                    }
                }, e -> {
                    mBtnNext.setEnabled(true);
                    LogHelper.log(e);
                });
    }
}
