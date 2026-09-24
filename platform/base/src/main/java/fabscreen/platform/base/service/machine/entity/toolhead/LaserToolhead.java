package fabscreen.platform.base.service.machine.entity.toolhead;

import java.io.IOException;
import java.util.List;

import fabscreen.platform.base.R;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.entity.Toolhead;
import fabscreen.platform.base.service.machine.entity.parts.Fan;
import fabscreen.platform.base.service.machine.entity.parts.LaserTube;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.LaserSafetyStateStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.prop.ArrayProp;
import fabscreen.platform.base.service.machine.structure.prop.FloatProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.lib.SubjectHolder;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import okio.Buffer;

public class LaserToolhead extends Toolhead {
    private BehaviorSubject<LaserToolheadInfo> mLaserToolheadInfoSubject = BehaviorSubject.createDefault(new LaserToolheadInfo());
    private SubjectHolder<LaserToolheadInfo> mLaserToolheadInfoSubjectHolder = new SubjectHolder<>(mLaserToolheadInfoSubject);
    private BehaviorSubject<LaserSafetyStateStructure> mLaserSafetyStateSubject = BehaviorSubject.createDefault(new LaserSafetyStateStructure());
    private SubjectHolder<LaserSafetyStateStructure> mLaserSafetyStateSubjectHolder = new SubjectHolder<>(mLaserSafetyStateSubject);
    private final CompositeDisposable mDisposables = new CompositeDisposable();

    private String bluetoothMacAddress = "";
    IAppService mAppService;
    boolean isShow = false;
    ModuleInfo mModuleInfo;

    public LaserToolhead(ModuleInfo info, IMachine mc, MachineConnectionController cc, IAppService appService) {
        super(info, mc, cc);
        mModuleInfo = info;
        MachineInfo machineInfo = mc.getMachineInfoSubjectHolder().getValue();
        machineInfo.workType = IMachine.WorkType.LASER;
        mAppService = appService;
    }

    @Override
    public void init() {
        Disposable subscribe = requestInfo()
                .subscribe(responseStructure -> {
                }, LogHelper::log);
        mDisposables.add(subscribe);

        ResponseStructure<BaseStructure> baseStructureResponseStructure = new ResponseStructure<>();
        BaseStructure laserTubeStateRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("currentPower", new FloatProp());
                addProp("targetPower", new FloatProp());
            }
        };
        baseStructureResponseStructure.dataProp = laserTubeStateRequest;

        subscribe = mConnectionController.watch(0x12, 0xa1, baseStructureResponseStructure).subscribe(response -> {
            BaseStructure responseStructure = response.dataProp;
            int key = (int) responseStructure.getProp("key").getValue();
            if (key == mModuleInfo.getKey()) {
                LaserToolheadInfo value = mLaserToolheadInfoSubject.getValue();
                LaserTube laserTube = new LaserTube();
                laserTube.setTargetPower((Float) responseStructure.getProp("targetPower").getValue());
                laserTube.setCurrentPower((Float) responseStructure.getProp("currentPower").getValue());
                value.setLaserTube(laserTube);
                mLaserToolheadInfoSubject.onNext(value);
            }

        });
        mDisposables.add(subscribe);

        subscribe = mConnectionController.watch(0x12, 0xa0, new ResponseStructure<>(new LaserSafetyStateStructure())).subscribe(response -> {
            LaserSafetyStateStructure laserSafetyStateSructure = response.dataProp;
            int key = laserSafetyStateSructure.getKey();
            if (key == mModuleInfo.getKey()) {
                if (laserSafetyStateSructure.getState() != 0) {
                    isShow = true;
                    mAppService.DataPipe("Laser Safety Error : \n" + laserSafetyStateSructure.toString(), true);
                } else if (isShow) {
                    isShow = false;
                    mAppService.DataPipe("", false);
                }
                mLaserSafetyStateSubject.onNext(laserSafetyStateSructure);
            }
        });
        mDisposables.add(subscribe);
    }

    @Override
    public String getDisplayName() {
        int headType = mModuleInfo.getModuleId();
        if (headType == ModuleType.HEAD_LASER_10W) {
            return getAppContext().getString(R.string.all_10w_laser);
        } else if (headType == ModuleType.HEAD_LASER) {
            return getAppContext().getString(R.string.all_laser);
        } else {
            return "unknown laser module";
        }
    }

    public String getBluetoothMacAddress() {
        return bluetoothMacAddress;
    }

    public void setBluetoothMacAddress(String macAddress) {
        this.bluetoothMacAddress = macAddress;
    }

    @Override
    public Observable<ResponseStructure<LaserToolheadInfo>> requestInfo() {
        BaseStructure laserRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
            }
        };
        laserRequest.getProp("key").setValue(getModuleInfo().getKey());
        LaserToolheadInfo laserToolheadInfo = new LaserToolheadInfo();
        laserToolheadInfo.fansListProp.addElement(new Fan());
        ResponseStructure<LaserToolheadInfo> laserToolheadInfoResponseStructure = new ResponseStructure<>();
        laserToolheadInfoResponseStructure.dataProp = laserToolheadInfo;
        // FIXME: 2022/1/27 request multiple times
        return mConnectionController.request(0x12, 0x01, laserRequest, laserToolheadInfoResponseStructure)
                .doOnNext(responseStructure -> {
                    mLaserToolheadInfoSubject.onNext(responseStructure.dataProp);
                });
    }

    public Observable<LaserToolheadInfo> getLaserToolHeadInfoObservable() {
        return mLaserToolheadInfoSubjectHolder.getObservable();
    }

    public LaserToolheadInfo getLaserToolHeadInfoValue() {
        return mLaserToolheadInfoSubjectHolder.getValue();
    }

    public Observable<LaserSafetyStateStructure> getLaserSafetyStateObservable() {
        return mLaserSafetyStateSubjectHolder.getObservable();
    }

    public LaserSafetyStateStructure getLaserSafetyStateValue() {
        return mLaserSafetyStateSubjectHolder.getValue();
    }


    public static class LaserToolheadInfo implements IStructure {
        private UInt8Prop keyProp = new UInt8Prop();
        private UInt8Prop headStatusProp = new UInt8Prop();
        private FloatProp laserFocalLengthProp = new FloatProp();
        private FloatProp platformHeightProp = new FloatProp();
        private FloatProp axisCenterHeightProp = new FloatProp();
        private LaserTube laserTubeProp = new LaserTube();
        private ArrayProp<Fan> fansListProp = new ArrayProp<>();


        @Override
        public byte[] toByteArray() {
            Buffer buffer = new Buffer();
            buffer.write(keyProp.toByteArray());
            buffer.write(headStatusProp.toByteArray());
            buffer.write(laserFocalLengthProp.toByteArray());
            buffer.write(platformHeightProp.toByteArray());
            buffer.write(axisCenterHeightProp.toByteArray());
            buffer.write(laserTubeProp.toByteArray());
            buffer.write(fansListProp.toByteArray());
            return buffer.readByteArray();
        }

        @Override
        public Buffer readBuffer(Buffer buffer) throws IOException {
            keyProp.readBuffer(buffer);
            headStatusProp.readBuffer(buffer);
            laserFocalLengthProp.readBuffer(buffer);
            platformHeightProp.readBuffer(buffer);
            axisCenterHeightProp.readBuffer(buffer);
            laserTubeProp.readBuffer(buffer);
            fansListProp.readBuffer(buffer);
            return buffer;
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

        public float getLaserFocalLength() {
            return laserFocalLengthProp.getValue();
        }

        public void setLaserFocalLength(float laserFocalLength) {
            laserFocalLengthProp.setValue(laserFocalLength);
        }

        public void setPlatformHeight(float platformHeight) {
            platformHeightProp.setValue(platformHeight);
        }

        public float getPlatformHeight() {
            return platformHeightProp.getValue();
        }

        public void setAxisCenterHeight(float height) {
            axisCenterHeightProp.setValue(height);
        }

        public float getAxisCenterHeight() {
            return axisCenterHeightProp.getValue();
        }

        public LaserTube getLaserTube() {
            return laserTubeProp;
        }

        public void setLaserTube(LaserTube laserTube) {
            laserTubeProp = laserTube;
        }

        public List<Fan> getFans() {
            return fansListProp.getValue();
        }

        public void setFansList(List<Fan> fansList) {
            fansListProp.setValue(fansList);
        }

        @Override
        public String toString() {
            return "LaserToolheadInfo{" +
                    "keyProp=" + keyProp +
                    ", headStatusProp=" + headStatusProp +
                    ", laserFocalLengthProp=" + laserFocalLengthProp +
                    ", platformHeightProp=" + platformHeightProp +
                    ", axisCenterHeightProp=" + axisCenterHeightProp +
                    ", laserTubeProp=" + laserTubeProp +
                    ", fansListProp=" + fansListProp +
                    '}';
        }
    }
}
