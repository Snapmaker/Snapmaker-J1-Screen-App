package fabscreen.features.machinetools.calibration.a400platform.laser.w_10.platformHeight;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.Constants;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.MockConst;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.data.MoveController;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

public class TouchPlatformViewModel extends BaseViewModel {
    private MachineInfo machineInfoObservable;
    //    private DeprecatedMachineController mDeprecatedMachineController;
    private Subject<Boolean> mIsMovingSubject = BehaviorSubject.create();
    private Subject<Boolean> mIsMachineMovingSubject = BehaviorSubject.create();
    private final MachineController mMachineController;

    public TouchPlatformViewModel() {
        super();
        machineInfoObservable = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue();
//        mDeprecatedMachineController = ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().getMachineController();
        mMachineController = getServiceContainer().getService(IMachine.class).getMachineController();

    }

    public void moveXYZByStep(MoveController.Direction direction, float stepWidth) {
        mIsMovingSubject.onNext(true);
        MoveController.getInstance()
                .stepToPosition(direction, stepWidth)
                .as(bindToLifecycle())
                .subscribe(response -> mIsMovingSubject.onNext(false));
    }

    public void initToolheadPosition() {
        mIsMachineMovingSubject.onNext(true);
        mMachineController.updateCoordinateSystem(0)
                .flatMap(machineStatus -> {
                    Vector vector = new Vector();
                    vector.setX(machineInfoObservable.size.getX() * 0.5f);
                    vector.setY(machineInfoObservable.size.getY() * 0.5f);
                    vector.setZ(machineInfoObservable.size.getZ() * 0.5f);
                    return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().gotoAbsolutePosition(vector);
                })
                .flatMap(coordinate -> mMachineController.updateCoordinateSystem(1))
                .as(bindToLifecycle())
                .subscribe(response -> mIsMachineMovingSubject.onNext(false));

    }

    public Observable<Boolean> getIsMovingObservable() {
        return mIsMovingSubject.hide();
    }

    public Observable<Boolean> getIsMachineMovingObservable() {
        return mIsMachineMovingSubject.hide();
    }

    public Observable<ResponseStructure> savePlatformZOffset() {
        MachineStatus machineStatus = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue();
        float offsetZ = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getZ();
//        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLaserPlatformZ(machineStatus.currentPosition.getZ() - machineStatus.originOffset.getZ());
        Logger.d("---FDT--- TEMP savePlatformZOffset " + (machineStatus.currentPosition.getZ() - offsetZ));
        Observable<ResponseStructure> responseStructureObservable;
        if (ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getHeadType() == Module.ModuleType.HEAD_LASER_10W) {
            responseStructureObservable = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().setPlatformHeight(machineStatus.currentPosition.getZ() - offsetZ - MockConst.LASER_MATERIAL_MEASURE_CALIBRATION_OBJECT_HEIGHT)
                    .flatMap(responseStructure -> ServiceContainer.getInstance().getService(IMachine.class).getLaserController().setFocalLength(MockConst.LASER_MATERIAL_MEASURE_CALIBRATION_OBJECT_HEIGHT));
        } else {
            responseStructureObservable = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().setPlatformHeight(machineStatus.currentPosition.getZ() - offsetZ);
        }
        return responseStructureObservable;
    }

    public Observable<Boolean> upLiftToolhead() {
        mIsMachineMovingSubject.onNext(true);
        return MoveController.getInstance()
                .stepToPosition(MoveController.Direction.UP, Constants.LASER_10W_CAMERA_FOCAL_LENGTH)
                .flatMap(response -> Observable.just(true))
                .doOnNext(success -> mIsMachineMovingSubject.onNext(false));
    }

    public Observable<Boolean> checkHome() {
        IMachine service = ServiceContainer.getInstance().getService(IMachine.class);
        if (!service.getMachineStatusSubjectHolder().getValue().isHomed) {
            mIsMachineMovingSubject.onNext(true);
            return service.getMachineController().updateCoordinateSystem(0)
                    .flatMap(machineStatus -> service.getMachineController().home(0))
                    .flatMap(integer -> service.getMachineController().updateCoordinateSystem(1))
                    .flatMap(machineStatus -> Observable.just(machineStatus.isHomed))
                    .doOnNext(machineStatus -> mIsMachineMovingSubject.onNext(false))
                    ;
        } else {
            return service.getMachineController().updateCoordinateSystem(1)
                    .flatMap(machineStatus -> Observable.just(machineStatus.isHomed));
        }
    }
}
