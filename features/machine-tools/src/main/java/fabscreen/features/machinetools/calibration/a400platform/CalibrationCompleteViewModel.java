package fabscreen.features.machinetools.calibration.a400platform;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class CalibrationCompleteViewModel extends BaseViewModel {
    BehaviorSubject<Boolean> mIsExitingSubj = BehaviorSubject.createDefault(true);

    public Observable<Boolean> saveAndExitCalibration() {
        //noinspection rawtypes
        Observable<ResponseStructure> responseStructureObservable = null;
        IMachine.WorkType workType = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType;
        switch (workType) {
            case FDM:
                responseStructureObservable = ServiceContainer.getInstance().getService(IMachine.class).getFDMController().exitCalibration(true);
                break;
            case LASER:
                responseStructureObservable = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().exitCalibration(true);
                break;
            case CNC:
                responseStructureObservable = ServiceContainer.getInstance().getService(IMachine.class).getCNCController().exitCalibration(true);
                break;
            default:
                break;
        }
        if (responseStructureObservable == null) {
            mIsExitingSubj.onNext(false);
            return mIsExitingSubj.hide();
        }

        responseStructureObservable
                .doOnSubscribe(disposable -> mIsExitingSubj.onNext(true))
                .doOnNext(response -> mIsExitingSubj.onNext(false))
                .doOnError(e -> mIsExitingSubj.onNext(false))
                .as(bindToLifecycle())
                .subscribe(success -> {
                    if (!success.isSuccess()) {
                        Logger.d("Exit Calibration: " + success);
                    }
                }, LogHelper::log);

        coolDownBedIfHave();
        return mIsExitingSubj.hide();
    }

    private void coolDownBedIfHave() {
        MachineController machineController = ServiceContainer.getInstance().getService(IMachine.class).getMachineController();
        if (machineController.getHeatedBed() != null) {
            machineController.getHeatedBed().setZoneTargetTemperature(0, 0).as(bindToLifecycle()).subscribe(success -> {
            }, LogHelper::log);
        }
    }
}
