package fabscreen.features.print.a400platform.viewmodel;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.entity.module.HeatedBed;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;

public class A400HeatedBedControlViewModel extends BaseViewModel {

    private final MachineController mMachineController;
    private final IMachine mMachine;

    public A400HeatedBedControlViewModel() {
        mMachine = ServiceContainer.getInstance().getService(IMachine.class);
        mMachineController = mMachine.getMachineController();
    }

    public void setAllZonesTemp(int degree) {
        mMachineController.getHeatedBed()
                .setAllTargetTemperature(degree)
                .as(bindToLifecycle())
                .subscribe(result -> {
                }, LogHelper::log);
    }

    public void setZoneTemp(int zoneIndex, int degree) {
        mMachineController.getHeatedBed()
                .setZoneTargetTemperature(zoneIndex, degree)
                .as(bindToLifecycle())
                .subscribe(result -> {
                }, LogHelper::log);
    }


    public Observable<HeatedBed.HeatedBedStatus> getHeatedBedStatusObservable() {
        return mMachineController.getHeatedBed().getHeatedBedStatusSubjectHolder().getObservable();
    }

    public void subscribeTemperatureChange() {
        mMachineController.getHeatedBed().subscribeTemperatureChange();
    }

    public void unSubscribeTemperatureChange() {
        mMachineController.getHeatedBed().unsubscribeTemperatureChange();
    }

    public int getMachineSeriesId() {
        return mMachine.getMachineInfoSubjectHolder().getValue().seriesId;
    }

//    public DeprecatedMachineInfo getMachineStatus() {
//        return MachineStatusManager.getMachineInfoHolder().getValue();
//    }
}
