package fabscreen.features.settings.common;

import java.util.ArrayList;
import java.util.List;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseViewModel;

public class A400ModuleAssistantViewModel extends BaseViewModel {

    private final IMachine mMachine;

    public A400ModuleAssistantViewModel() {
        mMachine = getServiceContainer().getService(IMachine.class);
    }

    public String getMachineName() {
        return ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineName();
    }

    public List<String> getModuleNameList() {
        List<String> nameList = new ArrayList<>();
        MachineInfo machineInfo = mMachine.getMachineInfoSubjectHolder().getValue();
        if (machineInfo.moduleList != null) {
            for (Module module : machineInfo.moduleList) {
                String displayName = module.getDisplayName();
                nameList.add(displayName);
            }
        }

        return nameList;
    }
}
