package fabscreen.platform.base.service.machine.controller;

import androidx.annotation.NonNull;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fabscreen.platform.base.model.ModelBoundary;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.entity.ModuleFactory;
import fabscreen.platform.base.service.machine.entity.module.AirPurifier;
import fabscreen.platform.base.service.machine.entity.module.DryBox;
import fabscreen.platform.base.service.machine.entity.module.EmergencyButton;
import fabscreen.platform.base.service.machine.entity.module.Enclosure;
import fabscreen.platform.base.service.machine.entity.module.HeatedBed;
import fabscreen.platform.base.service.machine.entity.module.LinearModule;
import fabscreen.platform.base.service.machine.entity.module.RotaryModule;
import fabscreen.platform.base.service.machine.entity.parts.LinearLimit;
import fabscreen.platform.base.service.machine.entity.parts.Motor;
import fabscreen.platform.base.service.machine.entity.toolhead.CNCToolhead;
import fabscreen.platform.base.service.machine.entity.toolhead.FdmToolhead;
import fabscreen.platform.base.service.machine.entity.toolhead.LaserToolhead;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.CoordinateStructure;
import fabscreen.platform.base.service.machine.structure.CoordinateSystemInfo;
import fabscreen.platform.base.service.machine.structure.FrequencyConfig;
import fabscreen.platform.base.service.machine.structure.MachineProductInfo;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.StructureVectorMapper;
import fabscreen.platform.base.service.machine.structure.SubscribeStructure;
import fabscreen.platform.base.service.machine.structure.VibrationCompensationConfig;
import fabscreen.platform.base.service.machine.structure.prop.ArrayProp;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.FloatProp;
import fabscreen.platform.base.service.machine.structure.prop.StringProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt16Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Movement and heated bed and Add-ons.
 */
public class MachineController {
    private static final String TAG = "MachineController";
    private final MachineConnectionController mConnectionController;
    private final BehaviorSubject<MachineStatus> mStatusSubject;
    private final BehaviorSubject<MachineInfo> mInfoSubject;
    private BehaviorSubject<Integer> mHomeResultSubject;
    private BehaviorSubject<ResponseStructure> mRestartMachineSubject;
    private BehaviorSubject<Boolean> mMotorPotentialState;
    Disposable MotorPotentialStateSub;
    IMachine mMachine;
    IAppService mAppService;
    Disposable RelativeHomeLocationSub;
    private BehaviorSubject<Vector> mRelativeHomeLocation;
    private final CompositeDisposable mDisposables = new CompositeDisposable();


    public MachineController(IMachine mc, MachineConnectionController cc, BehaviorSubject<MachineInfo> machineInfoSubject, BehaviorSubject<MachineStatus> statusSubject, IAppService appService) {
        mMachine = mc;
        mAppService = appService;
        mConnectionController = cc;
        mInfoSubject = machineInfoSubject;
        mStatusSubject = statusSubject;
        observeMachineStatus();
        watchCoordinateUpdate();
    }

    private void watchCoordinateUpdate() {
        // CoordinateSystemInfo
        // Watch for coordinate push
        ResponseStructure<CoordinateSystemInfo> coordinatePayload = new ResponseStructure<>(new CoordinateSystemInfo());
        mDisposables.add(mConnectionController.watch(0x01, 0xa2, coordinatePayload)
                .filter(ResponseStructure::isSuccess)
                .subscribe(response -> updateMachineStatus(response.dataProp), LogHelper::log));
    }

    private void observeMachineStatus() {
        mDisposables.add(mMachine.getMachineStatusSubjectHolder()
                .getObservable()
                .filter(machineStatus -> machineStatus.connected && machineStatus.getLastStatus() != null && !machineStatus.getLastStatus().connected)
                .subscribe(machineStatus -> {
                    initModules();
                    initControllerLog();
                }, LogHelper::log));
    }

    private void initControllerLog() {
        // Log
        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("logLevel", new UInt8Prop());
                addProp("log", new StringProp());
            }
        };

        ResponseStructure<IStructure> responseStructure = new ResponseStructure<>();
        responseStructure.dataProp = baseStructure;

        mDisposables.add(mConnectionController.watch(0x01, 0xa1, responseStructure)
                .subscribe(logResponseStructure -> {
                    BaseStructure logResponse = (BaseStructure) logResponseStructure.dataProp;
                    int logLever = (int) logResponse.getProp("logLevel").getValue();
                    String log = (String) logResponse.getProp("log").getValue();
                    LogHelper.firmwareLog(logLever, log);
                }, LogHelper::log));

        SubscribeStructure subStruct = new SubscribeStructure(0x01, 0xa1, 5000);
        ResponseStructure<IStructure> resStruct = new ResponseStructure<>();
        mDisposables.add(setMachineLogLevel(2)
                .flatMap(res -> mConnectionController.request(0x01, 0x00, subStruct, resStruct))
                .subscribe(response -> {
                }, LogHelper::log));
    }

    public void initModules() {
        ArrayProp<Module.ModuleInfo> moduleList = new ArrayProp<>();
        moduleList.addElement(new Module.ModuleInfo());
//        Logger.d("sacp-debug: machine status changed - connected");

        ResponseStructure<ArrayProp<Module.ModuleInfo>> moduleListStructure = new ResponseStructure<>();
        moduleListStructure.dataProp = moduleList;
        Observable<ResponseStructure<ArrayProp<Module.ModuleInfo>>> moduleListRequest = mConnectionController.request(0x01, 0x20, null, moduleListStructure);

        ResponseStructure<MachineProductInfo> productInfoStructure = new ResponseStructure<>();
        productInfoStructure.dataProp = new MachineProductInfo();
        Observable<ResponseStructure<MachineProductInfo>> productInfoRequest = mConnectionController.request(0x01, 0x21, null, productInfoStructure);

        ResponseStructure<BaseStructure> responseStructure = new ResponseStructure<>();
        responseStructure.dataProp = new BaseStructure() {
            @Override
            protected void init() {
                addProp("AxisLength", new ArrayProp<>(new CoordinateStructure()));
                addProp("homeOffset ", new ArrayProp<>(new CoordinateStructure()));
            }
        };
        Observable<ResponseStructure<BaseStructure>> machineSizeRequest = mConnectionController.request(0x01, 0x22, null, responseStructure);

        Disposable subscribe = Observable.zip(moduleListRequest, productInfoRequest, machineSizeRequest, (moduleListResponse, productInfoResponse, machineSizeResponse) -> {
            ArrayProp<Module.ModuleInfo> moduleList1 = moduleListResponse.dataProp;
            MachineProductInfo productInfo = productInfoResponse.dataProp;
            BaseStructure machineSizeProp = machineSizeResponse.dataProp;
            //noinspection unchecked
            List<CoordinateStructure> axisLengthList = ((ArrayProp<CoordinateStructure>) machineSizeProp.getProp("AxisLength")).getValue();
            Vector axisLengthVector = StructureVectorMapper.structureListToVector(axisLengthList);

            Logger.d("moduleList1:%1$s\nproductInfo: %2$s\nmachineSizeProp:%3$s", moduleList1.toString(), productInfo.toString(), machineSizeProp.toString());
            List<Module.ModuleInfo> moduleInfoList = moduleList1.getValue();
            MachineInfo machineInfo = extractMachineInfoAndInitToolheadController(productInfo, moduleInfoList);
            machineInfo.size = axisLengthVector;
            return machineInfo;
        }).subscribe(mInfoSubject::onNext, LogHelper::log);
        mDisposables.add(subscribe);
    }

    @NonNull
    private MachineInfo extractMachineInfoAndInitToolheadController(MachineProductInfo productInfo, List<Module.ModuleInfo> moduleInfoList) throws Exception {
        MachineInfo info = mMachine.getMachineInfoSubjectHolder().getValue();
        // This is important, give a new module list when init.
        info.moduleList = new ArrayList<>();
        getInfoFromModuleList(moduleInfoList, info);
        getInfoFromProductInfo(productInfo, info);
        Logger.d("machine info: %s", info.toString());
        return info;
    }

    private void getInfoFromModuleList(List<Module.ModuleInfo> moduleInfoList, MachineInfo info) throws Exception {
//        Logger.d("added modules: %s", moduleInfoList);
        for (Module.ModuleInfo moduleInfo : moduleInfoList) {
            Module module = ModuleFactory.createModule(moduleInfo, mMachine, mConnectionController, mAppService);
//            Logger.d("added module: %d", module.getModuleInfo().getModuleId());
            if (module instanceof FdmToolhead) {
                mMachine.getFDMController().addToolHead((FdmToolhead) module);
                info.workType = requireLegalWorkType(info.workType, IMachine.WorkType.FDM);
            } else if (module instanceof LaserToolhead) {
                mMachine.getLaserController().addToolHead((LaserToolhead) module);
                info.workType = requireLegalWorkType(info.workType, IMachine.WorkType.LASER);
            } else if (module instanceof CNCToolhead) {
                mMachine.getCNCController().addToolHead((CNCToolhead) module);
                info.workType = requireLegalWorkType(info.workType, IMachine.WorkType.CNC);
            } else if (module instanceof HeatedBed) {
                info.isHeatedBedAvailable = true;
            } else if (module instanceof Enclosure) {
                info.isEnclosureAvailable = true;
            } else if (module instanceof RotaryModule) {
                info.isRotaryAvailable = true;
            } else if (module instanceof AirPurifier) {
                info.isAirPurifierAvailable = true;
            } else if (module instanceof EmergencyButton) {
                info.isEmergencyStopAvailable = true;
            } else if (module instanceof DryBox) {
                info.isDryBoxAvailable = true;
            }
            info.moduleList.add(module);
        }
    }

    private void getInfoFromProductInfo(MachineProductInfo productInfo, MachineInfo info) {
        info.productId = productInfo.getProductId();
        info.modelId = productInfo.getModel();
        info.seriesId = productInfo.getBrand();
        info.controllerFWVersion = productInfo.getControllerFWVersion();
        info.serialNo = productInfo.getSn();
    }

    private IMachine.WorkType requireLegalWorkType(IMachine.WorkType current, IMachine.WorkType replace) throws Exception {
        if (current == IMachine.WorkType.NONE || current == replace) return replace;
        else throw new Exception("Machines do not support headers of more than one work type");
    }

    public Observable<ResponseStructure> sendGcode(String gcode) {
        return mConnectionController.request(0x01, 0x02, new StringProp(gcode), new ResponseStructure<>());
    }

    public Observable<ResponseStructure> restartMachine() {
        Logger.d("Restarting %s", mMachine.getMachineInfoSubjectHolder().getValue().seriesId == IMachine.MachineSeries.J ? "J series" : "A series");
        // J1 doesn't implement this interface, it will restart by itself, just return 0(success).
        if (mMachine.getMachineInfoSubjectHolder().getValue().seriesId == IMachine.MachineSeries.J) {
            return Observable.just(new ResponseStructure<>(0));
        }

        if (mRestartMachineSubject != null) {
            mRestartMachineSubject.onComplete();
            mRestartMachineSubject = null;
        }
        mRestartMachineSubject = BehaviorSubject.create();
        return mConnectionController.request(0x01, 0x03, null, new ResponseStructure<>())
                .flatMap(response -> mRestartMachineSubject.hide());
    }

    public Observable<ResponseStructure> setMachineLogLevel(int logLevel) {
        return mConnectionController.request(0x01, 0x10, new UInt8Prop(logLevel), new ResponseStructure<>());
    }

    public Observable<ResponseStructure> setMachineAcceptanceAgreementType(int agreementType) {
        return mConnectionController.request(0x01, 0x11, new UInt8Prop(agreementType), new ResponseStructure<>());
    }

    public Observable<ResponseStructure> requestMachineFactoryReset(int opt) {
        return mConnectionController.request(0x01, 0x13, new UInt8Prop(opt), new ResponseStructure());
    }

    public Observable<ResponseStructure<BaseStructure>> getMachineSize() {
        ResponseStructure<BaseStructure> responseStructure = new ResponseStructure<>();
        responseStructure.dataProp = new BaseStructure() {
            @Override
            protected void init() {
                addProp("AxisLength", new ArrayProp<>(new CoordinateStructure()));
                addProp("homeOffset ", new ArrayProp<>(new CoordinateStructure()));
            }
        };
        return mConnectionController.request(0x01, 0x22, null, responseStructure);
    }

    /*---------------------------machine state-------------------------*/

    /**
     * Request latest coordinate and update the coordinate in machine status.
     */
    public Observable<ResponseStructure<CoordinateSystemInfo>> pullCoordinate() {
        // Request machine coordinate system
        return pullCoordinate(0);
    }

    public Observable<ResponseStructure<CoordinateSystemInfo>> pullCoordinate(int type) {
        return mConnectionController.request(0x01, 0x30, new UInt8Prop(type), new ResponseStructure<>(new CoordinateSystemInfo()))
                .doOnNext(response -> updateMachineStatus(response.dataProp));
    }

    private void updateMachineStatus(CoordinateSystemInfo coordinateInfo) {
        boolean offsetAligned = coordinateInfo.getOffsetAligned();
        boolean homed = coordinateInfo.getHomed();
        int coordinateSystemId = coordinateInfo.getCoordinateSystemId();
        List<CoordinateStructure> coordinates = coordinateInfo.getCoordinates();
        List<CoordinateStructure> offsets = coordinateInfo.getOffsets();
        mStatusSubject.onNext(
                mStatusSubject.getValue().CreateBuilder()
                        .changeCoordinateAligned(offsetAligned)
                        .changeCoordinateId(coordinateInfo.getCoordinateSystemId())
                        .changeHomed(homed)
                        .changeCoordinateId(coordinateSystemId)
                        .changeCurrentPosition(StructureVectorMapper.structureListToVector(coordinates))
                        .changeOriginOffset(StructureVectorMapper.structureListToVector(offsets))
                        .build());
    }

    public Observable<MachineStatus> updateCoordinateSystem() {
        return updateCoordinateSystem(0);
    }

    /**
     * Update CoordinateSystem and refresh the coordinate.
     */
    public Observable<MachineStatus> updateCoordinateSystem(int index) {
        return mConnectionController.request(0x01, 0x31, new UInt8Prop(index), new ResponseStructure<>())
                .flatMap(success -> pullCoordinate())
                .flatMap(success -> Observable.just(mStatusSubject.getValue()));
    }

    public Observable<MachineStatus> updateCoordinateSystemIfNot(int index) {
        if (mStatusSubject.getValue().coordinateID == index) {
            // Already on the required system, return directly.
            return Observable.just(mStatusSubject.getValue());
        }
        return mConnectionController.request(0x01, 0x31, new UInt8Prop(index), new ResponseStructure<>())
                .flatMap(success -> pullCoordinate())
                .flatMap(success -> Observable.just(mStatusSubject.getValue()));
    }

    public Observable<ResponseStructure> setWorkOrigin(Vector vector) {
        ArrayProp<CoordinateStructure> coordinateInformationArrayProp = new ArrayProp<>(StructureVectorMapper.vectorToStructureList(vector));
        return mConnectionController.request(0x01, 0x32, coordinateInformationArrayProp, new ResponseStructure());
    }


    @Deprecated
    public Observable<ResponseStructure> gotoRelativePosition(Vector vector) {
//        ArrayProp<MovementStructure> coordinateInformationArrayProp = new ArrayProp<>(StructureVectorMapper.vectorToMoveStructureList(vector, 0));
//        return mConnectionController.request(0x01, 0x33, coordinateInformationArrayProp, new ResponseStructure());
        return Observable.just(new ResponseStructure<>());
    }

    /**
     * Make sure you are on G53 system before call this if you want to go to a position like half of axis.
     */
    public Observable<ResponseStructure> gotoAbsolutePosition(Vector vector) {
        return gotoAbsolutePosition(vector, 1800);
    }

    public Observable<ResponseStructure> goToOrigin() {
        Vector vector = new Vector();
        vector.setX(0);
        vector.setY(0);
        vector.setZ(0);
        if (mMachine.getMachineInfoSubjectHolder().getValue().isRotaryAvailable) {
            vector.setB(0);
        }
        return gotoAbsolutePosition(vector, 3000);
    }

    public Observable<ResponseStructure> goToXYOrigin() {
        Vector vector = new Vector();
        vector.setX(0);
        vector.setY(0);
        return gotoAbsolutePosition(vector);
    }

    /**
     * Go to absolute position and pull coordinateSystem to refresh local data.
     *
     * @param vector destination
     * @param speed  mm per minute
     */
    public Observable<ResponseStructure> gotoAbsolutePosition(Vector vector, int speed) {
        return gotoAbsolutePosition(vector, speed, 0);
    }

    /**
     * Go to absolute position and pull coordinateSystem to refresh local data.
     *
     * @param vector    destination
     * @param speed     mm per minute
     * @param coordType 0 (logical) or 1 (physical)
     */
    public Observable<ResponseStructure> gotoAbsolutePosition(Vector vector, int speed, int coordType) {
        BaseStructure moveStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("movement", new ArrayProp<CoordinateStructure>());
                addProp("speed", new UInt16Prop());
                addProp("coordType", new UInt8Prop());
            }
        };
        moveStructure.getProp("movement").setValue(StructureVectorMapper.vectorToStructureList(vector));
        moveStructure.getProp("speed").setValue(speed);
        moveStructure.getProp("coordType").setValue(coordType);

        return mConnectionController.request(0x01, 0x34, moveStructure, new ResponseStructure())
                .doOnNext(response -> pullCoordinate());
    }

    public Observable<Integer> homeIfNotYet(int axis) {
        if (mStatusSubject.getValue().isHomed) {
            // Already homed, return directly.
            return Observable.just(0);
        } else {
            return home(axis);
        }
    }

    public Observable<Integer> home(int axis) {
        return realHome(axis).doOnNext(result -> {
            pullCoordinate().subscribe();
        });
    }

    public Observable<Integer> home(int axis, boolean isForce) {
        if (isForce) {
            return home(axis);
        } else {
            return homeIfNotYet(axis);
        }
    }

    private Observable<Integer> realHome(int axis) {
        if (mHomeResultSubject != null) {
            mHomeResultSubject.onComplete();
            mHomeResultSubject = null;
        }
        mHomeResultSubject = BehaviorSubject.create();
        return mConnectionController.request(0x01, 0x35, new UInt8Prop(axis), new ResponseStructure())
                .flatMap(response -> mHomeResultSubject.hide());
    }

    public Observable<ResponseStructure> MoveRelativeHome(Vector vector, int speed) {
        BaseStructure moveStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("axis", new UInt8Prop());
                addProp("value", new FloatProp());
                addProp("speed", new UInt16Prop());
            }
        };
        moveStructure.getProp("axis").setValue(0);
        moveStructure.getProp("value").setValue(vector.getX());
        moveStructure.getProp("speed").setValue(speed);

        return mConnectionController.request(0x01, 0x3c, moveStructure, new ResponseStructure<>());
    }

    /*---------------------------LinearModule-------------------------*/
    public LinearModule getLinearModule(int moduleId) {
        for (Module module : mInfoSubject.getValue().getModuleList()) {
            if (module instanceof LinearModule && module.getModuleInfo().getModuleIndex() == moduleId)
                return (LinearModule) module;
        }
        return null;
    }

    public Observable<ResponseStructure> SetLimitEnable(boolean isEnable) {
        return mConnectionController.request(0x13, 0x02, new BoolProp(isEnable), new ResponseStructure());
    }

    public void subscribeLinearLimitStatus() {
        SubscribeStructure structure = new SubscribeStructure(0x13, 0xa0, 1000);
        mConnectionController.request(0x01, 0x00, structure, new ResponseStructure<>()).subscribe();
    }

    public void unsubscribeLinearLimitStatus() {
        SubscribeStructure structure = new SubscribeStructure(0x13, 0xa0, 1000);
        mConnectionController.request(0x01, 0x01, structure, new ResponseStructure<>()).subscribe();
    }

    public Observable<List<LinearLimit>> getLinearLimitStateObservable() {
        return mConnectionController.watch(0x13, 0xa0, new ResponseStructure<>(new ArrayProp<>(new LinearLimit())))
                .map(response -> response.dataProp.getValue());
    }


    /*---------------------------heatedBed-------------------------*/
    public HeatedBed getHeatedBed() {
        if (mInfoSubject.getValue() == null || mInfoSubject.getValue().getModuleList() == null)
            return null;
        for (Module module : mInfoSubject.getValue().getModuleList()) {
            if (module instanceof HeatedBed)
                return (HeatedBed) module;
        }
        return null;
    }

    /*---------------------------AirPurifier-------------------------*/
    // mAirPurifier
    public AirPurifier getAirPurifier() {
        for (Module module : mInfoSubject.getValue().getModuleList()) {
            if (module instanceof AirPurifier)
                return (AirPurifier) module;
        }
        return null;
    }

    /*---------------------------Enclosure-------------------------*/
    public Enclosure getEnclosure() {
        for (Module module : mInfoSubject.getValue().getModuleList()) {
            if (module instanceof Enclosure)
                return (Enclosure) module;
        }
        return null;
    }

    /*---------------------------RotaryModule-------------------------*/
    public RotaryModule getRotaryModule() {
        for (Module module : mInfoSubject.getValue().getModuleList()) {
            if (module instanceof RotaryModule)
                return (RotaryModule) module;
        }
        return null;
    }

    /*--------------------------- DryBox -------------------------*/
    public DryBox getDryBox() {
        for (Module module : mInfoSubject.getValue().getModuleList()) {
            if (module instanceof DryBox)
                return (DryBox) module;
        }
        return null;
    }

    /*---------------------------To be processed-------------------------*/

    public void setHeartbeatEnabled(boolean enabled) {
    }

    public Observable<Short> watchPacketIndexRequest() {
        return null;
    }

    public Observable<Boolean> startUpdate() {
        return null;
    }

    public void sendUpdatePackage(byte opCode, short index, byte[] content) {
    }


    public Observable<Boolean> getFilamentObservable() {
        // TODO filament
        return Observable.just(false);
    }

    public void clearFilamentOutFlag() {
    }

    public Observable<Boolean> getPowerOutageObservable() {
        return null;
    }

    public boolean isFilamentOut() {
        return false;
    }

    public Observable<Boolean> getMotorStateObservable() {
        return getRawMotorState()
                .flatMap((Function<ResponseStructure<ArrayProp<Motor>>, ObservableSource<Boolean>>) structure -> {
                    if (!structure.isSuccess()) throw new Exception("Fetch motor state fail!");
                    List<Motor> motors = structure.dataProp.getValue();
                    Map<Integer, Boolean> motorStateMap = new HashMap<>();
                    for (Motor motor : motors) {
                        int axis = motor.getAxis();
                        boolean state = motor.getState();
                        motorStateMap.put(axis, state);
                    }
                    //noinspection ConstantConditions
                    boolean isMotorOn = motorStateMap.get(0) && motorStateMap.get(1) && motorStateMap.get(2) && motorStateMap.get(6);
                    return Observable.just(isMotorOn);
                });
    }

    private Observable<ResponseStructure<ArrayProp<Motor>>> getRawMotorState() {
        ResponseStructure<ArrayProp<Motor>> structure = new ResponseStructure<>();
        structure.dataProp = new ArrayProp<>(new Motor());
        return mConnectionController.request(0x01, 0x37, null, structure);
    }

    public Observable<ResponseStructure<IStructure>> controlSwitchMotor(boolean isChecked) {
        ResponseStructure<IStructure> structure = new ResponseStructure<>();
        ArrayProp<Motor> params = new ArrayProp<>();
        params.addElement(new Motor(0, isChecked));
        params.addElement(new Motor(1, isChecked));
        params.addElement(new Motor(2, isChecked));
        params.addElement(new Motor(6, isChecked));
        return mConnectionController.request(0x01, 0x38, params, structure)
                .doOnNext(resStructure -> pullCoordinate().subscribe());
    }

    public void subscribeCoordinate() {
        SubscribeStructure params = new SubscribeStructure(0x01, 0xa2, 1000);
        ResponseStructure<IStructure> responseStructure = new ResponseStructure<>();
        mDisposables.add(mConnectionController.request(0x01, 0x00, params, responseStructure)
                .subscribe(response -> {
                }, LogHelper::log));
    }

    public void unSubscribeCoordinate() {
        SubscribeStructure params = new SubscribeStructure(0x01, 0xa2, 1000);
        ResponseStructure<IStructure> responseStructure = new ResponseStructure<>();
        mDisposables.add(mConnectionController.request(0x01, 0x01, params, responseStructure)
                .subscribe(response -> {
                }, LogHelper::log));
    }

    /**
     * Get coordinate using originOffset or not.
     */
    public Observable<MachineStatus> getCachedCoordinateObservable() {
        return mMachine.getMachineStatusSubjectHolder().getObservable();
    }

    public Vector getCachedCoordinate() {
        return mMachine.getMachineStatusSubjectHolder().getValue().currentPosition;
    }

    public Observable<Vector> getCurrentCoordinateObservable() {
        return getCurrentCoordinateObservable(0);
    }

    public Observable<Vector> getCurrentCoordinateObservable(int type) {
        return pullCoordinate(type).map(response -> StructureVectorMapper.structureListToVector(response.dataProp.getCoordinates()));
    }

    private Vector applyOffsetToVector(Vector currentPosition, Vector originOffset) {
        Vector vector = new Vector();
        if (currentPosition == null || originOffset == null) return vector;
        vector.setX(currentPosition.getX() - originOffset.getX());
        vector.setY(currentPosition.getY() - originOffset.getY());
        vector.setZ(currentPosition.getZ() - originOffset.getZ());
        vector.setB(currentPosition.getB() - originOffset.getB());
        vector.setX2(currentPosition.getX2() - originOffset.getX2());
        return vector;
    }

    public void onHomeResult(int commandSet, int commandId, int sequence, UInt8Prop homeResult) {
        mConnectionController.sendResponse(commandSet, commandId, sequence, new UInt8Prop(0));
        // homeResult: 0 success; 1 timeout
        mHomeResultSubject.onNext(homeResult.getValue());
    }

    public Observable<ResponseStructure> goToBoundaryVertex(int index, ModelBoundary boundary, int speed) {
        if (index == 5) {
            return goToXYOrigin();
        } else {
            // 0 -> 1 -> 2 -> 3 -> 4(0) -> 5(origin)
            final float[] point = boundary.getBoundaryPoint(index % 4);
            Vector vector = new Vector();
            vector.setX(point[0]);
            vector.setY(point[1]);
            return mMachine.getMachineController().gotoAbsolutePosition(vector, speed)
                    .flatMap(response -> goToBoundaryVertex(index + 1, boundary, speed));
        }
    }

    /**
     * Replacing modules has 2 work mode
     * 1. power off all except 4 pin(now add-ons on 4 pin)
     * 2. power off all with no exception
     *
     * @param keep4pinOn except 4 pin while power off
     */
    public Observable<ResponseStructure<IStructure>> startReplacePartsMode(boolean keep4pinOn) {
        return mConnectionController.request(0x01, 0x3d, new BoolProp(keep4pinOn), new ResponseStructure<>());
    }

    public void shutdownWorkingParts() {

        switch (mMachine.getMachineInfoSubjectHolder().getValue().workType) {
            case FDM:
                mDisposables.add(mMachine.getFDMController()
                        .stopExtruderHeat()
                        .subscribe(responseStructure -> {
                        }, LogHelper::log));
                break;
            case LASER:
                mDisposables.add(mMachine.getLaserController()
                        .setLaserPower(0, 0)
                        .subscribe(responseStructure -> {
                        }, LogHelper::log));
                break;
            case CNC:
                mDisposables.add(mMachine.getCNCController()
                        .switchCNC(0, false)
                        .subscribe(responseStructure -> {
                        }, LogHelper::log));
                break;
            default:
                break;
        }
        if (mMachine.getMachineController().getHeatedBed() != null) {
            mDisposables.add(mMachine.getMachineController()
                    .getHeatedBed()
                    .setAllTargetTemperature(0)
                    .subscribe(responseStructure -> {
                    }, LogHelper::log));
        }
    }

    public void setRestartMachineResult(ResponseStructure<IStructure> responseStructure) {
        mRestartMachineSubject.onNext(responseStructure);
    }


    public Observable<ResponseStructure> subscribeMotorPotentialState() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0x01, 0x37, 1000);
        return mConnectionController.request(0x01, 0x00, subscribeStructure, new ResponseStructure())
                .doOnNext(responseStructure -> {
                    if (mMotorPotentialState == null) {
                        mMotorPotentialState = BehaviorSubject.create();
                    }
                    ResponseStructure<ArrayProp<Motor>> structure = new ResponseStructure<>();
                    structure.dataProp = new ArrayProp<>(new Motor());
                    MotorPotentialStateSub = mConnectionController.watch(0x01, 0x37, structure)
                            .subscribe(response -> {
                                List<Motor> motors = response.dataProp.getValue();
                                Map<Integer, Boolean> motorStateMap = new HashMap<>();
                                for (Motor motor : motors) {
                                    int axis = motor.getAxis();
                                    boolean state = motor.getState();
                                    motorStateMap.put(axis, state);
                                }
                                //noinspection ConstantConditions
                                mMotorPotentialState.onNext(motorStateMap.get(0) && motorStateMap.get(1) && motorStateMap.get(2) && motorStateMap.get(6));
                            });
                });
    }


    public Observable<ResponseStructure> unSubscribeMotorPotentialState() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0x01, 0x37, 0);
        return mConnectionController.request(0x01, 0x01, subscribeStructure, new ResponseStructure())
                .doOnNext(responseStructure -> {
                    mMotorPotentialState.onComplete();
                    mMotorPotentialState = null;
                    if (MotorPotentialStateSub != null && !MotorPotentialStateSub.isDisposed()) {
                        MotorPotentialStateSub.dispose();
                    }
                });
    }

    public Observable<Boolean> getMotorPotentialStateObservable() {
        return mMotorPotentialState.hide();
    }

    public BehaviorSubject<Vector> getRelativeHomeLocationSubject() {
        return mRelativeHomeLocation;
    }

    public Observable<ResponseStructure> subscribeRelativeHomeLocation() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0x01, 0xa3, 1000);
        return mConnectionController.request(0x01, 0x00, subscribeStructure, new ResponseStructure())
                .doOnNext(responseStructure -> {
                    if (mRelativeHomeLocation == null) {
                        mRelativeHomeLocation = BehaviorSubject.create();
                    }
                    ResponseStructure<ArrayProp<CoordinateStructure>> structure = new ResponseStructure<>();
                    structure.dataProp = new ArrayProp<>(new CoordinateStructure());
                    RelativeHomeLocationSub = mConnectionController.watch(0x01, 0xa3, structure)
                            .subscribe(response -> {
                                List<CoordinateStructure> value = response.dataProp.getValue();
                                Vector vector = StructureVectorMapper.structureListToVector(value);
                                mRelativeHomeLocation.onNext(vector);
                            }, LogHelper::log);
                });
    }

    public Observable<ResponseStructure> unSubscribeRelativeHomeLocation() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0x01, 0xa3, 0);
        return mConnectionController.request(0x01, 0x01, subscribeStructure, new ResponseStructure())
                .doOnNext(responseStructure -> {
                    mRelativeHomeLocation.onComplete();
                    mRelativeHomeLocation = null;
                    if (RelativeHomeLocationSub != null && !RelativeHomeLocationSub.isDisposed()) {
                        RelativeHomeLocationSub.dispose();
                    }
                });
    }

    public Observable<ResponseStructure> getInterruptAutoLevelingObservable() {
        return mConnectionController.request(0xa0, 0x09, null, new ResponseStructure());
    }

    public Observable<ResponseStructure<IStructure>> saveVibrationCompensationFreq(int type, float freq) {
        BaseStructure frequencyConfigStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("type", new UInt8Prop(type));
                addProp("frequency", new FloatProp(freq));
            }
        };
        return mConnectionController.request(0x01, 0x3e, frequencyConfigStructure, new ResponseStructure<>());
    }

    public Observable<ResponseStructure<FrequencyConfig>> getVibrationCompensationFreq(int type) {
        return mConnectionController.request(0x01, 0x3f, new UInt8Prop(type), new ResponseStructure<>(new FrequencyConfig()));
    }

    public Observable<ResponseStructure<IStructure>> setVibrationCompensationEnabled(boolean enabled) {
        return mConnectionController.request(0x01, 0x40, new BoolProp(enabled), new ResponseStructure<>());
    }

    public Observable<ResponseStructure<IStructure>> getVibrationCompensationEnabled() {
        return mConnectionController.request(0x01, 0x41, null, new ResponseStructure<>(new BoolProp()));
    }

    public void subscribeVibrationCompensationConfig() {
        mConnectionController.request(0x01, 0x00, new SubscribeStructure(0x01, 0xa5, 1000), new ResponseStructure<>()).subscribe();
    }

    public void unsubscribeVibrationCompensationConfig() {
        mConnectionController.request(0x01, 0x01, new SubscribeStructure(0x01, 0xa5, 1000), new ResponseStructure<>()).subscribe();
    }

    public Observable<VibrationCompensationConfig> watchVibrationCompensationConfig() {
        return mConnectionController.watch(0x01, 0xa5, new ResponseStructure<>(new VibrationCompensationConfig()))
                .filter(ResponseStructure::isSuccess)
                .map(response -> response.dataProp);
    }

    public Observable<ResponseStructure<IStructure>> setHomeStallDetectionEnabled(boolean enabled) {
        return mConnectionController.request(0x01, 0x42, new BoolProp(enabled), new ResponseStructure<>());
    }

    public Observable<ResponseStructure<IStructure>> getHomeStallDetectionEnabled() {
        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("home_stall_detection_status", new UInt8Prop());
            }
        };
        ResponseStructure responseStructure = new ResponseStructure();
        responseStructure.resultProp = new UInt8Prop();
        responseStructure.dataProp = baseStructure;
        return mConnectionController.request(0x01, 0x43, null, responseStructure);
    }

    public Observable<ResponseStructure> setPlatformHeight(float height) {
        return mConnectionController.request(0x01, 0x44, new FloatProp(height), new ResponseStructure<>());
    }

    public Observable<ResponseStructure<IStructure>> getPlatformHeight() {
        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("machine_platform_height", new FloatProp());
            }
        };
        ResponseStructure responseStructure = new ResponseStructure();
        responseStructure.resultProp = new UInt8Prop();
        responseStructure.dataProp = baseStructure;
        return mConnectionController.request(0x01, 0x45, null, responseStructure);
    }

    public void saveConfigToFlash() {
        mDisposables.add(mConnectionController.request(0x01, 0x24, null, new ResponseStructure<>()).subscribe(result -> {
        }, LogHelper::log));
    }

    public Observable<ResponseStructure> requestBulkUnsubscribe(int hostId) {
        return mConnectionController.request(0x01, 0x07, new UInt8Prop(hostId), new ResponseStructure());
    }
}
