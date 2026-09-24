package fabscreen.platform.base.service.machine.entity.module;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.PipedReader;

import fabscreen.platform.base.R;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.SubscribeStructure;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt16Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.base.view.BaseActivity;
import fabscreen.platform.base.view.PerpetualPopuBean;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.lib.SubjectHolder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import okio.Buffer;

public class AirPurifier extends Module {
    private BehaviorSubject<AirPurifierStatus> mStatus = BehaviorSubject.createDefault(new AirPurifierStatus());
    private SubjectHolder<AirPurifierStatus> mAirPurifierStatusSubjectHolder = new SubjectHolder<>(mStatus);
    private CompositeDisposable mDisposables = new CompositeDisposable();
    IAppService mAppService;

    private int mPowerState = -1;
    private int mCutterState = -1;
    public static final int ON_TYPE = 101;
    public static final int OFF_TYPE = 10;

    public AirPurifier(ModuleInfo info, IMachine mc, MachineConnectionController cc, IAppService appService) {
        super(info, mc, cc);
        mAppService = appService;
    }

    @Override
    public void init() {
        Disposable subscribe = requestInfo().subscribe();
        mDisposables.add(subscribe);

        subscribe = mConnectionController.watch(0x17, 0xa0, new ResponseStructure<>(new AirPurifierStatus()))
                .subscribe(response -> {
                    if (response.isSuccess() && response.dataProp.getKey() == getModuleInfo().getKey()) {
                        mStatus.onNext(response.dataProp);
                        Logger.e("====tian 净化器有数据");
                        mCutterState = response.dataProp.blowerSwitchProp.getValue() ? ON_TYPE : OFF_TYPE;
                        if (mPowerState != -1 && mPowerState == mCutterState) {
                            return;
                        }
                        mPowerState = mCutterState;
                        if (mAppService.getNowViewContext() != null) {
                            ((BaseActivity) mAppService.getNowViewContext()).sendAirPurifierMessage(
                                    new PerpetualPopuBean(R.drawable.pic_air_purifier_136x136, R.string.all_air_purifier,
                                            response.dataProp.blowerSwitchProp.getValue() ? R.string.all_turned_on : R.string.all_turned_off));
                        }
                    }

                }, LogHelper::log);
        mDisposables.add(subscribe);
    }

    @Override
    public String getDisplayName() {
        return getAppContext().getString(R.string.all_air_purifier);
    }

    @Override
    public Observable<ResponseStructure<AirPurifierStatus>> requestInfo() {
        BaseStructure airPurifierRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
            }
        };
        airPurifierRequest.getProp("key").setValue(getModuleInfo().getKey());
        return mConnectionController.request(0x17, 0x01, airPurifierRequest, new ResponseStructure<>(new AirPurifierStatus()))
                .doOnNext(response -> {
                    mStatus.onNext(response.dataProp);
                });
    }

    public Observable<ResponseStructure> setFanSpeedLevel(int index, int level) {
        BaseStructure heatedBedRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("level", new UInt8Prop());
            }
        };
        heatedBedRequest.getProp("key").setValue(getModuleInfo().getKey());
        heatedBedRequest.getProp("level").setValue(level);

        return mConnectionController.request(0x17, 0x02, heatedBedRequest, new ResponseStructure());
    }

    public Observable<ResponseStructure> setBlowerSwitch(int index, boolean statue) {
        BaseStructure heatedBedRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("statue", new BoolProp());
            }
        };
        heatedBedRequest.getProp("key").setValue(getModuleInfo().getKey());
        heatedBedRequest.getProp("statue").setValue(statue);
        return mConnectionController.request(0x17, 0x03, heatedBedRequest, new ResponseStructure());
    }

    public Observable<ResponseStructure> setAutoState(IMachine.WorkType workType, boolean autoState) {
        BaseStructure heatedBedRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("workType", new UInt8Prop());
                addProp("AutoState", new BoolProp());
            }
        };
        heatedBedRequest.getProp("key").setValue(getModuleInfo().getKey());
        int type = -1;
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
        heatedBedRequest.getProp("workType").setValue(type);
        heatedBedRequest.getProp("AutoState").setValue(autoState);
        return mConnectionController.request(0x17, 0x04, heatedBedRequest, new ResponseStructure());
    }

    public Observable<ResponseStructure> getAutoState(IMachine.WorkType workType) {
        BaseStructure heatedBedRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("workType", new UInt8Prop());
            }
        };
        heatedBedRequest.getProp("key").setValue(getModuleInfo().getKey());
        int type = -1;
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
        heatedBedRequest.getProp("workType").setValue(type);
        return mConnectionController.request(0x17, 0x05, heatedBedRequest, new ResponseStructure(new BoolProp()));
    }

    public Observable<ResponseStructure> setDelayStop(IMachine.WorkType workType, int delayTime) {
        BaseStructure heatedBedRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("workType", new UInt8Prop());
                addProp("delayTime", new UInt16Prop());
            }
        };
        heatedBedRequest.getProp("key").setValue(getModuleInfo().getKey());
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
        heatedBedRequest.getProp("workType").setValue(type);
        heatedBedRequest.getProp("delayTime").setValue(delayTime);
        return mConnectionController.request(0x17, 0x06, heatedBedRequest, new ResponseStructure());
    }

    public Observable<ResponseStructure> getDelayStop(IMachine.WorkType workType) {
        BaseStructure heatedBedRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("workType", new UInt8Prop());
            }
        };
        heatedBedRequest.getProp("key").setValue(getModuleInfo().getKey());
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
        heatedBedRequest.getProp("workType").setValue(type);
        return mConnectionController.request(0x17, 0x07, heatedBedRequest, new ResponseStructure(new UInt16Prop()));
    }

    public void subscribeAirPurifierStatusChange() {
        Logger.d("Subscribe air purifier...");
        Logger.d("======tian 净化器Subscribe air purifier...");
        SubscribeStructure subscribeStructure = new SubscribeStructure(0x17, 0xa0, 1000);
        Disposable subscribe = mConnectionController.request(0x01, 0x00, subscribeStructure, new ResponseStructure()).subscribe();
        mDisposables.add(subscribe);
    }

    public void unsubscribeAirPurifierStatusChange() {
        Logger.d("======tian 净化器unSubscribe air purifier...");
        SubscribeStructure subscribeStructure = new SubscribeStructure(0x17, 0xa0, 0);
        mDisposables.add(mConnectionController.request(0x01, 0x01, subscribeStructure, new ResponseStructure<>())
                .subscribe(result -> {
                }, LogHelper::log));
    }

    public Observable<AirPurifierStatus> getAirPurifierStatusObservable() {
        return mAirPurifierStatusSubjectHolder.getObservable();
    }

    public AirPurifierStatus getAirPurifierStatusValue() {
        return mAirPurifierStatusSubjectHolder.getValue();
    }


    public static class AirPurifierStatus implements IStructure {
        private UInt8Prop keyProp = new UInt8Prop();
        private UInt8Prop moduleStatusProp = new UInt8Prop();
        private BoolProp powerSwitchProp = new BoolProp();
        private BoolProp blowerSwitchProp = new BoolProp();
        private UInt8Prop fanSpeedLevelProp = new UInt8Prop();
        private UInt8Prop filterLifeProp = new UInt8Prop();
        private BoolProp filterAliveProp = new BoolProp();

        public AirPurifierStatus(int key, int moduleStatus, boolean powerSwitch, boolean blowerSwitch, int fanSpeedLevel, int filterLife, boolean filterAlive) {
            keyProp.setValue(key);
            moduleStatusProp.setValue(moduleStatus);
            powerSwitchProp.setValue(powerSwitch);
            blowerSwitchProp.setValue(blowerSwitch);
            fanSpeedLevelProp.setValue(fanSpeedLevel);
            filterLifeProp.setValue(filterLife);
            filterAliveProp.setValue(filterAlive);
        }

        public AirPurifierStatus() {
        }

        @Override
        public byte[] toByteArray() {
            Buffer buffer = new Buffer();
            buffer.write(keyProp.toByteArray());
            buffer.write(moduleStatusProp.toByteArray());
            buffer.write(powerSwitchProp.toByteArray());
            buffer.write(blowerSwitchProp.toByteArray());
            buffer.write(fanSpeedLevelProp.toByteArray());
            buffer.write(filterLifeProp.toByteArray());
            buffer.write(filterAliveProp.toByteArray());
            return buffer.readByteArray();
        }

        @Override
        public Buffer readBuffer(Buffer buffer) throws IOException {
            keyProp.readBuffer(buffer);
            moduleStatusProp.readBuffer(buffer);
            powerSwitchProp.readBuffer(buffer);
            blowerSwitchProp.readBuffer(buffer);
            fanSpeedLevelProp.readBuffer(buffer);
            filterLifeProp.readBuffer(buffer);
            filterAliveProp.readBuffer(buffer);
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

        public boolean isPowerOn() {
            return powerSwitchProp.getValue();
        }

        public void setPowerSwitch(boolean powerSwitch) {
            powerSwitchProp.setValue(powerSwitch);
        }

        public boolean isFanOn() {
            return blowerSwitchProp.getValue();
        }

        public void setBlowerSwitch(boolean blowerSwitch) {
            blowerSwitchProp.setValue(blowerSwitch);
        }

        public int getFanSpeedLevel() {
            return fanSpeedLevelProp.getValue();
        }

        public void setFanSpeedLevel(int fanSpeedLevel) {
            fanSpeedLevelProp.setValue(fanSpeedLevel);
        }

        public int getFilterLife() {
            return filterLifeProp.getValue();
        }

        public void setFilterLife(int filterLife) {
            filterLifeProp.setValue(filterLife);
        }

        public boolean isFilterAlive() {
            return filterAliveProp.getValue();
        }

        public void setFilterAlive(boolean filterAlive) {
            filterAliveProp.setValue(filterAlive);
        }

        @Override
        public String toString() {
            return "AirPurifierStatus{" +
                    "keyProp=" + keyProp +
                    ", moduleStatusProp=" + moduleStatusProp +
                    ", powerSwitchProp=" + powerSwitchProp +
                    ", blowerSwitchProp=" + blowerSwitchProp +
                    ", fanSpeedLevelProp=" + fanSpeedLevelProp +
                    ", filterLifeProp=" + filterLifeProp +
                    ", filterAliveProp=" + filterAliveProp +
                    '}';
        }
    }
}
