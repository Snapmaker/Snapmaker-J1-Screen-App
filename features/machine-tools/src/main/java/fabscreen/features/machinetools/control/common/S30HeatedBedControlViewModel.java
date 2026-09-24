package fabscreen.features.machinetools.control.common;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.entity.module.HeatedBed;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.data.HeatingStatedata;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class S30HeatedBedControlViewModel extends BaseViewModel {

    private final MachineController mMachineController;
    private final IMachine mMachine;
    int mPreheatingValue = 0;
    BehaviorSubject<HeatingStatedata> mZone0StateSubject = BehaviorSubject.createDefault(new HeatingStatedata(0, 0, false, -1, 60, false));

    public S30HeatedBedControlViewModel() {
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
    }

    public Observable<HeatingStatedata> geZone0StateObservable() {
        return mZone0StateSubject.hide();
    }

    public void subscribeTemperatureChange() {
        mMachineController.getHeatedBed().subscribeTemperatureChange();
    }

    public void unSubscribeTemperatureChange() {
        mMachineController.getHeatedBed().unsubscribeTemperatureChange();
    }

    public void changeHeating(int index, int targetTemperature) {
        HeatingStatedata value;
        if (index == 0) {
            value = mZone0StateSubject.getValue();
            if (targetTemperature == 0) {
                value.targetTemperature = targetTemperature;
            } else {
                if (value.heatingStats) {
                    value.targetTemperature = targetTemperature;
                } else {
                    value.preStopTemperature = targetTemperature;
                }
            }
            mZone0StateSubject.onNext(value);
        }
        mMachineController.getHeatedBed().setAllTargetTemperature(targetTemperature).as(bindToLifecycle()).subscribe(responseStructure -> {
        }, LogHelper::log);
    }

    public void setTargetChange(int index, boolean change) {
        HeatingStatedata value;
        if (index == 0) {
            value = mZone0StateSubject.getValue();
            if (value.targetChange == change) {
                return;
            }
            value.targetChange = change;
            mZone0StateSubject.onNext(value);
            if (!change && value.isHeatingStats()) {
                changeHeating(0, value.getStopTemperature());
            }
        }
    }

    public int getTemperature(int index, int targetTemperature) {
        HeatingStatedata value;
        boolean heatingStats;
        mPreheatingValue = 0;
        if (index == 0) {
            value = mZone0StateSubject.getValue();
            heatingStats = value.heatingStats;
            if (heatingStats) {
                value.preStopTemperature = targetTemperature;
                mZone0StateSubject.onNext(value);
            } else {
                mPreheatingValue = value.preStopTemperature;
            }
        }
        return mPreheatingValue;
    }

    public void changeStopTemperature(int index, int v) {
        if (index == 0) {
            HeatingStatedata value = mZone0StateSubject.getValue();
            if (value.preStopTemperature == v) return;
            value.preStopTemperature = v;
            mZone0StateSubject.onNext(value);
        }
    }

//    public void setAllZonesTemp(int degree) {
//        mMachineController.getHeatedBed()
//                .setAllTargetTemperature(degree)
//                .as(bindToLifecycle())
//                .subscribe(result -> {
//                }, LogHelper::log);
//    }

//    public void setZoneTemp(int zoneIndex, int degree) {
//        mMachineController.getHeatedBed()
//                .setZoneTargetTemperature(zoneIndex, degree)
//                .as(bindToLifecycle())
//                .subscribe(result -> {
//                }, LogHelper::log);
//    }


//    public Observable<HeatedBed.HeatedBedStatus> getHeatedBedStatusObservable() {
//        return mMachineController.getHeatedBed().getHeatedBedStatusSubjectHolder().getObservable();
//    }


//    public int getMachineSeriesId() {
//        return mMachine.getMachineInfoSubjectHolder().getValue().seriesId;
//    }

//    public DeprecatedMachineInfo getMachineStatus() {
//        return MachineStatusManager.getMachineInfoHolder().getValue();
//    }
}
