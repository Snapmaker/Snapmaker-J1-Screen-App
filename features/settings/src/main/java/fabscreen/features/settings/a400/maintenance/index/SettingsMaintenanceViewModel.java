package fabscreen.features.settings.a400.maintenance.index;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.Observable;

public class SettingsMaintenanceViewModel extends BaseViewModel {

    private final IMachine mMachine;

    public SettingsMaintenanceViewModel() {
        mMachine = getServiceContainer().getService(IMachine.class);
    }

    public Observable<Boolean> resetMachine() {
        return mMachine.getMachineController().requestMachineFactoryReset(0).map(ResponseStructure::isSuccess);
    }

    public IMachine.WorkType getWorkType() {
        return mMachine.getMachineInfoSubjectHolder().getValue().workType;
    }
}
