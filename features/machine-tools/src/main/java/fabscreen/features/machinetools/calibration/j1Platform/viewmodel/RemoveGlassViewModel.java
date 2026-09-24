package fabscreen.features.machinetools.calibration.j1Platform.viewmodel;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.entity.module.HeatedBed;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class RemoveGlassViewModel extends BaseViewModel {
    private BehaviorSubject<Integer> mBedTemperatureSubj = BehaviorSubject.create();

    public RemoveGlassViewModel() {
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getHeatedBed()
                .getHeatedBedStatusSubjectHolder()
                .getObservable()
                .as(bindToLifecycle())
                .subscribe(bedStatus -> {
                    HeatedBed.ZoneInfo zoneInfo = bedStatus.getZoneList().get(0);
                    int temperature = (int) zoneInfo.getCurrentTemperature();
                    mBedTemperatureSubj.onNext(temperature);
                }, LogHelper::log);
    }

    public Observable<Integer> getBedTempObservable() {
        return mBedTemperatureSubj.hide();
    }

    public void subscribeHeatedBedChange() {
        ServiceContainer.getInstance().getService(IMachine.class)
                .getMachineController().getHeatedBed().subscribeTemperatureChange();
    }

    public void unsubscribeHeatedBedChange() {
        ServiceContainer.getInstance().getService(IMachine.class)
                .getMachineController().getHeatedBed().unsubscribeTemperatureChange();
    }
}
