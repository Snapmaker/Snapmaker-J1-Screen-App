package fabscreen.platform.base.service.machine.entity;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineConnectionController;

public abstract class Toolhead extends Module {
    public Toolhead(ModuleInfo info, IMachine mc, MachineConnectionController cc) {
        super(info, mc, cc);
    }

    public static Boolean isToolhead(Object o) {
        return o instanceof Toolhead;
    }

    public static class HeadFactoryId {
        // special tool head for factory and testing
        public static final int HEAD_FACTORY_3DP = 0x97;
        public static final int HEAD_FACTORY_CNC = 0x98;
        public static final int HEAD_FACTORY_LASER = 0x99;

    }
}
