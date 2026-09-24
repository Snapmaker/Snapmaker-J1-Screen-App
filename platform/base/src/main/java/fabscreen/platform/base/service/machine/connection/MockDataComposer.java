package fabscreen.platform.base.service.machine.connection;

import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_ENCLOSURE_A400;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_HEATED_BED_J1;

import android.os.SystemClock;
import android.util.SparseArray;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.connection.mock.DebugModule;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockAirPurifier;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockCNCToolHead;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockDryBox;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockEnclosure;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockExtruder;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockFan;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockFdmToolHead;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockHeatedBed;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockLaserToolHead;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockLinear;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockModule;
import fabscreen.platform.base.service.machine.connection.mock.entity.MockZone;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.entity.module.AirPurifier;
import fabscreen.platform.base.service.machine.entity.module.HeatedBed;
import fabscreen.platform.base.service.machine.entity.parts.DryBoxStatus;
import fabscreen.platform.base.service.machine.entity.parts.Extruder;
import fabscreen.platform.base.service.machine.entity.parts.Fan;
import fabscreen.platform.base.service.machine.entity.parts.LaserTube;
import fabscreen.platform.base.service.machine.entity.parts.Motor;
import fabscreen.platform.base.service.machine.entity.toolhead.CNCToolhead;
import fabscreen.platform.base.service.machine.entity.toolhead.FdmToolhead;
import fabscreen.platform.base.service.machine.protocol.IProtocol;
import fabscreen.platform.base.service.machine.protocol.SACPProtocol;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.CoordinateStructure;
import fabscreen.platform.base.service.machine.structure.ExtruderOffsetStructure;
import fabscreen.platform.base.service.machine.structure.FDMZOffsetStructure;
import fabscreen.platform.base.service.machine.structure.LaserSafetyStateStructure;
import fabscreen.platform.base.service.machine.structure.MachineFault;
import fabscreen.platform.base.service.machine.structure.MachineProductInfo;
import fabscreen.platform.base.service.machine.structure.OpenDoorDetectionState;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.StructureVectorMapper;
import fabscreen.platform.base.service.machine.structure.ZOffsetInfo;
import fabscreen.platform.base.service.machine.structure.print.BatchGcodeRequest;
import fabscreen.platform.base.service.machine.structure.prop.ArrayProp;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.FloatProp;
import fabscreen.platform.base.service.machine.structure.prop.StringProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt16Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt32Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.lib.SubjectHolder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import okio.Buffer;


public class MockDataComposer {
    private int mId = -1;
    private final IProtocol mProtocol;
    private IPreferences mPreferences;
    private SparseArray<Disposable> mPushDisposables = new SparseArray<>();
    private MockMachine mMockMachine;

    BehaviorSubject<byte[]> mDataSubject = BehaviorSubject.create();
    SubjectHolder<byte[]> mDataSubjectHolder = new SubjectHolder<>(mDataSubject);

    public MockDataComposer(IProtocol protocol, IPreferences preferences) {
        mProtocol = protocol;
        mPreferences = preferences;
        initMockMachine(mPreferences.getHelper().getDebugMachineSeries(), mPreferences.getHelper().getDebugMachineModel(), mPreferences.getHelper().getDebugModuleList());
//        initMockMachine(IMachine.MachineSeries.J, IMachine.MachineModel.J1, mPreferences.getHelper().getDebugModuleList());
    }

    int gridPoints = 0;
    int nowPoints = 0;
    float zOffset = 0;
    float mVibrationFrequency = 30f;
    int mNoiseOption = 1;

    private void stopPush(int cmdSet, int cmdId) {
        int pushKey = getPushKey(cmdSet, cmdId);
        Disposable disposable = mPushDisposables.get(pushKey);
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    private int getId() {
        if (mId < 0xff) {
            mId++;
        } else {
            throw new IllegalStateException("ModuleId resource not sufficient");
        }
        return mId;
    }

    public SubjectHolder<byte[]> getDataSubjectHolder() {
        return mDataSubjectHolder;
    }

    private int getPushKey(int cmdSet, int cmdId) {
        return cmdSet * 1000 + cmdId;
    }

    List<XYLeveling> xyLevelings;

    public void initMockMachine(int seriesId, int modelId, Set<String> debugModuleList) {
        mMockMachine = new MockMachine(seriesId, modelId);
//         If it is J1, it needs to realize 3DP head and hot bed
        if (seriesId == IMachine.MachineSeries.J) {
            for (int i = 0; i < 2; i++) {
                mMockMachine.addModel(Module.ModuleType.HEAD_3DP, i);
            }
            mMockMachine.addModel(ADDON_HEATED_BED_J1, 0);
            mMockMachine.addModel(ADDON_ENCLOSURE_A400, 0);
            return;
        }
        if (debugModuleList == null) return;
        for (String s : debugModuleList) {
            DebugModule debugModule = new DebugModule(s);
            mMockMachine.addModel(debugModule.moduleId, debugModule.index);
            if (debugModule.moduleId == Module.ModuleType.HEAD_LASER || debugModule.moduleId == Module.ModuleType.HEAD_LASER_10W) {
                initCameraMac();
            }
        }
    }

    private void initCameraMac() {
        SACPProtocol.MessageHeader header = new SACPProtocol.MessageHeader();
        header.attribute = SACPProtocol.Attribute.ACK;
        header.commandSet = 0x12;
        header.commandId = 0x06;
        header.receiverId = SACPProtocol.CommunicationId.SCREEN;
        header.senderId = SACPProtocol.CommunicationId.CONTROLLER;
        Disposable disposable = Observable.timer(3, TimeUnit.SECONDS)
                .subscribe(aLong -> {
                    BaseStructure baseStructure = new BaseStructure() {
                        @Override
                        protected void init() {
                            addProp("key", new UInt8Prop());
                            addProp("status", new UInt8Prop());
                            addProp("macAddress", new StringProp());
                        }
                    };
                    baseStructure.getProp("key").setValue(mMockMachine.getLaserToolHead().key);
                    baseStructure.getProp("status").setValue(mMockMachine.getLaserToolHead().moduleState);
                    baseStructure.getProp("macAddress").setValue("macAddress");

                    mDataSubject.onNext(mProtocol.encode(header, baseStructure));
                }, LogHelper::log);
    }

    private void startPush(int cmdSet, int cmdId, int pushInterval, int senderId, int receiverId) {
//        Logger.d("sacp-debug: starting push, set=%1$s, id=%2$s, interval=%3$d", Integer.toHexString(cmdSet), Integer.toHexString(cmdId), pushInterval);
//        Logger.d("mock data: sender id: %1$s, receiver id: %2$s", senderId, receiverId);
        int pushKey = getPushKey(cmdSet, cmdId);
        SACPProtocol.MessageHeader header = new SACPProtocol.MessageHeader();
        header.attribute = SACPProtocol.Attribute.ACK;
        header.commandSet = cmdSet;
        header.commandId = cmdId;
        header.receiverId = receiverId;
        header.senderId = senderId;
        Disposable disposable;
        if (cmdSet == 0x01 && cmdId == 0xa0) {
            // heartbeat
            disposable = Observable.interval(0, pushInterval, TimeUnit.MILLISECONDS)
                    .subscribe(aLong -> {
                        ResponseStructure responseStructure = new ResponseStructure();
                        responseStructure.dataProp = new UInt8Prop();
                        mDataSubject.onNext(mProtocol.encode(header, responseStructure));
                    }, LogHelper::log);
            mPushDisposables.put(pushKey, disposable);
        }
        if (cmdSet == 0x01 && cmdId == 0xa1) {
            disposable = Observable.interval(0, pushInterval, TimeUnit.MILLISECONDS)
                    .subscribe(aLong -> {
                        ResponseStructure responseStructure = new ResponseStructure();
                        BaseStructure baseStructure = new BaseStructure() {
                            @Override
                            protected void init() {
                                addProp("logLever", new UInt8Prop());
                                addProp("log", new StringProp());
                            }
                        };
                        baseStructure.getProp("logLever").setValue(0);
                        baseStructure.getProp("log").setValue("test");
                        responseStructure.dataProp = baseStructure;
                        mDataSubject.onNext(mProtocol.encode(header, responseStructure));
                    }, LogHelper::log);
            mPushDisposables.put(pushKey, disposable);
        }
        if (cmdSet == 0x01 && cmdId == 0xa2) {
            disposable = Observable.interval(0, pushInterval, TimeUnit.MILLISECONDS)
                    .subscribe(aLong -> {
                        ResponseStructure responseStructure = new ResponseStructure();
                        responseStructure.dataProp = mMockMachine.getCoordinateSystemInformation();
//                        Logger.d("sacp-debug: push coordinate: %s", responseStructure.toString());
                        mDataSubject.onNext(mProtocol.encode(header, responseStructure));
                    }, LogHelper::log);
            mPushDisposables.put(pushKey, disposable);
        }

        if (cmdSet == 0x01 && cmdId == 0xa3) {
            disposable = Observable.interval(0, pushInterval, TimeUnit.MILLISECONDS)
                    .subscribe(aLong -> {
                        ResponseStructure responseStructure = new ResponseStructure(mMockMachine.getEmergencyInfo());
                        mDataSubject.onNext(mProtocol.encode(header, responseStructure));
                    }, LogHelper::log);
            mPushDisposables.put(pushKey, disposable);
        }

        if (cmdSet == 0x10 && cmdId == 0xa0) {
            List<MockFdmToolHead> fdmToolHeads = mMockMachine.getFDMToolHead();
            disposable = Observable.interval(0, pushInterval, TimeUnit.MILLISECONDS)
                    .subscribe(aLong -> {
                        for (int i = 0; i < fdmToolHeads.size(); i++) {
                            MockFdmToolHead mockFdmToolHead = fdmToolHeads.get(i);
                            BaseStructure baseStructure = new BaseStructure() {
                                @Override
                                protected void init() {
                                    addProp("modelId", new UInt8Prop());
                                    addProp("extruders", new ArrayProp<>(new Extruder()));
                                }
                            };
                            baseStructure.getProp("modelId").setValue(mockFdmToolHead.key);
                            ArrayList<Extruder> extruders = new ArrayList<>();
                            for (int j = 0; j < mockFdmToolHead.getExtruderList().size(); j++) {
                                extruders.add(mockFdmToolHead.getExtruderList().get(j).getExtruder());
                            }
                            baseStructure.getProp("extruders").setValue(extruders);
                            ResponseStructure<IStructure> iStructureResponseStructure = new ResponseStructure<>();
                            iStructureResponseStructure.dataProp = baseStructure;
                            mDataSubject.onNext(mProtocol.encode(header, iStructureResponseStructure));
                        }
                    }, LogHelper::log);
            mPushDisposables.put(pushKey, disposable);
        }

        if (cmdSet == 0x11 && cmdId == 0xa0) {
            MockCNCToolHead cncToolHead = mMockMachine.getCncToolHead();
            disposable = Observable.interval(0, pushInterval, TimeUnit.MILLISECONDS)
                    .subscribe(aLong -> {
                        ResponseStructure<CNCToolhead.CNCToolheadInfo> hesdBedResponse = new ResponseStructure<>();
                        hesdBedResponse.dataProp = cncToolHead.getCncToolHeadInfo();
                        mDataSubject.onNext(mProtocol.encode(header, hesdBedResponse));
                    }, LogHelper::log);
            mPushDisposables.put(pushKey, disposable);
        }

        if (cmdSet == 0x12 && cmdId == 0xa0) {
            MockLaserToolHead laserToolHead = mMockMachine.getLaserToolHead();
            disposable = Observable.interval(0, pushInterval, TimeUnit.MILLISECONDS)
                    .subscribe(aLong -> {
                        ResponseStructure<LaserSafetyStateStructure> hesdBedResponse = new ResponseStructure<>();
                        hesdBedResponse.dataProp = laserToolHead.getLaserSafetyStateStructure();
                        mDataSubject.onNext(mProtocol.encode(header, hesdBedResponse));
                    }, LogHelper::log);
            mPushDisposables.put(pushKey, disposable);
        }

        if (cmdSet == 0x12 && cmdId == 0xa1) {
            MockLaserToolHead laserToolHead = mMockMachine.getLaserToolHead();
            disposable = Observable.interval(0, pushInterval, TimeUnit.MILLISECONDS)
                    .subscribe(aLong -> {
                        ResponseStructure<LaserTube> hesdBedResponse = new ResponseStructure<>();
                        hesdBedResponse.dataProp = laserToolHead.getLaserTube();
                        mDataSubject.onNext(mProtocol.encode(header, hesdBedResponse));
                    }, LogHelper::log);
            mPushDisposables.put(pushKey, disposable);
        }

        if (cmdSet == 0x14 && cmdId == 0xa0) {
            MockHeatedBed heatedBed = mMockMachine.getHeatedBed();
            disposable = Observable.interval(0, pushInterval, TimeUnit.MILLISECONDS)
                    .subscribe(aLong -> {
                        ResponseStructure<HeatedBed.HeatedBedStatus> hesdBedResponse = new ResponseStructure<>();
                        hesdBedResponse.resultProp = new UInt8Prop(0);
                        hesdBedResponse.dataProp = new HeatedBed.HeatedBedStatus();
                        hesdBedResponse.dataProp.setKey(heatedBed.key);
                        ArrayList<HeatedBed.ZoneInfo> zoneInfos = new ArrayList<>();
                        for (int i = 0; i < heatedBed.getMockZonList().size(); i++) {
                            zoneInfos.add(heatedBed.getMockZonList().get(i).getZoneInfo());
                        }
                        hesdBedResponse.dataProp.setZoneList(zoneInfos);
                        mDataSubject.onNext(mProtocol.encode(header, hesdBedResponse));
                    }, LogHelper::log);
            mPushDisposables.put(pushKey, disposable);
        }

        if (cmdSet == 0x17 && cmdId == 0xa0) {
            MockAirPurifier airPurifier = mMockMachine.getAirPurifier();
            disposable = Observable.interval(0, pushInterval, TimeUnit.MILLISECONDS)
                    .subscribe(aLong -> {
                        ResponseStructure<AirPurifier.AirPurifierStatus> hesdBedResponse = new ResponseStructure<>();
                        hesdBedResponse.dataProp = airPurifier.getInfo();
                        mDataSubject.onNext(mProtocol.encode(header, hesdBedResponse));
                    }, LogHelper::log);
            mPushDisposables.put(pushKey, disposable);
        }

        if (cmdSet == 0x18 && cmdId == 0xa0) {
            MockDryBox mockDryBox = mMockMachine.getDryBox();
            disposable = Observable.interval(0, pushInterval, TimeUnit.MILLISECONDS)
                    .subscribe(aLong -> {
                        ResponseStructure<DryBoxStatus> hesdBedResponse = new ResponseStructure<>();
                        hesdBedResponse.dataProp = mockDryBox.getDryBoxStatus();
                        mDataSubject.onNext(mProtocol.encode(header, hesdBedResponse));
                    }, LogHelper::log);
            mPushDisposables.put(pushKey, disposable);
        }

        if (cmdSet == 0xa0 && cmdId == 0xa0) {
            final int[] times = {0};
            disposable = Observable.interval(0, pushInterval, TimeUnit.MILLISECONDS)
                    .subscribe(aLong -> {
                        ResponseStructure<BaseStructure> responseStructure = new ResponseStructure<>();
                        responseStructure.resultProp = new UInt8Prop(0);
                        BaseStructure baseStructure = new BaseStructure() {
                            @Override
                            protected void init() {
                                addProp("index", new UInt8Prop());
                                addProp("heightDifference", new FloatProp());
                            }
                        };
                        baseStructure.getProp("index").setValue(0);
                        float random = (((int) (Math.random() * 50)) * 1000 / 1000f - 25) / 10;
                        if ((++times[0] % 3) == 0)
                            random = 0;
                        baseStructure.getProp("heightDifference").setValue(random);
                        responseStructure.dataProp = baseStructure;
                        mDataSubject.onNext(mProtocol.encode(header, responseStructure));
                    }, LogHelper::log);
            mPushDisposables.put(pushKey, disposable);
        }

        if (cmdSet == 0xa0 && cmdId == 0xa1) {
            disposable = Observable.interval(0, pushInterval, TimeUnit.MILLISECONDS)
                    .subscribe(aLong -> {
                        ResponseStructure<BaseStructure> responseStructure = new ResponseStructure<>();
                        responseStructure.resultProp = new UInt8Prop(0);
                        BaseStructure baseStructure = new BaseStructure() {
                            @Override
                            protected void init() {
                                addProp("pointIndex", new UInt8Prop());
                                addProp("pointIndexState", new UInt8Prop());
                            }
                        };
                        baseStructure.getProp("pointIndex").setValue(++nowPoints);
                        baseStructure.getProp("pointIndexState").setValue(0);
                        responseStructure.dataProp = baseStructure;
                        mDataSubject.onNext(mProtocol.encode(header, responseStructure));
                    }, LogHelper::log);
            mPushDisposables.put(pushKey, disposable);
        }

        if (cmdSet == 0xac && cmdId == 0xa5) {
            disposable = Observable.interval(0, pushInterval, TimeUnit.MILLISECONDS)
                    .subscribe(aLong -> {
                        ResponseStructure<UInt32Prop> responseStructure = new ResponseStructure<>();
                        responseStructure.resultProp = new UInt8Prop(0);
                        responseStructure.dataProp = new UInt32Prop(SystemClock.elapsedRealtime() / 1000L);
                        mDataSubject.onNext(mProtocol.encode(header, responseStructure));
                    }, LogHelper::log);
            mPushDisposables.put(pushKey, disposable);
        }

//        case 0xa0:
//
//        break;
//
//        case 0xa1:
//
//        break;
    }

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    public void composeResponseData(SACPProtocol.Packet packet) throws IOException {
        int commandSet = packet.header.commandSet;
        int commandId = packet.header.commandId;
        byte[] payload = packet.payload;
        Buffer buffer = new Buffer();
        buffer.write(payload);
        packet.header.attribute = SACPProtocol.Attribute.ACK;
        int rawSenderId = packet.header.senderId;
        //noinspection UnnecessaryLocalVariable
        int rawReceiverId = packet.header.receiverId;
        packet.header.senderId = rawReceiverId;
        packet.header.receiverId = rawSenderId;
        int key;
        MockModule moduleByKey;
        ResponseStructure responseStructure = new ResponseStructure();
        switch (commandSet) {
            case 0x01:
                switch (commandId) {
                    case 0x00:
                        // TODO: Specific push mechanisms need to be handled
                        int startPushCmdSet = new UInt8Prop().readBufferToValue(buffer);
                        int startPushCmdId = new UInt8Prop().readBufferToValue(buffer);
                        int pushInterval = new UInt16Prop().readBufferToValue(buffer);
                        startPush(startPushCmdSet, startPushCmdId, pushInterval, packet.header.senderId, packet.header.receiverId);
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x01:
                        int stopPushCmdSet = new UInt8Prop().readBufferToValue(buffer);
                        int stopPushCmdId = new UInt8Prop().readBufferToValue(buffer);
                        stopPush(stopPushCmdSet, stopPushCmdId);
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x02:
                        String gcode = new StringProp().readBufferToValue(buffer);
                        Logger.d("Analog reception gcode " + gcode);
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x03:
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x10:
                        int logLevel = new UInt8Prop().readBufferToValue(buffer);
                        Logger.d("Analog reception logLevel " + logLevel);
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x11:
                        int protocolType = new UInt8Prop().readBufferToValue(buffer);
                        Logger.d("Analog reception protocolType " + protocolType);
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x12:
                        int controlInput = new UInt8Prop().readBufferToValue(buffer);
                        Logger.d("Analog reception controlInput  " + controlInput);
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x13:
                        int resetOpt = new UInt8Prop().readBufferToValue(buffer);
                        Logger.d("Analog reception resetOpt " + resetOpt);
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        // machine info
                    case 0x20:
                        responseStructure.dataProp = mMockMachine.getModuleInfos();
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x21:
                        MachineProductInfo machineProductInfo = new MachineProductInfo();
                        machineProductInfo.setSeries(mMockMachine.seriesId);
                        machineProductInfo.setModel(mMockMachine.modelId);
                        machineProductInfo.setControllerFWVersion(mMockMachine.controllerVersion);
                        machineProductInfo.setSn(mMockMachine.serialNo);
                        responseStructure.dataProp = machineProductInfo;
                        byte[] machineInfoBytes = mProtocol.encode(packet.header, responseStructure);
                        mDataSubject.onNext(machineInfoBytes);
                        break;
                    case 0x22:
                        responseStructure.dataProp = mMockMachine.getMachineSizeInfo();
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    // machine status
                    case 0x30:
                        responseStructure.dataProp = mMockMachine.getCoordinateSystemInformation();
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x31:
                        int coordinateSystemId = new UInt8Prop().readBufferToValue(buffer);
                        mMockMachine.updateCoordinateSystem(coordinateSystemId);
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x32:
                        List<CoordinateStructure> coordinateStructures = new ArrayProp<CoordinateStructure>(new CoordinateStructure()).readBufferToValue(buffer);
                        Vector vector = StructureVectorMapper.structureListToVector(coordinateStructures);
                        mMockMachine.setCoordinate(vector);
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x33:
                        Logger.d("不建议使用这个接口哦");
//                        List<MovementStructure> relativeMove = new ArrayProp<MovementStructure>(new MovementStructure(0, 0)).readBufferToValue(buffer);
//                        mMockMachine.machineMoving(0, relativeMove);
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x34:
                        List<CoordinateStructure> absoluteMove = new ArrayProp<>(new CoordinateStructure()).readBufferToValue(buffer);
                        mMockMachine.machineMoving(1, absoluteMove);
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x35:
                        int home = new UInt8Prop().readBufferToValue(buffer);
                        mMockMachine.goHome(home);
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        packet.header.commandId = 0x36;
                        packet.header.receiverId = IProtocol.CommunicationId.SCREEN;
                        packet.header.senderId = IProtocol.CommunicationId.CONTROLLER;
                        packet.header.attribute = IProtocol.Attribute.REQUEST;
                        DelayTimes(packet.header, responseStructure);
//                        mDataSubject.onNext(mProtocol.encode(packet.header, new UInt8Prop(0)));
                        break;
                    case 0x37:
                        // get motor status
                        List<Motor> motors = new ArrayList<>();
                        motors.add(new Motor(0, true));
                        motors.add(new Motor(1, true));
                        motors.add(new Motor(2, true));
                        motors.add(new Motor(6, true));
                        responseStructure.dataProp = new ArrayProp<>(motors);
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x38:
                        responseStructure.resultProp.setValue(0);
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x3c:
                        CoordinateStructure coordinateStructure = new CoordinateStructure().readBufferToValue(buffer);
                        int speed = new UInt16Prop().readBufferToValue(buffer);
                        Logger.d("0x01 0x3c %s,Speed:%d", coordinateStructure, speed);
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x3d:
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x3e:
                        responseStructure.resultProp.setValue(0);
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x3f:
                        BaseStructure baseStructure = new BaseStructure() {
                            @Override
                            protected void init() {
                                addProp("frequency", new FloatProp(mVibrationFrequency));
                            }
                        };
                        responseStructure.dataProp = baseStructure;
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x40:
                        responseStructure.resultProp.setValue(0);
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x41:
                        BaseStructure frequencySubject = new BaseStructure() {
                            @Override
                            protected void init() {
                                addProp("isVibrationResistEnabled", new BoolProp(true));
                            }
                        };
                        responseStructure.dataProp = frequencySubject;
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                    default:
                        break;
                }
                break;
            case 0x04:
                switch (commandId) {
                    case 0x02:
                        BaseStructure baseStructure = new BaseStructure() {
                            @Override
                            protected void init() {
                                addProp("exceptionInfos", new ArrayProp<MachineFault>(new MachineFault()));
                                addProp("machineBehaviorStates", new ArrayProp<UInt8Prop>(new UInt8Prop()));
                            }
                        };
                        responseStructure.dataProp = baseStructure;
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                }
                break;
            // Fdm Toolhead
            case 0x10:
                MockFdmToolHead mockFdmToolHead;
                MockExtruder mockExtruder;
                switch (commandId) {
                    case 0x01:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockFdmToolHead = (MockFdmToolHead) moduleByKey;
                        responseStructure.resultProp = new UInt8Prop(0);
                        FdmToolhead.FdmToolheadStatus fdmToolheadStatus = new FdmToolhead.FdmToolheadStatus();
                        fdmToolheadStatus.setId(mockFdmToolHead.key);
//                        fdmToolheadStatus.setFilamentStatus(mockFdmToolHead.getFilamentStatus());
                        fdmToolheadStatus.setHeadStatus(mockFdmToolHead.getHeadStatus());
                        fdmToolheadStatus.setHeadActive(mockFdmToolHead.isHeadActive());
                        ArrayList<Extruder> extruders = new ArrayList<>();
                        for (int i = 0; i < mockFdmToolHead.getExtruderList().size(); i++) {
                            extruders.add(mockFdmToolHead.getExtruderList().get(i).getExtruder());
                        }
                        fdmToolheadStatus.setExtruderList(extruders);
                        ArrayList<Fan> fans = new ArrayList<>();
                        for (int i = 0; i < mockFdmToolHead.getFanList().size(); i++) {
                            fans.add(mockFdmToolHead.getFanList().get(i).getFan());
                        }
                        fdmToolheadStatus.setFanList(fans);
                        responseStructure.dataProp = fdmToolheadStatus;
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x02:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockFdmToolHead = (MockFdmToolHead) moduleByKey;
                        key = new UInt8Prop().readBufferToValue(buffer);
                        if (mockFdmToolHead.getExtruderList().size() <= key) {
                            // TODO: There is no extruder,
                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockExtruder = mockFdmToolHead.getExtruderList().get(key);
                        mockExtruder.setTargetTemperature(new UInt16Prop().readBufferToValue(buffer));
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x03:
                        // TODO:
                        break;
                    case 0x04:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockFdmToolHead = (MockFdmToolHead) moduleByKey;
                        key = new UInt8Prop().readBufferToValue(buffer);
                        if (mockFdmToolHead.getExtruderList().size() <= key) {
                            // TODO: There is no extruder,
                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockFdmToolHead.setFilamentStatus(new UInt8Prop().readBufferToValue(buffer));
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x05:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockFdmToolHead = (MockFdmToolHead) moduleByKey;
                        key = new UInt8Prop().readBufferToValue(buffer);
                        if (mockFdmToolHead.getExtruderList().size() <= key) {
                            // TODO: There is no extruder,
                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        for (MockExtruder extruder : mockFdmToolHead.getExtruderList()) {
                            extruder.setStatus(0);
                        }
                        mockExtruder = mockFdmToolHead.getExtruderList().get(key);
                        mockExtruder.setStatus(1);
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x06:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockFdmToolHead = (MockFdmToolHead) moduleByKey;
                        key = new UInt8Prop().readBufferToValue(buffer);
                        if (mockFdmToolHead.getFanList().size() <= key) {
                            // TODO: There is no extruder,
                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        MockFan mockfan = mockFdmToolHead.getFanList().get(key);
                        mockfan.setSpeedLevel(new UInt8Prop().readBufferToValue(buffer));
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x07:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {
                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        ((MockFdmToolHead) moduleByKey).setExtruderOffset(new ArrayProp<>(new ExtruderOffsetStructure()).readBufferToValue(buffer));
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x08:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        responseStructure.dataProp = ((MockFdmToolHead) moduleByKey).getExtruderOffsetsInfo();
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x09:
//                        Logger.d("Mock Extruder movement: \tMovementType :%d" +
//                                        "\nLengthIn:%2f\tSpeedIn:%3f " +
//                                        "\nlengthOut：%4d\tSpeedOut:%5d",
//                                new UInt8Prop().readBufferToValue(buffer),
//                                new FloatProp().readBufferToValue(buffer),
//                                new FloatProp().readBufferToValue(buffer),
//                                new FloatProp().readBufferToValue(buffer),
//                                new FloatProp().readBufferToValue(buffer));
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    default:
                        break;

                }
                break;
            // Cnc Toolhead
            case 0x11:
                MockCNCToolHead mockCncToolHead;
                switch (commandId) {
                    case 0x01:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockCncToolHead = (MockCNCToolHead) moduleByKey;
                        responseStructure.dataProp = mockCncToolHead.getCncToolHeadInfo();
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x02:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockCncToolHead = (MockCNCToolHead) moduleByKey;
                        mockCncToolHead.setTargetPower(new UInt8Prop().readBufferToValue(buffer));
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x03:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockCncToolHead = (MockCNCToolHead) moduleByKey;
                        mockCncToolHead.setCurrentSpeed(new UInt32Prop().readBufferToValue(buffer));
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x04:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockCncToolHead = (MockCNCToolHead) moduleByKey;
                        mockCncToolHead.setControlMode(new UInt8Prop().readBufferToValue(buffer));
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    default:
                        break;
                }
                break;
            // Laser Toolhead
            case 0x12:
                MockLaserToolHead mockLaserToolHead;
                switch (commandId) {
                    case 0x01:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockLaserToolHead = (MockLaserToolHead) moduleByKey;
                        responseStructure.dataProp = mockLaserToolHead.getLaserToolHeadInfo();
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x02:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockLaserToolHead = (MockLaserToolHead) moduleByKey;
                        mockLaserToolHead.setTargetPower(new FloatProp().readBufferToValue(buffer));
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x03:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockLaserToolHead = (MockLaserToolHead) moduleByKey;
                        mockLaserToolHead.setBrightness(new UInt8Prop().readBufferToValue(buffer));
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x04:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockLaserToolHead = (MockLaserToolHead) moduleByKey;
                        mockLaserToolHead.setLaserFocalLength(new FloatProp().readBufferToValue(buffer));
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x05:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockLaserToolHead = (MockLaserToolHead) moduleByKey;
                        mockLaserToolHead.setTrecoveryTemperature(new UInt8Prop().readBufferToValue(buffer));
                        mockLaserToolHead.setProtectTemperature(new UInt8Prop().readBufferToValue(buffer));
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    default:
                        break;
                }
                break;
            case 0x13:
                MockLinear mockLinear;
                switch (commandId) {
                    case 0x01:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockLinear = (MockLinear) moduleByKey;
                        responseStructure.dataProp = mockLinear.getInfo();
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x02:
                        mMockMachine.lineCanMake(new BoolProp().readBufferToValue(buffer));
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    default:
                        break;
                }
                break;
            case 0x14:
                MockHeatedBed mockHeatedBed;
                switch (commandId) {
                    case 0x01:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {
                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockHeatedBed = (MockHeatedBed) moduleByKey;
                        responseStructure.dataProp = mockHeatedBed.getBedInfo();
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x02:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockHeatedBed = (MockHeatedBed) moduleByKey;
                        key = new UInt8Prop().readBufferToValue(buffer);
                        if (mockHeatedBed.getMockZonList().size() <= key) {
                            // TODO: There is no extruder,
                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        MockZone mockZone = mockHeatedBed.getMockZonList().get(key);
                        mockZone.setTargetTemperature(new UInt16Prop().readBufferToValue(buffer));
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    default:
                        break;
                }
                break;
            case 0x15:
                MockEnclosure mockEnclosure;
                switch (commandId) {
                    case 0x01:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockEnclosure = (MockEnclosure) moduleByKey;
                        responseStructure.dataProp = mockEnclosure.getBedInfo();
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x02:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockEnclosure = (MockEnclosure) moduleByKey;
                        mockEnclosure.setLedvalue(new UInt8Prop().readBufferToValue(buffer));
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x03:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {
                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockEnclosure = (MockEnclosure) moduleByKey;
                        int type = new UInt8Prop().readBufferToValue(buffer);
                        boolean statue = new BoolProp().readBufferToValue(buffer);
                        mockEnclosure.setDoorDetectionEnabled(new OpenDoorDetectionState(type, statue));
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x04:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockEnclosure = (MockEnclosure) moduleByKey;
                        mockEnclosure.setFanSpeed(new UInt8Prop().readBufferToValue(buffer));
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    default:
                        break;
                }
                break;
            case 0x16:
                break;
            case 0x17:
                MockAirPurifier mockAirPurifier;
                switch (commandId) {
                    case 0x01:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockAirPurifier = (MockAirPurifier) moduleByKey;
                        responseStructure.dataProp = mockAirPurifier.getInfo();
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x02:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockAirPurifier = (MockAirPurifier) moduleByKey;
                        mockAirPurifier.setFanSpeedLevel(new UInt8Prop().readBufferToValue(buffer));
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x03:
                        key = new UInt8Prop().readBufferToValue(buffer);
                        moduleByKey = mMockMachine.getModuleByKey(key);
                        if (moduleByKey == null) {

                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                            break;
                        }
                        mockAirPurifier = (MockAirPurifier) moduleByKey;
                        mockAirPurifier.setBlowerSwitch(new BoolProp().readBufferToValue(buffer));
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    default:
                        break;
                }
                break;
//            case 0x18:
//                MockDryBox mockDryBox;
//                switch (commandId) {
//                    case 0x01:
//                        key = new UInt8Prop().readBufferToValue(buffer);
//                        moduleByKey = mMockMachine.getModuleByKey(key);
//                        if (moduleByKey == null) {
//                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
//                            break;
//                        }
//                        mockDryBox = (MockDryBox) moduleByKey;
//                        responseStructure.dataProp = mockDryBox.getInfo();
//                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
//                        break;
//                    case 0x02:
//                        key = new UInt8Prop().readBufferToValue(buffer);
//                        moduleByKey = mMockMachine.getModuleByKey(key);
//                        if (moduleByKey == null) {
//
//                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
//                            break;
//                        }
//                        mockDryBox = (MockDryBox) moduleByKey;
//                        mockDryBox.setTempTargetChamber(new UInt8Prop().readBufferToValue(buffer));
//                        mockDryBox.setHeatingTime(new UInt16Prop().readBufferToValue(buffer));
//                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
//                        break;
//                    case 0x03:
//                        key = new UInt8Prop().readBufferToValue(buffer);
//                        moduleByKey = mMockMachine.getModuleByKey(key);
//                        if (moduleByKey == null) {
//
//                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
//                            break;
//                        }
//                        mockDryBox = (MockDryBox) moduleByKey;
//                        mockDryBox.setDryState(new UInt8Prop().readBufferToValue(buffer));
//                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
//                        break;
//                    case 0x04:
//                        key = new UInt8Prop().readBufferToValue(buffer);
//                        moduleByKey = mMockMachine.getModuleByKey(key);
//                        if (moduleByKey == null) {
//
//                            mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
//                            break;
//                        }
//                        mockDryBox = (MockDryBox) moduleByKey;
//                        responseStructure.dataProp = new FloatProp(mockDryBox.getTempCurrentOutlet());
//                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
//                        break;
//                    default:
//                        break;
//                }
//                break;
            case 0xa0:
                switch (commandId) {
                    case 0x00:
                        Logger.d("Leveling mode :" + new UInt8Prop().readBufferToValue(buffer));
                        DelayTimes(packet.header, responseStructure);
                        break;
                    case 0x01:
                        Logger.d("calibratePointIndex: %d ,autoCalibrate: %b ", new UInt8Prop().readBufferToValue(buffer), new BoolProp().readBufferToValue(buffer));
                        DelayTimes(packet.header, responseStructure);
                        break;
                    case 0x02:
                        Logger.d("Start detecting height difference between nozzle and hot bed:%b ", new BoolProp().readBufferToValue(buffer));
                        DelayTimes(packet.header, responseStructure);
                        break;
                    case 0x03:
                        gridPoints = new UInt8Prop().readBufferToValue(buffer);
                        nowPoints = 0;
                        Logger.d("gridPoints:%d", gridPoints);
                        DelayTimes(packet.header, responseStructure);
                        break;
                    case 0x04:
                        Logger.d("gridPoints:%d", ++nowPoints);
                        DelayTimes(packet.header, responseStructure);
                        break;
                    case 0x06:
                        Logger.d("Exit and Save:", new BoolProp().readBufferToValue(buffer));
                        DelayTimes(packet.header, responseStructure);
                        break;
                    case 0x07:
                        // TODO: Query master control adjustment state
                        responseStructure.dataProp = new BoolProp();
                        DelayTimes(packet.header, responseStructure);
                        break;
                    case 0x08:
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x10:
                        Logger.d("calibrate Z PointIndex: %d ,", new UInt8Prop().readBufferToValue(buffer));
                        DelayTimes(packet.header, responseStructure);
                        break;
                    case 0x11:
                        Logger.d("statrt calibrate Z PointIndex: %d ,", new UInt8Prop().readBufferToValue(buffer));
                        DelayTimes(packet.header, responseStructure);
                        break;
                    case 0x12:
                    case 0x13:
                        Logger.d("commandId: %d, extruder Index: %d ,", commandId, new UInt8Prop().readBufferToValue(buffer));
                        DelayTimes(packet.header, responseStructure);
                        break;
                    case 0x15:
                        zOffset = new FloatProp().readBufferToValue(buffer);
                        Logger.d("set z offset: %3f", zOffset);
                        DelayTimes(packet.header, responseStructure);
                        break;
                    case 0x16:
                        List<ZOffsetInfo> zOffsetInfoList = new ArrayList<>();
                        ZOffsetInfo info = new ZOffsetInfo();
                        info.setIndex(0);
                        info.setZOffset(zOffset);
                        zOffsetInfoList.add(info);
                        responseStructure.dataProp = new FDMZOffsetStructure(0, zOffsetInfoList);
                        DelayTimes(packet.header, responseStructure);
                        break;
                    case 0x21:
                        Logger.d("Perform xy axis leveling");
                        DelayTimes(packet.header, responseStructure);
                        break;
                    case 0x22:
                        xyLevelings = new ArrayProp<>(new XYLeveling()).readBufferToValue(buffer);
                        DelayTimes(packet.header, responseStructure);
                        break;
                    case 0x23:
                        ArrayProp<IStructure> iStructureArrayProp = new ArrayProp<>();
                        for (int i = 0; i < xyLevelings.size(); i++) {
                            iStructureArrayProp.addElement(xyLevelings.get(i));
                        }
                        responseStructure.dataProp = iStructureArrayProp;
                        DelayTimes(packet.header, responseStructure);
                        break;
                    default:
                        break;
                }
                break;
            case 0xa4:
                switch (commandId) {
                    case 0x00:
                        Logger.d("Set CNC CalibrationMode :" + new UInt8Prop().readBufferToValue(buffer));
                        DelayTimes(packet.header, responseStructure);
                        break;
                    case 0x01:
                        Logger.d("CNC Exit and Save:", new BoolProp().readBufferToValue(buffer));
                        DelayTimes(packet.header, responseStructure);
                        break;
                    default:
                        break;
                }
                break;
            case 0xa8:
                switch (commandId) {
                    case 0x00:
                        Logger.d("Moving coordinates: X:%.3f\tY:.3f\tZ:.3f",
                                new FloatProp().readBufferToValue(buffer),
                                new FloatProp().readBufferToValue(buffer),
                                new FloatProp().readBufferToValue(buffer));
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x01:
                        Logger.d("PatternOffset:%.3f", new FloatProp().readBufferToValue(buffer));
                        AndroidSchedulers.mainThread().scheduleDirect(() -> mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure)), 1000, TimeUnit.MILLISECONDS);
                        break;
                    case 0x02:
                        Logger.d("Set LaserCalibrationMode :" + new UInt8Prop().readBufferToValue(buffer));
                        DelayTimes(packet.header, responseStructure);
                        break;
                    case 0x03:
                        Logger.d("Laser Exit and Save:", new BoolProp().readBufferToValue(buffer));
                        DelayTimes(packet.header, responseStructure);
                        break;
                    default:
                        break;
                }
                break;
            // Print Business Protocol
            // TODO: Implement a printer state machine, which can consume G-code and change print state.
            case 0xac:
                switch (commandId) {
                    // request print gcode file info
                    case 0x00:
                        BaseStructure baseStructure = new BaseStructure() {
                            @Override
                            protected void init() {
                                addProp("md5", new StringProp("c319528c5c360d46031b69d39e01ceb3"));
                                addProp("filename", new StringProp("testFile.gcode"));
                            }
                        };
                        responseStructure.resultProp = new UInt8Prop(0);
                        responseStructure.dataProp = baseStructure;
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    // print issue request
                    case 0x01:
                        break;
                    //  send print G-code batch
                    case 0x02:
                        BatchGcodeRequest request = new BatchGcodeRequest();
                        responseStructure.dataProp = request;
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    // request print start
                    case 0x03:
                        // request print pause
                    case 0x04:
                        // request print resume
                    case 0x05:
                        // request print stop
                    case 0x06:
                        // request print power loss status
                        responseStructure.resultProp = new UInt8Prop(0);
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                    case 0x07:
                        // request print resume from power loss
                    case 0x08:
                    case 0x09:
                        // request print mode
                        break;
                    case 0x1c:
                        mNoiseOption = new UInt8Prop().readBufferToValue(buffer);
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    case 0x1d:
                        responseStructure.dataProp  = new UInt8Prop(mNoiseOption);
                        mDataSubject.onNext(mProtocol.encode(packet.header, responseStructure));
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    private void DelayTimes(IProtocol.MessageHeader header, ResponseStructure responseStructure) {
        compositeDisposable.add(Observable.timer(3, TimeUnit.SECONDS)
                .subscribe(time -> mDataSubject.onNext(mProtocol.encode(header, responseStructure))));
    }

    public class XYLeveling implements IStructure {
        UInt8Prop directionProp;
        FloatProp deviationProp;

        @Override
        public byte[] toByteArray() {
            Buffer buffer = new Buffer();
            buffer.write(directionProp.toByteArray());
            buffer.write(deviationProp.toByteArray());
            return buffer.readByteArray();
        }

        @Override
        public Buffer readBuffer(Buffer buffer) throws IOException {
            directionProp.readBuffer(buffer);
            deviationProp.readBuffer(buffer);
            return buffer;
        }

        @Override
        public String toString() {
            return null;
        }
    }
}
