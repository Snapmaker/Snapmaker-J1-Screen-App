package fabscreen.features.machinetools.control.common;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.controller.CNCController;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.entity.toolhead.CNCToolhead;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class S30CNCControlViewModel extends BaseViewModel {

    private final BehaviorSubject<Boolean> mPowerOnOffSubject = BehaviorSubject.createDefault(false);
    private final BehaviorSubject<Long> mTargetSpeedSubject = BehaviorSubject.createDefault(0L);
    private final BehaviorSubject<Long> mCurrentSpeedSubject = BehaviorSubject.createDefault(0L);
    private final CNCController mCncController;

    public static final int POWER_MODE = 0;
    public static final int RPM_MODE = 1;
    private final int mControlMode;

    public S30CNCControlViewModel() {
        IMachine machine = getServiceContainer().getService(IMachine.class);
        mCncController = machine.getCNCController();
        MachineInfo info = machine.getMachineInfoSubjectHolder().getValue();
        // Control Mode always set to 1 in A400 machine.
        mControlMode = mCncController.getHeadType() == Module.ModuleType.HEAD_CNC_200W ? RPM_MODE : POWER_MODE;
        mCncController.setControlMode(0, mControlMode)
                .as(bindToLifecycle())
                .subscribe(response -> {
                }, LogHelper::log);

        mCncController.getCncToolHeadInfoObservable()
                .as(bindToLifecycle())
                .subscribe(response -> {
                    Logger.d("vmrsp: %s", response);
                    long targetSpeed = response.getTargetSpeed();
                    long currentSpeed = response.getCurrentSpeed();
                    boolean isOn = response.getRunningState() == 1;
                    if (mTargetSpeedSubject.getValue() != targetSpeed) {
                        mTargetSpeedSubject.onNext(targetSpeed);
                    }
                    if (mCurrentSpeedSubject.getValue() != currentSpeed) {
                        mCurrentSpeedSubject.onNext(currentSpeed);
                    }
                    if (mPowerOnOffSubject.getValue() != isOn) {
                        mPowerOnOffSubject.onNext(isOn);
                    }
                }, LogHelper::log);

    }

    public Observable<Long> getTargetSpeedObservable() {
        return mTargetSpeedSubject.hide();
    }

    public Observable<Long> getCurrentSpeedObservable() {
        return mCurrentSpeedSubject.hide();
    }

    public Observable<CNCToolhead.CNCToolheadInfo> getCncToolHeadInfoObservable() {
        return mCncController.getCncToolHeadInfoObservable();
    }

    public void switchControlMode(int mode) {
        mCncController.setControlMode(0, mode)
                .as(bindToLifecycle())
                .subscribe();
    }

    public void setSpeedInPower(int percent) {
        mCncController.setSpindlePower(0, percent)
                .as(bindToLifecycle())
                .subscribe();
    }

    public void switchSpindlePower() {
        int currentPower = mCncController.getCncToolHeadInfoValue().getCurrentPower();
        Observable<ResponseStructure<IStructure>> switchCNCObservable;
        if (currentPower > 0) {
            // Spindle is powered on, let's switch it off.
            switchCNCObservable = mCncController.switchCNC(0, false);
        } else {
            switchCNCObservable = mCncController.switchCNC(0, true);
        }

        switchCNCObservable
                .as(bindToLifecycle())
                .subscribe(response -> {
                }, LogHelper::log);
    }

    public void setSpeedInRPM(int rpm) {
        mCncController.setTargetSpeed(0, rpm)
                .as(bindToLifecycle())
                .subscribe(response -> {
                }, LogHelper::log);
    }

    public void setSpindleSpeed(int percent) {
        if (mControlMode == POWER_MODE) {
            setSpeedInPower(percent);
        } else if (mControlMode == RPM_MODE) {
            setSpeedInRPM((int) (20000 * (percent / 100f)));
        }
    }

    public Observable<Boolean> getPowerOnOffObservable() {
        return mPowerOnOffSubject.hide();
    }

    public void subscribeCNCInfo() {
        mCncController.subscribeCNCInfo();
    }

    public void unSubscribeCNCInfo() {
        mCncController.unSubscribeCNCInfo();
    }

    public int getMode() {
        return mControlMode;
    }

    public int getCurrentPower() {
        return mCncController.getCncToolHeadInfoValue().getCurrentPower();
    }
}
