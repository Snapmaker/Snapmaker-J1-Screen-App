package fabscreen.platform.base.service.machine.entity.module;

import com.orhanobut.logger.Logger;

import java.io.IOException;

import fabscreen.platform.base.R;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.entity.parts.DryBoxStatus;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.SubscribeStructure;
import fabscreen.platform.base.service.machine.structure.prop.Int16Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt32Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.lib.SubjectHolder;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import okio.Buffer;

public class DryBox extends Module {
    private BehaviorSubject<DryBoxInfo> mStatus = BehaviorSubject.createDefault(new DryBoxInfo());
    private SubjectHolder<DryBoxInfo> mStatusSubjectHolder = new SubjectHolder<>(mStatus);
    private CompositeDisposable mDisposables = new CompositeDisposable();

    public DryBox(ModuleInfo info, IMachine mc, MachineConnectionController cc) {
        super(info, mc, cc);
    }

    public SubjectHolder<DryBoxInfo> getDryBoxStatusHolder() {
        return mStatusSubjectHolder;
    }

    @Override
    protected void init() {
        Disposable subscribe = requestInfo().subscribe();
        mDisposables.add(subscribe);

        mDisposables.add(mConnectionController.watch(0x18, 0xa0, new ResponseStructure<>(new DryBoxStatus()))
                .filter(ResponseStructure::isSuccess)
                .subscribe(response -> {
                    DryBoxInfo value = mStatus.getValue();
                    value.setDryBoxStatus(response.dataProp);
                    mStatus.onNext(value);
                }, LogHelper::log));
    }

    @Override
    public String getDisplayName() {
        return getAppContext().getString(R.string.dry_box_module);
    }

    @Override
    public Observable<ResponseStructure<DryBoxInfo>> requestInfo() {
        int key = getModuleInfo().getKey();
        return mConnectionController.request(0x18, 0x01, new UInt8Prop(key), new ResponseStructure<>(new DryBoxInfo()))
                .doOnNext(response -> {
                    mStatus.onNext(response.dataProp);
                });
    }

    public Observable<ResponseStructure<IStructure>> setTargetTemperature(int targetTemperature) {
        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("targetTemperature", new Int16Prop());
            }
        };
        baseStructure.getProp("key").setValue(getModuleInfo().getKey());
        baseStructure.getProp("targetTemperature").setValue(targetTemperature);
        return mConnectionController.request(0x18, 0x02, baseStructure, new ResponseStructure<>());
    }

    public Observable<ResponseStructure<IStructure>> setTargetTime(long seconds) {
        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("dryDuration", new UInt32Prop());
            }
        };
        baseStructure.getProp("key").setValue(getModuleInfo().getKey());
        baseStructure.getProp("dryDuration").setValue(seconds);
        return mConnectionController.request(0x18, 0x03, baseStructure, new ResponseStructure<>());
    }

    public Observable<ResponseStructure> switchDryState(int state) {
        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("state", new UInt8Prop());
            }
        };
        baseStructure.getProp("key").setValue(getModuleInfo().getKey());
        baseStructure.getProp("state").setValue(state);
        return mConnectionController.request(0x18, 0x04, baseStructure, new ResponseStructure());
    }

    public void subscribeStatus() {
        Logger.d("Subscribe dryer...");
        SubscribeStructure param = new SubscribeStructure(0x18, 0xa0, 10000);
        mDisposables.add(mConnectionController.request(0x01, 0x00, param, new ResponseStructure<>())
                .subscribe(response -> {
                }, LogHelper::log));
    }

    public void unSubscribeStatus() {
        SubscribeStructure param = new SubscribeStructure(0x18, 0xa0, 10000);
        mDisposables.add(mConnectionController.request(0x01, 0x01, param, new ResponseStructure<>())
                .subscribe(response -> {
                }, LogHelper::log));
    }

    public static class DryBoxInfo implements IStructure {
        private UInt8Prop keyProp = new UInt8Prop();
        private UInt8Prop moduleStatusProp = new UInt8Prop();
        private DryBoxStatus dryBoxStatusProp = new DryBoxStatus();

        public DryBoxInfo(int key, int moduleStatus, DryBoxStatus dryBoxStatus) {
            keyProp.setValue(key);
            moduleStatusProp.setValue(moduleStatus);
            dryBoxStatusProp = dryBoxStatus;
        }

        public DryBoxInfo() {
        }

        @Override
        public byte[] toByteArray() {
            Buffer buffer = new Buffer();
            buffer.write(keyProp.toByteArray());
            buffer.write(moduleStatusProp.toByteArray());
            buffer.write(dryBoxStatusProp.toByteArray());
            return buffer.readByteArray();
        }

        @Override
        public Buffer readBuffer(Buffer buffer) throws IOException {
            keyProp.readBuffer(buffer);
            moduleStatusProp.readBuffer(buffer);
            dryBoxStatusProp.readBuffer(buffer);
            return buffer;
        }

        public int getKey() {
            return keyProp.getValue();
        }

        public void setKey(int key) {
            keyProp.setValue(key);
        }

        public int getModuleStatus() {
            return moduleStatusProp.getValue();
        }

        public void setModuleStatus(int moduleStatus) {
            moduleStatusProp.setValue(moduleStatus);
        }

        public DryBoxStatus getDryBoxStatus() {
            return dryBoxStatusProp;
        }

        public void setDryBoxStatus(DryBoxStatus dryBoxStatus) {
            dryBoxStatusProp = dryBoxStatus;
        }

        @Override
        public String toString() {
            return "DryBoxInfo{" +
                    "key=" + keyProp.getValue() +
                    ", moduleStatus=" + moduleStatusProp.getValue() +
                    ", dryBoxStatus=" + dryBoxStatusProp +
                    '}';
        }
    }
}
