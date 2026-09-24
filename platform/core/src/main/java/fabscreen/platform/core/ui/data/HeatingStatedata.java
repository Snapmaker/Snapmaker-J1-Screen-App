package fabscreen.platform.core.ui.data;

public class HeatingStatedata {
    public float temperature = 0;
    public float targetTemperature = 0;
    public boolean heatingStats = false;
    public int movementStats = -1;
    public int preStopTemperature = 200;
    public boolean targetChange = false;

    public HeatingStatedata() {
    }

    public HeatingStatedata(float temperature, float targetTemperature, boolean heatingStats, int movementStats, int preStopTemperature, boolean targetChange) {
        this.temperature = temperature;
        this.targetTemperature = targetTemperature;
        this.heatingStats = heatingStats;
        this.movementStats = movementStats;
        this.preStopTemperature = preStopTemperature;
        this.targetChange = targetChange;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getTargetTemperature() {
        return targetTemperature;
    }

    public boolean isHeatingStats() {
        return heatingStats;
    }

    public boolean isMovement() {
        return movementStats != -1;
    }

    public int getMovementStats() {
        return movementStats;
    }

    public boolean isCanMovement() {
        return targetTemperature != 0 && temperature >= targetTemperature - 3;
    }

    public boolean isTargetChange() {
        return targetChange;
    }


    public int getStopTemperature() {
        return preStopTemperature;
    }

    public void setTemperature(int temperature) {
        if (heatingStats) {
            targetTemperature = temperature;
        } else {
            preStopTemperature = temperature;
        }
    }
}
