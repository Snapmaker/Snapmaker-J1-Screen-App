package fabscreen.features.machinetools.calibration.j1Platform.vibrationcalibration;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.Observable;

public class VibrationCalibrationViewModel extends BaseViewModel {
    private final int[] mFreqs = new int[]{80, 65, 50, 35};
    private final MachineController mMachineController;

    public VibrationCalibrationViewModel() {
        mMachineController = getServiceContainer().getService(IMachine.class).getMachineController();
    }

    public Observable<Boolean> enableAndSetCompensationFreq(int type, int index) {
        return ensureVibrationCompensationEnabled()
                .flatMap(enabled -> mMachineController.saveVibrationCompensationFreq(type, mFreqs[index]))
                .map(ResponseStructure::isSuccess);
    }

    private Observable<Boolean> ensureVibrationCompensationEnabled() {
        return mMachineController.getVibrationCompensationEnabled()
                .flatMap(response -> response.isSuccess() && ((BoolProp) response.dataProp).getValue() ? Observable.just(true) : enableCompensation());
    }

    public Observable<Boolean> enableCompensation() {
        return mMachineController.setVibrationCompensationEnabled(true)
                .map(ResponseStructure::isSuccess);
    }
}
