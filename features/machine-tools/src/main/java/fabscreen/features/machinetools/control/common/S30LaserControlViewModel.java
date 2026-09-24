package fabscreen.features.machinetools.control.common;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.controller.LaserController;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.entity.toolhead.LaserToolhead;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public class S30LaserControlViewModel extends BaseViewModel {
    private final LaserController mLaserController;
    private final int mHeadType;

    public S30LaserControlViewModel() {
        IMachine machine = getServiceContainer().getService(IMachine.class);
        mLaserController = machine.getLaserController();
        mHeadType = mLaserController.getHeadType();
    }

    // Switch on if off, switch off if on.
    @SuppressWarnings("ConstantConditions")
    public void switchLaserPower() {
        float power = mLaserController.getLaserToolHeadInfoValue().getLaserTube().getCurrentPower();
        Observable<ResponseStructure<IStructure>> setPowerObservable;
        if (power > 0) {
            setPowerObservable = mLaserController.setLaserPower(0, 0f);
        } else {
            setPowerObservable = mLaserController.setLaserPower(0, getSwitchOnTargetPower());
        }

        setPowerObservable
                .as(bindToLifecycle())
                .subscribe(response -> {
                    // TODO: 2022/3/1  set success
                }, LogHelper::log);
    }

    public Observable<Boolean> getLaserPowerObservable() {
        return mLaserController.getLaserToolHeadInfoObservable()
                .flatMap((Function<LaserToolhead.LaserToolheadInfo, ObservableSource<Boolean>>) toolheadInfo
                        -> Observable.just(toolheadInfo.getLaserTube().getCurrentPower() > 0));
    }

    public Observable<String> getBluetoothAddressObservable() {
        return mLaserController.getBluetoothAddressObservable();
    }

    public void subscribeLaserStatus() {
        mLaserController.subscribeLaserTubeStatus().as(bindToLifecycle()).subscribe();
    }

    public void unsubscribeLaserStatus() {
        mLaserController.unSubscribeLaserTubeStatus().as(bindToLifecycle()).subscribe();
    }

    public Observable<Boolean> switchAssistLight(boolean on) {
        return mLaserController.switchFocusAssistLight(on ? 100 : 0).flatMap(structure -> Observable.just(structure.isSuccess()));
    }

    public Observable<Boolean> setFocalLen(float len) {
        return mLaserController.setFocalLength(len).flatMap(structure -> Observable.just(structure.isSuccess()));
    }


    public Observable<Boolean> setTemp(int protect, int resume) {
        return mLaserController.setTemperatureThreshold(resume, protect).flatMap(structure -> Observable.just(structure.isSuccess()));
    }

    private float getSwitchOnTargetPower() {
        float targetPower = 0f;
        if (mHeadType == Module.ModuleType.HEAD_LASER) {
            targetPower = 1f;
        } else if (mHeadType == Module.ModuleType.HEAD_LASER_10W) {
            targetPower = 0.5f;
        }
        return targetPower;
    }

    public float getCurrentPower() {
        return mLaserController.getLaserToolHeadInfoValue().getLaserTube().getCurrentPower();
    }
}
