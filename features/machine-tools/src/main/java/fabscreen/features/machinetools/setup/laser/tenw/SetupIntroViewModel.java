package fabscreen.features.machinetools.setup.laser.tenw;

import static fabscreen.platform.base.RoutePath.TOOLS_CALIBRATION_A400_LASER_10W_CAMERA_CALIBRATION;
import static fabscreen.platform.base.RoutePath.TOOLS_CALIBRATION_A400_LASER_THICKNESS_MEASURE_CALIBRATION;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.LaserController;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.Observable;

public class SetupIntroViewModel extends BaseViewModel {

    public Observable<Integer> setMode(String destination) {
        LaserController laserController = getServiceContainer().getService(IMachine.class).getLaserController();
        switch (destination) {
            case TOOLS_CALIBRATION_A400_LASER_10W_CAMERA_CALIBRATION:
                return laserController.setCalibrationMode(2).map(response -> response.resultProp.getValue());

            case TOOLS_CALIBRATION_A400_LASER_THICKNESS_MEASURE_CALIBRATION:
                return laserController.setCalibrationMode(0).map(response -> response.resultProp.getValue());

            default:
                return Observable.just(0);
        }
    }
}
