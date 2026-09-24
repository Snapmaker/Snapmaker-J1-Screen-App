package fabscreen.features.machinetools.calibration.j1Platform.viewmodel;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.FDMController;
import fabscreen.platform.base.service.machine.entity.module.HeatedBed;
import fabscreen.platform.base.service.machine.entity.parts.Extruder;
import fabscreen.platform.base.service.machine.entity.toolhead.FdmToolhead;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class NozzleBedHeatingViewModel extends BaseViewModel {
    FDMController fdmController;
    private Observable<FdmToolhead.FdmToolheadStatus> mLFdmToolheadStatusObservable;
    private Observable<FdmToolhead.FdmToolheadStatus> mRFdmToolheadStatusObservable;
    private Observable<HeatedBed.HeatedBedStatus> mBedStatusObservable;
    private Observable<Boolean> mNextObservable;
    private BehaviorSubject<Boolean> mLeftTemperatureState = BehaviorSubject.createDefault(false);
    private BehaviorSubject<Boolean> mRightTemperatureState = BehaviorSubject.createDefault(false);
    private BehaviorSubject<Boolean> mBedTemperatureState = BehaviorSubject.createDefault(false);


    public NozzleBedHeatingViewModel() {
        fdmController = ServiceContainer.getInstance().getService(IMachine.class).getFDMController();
        heating();
        mLFdmToolheadStatusObservable = fdmController
                .getToolheadStatusSubjectHolder(0)
                .getObservable()
                .doOnNext(fdmToolheadStatus -> {
                    Extruder extruder = fdmToolheadStatus.getExtruderList().get(0);
                    int currentTemperature = (int) extruder.getTemperature();
                    int targetTemperature = (int) extruder.getTargetTemperature();
                    mLeftTemperatureState.onNext(targetTemperature != 0 && currentTemperature >= targetTemperature - 3);
                });
        mRFdmToolheadStatusObservable = fdmController
                .getToolheadStatusSubjectHolder(1)
                .getObservable()
                .doOnNext(fdmToolheadStatus -> {
                    Extruder extruder = fdmToolheadStatus.getExtruderList().get(0);
                    int currentTemperature = (int) extruder.getTemperature();
                    int targetTemperature = (int) extruder.getTargetTemperature();
                    mRightTemperatureState.onNext(targetTemperature != 0 && currentTemperature >= targetTemperature - 3);
                });
        mBedStatusObservable = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getHeatedBed()
                .getHeatedBedStatusSubjectHolder()
                .getObservable()
                .doOnNext(bedStatus -> {
                    HeatedBed.ZoneInfo zoneInfo = bedStatus.getZoneList().get(0);
                    int currentTemperature = (int) zoneInfo.getCurrentTemperature();
                    int targetTemperature = zoneInfo.getTargetTemperature();
                    mBedTemperatureState.onNext(targetTemperature != 0 && currentTemperature >= targetTemperature - 3);

                });

        mNextObservable = Observable.zip(mLeftTemperatureState, mRightTemperatureState, mBedTemperatureState, NozzleBedHeatingViewModel::getTemperatureState);
    }

    private static Boolean getTemperatureState(Boolean leftTemperatureState, Boolean rightTemperatureState, boolean bedTemperatureState) {
        return leftTemperatureState && rightTemperatureState && bedTemperatureState;
    }

    public void heating() {
        fdmController.setExtruderTemperature(0, 0, 220)
                .flatMap(success -> success.isSuccess() ? fdmController.setExtruderTemperature(1, 0, 220) : Observable.just(success))
                .flatMap(success -> success.isSuccess() ? ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getHeatedBed().setZoneTargetTemperature(0, 60) : Observable.just(success))
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    if (!responseStructure.isSuccess()) {
                        Logger.d("Set extruder temperature %d  return abnormal %d",
                                220,
                                responseStructure.resultProp.getValue());
                    }
                }, LogHelper::log);
    }

    public void subscribeTemperatureChange() {
        ServiceContainer.getInstance().getService(IMachine.class)
                .getFDMController().subscribeExtruderChange();
        ServiceContainer.getInstance().getService(IMachine.class)
                .getMachineController().getHeatedBed().subscribeTemperatureChange();
    }

    public void unsubscribeTemperatureChange() {
        ServiceContainer.getInstance().getService(IMachine.class)
                .getFDMController().unSubscribeExtruderChange();
        ServiceContainer.getInstance().getService(IMachine.class)
                .getMachineController().getHeatedBed().unsubscribeTemperatureChange();
    }

    public Observable<FdmToolhead.FdmToolheadStatus> getLFdmToolheadStatusObservable() {
        return mLFdmToolheadStatusObservable;
    }

    public Observable<FdmToolhead.FdmToolheadStatus> getRFdmToolheadStatusObservable() {
        return mRFdmToolheadStatusObservable;
    }

    public Observable<HeatedBed.HeatedBedStatus> getBedStatusObservable() {
        return mBedStatusObservable;
    }

    public Observable<Boolean> getNextObservable() {
        return mNextObservable;
    }
}
