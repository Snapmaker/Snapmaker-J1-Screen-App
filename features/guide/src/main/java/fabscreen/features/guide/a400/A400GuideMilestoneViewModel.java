package fabscreen.features.guide.a400;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseViewModel;

public class A400GuideMilestoneViewModel extends BaseViewModel {

    private final IMachine mMachine;

    public A400GuideMilestoneViewModel() {
        mMachine = getServiceContainer().getService(IMachine.class);
    }


    public boolean isRotaryAvailable() {
        return mMachine.getMachineInfoSubjectHolder().getValue().isRotaryAvailable;
    }

    public int getHeadType() {
        return mMachine.getMachineInfoSubjectHolder().getValue().headType;
    }
}
