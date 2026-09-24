package fabscreen.features.machinetools.control.a350.modules.toolhead.laser;

import java.util.Locale;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.controller.PrintController;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class LaserPowerControlViewModel extends BaseViewModel {

    public float getInitialLaserPower() {
        return ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserControlPower();
    }

    public void saveLaserPower(float value) {
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLaserControlPower(value);
    }

    public boolean is10wLaserNormalMode() {
        return ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId() == Module.ModuleType.HEAD_LASER_10W && !ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getDebugFlag();
    }

    public void switchOffLaser() {
        ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("M5")
                .as(bindToLifecycle())
                .subscribe();
    }

    public void switchOnLaser(float targetValue) {
        ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "M3 P%.1f", targetValue))
                .as(bindToLifecycle())
                .subscribe();
    }

    public Observable<PrintController.HeaderSecurity> getHeaderSecurityStatus() {
        return ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getHeaderSecurityStatus();
    }

    public void switchOn10wLaser() {
        ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("M3 P1")
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe();
    }
}
