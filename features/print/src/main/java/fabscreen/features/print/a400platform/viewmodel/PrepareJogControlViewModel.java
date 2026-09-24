package fabscreen.features.print.a400platform.viewmodel;

import static fabscreen.platform.core.ui.data.MoveController.Direction;
import static fabscreen.platform.core.ui.data.MoveController.getInstance;

import androidx.annotation.IntDef;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class PrepareJogControlViewModel extends BaseViewModel {
    private final MachineInfo mMachineInfo;
    private final float[] mLinearStepWidths = {0.1f, 1f, 10f, 100f};
    private final float[] mRotaryStepWidths = {0.2f, 1f, 10f, 90f};
    private int mStepWidthPos;
    private final BehaviorSubject<Boolean> mMovingSubject = BehaviorSubject.createDefault(false);
    private final BehaviorSubject<WorkOriginControlViewModel.ActiveStatus> mButtonActiveSubject = BehaviorSubject.create();
    private final MachineController mMachineController;
    private int mCoordinateType = 0;

    public void setCoordinateType(int type) {
        mCoordinateType = type;
    }

    @IntDef({WORK, MACHINE})
    @interface CoordinateShowMode {
    }

    public static final int WORK = 0;
    public static final int MACHINE = 1;

    public PrepareJogControlViewModel() {
        IMachine machine = ServiceContainer.getInstance().getService(IMachine.class);
        mMachineInfo = machine.getMachineInfoSubjectHolder().getValue();
        mMachineController = machine.getMachineController();
        int coordinateSystemIndex = mMachineInfo.workType == IMachine.WorkType.FDM ? 0 : 1;
        mMachineController.updateCoordinateSystem(coordinateSystemIndex);

        mStepWidthPos = 1;
    }

    public Observable<Vector> getCoordinateObservable() {
        return mMachineController.getCachedCoordinateObservable()
                .flatMap(machineStatus -> {
                    Vector vector;
                    if (mCoordinateType == WORK) {
                        vector = machineStatus.currentPosition;
                    } else {
                        vector = applyOffsetToVector(machineStatus.currentPosition, machineStatus.originOffset);
                    }
                    return Observable.just(vector == null ? new Vector() : vector);
                });
    }

    public boolean isRotaryAvailable() {
        return mMachineInfo.isRotaryAvailable;
    }

    public void changeStepWidth(int position) {
        mStepWidthPos = position;
    }

    public Observable<ResponseStructure> moveToPosition(Direction direction) {
        if (direction == null) {
            ResponseStructure<IStructure> iStructureResponseStructure = new ResponseStructure<>();
            iStructureResponseStructure.resultProp.setValue(1);
            return Observable.just(iStructureResponseStructure);
        }

        float stepWidth = Direction.isRotary(direction) ? mRotaryStepWidths[mStepWidthPos] : mLinearStepWidths[mStepWidthPos];
        mMovingSubject.onNext(true);
        return getInstance()
                .stepToPosition(direction, stepWidth)
                .doOnNext(response -> mMovingSubject.onNext(false))
                .doOnError(e -> mMovingSubject.onNext(false));
    }

    public Observable<WorkOriginControlViewModel.ActiveStatus> getButtonActiveObservable() {
        return mButtonActiveSubject.distinctUntilChanged();
    }

    public void setOrigin(Vector vector, int viewId) {
        mMovingSubject.onNext(true);
        mButtonActiveSubject.onNext(new WorkOriginControlViewModel.ActiveStatus(viewId, true));

        Logger.i("Requesting set origin %s ...", vector);
        mMachineController.setWorkOrigin(vector)
                .as(bindToLifecycle())
                .subscribe(res -> {
                    mMovingSubject.onNext(false);
                    mButtonActiveSubject.onNext(new WorkOriginControlViewModel.ActiveStatus(viewId, false));
                });
    }

    public static class ActiveStatus {
        public int viewId;
        public boolean isActive;

        public ActiveStatus(int viewId, boolean isActive) {
            this.viewId = viewId;
            this.isActive = isActive;
        }
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
}
