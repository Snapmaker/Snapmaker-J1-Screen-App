package fabscreen.features.machinetools.control.j1;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.view.BaseViewModel;

public class J1ControlViewModel extends BaseViewModel {

    private final MachineController mMachineController;

    public J1ControlViewModel() {
        mMachineController = getServiceContainer().getService(IMachine.class).getMachineController();
        mMachineController.subscribeCoordinate();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mMachineController.unSubscribeCoordinate();
    }
}
