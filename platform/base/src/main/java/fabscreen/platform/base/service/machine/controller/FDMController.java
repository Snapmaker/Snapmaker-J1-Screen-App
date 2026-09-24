package fabscreen.platform.base.service.machine.controller;

import android.util.SparseArray;

import com.orhanobut.logger.Logger;

import java.util.List;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.entity.Toolhead;
import fabscreen.platform.base.service.machine.entity.toolhead.FdmToolhead;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.DeviationStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.SubscribeStructure;
import fabscreen.platform.base.service.machine.structure.ZOffsetInfo;
import fabscreen.platform.base.service.machine.structure.prop.ArrayProp;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.FloatProp;
import fabscreen.platform.base.service.machine.structure.prop.Int8Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt16Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.lib.SubjectHolder;
import io.reactivex.Observable;

public class FDMController {
    private final MachineConnectionController mConnectionController;
    private int mHeadType = -1;

    // Mapped by moduleIndex.
    SparseArray<FdmToolhead> mToolheads = new SparseArray<>();
    private final SubjectHolder<MachineInfo> mMachineInfoSubjectHolder;

    public FDMController(IMachine machine, MachineConnectionController cc) {
        mConnectionController = cc;
        mMachineInfoSubjectHolder = machine.getMachineInfoSubjectHolder();
    }

    public void addToolHead(FdmToolhead toolheads) {
        mMachineInfoSubjectHolder.getValue().headType = mHeadType = toolheads.getModuleInfo().getModuleId();
        mToolheads.put(toolheads.getModuleInfo().getModuleIndex(), toolheads);
    }

    public int getHeadType() {
        return mHeadType;
    }

    public int getModuleIdFromIndex(int toolHeadIndex) {
        Toolhead toolhead = mToolheads.get(toolHeadIndex);
        if (toolhead == null) {
            Logger.w("getModuleIdFromIndex Error!");
            return -1;
        } else {
            return toolhead.getModuleInfo().getKey();
        }
    }

    public void subscribeExtruderChange() {
        Logger.d("Subscribe extruder change...");
        mToolheads.get(0).subscribeExtruderChange().subscribe();
    }

    public void unSubscribeExtruderChange() {
        mToolheads.get(0).unSubscribeExtruderChange();
    }

    public void subscribeFanChange() {
        Logger.d("Subscribe fdm fan change...");
        mToolheads.get(0).subscribeFanChange().subscribe();
    }

    public void unSubscribeFanChange() {
        mToolheads.get(0).unSubscribeFanChange();
    }


    public SubjectHolder<FdmToolhead.FdmToolheadStatus> getToolheadStatusSubjectHolder() {
        // The default interface and optional parameter interface are available for upper-layer services
        return getToolheadStatusSubjectHolder(0);
    }

    public SubjectHolder<FdmToolhead.FdmToolheadStatus> getToolheadStatusSubjectHolder(int toolheadIndex) {
        // It may be empty. Eg: Input parameter ID is 1 in case of single head and single injection (second execution head)
        return mToolheads.get(toolheadIndex).getToolheadStatusSubjectHolder();
    }

    public Observable<ResponseStructure> setExtruderTemperature(int toolheadIndex, int extruderIndex, int temperature) {
        FdmToolhead fdmToolhead = mToolheads.get(toolheadIndex);
        if (fdmToolhead == null)
            // TODO:no module
            return Observable.just(new ResponseStructure());
        int key = fdmToolhead.getModuleInfo().getKey();

        BaseStructure fdmRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("extruderIndex", new UInt8Prop());
                addProp("temperature", new UInt16Prop());
            }
        };
        fdmRequest.getProp("key").setValue(key);
        fdmRequest.getProp("extruderIndex").setValue(extruderIndex);
        fdmRequest.getProp("temperature").setValue(temperature);
        return mConnectionController.request(0x10, 0x02, fdmRequest, new ResponseStructure());
    }


    public Observable<ResponseStructure> setFilamentSensorStatus(int id, int extruderId, int status) {
        FdmToolhead fdmToolhead = mToolheads.get(id);
        if (fdmToolhead == null)
            return Observable.just(new ResponseStructure());
        int moduleId = fdmToolhead.getModuleInfo().getKey();

        BaseStructure fdmRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("extruderId", new UInt8Prop());
                addProp("status", new UInt8Prop());
            }
        };
        fdmRequest.getProp("key").setValue(moduleId);
        fdmRequest.getProp("extruderId").setValue(extruderId);
        fdmRequest.getProp("status").setValue(status);
        return mConnectionController.request(0x10, 0x04, fdmRequest, new ResponseStructure());
    }

    // FIXME: Why implement this interface?
    public Observable<ResponseStructure> switchToLeftExtruder() {
        FdmToolhead leftToolhead = mToolheads.get(0);
        int leftExtruderId = leftToolhead.getToolheadStatusSubjectHolder().getValue().getExtruderList().get(0).getId();
        return switchExtruder(leftToolhead.getModuleInfo().getKey(), leftExtruderId);
    }

    public Observable<ResponseStructure> switchExtruder(int toolheadIndex, int extruderIndex) {
        FdmToolhead fdmToolhead = mToolheads.get(toolheadIndex);
        if (fdmToolhead == null)
            return Observable.just(new ResponseStructure());
        int key = fdmToolhead.getModuleInfo().getKey();
        BaseStructure fdmRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("toolheadId", new UInt8Prop());
                addProp("extruderId", new UInt8Prop());
            }
        };
        fdmRequest.getProp("toolheadId").setValue(key);
        fdmRequest.getProp("extruderId").setValue(extruderIndex);
        return mConnectionController.request(0x10, 0x05, fdmRequest, new ResponseStructure())
                .flatMap(response -> mToolheads.get(0).requestInfo())
                .flatMap(response -> mToolheads.size() == 2 ? mToolheads.get(1).requestInfo() : Observable.just(response));
    }

    public Observable<ResponseStructure> setFanSpeed(int id, int extruderId, int fanSpeed) {
        FdmToolhead fdmToolhead = mToolheads.get(id);
        if (fdmToolhead == null)
            return Observable.just(new ResponseStructure());
        int moduleId = fdmToolhead.getModuleInfo().getKey();

        BaseStructure fdmRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("modelID", new UInt8Prop());
                addProp("extruderId", new UInt8Prop());
                addProp("fanSpeed", new UInt8Prop());
            }
        };

        fdmRequest.getProp("modelID").setValue(moduleId);
        fdmRequest.getProp("extruderId").setValue(extruderId);
        fdmRequest.getProp("fanSpeed").setValue(fanSpeed);
        return mConnectionController.request(0x10, 0x06, fdmRequest, new ResponseStructure());
    }

    public Observable<ResponseStructure> setExtruderOffset(int id, List<DeviationStructure> extruderOffsetList) {
        FdmToolhead fdmToolhead = mToolheads.get(id);
        if (fdmToolhead == null)
            return Observable.just(new ResponseStructure<>());
        int moduleId = fdmToolhead.getModuleInfo().getKey();

        BaseStructure fdmRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("modelID", new UInt8Prop());
                addProp("extruderOffset", new ArrayProp<>());
            }
        };

        fdmRequest.getProp("modelID").setValue(moduleId);
        fdmRequest.getProp("extruderOffset").setValue(extruderOffsetList);
        return mConnectionController.request(0x10, 0x07, fdmRequest, new ResponseStructure());
    }

    public Observable<ResponseStructure> getExtruderOffset(int id) {
        FdmToolhead fdmToolhead = mToolheads.get(id);
        if (fdmToolhead == null)
            return Observable.just(new ResponseStructure<>());
        int moduleId = fdmToolhead.getModuleInfo().getKey();
        return mConnectionController.request(0x10, 0x08, new UInt8Prop(moduleId), new ResponseStructure(new ArrayProp<>(new DeviationStructure())));
    }

    public Observable<ResponseStructure> requestActivatedExtrusion(int type, float lengthIn, float speedIn, float lengthOut, float speedOut) {
        return requestActivatedExtrusion(0, type, lengthIn, speedIn, lengthOut, speedOut);
    }

    /**
     * This method will only take effect on the activated toolhead.
     * Currently only implemented in generation A products
     */
    public Observable<ResponseStructure> requestActivatedExtrusion(int id, int movementType, float lengthIn, float speedIn, float lengthOut, float speedOut) {
        FdmToolhead fdmToolhead = mToolheads.get(id);
        if (fdmToolhead == null)
            return Observable.just(new ResponseStructure());
        int moduleId = fdmToolhead.getModuleInfo().getKey();

        BaseStructure fdmRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("modelID", new UInt8Prop());
                addProp("movementType", new UInt8Prop());
                addProp("lengthIn", new FloatProp());
                addProp("speedIn", new FloatProp());
                addProp("lengthOut", new FloatProp());
                addProp("speedOut", new FloatProp());
            }
        };

        fdmRequest.getProp("modelID").setValue(moduleId);
        fdmRequest.getProp("movementType").setValue(movementType);
        fdmRequest.getProp("lengthIn").setValue(lengthIn);
        fdmRequest.getProp("speedIn").setValue(speedIn);
        fdmRequest.getProp("lengthOut").setValue(lengthOut);
        fdmRequest.getProp("speedOut").setValue(speedOut);
        return mConnectionController.request(0x10, 0x09, fdmRequest, new ResponseStructure());
    }

    /**
     * This method will only take effect on the activated toolhead.
     * Currently only implemented in generation J products
     *
     * @param movementType 0:Squeeze out    1:Back to the suction
     * @param length       -1: Keep on moving  0: Stop moving   >0:Movement length
     */
    public Observable<ResponseStructure> requestExtruderMovement(int id, int movementType, float length, float speed) {
        FdmToolhead fdmToolhead = mToolheads.get(id);
        if (fdmToolhead == null)
            return Observable.just(new ResponseStructure());
        int moduleId = fdmToolhead.getModuleInfo().getKey();

        BaseStructure fdmRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("movementType", new UInt8Prop());
                addProp("length", new FloatProp());
                addProp("speed", new FloatProp());
            }
        };

        fdmRequest.getProp("key").setValue(moduleId);
        fdmRequest.getProp("movementType").setValue(movementType);
        fdmRequest.getProp("length").setValue(length);
        fdmRequest.getProp("speed").setValue(speed);
        return mConnectionController.request(0x10, 0x0a, fdmRequest, new ResponseStructure());
    }

    /*---------------------------- Head operation ------------------------------------*/
    // calibration
    // heated bed calibration
    public Observable<ResponseStructure> exitCalibration(boolean save) {
        return mConnectionController.request(0xa0, 0x06, new BoolProp(save), new ResponseStructure());
    }

    public Observable<ResponseStructure> setCalibrationMode(int mode) {
        return mConnectionController.request(0xa0, 0x00, new UInt8Prop(mode), new ResponseStructure());
    }


    public Observable<ResponseStructure> calibratePointByIndex(int calibratePointIndex, boolean autoCalibrate) {
        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("calibratePointIndex", new UInt8Prop());
                addProp("autoCalibrate ", new BoolProp());
            }
        };
        baseStructure.getProp("calibratePointIndex").setValue(calibratePointIndex);
        baseStructure.getProp("autoCalibrate ").setValue(autoCalibrate);
        return mConnectionController.request(0xa0, 0x01, baseStructure, new ResponseStructure());
    }

    public Observable<ResponseStructure> setGetHeightDifferenceState(boolean enabled) {
        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("getZHeightDifferenceState", new BoolProp());
            }
        };
        baseStructure.getProp("getZHeightDifferenceState").setValue(enabled);
        return mConnectionController.request(0xa0, 0x02, baseStructure, new ResponseStructure());
    }

    public Observable<ResponseStructure> subscribeGetHeightDifference() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0xa0, 0xa0, 500);
        return mConnectionController.request(0x01, 0x00, subscribeStructure, new ResponseStructure());
    }

    public Observable<ResponseStructure> unSubscribeGetZHeightDifference() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0xa0, 0xa0, 0);
        return mConnectionController.request(0x01, 0x01, subscribeStructure, new ResponseStructure<>());
    }

    public Observable<ResponseStructure> watchHeightDifferenceState() {
        ResponseStructure<BaseStructure> baseStructureResponseStructure = new ResponseStructure<>();
        BaseStructure returnStruct = new BaseStructure() {
            @Override
            protected void init() {
                addProp("index", new UInt8Prop());
                addProp("heightDifference", new FloatProp());
            }
        };
        baseStructureResponseStructure.resultProp = new UInt8Prop();
        baseStructureResponseStructure.dataProp = returnStruct;
        return mConnectionController.watch(0xa0, 0xa0, baseStructureResponseStructure);
    }

    public Observable<ResponseStructure> startGridCalibration(int patch) {
        return mConnectionController.request(0xa0, 0x03, new UInt8Prop(patch), new ResponseStructure());
    }

    public Observable<ResponseStructure> GridCalibration(int index) {
        return mConnectionController.request(0xa0, 0x04, new UInt8Prop(index), new ResponseStructure());
    }

    public Observable<ResponseStructure> queryBedCalibrationStatus() {
        return mConnectionController.request(0xa0, 0x07, null, new ResponseStructure(new BoolProp()));
    }

    public Observable<ResponseStructure> CalibrationDrawBackZ() {
        return mConnectionController.request(0xa0, 0x08, null, new ResponseStructure());
    }

    public Observable<ResponseStructure> watchGridCalibrationStatus() {
        ResponseStructure<BaseStructure> baseStructureResponseStructure = new ResponseStructure<>();
        BaseStructure returnStruct = new BaseStructure() {
            @Override
            protected void init() {
                addProp("index", new UInt8Prop());
                addProp("status", new UInt8Prop());
            }
        };
        baseStructureResponseStructure.dataProp = returnStruct;
        return mConnectionController.watch(0xa0, 0xa1, baseStructureResponseStructure);
    }

    public Observable<ResponseStructure> subscribeGridCalibrationStatus() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0xa0, 0xa1, 500);
        return mConnectionController.request(0x01, 0x00, subscribeStructure, new ResponseStructure());
    }

    public Observable<ResponseStructure> unSubscribeGridCalibrationStatus() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0xa0, 0xa1, 0);
        return mConnectionController.request(0x01, 0x01, subscribeStructure, new ResponseStructure<>());
    }


    // z offset calibration
    public Observable<ResponseStructure> moveZCalibrationIndex(int toolheadIndex, int calibratePointIndex) {
        FdmToolhead fdmToolhead = mToolheads.get(toolheadIndex);
        if (fdmToolhead == null)
            return Observable.just(new ResponseStructure());
        int moduleId = fdmToolhead.getModuleInfo().getKey();

        BaseStructure fdmRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("calibratePointIndex", new Int8Prop());
            }
        };

        fdmRequest.getProp("key").setValue(moduleId);
        fdmRequest.getProp("calibratePointIndex").setValue(calibratePointIndex);
        return mConnectionController.request(0xa0, 0x10, fdmRequest, new ResponseStructure());
    }

    public Observable<ResponseStructure> startZAuxiliaryCalibration(int index) {
        return mConnectionController.request(0xa0, 0x11, new UInt8Prop(index), new ResponseStructure());
    }

    public Observable<ResponseStructure> startZOffsetCalibration(int extruderIndex) {
        return mConnectionController.request(0xa0, 0x12, new UInt8Prop(extruderIndex), new ResponseStructure());
    }

    public Observable<ResponseStructure> startExtruderSensorCalibration(int extruderIndex) {
        return mConnectionController.request(0xa0, 0x13, new UInt8Prop(extruderIndex), new ResponseStructure());
    }

    public Observable<ResponseStructure> setZOffset(int id, int extruderIndex, float absoluteZOffset) {
        int key = getModuleIdFromIndex(id);
        BaseStructure zOffsetRequestStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop(key));
                addProp("extruderIndex", new UInt8Prop(extruderIndex));
                addProp("zOffset", new FloatProp(absoluteZOffset));
            }
        };

        return mConnectionController.request(0xa0, 0x15, zOffsetRequestStructure, new ResponseStructure());
    }

    public Observable<ResponseStructure> getZOffset(int toolHeadIndex) {
        int key = getModuleIdFromIndex(toolHeadIndex);
        BaseStructure zOffsetRequestStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop(key));
            }
        };

        ResponseStructure responseStructure = new ResponseStructure();

        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("zOffsetList", new ArrayProp<>(new ZOffsetInfo()));
            }
        };


        responseStructure.resultProp = new UInt8Prop();
        responseStructure.dataProp = baseStructure;
        return mConnectionController.request(0xa0, 0x16, zOffsetRequestStructure, responseStructure);
    }


    public Observable<ResponseStructure> startXYCalibration() {
        return mConnectionController.request(0xa0, 0x21, null, new ResponseStructure());
    }

    public Observable<ResponseStructure<FdmToolhead.FdmToolheadStatus>> getToolheadInfoObservable(int toolheadIndex) {
        return mToolheads.get(toolheadIndex).requestInfo();
    }

    public void unWatchGridCalibrationStatus() {
        mConnectionController.unWatch(0xa0, 0xa1);
    }

    public Observable<ResponseStructure> startHotEndPIDAutoTune(int id, int extruderId) {
        FdmToolhead fdmToolhead = mToolheads.get(id);
        if (fdmToolhead == null)
            return Observable.just(new ResponseStructure());
        int moduleId = fdmToolhead.getModuleInfo().getKey();

        BaseStructure structure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop(moduleId));
                addProp("extruderId", new UInt8Prop(extruderId));
            }
        };

        return mConnectionController.request(0xa0, 0x40, structure, new ResponseStructure());
    }

    public Observable<ResponseStructure> stopExtruderHeat() {
        return setExtruderTemperature(0, 0, 0)
                .flatMap(responseStructure -> mToolheads.get(0).getToolheadStatusSubjectHolder().getValue().getExtruderList().size() == 2 ? setExtruderTemperature(0, 1, 0) : Observable.just(responseStructure))
                .flatMap(responseStructure -> mToolheads.size() == 2 ? setExtruderTemperature(1, 0, 0) : Observable.just(responseStructure))
                .flatMap(responseStructure -> mToolheads.size() == 2 && mToolheads.get(1).getToolheadStatusSubjectHolder().getValue().getExtruderList().size() == 2 ? setExtruderTemperature(1, 1, 0) : Observable.just(responseStructure));
    }

    public Observable<ResponseStructure> setAllExtruderTemperature(int temperature) {
        return setExtruderTemperature(0, 0, temperature)
                .flatMap(responseStructure -> mToolheads.get(0).getToolheadStatusSubjectHolder().getValue().getExtruderList().size() == 2 ? setExtruderTemperature(0, 1, temperature) : Observable.just(responseStructure))
                .flatMap(responseStructure -> mToolheads.size() == 2 ? setExtruderTemperature(1, 0, temperature) : Observable.just(responseStructure))
                .flatMap(responseStructure -> mToolheads.size() == 2 && mToolheads.get(1).getToolheadStatusSubjectHolder().getValue().getExtruderList().size() == 2 ? setExtruderTemperature(1, 1, temperature) : Observable.just(responseStructure));
    }

    public void reset() {
        mToolheads.clear();
    }

    public void subscribeFdmToolHeadInfo() {
        Logger.d("Subscribe FdmToolHeadInfo change...");
        mToolheads.get(0).subscribeFdmToolHeadInfoChange().subscribe();
    }

    public void unSubscribeFdmToolHeadInfo() {
        mToolheads.get(0).unSubscribeFdmToolHeadInfoChange();
    }

    public void requestInfoAll() {
        for (int i = 0; i < mToolheads.size(); i++) {
            mToolheads.get(i).requestInfo().subscribe();
        }
    }
}
