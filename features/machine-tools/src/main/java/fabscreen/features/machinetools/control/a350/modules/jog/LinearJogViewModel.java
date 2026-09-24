package fabscreen.features.machinetools.control.a350.modules.jog;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.data.MoveController;
import fabscreen.platform.core.ui.view.SteeringView;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class LinearJogViewModel extends BaseViewModel {
    private float[] mStepWidths = new float[3];
    private float mStepWidth;

    private BehaviorSubject<Boolean> mMovingSubject = BehaviorSubject.createDefault(false);

    public LinearJogViewModel() {
        mStepWidths[0] = 0.1f;
        mStepWidths[1] = 1f;
        mStepWidths[2] = 10f;
        if (isRotaryAvailable()) {
            mStepWidths[2] = 5f;
        }
        mStepWidth = mStepWidths[1];
    }

    public boolean isRotaryAvailable() {
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isRotaryAvailable;
    }

    public void changeStepWidth(int position) {
        mStepWidth = mStepWidths[position];
    }

    public Observable<Boolean> getMovingObservable() {
        return mMovingSubject.hide();
    }

    /**
     * Move to direction by step.
     *
     * @param direction XYZMoveController.Direction
     */
    public void moveXYZByStep(MoveController.Direction direction) {
        if (direction == null) return;
        mMovingSubject.onNext(true);
        MoveController.getInstance()
                .moveByStep(direction, mStepWidth)
                .as(bindToLifecycle())
                .subscribe(response -> mMovingSubject.onNext(false));
    }

    /**
     * Compat method for xyz move.
     *
     * @param direction Directions defined in SteeringView.
     */
    public void moveXYZByStep(int direction) {
        MoveController.Direction xyzDirection = null;
        switch (direction) {
            case SteeringView.DIRECTION_UP:
                xyzDirection = MoveController.Direction.FORWARD;
                break;
            case SteeringView.DIRECTION_DOWN:
                xyzDirection = MoveController.Direction.BACKWARD;
                break;

            case SteeringView.DIRECTION_LEFT:
                xyzDirection = MoveController.Direction.LEFT;
                break;

            case SteeringView.DIRECTION_RIGHT:
                xyzDirection = MoveController.Direction.RIGHT;
                break;
        }
        moveXYZByStep(xyzDirection);
    }
}
