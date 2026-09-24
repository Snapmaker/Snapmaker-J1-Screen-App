package fabscreen.platform.base.service.machine.entity.module;

import fabscreen.platform.base.R;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import io.reactivex.Observable;

public class RotaryModule extends Module {

    public RotaryModule(ModuleInfo info, IMachine mc, MachineConnectionController cc) {
        super(info, mc, cc);
    }

    @Override
    protected void init() {
        // No inherent properties
    }

    @Override
    public String getDisplayName() {
        return getAppContext().getString(R.string.all_rotary_module);
    }

    @Override
    public Observable<ResponseStructure> requestInfo() {
        // No inherent properties
        return null;
    }
}
