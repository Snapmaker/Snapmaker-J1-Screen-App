package fabscreen.features.machinetools.calibration.j1Platform.levelingBed;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.FDMController;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.prop.FloatProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

public class LevelingBedAuxiliaryCalibrationModel extends BaseViewModel {
    public static final int FIRST_CALIBRATION = 1;
    public static final int SECOND_CALIBRATION = 2;
    public static final int THIRD_CALIBRATION = 3;
    FDMController fdmController;
    private int mCount;
    private boolean mGetZHeightDifferenceState = false;
    private Subject<Boolean> mIsMovingSubject = BehaviorSubject.create();
    private Subject<Float> mProgressSubject = BehaviorSubject.create();
    private Subject<Boolean> GetZHeight = BehaviorSubject.create();
    private Subject<ResponseStructure> onErrorSubject = BehaviorSubject.create();

    public LevelingBedAuxiliaryCalibrationModel() {
        super();
        fdmController = ServiceContainer.getInstance().getService(IMachine.class).getFDMController();
        mIsMovingSubject
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aBoolean -> {
                    if (mCount == 2 || mCount == 3) {
                        mGetZHeightDifferenceState = !aBoolean;
                        GetZHeight.onNext(mGetZHeightDifferenceState);
                    }
                });

        fdmController.watchHeightDifferenceState()
                .takeUntil(responseStructure -> !responseStructure.isSuccess())
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    if (mGetZHeightDifferenceState && responseStructure.isSuccess()) {
                        BaseStructure baseStructure = (BaseStructure) responseStructure.dataProp;
                        int index = ((UInt8Prop) (baseStructure.getProp("index"))).getValue();
                        float heightDifference = ((FloatProp) (baseStructure.getProp("heightDifference"))).getValue();
                        mProgressSubject.onNext(heightDifference);
                    } else if (mGetZHeightDifferenceState && responseStructure.resultProp.getValue().equals(200)) {
                        mProgressSubject.onNext(-999f);
                    }
                });

        GetZHeight
                .flatMap(this::setGetHeightDifferenceState)
                .flatMap(responseStructure ->
                        responseStructure.isSuccess() ?
                                fdmController.subscribeGetHeightDifference() : Observable.just(responseStructure))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(resultStructure -> {
                });
    }

    public Observable<ResponseStructure> setGetHeightDifferenceState(boolean state) {
        return fdmController.setGetHeightDifferenceState(state);
    }

    public Observable<Float> getProgress() {
        return mProgressSubject.hide();
    }

    public Observable<ResponseStructure> exitCalibration(boolean save) {
        return fdmController.exitCalibration(save);
    }

    public Observable<ResponseStructure> unSubscribeGetZHeightDifference() {
        return fdmController.unSubscribeGetZHeightDifference();
    }

    public Observable<ResponseStructure> getOnErrorSubject() {
        return onErrorSubject.hide();
    }

    public Observable<Boolean> getIsMovingObservable() {
        return mIsMovingSubject.hide();
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        mCount = count;
    }

    public void setGetZhightState(boolean state) {
        GetZHeight.onNext(state);
    }

    public void initContent(int count) {
        mCount = count;
        mIsMovingSubject.onNext(false);
    }

    public void moveContent(int count, boolean autoCalibrate) {
        mCount = count;
        mIsMovingSubject.onNext(true);
        fdmController.calibratePointByIndex(count, autoCalibrate)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    if (success.isSuccess()) {
                        mIsMovingSubject.onNext(false);
                    } else {
                        onErrorSubject.onNext(success);
                    }
                }, LogHelper::log);

    }

    public void init() {
        fdmController.setCalibrationMode(0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    if (success.isSuccess()) {
                        moveContent(1, true);
                    }
                }, LogHelper::log);
    }
}
