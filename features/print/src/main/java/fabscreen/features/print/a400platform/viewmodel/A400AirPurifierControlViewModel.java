package fabscreen.features.print.a400platform.viewmodel;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.entity.module.AirPurifier;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt16Prop;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class A400AirPurifierControlViewModel extends BaseViewModel {
    private final AirPurifier mAirPurifier;
    private BehaviorSubject<Boolean> mPowerStatusSubject = BehaviorSubject.createDefault(false);
    // level 1, 2, 3
    private BehaviorSubject<Boolean> mFanOnOffSubject = BehaviorSubject.createDefault(false);
    private BehaviorSubject<Integer> mFanSpeedSubject = BehaviorSubject.createDefault(1);
    private BehaviorSubject<Integer> mFilterLifeSubject = BehaviorSubject.createDefault(1);
    private PublishSubject<Integer> mPurifierErrorSubject = PublishSubject.create();

    public A400AirPurifierControlViewModel() {
        mAirPurifier = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getAirPurifier();

        mAirPurifier.getAirPurifierStatusObservable()
                .as(bindToLifecycle())
                .subscribe(info -> {
                    mPowerStatusSubject.onNext(info.isPowerOn());
                    mPurifierErrorSubject.onNext(info.getModuleStatus());
                    mFanOnOffSubject.onNext(info.isFanOn());
                    mFanSpeedSubject.onNext(info.getFanSpeedLevel());
                    mFilterLifeSubject.onNext(info.getFilterLife());
                }, LogHelper::log);
    }

    public void switchPurifierPower() {
        mAirPurifier.setBlowerSwitch(0, !mFanOnOffSubject.getValue())
                .as(bindToLifecycle())
                .subscribe(result -> {
                }, LogHelper::log);
    }

    public void setPurifierFanSpeed(int level) {
        mAirPurifier.setFanSpeedLevel(0, level)
                .as(bindToLifecycle())
                .subscribe(result -> {
                }, LogHelper::log);
    }

    public Observable<Boolean> getPowerStatusObservable() {
        return mPowerStatusSubject.hide();
    }

    public Observable<Boolean> getFanOnOffObservable() {
        return mFanOnOffSubject.hide();
    }

    public Observable<Integer> getFanSpeedObservable() {
        return mFanSpeedSubject.hide();
    }

    public Observable<Integer> getFilterLifeObservable() {
        return mFilterLifeSubject.hide();
    }

    public Observable<Integer> getPurifierErrorObservable() {
        return mPurifierErrorSubject.hide();
    }

    public void subscribePurifierStatus() {
        mAirPurifier.subscribeAirPurifierStatusChange();
    }

    public void unsubscribePurifierStatus() {
        mAirPurifier.unsubscribeAirPurifierStatusChange();
    }

    public Observable<Boolean> getAutoState(IMachine.WorkType workType) {
        return mAirPurifier.getAutoState(workType)
                .flatMap(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        return Observable.just(((BoolProp) responseStructure.dataProp).getValue());
                    } else {
                        return Observable.just(false);
                    }
                });
    }

    public Observable<Boolean> setAutoState(IMachine.WorkType workType, boolean autoState) {
        return mAirPurifier.setAutoState(workType, autoState)
                .flatMap(responseStructure -> Observable.just(responseStructure.isSuccess()));
    }

    public Observable<Boolean> setDelayStop(IMachine.WorkType workType, int delayTime) {
        return mAirPurifier.setDelayStop(workType, delayTime)
                .flatMap(responseStructure -> Observable.just(responseStructure.isSuccess()));
    }


    public Observable<Integer> getDelayStop(IMachine.WorkType workType) {
        return mAirPurifier.getDelayStop(workType)
                .flatMap(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        return Observable.just(((UInt16Prop) responseStructure.dataProp).getValue());
                    } else {
                        return Observable.just(0);
                    }
                });
    }
}
