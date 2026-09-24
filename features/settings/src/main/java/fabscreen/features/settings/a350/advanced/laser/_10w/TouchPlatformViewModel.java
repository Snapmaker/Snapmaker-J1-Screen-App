package fabscreen.features.settings.a350.advanced.laser._10w;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.Constants;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.data.MoveController;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

public class TouchPlatformViewModel extends BaseViewModel {

    private Subject<Boolean> mIsMovingSubject = BehaviorSubject.create();

    public TouchPlatformViewModel() {
        super();
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
        float initX = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getX() * 0.5f;
        float initY = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getY() * 0.5f;
        float initZ = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getY() * 0.5f;
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .flatMap(success -> {
                    Logger.d("gotoAbsolutePosition: x:%s,y:%s,z:%s", initX, initY, initZ);
                    Vector vector = new Vector();
                    vector.setX(initX);
                    vector.setY(initY);
                    vector.setZ(initZ);
                    return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().gotoAbsolutePosition(vector);
                })
                .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1))
                .as(bindToLifecycle())
                .subscribe(coordinateSystem -> {
                    mIsMovingSubject.onNext(false);
                });
    }

    public Observable<Boolean> getIsMovingObservable() {
        return mIsMovingSubject.hide();
    }

    public void savePlatformZOffset() {
        MachineStatus status = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue();
        float offsetZ = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getZ();
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLaserPlatformZ(status.currentPosition.getZ() - offsetZ);
        ServiceContainer.getInstance().getService(IMachine.class).getLaserController().setFocalLength(status.currentPosition.getZ() - offsetZ + 10).as(bindToLifecycle()).subscribe();
    }

    public Observable<Boolean> upLiftToolhead() {
        mIsMovingSubject.onNext(true);
        return MoveController.getInstance()
                .moveByStep(MoveController.Direction.UP, Constants.LASER_10W_CAMERA_FOCAL_LENGTH)
                .flatMap(response -> Observable.just(true))
                .doOnNext(success -> mIsMovingSubject.onNext(false));
    }
}
