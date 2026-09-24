package fabscreen.features.machinetools.control.a350.modules.jog;

import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.model.system.DeprecatedMachineInfo;
import fabscreen.platform.base.model.system.MachineStatusManager;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class ControlJogViewModel extends BaseViewModel {
    private BehaviorSubject<Boolean> mMovingSubject = BehaviorSubject.createDefault(false);

    public boolean isRotaryAvailable() {
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isRotaryAvailable;
    }

    public Observable<DeprecatedMachineInfo> getMachineStatusObservable() {
        return MachineStatusManager.getMachineInfoHolder().getObservable();
    }

    public void _3DPGoHome() {
        mMovingSubject.onNext(true);
        // Note that this widget is only used for 3DP
        // We simply use CS#0, thus no coordinate system need to be updated
         ServiceContainer.getInstance().getService(IMachine.class).getMachineController().home(0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    mMovingSubject.onNext(false);
                }, e -> {
                    LogHelper.log(e);
                    mMovingSubject.onNext(false);
                });
    }

    public Observable<Boolean> getHomeButtonMovingObservable() {
        return mMovingSubject.hide();
    }
}
