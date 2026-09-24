package fabscreen.platform.base.service.machine.connection.mock.entity;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.service.machine.entity.module.DryBox;
import fabscreen.platform.base.service.machine.entity.parts.DryBoxStatus;

public class MockDryBox extends MockModule {
    private int dryState;
    private int tempCurrentChamber;
    private int tempTargetChamber;
    private int tempWindHole;
    private int currentHumidity;
    private int targetHumidity;
    private int residualHeatingTime;
    private int targetHeatingTime;
    private int cumulativeHeatingTime;
    private int lidState;
    private int heaterBlockState;

    private float tempCurrentOutlet;

    public DryBox.DryBoxInfo getInfo() {
        DryBox.DryBoxInfo dryBoxInfo = new DryBox.DryBoxInfo();
        dryBoxInfo.setKey(key);
        dryBoxInfo.setModuleStatus(moduleState);
        DryBoxStatus dryBoxStatus = new DryBoxStatus();
        dryBoxStatus.setDryState(getDryState());
        dryBoxStatus.setTempCurrentChamber(getTempCurrentChamber());
        dryBoxStatus.setTempTargetChamber(getTempTargetChamber());
        dryBoxStatus.setTempWindHole(getTempWindHole());
        dryBoxStatus.setCurrentHumidity(getCurrentHumidity());
        dryBoxStatus.setTargetHumidity(getTargetHumidity());
        dryBoxStatus.setResidualHeatingTime(getResidualHeatingTime());
        dryBoxStatus.setTargetHeatingTime(getTargetHeatingTime());
        dryBoxStatus.setCumulativeHeatingTime(getCumulativeHeatingTime());
        dryBoxStatus.setLidState(getLidState());
        dryBoxStatus.setHeaterBlockState(getHeaterBlockState());
        dryBoxInfo.setDryBoxStatus(dryBoxStatus);
        return dryBoxInfo;
    }

    public int getHeaterBlockState() {
        return heaterBlockState;
    }

    public void setHeaterBlockState(int heaterBlockState) {
        this.heaterBlockState = heaterBlockState;
    }

    public long getTargetHeatingTime() {
        return targetHeatingTime;
    }

    public void setTargetHeatingTime(int targetHeatingTime) {
        this.targetHeatingTime = targetHeatingTime;
    }

    public long getResidualHeatingTime() {
        return residualHeatingTime;
    }

    public void setResidualHeatingTime(int residualHeatingTime) {
        this.residualHeatingTime = residualHeatingTime;
    }

    public int getTempWindHole() {
        return tempWindHole;
    }

    public void setTempWindHole(int tempWindHole) {
        this.tempWindHole = tempWindHole;
    }

    public float getTempCurrentOutlet() {
        return tempCurrentOutlet;
    }

    public void setTempCurrentOutlet(float tempCurrentOutlet) {
        this.tempCurrentOutlet = tempCurrentOutlet;
    }

    public int getDryState() {
        return dryState;
    }

    public void setDryState(int dryState) {
        this.dryState = dryState;
    }

    public int getTempCurrentChamber() {
        return tempCurrentChamber;
    }

    public void setTempCurrentChamber(int tempCurrentChamber) {
        this.tempCurrentChamber = tempCurrentChamber;
    }

    public int getTempTargetChamber() {
        return tempTargetChamber;
    }

    public void setTempTargetChamber(int tempTargetChamber) {
        this.tempTargetChamber = tempTargetChamber;
    }

    public int getCurrentHumidity() {
        return currentHumidity;
    }

    public void setCurrentHumidity(int currentHumidity) {
        this.currentHumidity = currentHumidity;
    }

    public int getTargetHumidity() {
        return targetHumidity;
    }

    public void setTargetHumidity(int targetHumidity) {
        this.targetHumidity = targetHumidity;
    }

    public int getCumulativeHeatingTime() {
        return cumulativeHeatingTime;
    }

    public void setCumulativeHeatingTime(int cumulativeHeatingTime) {
        this.cumulativeHeatingTime = cumulativeHeatingTime;
    }

    public int getLidState() {
        return lidState;
    }

    public void setLidState(int lidState) {
        this.lidState = lidState;
    }

    public DryBoxStatus getDryBoxStatus() {
        DryBoxStatus dryBoxStatus = new DryBoxStatus();
        dryBoxStatus.setDryState(getDryState());
        dryBoxStatus.setTempCurrentChamber(getTempCurrentChamber());
        dryBoxStatus.setTempTargetChamber(getTempTargetChamber());
        dryBoxStatus.setTempWindHole(getTempWindHole());
        dryBoxStatus.setCurrentHumidity(getCurrentHumidity());
        dryBoxStatus.setTargetHumidity(getTargetHumidity());
        dryBoxStatus.setResidualHeatingTime(getResidualHeatingTime());
        dryBoxStatus.setTargetHeatingTime(getTargetHeatingTime());
        dryBoxStatus.setCumulativeHeatingTime(getCumulativeHeatingTime());
        dryBoxStatus.setLidState(getLidState());
        return dryBoxStatus;
    }
}
