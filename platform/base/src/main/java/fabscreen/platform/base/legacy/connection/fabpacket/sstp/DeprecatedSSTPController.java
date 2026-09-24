package fabscreen.platform.base.legacy.connection.fabpacket.sstp;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.Constants;
import fabscreen.platform.base.FabException;
import fabscreen.platform.base.ModuleVersion;
import fabscreen.platform.base.legacy.DeprecatedMachineController;
import fabscreen.platform.base.legacy.ISlaveComputer;
import fabscreen.platform.base.legacy.connection.IPacket;
import fabscreen.platform.base.legacy.connection.ISerialConnection;
import fabscreen.platform.base.legacy.connection.RequestReceiver;
import fabscreen.platform.base.legacy.connection.SSTPPacket;
import fabscreen.platform.base.legacy.connection.SSTPPacketContent;
import fabscreen.platform.base.model.ILaserCameraController;
import fabscreen.platform.base.model.system.DeprecatedMachineInfo;
import fabscreen.platform.base.model.system.MachineStatusManager;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.lib.SubjectHolder;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import okio.Buffer;
import okio.ByteString;

@Deprecated
public class DeprecatedSSTPController implements ISlaveComputer {
    private static final String TAG = "SerialController";
    private final ISerialConnection mConnection;
    private Context mContext;
    private CompositeDisposable mDisposables = new CompositeDisposable();
    // FabPacket receivers to handle responses
    private SparseArray<RequestReceiver> mReceivers = new SparseArray<>();
    // connection
    private long mLastReceiveTimeMillis = 0;
    private Disposable mPollingSubscription = null;
    private Disposable mHeartbeatSubscription = null;
    private SubjectHolder<DeprecatedMachineInfo> mMachineStatusSubject = MachineStatusManager.getMachineInfoHolder();
    private BehaviorSubject<SSTPPacketContent.DualExtruderName> mDualExtruderNameSubject = BehaviorSubject.createDefault(SSTPPacketContent.DualExtruderName.getDefaultInstance());
    // data - print
//    private Subject<String> mPrintGcodeResponseSubject = PublishSubject.create();
    // data - G-code response content
    private SSTPPacketContent.GcodeResponse mGcodeResponse = SSTPPacketContent.GcodeResponse.EMPTY_GCODE_RESPONSE;
    // data - settings
    private Subject<Integer> mAutoCalibrationProgressSubject = PublishSubject.create();
    private PublishSubject<SSTPPacketContent.HeaderSecurity> mHeaderSecuritySubject = PublishSubject.create();
    private PublishSubject<Integer> mPausePrintSubject = PublishSubject.create();
    // data - batch G-code response content
    private PublishSubject<SSTPPacketContent.BatchGcodeResponse> mBatchGcodeResponseSubject = PublishSubject.create();
    // data - Listen for master status updates
    private PublishSubject<SSTPPacketContent.MasterState> mMasterStateSubject = PublishSubject.create();
    private DeprecatedMachineController mDeprecatedMachineController;
    private byte mLaser10WErrorState;

    public DeprecatedSSTPController() {
        mConnection = ISerialConnection.getInstance(ISerialConnection.SSTP);
        mConnection.setConnectionListener(this::onConnection);
        mConnection.setSerialDataListener(this::onReceive);
    }

    @Override
    public Observable<SSTPPacketContent.BatchGcodeResponse> getBatchGcodeResponseSubject() {
        return mBatchGcodeResponseSubject.hide();
    }

    @Override
    public Observable<SSTPPacketContent.MasterState> getMasterState() {
        return mMasterStateSubject.hide();
    }

    public void connect() {
        mConnection.connect();
    }

    private void disconnect() {
        mConnection.disconnect();

        mDisposables.clear();

        mReceivers.clear();
    }

    public void setHeartbeatEnabled(boolean enabled) {
        if (enabled) {
            Logger.i("Enable heartbeat.");
            mLastReceiveTimeMillis = SystemClock.elapsedRealtime();

            // Polling machine status every few seconds
            if (mPollingSubscription != null) { // in case onConnected being called twice in a row
                mPollingSubscription.dispose();
            }
            mPollingSubscription = Observable.interval(Constants.POLLING_INTERVAL, TimeUnit.MILLISECONDS)
                    .flatMap(t -> requestMachineStatus()
                            .onErrorReturnItem(DeprecatedMachineInfo.getDefaultInstance()))
                    .subscribe(machineStatus -> {
                        if (!machineStatus.isDefault) {
                            // FIXME:
//                            mMachineStatusSubject.onNext(machineStatus);
                        } else {
                            Logger.w("Not getting machine status, replace with default status.");
                        }
                    }, e -> {
                        Log.e(TAG, "Failed to request machine status.");
                        e.printStackTrace();
                    });

            // Check response interval
            if (mHeartbeatSubscription != null) {
                mHeartbeatSubscription.dispose();
            }
            mHeartbeatSubscription = Observable.interval(Constants.HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS)
                    .subscribe(t -> {
                        long time = SystemClock.elapsedRealtime();
                        if (time - mLastReceiveTimeMillis > Constants.HEARTBEAT_INTERVAL) {
                            disconnect();
                        }
                    });
        } else {
            Logger.i("Disable heartbeat.");
            if (mPollingSubscription != null) {
                mPollingSubscription.dispose();
                mPollingSubscription = null;
            }
            if (mHeartbeatSubscription != null) {
                mHeartbeatSubscription.dispose();
                mHeartbeatSubscription = null;
            }
        }
    }

    private Observable<Object> watch(SSTPPacket packet) {
        final int key = packet.getKey();

        return Observable.create(emitter -> {
            if (!MachineStatusManager.getConnectedStatus().getValue()) {
                emitter.onError(new IOException("Serial port service is not connected."));
                return;
            }

            if (!MachineStatusManager.getConnectedStatus().getValue()) {
                emitter.onError(new IOException("Serial port is not connected."));
                return;
            }

            RequestReceiver receiver = mReceivers.get(key);
            if (receiver == null) {
                receiver = new RequestReceiver(key);
                mReceivers.put(packet.getKey(), receiver);
            }

            receiver.setDefaultEmitter(emitter);
        });
    }

    private Observable<Object> request(SSTPPacket packet, int timeout, Observable<Object> observable) {
        return request(packet).timeout(timeout, TimeUnit.MILLISECONDS, observable);
    }

    private Observable<Object> request(SSTPPacket packet, int timeout) {
        return request(packet).timeout(timeout, TimeUnit.MILLISECONDS);
    }

    private Observable<Object> request(SSTPPacket packet) {
        final int key = packet.getKey();

        return Observable.create(emitter -> {
            if (emitter.isDisposed()) {
                Logger.d("Emitter is disposed, skip sending request.");
                return;
            }

            if (!MachineStatusManager.getConnectedStatus().getValue()) {
                emitter.onError(new FabException("Serial port is not connected."));
                return;
            }

            RequestReceiver receiver = mReceivers.get(key);
            if (receiver == null) {
                receiver = new RequestReceiver(key);
                mReceivers.put(key, receiver);
            }
            receiver.addEmitter(emitter);

            // Check send 0x09 0x14
            if (packet.getEventId() == SSTPPacket.SETTINGS_REQUEST_EVENT_ID && Arrays.equals(packet.getContent(), new byte[]{(byte) 0x14})) {
                Logger.d("Request 0x09 0x14 ");
            }
            send(packet);
        });
    }

    /**
     * Send SSTPPacket to serial port.
     *
     * @param packet packet to be sent.
     */
    @Override
    public void send(SSTPPacket packet) {
        mConnection.send(packet);
    }

    @Override
    public Observable<SSTPPacketContent.GcodeResponse> sendGcode(String gcode) {
        return request(SSTPPacketBuilder.gcodeRequest(gcode, 0))
                .map(o -> (SSTPPacketContent.GcodeResponse) o);
    }

    @Override
    public Observable<SSTPPacketContent.GcodeResponse> sendPrintGcode(String gcode, int lineno) {
        return request(SSTPPacketBuilder.printGcodeRequest(gcode, lineno))
                .map(o -> (SSTPPacketContent.GcodeResponse) o);
    }

    @Override
    public void sendPrintBatchGcode(int startLine, int endLine, String gcode) {
        send(SSTPPacketBuilder.printBatchGcodeRequest(startLine, endLine, gcode));
    }

    public Observable<SSTPPacketContent.GcodeResponse> sendGcode(String gcode, boolean replyContent) {
        SSTPPacket packet = SSTPPacketBuilder.gcodeRequest(gcode, 0);
        byte[] bytes = packet.toByteArray();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        Logger.d("G-code Request Hex: %s", sb.toString());
        return request(SSTPPacketBuilder.gcodeRequest(gcode, 0))
                .flatMap(o1 -> watch(SSTPPacketBuilder.extendGcodeRequest())
                        .map(o2 -> (SSTPPacketContent.GcodeResponse) o2));
    }


    /**
     * Status Request: Get Machine Status. (0x07 0x01)
     */
    @Override
    public Observable<DeprecatedMachineInfo> requestMachineStatus() {
        SSTPPacket packet = SSTPPacketBuilder.statusRequestMachineStatus();
        return request(packet, 1000).map(o -> (DeprecatedMachineInfo) o);
    }

    /**
     * Status Request: Get Abnormal Status. (0x07 0x02)
     */
    @Override
    public Observable<SSTPPacketContent.MachineErrors> getMachineErrors() {
        SSTPPacket packet = SSTPPacketBuilder.statusRequestMachineAbnormalStatus();
        return request(packet).map(o -> (SSTPPacketContent.MachineErrors) o);
    }

    @Override
    public Observable<SSTPPacketContent.MachineErrors> getMachineErrors(int timeout) {
        SSTPPacket packet = SSTPPacketBuilder.statusRequestMachineAbnormalStatus();
        return request(packet, timeout).map(o -> (SSTPPacketContent.MachineErrors) o);
    }

    @Override
    public Observable<SSTPPacketContent.MachineErrors> watchMachineErrors() {
        SSTPPacket packet = SSTPPacketBuilder.statusRequestMachineAbnormalStatus();
        return watch(packet).map(o -> (SSTPPacketContent.MachineErrors) o);
    }

    @Override
    public Observable<Boolean> watchWaitEvents() {
        SSTPPacket packet = SSTPPacketBuilder.statusWaitRequest();
        return watch(packet).map(o -> true);
    }

    /**
     * Status request: start online print. (0x07 0x03)
     */
    @Override
    public Observable<Integer> start() {
        SSTPPacket packet = SSTPPacketBuilder.statusRequestMachineStartPrint();
        return request(packet).map(o -> (Integer) o);
    }

    @Override
    public Observable<Integer> useBatchGcodeMode(int check) {
        SSTPPacket packet = SSTPPacketBuilder.setgcodeBatchSending(check);
        return request(packet, 500).map(o -> (Integer) o);
    }

    /**
     * Status request: pause print. (0x07 0x04)
     */
    @Override
    public Observable<Integer> pause() {
        SSTPPacket packet = SSTPPacketBuilder.statusRequestMachinePausePrint();
        return request(packet).map(o -> (Integer) o);
    }

    /**
     * Status request: resume print. (0x07 0x05)
     */
    @Override
    public Observable<Integer> resume() {
        SSTPPacket packet = SSTPPacketBuilder.statusRequestMachineResumePrint();
        return request(packet).map(o -> (Integer) o);
    }

    /**
     * Status request: stop print. (0x07 0x06)
     */
    @Override
    public Observable<Integer> stop() {
        SSTPPacket packet = SSTPPacketBuilder.statusRequestMachineStopPrint();
        return request(packet).map(o -> (Integer) o);
    }

    /**
     * Status request: finish print. (0x07 0x07)
     */
    @Override
    public Observable<Integer> finish() {
        SSTPPacket packet = SSTPPacketBuilder.statusRequestMachineFinishPrint();
        return request(packet).map(o -> (Integer) o);
    }

    /**
     * Status request: Get line number. (0x07 0x08)
     */
    @Override
    public Observable<Integer> getLineNumber() {
        SSTPPacket packet = SSTPPacketBuilder.statusRequestLineNumber();
        return request(packet).map(o -> (Integer) o);
    }

    @Override
    public Observable<Integer> resetErrorFlag() {
        SSTPPacket packet = SSTPPacketBuilder.statusRequestResetErrorFlag();
        return request(packet).map(o -> (Integer) o);
    }

    /**
     * Status request: Resume print. (0x07 0x0b)
     */
    @Override
    public Observable<Integer> resumeFromPowerOutage() {
        SSTPPacket packet = SSTPPacketBuilder.statusRequestResumePrint();
        return request(packet).map(o -> (Integer) o);
    }

    @Override
    public Observable<SSTPPacketContent.CoordinateSystem> requestCoordinateSystem() {
        SSTPPacket packet = SSTPPacketBuilder.statusRequestCoodinateSystem();
        return request(packet).map(o -> (SSTPPacketContent.CoordinateSystem) o);
    }

    @Override
    public Observable<SSTPPacketContent.CoordinateSystem> requestCoordinateSystem(int timeout) {
        SSTPPacket packet = SSTPPacketBuilder.statusRequestCoodinateSystem();
        return request(packet, timeout).map(o -> (SSTPPacketContent.CoordinateSystem) o);
    }

    /**
     * Settings: Set Workspace.
     */
    @Override
    public Observable<Boolean> setWorkspace(int xSize, int xHomeOffset, int xMaxDir, int xStepperDir,
                                            int ySize, int yHomeOffset, int yMaxDir, int yStepperDir,
                                            int zSize, int zHomeOffset, int zMaxDir, int zStepperDir) {
        SSTPPacket packet = SSTPPacketBuilder.setWorkspace(
                xSize, xHomeOffset, xMaxDir, xStepperDir,
                ySize, yHomeOffset, yMaxDir, yStepperDir,
                zSize, zHomeOffset, zMaxDir, zStepperDir);

        // time out up to 10 second
        return request(packet, 10 * 1000).map(o -> (Boolean) o);
    }

    /**
     * Settings: Start Auto Calibration (0x09 0x02).
     */
    @Override
    public Observable<Boolean> startAutoCalibration() {
        SSTPPacket packet = SSTPPacketBuilder.startAutoCalibration();
        return request(packet).map(o -> (Boolean) o);
    }

    @Override
    public Observable<Boolean> startAutoCalibration(int grid) {
        SSTPPacket packet = SSTPPacketBuilder.startAutoCalibration(grid);
        return request(packet).map(o -> (Boolean) o);
    }

    @Override
    public Observable<Integer> getAutoCalibrationProgress() {
        return mAutoCalibrationProgressSubject;
    }

    /**
     * Settings: Start Manual Calibration. (0x09 0x04)
     */
    @Override
    public Observable<Boolean> startManualCalibration() {
        SSTPPacket packet = SSTPPacketBuilder.startManualCalibration();
        return request(packet).map(o -> (Boolean) o);
    }

    @Override
    public Observable<Boolean> startManualCalibration(int grid) {
        SSTPPacket packet = SSTPPacketBuilder.startManualCalibration(grid);
        return request(packet).map(o -> (Boolean) o);
    }

    /**
     * Settings: Goto Manual Calibration Point. (0x09 0x05)
     */
    @Override
    public Observable<Boolean> gotoCalibrationPoint(int point) {
        SSTPPacket packet = SSTPPacketBuilder.gotoCalibrationPoint(point);
        return request(packet).map(o -> (Boolean) o);
    }

    /**
     * Settings: Move Manual Calibration Point. (0x09 0x06)
     */
    @Override
    public Observable<Boolean> moveCalibrationPoint(double offset) {
        SSTPPacket packet = SSTPPacketBuilder.moveCalibrationPoint(offset);
        return request(packet).map(o -> (Boolean) o);
    }

    // 0x09 0x07
    @Override
    public Observable<Boolean> saveCalibration() {
        SSTPPacket packet = SSTPPacketBuilder.saveCalibration();
        return request(packet).map(o -> (Boolean) o);
    }

    /**
     * Settings: Exit Calibration. (0x09 0x08)
     */
    @Override
    public Observable<Boolean> exitCalibration() {
        SSTPPacket packet = SSTPPacketBuilder.exitCalibration();
        return request(packet).map(o -> (Boolean) o);
    }

    /**
     * Settings: Reset Calibration. (0x09 0x09)
     */
    @Override
    public Observable<Boolean> resetCalibration() {
        SSTPPacket packet = SSTPPacketBuilder.resetCalibration();
        return request(packet).map(o -> (Boolean) o);
    }

    /**
     * Settings: Get Laser Focal Length (0x09 0x0a)
     */
    @Override
    public Observable<Float> getLaserFocalLength() {
        SSTPPacket packet = SSTPPacketBuilder.getLaserFocalLength();
        return request(packet).map(o -> (Float) o);
    }

    @Override
    public Observable<Float> getLaserFocalLength(int timeout) {
        SSTPPacket packet = SSTPPacketBuilder.getLaserFocalLength();
        return request(packet, timeout).map(o -> (Float) o);
    }

    /**
     * Settings: Set Laser Focus (0x09 0x0b)
     */
    @Override
    public Observable<Boolean> setLaserFocalLength(float focalLength) {
        SSTPPacket packet = SSTPPacketBuilder.setLaserFocalLength(focalLength);
        return request(packet).map(o -> (Boolean) o);
    }

    /**
     * Setting: Start Laser Focus (0x09 0x0c)
     */
    @Override
    public Observable<Boolean> startLaserFocusSetting(float xPosition, float yPosition, float zPosition) {
        SSTPPacket packet = SSTPPacketBuilder.startLaserFocusSetting(xPosition, yPosition, zPosition);
        return request(packet).map(o -> (Boolean) o);
    }

    /**
     * Setting: Start Laser Fine Tune (0x09 0x0d)
     */
    @Override
    public Observable<Boolean> startLaserFineTune() {
        SSTPPacket packet = SSTPPacketBuilder.startLaserFineTune();
        return request(packet).map(o -> (Boolean) o);
    }

    @Override
    public Observable<Boolean> startLaserFineTune(float zOffset) {
        SSTPPacket packet = SSTPPacketBuilder.startLaserFineTune(zOffset);
        return request(packet).map(o -> (Boolean) o);
    }

    // 0x09 0x0e
    @Override
    public Observable<Integer> fastCalibration() {
        SSTPPacket packet = SSTPPacketBuilder.fastCalibration();
        return request(packet).map(o -> (Integer) o);
    }

    // 0x09 0x0f
    @Override
    public Observable<Integer> requestAdjustSetting(int type, float value) {
        SSTPPacket packet = SSTPPacketBuilder.requestAdjustSettings((byte) type, value);
        return request(packet).map(o -> (Integer) o);
    }

    // 0xa1 0x01
    @Override
    public Observable<Integer> getMachineType() {
        SSTPPacket packet = SSTPPacketBuilder.getMachineType();
        return request(packet).map(o -> (Integer) o);
    }

    @Override
    public Observable<Integer> requestAdjustSettingFeedRate(float value) {
        return requestAdjustSetting(0, value);
    }

    @Override
    public Observable<Integer> requestAdjustSettingNozzleTemp(float value) {
        return requestAdjustSetting(1, value);
    }

    @Override
    public Observable<Integer> requestAdjustSettingHeatedBedTemp(float value) {
        return requestAdjustSetting(2, value);
    }

    @Override
    public Observable<Integer> requestAdjustSettingLaserPower(float value) {
        return requestAdjustSetting(3, value);
    }

    @Override
    public Observable<Integer> requestAdjustSettingZOffset(float value) {
        return requestAdjustSetting(4, value);
    }

    @Override
    public Observable<Integer> requestAdjustSettingCNCPower(float value) {
        return requestAdjustSetting(5, value);
    }

    @Override
    public Observable<SSTPPacketContent.AdjustSettings> getAdjustSetting(int type) {
        SSTPPacket packet = SSTPPacketBuilder.getAdjustSettings((byte) type);
        return request(packet).map(o -> (SSTPPacketContent.AdjustSettings) o);
    }

    @Override
    public Observable<Boolean> setAFAssistLightState(int state) {
        SSTPPacket packet = SSTPPacketBuilder.setAFAssistLightState((byte) state);
        return request(packet).map(o -> (byte) o == 0);
    }

    @Override
    public Observable<SSTPPacketContent.AdjustSettings> getAdjustSettingFeedRate() {
        return getAdjustSetting(0);
    }

    @Override
    public Observable<SSTPPacketContent.AdjustSettings> getAdjustSettingLaserPower() {
        return getAdjustSetting(1);
    }

    @Override
    public Observable<SSTPPacketContent.AdjustSettings> getAdjustSettingZOffset() {
        return getAdjustSetting(4);
    }

    @Override
    public Observable<SSTPPacketContent.AdjustSettings> getAdjustSettingCNCPower() {
        return getAdjustSetting(5);
    }

    @Override
    public Observable<SSTPPacketContent.MachineSize> getMachineSize() {
        SSTPPacket packet = SSTPPacketBuilder.getMachineSize();
        return request(packet, 500).map(o -> (SSTPPacketContent.MachineSize) o);
    }

    @Override
    public Observable<Boolean> checkCalibrationEverSucceeded() {
        SSTPPacket packet = SSTPPacketBuilder.checkCalibrationEverSucceeded();
        return request(packet).map(o -> (Boolean) o);
    }

    /**
     * Movement: G28 Z (0x0b 0x01)
     */
    @Override
    public Observable<Boolean> gotoZHome() {
        SSTPPacket packet = SSTPPacketBuilder.gotoZHome();
        return request(packet).map(o -> (Boolean) o);
    }

    @Override
    public Observable<Boolean> setPosition(float x, float y, float z, int flag) {
        return setPosition(x, y, z, 0, flag);
    }

    @Override
    public Observable<Boolean> setPosition(float x, float y, float z, float b, int flag) {
        String cmd = "G92";
        if ((flag & ISlaveComputer.FLAG_X) > 0) {
            cmd += String.format(Locale.US, " X%.2f", x);
        }
        if ((flag & ISlaveComputer.FLAG_Y) > 0) {
            cmd += String.format(Locale.US, " Y%.2f", y);
        }
        if ((flag & ISlaveComputer.FLAG_Z) > 0) {
            cmd += String.format(Locale.US, " Z%.2f", z);
        }
        if ((flag & ISlaveComputer.FLAG_B) > 0) {
            cmd += String.format(Locale.US, " B%.2f", b);
        }

        SSTPPacket packet = SSTPPacketBuilder.gcodeRequest(cmd, 0);
        return request(packet).map(o -> true).delay(2500, TimeUnit.MILLISECONDS);
    }

    /**
     * Movement: Absolute axis movement (0x0b 0x02)
     */
    @Override
    public Observable<Boolean> gotoAbsolutePosition(float x, float y, float z) {
        SSTPPacket packet = SSTPPacketBuilder.gotoAbsolutePosition(x, y, z);
        return request(packet).map(o -> (Boolean) o);
    }

    @Override
    public Observable<Boolean> gotoAbsolutePosition(float x, float y, float z, float f) {
        SSTPPacket packet = SSTPPacketBuilder.gotoAbsolutePosition(x, y, z, f);
        return request(packet).map(o -> (Boolean) o);
    }

    /**
     * Movement: Relative axis movement (0x0b 0x03)
     */
    public Observable<Boolean> gotoRelativePosition(float x, float y, float z) {
        SSTPPacket packet = SSTPPacketBuilder.gotoRelativePosition(x, y, z);
        return request(packet).map(o -> (Boolean) o);
    }

    public Observable<Boolean> gotoRelativePosition(float x, float y, float z, float f) {
        SSTPPacket packet = SSTPPacketBuilder.gotoRelativePosition(x, y, z, f);
        return request(packet).map(o -> (Boolean) o);
    }

    /**
     * Movement: Request Extrusion (0x0b 0x04)
     */
    @Override
    public Observable<Boolean> requestExtrusion(int type, float lengthIn, float speedIn, float lengthOut, float speedOut) {
        SSTPPacket packet = SSTPPacketBuilder.requestExtrusion(type, lengthIn, speedIn, lengthOut, speedOut);
        return request(packet).map(o -> (Boolean) o);
    }

    /**
     * Add-on: Get enclosure status (0x11 0x01)
     */
    public Observable<SSTPPacketContent.EnclosureStatus> getEnclosureStatus() {
        SSTPPacket packet = SSTPPacketBuilder.getEnclosureStatus();
        Logger.d("requesting enclosure status...event id is %s", ByteString.of(packet.getEventId()).hex());
        return request(packet).map(o -> (SSTPPacketContent.EnclosureStatus) o);
    }

    /**
     * Add-on: Set enclosure led (0x11 0x02)
     *
     * @param value led value (0 - 100)
     */
    public Observable<Boolean> setEnclosureLed(int value) {
        SSTPPacket packet = SSTPPacketBuilder.setEnclosureLed(value);
        return request(packet).map(o -> (Boolean) o);
    }

    /**
     * Add-on: Set enclosure fan on/off (0x11 0x03)
     *
     * @param value fan value (0 - 100)
     */
    public Observable<Boolean> setEnclosureFan(int value) {
        SSTPPacket packet = SSTPPacketBuilder.setEnclosureFan(value);
        return request(packet).map(o -> (Boolean) o);
    }

    /**
     * Add-on: Set enclosure door detection (0x11 0x04)
     */
    public Observable<Boolean> setEnclosureDoorDetection(boolean enabled) {
        SSTPPacket packet = SSTPPacketBuilder.setEnclosureDoorDetection(enabled);
        return request(packet).map(o -> (Boolean) o);
    }

    /**
     * Add-on: request rotary module status (0x11 0x08)
     */
    public Observable<Byte> requestRotaryModuleStatus() {
        SSTPPacket packet = SSTPPacketBuilder.requestRotaryModuleStatus();
        return request(packet).map(o -> (Byte) o);
    }

    /**
     * Byte 0 emergency stop connected & idle
     * 1 emergency stop not connected or not available
     * 2 emergency stop triggered
     **/
    public Observable<Byte> watchEmergencyStopStatus() {
        SSTPPacket packet = SSTPPacketBuilder.requestEmergencyStopStatus();
        return watch(packet).map(o -> (Byte) o);
    }

    public Observable<Byte> requestEmergencyStopStatus() {
        SSTPPacket packet = SSTPPacketBuilder.requestEmergencyStopStatus();
        return request(packet).map(o -> (Byte) o);
    }

    @Override
    public Observable<SSTPPacketContent.AirPurifierStatus> requestAirPurifierAddOnStatus() {
        SSTPPacket packet = SSTPPacketBuilder.requestAirPurifierAddOnStatus();
        return request(packet).map(o -> (SSTPPacketContent.AirPurifierStatus) o);
    }

    @Override
    public Observable<SSTPPacketContent.AirPurifierStatus> watchAirPurifierAddOnStatus() {
        SSTPPacket packet = SSTPPacketBuilder.requestAirPurifierAddOnStatus();
        return watch(packet).map(o -> (SSTPPacketContent.AirPurifierStatus) o);
    }

    @Override
    public Observable<SSTPPacketContent.AirPurifierFan> requestAirPurifierFan() {
        SSTPPacket packet = SSTPPacketBuilder.requestAirPurifierFanStatus();
        return request(packet).map(o -> (SSTPPacketContent.AirPurifierFan) o);
    }

    public Observable<SSTPPacketContent.DualExtruderName> getDualExtruderNameObservable() {
        return mDualExtruderNameSubject;
    }

    @Override
    public Observable<Boolean> moveLevelingBedCalibration(int index) {
        return Observable.just(true);
    }

    @Override
    public DeprecatedMachineController getMachineController() {
        return mDeprecatedMachineController;
    }

    @Override
    public void setMachineController(DeprecatedMachineController mc) {
        mDeprecatedMachineController = mc;
    }

    @Override
    public ILaserCameraController getLaserCameraController() {
        return null;
    }

    @Override
    public void setLaserCameraController(ILaserCameraController lc) {

    }

    @Override
    public Observable<Boolean> setAirPurifierEnabled(boolean enabled) {
        SSTPPacket packet = SSTPPacketBuilder.setAirPurifierFanEnabled(enabled);
        return request(packet).map(o -> (Boolean) o);
    }

    @Override
    public Observable<Boolean> setAirPurifierFanSpeedLevel(int level) {
        SSTPPacket packet = SSTPPacketBuilder.setAirPurifierFanSpeed(level);
        return request(packet).map(o -> (Boolean) o);
    }

    @Override
    public Observable<Integer> getAirPurifierFilterLifeTime() {
        SSTPPacket packet = SSTPPacketBuilder.requestAirPurifierFilterLifeTime();
        return request(packet).map(o -> (Integer) o);
    }

    @Override
    public Observable<Integer> watchAirPurifierFilterLifeTime() {
        SSTPPacket packet = SSTPPacketBuilder.requestAirPurifierFilterLifeTime();
        return watch(packet).map(o -> (Integer) o);
    }

    @Override
    public Observable<SSTPPacketContent.HeaderSecurity> requestHeaderSecurityStatus() {
        SSTPPacket packet = SSTPPacketBuilder.requestHeaderSecurityStatus();
        return request(packet).map(o -> (SSTPPacketContent.HeaderSecurity) o);
    }

    @Override
    public Observable<SSTPPacketContent.HeaderSecurity> watchHeaderSecurityStatus() {
        return mHeaderSecuritySubject.hide();
    }

    @Override
    public Observable<Integer> requestHeaderOnlineSyncId(int timeout) {
        SSTPPacket packet = SSTPPacketBuilder.requestHeaderOnlineSyncId();
        return request(packet, timeout).map(o -> (Integer) o);
    }

    @Override
    public Observable<Boolean> setHeaderOnlineSyncId(int headerId) {
        SSTPPacket packet = SSTPPacketBuilder.setHeaderOnlineSyncId(headerId);
        return request(packet).map(o -> (Boolean) o);
    }

    @Override
    public void setAbnormalTemperatureRange(int protectTemperature, int recoveryTemperature) {
        SSTPPacket packet = SSTPPacketBuilder.setAbnormalTemperatureRange(protectTemperature, recoveryTemperature);
        send(packet);
    }


    @Override
    public Observable<Integer> watchPrintPauseState() {
        return mPausePrintSubject.hide();
    }

    public Observable<String> getControllerVersion() {
        SSTPPacket packet = SSTPPacketBuilder.checkControllerVersion();
        return request(packet).map(o -> (String) o);
    }

    public Observable<Boolean> startUpdate() {
        SSTPPacket packet = SSTPPacketBuilder.startUpdate();
        return request(packet).map(o -> (Boolean) o);
    }

    public Observable<Short> watchPacketIndexRequest() {
        SSTPPacket packet = SSTPPacketBuilder.requestUpdatePackage();
        return watch(packet).map(o -> (Short) o);
    }

    public void sendUpdatePackage(byte opCode, short index, byte[] content) {
        SSTPPacket packet = SSTPPacketBuilder.sendUpdatePackage(opCode, index, content);
        send(packet);
    }

    public void requestModuleVersion() {
        SSTPPacket packet = SSTPPacketBuilder.requestModuleVersion();
        send(packet);
    }

    public Observable<ModuleVersion> watchModuleVersion() {
        SSTPPacket packet = SSTPPacketBuilder.requestModuleVersion();
        return watch(packet).map(o -> (ModuleVersion) o);
    }

    /**
     * Laser Camera Operation: set Camera Wi-Fi
     */
    public Observable<Boolean> setupLaserNetwork(String SSID, String password) {
        SSTPPacket packet = SSTPPacketBuilder.setupLaserNetwork(SSID, password);
        return request(packet).map(o -> (Boolean) o);
    }

    /**
     * Laser Camera Operation: Get laser status.
     */
    public Observable<SSTPPacketContent.LaserWifiStatus> getLaserWifiStatus() {
        SSTPPacket packet = SSTPPacketBuilder.getLaserStatus();
        return request(packet).map(o -> (SSTPPacketContent.LaserWifiStatus) o);
    }

    public Observable<SSTPPacketContent.LaserBtStatus> getLaserBluetoothStatus() {
        SSTPPacket packet = SSTPPacketBuilder.getLaserBtStatus();
        return request(packet).map(o -> (SSTPPacketContent.LaserBtStatus) o);
    }

    /**
     * Receive connection changes.
     *
     * @param connected whether serial port is connected
     */
    private void onConnection(boolean connected) {
        //FIXME:
//        MachineStatusManager.getConnectedStatus().onNext(connected);
//
//        Logger.i("onConnection, connected = " + connected);
//
//        if (connected) {
//            MachineStatusManager.getConnectedStatus().onNext(true);
//
//            setHeartbeatEnabled(true);
//
//            // Save connected device
////            SharedPreferences sharedPref = mContext.getSharedPreferences("com.snapmaker.fabscreen.PREFERENCE_DEFAULT", Context.MODE_PRIVATE);
////            sharedPref.edit().putString("SERIAL_PORT_PATH", mDevice).apply();
//        } else {
////            mDevice = null;
//            MachineStatusManager.getConnectedStatus().onNext(false);
//            mMachineStatusSubject.onNext(DeprecatedMachineInfo.getDefaultInstance());
//
//            setHeartbeatEnabled(false);
//        }
    }

    /**
     * Receive data from serial connection.
     * <p>
     * Note that this method is NOT called on UI thread.
     *
     * @param packet received packet
     */
    private void onReceive(IPacket packet) {
        mLastReceiveTimeMillis = SystemClock.elapsedRealtime();
        // Too many logs, comment it out unless you are debugging the heartbeat.
//        Log.i(TAG, "Receive event " + packet.getEventId() + ": " + packet + " time: " + mLastReceiveTimeMillis);
        if (!(packet.getEventId() == SSTPPacket.STATUS_RESPONSE_EVENT_ID && packet.getContent()[0] == 1)) {
            Logger.v("Packet received, %s", new ByteString(packet.toByteArray()).hex());
        }


        switch (packet.getEventId()) {
            // 0x02 G-code response
            // 0x04 File print G-code response
            case SSTPPacket.GCODE_RESPONSE_EVENT_ID:
            case SSTPPacket.PRINT_GCODE_RESPONSE_EVENT_ID: {
                SSTPPacketContent.GcodeResponse response = SSTPPacketContent.GcodeResponse.parse(packet.getContent());
                if (response != null) {
                    mGcodeResponse.setLineNo(response.getLineNo());
                    sendResponse(packet, response);
                } else {
                    Logger.e("response is null, data is not parsed properly.");
                }
                break;
            }
            case SSTPPacket.GCODE_RESPONSE_EXTEND_EVENT_ID: {
                byte operationId = packet.getContent()[0];
                switch (operationId) {
                    case 0x01:
                        Buffer buffer = new Buffer();
                        buffer.write(packet.getContent());
                        try {
                            // skip operation ID
                            buffer.readByte();
                            mGcodeResponse.mergeContent(buffer.readUtf8());
                        } catch (IOException e) {
                            Logger.e("Write G-code content failed.");
                        }
                        break;
                    case 0x02:
                        // Merge G-code response data
                        if (mGcodeResponse != null) {
                            sendResponse(packet, mGcodeResponse);
                            // erase Last Gcode Response
                            mGcodeResponse = new SSTPPacketContent.GcodeResponse();

                        }
                        break;
                }
                break;
            }


            // 0x08 Status Sync
            case SSTPPacket.STATUS_RESPONSE_EVENT_ID: {
                byte subEventId = packet.getContent()[0];
                if (packet.getContent().length == 1) break;
                switch (subEventId) {
                    case 0x01:
                        DeprecatedMachineInfo machineInfo = SSTPPacketContent.parseMachineStatus(packet.getContent());
                        if (machineInfo != null) {
                            sendResponse(packet, machineInfo);
                        }
                        break;
                    case 0x02:
                        // TODO:
                        SSTPPacketContent.MachineErrors machineErrors = SSTPPacketContent.MachineErrors.parse(packet.getContent());
                        if (machineErrors != null) {
                            RequestReceiver receiver = mReceivers.get(packet.getKey());
                            if (receiver != null) {
                                receiver.receive(machineErrors);
                            }
                        }
                        break;
                    case 0x04:
                        // Processing 04 pause
                        mPausePrintSubject.onNext((packet.getContent()[1] & 0xff));
                    case 0x03:
                    case 0x05:
                    case 0x06:
                    case 0x07:
                    case 0x0a:
                    case 0x0b:
                    case 0x0c:
                    case 0x12: {
                        RequestReceiver receiver = mReceivers.get(packet.getKey());
                        if (receiver != null) {
                            receiver.receive(packet.getContent()[1] & 0xff);
                        } else {
                            mMasterStateSubject.onNext(SSTPPacketContent.MasterState.parse(packet.getContent()));
                        }
                        break;
                    }
                    case 0x08: {
                        // TODO
                        Buffer buffer = new Buffer();
                        buffer.write(packet.getContent());
                        try {
                            buffer.readByte();
                            buffer.readByte(); // isPowerPanic
                            buffer.readByte(); // isLocal
                            final int lineno = buffer.readInt();

                            sendResponse(packet, lineno);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case 0x09: {
                        Buffer buffer = new Buffer();
                        buffer.write(packet.getContent());
                        try {
                            buffer.readByte();
                            final int progress = buffer.readInt();
                            RequestReceiver receiver = mReceivers.get(packet.getKey());
                            if (receiver != null) {
                                receiver.receive(progress);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case 0x0e: {
                        SSTPPacketContent.CoordinateSystem coordinateSystem = SSTPPacketContent.CoordinateSystem.parse(packet.getContent());
                        sendResponse(packet, coordinateSystem);
                        break;
                    }
                    case 0x0f: {
                        byte logLevel = packet.getContent()[1];
                        LogHelper.setFirmwareLogLevel(logLevel);
                        break;
                    }
                    case 0x10: {
                        Buffer buffer = new Buffer();
                        buffer.write(packet.getContent());

                        try {
                            buffer.readByte();
                            int level = buffer.readByte();
                            buffer.readByte();
                            String msg = buffer.readString(StandardCharsets.US_ASCII).trim();
                            LogHelper.firmwareLog(level, msg);
                        } catch (IOException e) { /* */ }
                        break;
                    }
                    case 0x11: {
                        RequestReceiver receiver = mReceivers.get(packet.getKey());
                        SSTPPacketContent.HeaderSecurity headerSecurity = SSTPPacketContent.HeaderSecurity.parse(packet.getContent());
                        if (receiver != null) {
                            receiver.receive(headerSecurity);
                        }
                        mHeaderSecuritySubject.onNext(headerSecurity);
                        break;
                    }
                    case 0x13: {
                        mDualExtruderNameSubject.onNext(SSTPPacketContent.DualExtruderName.parse(packet.getContent()));
                        break;
                    }
                }
                break;
            }

            // 0x0a Setting Response
            case SSTPPacket.SETTINGS_RESPONSE_EVENT_ID: {
                byte subEventId = packet.getContent()[0];
                switch (subEventId) {
                    case 0x01:
                    case 0x0b:
                    case 0x0c:
                    case 0x13:
                    case 0x0d: {
                        sendResponse(packet, packet.getContent()[1] == 0);
                        break;
                    }
                    case 0x02:
                        if (packet.getContent()[1] != 0) {
                            // Auto calibration fail.
                            RequestReceiver receiver = mReceivers.get(packet.getKey());
                            receiver.receive(false);
                            break;
                        }
                    case 0x04:
                    case 0x05:
                    case 0x06:
                    case 0x07:
                    case 0x08:
                    case 0x09: {
                        RequestReceiver receiver = mReceivers.get(packet.getKey());
                        if (receiver != null) {
                            receiver.receive(true);
                        }
                        break;
                    }
                    case 0x03: {
                        if (packet.getContent()[1] != 0) {
                            // TODO: handle error response
                        }
                        mAutoCalibrationProgressSubject.onNext((packet.getContent()[2] & 0xFF));
                        break;
                    }
                    case 0x0a: {
                        Buffer buffer = new Buffer();
                        buffer.write(packet.getContent());
                        try {
                            buffer.readByte(); // sub-event
                            buffer.readByte(); // success
                            final int focalLengthX1000 = buffer.readInt();
                            sendResponse(packet, focalLengthX1000 / 1000f);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case 0x0e:
                    case 0x0f: {
                        sendResponse(packet, (int) packet.getContent()[1]);
                        break;
                    }
                    case 0x10: {
                        SSTPPacketContent.AdjustSettings settings = SSTPPacketContent.AdjustSettings.parse(packet.getContent());
                        sendResponse(packet, settings);
                        break;
                    }
                    case 0x11: {
                        // Light control result
                        sendResponse(packet, packet.getContent()[1]);
                        break;
                    }
                    case 0x12: {
                        Buffer buffer = new Buffer();
                        buffer.write(packet.getContent());
                        try {
                            buffer.readByte();
                            sendResponse(packet, buffer.readInt());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case 0x14: {
                        SSTPPacketContent.MachineSize machineSize = SSTPPacketContent.MachineSize.parse(packet.getContent());
                        Logger.d("Response 0x0a 0x14");
                        if (machineSize != null) {
                            sendResponse(packet, machineSize);
                        } else {
                            Logger.d("Response 0x0a 0x14 machine size is null, packet is: ", new String(packet.toByteArray()));
                        }
                        break;
                    }
                    case 0x15: {
                        sendResponse(packet, packet.getContent()[1] == 1);
                        break;
                    }
                }
                break;
            }

            // 0x0c Movement Response
            case SSTPPacket.MOVEMENT_RESPONSE_EVENT_ID: {
                byte subEventId = packet.getContent()[0];
                switch (subEventId) {
                    case 0x01:
                    case 0x02:
                    case 0x03:
                    case 0x04: {
                        sendResponse(packet, packet.getContent()[1] == 0);
                        break;
                    }
                }
                break;
            }

            // 0x0e Laser Camera Operation Response
            case SSTPPacket.LASER_CAMERA_OPERATION_RESPONSE_EVENT_ID: {
                byte subEventId = packet.getContent()[0];
                switch (subEventId) {
                    case 0x01: {
                        sendResponse(packet, packet.getContent()[1] == 0);
                        break;
                    }

                    case 0x02: {
                        SSTPPacketContent.LaserWifiStatus status = SSTPPacketContent.LaserWifiStatus.parse(packet.getContent());
                        if (status != null) {
                            sendResponse(packet, status);
                        }
                        break;
                    }
                    case 0x07: {
                        SSTPPacketContent.LaserBtStatus status = SSTPPacketContent.LaserBtStatus.parse(packet.getContent());
                        if (status != null) {
                            sendResponse(packet, status);
                        }
                        break;
                    }
                }
                break;
            }

            // 0x12 Add-on Operation Response
            case SSTPPacket.ADD_ON_OPERATION_RESPONSE_EVENT_ID: {
                byte subEventId = packet.getContent()[0];
                switch (subEventId) {
                    case 0x00: {
                        // TODO: add-on module list response, it's not defined yet.
                        break;
                    }
                    case 0x01: {
                        SSTPPacketContent.EnclosureStatus status = SSTPPacketContent.EnclosureStatus.parse(packet.getContent());
                        if (status != null) {
                            sendResponse(packet, status);
                        }
                        break;
                    }
                    case 0x02:
                    case 0x03:
                    case 0x04:
                    case 0x0B:
                    case 0x0C: {
                        sendResponse(packet, packet.getContent()[1] == 0);
                        break;
                    }
                    case 0x07:
                    case 0x08: {
                        sendResponse(packet, packet.getContent()[1]);
                        break;
                    }
                    case 0x09: {
                        SSTPPacketContent.AirPurifierStatus status = SSTPPacketContent.AirPurifierStatus.parse(packet.getContent());
                        if (status != null) {
                            sendResponse(packet, status);
                        }
                        break;
                    }
                    case 0x0A: {
                        SSTPPacketContent.AirPurifierFan airPurifierFan = SSTPPacketContent.AirPurifierFan.parse(packet.getContent());
                        if (airPurifierFan != null) {
                            sendResponse(packet, airPurifierFan);
                        }
                        break;
                    }
                    case 0x0D: {
                        Buffer buffer = new Buffer();
                        try {
                            buffer.write(packet.getContent());
                            buffer.readByte(); // operation id
                            int filterLifeTime = buffer.readByte();
                            sendResponse(packet, filterLifeTime);
                        } catch (IOException e) {
                            LogHelper.log(e);
                        }
                        break;
                    }
                }
                break;
            }
            // 0x14 Batch Gcode Response
            case SSTPPacket.PRINT_BATCH_GCODE_RESPONSE_EVENT_ID: {
                SSTPPacketContent.BatchGcodeResponse batchGcodeResponse = SSTPPacketContent.BatchGcodeResponse.parse(packet.getContent());
                if (batchGcodeResponse != null) {
                    mBatchGcodeResponseSubject.onNext(batchGcodeResponse);
                }
                break;
            }
            case SSTPPacket.MOCK_RESPONSE_EVENT_ID: {
                byte subEventId = packet.getContent()[0];
                switch (subEventId) {
                    case 0x01: {
                        Buffer buffer = new Buffer();
                        int machineType = 0;
                        try {
                            buffer.write(packet.getContent());
                            buffer.readByte(); // subEventID
                            machineType = buffer.readInt();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        sendResponse(packet, machineType);
                        break;
                    }
                }
                break;
            }
            // 0xaa Update
            case SSTPPacket.UPDATE_RESPONSE_EVENT_ID: {
                byte subEventId = packet.getContent()[0];
                switch (subEventId) {
                    case 0x00: {
                        sendResponse(packet, packet.getContent()[1] == 0);
                        break;
                    }
                    case 0x01: {
                        Buffer buffer = new Buffer();
                        short packageIndex = 0;
                        try {
                            buffer.write(packet.getContent());
                            buffer.readByte(); // subEventID
                            packageIndex = buffer.readShort();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        sendResponse(packet, packageIndex);
                        break;
                    }
                    case 0x02: {
                        break;
                    }
                    case 0x03: {
                        Buffer buffer = new Buffer();
                        buffer.write(packet.getContent());
                        String version = "";
                        try {
                            buffer.readByte();
                            // FIXME: readString could cause problem，the return string contains 00 in the end of data
                            version = buffer.readString(SSTPPacketContent.UTF_8).trim();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        sendResponse(packet, version);
                        break;
                    }

                    case 0x07: {
                        ModuleVersion moduleVersion = ModuleVersion.parse(packet.getContent());
                        if (moduleVersion != null) {
                            sendResponse(packet, moduleVersion);
                        }
                        break;
                    }
                }
                break;
            }
        }
    }

    private void sendResponse(IPacket packet, Object result) {
        RequestReceiver receiver = mReceivers.get(packet.getKey());
        if (receiver != null) {
            if (result != null) {
                receiver.receive(result);
            } else {
                receiver.error(new FabException("Unable to parse response body."));
            }
        }
    }

    public void onEmergencyStop() {
        // disable heartbeat
        setHeartbeatEnabled(false);
        // complete all the emitters
        for (int i = 0; i < mReceivers.size(); i++) {
            int key = mReceivers.keyAt(i);
            RequestReceiver receiver = mReceivers.get(key);
            receiver.complete();
        }
    }
}
