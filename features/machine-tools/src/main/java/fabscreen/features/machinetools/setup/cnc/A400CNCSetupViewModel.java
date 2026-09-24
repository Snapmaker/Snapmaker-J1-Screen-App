package fabscreen.features.machinetools.setup.cnc;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseViewModel;

public class A400CNCSetupViewModel extends BaseViewModel {

    private final MachineInfo mMachineInfo;

    public A400CNCSetupViewModel() {
        mMachineInfo = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue();
    }

    public boolean isRotaryAvailable() {
        return mMachineInfo.isRotaryAvailable;
    }

    public boolean is200WPower() {
        return mMachineInfo.headType == Module.ModuleType.HEAD_CNC_200W;
    }
}
