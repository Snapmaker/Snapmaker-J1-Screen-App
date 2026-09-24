package fabscreen.platform.base.service.machine.connection.mock.entity;

import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.service.machine.entity.module.HeatedBed;
import io.reactivex.Observable;

public class MockZone {
    private int mIndex;
    private float mCurrentTemperature;
    private int mTargetTemperature;

    public MockZone(int index, float currentTemperature, int targetTemperature) {
        this.mIndex = index;
        this.mCurrentTemperature = currentTemperature;
        this.mTargetTemperature = targetTemperature;
        Observable.interval(1, TimeUnit.SECONDS)
                .subscribe(time -> {
                    int difference = (int) (Math.random() * 5);
                    if (mCurrentTemperature <= mTargetTemperature) {
                        mCurrentTemperature += difference;
                    } else {
                        mCurrentTemperature -= difference;
                        mCurrentTemperature = Math.max(mCurrentTemperature, 0);
                    }
                });
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int index) {
        this.mIndex = index;
    }

    public float getCurrentTemperature() {
        return mCurrentTemperature;
    }

    public void setCurrentTemperature(float currentTemperature) {
        this.mCurrentTemperature = currentTemperature;
    }

    public int getTargetTemperature() {
        return mTargetTemperature;
    }

    public void setTargetTemperature(int targetTemperature) {
        this.mTargetTemperature = targetTemperature;
    }


    public HeatedBed.ZoneInfo getZoneInfo() {
        return new HeatedBed.ZoneInfo(getIndex(), getCurrentTemperature(), getTargetTemperature());
    }
}
