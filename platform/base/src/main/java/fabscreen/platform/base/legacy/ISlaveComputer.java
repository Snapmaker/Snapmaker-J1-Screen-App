package fabscreen.platform.base.legacy;

import fabscreen.platform.base.ModuleVersion;
import fabscreen.platform.base.legacy.connection.SSTPPacket;
import fabscreen.platform.base.legacy.connection.SSTPPacketContent;
import fabscreen.platform.base.model.ILaserCameraController;
import fabscreen.platform.base.model.system.DeprecatedMachineInfo;
import io.reactivex.Observable;

@Deprecated
public interface ISlaveComputer {

    int FLAG_X = 1;
    int FLAG_Y = 1 << 1;
    int FLAG_Z = 1 << 2;
    int FLAG_B = 1 << 3;
    int FLAG_XY = FLAG_X | FLAG_Y;
    int FLAG_XYZ = FLAG_X | FLAG_Y | FLAG_Z;
    int FLAG_XYZB = FLAG_X | FLAG_Y | FLAG_Z | FLAG_B;


    // TODO: 2021/12/31 Below codes are for test use, should be deleted. SACP and SSTP are not compatible.

    void connect();

    void setHeartbeatEnabled(boolean enabled);

    void send(SSTPPacket packet);

    void onEmergencyStop();

    /**
     * Send G-code (not print) to serial port (0x01).
     *
     * @param gcode G-code command to be sent.
     */
    Observable<SSTPPacketContent.GcodeResponse> sendGcode(String gcode);

    Observable<SSTPPacketContent.GcodeResponse> sendGcode(String gcode, boolean replyContent);

    // 0x03 Print G-code request
    Observable<SSTPPacketContent.GcodeResponse> sendPrintGcode(String gcode, int lineno);

    // 0x13 Batch Print G-code request
    void sendPrintBatchGcode(int startLine, int endLine, String gcode);

    // 0x07 Machine Status
    // 0x07 0x01
    Observable<DeprecatedMachineInfo> requestMachineStatus();

    // 0x07 0x02
    Observable<SSTPPacketContent.MachineErrors> getMachineErrors();

    Observable<SSTPPacketContent.MachineErrors> getMachineErrors(int timeout);

    Observable<SSTPPacketContent.MachineErrors> watchMachineErrors();

    // 0x07 0x03
    Observable<Integer> start();

    Observable<Integer> pause();

    Observable<Integer> resume();

    Observable<Integer> stop();

    Observable<Integer> finish();

    Observable<Integer> getLineNumber();

    /**
     * Enable the Batch Print Gcode.  (0x07 0x12)
     */
    Observable<Integer> useBatchGcodeMode(int check);

    /**
     * Status request: Reset Error flag. (0x07 0x0a)
     */
    Observable<Integer> resetErrorFlag();

    /**
     * 0x08 0x0b
     */
    Observable<Integer> resumeFromPowerOutage();

    /**
     * 0x08 0x0c
     * WAIT event, send only by FW.
     * <p>
     * When firmware buffer is drained, it may send WAIT event asking for more G-code.
     * It sendsWAIT event when in print mode only.
     */
    Observable<Boolean> watchWaitEvents();

    // 0x07 0x0e
    Observable<SSTPPacketContent.CoordinateSystem> requestCoordinateSystem();

    Observable<SSTPPacketContent.CoordinateSystem> requestCoordinateSystem(int timeout);

    // 0x09 Machine Settings
    Observable<Boolean> setWorkspace(int xSize, int xHomeOffset, int xMaxDir, int xStepperDir,
                                     int ySize, int yHomeOffset, int yMaxDir, int yStepperDir,
                                     int zSize, int zHomeOffset, int zMaxDir, int zStepperDir);

    Observable<Boolean> startAutoCalibration();

    Observable<Boolean> startAutoCalibration(int grid);

    Observable<Integer> getAutoCalibrationProgress();

    Observable<Boolean> startManualCalibration();

    Observable<Boolean> startManualCalibration(int grid);

    Observable<Boolean> gotoCalibrationPoint(int point);

    Observable<Boolean> moveCalibrationPoint(double offset);

    /**
     * Settings: Save Calibration. (0x09 0x07)
     */
    Observable<Boolean> saveCalibration();

    Observable<Boolean> exitCalibration();

    Observable<Boolean> resetCalibration();

    Observable<SSTPPacketContent.MachineSize> getMachineSize();

    /**
     * Settings: Get Focal Length (0x09 0x0a)
     */
    Observable<Float> getLaserFocalLength();

    Observable<Float> getLaserFocalLength(int timeout);

    // 0x09 0x0b
    Observable<Boolean> setLaserFocalLength(float focalLength);

    // 0x09 0x0c
    Observable<Boolean> startLaserFocusSetting(float xPosition, float yPosition, float zPosition);

    // 0x09 0x0d
    Observable<Boolean> startLaserFineTune();

    Observable<Boolean> startLaserFineTune(float zOffset);

    /**
     * Settings: Fast 3DP Calibration (0x09 0x0e)
     */
    Observable<Integer> fastCalibration();

    /**
     * Settings: Adjust Settings (0x09 0x0f)
     */
    Observable<Integer> requestAdjustSetting(int type, float value);

    Observable<Integer> requestAdjustSettingFeedRate(float value);

    Observable<Integer> requestAdjustSettingNozzleTemp(float value);

    Observable<Integer> requestAdjustSettingHeatedBedTemp(float value);

    Observable<Integer> requestAdjustSettingLaserPower(float value);

    Observable<Integer> requestAdjustSettingZOffset(float value);

    Observable<Integer> requestAdjustSettingCNCPower(float value);

    Observable<SSTPPacketContent.AdjustSettings> getAdjustSetting(int type);

    Observable<Boolean> setAFAssistLightState(int state);

    Observable<SSTPPacketContent.AdjustSettings> getAdjustSettingFeedRate();

    Observable<SSTPPacketContent.AdjustSettings> getAdjustSettingLaserPower();

    Observable<SSTPPacketContent.AdjustSettings> getAdjustSettingZOffset();

    Observable<SSTPPacketContent.AdjustSettings> getAdjustSettingCNCPower();

    /**
     * 0x09 0x15
     */
    Observable<Boolean> checkCalibrationEverSucceeded();

    /**
     * Movement: Home Z (G28 Z) (0x0b 0x01)
     */
    // 0x0b
    Observable<Boolean> gotoZHome();

    /**
     * Movement: Set Position (G92), this method is used to solve the G92 saving issue temporarily.
     * <p>
     * G92 X{} Y{} Z{}
     */
    Observable<Boolean> setPosition(float x, float y, float z, int flag);

    Observable<Boolean> setPosition(float x, float y, float z, float b, int flag);

    Observable<Boolean> gotoAbsolutePosition(float x, float y, float z);

    Observable<Boolean> gotoAbsolutePosition(float x, float y, float z, float f);

    Observable<Boolean> gotoRelativePosition(float x, float y, float z);

    Observable<Boolean> gotoRelativePosition(float x, float y, float z, float f);

    Observable<String> getControllerVersion();

    Observable<Boolean> startUpdate();

    Observable<Short> watchPacketIndexRequest();

    /**
     * Movement: Request Extrusion (0x0b 0x04)
     */
    Observable<Boolean> requestExtrusion(int type, float lengthIn, float speedIn, float lengthOut, float speedOut);

    /**
     * 0xa9 0x01
     * 0xa9 0x02
     */
    void sendUpdatePackage(byte opCode, short index, byte[] content);

    void requestModuleVersion();

    Observable<ModuleVersion> watchModuleVersion();

    // 0x0d 0x01
    Observable<Boolean> setupLaserNetwork(String SSID, String password);

    Observable<SSTPPacketContent.LaserWifiStatus> getLaserWifiStatus();

    // 0x0d 0x05
    Observable<SSTPPacketContent.LaserBtStatus> getLaserBluetoothStatus();

    /**
     * Add-on: Get enclosure status (0x11 0x01)
     */
    Observable<SSTPPacketContent.EnclosureStatus> getEnclosureStatus();

    /**
     * Add-on: Set enclosure Led (0x11 0x02)
     */
    Observable<Boolean> setEnclosureLed(int value);

    /**
     * Add-on: Set enclosure Fan (0x11 0x03)
     */
    Observable<Boolean> setEnclosureFan(int value);

    /**
     * Add-on: Set enclosure Door Detection (0x11 0x04)
     */
    Observable<Boolean> setEnclosureDoorDetection(boolean enabled);

    // 0x11 0x08
    Observable<Byte> requestRotaryModuleStatus();

    // 0x11 0x07
    Observable<Byte> requestEmergencyStopStatus();

    Observable<Byte> watchEmergencyStopStatus();

    Observable<SSTPPacketContent.AirPurifierStatus> requestAirPurifierAddOnStatus();

    Observable<SSTPPacketContent.AirPurifierStatus> watchAirPurifierAddOnStatus();

    Observable<SSTPPacketContent.AirPurifierFan> requestAirPurifierFan();

    Observable<Boolean> setAirPurifierEnabled(boolean enabled);

    Observable<Boolean> setAirPurifierFanSpeedLevel(int level);

    Observable<Integer> getAirPurifierFilterLifeTime();

    Observable<Integer> watchAirPurifierFilterLifeTime();

    Observable<SSTPPacketContent.BatchGcodeResponse> getBatchGcodeResponseSubject();

    Observable<SSTPPacketContent.MasterState> getMasterState();

    /**
     * Request execution header security status (07 11)
     */
    Observable<SSTPPacketContent.HeaderSecurity> requestHeaderSecurityStatus();

    Observable<SSTPPacketContent.HeaderSecurity> watchHeaderSecurityStatus();

    /**
     * Listen to the master actively suspend the task signal (08 04)
     */
    Observable<Integer> watchPrintPauseState();

    /**
     * Request header Online Sync ID (09 12)
     */
    Observable<Integer> requestHeaderOnlineSyncId(int timeout);

    /**
     * Set header Online Sync ID (09 13)
     *
     * @param headerId
     * @return
     */
    Observable<Boolean> setHeaderOnlineSyncId(int headerId);

    void setAbnormalTemperatureRange(int protectTemperature, int recoveryTemperature);

    Observable<Integer> getMachineType();

    Observable<SSTPPacketContent.DualExtruderName> getDualExtruderNameObservable();

    Observable<Boolean> moveLevelingBedCalibration(int index);

    DeprecatedMachineController getMachineController();

    void setMachineController(DeprecatedMachineController mc);

    ILaserCameraController getLaserCameraController();

    void setLaserCameraController(ILaserCameraController lc);
}
