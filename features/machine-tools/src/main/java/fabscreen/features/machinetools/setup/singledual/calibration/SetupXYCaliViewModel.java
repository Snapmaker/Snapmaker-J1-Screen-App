package fabscreen.features.machinetools.setup.singledual.calibration;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.Observable;

public class SetupXYCaliViewModel extends BaseViewModel {
    public Observable<ResponseStructure> setXYCaliMode() {
        return ServiceContainer.getInstance().getService(IMachine.class)
                .getFDMController()
                .setCalibrationMode(101);
    }
}
