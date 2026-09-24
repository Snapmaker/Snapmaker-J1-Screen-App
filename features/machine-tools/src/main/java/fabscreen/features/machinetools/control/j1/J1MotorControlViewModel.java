package fabscreen.features.machinetools.control.j1;

import android.util.Log;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class J1MotorControlViewModel extends BaseViewModel {

    private final MachineController mMachineController;
    private final BehaviorSubject<Boolean> mSwitchSubject = BehaviorSubject.createDefault(false);

    public J1MotorControlViewModel() {
        mMachineController = getServiceContainer().getService(IMachine.class).getMachineController();
    }

    public void switchMotor(boolean isChecked) {
        mMachineController.controlSwitchMotor(isChecked)
                .as(bindToLifecycle())
                .subscribe(structure -> {
                    if (structure.isSuccess()) {
                        Logger.d("Switch motor success!");
                        mSwitchSubject.onNext(isChecked);
                    } else {
                        Logger.d("Switch motor fail!");
                    }
                }, throwable -> {
                    Logger.d("Switch motor fail!");
                    LogHelper.log(throwable);
                });
    }

    public Observable<Boolean> getMotorStateObservable() {
        return mMachineController.getMotorStateObservable();
    }

    public Observable<Boolean> getMovingObservable() {
        return mSwitchSubject.hide();
    }
}
