package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingZ;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.controller.FDMController;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.data.MoveController;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

public class A400LevelingZViewModel extends BaseViewModel {
    int calibrationMode;
    IMachine iMachine;
    FDMController fdmController;
    MachineController machineController;
    private BehaviorSubject<Boolean> mIsMovingSubject = BehaviorSubject.createDefault(false);
    private BehaviorSubject<Boolean> mIsMovePopUpSubject = BehaviorSubject.createDefault(false);
    private BehaviorSubject<Integer> mResultSubject;
    private int extruderIndex = 0;
    private Disposable subscribe;

    public A400LevelingZViewModel() {
        super();
        IPreferences.Helper helper = ServiceContainer.getInstance().getService(IPreferences.class).getHelper();
        iMachine = getServiceContainer().getService(IMachine.class);
        fdmController = iMachine.getFDMController();
        machineController = iMachine.getMachineController();
        calibrationMode = helper.getA400LevelingZCalibrationMode();
        mResultSubject = BehaviorSubject.createDefault(-1);
    }

    public void startCalibration() {
        A400LevelingZSensorCalibration(0);
        if (subscribe != null) subscribe.dispose();
    }

    public int getExtruderIndex() {
        return extruderIndex;
    }

    public Observable<Integer> getResultObservable() {
        return mResultSubject.hide();
    }

    public void A400LevelingZCalibration(int index) {
        extruderIndex = index;
        if (subscribe != null) subscribe.dispose();
        mIsMovePopUpSubject.onNext(true);
        subscribe = fdmController.startZOffsetCalibration(extruderIndex)
                .as(bindToLifecycle())
                .subscribe(success -> {
                    if (success.isSuccess()) {
                        mIsMovePopUpSubject.onNext(false);
                        mResultSubject.onNext(extruderIndex);
                    } else {
                        mIsMovePopUpSubject.onNext(false);
                        Logger.d("---FDT--- startA400LevelingZCalibrationMode Error: " + success);
                    }
                }, e -> {
                    LogHelper.log(e);
                    mIsMovePopUpSubject.onNext(false);
                });
    }

    public void A400LevelingZSensorCalibration(int index) {
        extruderIndex = index;
        if (subscribe != null) subscribe.dispose();
        mIsMovePopUpSubject.onNext(true);
        subscribe = fdmController.startExtruderSensorCalibration(extruderIndex)
                .as(bindToLifecycle())
                .subscribe(success -> {
                    if (success.isSuccess()) {
                        mIsMovePopUpSubject.onNext(false);
                        mResultSubject.onNext(extruderIndex);
                    } else {
                        Logger.d("---FDT--- startA400LevelingZCalibrationMode Error: " + success);
                    }
                }, e -> {
                    LogHelper.log(e);
                    mIsMovePopUpSubject.onNext(false);
                });
    }

    public Observable<ResponseStructure> exitCalibration() {
        return fdmController.exitCalibration(true);
    }

    public void move(MoveController.Direction direction, float stepWidth) {
        mIsMovingSubject.onNext(true);
        MoveController.getInstance()
                .stepToPosition(direction, stepWidth)
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    mIsMovingSubject.onNext(false);
                }, LogHelper::log);
    }

    public Observable<Boolean> checkHome() {
        IMachine service = ServiceContainer.getInstance().getService(IMachine.class);
        if (!service.getMachineStatusSubjectHolder().getValue().isHomed) {
            mIsMovePopUpSubject.onNext(true);
            return service.getMachineController().updateCoordinateSystem(0)
                    .flatMap(machineStatus -> service.getMachineController().home(0))
                    .doOnNext(machineStatus -> {
                        mIsMovePopUpSubject.onNext(false);
                    })
                    .flatMap(integer -> Observable.just(integer == 0));
        } else {
            return Observable.just(true);
        }
    }

    public Observable<Boolean> getIsMovePopUpObservable() {
        return mIsMovePopUpSubject.hide();
    }

    public Observable<Boolean> getIsMovingObservable() {
        return mIsMovingSubject.hide();
    }


}
