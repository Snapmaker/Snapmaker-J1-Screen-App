package fabscreen.features.machinetools.calibration.s20._30._cnc.changeassistant;

import com.orhanobut.logger.Logger;

import java.util.Locale;

import fabscreen.platform.base.Constants;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.DeprecatedMachineController;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.data.MoveController;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

public class CncChangeAssistantModel extends BaseViewModel {


    private Subject<Boolean> mIsMovingSubject = BehaviorSubject.create();

    private float mChangeAssistantX;
    private float mChangeAssistantY;
    private float mChangeAssistantZ;
    private MachineInfo machineInfoObservable;
    private DeprecatedMachineController mDeprecatedMachineController;


    public CncChangeAssistantModel() {
        super();
        machineInfoObservable = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue();
        mDeprecatedMachineController = ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().getMachineController();
    }

    public void moveXYZByStep(MoveController.Direction direction, float stepWidth) {
        mIsMovingSubject.onNext(true);
        MoveController.getInstance()
                .moveByStep(direction, stepWidth)
                .as(bindToLifecycle())
                .subscribe(response -> mIsMovingSubject.onNext(false));
    }

    public void initToolheadPosition() {
        mIsMovingSubject.onNext(true);
        // TODO: getSizeX
        float initX = (float) (machineInfoObservable.size.getX() * 0.5f);
        float initY = (float) (machineInfoObservable.size.getY() * 0.5f);
        float initZ = (float) (machineInfoObservable.size.getX() * 0.5f);
        mDeprecatedMachineController.updateCoordinateSystem(0)
                .flatMap(success -> {
                    Logger.d("gotoAbsolutePosition: x:%s,y:%s,z:%s", initX, initY, initZ);
                    Vector vector = new Vector();
                    vector.setX(initX);
                    vector.setY(initY);
                    vector.setZ(initZ);
                    return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().gotoAbsolutePosition(vector);
                })
                .flatMap(success -> mDeprecatedMachineController.updateCoordinateSystem(1))
                .as(bindToLifecycle())
                .subscribe(coordinateSystem -> {
                    mIsMovingSubject.onNext(false);
                });
    }

    public Observable<Boolean> getIsMovingObservable() {
        return mIsMovingSubject.hide();
    }

    public Observable<Object> setChangeAssistant() {
        mIsMovingSubject.onNext(true);
        // TODO:Save current position as first bit position.
        final MachineStatus status = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue();
        this.mChangeAssistantX = (float) status.currentPosition.getX();
        this.mChangeAssistantY = (float) status.currentPosition.getY();
        this.mChangeAssistantZ = (float) status.currentPosition.getZ();
        return mDeprecatedMachineController.updateCoordinateSystem(0)
                .flatMap(system -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().home(3))
                .flatMap(response -> mDeprecatedMachineController.updateCoordinateSystem(1));
    }

    public void savePlatformZOffset() {
        MachineStatus status = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue();
        float offsetZ = (float) ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getZ();
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLaserPlatformZ((float) (status.currentPosition.getZ() - offsetZ));
        ServiceContainer.getInstance().getService(IMachine.class).getLaserController().setFocalLength((float) (status.currentPosition.getZ() - offsetZ + 10)).as(bindToLifecycle()).subscribe();
    }

    public Observable<Boolean> upLiftToolhead() {
        mIsMovingSubject.onNext(true);
        return MoveController.getInstance()
                .moveByStep(MoveController.Direction.UP, Constants.LASER_10W_CAMERA_FOCAL_LENGTH)
                .flatMap(response -> Observable.just(true))
                .doOnNext(success -> mIsMovingSubject.onNext(false));
    }

    public Observable<Boolean> moveXY() {
        mIsMovingSubject.onNext(true);
        return ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer()
                .sendGcode(String.format(Locale.getDefault(), "G0 X%.2f Y%.2f F3000", mChangeAssistantX, mChangeAssistantX))
                .flatMap(success -> {
                    mIsMovingSubject.onNext(false);
                    return Observable.just(true);
                });
    }


    public Observable<MachineStatus> setZPosition() {
        mIsMovingSubject.onNext(true);
        final float currentZ = (float) ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().currentPosition.getZ();
        final float deltaZ = mChangeAssistantZ - currentZ;

        Logger.d("deltaZ is %.2f", deltaZ);

        // Apply Z Offset into work origin.
        Vector vector = new Vector();
        vector.setZ(currentZ + deltaZ);
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector)
                .flatMap(ret -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0))
                .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().home(3))
                .flatMap(response -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1));
    }
}
