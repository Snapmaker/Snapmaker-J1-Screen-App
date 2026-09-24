package fabscreen.platform.base.service.machine.entity.module;

import fabscreen.platform.base.R;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.lib.SubjectHolder;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;

// TODO: Does an emergency stop require an active subscription?
public class EmergencyButton extends Module {
    private BehaviorSubject<Boolean> mStatus = BehaviorSubject.createDefault(false);
    private SubjectHolder<Boolean> mEmergencyButtonStatusSubjectHolder = new SubjectHolder<>(mStatus);

    private CompositeDisposable mDisposables = new CompositeDisposable();

    public EmergencyButton(ModuleInfo info, IMachine mc, MachineConnectionController cc) {
        super(info, mc, cc);
    }

    @Override
    public void init() {
//        Disposable subscribe = mConnectionController.watch(0x01, 0xa3, new ResponseStructure<>(new BoolProp()))
//                .subscribe(boolPropResponseStructure -> {
//                    if (boolPropResponseStructure.isSuccess() && (!boolPropResponseStructure.dataProp.getValue().equals(mStatus.getValue()))) {
//                        mStatus.onNext(boolPropResponseStructure.dataProp.getValue());
//                    }
//                });
//        mDisposables.add(subscribe);
    }

    @Override
    public String getDisplayName() {
        return getAppContext().getString(R.string.all_emergency_stop);
    }

    @Override
    public Observable<ResponseStructure> requestInfo() {
        // No inherent properties
        return null;
    }

    public Observable<Boolean> getEmergencyObservable() {
        return mEmergencyButtonStatusSubjectHolder.getObservable();
    }

    public boolean getEmergencyValue() {
        return mEmergencyButtonStatusSubjectHolder.getValue();
    }
}
