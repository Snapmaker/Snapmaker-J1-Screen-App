package fabscreen.platform.base.service.machine.controller;

import java.util.HashSet;

public enum MachineOperationStatus {
    /**
     * Machine operation status
     * https://github.com/Snapmaker/SnapmakerController-IDEX/blob/main/snapmaker/module/system.h
     */
    SYSTEM_STATUS_IDLE(0),
    SYSTEM_STATUS_STARTING(1),
    SYSTEM_STATUS_PRINTING(2),
    SYSTEM_STATUS_PAUSING(3),
    SYSTEM_STATUS_PAUSED(4),
    SYSTEM_STATUS_STOPPING(5),
    SYSTEM_STATUS_STOPPED(6),
    SYSTEM_STATUS_FINISHING(7),
    SYSTEM_STATUS_COMPLETED(8),
    SYSTEM_STATUS_RECOVERING(9),
    SYSTEM_STATUS_RESUMING(10),
    SYSTEM_STATUS_POWER_LOSS_RESUMING(11),

    // 3Dp calibtration
    SYSTEM_STATUE_CALIBRATION(31),
    SYSTEM_STATUE_CALIBRATION_Z_PROBING(32),
    SYSTEM_STATUE_CALIBRATION_XY_PROBING(33);

    private static final HashSet<MachineOperationStatus> PRINT_STATUS = new HashSet<MachineOperationStatus>() {{
        add(SYSTEM_STATUS_STARTING);
        add(SYSTEM_STATUS_PRINTING);
        add(SYSTEM_STATUS_PAUSING);
        add(SYSTEM_STATUS_PAUSED);
        add(SYSTEM_STATUS_STOPPING);
        add(SYSTEM_STATUS_STOPPED);
        add(SYSTEM_STATUS_FINISHING);
        add(SYSTEM_STATUS_RECOVERING);
        add(SYSTEM_STATUS_RESUMING);
        add(SYSTEM_STATUS_POWER_LOSS_RESUMING);
    }};

    private static final HashSet<MachineOperationStatus> PRINT_STATUS_FILAMENT_OPERATION_AVAILABLE = new HashSet<MachineOperationStatus>() {{
        add(SYSTEM_STATUS_IDLE);
        add(SYSTEM_STATUS_PAUSED);
        add(SYSTEM_STATUS_STOPPED);
        add(SYSTEM_STATUS_COMPLETED);
    }};

    private static final HashSet<MachineOperationStatus> PRINT_STATUS_CHANGES = new HashSet<MachineOperationStatus>() {{
        add(SYSTEM_STATUS_STARTING);
        add(SYSTEM_STATUS_PAUSING);
        add(SYSTEM_STATUS_STOPPING);
        add(SYSTEM_STATUS_FINISHING);
        add(SYSTEM_STATUS_RECOVERING);
        add(SYSTEM_STATUS_RESUMING);
        add(SYSTEM_STATUS_POWER_LOSS_RESUMING);
    }};

    private int value = 0;

    private MachineOperationStatus(int value) {
        this.value = value;
    }

    public static MachineOperationStatus valueOf(int value) {
        switch (value) {
            case 0:
                return SYSTEM_STATUS_IDLE;
            case 1:
                return SYSTEM_STATUS_STARTING;
            case 2:
                return SYSTEM_STATUS_PRINTING;
            case 3:
                return SYSTEM_STATUS_PAUSING;
            case 4:
                return SYSTEM_STATUS_PAUSED;
            case 5:
                return SYSTEM_STATUS_STOPPING;
            case 6:
                return SYSTEM_STATUS_STOPPED;
            case 7:
                return SYSTEM_STATUS_FINISHING;
            case 8:
                return SYSTEM_STATUS_COMPLETED;
            case 9:
                return SYSTEM_STATUS_RECOVERING;
            case 10:
                return SYSTEM_STATUS_RESUMING;
            case 11:
                return SYSTEM_STATUS_POWER_LOSS_RESUMING;
            case 31:
                return SYSTEM_STATUE_CALIBRATION;
            case 32:
                return SYSTEM_STATUE_CALIBRATION_Z_PROBING;
            case 33:
                return SYSTEM_STATUE_CALIBRATION_XY_PROBING;
            default:
                return null;
        }
    }

    public static boolean isPrinting(int value) {
        MachineOperationStatus machineOperationStatus = MachineOperationStatus.valueOf(value);
        if (machineOperationStatus == null) return false;
        return PRINT_STATUS.contains(machineOperationStatus);
    }

    public static boolean isPrintChange(int value) {
        MachineOperationStatus machineOperationStatus = MachineOperationStatus.valueOf(value);
        if (machineOperationStatus == null) return false;
        return PRINT_STATUS_CHANGES.contains(machineOperationStatus);
    }

    public static boolean isPrintFilamentChangeAvailable(int value) {
        MachineOperationStatus machineOperationStatus = MachineOperationStatus.valueOf(value);
        if (machineOperationStatus == null) return false;
        return PRINT_STATUS_FILAMENT_OPERATION_AVAILABLE.contains(machineOperationStatus);
    }

    public int value() {
        return this.value;
    }

    public boolean valueEquals(int status) {
        return this.value == status;
    }
}
