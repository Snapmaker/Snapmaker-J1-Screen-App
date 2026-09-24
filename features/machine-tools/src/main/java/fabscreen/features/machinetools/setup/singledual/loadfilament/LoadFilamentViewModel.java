package fabscreen.features.machinetools.setup.singledual.loadfilament;

import com.orhanobut.logger.Logger;

import java.util.List;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.FDMController;
import fabscreen.platform.base.service.machine.entity.parts.Extruder;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.data.MoveController;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class LoadFilamentViewModel extends BaseViewModel {

    private final FDMController mFdmController;
    private final BehaviorSubject<Integer> mLoadFilamentSubject = BehaviorSubject.create();
    private final BehaviorSubject<Float> mE0TemperatureSubject = BehaviorSubject.create();
    private final BehaviorSubject<Float> mE1TemperatureSubject = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> mHeatingResultSubject = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> mIsLoadingSubject = BehaviorSubject.createDefault(false);
    private final BehaviorSubject<Boolean> mIsHeatingSubject = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> mIsMovingSubject = BehaviorSubject.createDefault(false);
    private int mCurrentLoading;
    private boolean mE0Last = true;
    private boolean mE1Last = true;
    private List<Extruder> mExtruders;


    public LoadFilamentViewModel() {
        IMachine machine = getServiceContainer().getService(IMachine.class);
        mFdmController = machine.getFDMController();
        // Home first
        machine.getMachineController().home(0)
                .doOnSubscribe(disposable -> mIsMovingSubject.onNext(true))
                .flatMap(result -> MoveController.getInstance().stepToPosition(MoveController.Direction.DOWN, 100, 3000))
                .flatMap(response -> MoveController.getInstance().stepToPosition(MoveController.Direction.RIGHT, 200, 6000))
                .flatMap(response -> MoveController.getInstance().stepToPosition(MoveController.Direction.FORWARD, 100, 6000))
                .doOnNext(result -> mIsMovingSubject.onNext(false))
                .doOnError(e -> mIsMovingSubject.onNext(false))
                .as(bindToLifecycle())
                .subscribe(result -> {
                },LogHelper::log);
        watchExtruderChange();
    }

    public void heatExtruders() {
        setExtruderTemperature(0, 205);
        setExtruderTemperature(1, 205);

        // waiting for extruder0 temperature reaching 205, ignoring extruder1.
        mIsHeatingSubject.onNext(true);
        mE0TemperatureSubject
                .doOnSubscribe(disposable -> mIsHeatingSubject.onNext(true))
                .filter(temperature -> temperature >= 205)
                .take(1)
                .doOnNext(temperature -> mIsHeatingSubject.onNext(false))
                .as(bindToLifecycle())
                .subscribe(temperature -> {
//                    mIsHeatingSubject.onNext(false);
                }, LogHelper::log);
    }

    private void watchExtruderChange() {
        mFdmController.getToolheadStatusSubjectHolder().getObservable()
                .as(bindToLifecycle())
                .subscribe(status -> {
                    Logger.d(status);
                    mExtruders = status.getExtruderList();
                    mE0TemperatureSubject.onNext(mExtruders.get(0).getTemperature());
                    mE1TemperatureSubject.onNext(mExtruders.get(1).getTemperature());
                    boolean e0FilamentDetected = mExtruders.get(0).getFilamentStatus();
                    boolean e1FilamentDetected = mExtruders.get(1).getFilamentStatus();
                    onDetectionFilament(e0FilamentDetected, e1FilamentDetected);
                }, LogHelper::log);
    }

    private void onDetectionFilament(boolean e0, boolean e1) {
        Logger.d("last e0 is %1$s, e0 is %2$s, last e1 is %3$s, e1 is %4$s", mE0Last, e0, mE1Last, e1);
        if (!e0 && mE0Last) {
            onFilamentTriggered(0);
        }

        if (!e1 && mE1Last) {
            onFilamentTriggered(1);
        }
        mE0Last = e0;
        mE1Last = e1;
    }

    private void onFilamentTriggered(int index) {
        float eTemperature = mExtruders.get(index).getTemperature();
        // Expect extruder temperature 205 celsius degrees, but we add more tolerance for temperature fluctuation(Current -5).
        if (eTemperature <= 200) {
            // not hot enough, ignore.
            Logger.d("Extruder %d temperature not enough, current %.0f", index, eTemperature);
            return;
        }

        if (mIsLoadingSubject.getValue()) {
            // is already loading, ignore
            Logger.d("is loading, ignore, %d", index);
            return;
        }

        if (mCurrentLoading == index) {
            startExtrudeFilament(mCurrentLoading);
        }
    }

    public void startExtrudeFilament(int index) {
        mFdmController.requestActivatedExtrusion(0, 90, 240, 0, 0)
                .doOnSubscribe(disposable -> mIsLoadingSubject.onNext(true))
                .doOnNext(response -> mIsLoadingSubject.onNext(false))
                .as(bindToLifecycle())
                .subscribe(response -> {
                    if (response.isSuccess()) {
                        mLoadFilamentSubject.onNext(index);
                    } else {
                        throw new IllegalStateException("Extruder" + index + "extrude fail");
                    }
                }, LogHelper::log);
    }

    public void confirmLoad(int index) {
        // Cool down current loading Extruder with "stand by temperature".
        setExtruderTemperature(mCurrentLoading, 150);
        // We had at most 2 extruder for the loading procedure,
        // so we will load next extruder if unload extruder available.
        if (index < 2) {
            loadFilament(index);
        }
    }

    private void setExtruderTemperature(int index, int temperature) {
        mFdmController.setExtruderTemperature(0, index, temperature)
                .as(bindToLifecycle())
                .subscribe(response -> {
                }, LogHelper::log);
    }

    public void loadFilament(int extruderIndex) {

        // reset to true for wrong status before load
        if (extruderIndex == 0) {
            mE0Last = true;
        } else {
            mE1Last = true;
        }
        mCurrentLoading = extruderIndex;

        mFdmController.switchExtruder(0, extruderIndex)
                .as(bindToLifecycle())
                .subscribe(response -> {
                }, LogHelper::log);
    }

    public Observable<Integer> getLoadFilamentResultObservable() {
        return mLoadFilamentSubject.hide();
    }

    public void unsubscribeExtruder() {
        mFdmController.unSubscribeExtruderChange();
    }

    public void subscribeExtruder() {
        mFdmController.subscribeExtruderChange();
    }

    public Observable<Boolean> getHeatingObservable() {
        return mIsHeatingSubject.hide();
    }

    public Observable<Boolean> getLoadingObservable() {
        return mIsLoadingSubject.hide();
    }

    public Observable<Boolean> getMovingObservable() {
        return mIsMovingSubject.hide();
    }
}
