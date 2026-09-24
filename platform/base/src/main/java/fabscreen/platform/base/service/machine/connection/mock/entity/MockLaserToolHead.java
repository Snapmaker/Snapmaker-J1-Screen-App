package fabscreen.platform.base.service.machine.connection.mock.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.service.machine.entity.parts.Fan;
import fabscreen.platform.base.service.machine.entity.parts.LaserTube;
import fabscreen.platform.base.service.machine.entity.toolhead.LaserToolhead;
import fabscreen.platform.base.service.machine.structure.LaserSafetyStateStructure;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

public class MockLaserToolHead extends MockModule {
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private int mHeadStatus;
    private int mBrightness;
    private float mLaserFocalLength;
    private float mCurrentPower;
    private float mTargetPower;
    private List<MockFan> mFanList;

    private float mCurrentTemperature;
    private float mTubeRollAngle;
    private float mTubePitchAngle;
    private int mTrecoveryTemperature;
    private int mProtectTemperature;

    public MockLaserToolHead(int headStatus, int brightness, float laserFocalLength, float currentPower, float targetPower, List<MockFan> fanList) {
        this.mHeadStatus = headStatus;
        this.mBrightness = brightness;
        this.mLaserFocalLength = laserFocalLength;
        this.mCurrentPower = currentPower;
        this.mTargetPower = targetPower;
        this.mFanList = fanList;
        mDisposable.add(Observable.interval(1, TimeUnit.SECONDS)
                .subscribe(time -> {
                    // mState == 2, extruder unusable.
                    if (mHeadStatus == 2) return;
                    mCurrentPower = mTargetPower;
                }));
    }

    public LaserToolhead.LaserToolheadInfo getLaserToolHeadInfo() {
        LaserToolhead.LaserToolheadInfo laserToolheadInfo = new LaserToolhead.LaserToolheadInfo();
        laserToolheadInfo.setKey(key);
        laserToolheadInfo.setHeadStatus(getHeadStatus());
        laserToolheadInfo.setLaserFocalLength(getLaserFocalLength());
        laserToolheadInfo.setLaserTube(getLaserTube());
        ArrayList<Fan> fans = new ArrayList<>();
        for (int i = 0; i < getFanList().size(); i++) {
            fans.add(getFanList().get(i).getFan());
        }
        laserToolheadInfo.setFansList(fans);

        return laserToolheadInfo;
    }

    public LaserTube getLaserTube() {
        LaserTube laserTube = new LaserTube();
        laserTube.setCurrentPower(getCurrentPower());
        laserTube.setTargetPower(getTargetPower());
        return laserTube;
    }

    public LaserSafetyStateStructure getLaserSafetyStateStructure() {
        LaserSafetyStateStructure laserSafetyStateStructure = new LaserSafetyStateStructure();
        laserSafetyStateStructure.setKey(key);
        // TODO:
        laserSafetyStateStructure.setState(0);
        laserSafetyStateStructure.setTubeTemperature(mCurrentTemperature);
        laserSafetyStateStructure.setTubeRollAngle(mTubeRollAngle);
        laserSafetyStateStructure.setTubePitchAngle(mTubePitchAngle);
        return laserSafetyStateStructure;
    }

    public int getHeadStatus() {
        return mHeadStatus;
    }

    public void setHeadStatus(int headStatus) {
        this.mHeadStatus = headStatus;
    }

    public float getLaserFocalLength() {
        return mLaserFocalLength;
    }

    public void setLaserFocalLength(float laserFocalLength) {
        this.mLaserFocalLength = laserFocalLength;
    }

    public float getCurrentPower() {
        return mCurrentPower;
    }

    public void setCurrentPower(float currentPower) {
        this.mCurrentPower = currentPower;
    }

    public float getTargetPower() {
        return mTargetPower;
    }

    public void setTargetPower(float targetPower) {
        this.mTargetPower = targetPower;
    }

    public List<MockFan> getFanList() {
        return mFanList;
    }

    public void setFanList(List<MockFan> fanList) {
        this.mFanList = fanList;
    }

    public int getBrightness() {
        return mBrightness;
    }

    public void setBrightness(int brightness) {
        mBrightness = brightness;
    }

    public void setTrecoveryTemperature(int trecoveryTemperature) {
        mTrecoveryTemperature = trecoveryTemperature;
    }

    public void setProtectTemperature(int protectTemperature) {
        mProtectTemperature = protectTemperature;
    }
}
