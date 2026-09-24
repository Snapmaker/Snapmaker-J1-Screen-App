package fabscreen.platform.base.service.machine.entity.toolhead;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.List;

import fabscreen.platform.base.R;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.entity.Toolhead;
import fabscreen.platform.base.service.machine.entity.parts.Extruder;
import fabscreen.platform.base.service.machine.entity.parts.Fan;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.SubscribeStructure;
import fabscreen.platform.base.service.machine.structure.prop.ArrayProp;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.lib.SubjectHolder;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import okio.Buffer;

public class FdmToolhead extends Toolhead {
    private BehaviorSubject<FdmToolheadStatus> mToolHeadStatusSubject = BehaviorSubject.createDefault(new FdmToolheadStatus());
    private SubjectHolder<FdmToolheadStatus> mToolheadStatusSubjectHolder = new SubjectHolder<>(mToolHeadStatusSubject);
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private ModuleInfo mModuleInfo;

    public FdmToolhead(ModuleInfo moduleInfo, IMachine mc, MachineConnectionController cc) {
        super(moduleInfo, mc, cc);
        mModuleInfo = moduleInfo;
        MachineInfo machineInfo = mc.getMachineInfoSubjectHolder().getValue();
        machineInfo.workType = IMachine.WorkType.FDM;
    }

    @Override
    public void init() {
        Disposable subscribe = requestInfo().subscribe(response -> Logger.d("fdmth init result, %s", response), LogHelper::log);
        mDisposables.add(subscribe);

        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("extruders", new ArrayProp<>(new Extruder()));
            }
        };

        ResponseStructure iStructureResponseStructure = new ResponseStructure<>();
        iStructureResponseStructure.dataProp = baseStructure;

        subscribe = mConnectionController.watch(0x10, 0xa0, iStructureResponseStructure).subscribe(response -> {
            BaseStructure responseStructure = (BaseStructure) response.dataProp;
            int key = ((UInt8Prop) responseStructure.getProp("key")).getValue();
            if (key == mModuleInfo.getKey()) {
                FdmToolheadStatus value = mToolHeadStatusSubject.getValue();
                value.setExtruderList(((ArrayProp<Extruder>) responseStructure.getProp("extruders")).getValue());
                mToolHeadStatusSubject.onNext(value);
            }
        });
        mDisposables.add(subscribe);

        ResponseStructure iStructureResponseFdmtool = new ResponseStructure<>();
        iStructureResponseFdmtool.dataProp = new FdmToolheadStatus();
        subscribe = mConnectionController.watch(0x10, 0xa4, iStructureResponseFdmtool).subscribe(response -> {
            FdmToolheadStatus fdmToolheadStatus = (FdmToolheadStatus) response.dataProp;
            if (fdmToolheadStatus.keyProp.getValue() == mModuleInfo.getKey()) {
                mToolHeadStatusSubject.onNext(fdmToolheadStatus);
            }
        });
        mDisposables.add(subscribe);

        BaseStructure baseStructure1 = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("fans", new ArrayProp<>(new Fan()));
            }
        };

        ResponseStructure iStructureResponseStructure1 = new ResponseStructure<>();
        iStructureResponseStructure1.dataProp = baseStructure1;

        subscribe = mConnectionController.watch(0x10, 0xa3, iStructureResponseStructure1).subscribe(response -> {
            BaseStructure responseStructure = (BaseStructure) response.dataProp;
            int key = ((UInt8Prop) responseStructure.getProp("key")).getValue();
            if (key == mModuleInfo.getKey()) {
                FdmToolheadStatus value = mToolHeadStatusSubject.getValue();
                value.setFanList(((ArrayProp<Fan>) responseStructure.getProp("fans")).getValue());
                mToolHeadStatusSubject.onNext(value);
            }
        });
        mDisposables.add(subscribe);
    }

    @Override
    public String getDisplayName() {
        int headType = mModuleInfo.getModuleId();
        if (headType == ModuleType.HEAD_3DP) {
            return getAppContext().getString(R.string.all_3d_printing);
        } else if (headType == ModuleType.HEAD_3DP_DOUBLE_EXTRUDER) {
            return getAppContext().getString(R.string.all_3d_printing_dual_extruder);
        } else {
            return "unknown 3dp toolhead";
        }
    }

    @Override
    public Observable<ResponseStructure<FdmToolheadStatus>> requestInfo() {
        BaseStructure fdmRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
            }
        };
        fdmRequest.getProp("key").setValue(getModuleInfo().getKey());

        ResponseStructure<FdmToolheadStatus> fdmResponse = new ResponseStructure<>();
        fdmResponse.dataProp = new FdmToolheadStatus();
        return mConnectionController.request(0x10, 0x01, fdmRequest, fdmResponse)
                .doOnNext(responseStructure -> {
                    if (responseStructure.isSuccess())
                        mToolHeadStatusSubject.onNext(responseStructure.dataProp);
                });
    }


    // TODO: 2022/1/28 Who need this result Observable ???
    public Observable<ResponseStructure> subscribeExtruderChange() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0x10, 0xa0, 1000);
        return mConnectionController.request(0x01, 0x00, subscribeStructure, new ResponseStructure());
    }

    public void unSubscribeExtruderChange() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0x10, 0xa0, 0);
        mDisposables.add(mConnectionController.request(0x01, 0x01, subscribeStructure, new ResponseStructure<>())
                .subscribe(result -> {
                }, LogHelper::log));
    }

    public Observable<ResponseStructure> subscribeFdmToolHeadInfoChange() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0x10, 0xa4, 1000);
        return mConnectionController.request(0x01, 0x00, subscribeStructure, new ResponseStructure());
    }

    public void unSubscribeFdmToolHeadInfoChange() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0x10, 0xa4, 0);
        mDisposables.add(mConnectionController.request(0x01, 0x01, subscribeStructure, new ResponseStructure<>())
                .subscribe(result -> {
                }, LogHelper::log));
    }

    public Observable<ResponseStructure> subscribeFanChange() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0x10, 0xa3, 1000);
        return mConnectionController.request(0x01, 0x00, subscribeStructure, new ResponseStructure());
    }

    public void unSubscribeFanChange() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0x10, 0xa3, 0);
        mDisposables.add(mConnectionController.request(0x01, 0x01, subscribeStructure, new ResponseStructure<>())
                .subscribe(result -> {
                }, LogHelper::log));
    }

    public SubjectHolder<FdmToolheadStatus> getToolheadStatusSubjectHolder() {
        return mToolheadStatusSubjectHolder;
    }

    public static class FdmToolheadStatus implements IStructure {
        private UInt8Prop keyProp = new UInt8Prop();
        private UInt8Prop headStatusProp = new UInt8Prop();
        private BoolProp headActiveProp = new BoolProp();
        private ArrayProp<Extruder> extruderListProp = new ArrayProp<>(new Extruder());
        private ArrayProp<Fan> fansListProp = new ArrayProp<>(new Fan());

        @Override
        public byte[] toByteArray() {
            Buffer buffer = new Buffer();
            buffer.write(keyProp.toByteArray());
            buffer.write(headStatusProp.toByteArray());
            buffer.write(headActiveProp.toByteArray());
            buffer.write(extruderListProp.toByteArray());
            buffer.write(fansListProp.toByteArray());
            return buffer.readByteArray();
        }

        @Override
        public Buffer readBuffer(Buffer buffer) throws IOException {
            keyProp.readBuffer(buffer);
            headStatusProp.readBuffer(buffer);
            headActiveProp.readBuffer(buffer);
            extruderListProp.readBuffer(buffer);
            fansListProp.readBuffer(buffer);
            return buffer;
        }

        @Override
        public String toString() {
            return "FdmToolheadStatus{" +
                    "\nidProp=" + keyProp +
                    ",\n headStatusProp=" + headStatusProp +
                    ",\n headActiveProp=" + headActiveProp +
                    ",\n extruderListProp=" + extruderListProp +
                    ",\n fansListProp=" + fansListProp +
                    '}';
        }

        public int getId() {
            return keyProp.getValue();
        }

        public void setId(int id) {
            keyProp.setValue(id);
        }

        public void setHeadStatus(int headStatus) {
            this.headStatusProp.setValue(headStatus);
        }

        public boolean isActive() {
            return headActiveProp.getValue();
        }

        public void setHeadActive(boolean headActive) {
            this.headActiveProp.setValue(headActive);
        }

        public List<Extruder> getExtruderList() {
            return extruderListProp.getValue();
        }

        public void setExtruderList(List<Extruder> extruderList) {
            this.extruderListProp.setValue(extruderList);
        }

        public void setExtruder(Extruder extruder) {
            extruderListProp.addElement(extruder);
        }

        public List<Fan> getFanList() {
            return fansListProp.getValue();
        }

        public void setFanList(List<Fan> fanList) {
            fansListProp.setValue(fanList);
        }
    }
}
