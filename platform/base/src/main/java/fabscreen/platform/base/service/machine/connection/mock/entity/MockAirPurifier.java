package fabscreen.platform.base.service.machine.connection.mock.entity;

import fabscreen.platform.base.service.machine.entity.module.AirPurifier;

public class MockAirPurifier extends MockModule {
    private boolean powerSwitch;
    private boolean blowerSwitch;
    private int fanSpeedLevel;
    private int filterLife;
    private boolean filterAlive;

    public AirPurifier.AirPurifierStatus getInfo() {
        AirPurifier.AirPurifierStatus airPurifierStatus = new AirPurifier.AirPurifierStatus();
        airPurifierStatus.setKey(key);
        airPurifierStatus.setModuleStatus(moduleState);
        airPurifierStatus.setPowerSwitch(isPowerSwitch());
        airPurifierStatus.setBlowerSwitch(isBlowerSwitch());
        airPurifierStatus.setFanSpeedLevel(getFanSpeedLevel());
        airPurifierStatus.setFilterLife(getFilterLife());
        airPurifierStatus.setFilterAlive(isFilterAlive());
        return airPurifierStatus;
    }

    public boolean isPowerSwitch() {
        return powerSwitch;
    }

    public void setPowerSwitch(boolean powerSwitch) {
        this.powerSwitch = powerSwitch;
    }

    public boolean isBlowerSwitch() {
        return blowerSwitch;
    }

    public void setBlowerSwitch(boolean blowerSwitch) {
        this.blowerSwitch = blowerSwitch;
    }

    public int getFanSpeedLevel() {
        return fanSpeedLevel;
    }

    public void setFanSpeedLevel(int fanSpeedLevel) {
        this.fanSpeedLevel = fanSpeedLevel;
    }

    public int getFilterLife() {
        return filterLife;
    }

    public void setFilterLife(int filterLife) {
        this.filterLife = filterLife;
    }

    public boolean isFilterAlive() {
        return filterAlive;
    }

    public void setFilterAlive(boolean filterAlive) {
        this.filterAlive = filterAlive;
    }
}
