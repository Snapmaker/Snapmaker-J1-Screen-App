package fabscreen.platform.base.service.machine.connection.mock.entity;

import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.service.machine.entity.parts.Extruder;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

public class MockExtruder {
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private int mId;
    private int mStatus;
    private float mDiameter;
    private int mTemperature;
    private int mTargetTemperature;

    public MockExtruder(int id, int status, float diameter, int temperature, int targetTemperature) {
        mId = id;
        mStatus = status;
        mDiameter = diameter;
        mTemperature = temperature;
        mTargetTemperature = targetTemperature;
        mDisposable.add(Observable.interval(1, TimeUnit.SECONDS)
                .subscribe(time -> {
                    // mState == 2, extruder unusable.
                    if (mStatus == 2) return;
                    int difference = (int) (Math.random() * 20);
                    if (mTemperature <= mTargetTemperature) {
                        mTemperature += difference;
                    } else {
                        mTemperature -= difference;
                        mTemperature = Math.max(mTemperature, 0);
                    }
                }));
    }

    public Extruder getExtruder() {
        return new Extruder(mId, mStatus, mDiameter, mTemperature, mTargetTemperature);
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        this.mStatus = status;
    }

    public float getDiameter() {
        return mDiameter;
    }

    public void setDiameter(float diameter) {
        this.mDiameter = diameter;
    }

    public int getTemperature() {
        return mTemperature;
    }

    public void setTemperature(int temperature) {
        this.mTemperature = temperature;
    }

    public int getTargetTemperature() {
        return mTargetTemperature;
    }

    public void setTargetTemperature(int targetTemperature) {
        this.mTargetTemperature = targetTemperature;
    }

    @Override
    public String toString() {
        return "MockExtruder{" +
                "id=" + mId +
                ", state=" + mStatus +
                ", diameter=" + mDiameter +
                ", temperature=" + mTemperature +
                ", targetTemperature=" + mTargetTemperature +
                '}';
    }

    public void dispose() {
        mDisposable.dispose();
    }
}
