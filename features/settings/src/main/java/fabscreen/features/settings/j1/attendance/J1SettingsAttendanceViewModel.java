package fabscreen.features.settings.j1.attendance;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.INetwork;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.entity.toolhead.FdmToolhead;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.prop.FloatProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;
import kotlin.io.FilesKt;

public class J1SettingsAttendanceViewModel extends BaseViewModel {

    private final MachineController mMachineController;
    private final BehaviorSubject<Boolean> mVibrationCompensationEnabledSubj = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> mHomeStallDetectionEnabledSubject = BehaviorSubject.createDefault(false);
    private final BehaviorSubject<Float> mPlatformHeightSubject = BehaviorSubject.createDefault(-1f);

    public J1SettingsAttendanceViewModel() {
        mMachineController = getServiceContainer().getService(IMachine.class).getMachineController();
        mMachineController.subscribeVibrationCompensationConfig();
        mMachineController.watchVibrationCompensationConfig()
                .as(bindToLifecycle())
                .subscribe(config -> mVibrationCompensationEnabledSubj.onNext(config.getEnabled()), LogHelper::log);

        requestHomeStallDetection();
        requestPlatformHeight();
    }

    public Observable<Boolean> getVibrationCompensationEnabledObservable() {
        return mVibrationCompensationEnabledSubj.distinctUntilChanged();
    }

    public Observable<Boolean> J1FactoryReset() {
        return ServiceContainer.getInstance().getService(IMachine.class)
                .getMachineController()
                .requestMachineFactoryReset(0)
                .concatMap(response -> response.isSuccess() ? resetScreen() : Observable.just(false));
    }

    private Observable<Boolean> resetScreen() {
        return Observable.create(emitter -> {
            ServiceContainer.getInstance().getService(INetwork.class).removeOrDisableAllWifi();
            ServiceContainer.getInstance().getService(IPrintWorkspace.class).clearAllWorkSpaceFiles();
            FilesKt.deleteRecursively(ServiceContainer.getInstance().getService(IAppService.class).getCacheDir());
            FilesKt.deleteRecursively(ServiceContainer.getInstance().getService(IAppService.class).getFilesDir());
            emitter.onNext(true);
        }).concatMap(success -> resetSP().retry(10));
    }

    private Observable<Boolean> resetSP() {
        Logger.d("Resetting SP...");
        return Observable.create(emitter -> {
            if (ServiceContainer.getInstance().getService(IPreferences.class).getHelper().reset()) {
                emitter.onNext(true);
            } else {
                // Throw exception when reset SP fail, to trigger retry.
                emitter.onError(new IllegalStateException("Reset SP fail!"));
            }
        });
    }

    /**
     * @param extruderIndex 0 left, 1 right
     *                      query M412: 1 is on, 0 is off
     */
    public boolean isRunoutRecoveryEnabled(int extruderIndex) {
        FdmToolhead.FdmToolheadStatus fdmToolheadStatus = ServiceContainer.getInstance().getService(IMachine.class)
                .getFDMController()
                .getToolheadStatusSubjectHolder(extruderIndex)
                .getValue();
        int filamentDetectionStatus = fdmToolheadStatus.getExtruderList().get(0).getFilamentDetectionStatus();
        return filamentDetectionStatus == 1;
    }

    public boolean isLightOn() {
        // TODO: 2022/5/26 return the real value
        return true;
    }

    public Observable<Boolean> getHomeStallDetectionObservable() {
        return mHomeStallDetectionEnabledSubject.hide();
    }

    public boolean isHomeStallDetectionEnabled() {
        return mHomeStallDetectionEnabledSubject.getValue();
    }

    public void setHomeStallDetection(boolean isChecked) {
        if (mHomeStallDetectionEnabledSubject.getValue() == isChecked) return;
        mMachineController.setHomeStallDetectionEnabled(isChecked)
                .as(bindToLifecycle())
                .subscribe(structure -> {
                    requestHomeStallDetection();
                    if (!structure.isSuccess()) {
                        Logger.w("%s Home Stall Detection failed, code %s", isChecked ? "Enable" : "Disable", structure.resultProp.getValue());
                    }
                }, LogHelper::log);
    }

    private void requestHomeStallDetection() {
        mMachineController.getHomeStallDetectionEnabled()
                .as(bindToLifecycle())
                .subscribe(structure -> {
                    BaseStructure baseStructure = (BaseStructure) structure.dataProp;
                    int status = ((UInt8Prop) baseStructure.getProp("home_stall_detection_status")).getValue();
                    mHomeStallDetectionEnabledSubject.onNext(status != 0);
                });
    }

    public Observable<Float> getPlatformHeightObservable() {
        return mPlatformHeightSubject.hide();
    }

    private void requestPlatformHeight() {
        mMachineController.getPlatformHeight()
                .as(bindToLifecycle())
                .subscribe(structure -> {
                    BaseStructure baseStructure = (BaseStructure) structure.dataProp;
                    float platformHeight = ((FloatProp) baseStructure.getProp("machine_platform_height")).getValue();
                    mPlatformHeightSubject.onNext(platformHeight);
                });
    }

    public void setRunoutRecoveryEnabled(int index, boolean isChecked) {
        ServiceContainer.getInstance().getService(IMachine.class)
                .getFDMController()
                .setFilamentSensorStatus(index, 0, isChecked ? 1 : 0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                }, LogHelper::log);
    }

    public void setLightingEnabled(boolean isChecked) {
    }

    @Override
    protected void onCleared() {
        mMachineController.unsubscribeVibrationCompensationConfig();
        super.onCleared();
    }
}
