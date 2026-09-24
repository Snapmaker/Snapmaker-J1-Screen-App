package fabscreen.platform.core.ui.data;

public class FilamentState {
    private boolean mIsFailureState;
    private boolean mLeftFailureState;
    private boolean mRightFailureState;
    private boolean mLeftNowFilamentState;
    private boolean mRightNowFilamentState;
    private boolean mLeftFilamentStateChange;
    private boolean mRightFilamentStateChange;

    private float mLeftExtruderTargetTemp;
    private float mRightExtruderTargetTemp;
    private boolean isLeftTemperatureReached;
    private boolean isRightTemperatureReached;

    private int FailureFilamentIndex;
    private int mExtruderNum;

    public int getFailureFilamentIndex() {
        return FailureFilamentIndex;
    }

    public int getExtruderNum() {
        return mExtruderNum;
    }


    public FilamentState setExtruderNum(int extruderNum) {
        mExtruderNum = extruderNum;
        return this;
    }

    public FilamentState setIsFailureState(boolean isFailureState) {
        mIsFailureState = isFailureState;
        if (mExtruderNum == 1) {
            if (mLeftFilamentStateChange) {
                mLeftFailureState = mLeftNowFilamentState;
                FailureFilamentIndex = 0;
                mLeftFilamentStateChange = false;
            } else if (mRightFilamentStateChange) {
                mRightFailureState = mRightNowFilamentState;
                FailureFilamentIndex = 1;
                mRightFilamentStateChange = false;
            }
        } else if (mExtruderNum == 2) {
            mLeftFailureState = mLeftNowFilamentState;
            mRightFailureState = mRightNowFilamentState;
            if (mLeftFilamentStateChange) {
                FailureFilamentIndex = 0;
                mLeftFilamentStateChange = false;
            } else if (mRightFilamentStateChange) {
                FailureFilamentIndex = 1;
                mRightFilamentStateChange = false;
            }
        }
        return this;
    }


    public FilamentState setFilamentState(int index, boolean extruderFilamentStatus, float extruderTargetTemp, boolean isTemperatureReached) {
        if (index == 0) {
            if (mLeftNowFilamentState != extruderFilamentStatus) {
                mLeftFilamentStateChange = true;
            }
            mLeftNowFilamentState = extruderFilamentStatus;
            if (!mIsFailureState) {
                mLeftExtruderTargetTemp = extruderTargetTemp;
            }
            isLeftTemperatureReached = isTemperatureReached;
        } else if (index == 1) {
            if (mRightNowFilamentState != extruderFilamentStatus) {
                mRightFilamentStateChange = true;
            }
            mRightNowFilamentState = extruderFilamentStatus;
            if (!mIsFailureState) {
                mRightExtruderTargetTemp = extruderTargetTemp;
            }
            isRightTemperatureReached = isTemperatureReached;
        }
        return this;
    }

    public float getTarget() {
        return FailureFilamentIndex == 0 ? mLeftExtruderTargetTemp : mRightExtruderTargetTemp;
    }

    public float getLeftTarget() {
        return mLeftExtruderTargetTemp;
    }

    public float getRightTarget() {
        return mRightExtruderTargetTemp;
    }

    public boolean isTemperatureReached() {
        return FailureFilamentIndex == 0 ? isLeftTemperatureReached : isRightTemperatureReached;
    }

    public boolean getNowFilamentState() {
        return FailureFilamentIndex == 0 ? mLeftNowFilamentState : mRightNowFilamentState;
    }
}
