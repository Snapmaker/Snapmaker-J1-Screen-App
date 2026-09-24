package fabscreen.features.machinetools.control.common;

import android.util.SparseArray;
import android.util.SparseBooleanArray;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.controller.FDMController;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.entity.parts.Extruder;
import fabscreen.platform.base.service.machine.entity.toolhead.FdmToolhead;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class S30FilamentControlViewModel extends BaseViewModel {
    private final FDMController mFdmController;
    private final MachineInfo mMachineInfo;

    // TODO: 2022/5/6 use arrays instead
    private final SparseArray<Float> mMachineTargetTemps = new SparseArray<>();
    private final SparseArray<Float> mViewTargetTemps = new SparseArray<>();
    private final SparseArray<Float> mCurrentTemps = new SparseArray<>();
    private final SparseBooleanArray mHeatOnArray = new SparseBooleanArray();

    private final BehaviorSubject<SparseArray<Float>> mTargetTempSubj = BehaviorSubject.create();
    private final PublishSubject<SparseArray<Float>> mCurrentTempSubj = PublishSubject.create();
    private final BehaviorSubject<SparseBooleanArray> mHeatOnSubj = BehaviorSubject.create();
    private final PublishSubject<Boolean> mIsMovingSubj = PublishSubject.create();
    private final PublishSubject<Boolean> mExtruderPositionErrorSubj = PublishSubject.create();

    private BehaviorSubject<Boolean> mJ1PositionGoodSubj;

    private final MachineController mMachineController;
    private float mTargetX;
    private int mNeedMoveIndex;
    private int mOriginJ1Toolhead;
    private final IPreferences mPreferences;

    public S30FilamentControlViewModel() {
        IMachine machine = ServiceContainer.getInstance().getService(IMachine.class);
        mPreferences = ServiceContainer.getInstance().getService(IPreferences.class);
        mMachineController = machine.getMachineController();
        mFdmController = machine.getFDMController();
        mMachineInfo = machine.getMachineInfoSubjectHolder().getValue();


        mFdmController.subscribeExtruderChange();
        watchToolheadStatus();

        if (isJ1()) {
            mViewTargetTemps.put(0, mPreferences.getHelper().getJ1LeftTargetTemp());
            mViewTargetTemps.put(1, mPreferences.getHelper().getJ1RightTargetTemp());
        }

        mTargetTempSubj.onNext(mViewTargetTemps);
    }

    public void setHeatOn(int index, boolean on) {
        if (on) {
            if (isJ1()) {
                // Start heat, request set to display target temp.
                if (!mViewTargetTemps.get(index).equals(mMachineTargetTemps.get(index))) {
                    requestSetExtruderTemp(index, mViewTargetTemps.get(index).intValue());
                }
            } else {
                requestSetExtruderTemp(index, 200);
            }

        } else {
            // Stop heat, this won't affect the target temp display.
            requestSetExtruderTemp(index, 0);
        }

    }

    private void watchToolheadStatus() {
        if (isJ1()) {
            // J1 dual head.
            Observable<FdmToolhead.FdmToolheadStatus> toolhead1Observable = mFdmController.getToolheadStatusSubjectHolder(0).getObservable();
            Observable<FdmToolhead.FdmToolheadStatus> toolhead2Observable = mFdmController.getToolheadStatusSubjectHolder(1).getObservable();

            Observable.zip(toolhead1Observable, toolhead2Observable, (toolhead0Status, toolhead1Status) -> {
                List<Extruder> extruderList = new ArrayList<>();
                extruderList.addAll(toolhead0Status.getExtruderList());
                extruderList.addAll(toolhead1Status.getExtruderList());
                return extruderList;
            }).as(bindToLifecycle()).subscribe(this::onExtrudersChange, LogHelper::log);
        } else {
            // Single head.
            mFdmController.getToolheadStatusSubjectHolder().getObservable()
                    .flatMap(fdmToolheadStatus -> Observable.just(fdmToolheadStatus.getExtruderList()))
                    .as(bindToLifecycle())
                    .subscribe(this::onExtrudersChange, LogHelper::log);
        }
    }

    private void onExtrudersChange(List<Extruder> extruders) {
        mCurrentTemps.put(0, extruders.get(0).getTemperature());
        mCurrentTemps.put(1, extruders.get(1).getTemperature());
        mCurrentTempSubj.onNext(mCurrentTemps);

        mMachineTargetTemps.put(0, extruders.get(0).getTargetTemperature());
        mMachineTargetTemps.put(1, extruders.get(1).getTargetTemperature());

        mHeatOnArray.put(0, mMachineTargetTemps.get(0) > 0);
        mHeatOnArray.put(1, mMachineTargetTemps.get(1) > 0);
        mHeatOnSubj.onNext(mHeatOnArray);

        /*
               J1: Sync machine target if switch on, use local target if switch off.
           not J1: Always sync machine target.
         */
        if (isJ1()) {
            if (mHeatOnArray.get(0)) {
                mViewTargetTemps.put(0, mMachineTargetTemps.get(0));
                mTargetTempSubj.onNext(mViewTargetTemps);
            }

            if (mHeatOnArray.get(1)) {
                mViewTargetTemps.put(1, mMachineTargetTemps.get(1));
                mTargetTempSubj.onNext(mViewTargetTemps);
            }
        } else {
            mViewTargetTemps.put(0, mMachineTargetTemps.get(0));
            mViewTargetTemps.put(1, mMachineTargetTemps.get(1));
            mTargetTempSubj.onNext(mViewTargetTemps);
        }
    }

    public Observable<SparseArray<Float>> getTargetTempObservable() {
        return mTargetTempSubj.hide();
    }

    public Observable<SparseArray<Float>> getCurrentTempObservable() {
        return mCurrentTempSubj.hide();
    }

    public Observable<SparseBooleanArray> getHeatOnObservable() {
        return mHeatOnSubj.hide();
    }

    public SparseBooleanArray getHeatOn() {
        return mHeatOnSubj.getValue();
    }

    public Observable<Boolean> getIsMovingObservable() {
        return mIsMovingSubj.hide();
    }

    public Observable<Boolean> getExtruderPositionErrorObservable() {
        return mExtruderPositionErrorSubj.hide();
    }


    public Observable<Boolean> switchExtruder(int toolheadIndex, int extruderIndex) {
        // Do switch.
        return mFdmController.switchExtruder(toolheadIndex, extruderIndex).flatMap(responseStructure -> Observable.just(responseStructure.resultProp.getValue() == 0));
    }

    /**
     * Load filament for specified extruder. Activate the toolhead and extruder if it's not.
     *
     * @param pageExtruderIndex The specified extruderIndex show on page(ignore which head it belongs to).
     */
    public Observable<ResponseStructure> loadFilament(int pageExtruderIndex) {
        Logger.i("Loading filament...");
        int toolheadIndex;
        int extruderIndex;
        if (isJ1()) {
            toolheadIndex = pageExtruderIndex;
            extruderIndex = 0;
            // FIXME: not tested feature on machine, temporary comment this and use workaround method.
            /*
            switchExtruder(toolheadIndex, extruderIndex).filter(switched -> switched).flatMap((Function<Boolean, ObservableSource<ResponseStructure<IStructure>>>)
                    switched -> mFdmController.requestExtruderMovement(toolheadIndex, 1, -1, 200))
                    .as(bindToLifecycle())
                    .subscribe(structure -> {
                        if (structure.isSuccess()) {
                            Logger.d("Filament loaded.");
                        } else {
                            Logger.d("Filament load fail.");
                        }
                    }, LogHelper::log);

             */
            // Roll back extrusion method for available use.
            return switchExtruder(toolheadIndex, extruderIndex).filter(switched -> switched).flatMap((Function<Boolean, ObservableSource<ResponseStructure>>)
                    switched -> mFdmController.requestActivatedExtrusion(0, 60, 200, 0, 0));
//                    .as(bindToLifecycle())
//                    .subscribe(structure -> {
//                        if (structure.isSuccess()) {
//                            Logger.d("Filament loaded.");
//                        } else {
//                            Logger.d("Filament load fail.");
//                        }
//                    }, LogHelper::log);
        } else {
            toolheadIndex = 0;
            extruderIndex = pageExtruderIndex;
            return switchExtruder(toolheadIndex, extruderIndex).filter(switched -> switched).flatMap((Function<Boolean, ObservableSource<ResponseStructure>>)
                    switched -> mFdmController.requestActivatedExtrusion(0, 100, 200, 0, 0));
//                    .as(bindToLifecycle())
//                    .subscribe(structure -> {
//                        if (structure.isSuccess()) {
//                            Logger.d("Filament loaded.");
//                        } else {
//                            Logger.d("Filament load fail.");
//                        }
//                    }, LogHelper::log);
        }
    }


    public Observable<ResponseStructure> unloadFilament(int pageExtruderIndex) {
        Logger.i("Unloading filament...");
        int toolheadIndex;
        int extruderIndex;
        if (isJ1()) {
            toolheadIndex = pageExtruderIndex;
            extruderIndex = 0;
            // FIXME: not tested feature on machine, temporary comment this and use workaround method.
            /*
            switchExtruder(toolheadIndex, extruderIndex).filter(switched -> switched).flatMap((Function<Boolean, ObservableSource<ResponseStructure<IStructure>>>)
                    switched -> mFdmController.requestExtruderMovement(toolheadIndex, 0, -1, 200))
                    .as(bindToLifecycle())
                    .subscribe(structure -> {
                        if (structure.isSuccess()) {
                            Logger.d("Filament loaded.");
                        } else {
                            Logger.d("Filament load fail.");
                        }
                    }, LogHelper::log);

             */
            // Roll back extrusion method for available use.
            return switchExtruder(toolheadIndex, extruderIndex).filter(switched -> switched).flatMap((Function<Boolean, ObservableSource<ResponseStructure>>)
                    switched -> mFdmController.requestActivatedExtrusion(0, 6, 200, 60, 150));
//                    .as(bindToLifecycle())
//                    .subscribe(structure -> {
//                        if (structure.isSuccess()) {
//                            Logger.d("Filament unloaded.");
//                        } else {
//                            Logger.d("Filament unload fail.");
//                        }
//                    }, LogHelper::log);
        } else {
            toolheadIndex = 0;
            extruderIndex = pageExtruderIndex;
            return switchExtruder(toolheadIndex, extruderIndex).filter(switched -> switched).flatMap((Function<Boolean, ObservableSource<ResponseStructure>>)
                    switched -> mFdmController.requestActivatedExtrusion(0, 6, 200, 100, 150));
//                    .as(bindToLifecycle())
//                    .subscribe(structure -> {
//                        if (structure.isSuccess()) {
//                            Logger.d("Filament unloaded.");
//                        } else {
//                            Logger.d("Filament unload fail.");
//                        }
//                    }, LogHelper::log);
        }

    }

    /**
     * J1: Set temp and request set temp if switch is on.
     * not J1: Set temp and turn on switch android request set temp ~if switch is on~.
     */
    public void setTargetTemp(int pageExtruderIndex, int degree) {
        mViewTargetTemps.put(pageExtruderIndex, (float) degree);

        if (!isJ1()) {
            mHeatOnArray.put(pageExtruderIndex, true);
        }

        if (mHeatOnArray.get(pageExtruderIndex)) {
            requestSetExtruderTemp(pageExtruderIndex, degree);
        } else {
            mTargetTempSubj.onNext(mViewTargetTemps);
        }
    }

    private void requestSetExtruderTemp(int pageExtruderIndex, int degree) {
        int toolheadIndex;
        int extruderIndex;

        if (isJ1()) {
            toolheadIndex = pageExtruderIndex;
            extruderIndex = 0;
        } else {
            toolheadIndex = 0;
            extruderIndex = pageExtruderIndex;
        }
        mFdmController.setExtruderTemperature(toolheadIndex, extruderIndex, degree)
                .as(bindToLifecycle())
                .subscribe(structure -> {
                    if (structure.isSuccess()) {
                        Logger.d("Temperature set.");
                    } else {
                        Logger.d("Temperature set fail.");
                    }
                }, LogHelper::log);
    }

    public boolean isJ1() {
        return mMachineInfo.seriesId == IMachine.MachineSeries.J && mMachineInfo.modelId == IMachine.MachineModel.J1;
    }

    public boolean hasSecondExtruder() {
        return mFdmController.getHeadType() == Module.ModuleType.HEAD_3DP_DOUBLE_EXTRUDER || isJ1();
    }

    @Override
    protected void onCleared() {
        mFdmController.unSubscribeExtruderChange();
        if (isJ1()) {
            mPreferences.getHelper().setJ1LeftTargetTemp(mViewTargetTemps.get(0));
            mPreferences.getHelper().setJ1RightTargetTemp(mViewTargetTemps.get(1));
        }

        super.onCleared();
    }

    public boolean getIsHeatOn(int index) {
        return mHeatOnArray.get(index);
    }

    public void startHeat(int index) {
        if (isJ1()) {
            mMachineController.getMotorStateObservable()
                    .flatMap(enabled -> {
                        if (enabled) {
                            return ensureHeadPosition(index);
                        } else {
                            // motor not enabled, ignore position
                            return Observable.just(true);
                        }
                    })
                    .filter(positionGood -> positionGood)
                    .as(bindToLifecycle())
                    .subscribe(positionGood -> setHeatOn(index, true), LogHelper::log);
        } else {
            setHeatOn(index, true);
        }
    }

    private Observable<Boolean> ensureHeadPosition(int index) {

        if (mJ1PositionGoodSubj != null) {
            mJ1PositionGoodSubj.onComplete();
            mJ1PositionGoodSubj = null;
        }
        mJ1PositionGoodSubj = BehaviorSubject.create();

        mMachineController.getCurrentCoordinateObservable(1)
                .as(bindToLifecycle())
                .subscribe(position -> {
                    int goodDistance = 7;
                    float homeX = index == 0 ? -13 : 337;// magic number from machine
                    float headerX = index == 0 ? position.getX() : position.getX2();

                    if (Math.abs(headerX - homeX) >= goodDistance) {
                        mJ1PositionGoodSubj.onNext(true);
                        mJ1PositionGoodSubj.onComplete();
                        mJ1PositionGoodSubj = null;
                    } else {
                        // dialog
                        // save target x and index
                        mTargetX = index == 0 ? homeX + goodDistance : homeX - goodDistance;
                        mNeedMoveIndex = index;
                        mExtruderPositionErrorSubj.onNext(true);
                    }
                }, LogHelper::log);

        return mJ1PositionGoodSubj.hide();
    }

    public void moveToGoodXPosition() {
        mMachineController.homeIfNotYet(0)
                .doOnSubscribe(disposable -> mIsMovingSubj.onNext(true))
                .flatMap(result -> {
                    FdmToolhead.FdmToolheadStatus status = mFdmController.getToolheadStatusSubjectHolder(mNeedMoveIndex).getValue();
                    boolean active = status.isActive();
                    mOriginJ1Toolhead = active ? mNeedMoveIndex : Math.abs(mNeedMoveIndex - 1);
                    if (active) {
                        return Observable.just(new Object());
                    } else {
                        return mFdmController.switchExtruder(mNeedMoveIndex, 0);
                    }
                })
                .flatMap(success -> {
                    Vector vector = new Vector();
                    if (mNeedMoveIndex == 0) {
                        vector.setX(mTargetX);
                    } else if (mNeedMoveIndex == 1) {
                        vector.setX2(mTargetX);
                    }
                    return mMachineController.gotoAbsolutePosition(vector, 1800, 1);
                })
                .flatMap(response -> {
                    // Switch back if current toolhead not the original one.
                    boolean active = mFdmController.getToolheadStatusSubjectHolder(mNeedMoveIndex).getValue().isActive();
                    int curToolhead = active ? mNeedMoveIndex : Math.abs(mNeedMoveIndex - 1);
                    if (curToolhead != mOriginJ1Toolhead) {
                        return mFdmController.switchExtruder(mOriginJ1Toolhead, 0);
                    } else {
                        return Observable.just(new ResponseStructure<>(0));
                    }
                })
                .doOnNext(response -> mIsMovingSubj.onNext(false))
                .doOnError(e -> mIsMovingSubj.onNext(false))
                .as(bindToLifecycle())
                .subscribe(response -> {
                    mJ1PositionGoodSubj.onNext(response.isSuccess());
                    mJ1PositionGoodSubj.onComplete();
                    mJ1PositionGoodSubj = null;
                }, LogHelper::log);

    }
}
