package fabscreen.platform.base.service.machine.connection.mock.entity;

import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.service.machine.entity.toolhead.CNCToolhead;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

public class MockCNCToolHead extends MockModule {
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private int mHeadStatus;
    private boolean mHeadActive;
    private int mRunningState;
    private int mControlMode;
    private int mCurrentPower;
    private int mTargetPower;
    private long mCurrentSpeed;
    private long mTargetSpeed;

    public MockCNCToolHead(int headStatus, boolean headActive, int runningState, int controlMode, int currentPower, int targetPower, long currentSpeed, long targetSpeed) {
        this.mHeadStatus = headStatus;
        this.mHeadActive = headActive;
        this.mRunningState = runningState;
        this.mControlMode = controlMode;
        this.mCurrentPower = currentPower;
        this.mTargetPower = targetPower;
        this.mCurrentSpeed = currentSpeed;
        this.mTargetSpeed = targetSpeed;

        mDisposable.add(Observable.interval(1, TimeUnit.SECONDS)
                .subscribe(time -> {
                    // mState == 2, extruder unusable.
                    if (mRunningState == 2) return;
                    int difference = (int) (Math.random() * 20);
                    mCurrentPower = mTargetPower;
                    if (mCurrentSpeed <= mTargetSpeed) {
                        mCurrentSpeed += difference;
                        mCurrentSpeed = Math.min(mCurrentSpeed, mTargetSpeed);
                    } else {
                        mCurrentSpeed -= difference;
                        mCurrentSpeed = Math.max(mCurrentSpeed, 0);
                    }
                }));
    }

    public CNCToolhead.CNCToolheadInfo getCncToolHeadInfo() {
        CNCToolhead.CNCToolheadInfo cncToolheadInfo = new CNCToolhead.CNCToolheadInfo();
        cncToolheadInfo.setKey(key);
        cncToolheadInfo.setHeadStatus(getHeadStatus());
        cncToolheadInfo.setHeadActive(isHeadActive());
        cncToolheadInfo.setControlMode(getControlMode());
        cncToolheadInfo.setRunningState(getRunningState());
        cncToolheadInfo.setCurrentPower(getCurrentPower());
        cncToolheadInfo.setTargetPower(getTargetPower());
        cncToolheadInfo.setCurrentSpeed(getCurrentSpeed());
        cncToolheadInfo.setTargetSpeed(getTargetSpeed());
        return cncToolheadInfo;
    }

    public int getHeadStatus() {
        return mHeadStatus;
    }

    public void setHeadStatus(int headStatus) {
        this.mHeadStatus = headStatus;
    }

    public boolean isHeadActive() {
        return mHeadActive;
    }

    public void setHeadActive(boolean headActive) {
        this.mHeadActive = headActive;
    }

    public int getRunningState() {
        return mRunningState;
    }

    public void setRunningState(int runningState) {
        this.mRunningState = runningState;
    }

    public int getControlMode() {
        return mControlMode;
    }

    public void setControlMode(int controlMode) {
        this.mControlMode = controlMode;
    }

    public int getCurrentPower() {
        return mCurrentPower;
    }

    public void setCurrentPower(int currentPower) {
        this.mCurrentPower = currentPower;
    }

    public int getTargetPower() {
        return mTargetPower;
    }

    public void setTargetPower(int targetPower) {
        this.mTargetPower = targetPower;
    }

    public long getCurrentSpeed() {
        return mCurrentSpeed;
    }

    public void setCurrentSpeed(long currentSpeed) {
        this.mCurrentSpeed = currentSpeed;
    }

    public long getTargetSpeed() {
        return mTargetSpeed;
    }

    public void setTargetSpeed(long targetSpeed) {
        this.mTargetSpeed = targetSpeed;
    }
}
