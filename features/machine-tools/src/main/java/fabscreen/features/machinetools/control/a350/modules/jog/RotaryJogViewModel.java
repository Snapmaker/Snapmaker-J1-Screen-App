package fabscreen.features.machinetools.control.a350.modules.jog;

import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class RotaryJogViewModel extends BaseViewModel {
    private float[] mStepAngles = new float[3];
    private float mStepAngle;
    private BehaviorSubject<Boolean> mMovingSubject = BehaviorSubject.createDefault(false);

    public RotaryJogViewModel() {
        mStepAngles[0] = 0.2f;
        mStepAngles[1] = 1f;
        mStepAngles[2] = 5f;
        mStepAngle = mStepAngles[1];
    }

    public void changeStepWidth(int position) {
        mStepAngle = mStepAngles[position];
    }

    public Observable<Boolean> getMovingObservable() {
        return mMovingSubject.hide();
    }

    public void spin(boolean isClockWise) {
        mMovingSubject.onNext(true);
        String gcodePrefix = isClockWise ? "G0 B-" : "G0 B+";
        ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G91")
                .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(gcodePrefix + mStepAngle + " F1800"))
                .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G90"))
                .as(bindToLifecycle())
                .subscribe(res -> {
                    mMovingSubject.onNext(false);
                }, e -> {
                    LogHelper.log(e);
                    mMovingSubject.onNext(false);
                });
    }
}
