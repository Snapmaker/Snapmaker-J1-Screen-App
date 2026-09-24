package fabscreen.platform.base.lib.update;

import java.util.List;

public class MachineInfoLite {
    public String controllerFWVersion;
    public List<ModuleInfoLite> moduleVersionList;

    public static class ModuleInfoLite {
        public int moduleId;
        public String version;
        public String displayName;

        public ModuleInfoLite(int id, String version, String displayName) {
            this.moduleId = id;
            this.version = version;
            this.displayName = displayName;
        }
    }
}
