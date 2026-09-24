package fabscreen.features.machinetools.calibration.a400platform.cnc.toolchange;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.data.MoveController;
import fabscreen.platform.lib.SubjectHolder;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

public class A400CncToolChangeAssistantViewModel extends BaseViewModel {
    private MachineController machineController;
    private SubjectHolder<MachineStatus> machineStatusSubjectHolder;
    private Subject<Boolean> mIsMovingSubject = BehaviorSubject.create();

    public A400CncToolChangeAssistantViewModel() {
        super();
        machineController = ServiceContainer.getInstance().getService(IMachine.class).getMachineController();
        machineStatusSubjectHolder = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder();
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
        return machineController.setWorkOrigin(machineStatusSubjectHolder.getValue().currentPosition);
    }
}
