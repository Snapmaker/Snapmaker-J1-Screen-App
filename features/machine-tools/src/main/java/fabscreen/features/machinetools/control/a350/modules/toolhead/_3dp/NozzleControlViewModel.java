package fabscreen.features.machinetools.control.a350.modules.toolhead._3dp;

import com.orhanobut.logger.Logger;

import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.Constants;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.model.system.DeprecatedMachineInfo;
import fabscreen.platform.base.model.system.MachineStatusManager;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class NozzleControlViewModel extends BaseViewModel {
    private BehaviorSubject<Boolean> mIsTemperatureEnoughSubject = BehaviorSubject.createDefault(false);
    private BehaviorSubject<Boolean> mIsLoadingSubject = BehaviorSubject.createDefault(false);
    private BehaviorSubject<Boolean> mIsUnLoadingSubject = BehaviorSubject.createDefault(false);

    public void requestSetTargetDegree(Integer degree) {
        ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("M104 S" + degree)
                .as(bindToLifecycle())
                .subscribe(res -> {
                }, LogHelper::log);
    }

    public Observable<DeprecatedMachineInfo> getMachineStatusObservable() {
        return MachineStatusManager.getMachineInfoHolder().getObservable()
                .throttleLast(Constants.THROTTLE_DURATION, TimeUnit.MILLISECONDS)
                .doOnNext(machineStatus -> {
                    // Enable filament buttons until print head temperature up to target temperature
                    boolean enough = (machineStatus.leftNozzleTemperature >= 175
                            && machineStatus.leftNozzleTemperature + 5 >= machineStatus.leftNozzleTargetTemperature);
                    mIsTemperatureEnoughSubject.onNext(enough);
                });
    }

    public DeprecatedMachineInfo getMachineStatus() {
        return MachineStatusManager.getMachineInfoHolder().getValue();
    }

    public Observable<Boolean> getReadyToLoadObservable() {
        return Observable.combineLatest(
                mIsLoadingSubject,
                mIsUnLoadingSubject,
                mIsTemperatureEnoughSubject.distinctUntilChanged(),
                (isLoading, isUnloading, enough) -> !isLoading && !isUnloading & enough);

    }

    public Observable<Boolean> getLoadingObservable() {
        return mIsLoadingSubject.hide();
    }

    public Observable<Boolean> getUnloadingObservable() {
        return mIsUnLoadingSubject.hide();
    }

    public void loadFilament() {
        Logger.i("Loading filament...");
        mIsLoadingSubject.onNext(true);
        ServiceContainer.getInstance().getService(IMachine.class).getFDMController().requestActivatedExtrusion(0, 60, 200, 0, 0)
                .as(bindToLifecycle())
                .subscribe(success -> {
                    mIsLoadingSubject.onNext(false);
                    Logger.d("Filament loaded.");
                }, e -> {
                    LogHelper.log(e);
                    mIsLoadingSubject.onNext(false);
                });
    }


    public void unloadFilament() {
        Logger.i("Unloading filament...");
        mIsUnLoadingSubject.onNext(true);
        ServiceContainer.getInstance().getService(IMachine.class).getFDMController().requestActivatedExtrusion(0, 6, 200, 60, 150)
                .as(bindToLifecycle())
                .subscribe(success -> {
                    mIsUnLoadingSubject.onNext(false);
                    Logger.d("Filament unloaded.");
                }, e -> {
                    LogHelper.log(e);
                    mIsUnLoadingSubject.onNext(false);
                });
    }
}
