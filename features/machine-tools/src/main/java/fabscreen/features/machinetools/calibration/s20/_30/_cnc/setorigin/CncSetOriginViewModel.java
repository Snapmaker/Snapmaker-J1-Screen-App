package fabscreen.features.machinetools.calibration.s20._30._cnc.setorigin;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.data.MoveController;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

public class CncSetOriginViewModel extends BaseViewModel {
    private Subject<Boolean> mIsMovingSubject = BehaviorSubject.create();

    public CncSetOriginViewModel() {
        super();
    }

    public void moveXYZByStep(MoveController.Direction direction, float stepWidth) {
        mIsMovingSubject.onNext(true);
        MoveController.getInstance()
                .moveByStep(direction, stepWidth)
                .as(bindToLifecycle())
                .subscribe(response -> mIsMovingSubject.onNext(false));
    }

    public Observable<Boolean> getIsMovingObservable() {
        return mIsMovingSubject.hide();
    }

    public Observable<Object> saveOrigin() {
        mIsMovingSubject.onNext(true);
        Vector vector = new Vector();
        vector.setX(0);
        vector.setY(0);
        vector.setZ(0);
        vector.setB(0);
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector)
                .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem()
                );
    }


}
