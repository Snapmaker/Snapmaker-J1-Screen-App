package fabscreen.platform.base.service.machine.entity.toolhead;

import com.orhanobut.logger.Logger;

import java.io.IOException;

import fabscreen.platform.base.R;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.entity.Toolhead;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.CncWorkStateStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.SubscribeStructure;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt32Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.lib.SubjectHolder;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import okio.Buffer;

public class CNCToolhead extends Toolhead {
    private final BehaviorSubject<CNCToolheadInfo> mToolHeadStatusSubject = BehaviorSubject.createDefault(new CNCToolheadInfo());
    private final SubjectHolder<CNCToolheadInfo> mToolheadStatusSubjectHolder = new SubjectHolder<>(mToolHeadStatusSubject);
    private final CompositeDisposable mDisposables = new CompositeDisposable();
    private ModuleInfo mModuleInfo;

    public CNCToolhead(ModuleInfo info, IMachine mc, MachineConnectionController cc) {
        super(info, mc, cc);
        mModuleInfo = info;
        MachineInfo machineInfo = mc.getMachineInfoSubjectHolder().getValue();
        machineInfo.workType = IMachine.WorkType.CNC;
    }

    @Override
    public void init() {
        Disposable subscribe = requestInfo().subscribe();
        mDisposables.add(subscribe);

        mDisposables.add(mConnectionController.watch(0x11, 0xa0, new ResponseStructure<>(new CncWorkStateStructure())).subscribe(response -> {
            Logger.d("cncth: %s", response);
            CncWorkStateStructure cncWorkStateStructure = response.dataProp;
            if (cncWorkStateStructure.getKey() == mModuleInfo.getKey()) {
                CNCToolheadInfo value = mToolHeadStatusSubject.getValue();
                value.setRunningState(cncWorkStateStructure.getSpindleStatue());
                value.setCurrentPower(cncWorkStateStructure.getCurrentPower());
                value.setTargetPower(cncWorkStateStructure.getTargetPower());
                value.setCurrentSpeed(cncWorkStateStructure.getCurrentSpeed());
                value.setTargetSpeed(cncWorkStateStructure.getTargetSpeed());
                mToolHeadStatusSubject.onNext(value);
            }

        }, LogHelper::log));
    }

    @Override
    public String getDisplayName() {
        int moduleType = mModuleInfo.getModuleId();
        if (moduleType == ModuleType.HEAD_CNC) {
            return getAppContext().getString(R.string.all_cnc);
        } else if (moduleType == ModuleType.HEAD_CNC_200W) {
            return getAppContext().getString(R.string.all_cnc_200w);
        } else {
            return "unknown cnc module";
        }
    }

    @Override
    public Observable<ResponseStructure<CNCToolheadInfo>> requestInfo() {
        BaseStructure cncRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
            }
        };
        cncRequest.getProp("key").setValue(getModuleInfo().getKey());
        // FIXME: 2022/1/27 request multiple times
        return mConnectionController.request(0x11, 0x01, cncRequest, new ResponseStructure<>(new CNCToolheadInfo()))
                .doOnNext(responseStructure -> {
                    if (responseStructure.isSuccess())
                        mToolHeadStatusSubject.onNext(responseStructure.dataProp);
                });
    }

    public Observable<CNCToolheadInfo> getCncToolHeadInfoObservable() {
        return mToolheadStatusSubjectHolder.getObservable();
    }

    public CNCToolheadInfo getCncToolHeadInfoValue() {
        return mToolheadStatusSubjectHolder.getValue();
    }

    public void subscribeCNCInfo() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0x11, 0xa0, 1000);
        mDisposables.add(mConnectionController.request(0x01, 0x00, subscribeStructure, new ResponseStructure<>())
                .subscribe(response -> {
                }, LogHelper::log));
    }

    public void unSubscribeCNCInfo() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0x11, 0xa0, 0);
        mDisposables.add(mConnectionController.request(0x01, 0x01, subscribeStructure, new ResponseStructure<>())
                .subscribe(result -> {
                }, LogHelper::log));
    }


    public static class CNCToolheadInfo implements IStructure {
        private UInt8Prop keyProp = new UInt8Prop();
        private UInt8Prop headStatusProp = new UInt8Prop();
        private BoolProp headActiveProp = new BoolProp();
        private UInt8Prop runningStateProp = new UInt8Prop();
        private UInt8Prop controlModeProp = new UInt8Prop();
        private UInt8Prop currentPowerProp = new UInt8Prop();
        private UInt8Prop targetPowerProp = new UInt8Prop();
        private UInt32Prop currentSpeedProp = new UInt32Prop();
        private UInt32Prop targetSpeedProp = new UInt32Prop();

        public CNCToolheadInfo(int key,
                               int headStatus,
                               boolean headActive,
                               int runningState,
                               int controlMode,
                               int currentPower,
                               int targetPower,
                               long currentSpeed,
                               long targetSpeed) {
            keyProp.setValue(key);
            headStatusProp.setValue(headStatus);
            headActiveProp.setValue(headActive);
            runningStateProp.setValue(runningState);
            controlModeProp.setValue(controlMode);
            currentPowerProp.setValue(currentPower);
            targetPowerProp.setValue(targetPower);
            currentSpeedProp.setValue(currentSpeed);
            targetSpeedProp.setValue(targetSpeed);
        }

        public CNCToolheadInfo() {
        }

        public int getKey() {
            return keyProp.getValue();
        }

        public void setKey(int key) {
            keyProp.setValue(key);
        }

        public int getHeadStatus() {
            return headStatusProp.getValue();
        }

        public void setHeadStatus(int headStatus) {
            headStatusProp.setValue(headStatus);
        }

        public boolean getHeadActive() {
            return headActiveProp.getValue();
        }

        public void setHeadActive(boolean headActive) {
            headActiveProp.setValue(headActive);
        }

        public int getRunningState() {
            return runningStateProp.getValue();
        }

        public void setRunningState(int runningState) {
            runningStateProp.setValue(runningState);
        }

        public int getControlMode() {
            return controlModeProp.getValue();
        }

        public void setControlMode(int controlMode) {
            controlModeProp.setValue(controlMode);
        }

        public int getCurrentPower() {
            return currentPowerProp.getValue();
        }

        public void setCurrentPower(int currentPower) {
            currentPowerProp.setValue(currentPower);
        }

        public int getTargetPower() {
            return targetPowerProp.getValue();
        }

        public void setTargetPower(int targetPower) {
            targetPowerProp.setValue(targetPower);
        }

        public long getCurrentSpeed() {
            return currentSpeedProp.getValue();
        }

        public void setCurrentSpeed(long currentSpeed) {
            currentSpeedProp.setValue(currentSpeed);
        }

        public long getTargetSpeed() {
            return targetSpeedProp.getValue();
        }

        public void setTargetSpeed(long targetSpeed) {
            targetSpeedProp.setValue(targetSpeed);
        }

        @Override
        public byte[] toByteArray() {
            Buffer buffer = new Buffer();
            buffer.write(keyProp.toByteArray());
            buffer.write(headStatusProp.toByteArray());
            buffer.write(headActiveProp.toByteArray());
            buffer.write(runningStateProp.toByteArray());
            buffer.write(controlModeProp.toByteArray());
            buffer.write(currentPowerProp.toByteArray());
            buffer.write(targetPowerProp.toByteArray());
            buffer.write(currentSpeedProp.toByteArray());
            buffer.write(targetSpeedProp.toByteArray());
            return buffer.readByteArray();
        }

        @Override
        public Buffer readBuffer(Buffer buffer) throws IOException {
            keyProp.readBuffer(buffer);
            headStatusProp.readBuffer(buffer);
            headActiveProp.readBuffer(buffer);
            runningStateProp.readBuffer(buffer);
            controlModeProp.readBuffer(buffer);
            currentPowerProp.readBuffer(buffer);
            targetPowerProp.readBuffer(buffer);
            currentSpeedProp.readBuffer(buffer);
            targetSpeedProp.readBuffer(buffer);
            return buffer;
        }

        @Override
        public String toString() {
            return "CNCToolheadInfo{" +
                    "keyProp=" + keyProp +
                    ", headStatusProp=" + headStatusProp +
                    ", headActiveProp=" + headActiveProp +
                    ", runningStateProp=" + runningStateProp +
                    ", controlModeProp=" + controlModeProp +
                    ", currentPowerProp=" + currentPowerProp +
                    ", targetPowerProp=" + targetPowerProp +
                    ", currentSpeedProp=" + currentSpeedProp +
                    ", targetSpeedProp=" + targetSpeedProp +
                    '}';
        }
    }
}
