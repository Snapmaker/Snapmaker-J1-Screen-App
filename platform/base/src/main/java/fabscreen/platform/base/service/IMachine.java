package fabscreen.platform.base.service;

import java.util.Set;

import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.service.machine.controller.CNCController;
import fabscreen.platform.base.service.machine.controller.ErrorController;
import fabscreen.platform.base.service.machine.controller.FDMController;
import fabscreen.platform.base.service.machine.controller.LaserController;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.controller.NewPrintController;
import fabscreen.platform.base.service.machine.controller.PrintController;
import fabscreen.platform.base.service.machine.controller.UpdateController;
import fabscreen.platform.lib.SubjectHolder;
import io.reactivex.Observable;

public interface IMachine {

    SubjectHolder<MachineInfo> getMachineInfoSubjectHolder();

    SubjectHolder<MachineStatus> getMachineStatusSubjectHolder();

    MachineController getMachineController();

    FDMController getFDMController();

    LaserController getLaserController();

    CNCController getCNCController();

    PrintController getPrintController();

    NewPrintController getNewPrintController();

    ErrorController getErrorController();

    UpdateController getUpdateController();

    void setMockModeEnabled(boolean active);

    boolean getMockModeEnabled();

    void setMockMachineSeriesModel(int series, int model, Set<String> debugModuleList);

    MachineConnectionController getConnectionController();

    Observable<Boolean> onRestart();

    class WorkStatus {
        public static final int WORK_STATUS_IDLE = 0;
        public static final int WORK_STATUS_STARTING = 1;
        public static final int WORK_STATUS_PRINTING = 2;
        public static final int WORK_STATUS_PAUSING = 3;
        public static final int WORK_STATUS_PAUSED = 4;
        public static final int WORK_STATUS_STOPPING = 5;
        public static final int WORK_STATUS_STOPPED = 6;
        public static final int WORK_STATUS_FINISHING = 7;
        public static final int WORK_STATUS_COMPLETED = 8;
        public static final int WORK_STATUS_RECOVERING = 9;
        public static final int WORK_STATUS_RESUMING = 10;
    }

    enum WorkType {
        NONE,
        FDM,
        LASER,
        CNC
    }

    class MachineSeries {
        public static final int UNDEFINED = 0;
        public static final int A = 1;
        public static final int J = 2;
    }

    class MachineModel {
        public static final int UNDEFINED = 0;
        public static final int A150 = 1;
        public static final int A250 = 2;
        public static final int A350 = 3;
        public static final int A400 = 4;
        public static final int J1 = 1;
    }

    class Product {
        public static final int A150 = 0;
        public static final int A250 = 1;
        public static final int A350 = 2;
        public static final int A400 = 3;
        public static final int J1 = 4;
    }


    class DeprecatedMachineModel {

        public static final int MACHINE_MODEL_UNKNOWN = 0;
        public static final int MACHINE_MODEL_SNAPMAKER_A150 = 1;
        public static final int MACHINE_MODEL_SNAPMAKER_A250 = 2;
        public static final int MACHINE_MODEL_SNAPMAKER_A350 = 3;
        public static final int MACHINE_MODEL_SNAPMAKER_A400 = 4;
    }

    class DeprecatedMachineSeries {
        // machine type
        public static final int MACHINE_UNKNOWN = 0;
        public static final int MACHINE_A_0 = 1;
        public static final int MACHINE_J_1 = 2;
        public static final int MACHINE_A_400 = 3;
    }

    class DeprecatedMachineTitle {

        // Machine
        public static final String MACHINE_TYPE_A150 = "Snapmaker 2 Model A150";
        public static final String MACHINE_TYPE_A250 = "Snapmaker 2 Model A250";
        public static final String MACHINE_TYPE_A350 = "Snapmaker 2 Model A350";
        public static final String MACHINE_TYPE_A400 = "Snapmaker 2 Model A400";
    }

    class CoordinateAxis {
        public static final int FLAG_X = 1;
        public static final int FLAG_Y = 1 << 1;
        public static final int FLAG_Z = 1 << 2;
        public static final int FLAG_B = 1 << 3;
        public static final int FLAG_XY = FLAG_X | FLAG_Y;
        public static final int FLAG_XYZ = FLAG_X | FLAG_Y | FLAG_Z;
        public static final int FLAG_XYZB = FLAG_X | FLAG_Y | FLAG_Z | FLAG_B;
    }
}
