package fabscreen.features.machinetools.control.a350.modules.toolhead.cnc;

import java.util.Locale;

import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseViewModel;

public class SpindleSpeedControlViewModel extends BaseViewModel {
    public void switchOnSpindle(float targetValue) {
        ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "M3 P%.1f", targetValue))
                .as(bindToLifecycle())
                .subscribe(res -> {
                }, LogHelper::log);
    }

    public void switchOffSpindle() {
        ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("M5")
                .as(bindToLifecycle())
                .subscribe(res -> {
                }, LogHelper::log);
    }

    public void saveCNCPower(Float value) {

        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLaserControlPower(value);
    }
}
