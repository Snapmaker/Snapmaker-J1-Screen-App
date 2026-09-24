package fabscreen.platform.base.model.system;

import java.util.Locale;

/**
 * Old Machine Status moved from FabScreenPacket
 * should be refactor later
 * its name conflicts with new class, so change it
 */
@Deprecated
public class DeprecatedMachineInfo {

    public static DeprecatedMachineInfo sDefaultInstance = new DeprecatedMachineInfo();
    public boolean isDefault = true;
    public double x;
    public double y;
    public double z;
    public double e;
    public int bedTemperature;
    public int bedTargetTemperature;
    public int leftNozzleTemperature;
    public int leftNozzleTargetTemperature;
    public short feedRate;
    public double laserPower;
    public int spindleSpeed;
    public double b;
    public int printerStatus;
    public int peripheralStatus;
    public int headStatus;
    public int performLineNumber;
    public int rightNozzleTemperature;
    public int rightNozzleTargetTemperature;
    //todo dual nozzle right nozzle status
    public int headTemperatureRight;
    public int headTargetTemperatureRight;

    public static DeprecatedMachineInfo getDefaultInstance() {
        return sDefaultInstance;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%s (%.2f, %.2f, %.2f) %.2f Temp %d/%d %d/%d headInfo %d %.1f %d (extend b-axis %.2f) printer %d %d %d",
                this.getClass().getSimpleName(), x, y, z, e,
                bedTemperature, bedTargetTemperature, leftNozzleTemperature, leftNozzleTargetTemperature,
                feedRate, laserPower, spindleSpeed, b,
                printerStatus, peripheralStatus, headStatus);
    }
}
