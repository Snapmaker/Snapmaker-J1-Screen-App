package fabscreen.features.machinetools.control.a350.modules.bed;

import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.Constants;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.model.system.DeprecatedMachineInfo;
import fabscreen.platform.base.model.system.MachineStatusManager;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.Observable;

public class HeatedBedControlViewModel extends BaseViewModel {
    public void requestSetTargetDegree(Integer degree) {
        ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("M140 S" + degree);
    }

    public Observable<DeprecatedMachineInfo> getMachineStatusObservable() {
        return MachineStatusManager.getMachineInfoHolder().getObservable()
                .throttleLast(Constants.THROTTLE_DURATION, TimeUnit.MILLISECONDS);
    }

    public DeprecatedMachineInfo getMachineStatus() {
        return MachineStatusManager.getMachineInfoHolder().getValue();
    }
}
