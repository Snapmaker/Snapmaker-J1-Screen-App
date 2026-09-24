package fabscreen.features.machinetools.calibration.a400platform.cnc.manualTool;

import com.orhanobut.logger.Logger;

import fabscreen.features.machinetools.control.a350.modules.bed.WorkOriginControlViewModel;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.data.MoveController;
import fabscreen.platform.lib.SubjectHolder;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

public class A400CncManualToolViewModel extends BaseViewModel {
    private MachineController machineController;
    private SubjectHolder<MachineStatus> machineStatusSubjectHolder;
    private Subject<Boolean> mIsMovingSubject = BehaviorSubject.create();
    private Subject<Boolean> mIsMachineMovingSubject = BehaviorSubject.create();
    private float mZ0 = 0;
    private int mCoordinateType = 0;

    public static final int WORK = 0;
    public static final int MACHINE = 1;
    private final IMachine mMachine;
    private  MachineInfo mMachineInfo;
    private  MachineController mMachineController;
    private final BehaviorSubject<Boolean> mMovingSubject = BehaviorSubject.createDefault(false);
    private final BehaviorSubject<WorkOriginControlViewModel.ActiveStatus> mButtonActiveSubject = BehaviorSubject.create();

    public void setCoordinateType(int type) {
        mCoordinateType = type;
    }

    public A400CncManualToolViewModel() {
        super();
        mMachine = ServiceContainer.getInstance().getService(IMachine.class);
        machineController = mMachine.getMachineController();
        machineStatusSubjectHolder = mMachine.getMachineStatusSubjectHolder();
        mMachineInfo = mMachine.getMachineInfoSubjectHolder().getValue();
        int coordinateSystemIndex = mMachineInfo.workType == IMachine.WorkType.FDM ? 0 : 1;
        mMachineController = mMachine.getMachineController();
        mMachineController.updateCoordinateSystem(coordinateSystemIndex);

    }

    public void move(MoveController.Direction direction, float stepWidth) {
        mIsMovingSubject.onNext(true);
        MoveController.getInstance()
                .stepToPosition(direction, stepWidth)
                .as(bindToLifecycle())
                .subscribe(response ->
                                mIsMovingSubject.onNext(false)
                        // ERROR?
                );
    }

    public Observable<Boolean> getIsMovingObservable() {
        return mIsMovingSubject.hide();
    }

    public Observable<ResponseStructure> setWorkOrigin() {
        Vector vector = new Vector();
        vector.setX(0);
        vector.setY(0);
        vector.setZ(0);
        return machineController.setWorkOrigin(vector);
    }

    public Observable<Boolean> getIsMachineMovingObservable() {
        return mIsMachineMovingSubject.hide();
    }

    public Observable<ResponseStructure> setZ() {
        mIsMachineMovingSubject.onNext(true);
        mZ0 = machineStatusSubjectHolder.getValue().currentPosition.getZ();
//        Logger.d("z0 saved, is %f", mZ0);
        float z = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getZ();
        Vector vector = new Vector();
        vector.setZ(z);
        return machineController.gotoAbsolutePosition(vector, 1800).doOnNext(responseStructure -> mIsMachineMovingSubject.onNext(false));
    }

    public Observable<ResponseStructure> applyZOffset() {
        mIsMachineMovingSubject.onNext(true);
        float z1 = machineStatusSubjectHolder.getValue().currentPosition.getZ();
        float offset = z1 - mZ0;
        float newOriginRelativeToCurrentPosition = -z1 + offset;
//        Logger.d("z0 is %1$f, z1 is %2$f, offset is %3$f, relative is %4$f", mZ0, z1, offset, newOriginRelativeToCurrentPosition);
        Vector vector = new Vector();
        vector.setZ(-newOriginRelativeToCurrentPosition);
        return machineController.setWorkOrigin(vector).doOnNext(responseStructure -> mIsMachineMovingSubject.onNext(false));
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

    public boolean isRotaryAvailable() {
        return mMachineInfo.isRotaryAvailable;
    }

    public Observable<WorkOriginControlViewModel.ActiveStatus> getButtonActiveObservable() {
        return mButtonActiveSubject.distinctUntilChanged();
    }
}
