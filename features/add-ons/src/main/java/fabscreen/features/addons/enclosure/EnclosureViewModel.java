package fabscreen.features.addons.enclosure;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.SSTPPacketContent;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class EnclosureViewModel extends BaseViewModel {

    private BehaviorSubject<SSTPPacketContent.EnclosureStatus> mEnclosureStatusSubject = BehaviorSubject.create();

    public EnclosureViewModel() {
        super();

        // update status
        updateEnclosureStatus();
    }

    private void updateEnclosureStatus() {
        // TODO: Does it need to be updated?
//        ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().getMachineController()
//                .updateEnclosureStatus()
//                .as(bindToLifecycle())
//                .subscribe(enclosureStatus -> {
//                    mEnclosureStatusSubject.onNext(enclosureStatus);
//                }, LogHelper::log);
        ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getObservable()
                .as(bindToLifecycle())
                .subscribe(machineInfo -> {
                    mEnclosureStatusSubject.onNext(null);
                }, LogHelper::log);
    }

    Observable<SSTPPacketContent.EnclosureStatus> getEnclosureStatusObservable() {
        return mEnclosureStatusSubject.hide();
    }

    boolean isDoorDetectionEnabled() {
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure().getEnclosureStatusValue().isDoorDetectionEnabled();
    }

    boolean isEnclosureLedOn() {
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure().getEnclosureStatusValue().isLedOn();
    }

    boolean isEnclosureFanOn() {
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure().getEnclosureStatusValue().isFanOn();
    }

    void setLedLevel(int value) {
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure()
                .setEnclosureLedLevel(value)
                .doOnNext(success -> updateEnclosureStatus())
                .as(bindToLifecycle())
                .subscribe(ret -> {/**/}, LogHelper::log);
    }

    void setFanLevel(int value) {
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure()
                .setEnclosureFanLevel(value)
                .doOnNext(success -> updateEnclosureStatus())
                .as(bindToLifecycle())
                .subscribe(ret -> {/**/}, LogHelper::log);
    }

    void setDoorDetection(boolean enabled) {
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure()
                .setEnclosureDoorDetection(enabled)
                .doOnNext(success -> updateEnclosureStatus())
                .as(bindToLifecycle())
                .subscribe(ret -> {/**/}, LogHelper::log);
    }

    boolean isEnclosureAutoLighting() {
        return ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getEnclosureAutoLightingOn();
    }

    void setEnclosureAutoLighting(boolean enabled) {
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setEnclosureAutoLightingOn(enabled);
    }
}
