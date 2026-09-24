package fabscreen.platform.base.legacy.connection.fabpacket.sstp;

import androidx.annotation.NonNull;

import fabscreen.platform.base.legacy.connection.SSTPPacket;
import okio.Buffer;
import okio.ByteString;

public class SSTPPacketBuilder {
    private static SSTPPacket buildPacket(byte eventId, byte[] content) {
        SSTPPacket packet = SSTPPacket.create();
        packet.setEventId(eventId);
        packet.setContent(content);
        packet.build();
        return packet;
    }

    public static SSTPPacket gcodeRequest(String gcode, int lineno) {
        Buffer buffer = new Buffer();
        buffer.writeInt(lineno);
        buffer.write(gcode.getBytes());

        return buildPacket(SSTPPacket.GCODE_REQUEST_EVENT_ID, buffer.readByteArray());
    }

    public static SSTPPacket printGcodeRequest(@NonNull String gcode, int lineno) {
        Buffer buffer = new Buffer();
        buffer.writeInt(lineno);
        buffer.write(gcode.getBytes());

        return buildPacket(SSTPPacket.PRINT_GCODE_REQUEST_EVENT_ID, buffer.readByteArray());
    }

    public static SSTPPacket printBatchGcodeRequest(int startLine, int endLine, String gcode) {
        Buffer buffer = new Buffer();
        buffer.writeInt(startLine);
        buffer.writeInt(endLine);
        buffer.write(gcode.getBytes());
        byte[] bytes = buffer.readByteArray();
        return buildPacket(SSTPPacket.PRINT_BATCH_GCODE_REQUEST_EVENT_ID, bytes);
    }

    public static SSTPPacket extendGcodeRequest() {
        return buildPacket(SSTPPacket.GCODE_REQUEST_EXTEND_EVENT_ID, new byte[]{(byte) 0x02});
    }

    /*
    public static SSTPPacket printGcodeResponse(@NonNull String response) {
        Buffer buffer = new Buffer();
        buffer.writeString(response, SSTPPacketContent.UTF_8);

        return buildPacket(SSTPPacket.PRINT_GCODE_RESPONSE_EVENT_ID, buffer.readByteArray());
    }
    */

    /**
     * File Operation Request: mount USB to check if USB is plugged in.
     */
    /*
    public static SSTPPacket fileOperationRequestMount() {
        byte[] content = ByteString.decodeHex("00").toByteArray();
        return buildPacket(SSTPPacket.FILE_OPERATION_REQUEST_EVENT_ID, content);
    }

    public static SSTPPacket fileOperationRequestCWD() {
        byte[] content = ByteString.decodeHex("01").toByteArray();
        return buildPacket(SSTPPacket.FILE_OPERATION_REQUEST_EVENT_ID, content);
    }

    public static SSTPPacket fileOperationRequestCD(String dir) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x03);
        buffer.write(dir.getBytes());

        return buildPacket(SSTPPacket.FILE_OPERATION_REQUEST_EVENT_ID, buffer.readByteArray());
    }
    */

    /**
     * File Operation Request: Get files on current directory. The firmware will determine
     * how many filenames to return for a single request, we need to request more than once
     * if there are files of more than one page (e.g. more than 20 files).
     *
     * @param rewind If rewind is true, the file pointer will move to the start and then scan files.
     */
    /*
    public static SSTPPacket fileOperationRequestGetFiles(boolean rewind) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x04);
        buffer.writeByte(rewind ? 0x01 : 0x00);

        return buildPacket(SSTPPacket.FILE_OPERATION_REQUEST_EVENT_ID, buffer.readByteArray());
    }
    */


    /**
     * File Operation Request: Print specified file.
     *
     * @param filename name of file to be print (on current directory).
     * @return packet
     */
    /*
    public static SSTPPacket fileOperationRequestPrintFile(String filename) {
        Buffer buffer = new Buffer();
        buffer.write(ByteString.decodeHex("06").toByteArray());
        buffer.write(filename.getBytes());

        return buildPacket(SSTPPacket.FILE_OPERATION_REQUEST_EVENT_ID, buffer.readByteArray());
    }
    */

    /**
     * Status Sync Request: Machine Status.
     *
     * @return packet
     */
    public static SSTPPacket statusRequestMachineStatus() {
        return buildPacket(SSTPPacket.STATUS_SYNC_REQUEST_EVENT_ID, new byte[]{(byte) 0x01});
    }

    /**
     * Status Sync Request: Anormal Status
     *
     * @return packet
     */
    public static SSTPPacket statusRequestMachineAbnormalStatus() {
        return buildPacket(SSTPPacket.STATUS_SYNC_REQUEST_EVENT_ID, new byte[]{(byte) 0x02});
    }

    /**
     * Status Sync Request: Start Print
     *
     * @return packet
     */
    public static SSTPPacket statusRequestMachineStartPrint() {
        return buildPacket(SSTPPacket.STATUS_SYNC_REQUEST_EVENT_ID, new byte[]{(byte) 0x03});
    }

    /**
     * Status Sync Request: Pause Print
     *
     * @return packet
     */
    public static SSTPPacket statusRequestMachinePausePrint() {
        byte[] content = ByteString.decodeHex("04").toByteArray();
        return buildPacket(SSTPPacket.STATUS_SYNC_REQUEST_EVENT_ID, content);
    }

    /**
     * Status Sync Request: Resume Print
     *
     * @return packet
     */
    public static SSTPPacket statusRequestMachineResumePrint() {
        byte[] content = ByteString.decodeHex("05").toByteArray();
        return buildPacket(SSTPPacket.STATUS_SYNC_REQUEST_EVENT_ID, content);
    }

    /**
     * Status Sync Request: Stop Print
     *
     * @return packet
     */
    public static SSTPPacket statusRequestMachineStopPrint() {
        byte[] content = ByteString.decodeHex("06").toByteArray();
        return buildPacket(SSTPPacket.STATUS_SYNC_REQUEST_EVENT_ID, content);
    }

    /**
     * Status Sync Request: Finish Print (0x07 0x07)
     *
     * @return packet
     */
    public static SSTPPacket statusRequestMachineFinishPrint() {
        return buildPacket(SSTPPacket.STATUS_SYNC_REQUEST_EVENT_ID, new byte[]{(byte) 0x07});
    }

    /**
     * Status Sync Request: Get Line Number (0x07 0x08)
     *
     * @return packet
     */
    public static SSTPPacket statusRequestLineNumber() {
        return buildPacket(SSTPPacket.STATUS_SYNC_REQUEST_EVENT_ID, new byte[]{(byte) 0x08});
    }

    /**
     * Status Request: Get Print Progress (0x07 0x09)
     *
     * @return packet
     */
    public static SSTPPacket statusRequestPrintProgress() {
        return buildPacket(SSTPPacket.STATUS_SYNC_REQUEST_EVENT_ID, new byte[]{(byte) 0x09});
    }

    /**
     * Status Request: Reset error Status Flag (0x07 0x0a)
     *
     * @return packet
     */
    public static SSTPPacket statusRequestResetErrorFlag() {
        return buildPacket(SSTPPacket.STATUS_SYNC_REQUEST_EVENT_ID, new byte[]{(byte) 0x0a});
    }

    /**
     * (0x07 0x0b)
     */
    public static SSTPPacket statusRequestResumePrint() {
        return buildPacket(SSTPPacket.STATUS_SYNC_REQUEST_EVENT_ID, new byte[]{(byte) 0x0b});
    }

    public static SSTPPacket statusWaitRequest() {
        return buildPacket(SSTPPacket.STATUS_SYNC_REQUEST_EVENT_ID, new byte[]{(byte) 0x0c});
    }

    /**
     * (0x07 0x0e)
     */
    public static SSTPPacket statusRequestCoodinateSystem() {
        return buildPacket(SSTPPacket.STATUS_SYNC_REQUEST_EVENT_ID, new byte[]{(byte) 0x0e});
    }

    /**
     * (0x07 0x12)
     */
    public static SSTPPacket setgcodeBatchSending(int check) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x12);
        buffer.writeByte(check == 0 ? 0x00 : 0x01);
        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.STATUS_SYNC_REQUEST_EVENT_ID, content);
    }

    /**
     * 0x08 0x01
     */
    public static SSTPPacket statusSyncMachineStatus(double x, double y, double z, double e) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x01);
        buffer.writeInt((int) (x * 1000));
        buffer.writeInt((int) (y * 1000));
        buffer.writeInt((int) (z * 1000));
        buffer.writeInt((int) (e * 1000));
        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.STATUS_RESPONSE_EVENT_ID, content);
    }

    /**
     * Settings: Set Workspace Size (0x09 0x01)
     */
    public static SSTPPacket setWorkspace(int xSize, int xHomeOffset, int xMaxDir, int xStepperDir,
                                          int ySize, int yHomeOffset, int yMaxDir, int yStepperDir,
                                          int zSize, int zHomeOffset, int zMaxDir, int zStepperDir) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x01);
        buffer.writeInt(xSize * 1000);
        buffer.writeInt(ySize * 1000);
        buffer.writeInt(zSize * 1000);
        buffer.writeInt(xMaxDir);
        buffer.writeInt(yMaxDir);
        buffer.writeInt(zMaxDir);
        buffer.writeInt(xStepperDir);
        buffer.writeInt(yStepperDir);
        buffer.writeInt(zStepperDir);
        buffer.writeInt(xHomeOffset * 1000);
        buffer.writeInt(yHomeOffset * 1000);
        buffer.writeInt(zHomeOffset * 1000);
        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, content);
    }

    /**
     * Settings: Start Auto Calibration (0x09 0x02)
     */
    public static SSTPPacket startAutoCalibration() {
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, new byte[]{(byte) 0x02});
    }

    public static SSTPPacket startAutoCalibration(int grid) {
        byte[] content = new byte[]{(byte) 0x02, (byte) grid};
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, content);
    }

    /**
     * Settings: Start Manual Calibration (0x09 0x04)
     */
    public static SSTPPacket startManualCalibration() {
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, new byte[]{(byte) 0x04});
    }

    public static SSTPPacket startManualCalibration(int grid) {
        byte[] content = new byte[]{(byte) 0x04, (byte) grid};
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, content);
    }

    /**
     * Settings: Goto Calibration Point (0x09 0x05)
     */
    public static SSTPPacket gotoCalibrationPoint(int point) {
        byte[] content = new byte[]{(byte) 0x05, (byte) point};
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, content);
    }


    /**
     * Settings: Move Calibration Point (0x09 0x06)
     */
    public static SSTPPacket moveCalibrationPoint(double offset) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x06);
        buffer.writeInt((int) (offset * 1000));
        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, content);
    }

    /**
     * Settings: Save Calibration (0x09 0x07)
     */
    public static SSTPPacket saveCalibration() {
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, new byte[]{(byte) 0x07});
    }

    /**
     * Settings: Exit Calibration (0x09 0x07)
     */
    public static SSTPPacket exitCalibration() {
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, new byte[]{(byte) 0x08});
    }

    /**
     * Settings: Reset Calibration (0x09 0x09)
     */
    public static SSTPPacket resetCalibration() {
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, new byte[]{(byte) 0x09});
    }

    /**
     * Settings: Get Laser Focus (0x09 0x0a)
     */
    public static SSTPPacket getLaserFocalLength() {
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, new byte[]{(byte) 0x0a});
    }

    /**
     * Settings: Set Laser Focus (0x09 0x0b)
     */
    public static SSTPPacket setLaserFocalLength(float focalHeight) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x0b);
        buffer.writeInt((int) (focalHeight * 1000));
        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, content);
    }

    /**
     * Setting: Start Laser Focus (0x09 0x0c)
     */
    public static SSTPPacket startLaserFocusSetting(float xPos, float yPos, float zPos) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x0c);
        buffer.writeInt((int) (xPos * 1000));
        buffer.writeInt((int) (yPos * 1000));
        buffer.writeInt((int) (zPos * 1000));
        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, content);
    }

    /**
     * Setting: Start Laser Fine Tune (0x09 0x0d)
     */
    public static SSTPPacket startLaserFineTune() {
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, new byte[]{(byte) 0x0d});
    }

    public static SSTPPacket startLaserFineTune(float zOffset) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x0d);
        buffer.writeInt((int) (zOffset * 1000));
        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, content);
    }

    // 0x09 0x0e
    public static SSTPPacket fastCalibration() {
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, new byte[]{(byte) 0x0e});
    }

    // 0x09 0x0f
    public static SSTPPacket requestAdjustSettings(byte type, float value) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x0f);
        buffer.writeByte(type);
        buffer.writeInt((int) (value * 1000));
        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, content);
    }

    public static SSTPPacket getAdjustSettings(byte type) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x10);
        buffer.writeByte(type);
        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, content);
    }

    /**
     * Set auto focus assist light on/off(0x09 0x11)
     *
     * @param state 0:off 1:on
     */
    public static SSTPPacket setAFAssistLightState(byte state) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x11);
        buffer.writeByte(state);
        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, content);
    }

    public static SSTPPacket getMachineSize() {
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, new byte[]{(byte) 0x14});
    }

    /**
     * Setting: Check if Calibration has ever succeeded(0x09 0x15)
     */
    public static SSTPPacket checkCalibrationEverSucceeded() {
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, new byte[]{(byte) 0x15});
    }

    /**
     * Movement: G28 Z (0x0b 0x01)
     */
    public static SSTPPacket gotoZHome() {
        return buildPacket(SSTPPacket.MOVEMENT_REQUEST_EVENT_ID, new byte[]{(byte) 0x01});
    }

    /**
     * Movement: Absolute axis movement
     */
    public static SSTPPacket gotoAbsolutePosition(float x, float y, float z) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x02);
        buffer.writeInt((int) (x * 1000));
        buffer.writeInt((int) (y * 1000));
        buffer.writeInt((int) (z * 1000));
        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.MOVEMENT_REQUEST_EVENT_ID, content);
    }

    public static SSTPPacket gotoAbsolutePosition(float x, float y, float z, float f) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x02);
        buffer.writeInt((int) (x * 1000));
        buffer.writeInt((int) (y * 1000));
        buffer.writeInt((int) (z * 1000));
        // FeedRate use mm per second by hsl
        buffer.writeInt((int) (f / 60 * 1000));
        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.MOVEMENT_REQUEST_EVENT_ID, content);
    }

    /**
     * Movement: Relative axis movement
     */
    public static SSTPPacket gotoRelativePosition(float x, float y, float z) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x03);
        buffer.writeInt((int) (x * 1000));
        buffer.writeInt((int) (y * 1000));
        buffer.writeInt((int) (z * 1000));
        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.MOVEMENT_REQUEST_EVENT_ID, content);
    }

    public static SSTPPacket gotoRelativePosition(float x, float y, float z, float f) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x03);
        buffer.writeInt((int) (x * 1000));
        buffer.writeInt((int) (y * 1000));
        buffer.writeInt((int) (z * 1000));
        // FeedRate use mm per second by hsl
        buffer.writeInt((int) (f / 60 * 1000));
        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.MOVEMENT_REQUEST_EVENT_ID, content);
    }

    /**
     * Movement: Request Extrusion (0x0b 0x04)
     */
    public static SSTPPacket requestExtrusion(int type, float lengthIn, float speedIn, float lengthOut, float speedOut) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x04);
        buffer.writeByte((byte) type);
        buffer.writeInt((int) (lengthIn * 1000));
        buffer.writeInt((int) (speedIn * 1000));
        buffer.writeInt((int) (lengthOut * 1000));
        buffer.writeInt((int) (speedOut * 1000));
        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.MOVEMENT_REQUEST_EVENT_ID, content);
    }

    /**
     * Laser Camera operation: Set Camera Wi-Fi (0x0d 0x01)
     *
     * @param ssid     SSID of Wi-Fi to connect
     * @param password Password of SSID
     */
    public static SSTPPacket setupLaserNetwork(String ssid, String password) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x01);
        if (ssid != null) {
            buffer.write(ssid.getBytes());
        }
        buffer.writeByte(0x00);
        if (password != null) {
            buffer.write(password.getBytes());
        }
        buffer.writeByte(0x00);

        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.LASER_CAMERA_OPERATION_REQUEST_EVENT_ID, content);
    }

    /**
     * Laser Camera operation: Get Laser Status (0x0d 0x02)
     */
    public static SSTPPacket getLaserStatus() {
        return buildPacket(SSTPPacket.LASER_CAMERA_OPERATION_REQUEST_EVENT_ID, new byte[]{(byte) 0x02});
    }


    /**
     * Laser Camera Operation: Get laser bluetooth status and mac address
     */
    public static SSTPPacket getLaserBtStatus() {
        return buildPacket(SSTPPacket.LASER_CAMERA_OPERATION_REQUEST_EVENT_ID, new byte[]{(byte) 0x07});
    }

    /**
     * Add-on Operation: Get Enclosure status
     */
    public static SSTPPacket getEnclosureStatus() {
        return buildPacket(SSTPPacket.ADD_ON_OPERATION_REQUEST_EVENT_ID, new byte[]{(byte) 0x01});
    }

    /**
     * Add-on Operation: Set enclosure led value
     */
    public static SSTPPacket setEnclosureLed(int value) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x02);
        buffer.writeByte((byte) value);
        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.ADD_ON_OPERATION_REQUEST_EVENT_ID, content);
    }

    /**
     * Add-on Operation: Set enclosure fan value
     */
    public static SSTPPacket setEnclosureFan(int value) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x03);
        buffer.writeByte((byte) value);
        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.ADD_ON_OPERATION_REQUEST_EVENT_ID, content);
    }

    /**
     * Add-on Operation: Set enclosure door detection
     */
    public static SSTPPacket setEnclosureDoorDetection(boolean enabled) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x04);
        buffer.writeByte(enabled ? 0x01 : 0x00);
        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.ADD_ON_OPERATION_REQUEST_EVENT_ID, content);
    }

    public static SSTPPacket requestRotaryModuleStatus() {
        return buildPacket(SSTPPacket.ADD_ON_OPERATION_REQUEST_EVENT_ID, new byte[]{(byte) 0x08});
    }

    public static SSTPPacket requestEmergencyStopStatus() {
        return buildPacket(SSTPPacket.ADD_ON_OPERATION_REQUEST_EVENT_ID, new byte[]{(byte) 0x07});
    }

    public static SSTPPacket requestAirPurifierAddOnStatus() {
        return buildPacket(SSTPPacket.ADD_ON_OPERATION_REQUEST_EVENT_ID, new byte[]{(byte) 0x09});
    }

    public static SSTPPacket requestAirPurifierFanStatus() {
        return buildPacket(SSTPPacket.ADD_ON_OPERATION_REQUEST_EVENT_ID, new byte[]{(byte) 0x0A});
    }

    public static SSTPPacket setAirPurifierFanEnabled(boolean enabled) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x0B);
        buffer.writeByte(enabled ? 0x01 : 0x00);
        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.ADD_ON_OPERATION_REQUEST_EVENT_ID, content);
    }

    public static SSTPPacket setAirPurifierFanSpeed(int level) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x0C);
        buffer.writeByte((byte) level);
        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.ADD_ON_OPERATION_REQUEST_EVENT_ID, content);
    }

    public static SSTPPacket requestHeaderSecurityStatus() {
        return buildPacket(SSTPPacket.STATUS_SYNC_REQUEST_EVENT_ID, new byte[]{(byte) 0x11});
    }

    public static SSTPPacket requestAirPurifierFilterLifeTime() {
        return buildPacket(SSTPPacket.ADD_ON_OPERATION_REQUEST_EVENT_ID, new byte[]{(byte) 0x0D});
    }

    /**
     * Update (0xa9 0x03)
     * <p>
     * TODO: re-consider the protocol
     */

    public static SSTPPacket startUpdate() {
        return buildPacket(SSTPPacket.UPDATE_REQUEST_EVENT_ID, new byte[]{(byte) 0x00});
    }

    public static SSTPPacket requestUpdatePackage() {
        return buildPacket(SSTPPacket.UPDATE_REQUEST_EVENT_ID, new byte[]{(byte) 0x01});
    }

    public static SSTPPacket sendUpdatePackage(byte opCode, short index, byte[] updatePackage) {
        Buffer buffer = new Buffer();
        buffer.writeByte(opCode);
        if (opCode == 0x01) {
            buffer.writeShort((int) index);
            buffer.write(updatePackage);
        }

        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.UPDATE_REQUEST_EVENT_ID, content);
    }

    public static SSTPPacket checkControllerVersion() {
        return buildPacket(SSTPPacket.UPDATE_REQUEST_EVENT_ID, new byte[]{(byte) 0x03});
    }

    public static SSTPPacket requestModuleVersion() {
        return buildPacket(SSTPPacket.UPDATE_REQUEST_EVENT_ID, new byte[]{(byte) 0x07});
    }

    /**
     * request header online sync id (0x09 0x12)
     */
    public static SSTPPacket requestHeaderOnlineSyncId() {
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, new byte[]{(byte) 0x12});
    }

    public static SSTPPacket setHeaderOnlineSyncId(int headerOnlineSyncId) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x13);
        buffer.writeInt(headerOnlineSyncId);
        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, content);
    }

    public static SSTPPacket setAbnormalTemperatureRange(int protectTemperature, int recoveryTemperature) {
        Buffer buffer = new Buffer();
        buffer.writeByte(0x16);
        buffer.writeByte(protectTemperature);
        buffer.writeByte(recoveryTemperature);
        byte[] content = buffer.readByteArray();
        return buildPacket(SSTPPacket.SETTINGS_REQUEST_EVENT_ID, content);
    }

    // 0xa1 0x01
    public static SSTPPacket getMachineType() {
        return buildPacket(SSTPPacket.MOCK_REQUEST_EVENT_ID, new byte[]{(byte) 0x01});
    }

    public static SSTPPacket requestDualExtruderName() {
        return buildPacket(SSTPPacket.STATUS_SYNC_REQUEST_EVENT_ID, new byte[]{(byte) 0x13});
    }

}

