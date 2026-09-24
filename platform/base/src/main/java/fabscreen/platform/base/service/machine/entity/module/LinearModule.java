package fabscreen.platform.base.service.machine.entity.module;

import java.io.IOException;

import fabscreen.platform.base.R;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.lib.SubjectHolder;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import okio.Buffer;

public class LinearModule extends Module {
    private BehaviorSubject<LinearModuleStatus> mStatus = BehaviorSubject.createDefault(new LinearModuleStatus());
    private SubjectHolder<LinearModuleStatus> mLinearModuleStatusSubjectHolder = new SubjectHolder<>(mStatus);
    private CompositeDisposable mDisposables = new CompositeDisposable();

    public LinearModule(ModuleInfo info, IMachine mc, MachineConnectionController cc) {
        super(info, mc, cc);
    }

    @Override
    protected void init() {
        Disposable subscribe = requestInfo().subscribe();
        mDisposables.add(subscribe);
    }

    @Override
    public String getDisplayName() {
        // FIXME: 2022/5/13 string res may be wrong
        return getAppContext().getString(R.string.all_linear_module);
    }

    @Override
    public Observable<ResponseStructure<LinearModuleStatus>> requestInfo() {
        BaseStructure Request = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
            }
        };
        Request.getProp("key").setValue(getModuleInfo().getKey());
        return mConnectionController.request(0x13, 0x01, Request, new ResponseStructure<>(new LinearModuleStatus()))
                .doOnNext(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        mStatus.onNext(responseStructure.dataProp);
                    }
                });
    }

    public Observable<LinearModuleStatus> getLinearModuleStatusObservable() {
        return mLinearModuleStatusSubjectHolder.getObservable();
    }

    public LinearModuleStatus getLinearModuleStatusValue() {
        return mLinearModuleStatusSubjectHolder.getValue();
    }

    public static class LinearModuleStatus implements IStructure {
        private UInt8Prop keyProp = new UInt8Prop();
        private BoolProp isHomeProp = new BoolProp();
        private BoolProp isHaveEnableLimitProp = new BoolProp();
        private BoolProp isEnableLimitProp = new BoolProp();

        public LinearModuleStatus(int key, boolean isHome, boolean isHaveEnableLimit, boolean isEnableLimit) {
            keyProp.setValue(key);
            isHomeProp.setValue(isHome);
            isHaveEnableLimitProp.setValue(isHaveEnableLimit);
            isEnableLimitProp.setValue(isEnableLimit);
        }

        public LinearModuleStatus() {
        }

        @Override
        public byte[] toByteArray() {
            Buffer buffer = new Buffer();
            buffer.write(keyProp.toByteArray());
            buffer.write(isHomeProp.toByteArray());
            buffer.write(isHaveEnableLimitProp.toByteArray());
            buffer.write(isEnableLimitProp.toByteArray());
            return buffer.readByteArray();
        }

        @Override
        public Buffer readBuffer(Buffer buffer) throws IOException {
            keyProp.readBuffer(buffer);
            isHomeProp.readBuffer(buffer);
            isHaveEnableLimitProp.readBuffer(buffer);
            isEnableLimitProp.readBuffer(buffer);
            return buffer;
        }

        public int getKey() {
            return keyProp.getValue();
        }

        public void setKey(int key) {
            keyProp.setValue(key);
        }

        public boolean getIsHome() {
            return isHomeProp.getValue();
        }

        public void setIsHome(boolean isHome) {
            isHomeProp.setValue(isHome);
        }

        public boolean isHaveEnableLimit() {
            return isHaveEnableLimitProp.getValue();
        }

        public void setHaveEnableLimit(boolean haveEnableLimit) {
            isHaveEnableLimitProp.setValue(haveEnableLimit);
        }

        public boolean isEnableLimit() {
            return isEnableLimitProp.getValue();
        }

        public void setEnableLimit(boolean enableLimit) {
            isEnableLimitProp.setValue(enableLimit);
        }
    }
}
