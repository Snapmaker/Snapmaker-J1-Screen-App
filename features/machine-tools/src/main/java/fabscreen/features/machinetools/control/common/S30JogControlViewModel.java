package fabscreen.features.machinetools.control.common;

import static fabscreen.platform.core.ui.data.MoveController.Direction;
import static fabscreen.platform.core.ui.data.MoveController.Direction.BACKWARD;
import static fabscreen.platform.core.ui.data.MoveController.Direction.FORWARD;
import static fabscreen.platform.core.ui.data.MoveController.Direction.LEFT;
import static fabscreen.platform.core.ui.data.MoveController.Direction.NONE;
import static fabscreen.platform.core.ui.data.MoveController.Direction.RIGHT;
import static fabscreen.platform.core.ui.data.MoveController.Direction.X2_LEFT;
import static fabscreen.platform.core.ui.data.MoveController.Direction.X2_RIGHT;
import static fabscreen.platform.core.ui.data.MoveController.getInstance;

import androidx.annotation.IntDef;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.controller.FDMController;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.view.SteeringView;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class S30JogControlViewModel extends BaseViewModel {
    private final MachineInfo mMachineInfo;
    private final float[] mLinearStepWidths = {0.1f, 1f, 10f, 100f};
    private final float[] mRotaryStepWidths = {0.2f, 1f, 10f, 90f};
    private int mStepWidthPos;
    private final MachineController mMachineController;
    private final IMachine mMachine;
    private int mCoordinateType = 0;
    private boolean mMotorPotentialState = false;
    private int mActiveToolHeadIndex = 0;

    private final BehaviorSubject<Boolean> mNeedHomeSubj = BehaviorSubject.createDefault(false);
    private final PublishSubject<Vector> mCoordinateSubj = PublishSubject.create();
    private final BehaviorSubject<Direction> mMovingStatusSubject = BehaviorSubject.createDefault(NONE);
    private final BehaviorSubject<Boolean> mHomingSubj = BehaviorSubject.createDefault(false);

    public void setCoordinateType(int type) {
        mCoordinateType = type;
    }

    public void pullCoordinateInfo() {
        mMachineController.pullCoordinate()
                .as(bindToLifecycle())
                .subscribe(coordinateInfo -> {
                }, LogHelper::log);
    }

    public IMachine.WorkType getWorkType() {
        return mMachineInfo.workType;
    }

    public boolean isCanUp() {
        float stepWidth = mLinearStepWidths[mStepWidthPos];
        return mMachineController.getCachedCoordinate().getZ() - stepWidth > 5;
    }

    @IntDef({WORK, MACHINE})
    @interface CoordinateShowMode {
    }

    public static final int WORK = 0;
    public static final int MACHINE = 1;

    public S30JogControlViewModel() {
        mMachine = ServiceContainer.getInstance().getService(IMachine.class);
        mMachineInfo = mMachine.getMachineInfoSubjectHolder().getValue();
        mMachineController = mMachine.getMachineController();
        int coordinateSystemIndex = mMachineInfo.workType == IMachine.WorkType.FDM ? 0 : 1;
        mMachineController.updateCoordinateSystem(coordinateSystemIndex);
        watchCoordinateChange();
        mStepWidthPos = 1;
    }

    private void watchCoordinateChange() {
        mMachineController.getCachedCoordinateObservable()
                .as(bindToLifecycle())
                .subscribe(machineStatus -> {
                    mNeedHomeSubj.onNext(!machineStatus.isHomed);
                    Vector vector;
                    if (mCoordinateType == WORK) {
                        vector = machineStatus.currentPosition;
                    } else {
                        vector = applyOffsetToVector(machineStatus.currentPosition, machineStatus.originOffset);
                    }
                    mCoordinateSubj.onNext(vector == null ? new Vector() : vector);
                }, LogHelper::log);
    }

    public Observable<Vector> getCoordinateObservable() {
        return mCoordinateSubj.hide();
    }

    public boolean isRotaryAvailable() {
        return mMachineInfo.isRotaryAvailable;
    }

    public void changeStepWidth(int position) {
        mStepWidthPos = position;
    }

    public Observable<Boolean> getNeedHomeObservable() {
        return mNeedHomeSubj.distinctUntilChanged();
    }

    public Observable<Direction> getMoveStateObservable() {
        return mMovingStatusSubject.hide();
    }

    public Observable<Boolean> getHomingObservable() {
        return mHomingSubj.hide();
    }

    public Observable<Integer> getActiveToolHeadIndexObservable() {
        FDMController fdmController = mMachine.getFDMController();
        return Observable
                .combineLatest(fdmController.getToolheadStatusSubjectHolder(0).getObservable(),
                        fdmController.getToolheadStatusSubjectHolder(1).getObservable(),
                        mNeedHomeSubj.hide(),
                        (o1, o2, o3) -> {
                            if (mMotorPotentialState && !o3) {
                                mActiveToolHeadIndex = o1.isActive() ? 0 : 1;
                            } else {
                                mActiveToolHeadIndex = -1;
                            }
                            return mActiveToolHeadIndex;
                        }).distinctUntilChanged();
    }

    public Observable<ResponseStructure> moveToPosition(Direction direction) {
        if (direction == null) {
            ResponseStructure<IStructure> iStructureResponseStructure = new ResponseStructure<>();
            iStructureResponseStructure.resultProp.setValue(1);
            return Observable.just(iStructureResponseStructure);
        }
        float stepWidth = Direction.isRotary(direction) ? mRotaryStepWidths[mStepWidthPos] : mLinearStepWidths[mStepWidthPos];
        mMovingStatusSubject.onNext(direction);
        return getInstance()
                .stepToPositionAndLimit(direction, stepWidth, 5)
                .doOnNext(response -> mMovingStatusSubject.onNext(NONE))
                .doOnError(e -> mMovingStatusSubject.onNext(NONE));
    }

    public Observable<ResponseStructure> moveToPosition(Direction direction, int feedrate) {
        if (direction == null) {
            ResponseStructure<IStructure> iStructureResponseStructure = new ResponseStructure<>();
            iStructureResponseStructure.resultProp.setValue(1);
            return Observable.just(iStructureResponseStructure);
        }

        float stepWidth = Direction.isRotary(direction) ? mRotaryStepWidths[mStepWidthPos] : mLinearStepWidths[mStepWidthPos];
        mMovingStatusSubject.onNext(direction);
        return getInstance()
                .stepToPosition(direction, stepWidth, feedrate)
                .doOnNext(response -> mMovingStatusSubject.onNext(NONE))
                .doOnError(e -> mMovingStatusSubject.onNext(NONE));
    }

    private Observable<Boolean> fdmGoHome(boolean isForce) {
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().home(0, isForce)
                .flatMap(integer -> Observable.just(integer.equals(0)))
                .doOnNext(aBoolean -> {
                    mHomingSubj.onNext(false);
                })
                .doOnError(e -> {
                    mHomingSubj.onNext(false);
                });
    }

    public void setOrigin(Vector vector) {
        Logger.i("Requesting set origin %s ...", vector);
        mMachineController.setWorkOrigin(vector)
                .as(bindToLifecycle())
                .subscribe(res -> {
                }, LogHelper::log);
    }

    /**
     * Go home with flag isForce
     *
     * @param isForce Will always go home when true, only go home if never go when false.
     */
    public Observable<Boolean> goHome(boolean isForce) {
        mHomingSubj.onNext(true);

        Logger.i("Homing...");

        if (mMachineInfo.workType == IMachine.WorkType.FDM) {
            // 3dp go home don't need coordinate switch.
            return fdmGoHome(isForce);
        }

        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .flatMap(response -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().home(0, isForce))
                .flatMap(response -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1).flatMap(machineStatus -> Observable.just(machineStatus.isHomed)))
                .doOnNext(aBoolean -> mHomingSubj.onNext(false))
                .doOnError(e -> mHomingSubj.onNext(false));
    }

    public void subscribeCoordinate() {
        mMachineController.subscribeCoordinate();
    }

    public void unSubscribeCoordinate() {
        mMachineController.unSubscribeCoordinate();
    }

    private Vector applyOffsetToVector(Vector currentPosition, Vector originOffset) {
        Vector vector = new Vector();
        if (currentPosition == null || originOffset == null) return vector;
        vector.setX(currentPosition.getX() - originOffset.getX());
        vector.setY(currentPosition.getY() - originOffset.getY());
        vector.setZ(currentPosition.getZ() - originOffset.getZ());
        vector.setB(currentPosition.getB() - originOffset.getB());
        vector.setX2(currentPosition.getX2() - originOffset.getX2());
        return vector;
    }

    public Observable<Boolean> switchExtruder(int toolheadIndex, int extruderIndex) {
//        FdmToolhead.FdmToolheadStatus toolheadStatus = mToolheadStatusList.get(toolheadIndex);
        // Toolhead and extruder already activated, just return true.
        // TODO: 2022/3/23 uncomment
//        if (toolheadStatus.getHeadActive() && toolheadStatus.getExtruderList().get(extruderIndex).getState() == 1) {
//            return Observable.just(true);
//        }
        // Do switch.
        FDMController fdmController = ServiceContainer.getInstance().getService(IMachine.class).getFDMController();
        return fdmController.switchExtruder(toolheadIndex, extruderIndex).flatMap(responseStructure -> Observable.just(responseStructure.resultProp.getValue() == 0));
    }

    private boolean isJ1() {
        return mMachineInfo.seriesId == IMachine.MachineSeries.J && mMachineInfo.modelId == IMachine.MachineModel.J1;
    }

    public Observable<Boolean> getMotorPotentialStateObservable() {
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getMotorStateObservable()
                .doOnNext(aBoolean -> mMotorPotentialState = aBoolean);
    }

    /**
     * Compat method for xyz move.
     * j1 has two x, move which x depends on which toolhead is active.
     *
     * @param direction Directions defined in SteeringView.
     */
    @Deprecated
    public Observable<ResponseStructure> moveXYZByStep(int direction) {
        Direction xyzDirection = null;
        switch (direction) {
            case SteeringView.DIRECTION_UP:
                xyzDirection = FORWARD;
                break;

            case SteeringView.DIRECTION_DOWN:
                xyzDirection = BACKWARD;
                break;

            case SteeringView.DIRECTION_LEFT:
                if (isJ1()) {
                    if (mMachine.getFDMController().getToolheadStatusSubjectHolder(0).getValue().isActive()) {
                        // j1 head 0 active
                        xyzDirection = LEFT;
                    } else {
                        // j1 head 1 active
                        xyzDirection = X2_LEFT;
                    }

                } else {
                    xyzDirection = LEFT;
                }
                break;

            case SteeringView.DIRECTION_RIGHT:
                if (isJ1()) {
                    if (mMachine.getFDMController().getToolheadStatusSubjectHolder(0).getValue().isActive()) {
                        // j1 head 0 active
                        xyzDirection = RIGHT;
                    } else {
                        // j1 head 1 active
                        xyzDirection = X2_RIGHT;
                    }

                } else {
                    xyzDirection = RIGHT;
                }
                break;
        }
        return moveToPosition(xyzDirection);
    }
}
