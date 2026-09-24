package fabscreen.features.print.a400platform.viewmodel;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.entity.module.Enclosure;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;

public class A400EnclosureControlViewModel extends BaseViewModel {
    private final Enclosure mEnclosure;

    public A400EnclosureControlViewModel() {
        super();
        mEnclosure = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure();
    }

    public Observable<Enclosure.EnclosureStatus> getEnclosureStatusObservable() {
        return mEnclosure.getEnclosureStatusObservable();
    }

    public boolean isDoorDetectionEnabled() {
        return mEnclosure.getEnclosureStatusValue().isDoorDetectionEnabled();
    }

    public boolean isEnclosureLedOn() {
        return mEnclosure.getEnclosureStatusValue().isLedOn();
    }

    public boolean isEnclosureFanOn() {
        return mEnclosure.getEnclosureStatusValue().isFanOn();
    }

    public void setLedLevel(int value) {
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure()
                .setEnclosureLedLevel(value)
                .as(bindToLifecycle())
                .subscribe(ret -> {/**/}, LogHelper::log);
    }

    public void setFanLevel(int value) {
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure()
                .setEnclosureFanLevel(value)
                .as(bindToLifecycle())
                .subscribe(ret -> {/**/}, LogHelper::log);
    }

    public void setDoorDetection(boolean enabled) {
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure()
                .setEnclosureDoorDetection(enabled)
                .as(bindToLifecycle())
                .subscribe(ret -> {/**/}, LogHelper::log);
    }

    public boolean isEnclosureAutoLighting() {
        return ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getEnclosureAutoLightingOn();
    }

    public void setEnclosureAutoLighting(boolean enabled) {
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setEnclosureAutoLightingOn(enabled);
    }

    public void subscribeEnclosureStatus() {
        mEnclosure.subscribeEnclosureInfo();
    }

    public void unSubscribeEnclosureStatus() {
        mEnclosure.unsubscribeEnclosureInfo();
    }
}
