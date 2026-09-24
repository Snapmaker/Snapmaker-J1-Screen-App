package fabscreen.platform.base.service.machine.entity.module;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.List;

import fabscreen.platform.base.R;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.OpenDoorDetectionState;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.SubscribeStructure;
import fabscreen.platform.base.service.machine.structure.prop.ArrayProp;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.base.view.BaseActivity;
import fabscreen.platform.base.view.PerpetualPopuBean;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.lib.SubjectHolder;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import okio.Buffer;

public class Enclosure extends Module {
    private final BehaviorSubject<EnclosureStatus> mStatusSubject = BehaviorSubject.createDefault(new EnclosureStatus());
    private final SubjectHolder<EnclosureStatus> mEnclosureStatusSubjectHolder = new SubjectHolder<>(mStatusSubject);
    private final CompositeDisposable mDisposables = new CompositeDisposable();
    IAppService mAppService;

    private int mPowerState = -1;
    private int mCutterState = -1;
    public static final int ON_TYPE = 101;
    public static final int OFF_TYPE = 10;

    public Enclosure(ModuleInfo info, IMachine mc, MachineConnectionController cc, IAppService appService) {
        super(info, mc, cc);
        mAppService = appService;
        Logger.d("enclosure init!");
    }

    @Override
    public void init() {
        Logger.d("enclosure init!");
        mDisposables.add(requestInfo().subscribe(response -> {
        }, LogHelper::log));

        mDisposables.add(mConnectionController.watch(0x15, 0xa0, new ResponseStructure<>(new EnclosureStatus()))
                .subscribe(response -> {
                    if (response.isSuccess() && response.dataProp.getKey() == getModuleInfo().getKey()) {
                        mStatusSubject.onNext(response.dataProp);

                        mCutterState = response.dataProp.isDoorOpenProp.getValue() ? ON_TYPE : OFF_TYPE;
                        if (mPowerState != -1 && mPowerState == mCutterState) {
                            return;
                        }
                        mPowerState = mCutterState;
                        if (mAppService.getNowViewContext() != null) {
                            ((BaseActivity) mAppService.getNowViewContext()).sendEnclosureMessage(
                                    new PerpetualPopuBean(R.drawable.pic_nclosure136x136, R.string.all_enclosure,
                                            response.dataProp.isDoorOpenProp.getValue() ? R.string.all_enclosure_open : R.string.all_enclosure_close));
                        }
                    }

                }, LogHelper::log));
    }

    @Override
    public String getDisplayName() {
        return getAppContext().getString(R.string.all_enclosure);
    }

    @Override
    public Observable<ResponseStructure<EnclosureStatus>> requestInfo() {
        BaseStructure Request = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
            }
        };
        Request.getProp("key").setValue(getModuleInfo().getKey());
        return mConnectionController.request(0x15, 0x01, Request, new ResponseStructure<>(new EnclosureStatus()))
                .doOnNext(response -> mStatusSubject.onNext(response.dataProp));
    }

    public Observable<ResponseStructure> setEnclosureLedLevel(int value) {
        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("value", new UInt8Prop());
            }
        };
        baseStructure.getProp("key").setValue(getModuleInfo().getKey());
        baseStructure.getProp("value").setValue(value);
        return mConnectionController.request(0x15, 0x02, baseStructure, new ResponseStructure<>());
    }

    public Observable<ResponseStructure> setEnclosureDoorDetection(boolean enabled) {
        IMachine.WorkType workType = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType;
        return setEnclosureDoorDetection(workType, enabled);
    }

    public Observable<ResponseStructure> setEnclosureDoorDetection(IMachine.WorkType workType, boolean enabled) {
        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("workType", new UInt8Prop());
                addProp("enabled", new BoolProp());
            }
        };
        baseStructure.getProp("key").setValue(getModuleInfo().getKey());
        baseStructure.getProp("key").setValue(getModuleInfo().getKey());
        int type = 0;
        switch (workType) {
            case FDM:
                type = 0;
                break;
            case LASER:
                type = 1;
                break;
            case CNC:
                type = 2;
                break;
            default:
        }
        baseStructure.getProp("workType").setValue(type);
        baseStructure.getProp("enabled").setValue(enabled);
        return mConnectionController.request(0x15, 0x03, baseStructure, new ResponseStructure<>());
    }

    public Observable<ResponseStructure> setEnclosureFanLevel(int value) {
        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("value", new UInt8Prop());
            }
        };
        baseStructure.getProp("key").setValue(getModuleInfo().getKey());
        baseStructure.getProp("value").setValue(value);
        return mConnectionController.request(0x15, 0x04, baseStructure, new ResponseStructure<>());
    }

    public void subscribeEnclosureInfo() {
        Logger.d("Subscribe enclosure...");

        SubscribeStructure subscribeStructure = new SubscribeStructure(0x15, 0xa0, 1000);
        Disposable subscribe = mConnectionController.request(0x01, 0x00, subscribeStructure, new ResponseStructure()).subscribe();
        mDisposables.add(subscribe);
    }

    public void unsubscribeEnclosureInfo() {
        Logger.d("Unsubscribe enclosure...");
        SubscribeStructure subscribeStructure = new SubscribeStructure(0x15, 0xa0, 0);
        mDisposables.add(mConnectionController.request(0x01, 0x01, subscribeStructure, new ResponseStructure<>())
                .subscribe(result -> {
                }, LogHelper::log));
    }

    //FIXME :It should not be modified and can be updated using requestInfo
    public void clearEnclosureDoorFlag() {
    }

    public Observable<EnclosureStatus> getEnclosureStatusObservable() {
        return mEnclosureStatusSubjectHolder.getObservable();
    }

    public EnclosureStatus getEnclosureStatusValue() {
        return mEnclosureStatusSubjectHolder.getValue();
    }


    public static class EnclosureStatus implements IStructure {
        private UInt8Prop keyProp = new UInt8Prop();
        private UInt8Prop statusProp = new UInt8Prop();
        private UInt8Prop ledValueProp = new UInt8Prop();
        private ArrayProp<OpenDoorDetectionState> openDoorDetectionStateArrayProp = new ArrayProp<>(new OpenDoorDetectionState());
        private BoolProp isDoorOpenProp = new BoolProp();
        private UInt8Prop fanSpeedProp = new UInt8Prop();

        public EnclosureStatus(int key, int status, int ledValue, List<OpenDoorDetectionState> openDoorDetectionStateList, boolean isDoorOpen, int fanSpeed) {
            keyProp.setValue(key);
            statusProp.setValue(status);
            ledValueProp.setValue(ledValue);
            openDoorDetectionStateArrayProp.setValue(openDoorDetectionStateList);
            isDoorOpenProp.setValue(isDoorOpen);
            fanSpeedProp.setValue(fanSpeed);
        }

        public EnclosureStatus() {
        }

        @Override
        public byte[] toByteArray() {
            Buffer buffer = new Buffer();
            buffer.write(keyProp.toByteArray());
            buffer.write(statusProp.toByteArray());
            buffer.write(ledValueProp.toByteArray());
            buffer.write(openDoorDetectionStateArrayProp.toByteArray());
            buffer.write(isDoorOpenProp.toByteArray());
            buffer.write(fanSpeedProp.toByteArray());
            return buffer.readByteArray();
        }

        @Override
        public Buffer readBuffer(Buffer buffer) throws IOException {
            keyProp.readBuffer(buffer);
            statusProp.readBuffer(buffer);
            ledValueProp.readBuffer(buffer);
            openDoorDetectionStateArrayProp.readBuffer(buffer);
            isDoorOpenProp.readBuffer(buffer);
            fanSpeedProp.readBuffer(buffer);
            return buffer;
        }

        public int getKey() {
            return keyProp.getValue();
        }

        public void setKey(int key) {
            keyProp.setValue(key);
        }

        public int getStatus() {
            return statusProp.getValue();
        }

        public void setStatus(int status) {
            statusProp.setValue(status);
        }

        public int getLedValue() {
            return ledValueProp.getValue();
        }

        public void setLedValue(int ledValue) {
            ledValueProp.setValue(ledValue);
        }

        public List<OpenDoorDetectionState> getDoorDetectionEnabled() {
            return openDoorDetectionStateArrayProp.getValue();
        }

        public void setDoorDetectionEnabled(List<OpenDoorDetectionState> openDoorDetectionStateList) {
            openDoorDetectionStateArrayProp.setValue(openDoorDetectionStateList);
        }

        public boolean isDoorOpen() {
            return isDoorOpenProp.getValue();
        }

        public void setDoorOpen(boolean doorOpen) {
            isDoorOpenProp.setValue(doorOpen);
        }

        public int getFanSpeed() {
            return fanSpeedProp.getValue();
        }

        public void setFanSpeed(int fanSpeed) {
            fanSpeedProp.setValue(fanSpeed);
        }

        public boolean isLedOn() {
            return ledValueProp.getValue() != 0;
        }

        public boolean isFanOn() {
            return fanSpeedProp.getValue() != 0;
        }

        @Override
        public String toString() {
            return "EnclosureStatus{" +
                    "keyProp=" + keyProp +
                    ", statusProp=" + statusProp +
                    ", ledValueProp=" + ledValueProp +
                    ", openDoorDetectionStateArrayProp =" + openDoorDetectionStateArrayProp +
                    ", isDoorOpenProp=" + isDoorOpenProp +
                    ", fanSpeedProp=" + fanSpeedProp +
                    '}';
        }

        public boolean isDoorDetectionEnabled() {
            IMachine.WorkType workType = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType;
            switch (workType) {
                case FDM:
                    return openDoorDetectionStateArrayProp.getValue().get(1).getState();
                case CNC:
                    return openDoorDetectionStateArrayProp.getValue().get(2).getState();
                case LASER:
                    return openDoorDetectionStateArrayProp.getValue().get(3).getState();
                default:
                    return false;
            }
        }
    }
}
