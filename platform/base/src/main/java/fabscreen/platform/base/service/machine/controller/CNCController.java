package fabscreen.platform.base.service.machine.controller;

import android.util.SparseArray;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.entity.toolhead.CNCToolhead;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt32Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

public class CNCController {
    SparseArray<CNCToolhead> mToolheads = new SparseArray<>();
    private IMachine mMachine;
    private MachineConnectionController mConnectionController;
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private int mHeadType = -1;


    public CNCController(IMachine iMachine, MachineConnectionController cc) {
        mMachine = iMachine;
        mConnectionController = cc;
    }

    public void addToolHead(CNCToolhead cncToolhead) {
        mMachine.getMachineInfoSubjectHolder().getValue().headType = mHeadType = cncToolhead.getModuleInfo().getModuleId();
        mToolheads.put(cncToolhead.getModuleInfo().getModuleIndex(), cncToolhead);
    }

    public int getHeadType() {
        return mHeadType;
    }

    public Observable<CNCToolhead.CNCToolheadInfo> getCncToolHeadInfoObservable() {
        return getCncToolHeadInfoObservable(0);
    }

    public Observable<CNCToolhead.CNCToolheadInfo> getCncToolHeadInfoObservable(int index) {
        return mToolheads.get(index).getCncToolHeadInfoObservable();
    }

    public CNCToolhead.CNCToolheadInfo getCncToolHeadInfoValue() {
        return getCncToolHeadInfoValue(0);
    }

    public CNCToolhead.CNCToolheadInfo getCncToolHeadInfoValue(int index) {
        return mToolheads.get(index).getCncToolHeadInfoValue();
    }

    public Observable<ResponseStructure> setSpindlePower(int index, int targetPower) {
        CNCToolhead cncToolhead = mToolheads.get(index);
        if (cncToolhead == null)
            throw new IllegalStateException("No toolhead found with index " + index);
        int key = cncToolhead.getModuleInfo().getKey();

        BaseStructure cncRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("targetPower", new UInt8Prop());
            }
        };
        cncRequest.getProp("key").setValue(key);
        cncRequest.getProp("targetPower").setValue(targetPower);
        return mConnectionController.request(0x11, 0x02, cncRequest, new ResponseStructure());
    }

    public Observable<ResponseStructure<IStructure>> switchCNC(int index, boolean on) {
        CNCToolhead cncToolhead = mToolheads.get(index);
        if (cncToolhead == null)
            throw new IllegalStateException("No toolhead found with index " + index);
        int key = cncToolhead.getModuleInfo().getKey();
        BaseStructure switchCNC = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("on", new BoolProp());
            }
        };
        switchCNC.getProp("key").setValue(key);
        switchCNC.getProp("on").setValue(on);
        return mConnectionController.request(0x11, 0x05, switchCNC, new ResponseStructure<>());
    }

    public Observable<ResponseStructure> setTargetSpeed(int index, long speed) {
        CNCToolhead cncToolhead = mToolheads.get(index);
        if (cncToolhead == null)
            // TODO:no module
            return Observable.just(new ResponseStructure());
        int key = cncToolhead.getModuleInfo().getKey();

        BaseStructure cncRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("speed", new UInt32Prop());
            }
        };
        cncRequest.getProp("key").setValue(key);
        cncRequest.getProp("speed").setValue(speed);
        return mConnectionController.request(0x11, 0x03, cncRequest, new ResponseStructure());
    }

    public Observable<ResponseStructure> setControlMode(int index, int mode) {
        CNCToolhead cncToolhead = mToolheads.get(index);
        if (cncToolhead == null)
            // TODO:no module
            return Observable.just(new ResponseStructure());
        int key = cncToolhead.getModuleInfo().getKey();

        BaseStructure cncRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("mode", new UInt8Prop());
            }
        };
        cncRequest.getProp("key").setValue(key);
        cncRequest.getProp("mode").setValue(mode);
        return mConnectionController.request(0x11, 0x04, cncRequest, new ResponseStructure());
    }

    public void subscribeCNCInfo() {
        Logger.d("Subscribe cnc...");
        mToolheads.get(0).subscribeCNCInfo();
    }

    public void unSubscribeCNCInfo() {
        mToolheads.get(0).unSubscribeCNCInfo();
    }


    public Observable<ResponseStructure> setCalibrationMode(int calibrationMode) {
        return mConnectionController.request(0xa4, 0x00, new UInt8Prop(calibrationMode), new ResponseStructure());
    }

    public Observable<ResponseStructure> exitCalibration(boolean isSave) {
        return mConnectionController.request(0xa4, 0x01, new BoolProp(isSave), new ResponseStructure());
    }

    public void reset() {
        mToolheads.clear();
    }
}
