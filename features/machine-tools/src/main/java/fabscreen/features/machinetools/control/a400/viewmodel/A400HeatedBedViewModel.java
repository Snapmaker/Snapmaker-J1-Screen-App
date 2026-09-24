package fabscreen.features.machinetools.control.a400.viewmodel;

import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.entity.module.HeatedBed;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.data.HeatingStatedata;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class A400HeatedBedViewModel extends BaseViewModel {
    public final int A400_BED_MIN_VALUE = 0;
    public final int A400_BED_MAX_VALUE = 110;
    int mPreheatingValue = 0;
    BehaviorSubject<HeatingStatedata> mZone0StateSubject = BehaviorSubject.createDefault(new HeatingStatedata());
    PublishSubject<Integer> mTargetSubject = PublishSubject.create();
    private MachineController mMachineController;
    private IMachine mMachine;

    public A400HeatedBedViewModel() {
        mMachine = ServiceContainer.getInstance().getService(IMachine.class);
        mMachineController = mMachine.getMachineController();
        mMachineController.getHeatedBed()
                .getHeatedBedStatusSubjectHolder()
                .getObservable()
                .as(bindToLifecycle())
                .subscribe(bedStatus -> {
                    HeatedBed.ZoneInfo zoneInfo = bedStatus.getZoneList().get(0);
                    float temperature = zoneInfo.getCurrentTemperature();
                    float targetTemperature = zoneInfo.getTargetTemperature();
                    HeatingStatedata value = mZone0StateSubject.getValue();
                    value.targetTemperature = targetTemperature;
                    value.temperature = temperature;
                    value.heatingStats = targetTemperature != 0;
                    mZone0StateSubject.onNext(value);
                });
        mTargetSubject.sample(300, TimeUnit.MILLISECONDS)
                .flatMap(integer -> mMachineController.getHeatedBed().setAllTargetTemperature(integer))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                }, LogHelper::log);
    }


    public Observable<HeatedBed.HeatedBedStatus> geZoneStateObservable() {
        return mMachineController.getHeatedBed()
                .getHeatedBedStatusSubjectHolder()
                .getObservable();
    }


    public void setTargetChange(int index, int temp) {
        mTargetSubject.onNext(temp);
    }

    public void subscribeTemperatureChange() {
        mMachineController.getHeatedBed().subscribeTemperatureChange();
    }

    public void unSubscribeTemperatureChange() {
        mMachineController.getHeatedBed().unsubscribeTemperatureChange();
    }

    public HeatedBed.HeatedBedStatus geZoneStatevable() {
        return mMachineController.getHeatedBed().getHeatedBedStatusSubjectHolder().getValue();
    }
}
