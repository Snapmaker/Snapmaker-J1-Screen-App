package fabscreen.features.machinetools.control.common;

import com.orhanobut.logger.Logger;

import java.math.BigDecimal;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.entity.module.DryBox;
import fabscreen.platform.base.service.machine.entity.parts.DryBoxStatus;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class S30DryBoxControlViewModel extends BaseViewModel {

    private final DryBox mDryBox;
    private final BehaviorSubject<Long> mRemainTimeSubject = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> mDryStateSubject = BehaviorSubject.createDefault(false);
    private final BehaviorSubject<Integer> mTargetTempSubject = BehaviorSubject.create();
    private final BehaviorSubject<Integer> mPowerStatusSubject = BehaviorSubject.create();
    private final BehaviorSubject<Integer> mDoorStatusSubject = BehaviorSubject.create();
    private final BehaviorSubject<Float> mCurrentTempSubject = BehaviorSubject.create();
    private final BehaviorSubject<Integer> mHumiditySubject = BehaviorSubject.create();
    private final BehaviorSubject<DryBoxStatus> mDryBoxInfoSubject = BehaviorSubject.create();

    public S30DryBoxControlViewModel() {
        IMachine machine = getServiceContainer().getService(IMachine.class);
        mDryBox = machine.getMachineController().getDryBox();

        mDryBox.getDryBoxStatusHolder().getObservable()
                .as(bindToLifecycle())
                .subscribe(dryBoxInfo -> {
                    Logger.d("---FDT--- dryBoxInfo:" + dryBoxInfo);
                    DryBoxStatus dryBoxStatus = dryBoxInfo.getDryBoxStatus();
                    mDryBoxInfoSubject.onNext(dryBoxStatus);
                    mDryStateSubject.onNext(dryBoxStatus.getDryState() == 1);
                    mCurrentTempSubject.onNext(dryBoxStatus.getTempCurrentChamber());
                    mTargetTempSubject.onNext(dryBoxStatus.getTempTargetChamber());
                    mRemainTimeSubject.onNext(dryBoxStatus.getResidualHeatingTime());
                    mHumiditySubject.onNext(dryBoxStatus.getCurrentHumidity());
                    mPowerStatusSubject.onNext(dryBoxStatus.getHeaterBlockState());
                    mDoorStatusSubject.onNext(dryBoxStatus.getLidState());
                }, LogHelper::log);
    }

    public Observable<Boolean> setTargetTemperature(int temperature) {
        return mDryBox.setTargetTemperature(temperature)
                .flatMap(structure -> Observable.just(structure.isSuccess()));
    }

    public Observable<Boolean> setTargetTime(float timeInHour) {
        return mDryBox.setTargetTime(hourToSeconds(timeInHour))
                .flatMap(structure -> Observable.just(structure.isSuccess()));
    }

    public Observable<Boolean> switchDryState() {
        return mDryBox.switchDryState(mDryStateSubject.getValue() ? 0 : 1)
                .flatMap(response -> Observable.just(response.isSuccess()))
                .doOnNext(aBoolean -> mDryBox.requestInfo());
    }

    public Observable<Long> getRemainTimeObservable() {
        return mRemainTimeSubject.distinctUntilChanged();
    }

    public Observable<Boolean> getDryStateObservable() {
        return mDryStateSubject.distinctUntilChanged();
    }

    public Observable<Integer> getTargetTempObservable() {
        return mTargetTempSubject.distinctUntilChanged();
    }

    public Observable<Float> getCurrentTempObservable() {
        return mCurrentTempSubject.distinctUntilChanged();
    }

    public Observable<Integer> getCurrentHumidityObservable() {
        return mHumiditySubject.distinctUntilChanged();
    }

    public Observable<DryBoxStatus> getDryBoxInfoObservable() {
        return mDryBoxInfoSubject.hide();
    }

    public void subscribeDryBoxStatus() {
        mDryBox.subscribeStatus();
    }

    public void unSubscribeDryBoxStatus() {
        mDryBox.unSubscribeStatus();
    }


    public String formatTime(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = (seconds % 3600) % 60;
        return (h < 10 ? ("0" + h) : h) + ":" + (m < 10 ? ("0" + m) : m) + ":" + (s < 10 ? ("0" + s) : s);
    }

    public int hourToSeconds(float timeInHour) {
        return (int) (timeInHour * 3600);
    }

    public float secondsToHour(long timeInSeconds) {
        if (timeInSeconds == 0) {
            return 0.5f;
        }
        float h = (float) timeInSeconds / 3600;
        return new BigDecimal(h).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue() > 0.5f ? h : 0.5f;
    }

    public String getHMFormatTime(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        return h + "h " + (m < 10 ? ("0" + m) : m) + "min";
    }

    public Observable<Integer> getDoorStatusObservable() {
        return mDoorStatusSubject.distinctUntilChanged();
    }

    public Observable<Integer> getPowerStatusObservable() {
        return mPowerStatusSubject.distinctUntilChanged();
    }
}
