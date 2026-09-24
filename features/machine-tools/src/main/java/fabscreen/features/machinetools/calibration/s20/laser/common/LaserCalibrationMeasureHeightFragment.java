package fabscreen.features.machinetools.calibration.s20.laser.common;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.s20.laser.CalibrationLaserActivity;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.presenter.ControlXYZPanelWidgetPresenter;

public class LaserCalibrationMeasureHeightFragment extends BaseFragment {
    private ControlXYZPanelWidgetPresenter mControlPanelPresenter;

    public static LaserCalibrationMeasureHeightFragment newInstance() {
        return new LaserCalibrationMeasureHeightFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.laser_calibration_measure_height);

        mControlPanelPresenter = new ControlXYZPanelWidgetPresenter(disposables);
        mControlPanelPresenter.bind(view, 0.1f, 1f, 10f);
        mControlPanelPresenter.connect();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_laser_calibration_measure_height;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_laser_calibration_next)
    void onClickNext() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getObservable()
                .as(bindToLifecycle())
                .subscribe(status -> {
                    float bottomZ = status.currentPosition.getZ();
                    ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLaserBottomZ(bottomZ);
                    Logger.d("Set bottomZ %.2f", bottomZ);
                });

        if (getActivity() != null) {
            ((CalibrationLaserActivity) getActivity()).gotoSafetyGogglesFragment();
        }
    }
}
