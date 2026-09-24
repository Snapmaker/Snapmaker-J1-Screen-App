package fabscreen.features.machinetools.control;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.model.system.DeprecatedMachineInfo;
import fabscreen.platform.base.model.system.MachineStatusManager;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.Observable;

public class ControlViewModel extends BaseViewModel {
    public int getHeadType() {
        return 0;//ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType;
    }

    public Observable<Boolean> leaveControl(int headType) {
        switch (headType) {
            case Module.ModuleType.HEAD_UNPLUGGED:
            case Module.ModuleType.HEAD_3DP:
                return turnOffHead().flatMap(success -> turnOffBed());
            case Module.ModuleType.HEAD_LASER:
            case Module.ModuleType.HEAD_LASER_10W:
            case Module.ModuleType.HEAD_CNC:
                return shutDownLaserOrCNC();
            default:
                return Observable.just(true);
        }
    }

    private Observable<Boolean> turnOffHead() {
        DeprecatedMachineInfo machineInfo = MachineStatusManager.getMachineInfoHolder().getValue();
        if (machineInfo.leftNozzleTargetTemperature > 0) {
            return ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("M104 S0").map(response -> true);
        } else {
            return Observable.just(true);
        }
    }

    private Observable<Boolean> turnOffBed() {
        DeprecatedMachineInfo machineInfo = MachineStatusManager.getMachineInfoHolder().getValue();
        if (machineInfo.bedTargetTemperature > 0) {
            return ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("M140 S0").map(response -> true);
        } else {
            return Observable.just(true);
        }
    }

    private Observable<Boolean> shutDownLaserOrCNC() {
        return ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("M5").flatMap(res -> Observable.just(true));
    }
}
