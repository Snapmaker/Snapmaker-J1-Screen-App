package fabscreen.features.print.a400platform.viewmodel;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.ISlaveComputer;
import fabscreen.platform.base.model.system.DeprecatedMachineInfo;
import fabscreen.platform.base.model.system.MachineStatusManager;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class WorkOriginControlViewModel extends BaseViewModel {
    private BehaviorSubject<Boolean> mMovingSubject = BehaviorSubject.createDefault(false);
    private BehaviorSubject<ActiveStatus> mButtonActiveSubject = BehaviorSubject.create();

    public boolean isRotaryAvailable() {
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isRotaryAvailable;
    }

    public Observable<DeprecatedMachineInfo> getMachineStatusObservable() {
        return MachineStatusManager.getMachineInfoHolder().getObservable();
    }

    public Observable<Boolean> getMovingObservable() {
        return mMovingSubject.distinctUntilChanged();
    }

    public Observable<ActiveStatus> getButtonActiveObservable() {
        return mButtonActiveSubject.distinctUntilChanged();
    }

    public float getOffsetX() {
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getX();
    }

    public float getOffsetY() {
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getY();
    }

    public float getOffsetZ() {
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getZ();
    }

    public void setOriginByFlag(Vector vector, int viewId) {
        mMovingSubject.onNext(true);
        mButtonActiveSubject.onNext(new ActiveStatus(viewId, true));

        Logger.i("Requesting set origin %s ...", vector);

        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector)
                .flatMap(this::updateCoordinateSystem)
                .as(bindToLifecycle())
                .subscribe(res -> {
                    mMovingSubject.onNext(false);
                    mButtonActiveSubject.onNext(new ActiveStatus(viewId, false));
                });
    }

    public void gotoOrigin(int viewId) {
        mMovingSubject.onNext(true);
        mButtonActiveSubject.onNext(new ActiveStatus(viewId, true));
        final float currentZ = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().currentPosition.getZ();
        Logger.i("Requesting go to origin...");
        if (currentZ > 0) {
            // Engage direction, move X Y linear module and B rotary module first, then Z.
            ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 X0 Y0 F3000")
                    .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 B0 F3000"))
                    .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 Z0 F1800"))
                    .as(bindToLifecycle())
                    .subscribe(success -> {
                        mMovingSubject.onNext(false);
                        mButtonActiveSubject.onNext(new ActiveStatus(viewId, false));
                    }, e -> {
                        LogHelper.log(e);
                        mMovingSubject.onNext(false);
                        mButtonActiveSubject.onNext(new ActiveStatus(viewId, false));
                    });
        } else {
            // Retract direction, move Z linear module first, then X Y and B.
            ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 Z0 F1800")
                    .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 X0 Y0 F3000"))
                    .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 B0 F3000"))
                    .as(bindToLifecycle())
                    .subscribe(success -> {
                        mMovingSubject.onNext(false);
                        mButtonActiveSubject.onNext(new ActiveStatus(viewId, false));
                    }, e -> {
                        LogHelper.log(e);
                        mMovingSubject.onNext(false);
                        mButtonActiveSubject.onNext(new ActiveStatus(viewId, false));
                    });
        }
    }

    public void goHome(int viewId) {
        mMovingSubject.onNext(true);
        mButtonActiveSubject.onNext(new ActiveStatus(viewId, true));

        Logger.i("Requesting G28...");

        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .flatMap(response -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().home(0))
                .flatMap(response -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1))
                .as(bindToLifecycle())
                .subscribe(success -> {
                    mMovingSubject.onNext(false);
                    mButtonActiveSubject.onNext(new ActiveStatus(viewId, false));
                }, e -> {
                    LogHelper.log(e);
                    mMovingSubject.onNext(false);
                    mButtonActiveSubject.onNext(new ActiveStatus(viewId, false));
                });
    }

    private String getAxisByNum(int axisFlag) {
        switch (axisFlag) {
            case ISlaveComputer.FLAG_X:
                return "x";
            case ISlaveComputer.FLAG_Y:
                return "y";
            case ISlaveComputer.FLAG_Z:
                return "z";
            case ISlaveComputer.FLAG_B:
                return "b";
            case ISlaveComputer.FLAG_XYZ:
                return "xyz";
            case ISlaveComputer.FLAG_XYZB:
                return "xyzb";
            default:
                return "";
        }
    }

    private Observable<MachineStatus> updateCoordinateSystem(Object response) {
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem();
    }

    public static class ActiveStatus {
        public int viewId;
        public boolean isActive;

        public ActiveStatus(int viewId, boolean isActive) {
            this.viewId = viewId;
            this.isActive = isActive;
        }
    }
}
