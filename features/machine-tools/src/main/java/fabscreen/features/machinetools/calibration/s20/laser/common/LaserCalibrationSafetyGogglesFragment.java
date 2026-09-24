package fabscreen.features.machinetools.calibration.s20.laser.common;


import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.s20.laser.CalibrationLaserActivity;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class LaserCalibrationSafetyGogglesFragment extends BaseFragment {
    public static LaserCalibrationSafetyGogglesFragment newInstance() {
        return new LaserCalibrationSafetyGogglesFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_preview_laser_prepare_safety_goggles;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_preview_laser_prepare_safety_goggles_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            MachineInfo machineInfo = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue();
            boolean isRotaryOnline = machineInfo.isRotaryAvailable;
            int calibrationMode;
            if (isRotaryOnline) {
                // Laser calibration with rotary module.
                calibrationMode = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaser4AxisCalibrationMode();
                if (calibrationMode == 0) {
                    ((CalibrationLaserActivity) getActivity()).gotoLaser4AxisSetOrigin();
                } else {
                    ((CalibrationLaserActivity) getActivity()).gotoLaser4AxisCoarseTurning();
                }
            } else {
                calibrationMode = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserCalibrationMode();
                if (calibrationMode == 0) {
                    ((CalibrationLaserActivity) getActivity()).gotoLaserCalibrationSetOriginFragment();
                } else {
                    ((CalibrationLaserActivity) getActivity()).gotoCoarseTuningFragment();
                }
            }
        }
    }
}
