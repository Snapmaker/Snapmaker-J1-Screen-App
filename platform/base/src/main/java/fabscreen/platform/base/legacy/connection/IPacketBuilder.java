package fabscreen.platform.base.legacy.connection;

import androidx.annotation.NonNull;

public interface IPacketBuilder {
    IPacket buildPacket(byte attribute, byte commandSet, byte commandId, byte[] payload);

    IPacket gcodeRequest(String gcode, int lineno);

    IPacket printGcodeRequest(@NonNull String gcode, int lineno);

    IPacket statusRequestMachineStatus();

    IPacket statusRequestMachineAbnormalStatus();

    IPacket statusRequestMachineStartPrint();

    IPacket statusRequestMachinePausePrint();

    IPacket statusRequestMachineResumePrint();

    IPacket statusRequestMachineStopPrint();

    IPacket statusRequestMachineFinishPrint();

    IPacket statusRequestLineNumber();

    IPacket statusRequestPrintProgress();

    IPacket statusRequestResetErrorFlag();

    IPacket statusRequestResumePrint();

    IPacket statusWaitRequest();

    IPacket statusRequestCoordinateSystem();

    IPacket statusSyncMachineStatus(double x, double y, double z, double e);

    IPacket setWorkspace(int xSize, int xHomeOffset, int xMaxDir, int xStepperDir,
                         int ySize, int yHomeOffset, int yMaxDir, int yStepperDir,
                         int zSize, int zHomeOffset, int zMaxDir, int zStepperDir);

    IPacket startAutoCalibration();

    IPacket startAutoCalibration(int grid);

    IPacket startManualCalibration();

    IPacket startManualCalibration(int grid);

    IPacket gotoCalibrationPoint(int point);

    IPacket moveCalibrationPoint(double offset);

    IPacket saveCalibration();

    IPacket exitCalibration();

    IPacket resetCalibration();

    IPacket getLaserFocalLength();

    IPacket setLaserFocalLength(float focalHeight);

    IPacket startLaserFocusSetting(float xPos, float yPos, float zPos);

    IPacket startLaserFineTune();

    IPacket startLaserFineTune(float zOffset);

    IPacket fastCalibration();

    IPacket requestAdjustSettings(byte type, float value);

    IPacket getAdjustSettings(byte type);

    IPacket setAFAssistLightState(byte state);

    IPacket getMachineSize();

    IPacket checkCalibrationEverSucceeded();

    IPacket gotoZHome();

    IPacket gotoAbsolutePosition(float x, float y, float z);

    IPacket gotoAbsolutePosition(float x, float y, float z, float f);

    IPacket gotoRelativePosition(float x, float y, float z);

    IPacket gotoRelativePosition(float x, float y, float z, float f);

    IPacket requestExtrusion(int type, float lengthIn, float speedIn, float lengthOut, float speedOut);

    IPacket setupLaserNetwork(String ssid, String password);

    IPacket getLaserStatus();

    IPacket getLaserBtStatus();

    IPacket getEnclosureStatus();

    IPacket setEnclosureLed(int value);

    IPacket setEnclosureFan(int value);

    IPacket setEnclosureDoorDetection(boolean enabled);

    IPacket requestRotaryModuleStatus();

    IPacket requestEmergencyStopStatus();

    IPacket requestAirPurifierAddOnStatus();

    IPacket requestAirPurifierFanStatus();

    IPacket setAirPurifierFanEnabled(boolean enabled);

    IPacket setAirPurifierFanSpeed(int level);

    IPacket requestAirPurifierFilterLifeTime();

    IPacket startUpdate();

    IPacket requestUpdatePackage();

    IPacket sendUpdatePackage(byte opCode, short index, byte[] updatePackage);

    IPacket checkControllerVersion();

    IPacket requestModuleVersion();

    IPacket printBatchGcodeRequest(int startLine, int endLine, String gcode);
}
