package fabscreen.platform.base.service.machine.connection.mock.entity;

import fabscreen.platform.base.service.machine.entity.module.LinearModule;

public class MockLinear extends MockModule {
    private boolean isHome;
    private boolean LimitSwitchState;
    private boolean LimitSwitch;
    private float lead;


    public LinearModule.LinearModuleStatus getInfo() {
        LinearModule.LinearModuleStatus moduleStatus = new LinearModule.LinearModuleStatus();
        moduleStatus.setKey(key);
        moduleStatus.setIsHome(isHome());
        moduleStatus.setHaveEnableLimit(isLimitSwitchState());
        moduleStatus.setEnableLimit(isLimitSwitch());
        return moduleStatus;

    }

    public boolean isHome() {
        return isHome;
    }

    public void setHome(boolean home) {
        isHome = home;
    }

    public boolean isLimitSwitchState() {
        return LimitSwitchState;
    }

    public void setLimitSwitchState(boolean limitSwitchState) {
        LimitSwitchState = limitSwitchState;
    }

    public boolean isLimitSwitch() {
        return LimitSwitch;
    }

    public void setLimitSwitch(boolean limitSwitch) {
        LimitSwitch = limitSwitch;
    }

    public float getLead() {
        return lead;
    }

    public void setLead(float lead) {
        this.lead = lead;
    }


}
