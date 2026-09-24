package fabscreen.platform.base.legacy;

import static fabscreen.platform.base.Constants.FIVE_MINUTES_DELAY_DURATION;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.Constants;
import fabscreen.platform.base.FabException;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.helper.SemVerHelper;
import fabscreen.platform.base.legacy.connection.MockConst;
import fabscreen.platform.base.legacy.connection.MockResponsePacketBuilder;
import fabscreen.platform.base.legacy.connection.SSTPPacketContent;
import fabscreen.platform.base.legacy.connection.print.DeprecatedPrintController;
import fabscreen.platform.base.legacy.print.IPrintController;
import fabscreen.platform.base.legacy.version.VersionRequirement;
import fabscreen.platform.base.legacy.version.VersionRequirementManager;
import fabscreen.platform.base.lib.fabserver.RetryWithDelay;
import fabscreen.platform.base.model.ILaserCameraController;
import fabscreen.platform.base.model.system.MachineStatusManager;
import fabscreen.platform.base.service.Preferences;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.entity.Toolhead;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Controller that manages machine status.
 */
@Deprecated
public class DeprecatedMachineController {
    private static final String DEVICE = "/dev/ttyHSL1";
    private CompositeDisposable disposables = new CompositeDisposable();
    private BehaviorSubject<Integer> mMachineTypeSubject = BehaviorSubject.create();
    private ISlaveComputer mSlaveComputer;
    private ILaserCameraController mLaserCameraController;
    private IPrintController mPrintController;
    private Preferences preferences;
    private VersionRequirementManager mVersionRequirementManager;

    //MachineInfo
    private int mHeadType = Module.ModuleType.HEAD_UNPLUGGED;
    private int mHeadKind = Module.ModuleType.HEAD_UNPLUGGED;
    private int mMachineModel = Constants.MACHINE_MODEL_UNKNOWN;
    private int mMachineType = Constants.MACHINE_UNKNOWN;
    private int mSizeX = 0;
    private int mSizeY = 0;
    private int mSizeZ = 0;
    private boolean mIsEmergencyStopAvailable = false;
    private boolean mIsRotaryAvailable = false;
    private BehaviorSubject<Byte> mRotaryModuleStatusSubject = BehaviorSubject.createDefault((byte) 1);
    private BehaviorSubject<Boolean> mEmergencyStopSubject = BehaviorSubject.createDefault(false);
    private BehaviorSubject<ArrayList<String>> mOutdatedVersionModuleListSubject = BehaviorSubject.createDefault(new ArrayList<>());

    //MachineStatus
    private boolean mHomed = false;
    private boolean mCoordinateAligned = false;
    private int mCoordinateID = 0;
    private float mCoordinateX = 0;
    private float mCoordinateY = 0;
    private float mCoordinateZ = 0;


    // EnclosureStatus
    private boolean mIsEnclosureReady = false;
    private boolean mIsEnclosureDoorDetectionEnabled = false;
    private int mEnclosureLedValue = 0;
    private int mEnclosureFanValue = 0;
    private BehaviorSubject<Boolean> mEnclosureDoorSubject = BehaviorSubject.createDefault(false);

    //Laser
    private byte mLaser10WErrorState = 0;
    private BehaviorSubject<Boolean> mPowerOutageSubject = BehaviorSubject.createDefault(false);
    private BehaviorSubject<Float> mLaserFocusSubject = BehaviorSubject.createDefault(0f);

    //Fdm
    private BehaviorSubject<Boolean> m3DPFilamentSubject = BehaviorSubject.createDefault(false);


    //AirPurifierStatus
    private boolean mAirPurifierIsOn = false;
    private int mAirPurifierFanSpeed = 0;
    private int mAirPurifierFilterLifeTime = 0;
    private Disposable mAirPurifierAutoTurnOffSubscription = null;
    private BehaviorSubject<SSTPPacketContent.AirPurifierStatus> mAirPurifierStatusSubject = BehaviorSubject.createDefault(SSTPPacketContent.AirPurifierStatus.MOCK_AIR_PURIFIER_STATUS_NOT_PLUGGED);
    private BehaviorSubject<SSTPPacketContent.AirPurifierFan> mAirPurifierFanSubject = BehaviorSubject.createDefault(new SSTPPacketContent.AirPurifierFan());
    private BehaviorSubject<Integer> mAirPurifierLifeTimeSubject = BehaviorSubject.createDefault(0);


    private Disposable printStateSubscription;
    private int mLaserCameraInterval = 10;
    private String mLaserCameraAddress;

    public DeprecatedMachineController(ISlaveComputer slaveComputer, ILaserCameraController laserCameraController, IPrintController printController,
                                       Preferences preferences, VersionRequirementManager versionRequirementManager) {
        mSlaveComputer = slaveComputer;
        mLaserCameraController = laserCameraController;
        mPrintController = printController;
        this.preferences = preferences;
        mVersionRequirementManager = versionRequirementManager;

        bind();
    }

    private void bind() {
        if (mSlaveComputer == null) {
            return;
        }

        // Subscribe connection state changes
        disposables.add(
                MachineStatusManager.getConnectedStatus().getObservable()
                        .subscribe(connected -> {
                            if (connected) {
                                this.onConnected();
                            } else {
                                this.onDisconnected();
                            }
                        })
        );
    }

    public void connect() {
        mSlaveComputer.connect();
    }

    public void reconnect() {
        connect();
    }

    private void onConnected() {
        Disposable sub = MachineStatusManager.getMachineInfoHolder().getObservable()
                .map(machineStatus -> machineStatus.headStatus)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(headStatus -> {
                    // Record head type
                    if (headStatus != mHeadType) {
                        mHeadType = headStatus;
                        switch (headStatus) {
                            case Module.ModuleType.HEAD_3DP:
                            case Toolhead.HeadFactoryId.HEAD_FACTORY_3DP:
                                mHeadKind = Module.ModuleType.HEAD_3DP;
                                break;
                            case Module.ModuleType.HEAD_CNC:
                            case Toolhead.HeadFactoryId.HEAD_FACTORY_CNC:
                                mHeadKind = Module.ModuleType.HEAD_CNC;
                                break;
                            case Module.ModuleType.HEAD_LASER:
                            case Module.ModuleType.HEAD_LASER_10W:
                            case Toolhead.HeadFactoryId.HEAD_FACTORY_LASER:
                                mHeadKind = Module.ModuleType.HEAD_LASER;
                                break;
                            default:
                                mHeadKind = Module.ModuleType.HEAD_UNPLUGGED;
                                break;
                        }
                        Logger.d("Head type %d found. Head Kind %d found.", mHeadType, mHeadKind);
                        onHeadTypeDetected();
                    }
                }, LogHelper::log);
        disposables.add(sub);
        RetryWithDelay retryWithDelay = new RetryWithDelay();
        sub = mSlaveComputer.getMachineSize()
                // Add a retransmission mechanism to reduce model loss problems
                // TODO: How to guarantee receipt?  What are the retransmission intervals and times?
                .retryWhen(retryWithDelay)
                .subscribe(machineSize -> {
                    if (machineSize != null) {
                        mMachineModel = machineSize.machineModel;
                        mSizeX = machineSize.xSize;
                        mSizeY = machineSize.ySize;
                        mSizeZ = machineSize.zSize;
                        Logger.d("Machine model %d, size x %d y %d z %d", mMachineModel, mSizeX, mSizeY, mSizeZ);
                        switch (mMachineModel) {
                            case Constants.MACHINE_MODEL_SNAPMAKER_A150: {
                                preferences.getHelper().setMachineModel(Constants.MACHINE_TYPE_A150);
                                break;
                            }
                            case Constants.MACHINE_MODEL_SNAPMAKER_A250: {
                                preferences.getHelper().setMachineModel(Constants.MACHINE_TYPE_A250);
                                break;
                            }
                            case Constants.MACHINE_MODEL_SNAPMAKER_A350: {
                                preferences.getHelper().setMachineModel(Constants.MACHINE_TYPE_A350);
                                break;
                            }
                            case Constants.MACHINE_MODEL_SNAPMAKER_A400:
                                preferences.getHelper().setMachineModel(Constants.MACHINE_TYPE_A400);
                                break;
                            case Constants.MACHINE_MODEL_UNKNOWN: {
                                // todo user modified model
                                break;
                            }
                            default:
                                break;
                        }
                    } else {
                        Logger.d("mMachineModel is NULL");
                    }
                }, LogHelper::log);
        disposables.add(sub);

        sub = mSlaveComputer.getMachineType()
                .subscribe(machineType -> {
                    Logger.d("mSlaveComputer.getMachineType() " + machineType);
                    mMachineType = machineType;
                }, LogHelper::log);
        disposables.add(sub);

        sub = mSlaveComputer.getMachineErrors()
                .subscribe(machineErrors -> {
                    final boolean isFilamentOut = (machineErrors.bits & SSTPPacketContent.MachineErrors.PRINT_FILAMENT_ERROR) != 0;
                    final boolean isPowerOutage = (machineErrors.bits & SSTPPacketContent.MachineErrors.PRINT_POWER_OFF) != 0;
                    final boolean isEnclosureDoorOpened = (machineErrors.bits & SSTPPacketContent.MachineErrors.ENCLOSURE_DOOR_OPEN) != 0;

                    m3DPFilamentSubject.onNext(isFilamentOut);
                    mPowerOutageSubject.onNext(isPowerOutage);
                    mEnclosureDoorSubject.onNext(isEnclosureDoorOpened);
                }, LogHelper::log);
        disposables.add(sub);

        // Watch machine errors in one place.
        sub = mSlaveComputer.watchMachineErrors()
                .subscribe(errors -> {
                    if (errors.bits == 0) return;

                    boolean isEnclosureDoorOpened = (errors.bits & SSTPPacketContent.MachineErrors.ENCLOSURE_DOOR_OPEN) != 0;
                    final boolean isFilamentOut = (errors.bits & SSTPPacketContent.MachineErrors.PRINT_FILAMENT_ERROR) != 0;

                    mEnclosureDoorSubject.onNext(isEnclosureDoorOpened);
                    m3DPFilamentSubject.onNext(isFilamentOut);
                }, LogHelper::log);
        disposables.add(sub);

        // Get Module Version and check if version is valid.
        sub = mSlaveComputer.watchModuleVersion()
                .subscribe(moduleVersion -> {
                    // Ensure if module version is valid.
                    String version = moduleVersion.version.trim().toUpperCase();
                    if (SemVerHelper.isValid(version)) {
                        VersionRequirement requirement = mVersionRequirementManager.getRequirementByModule(moduleVersion);
                        Logger.d("Module 0x%02X found, Module ID is 0x%06X, version is %s", moduleVersion.moduleType, moduleVersion.moduleID, moduleVersion.version);
                        if (requirement != null) {
                            try {
                                if (SemVerHelper.lt(version, requirement.getRequiredVersion())) {
                                    // Module version is outdated.
                                    addOutdatedVersionModule(requirement.getName());
                                }
                            } catch (FabException e) {
                                LogHelper.log(e);
                            }
                        }
                    } else {
                        Logger.w("Module 0x%02X version (%s) is invalid! ", moduleVersion.moduleType, moduleVersion.version);
                    }
                }, LogHelper::log);
        disposables.add(sub);

        Logger.d("Requesting module info...");
        mSlaveComputer.requestModuleVersion();

        // TODO: There's a better way to know which add-on was plugged instead of requesting status every time.
        //  Need to refactor this after we define add-on list request/response.
        sub = mSlaveComputer.getEnclosureStatus()
                .subscribe(enclosureStatus -> {
                    Logger.d("enclosure status is %s", enclosureStatus.enclosureEnabled);
                    mIsEnclosureReady = enclosureStatus.isReady();
                    mIsEnclosureDoorDetectionEnabled = enclosureStatus.isEnclosureEnabled();
                    mEnclosureLedValue = enclosureStatus.ledLevel;
                    mEnclosureFanValue = enclosureStatus.fanLevel;
                }, LogHelper::log);
        disposables.add(sub);

        final boolean isEnclosureAutoLightingOn = preferences.getHelper().getEnclosureAutoLightingOn();
        if (isEnclosureAutoLightingOn) {
            // Set Enclosure Lighting, range [0 - 100]
            // Full power(100) as "on" while setting enclosure lighting.
            final int value = 100;
            sub = mSlaveComputer.setEnclosureLed(100).subscribe(success -> {
                Logger.d("Set Enclosure lighting " + success);
            }, LogHelper::log);
            disposables.add(sub);
        }

        sub = mSlaveComputer.requestEmergencyStopStatus()
                .subscribe(status -> {
                    switch (status) {
                        case (byte) 0:
                            mEmergencyStopSubject.onNext(false);
                            mIsEmergencyStopAvailable = true;
                            onEmergencyStopConnected();
                            break;
                        case (byte) 1:
                            mEmergencyStopSubject.onNext(false);
                            mIsEmergencyStopAvailable = false;
                            break;
                        case (byte) 2:
                            mIsEmergencyStopAvailable = true;
                            mEmergencyStopSubject.onNext(true);
                            break;
                        default:
                            break;
                    }
                }, LogHelper::log);
        disposables.add(sub);

        sub = mSlaveComputer.requestRotaryModuleStatus()
                .subscribe(status -> {
                    mRotaryModuleStatusSubject.onNext(status);
                    switch (status) {
                        case (byte) 0:
                            // rotary connected and ready to go
                            mIsRotaryAvailable = true;
                            if (mHeadType == Module.ModuleType.HEAD_LASER) {
                                updateLaserFocus();
                            }
                            break;
                        case (byte) 1:
                            // rotary not connected
                        case (byte) 2:
                            // rotary detected but not available
                            mIsRotaryAvailable = false;
                            break;
                        default:
                            break;
                    }
                }, LogHelper::log);
        disposables.add(sub);

        sub = mSlaveComputer.requestAirPurifierAddOnStatus()
                .subscribe(airPurifierStatus -> {
                    mAirPurifierStatusSubject.onNext(airPurifierStatus);
                    if (airPurifierStatus.status != (byte) 0x01) {
                        onAirPurifierConnected();
                    }

                    switch (airPurifierStatus.status) {
                        case 0x00:
                            // OK
                            break;
                        case 0x01:
                            // not plugged
                            break;
                        case 0x02:
                            // no power
                            break;
                        case 0x03:
                            // error
                            break;
                        default:
                            break;
                    }
                }, LogHelper::log);
        disposables.add(sub);
    }

    private void onDisconnected() {
        // Back to default status
        mHeadType = Module.ModuleType.HEAD_UNPLUGGED;

        // Enclosure is not available while machine was disconnected.
        mIsEnclosureReady = false;
    }

    private void addOutdatedVersionModule(String moduleName) {
        ArrayList<String> moduleList = mOutdatedVersionModuleListSubject.getValue();
        if (!moduleList.isEmpty()) {
            // check if module name is already added.
            for (int i = 0; i < moduleList.size(); i++) {
                if (moduleList.get(i).equals(moduleName)) {
                    return;
                }
            }
        }

        moduleList.add(moduleName);
        mOutdatedVersionModuleListSubject.onNext(moduleList);
    }

    public Observable<ArrayList<String>> getOutdatedVersionModuleListObservable() {
        return mOutdatedVersionModuleListSubject.debounce(200, TimeUnit.MILLISECONDS).hide();
    }

    // enclosure

    public Observable<Boolean> getPowerOutageObservable() {
        return mPowerOutageSubject.hide();
    }

    public void clearPowerOutageFlag() {
        mPowerOutageSubject.onNext(false);
    }

    public void clearEnclosureDoorFlag() {
        mEnclosureDoorSubject.onNext(false);
    }

    public Observable<Boolean> getEnclosureDoorObservable() {
        return mEnclosureDoorSubject.hide();
    }

    public boolean isEnclosureOpen() {
        return mEnclosureDoorSubject.getValue();
    }

    public Observable<SSTPPacketContent.EnclosureStatus> updateEnclosureStatus() {
        return mSlaveComputer.getEnclosureStatus()
                .doOnNext(enclosureStatus -> {
                    mIsEnclosureReady = enclosureStatus.isReady();
                    mIsEnclosureDoorDetectionEnabled = enclosureStatus.isEnclosureEnabled();
                    mEnclosureLedValue = enclosureStatus.ledLevel;
                    mEnclosureFanValue = enclosureStatus.fanLevel;
                });
    }

    public boolean isEnclosureReady() {
        return mIsEnclosureReady;
    }

    public boolean isEnclosureLedOn() {
        return mEnclosureLedValue != 0;
    }

    public boolean isEnclosureFanOn() {
        return mEnclosureFanValue != 0;
    }

    public boolean isEnclosureDoorDetectionEnabled() {
        return mIsEnclosureDoorDetectionEnabled;
    }

    public int getEnclosureLed() {
        return mEnclosureLedValue;
    }

    public int getEnclosureFan() {
        return mEnclosureFanValue;
    }

    // Air Purifier
    public Observable<SSTPPacketContent.AirPurifierStatus> getAirPurifierStatusObservable() {
        return mAirPurifierStatusSubject.hide();
    }

    public boolean isAirPurifierPlugged() {
        return mAirPurifierStatusSubject.getValue().status != 0x01;
    }

    public boolean isAirPurifierReady() {
        return mAirPurifierStatusSubject.getValue().status == 0x00;
    }

    public Observable<SSTPPacketContent.AirPurifierStatus> updateAirPurifierStatus() {
        return mSlaveComputer.requestAirPurifierAddOnStatus().doOnNext(airPurifierStatus -> {
            mAirPurifierStatusSubject.onNext(airPurifierStatus);
        });
    }

    public SSTPPacketContent.AirPurifierStatus getAirPurifierStatus() {
        return mAirPurifierStatusSubject.getValue();
    }

    public Observable<SSTPPacketContent.AirPurifierFan> updateAirPurifierFan() {
        return mSlaveComputer.requestAirPurifierFan().doOnNext(airPurifierFan -> {
            mAirPurifierIsOn = airPurifierFan.isOn;
            mAirPurifierFanSpeed = airPurifierFan.level;
            mAirPurifierFanSubject.onNext(airPurifierFan);
        });
    }

    public int getAirPurifierFanSpeed() {
        return mAirPurifierFanSpeed;
    }

    public boolean isAirPurifierFanOn() {
        return mAirPurifierIsOn;
    }

    public int getAirPurifierFilterLifeTime() {
        return mAirPurifierLifeTimeSubject.getValue();
    }

    public Observable<Boolean> setAirPurifierEnabled(boolean enabled) {
        return mSlaveComputer.setAirPurifierEnabled(enabled)
                .doOnNext(ret -> {
                    disposables.add(updateAirPurifierFan().subscribe(success -> {/**/}, LogHelper::log));
                    if (isAirPurifierAutoTurnOffEnabled()) {
                        setAirPurifierAutoTurnOffEnabled(false);
                    }
                });
    }

    public Observable<Integer> getAirPurifierFilterLifeTimeObservable() {
        return mAirPurifierLifeTimeSubject.hide();
    }

    public Observable<SSTPPacketContent.AirPurifierFan> getAirPurifierFanObservable() {
        return mAirPurifierFanSubject.hide();
    }

    private void onAirPurifierConnected() {
        // Initialize air purifier status
        Disposable sub = mSlaveComputer.watchAirPurifierAddOnStatus()
                .subscribe(airPurifierStatus -> {
                    mAirPurifierStatusSubject.onNext(airPurifierStatus);
                }, LogHelper::log);
        disposables.add(sub);

        sub = mSlaveComputer.getAirPurifierFilterLifeTime()
                .doOnNext(l -> mAirPurifierLifeTimeSubject.onNext(l))
                .flatMap(life -> mSlaveComputer.watchAirPurifierFilterLifeTime())
                .subscribe(life -> {
                    mAirPurifierLifeTimeSubject.onNext(life);
                });
        disposables.add(sub);

        sub = mSlaveComputer.requestAirPurifierFan().subscribe(airPurifierFan -> {
            mAirPurifierIsOn = airPurifierFan.isOn;
            mAirPurifierFanSpeed = airPurifierFan.level;
            mAirPurifierFanSubject.onNext(airPurifierFan);
        }, LogHelper::log);
        disposables.add(sub);

        // Observe Print state with PrintController for auto turn off task.
        printStateSubscription = mPrintController.getPrintStateObservable()
                .distinctUntilChanged()
                .subscribe(state -> {
                    if (isAirPurifierAutoTurnOffNeeded()) {
                        setAirPurifierAutoTurnOffEnabled((state == DeprecatedPrintController.STATE_COMPLETED));
                    } else {
                        // TODO: Temporary fix.
                        //  If we stop or complete print job, update air purifier once for synchronizing status.
                        //  We need to do that because controller won't push air purifier status when print job finished or stopped.
                        if (state == DeprecatedPrintController.STATE_COMPLETED || state == DeprecatedPrintController.STATE_IDLE) {
                            disposables.add(updateAirPurifierStatus().subscribe());
                        }
                    }
                }, LogHelper::log);
        disposables.add(printStateSubscription);
    }

    public void setPrintController(IPrintController printController) {
        mPrintController = printController;
        if (printStateSubscription != null && !printStateSubscription.isDisposed()) {
            disposables.remove(printStateSubscription);
            printStateSubscription.dispose();
        }
        printStateSubscription = mPrintController.getPrintStateObservable()
                .distinctUntilChanged()
                .subscribe(state -> {
                    if (isAirPurifierAutoTurnOffNeeded()) {
                        setAirPurifierAutoTurnOffEnabled((state == DeprecatedPrintController.STATE_COMPLETED));
                    } else {
                        // TODO: Temporary fix.
                        //  If we stop or complete print job, update air purifier once for synchronizing status.
                        //  We need to do that because controller won't push air purifier status when print job finished or stopped.
                        if (state == DeprecatedPrintController.STATE_COMPLETED || state == DeprecatedPrintController.STATE_IDLE) {
                            disposables.add(updateAirPurifierStatus().subscribe());
                        }
                    }
                }, LogHelper::log);
        disposables.add(printStateSubscription);
    }

    private boolean isAirPurifierAutoTurnOffNeeded() {
        return preferences.getHelper().getAirPurifierAutoTurnOffFlag() && mAirPurifierIsOn;
    }

    private boolean isAirPurifierAutoTurnOffEnabled() {
        return mAirPurifierAutoTurnOffSubscription != null;
    }

    private void setAirPurifierAutoTurnOffEnabled(boolean enabled) {
        if (enabled) {
            // Prevent multiply subscribe when this calls.
            if (mAirPurifierAutoTurnOffSubscription != null) {
                mAirPurifierAutoTurnOffSubscription.dispose();
            }

            Logger.d("Start countdown for Air Purifier auto turn off...");

            mAirPurifierAutoTurnOffSubscription = Observable.interval(FIVE_MINUTES_DELAY_DURATION, Constants.TIME_UNIT)
                    .take(1)
                    .subscribe(t -> {
                        // Turn off the air purifier when timeout
                        Logger.d("Air Purifier auto turn off.");
                        disposables.add(
                                mSlaveComputer.setAirPurifierEnabled(false)
                                        .subscribe(success -> {/**/}, LogHelper::log)
                        );
                    });
        } else {
            Logger.d("Air Purifier auto turn off canceled.");
            // Disable Auto Turn Off Task
            if (mAirPurifierAutoTurnOffSubscription != null) {
                mAirPurifierAutoTurnOffSubscription.dispose();
                mAirPurifierAutoTurnOffSubscription = null;
            }
        }
    }

    // Rotary Module plugged and usable
    public boolean isRotaryModuleAvailable() {
        return mIsRotaryAvailable;
    }

    public Observable<Byte> getRotaryStatusObservable() {
        return mRotaryModuleStatusSubject.hide();
    }

    public Byte getRotaryModuleStatus() {
        return mRotaryModuleStatusSubject.getValue();
    }

    public void setRotaryModuleStatus(int moduleStatus) {
        MockResponsePacketBuilder.getInstance().setRotaryModuleStatus((byte) moduleStatus);
        Disposable sub = mSlaveComputer.requestRotaryModuleStatus()
                .subscribe(status -> {
                    mRotaryModuleStatusSubject.onNext(status);
                    switch (status) {
                        case (byte) 0:
                            // rotary connected and ready to go
                            mIsRotaryAvailable = true;
                            if (mHeadType == Module.ModuleType.HEAD_LASER) {
                                updateLaserFocus();
                            }
                            break;
                        case (byte) 1:
                            // rotary not connected
                        case (byte) 2:
                            // rotary detected but not available
                            mIsRotaryAvailable = false;
                            break;
                        default:
                            break;
                    }
                }, LogHelper::log);
        disposables.add(sub);
    }

    // Emergency Stop add-on
    public Observable<Boolean> getEmergencyStopObservable() {
        return mEmergencyStopSubject.hide();
    }

    // 3DP filament

    public boolean isEmergencyStopTriggered() {
        return mEmergencyStopSubject.getValue();
    }

    public void setEmergencyStopTriggered(boolean isTriggered) {
        mEmergencyStopSubject.onNext(isTriggered);
    }

    public boolean isEmergencyStopAvailable() {
        return mIsEmergencyStopAvailable;
    }

    public void clearFilamentOutFlag() {
        m3DPFilamentSubject.onNext(false);
    }

    public boolean isFilamentOut() {
        return m3DPFilamentSubject.getValue();
    }

    public Observable<Boolean> getFilamentObservable() {
        return m3DPFilamentSubject.hide();
    }

    /**
     * get machine size
     */

    public int getMachineModel() {
        return mMachineModel;
    }

    public String getMachineModelSeries() {
        switch (mMachineModel) {
            case Constants.MACHINE_MODEL_SNAPMAKER_A150:
                return "Snapmaker 2.0 A150";
            case Constants.MACHINE_MODEL_SNAPMAKER_A250:
                return "Snapmaker 2.0 A250";
            case Constants.MACHINE_MODEL_SNAPMAKER_A350:
                return "Snapmaker 2.0 A350";
            default:
                return "Unknown";
        }
    }

    public int getSizeX() {
        return mSizeX;
    }

    public int getSizeY() {
        return mSizeY;
    }

    public int getSizeZ() {
        return mSizeZ;
    }

    public byte getLaser10WErrorState() {
        return mLaser10WErrorState;
    }

    public void setLaser10WErrorState(byte laser10WErrorState) {
        this.mLaser10WErrorState = laser10WErrorState;
    }

    /**
     * Get Head Type
     */
    public int getHeadType() {
        return mHeadType;
    }

    private void onHeadTypeDetected() {
        switch (mHeadType) {
            case Module.ModuleType.HEAD_LASER:
            case Toolhead.HeadFactoryId.HEAD_FACTORY_LASER:
            case Module.ModuleType.HEAD_LASER_10W:
                initLaserCamera();
                initLaserFocus();
                break;
            default:
                break;
        }
    }

    /**
     * Check if home is executed after booted.
     */
    public boolean isHomed() {
        return mHomed;
    }

    public int getCoordinateID() {
        return mCoordinateID;
    }

    public boolean isCoordinateAligned() {
        return mCoordinateAligned;
    }

    public Observable<SSTPPacketContent.CoordinateSystem> updateCoordinateSystem() {
        return updateCoordinateSystem(-1);
    }

    public Observable<SSTPPacketContent.CoordinateSystem> updateCoordinateSystem(int coordinateID) {
//        if (coordinateID != -1) {
//            Logger.i("update coordinate system... CS#" + coordinateID);
//            return mSlaveComputer.sendGcode("G" + (53 + coordinateID))
//                    .flatMap(response -> mSlaveComputer.requestCoordinateSystem())
//                    .doOnNext(coordinateSystem -> {
//                        mHomed = coordinateSystem.isHomed;
//                        mCoordinateAligned = coordinateSystem.coordinateAligned;
//                        mCoordinateID = coordinateSystem.coordinateID;
//                        mCoordinateX = coordinateSystem.coordinateX;
//                        mCoordinateY = coordinateSystem.coordinateY;
//                        mCoordinateZ = coordinateSystem.coordinateZ;
//                        Logger.i("Coordinate system updated, CS#" + mCoordinateID + " X = " + mCoordinateX + ", Y = " + mCoordinateY + ", Z =" + mCoordinateZ);
//                    });
//        } else {
//            Logger.i("update coordinate system...");
//            return mSlaveComputer.requestCoordinateSystem()
//                    .doOnNext(coordinateSystem -> {
//                        mHomed = coordinateSystem.isHomed;
//                        mCoordinateAligned = coordinateSystem.coordinateAligned;
//                        mCoordinateID = coordinateSystem.coordinateID;
//                        mCoordinateX = coordinateSystem.coordinateX;
//                        mCoordinateY = coordinateSystem.coordinateY;
//                        mCoordinateZ = coordinateSystem.coordinateZ;
//                        Logger.i("Coordinate system updated, CS#" + mCoordinateID + " X = " + mCoordinateX + ", Y = " + mCoordinateY + ", Z =" + mCoordinateZ);
//                    });
//        }
        return null;
    }

    public Observable<SSTPPacketContent.CoordinateSystem> updateCoordinateSystem(int coordinateID, int timeout) {
//        if (coordinateID != -1) {
//            Logger.i("update coordinate system... CS#" + coordinateID);
//            return mSlaveComputer.sendGcode("G" + (53 + coordinateID))
//                    .flatMap(response -> mSlaveComputer.requestCoordinateSystem(timeout))
//                    .doOnNext(coordinateSystem -> {
//                        mHomed = coordinateSystem.isHomed;
//                        mCoordinateAligned = coordinateSystem.coordinateAligned;
//                        mCoordinateID = coordinateSystem.coordinateID;
//                        mCoordinateX = coordinateSystem.coordinateX;
//                        mCoordinateY = coordinateSystem.coordinateY;
//                        mCoordinateZ = coordinateSystem.coordinateZ;
//                        Logger.i("Coordinate system updated, CS#" + mCoordinateID + " X = " + mCoordinateX + ", Y = " + mCoordinateY + ", Z =" + mCoordinateZ);
//                    });
//        } else {
//            Logger.i("update coordinate system...");
//            return mSlaveComputer.requestCoordinateSystem(timeout)
//                    .doOnNext(coordinateSystem -> {
//                        mHomed = coordinateSystem.isHomed;
//                        mCoordinateAligned = coordinateSystem.coordinateAligned;
//                        mCoordinateID = coordinateSystem.coordinateID;
//                        mCoordinateX = coordinateSystem.coordinateX;
//                        mCoordinateY = coordinateSystem.coordinateY;
//                        mCoordinateZ = coordinateSystem.coordinateZ;
//                        Logger.i("Coordinate system updated, CS#" + mCoordinateID + " X = " + mCoordinateX + ", Y = " + mCoordinateY + ", Z =" + mCoordinateZ);
//                    });
//        }
        return null;
    }

    // - Laser

    public float getCoordinateOffsetX() {
        return mCoordinateX;
    }

    public float getCoordinateOffsetY() {
        return mCoordinateY;
    }

    public float getCoordinateOffsetZ() {
        return mCoordinateZ;
    }

    private void initLaserFocus() {
        Disposable sub = mSlaveComputer.getLaserFocalLength()
                .subscribe(focalLength -> {
                    float actualFocal;
                    if (mIsRotaryAvailable) {
                        actualFocal = focalLength
                                + MockConst.LASER_MOCK_ROTARY_WASTE_BOARD_HEIGHT
                                + MockConst.LASER_MOCK_ROTARY_HEIGHT;
                    } else {
                        actualFocal = focalLength + MockConst.LASER_MOCK_PLATE_HEIGHT;
                    }
                    mLaserFocusSubject.onNext(actualFocal);
                }, LogHelper::log);
        disposables.add(sub);
    }

    public void updateLaserFocus() {
        Disposable sub = mSlaveComputer.getLaserFocalLength()
                .subscribe(focalLength -> {
                    float actualFocal;
                    if (mIsRotaryAvailable) {
                        actualFocal = focalLength / 1000f
                                + MockConst.LASER_MOCK_ROTARY_WASTE_BOARD_HEIGHT
                                + MockConst.LASER_MOCK_ROTARY_HEIGHT;
                    } else {
                        actualFocal = focalLength / 1000f + MockConst.LASER_MOCK_PLATE_HEIGHT;
                    }
                    mLaserFocusSubject.onNext(actualFocal);
                });
        disposables.add(sub);
    }

    public float getLaserFocus() {
        return mLaserFocusSubject.getValue();
    }

    public Observable<Float> getLaserFocusObservable() {
        return mLaserFocusSubject.hide();
    }

    public Observable<Boolean> setLaserFocus(float laserFocus) {
        float actualFocal;
        if (mIsRotaryAvailable) {
            actualFocal = laserFocus - MockConst.LASER_MOCK_ROTARY_WASTE_BOARD_HEIGHT
                    - MockConst.LASER_MOCK_ROTARY_HEIGHT;
        } else {
            actualFocal = laserFocus - MockConst.LASER_MOCK_PLATE_HEIGHT;
        }
        return mSlaveComputer.setLaserFocalLength(actualFocal)
                .doOnNext(success -> mLaserFocusSubject.onNext(laserFocus));
    }

    private void initLaserCamera() {
        if (!mLaserCameraController.isEnabled()) {
            mLaserCameraController.setEnabled(true);
            // wait 5 seconds for opening bluetooth device
            AndroidSchedulers.mainThread().scheduleDirect(this::initLaserCamera, 5000, TimeUnit.MILLISECONDS);
            return;
        }
        waitForLaserCameraReady();
    }

    private void waitForLaserCameraReady() {
        Disposable sub = mSlaveComputer.getLaserBluetoothStatus()
                .subscribe(status -> {
                    Logger.d("Laser Camera status " + status.isReady() + "," + status.getMacAddress());
                    if (status.isReady()) {
                        mLaserCameraAddress = status.getMacAddress();

                        // remove pair records if count is over 10
                        if (mLaserCameraController.getBondedDeviceCount() > 10) {
                            mLaserCameraController.removeBondedDeviceRecords();
                        }
                        connectLaserCamera();
                    } else {
                        // wait another 10s
                        AndroidSchedulers.mainThread().scheduleDirect(this::waitForLaserCameraReady, 10000, TimeUnit.MILLISECONDS);
                    }
                });
        disposables.add(sub);
    }

    private void connectLaserCamera() {
        // update connection status
        mLaserCameraController.updateConnectionStatus();

        // reconnect if bluetooth is disconnected
        if (!mLaserCameraController.isConnected()) {
            Disposable sub = mLaserCameraController.connect(mLaserCameraAddress)
                    .observeOn(Schedulers.computation())
                    .subscribe(success -> {
                        if (success) {
                            Logger.d("Laser Camera connected!");
                            onLaserCameraConnected();
                        } else {
                            Logger.d("Laser Camera connect failed!");
                            onLaserCameraConnectFailed();
                        }
                    }, LogHelper::log);
            disposables.add(sub);
        } else {
            onLaserCameraConnected();
        }
    }

    private void onLaserCameraConnected() {
        mLaserCameraInterval = 10;
        AndroidSchedulers.mainThread().scheduleDirect(this::connectLaserCamera, 300, TimeUnit.SECONDS);
    }

    private void onLaserCameraConnectFailed() {
        mLaserCameraInterval = Math.max(3600, mLaserCameraInterval * 2);
        AndroidSchedulers.mainThread().scheduleDirect(this::connectLaserCamera, mLaserCameraInterval, TimeUnit.SECONDS);
    }

    private void onEmergencyStopConnected() {
        Disposable sub = mSlaveComputer.watchEmergencyStopStatus().subscribe(status -> {
            if (status == 2) {
                mEmergencyStopSubject.onNext(true);
            }
        });
        disposables.add(sub);
    }

    public int getHeadKind() {
        return mHeadKind;
    }

    public int getMachineType() {
        return mMachineType;
    }

    // This is weired, machine type needs to be in the heartbeat interval.
    public void setMachineType(int machineType) {
        MockResponsePacketBuilder.getInstance().setMachineType(machineType);
        // FIXME
//        Disposable subscribe = mSlaveComputer.getMachineType()
//                .subscribe(type -> MachineStatusManager.getSeriesHolder().onNext(type));
//        disposables.add(subscribe);
    }

    public Observable<Integer> getMachineTypeObservable() {
        return mMachineTypeSubject.hide();
    }
}
