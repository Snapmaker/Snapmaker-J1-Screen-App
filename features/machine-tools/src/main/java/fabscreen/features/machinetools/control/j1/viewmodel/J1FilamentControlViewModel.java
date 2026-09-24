package fabscreen.features.machinetools.control.j1.viewmodel;

import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.controller.FDMController;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.entity.parts.Extruder;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.data.HeatingStatedata;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

public class J1FilamentControlViewModel extends BaseViewModel {
    private final FDMController mFdmController;
    private final MachineController mMachineController;
    int mPreheatingValue = 0;
    BehaviorSubject<HeatingStatedata> mLeftExtruderStateSubject = BehaviorSubject.createDefault(new HeatingStatedata());
    BehaviorSubject<HeatingStatedata> mRightExtruderStateSubject = BehaviorSubject.createDefault(new HeatingStatedata());
    Disposable mLeftLongTimeMovementSubscribe;
    Disposable mRightLongTimeMovementSubscribe;

    private BehaviorSubject<Boolean> mJ1PositionGoodSubj;
    private final int GOOD_DISTANCE = 7;
//    private float mTargetX;
//    private int mNeedMoveIndex;
//    private int mOriginJ1Toolhead;

    public J1FilamentControlViewModel() {
        mFdmController = ServiceContainer.getInstance().getService(IMachine.class).getFDMController();
        mMachineController = ServiceContainer.getInstance().getService(IMachine.class).getMachineController();
        mFdmController.getToolheadStatusSubjectHolder(0)
                .getObservable()
                .as(bindToLifecycle())
                .subscribe(fdmToolheadStatus -> {
                    Extruder extruder = fdmToolheadStatus.getExtruderList().get(0);
                    float temperature = extruder.getTemperature();
                    float targetTemperature = extruder.getTargetTemperature();
                    HeatingStatedata value = mLeftExtruderStateSubject.getValue();
                    value.targetTemperature = targetTemperature;
                    value.temperature = temperature;
                    value.heatingStats = targetTemperature != 0;
                    mLeftExtruderStateSubject.onNext(value);
                });

        mFdmController.getToolheadStatusSubjectHolder(1)
                .getObservable()
                .as(bindToLifecycle())
                .subscribe(fdmToolheadStatus -> {
                    Extruder extruder = fdmToolheadStatus.getExtruderList().get(0);
                    float temperature = extruder.getTemperature();
                    float targetTemperature = extruder.getTargetTemperature();
                    HeatingStatedata value = mRightExtruderStateSubject.getValue();
                    value.targetTemperature = targetTemperature;
                    value.temperature = temperature;
                    value.heatingStats = targetTemperature != 0;
                    mRightExtruderStateSubject.onNext(value);
                });
        mLeftExtruderStateSubject
                .flatMap(heatingStatedata -> Observable.just(heatingStatedata.getMovementStats()))
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(integer -> {
                    if (mLeftLongTimeMovementSubscribe != null && !mLeftLongTimeMovementSubscribe.isDisposed()) {
                        mLeftLongTimeMovementSubscribe.dispose();
                    }
                    switch (integer) {
                        case 0:
                        case 1:
                            mLeftLongTimeMovementSubscribe = Observable.timer(2, TimeUnit.MINUTES)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .as(bindToLifecycle())
                                    .subscribe(times -> {
                                        movementExtruder(0, -1);
                                    });
                            break;
                    }
                });

        mRightExtruderStateSubject
                .flatMap(heatingStatedata -> Observable.just(heatingStatedata.getMovementStats()))
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(integer -> {
                    if (mRightLongTimeMovementSubscribe != null && !mRightLongTimeMovementSubscribe.isDisposed()) {
                        mRightLongTimeMovementSubscribe.dispose();
                    }
                    switch (integer) {
                        case 0:
                        case 1:
                            mRightLongTimeMovementSubscribe = Observable.timer(2, TimeUnit.MINUTES)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .as(bindToLifecycle())
                                    .subscribe(times -> {
                                        movementExtruder(1, -1);
                                    });
                            break;
                    }
                });
    }


    public Observable<HeatingStatedata> getLeftExtruderStateObservable() {
        return mLeftExtruderStateSubject.hide();
    }

    public Observable<HeatingStatedata> getRightExtruderStateObservable() {
        return mRightExtruderStateSubject.hide();
    }

    public void subscribeDataChange() {
        mFdmController.subscribeExtruderChange();
        mMachineController.subscribeRelativeHomeLocation().observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe(responseStructure -> {
        }, LogHelper::log);
    }

    public void unSubscribeDataChange() {
        mFdmController.unSubscribeExtruderChange();
        mMachineController.unSubscribeRelativeHomeLocation().observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe(responseStructure -> {
        }, LogHelper::log);
    }

    public void movementExtruder(int index, int moveType) {
        int movementType;
        float length;
        switch (moveType) {
            case 0:
                movementType = 0;
                length = -1;
                break;
            case 1:
                movementType = 1;
                length = -1;
                break;
            case -1:
            default:
                movementType = 0;
                length = 0;
                break;
        }
        mFdmController.requestExtruderMovement(index, movementType, length, movementType==0?200:450)
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        HeatingStatedata value;
                        if (index == 0) {
                            value = mLeftExtruderStateSubject.getValue();
                            value.movementStats = moveType;
                            mLeftExtruderStateSubject.onNext(value);
                        } else {
                            value = mRightExtruderStateSubject.getValue();
                            value.movementStats = moveType;
                            mRightExtruderStateSubject.onNext(value);
                        }
                    }
                }, LogHelper::log);
    }

    public void changeHeating(int index, int targetTemperature) {
        HeatingStatedata value;
        if (index == 0) {
            value = mLeftExtruderStateSubject.getValue();
            if (targetTemperature == 0) {
                value.targetTemperature = targetTemperature;
                movementExtruder(0, -1);
            } else {
                if (value.heatingStats) {
                    value.targetTemperature = targetTemperature;
                } else {
                    value.preStopTemperature = targetTemperature;
                }
            }
            mLeftExtruderStateSubject.onNext(value);
        } else if (index == 1) {
            value = mRightExtruderStateSubject.getValue();
            if (targetTemperature == 0) {
                value.targetTemperature = targetTemperature;
                movementExtruder(1, -1);
            } else {
                if (value.heatingStats) {
                    value.targetTemperature = targetTemperature;
                } else {
                    value.preStopTemperature = targetTemperature;
                }
            }
            mRightExtruderStateSubject.onNext(value);
        }
        mFdmController.setExtruderTemperature(index, 0, targetTemperature).as(bindToLifecycle()).subscribe(responseStructure -> {
        }, LogHelper::log);
    }

    public void setTargetChange(int index, boolean change) {
        HeatingStatedata value;
        if (index == 0) {
            value = mLeftExtruderStateSubject.getValue();
            if (value.targetChange == change) {
                return;
            }
            value.targetChange = change;
            mLeftExtruderStateSubject.onNext(value);
            if (!change && value.isHeatingStats()) {
                changeHeating(0, value.getStopTemperature());
            }
        } else if (index == 1) {
            value = mRightExtruderStateSubject.getValue();
            if (value.targetChange == change) {
                return;
            }
            value.targetChange = change;
            mRightExtruderStateSubject.onNext(value);
            if (!change && value.isHeatingStats()) {
                {
                    changeHeating(1, value.getStopTemperature());
                }
            }
        }
    }

    public int getTemperature(int index, int targetTemperature) {
        HeatingStatedata value;
        boolean heatingStats;
        mPreheatingValue = 0;
        if (index == 0) {
            value = mLeftExtruderStateSubject.getValue();
            heatingStats = value.heatingStats;
            if (heatingStats) {
                value.preStopTemperature = targetTemperature;
                mLeftExtruderStateSubject.onNext(value);
            } else {
                mPreheatingValue = value.preStopTemperature;
            }
        } else if (index == 1) {
            value = mRightExtruderStateSubject.getValue();
            heatingStats = value.heatingStats;
            if (heatingStats) {
                value.preStopTemperature = targetTemperature;
                mRightExtruderStateSubject.onNext(value);
            } else {
                mPreheatingValue = value.preStopTemperature;
            }
        }
        return mPreheatingValue;
    }

    public Observable<Boolean> checkMove(int index) {
        return mMachineController.getMotorStateObservable()
                .flatMap(enabled -> {
                    if (enabled) {
                        Vector vector = mMachineController.getRelativeHomeLocationSubject().getValue();
                        if (index == 0) {
                            return Observable.just(vector.getX() < GOOD_DISTANCE);
                        } else {
                            return Observable.just(vector.getX2() > -GOOD_DISTANCE);
                        }
                    } else {
                        // motor not enabled, ignore position
                        return Observable.just(false);
                    }
                });
    }

    private Observable<Boolean> ensureHeadPosition(int index) {
        if (mJ1PositionGoodSubj != null) {
            mJ1PositionGoodSubj.onComplete();
            mJ1PositionGoodSubj = null;
        }
        mJ1PositionGoodSubj = BehaviorSubject.create();
        return mMachineController.getCurrentCoordinateObservable(1)
                .flatMap(position -> {
                    float homeX = index == 0 ? -13 : 337;// magic number from machine
                    float headerX = index == 0 ? position.getX() : position.getX2();
//                    mTargetX = index == 0 ? homeX + GOOD_DISTANCE : homeX - GOOD_DISTANCE;
//                    mNeedMoveIndex = index;
                    return Observable.just(!(Math.abs(headerX - homeX) >= GOOD_DISTANCE));
                });
    }


    public Observable<Boolean> moveToGoodXPosition() {
        return mMachineController.homeIfNotYet(0)
                .flatMap(integer -> {
                    Vector vector = new Vector();
                    vector.setX(GOOD_DISTANCE);
                    return mMachineController.MoveRelativeHome(vector, 0);
                })
                .flatMap(responseStructure -> Observable.just(responseStructure.isSuccess()));
//                .flatMap(result -> {
//                    FdmToolhead.FdmToolheadStatus status = mFdmController.getToolheadStatusSubjectHolder(mNeedMoveIndex).getValue();
//                    boolean active = status.isActive();
//                    mOriginJ1Toolhead = active ? mNeedMoveIndex : Math.abs(mNeedMoveIndex - 1);
//                    if (active) {
//                        return Observable.just(new Object());
//                    } else {
//                        return mFdmController.switchExtruder(mNeedMoveIndex, 0);
//                    }
//                })
//                .flatMap(success -> {
//                    Vector vector = new Vector();
//                    if (mNeedMoveIndex == 0) {
//                        vector.setX(mTargetX);
//                    } else if (mNeedMoveIndex == 1) {
//                        vector.setX2(mTargetX);
//                    }
//                    return mMachineController.gotoAbsolutePosition(vector, 1800, 1);
//                })
//                .flatMap(response -> {
//                    // Switch back if current toolhead not the original one.
//                    boolean active = mFdmController.getToolheadStatusSubjectHolder(mNeedMoveIndex).getValue().isActive();
//                    int curToolhead = active ? mNeedMoveIndex : Math.abs(mNeedMoveIndex - 1);
//                    if (curToolhead != mOriginJ1Toolhead) {
//                        return mFdmController.switchExtruder(mOriginJ1Toolhead, 0).flatMap(responseStructure -> Observable.just(responseStructure.isSuccess()));
//                    } else {
//                        return Observable.just(true);
//                    }
//                });
    }

    public void changeStopTemperature(int index, int v) {
        if (index == 0) {
            HeatingStatedata value = mLeftExtruderStateSubject.getValue();
            if (value.preStopTemperature == v) {
                return;
            }
            value.preStopTemperature = v;
            mLeftExtruderStateSubject.onNext(value);
        } else {
            HeatingStatedata value = mRightExtruderStateSubject.getValue();
            if (value.preStopTemperature == v) {
                return;
            }
            value.preStopTemperature = v;
            mRightExtruderStateSubject.onNext(value);
        }
    }


    public HeatingStatedata getExtruderStateValue(int index) {
        return index == 0 ? mLeftExtruderStateSubject.getValue() : mRightExtruderStateSubject.getValue();
    }

    public HeatingStatedata getLeftExtruderStateValue() {
        return mLeftExtruderStateSubject.getValue();
    }

    public HeatingStatedata getRightExtruderStateValue() {
        return mRightExtruderStateSubject.getValue();
    }

}
