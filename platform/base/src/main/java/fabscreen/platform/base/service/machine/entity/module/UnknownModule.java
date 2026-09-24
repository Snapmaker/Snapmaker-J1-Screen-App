package fabscreen.platform.base.service.machine.entity.module;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import io.reactivex.Observable;

public class UnknownModule extends Module {
    public UnknownModule(ModuleInfo info, IMachine mc, MachineConnectionController cc) {
        super(info, mc, cc);
    }

    @Override
    protected void init() {

    }

    @Override
    public <T extends IStructure> Observable<ResponseStructure<T>> requestInfo() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Unknown Module";
    }
}
