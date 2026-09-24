package fabscreen.features.machinetools.calibration.j1Platform;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import fabscreen.platform.base.RoutePath;

public class J1CalibrationMode {
    private J1CalibrationModeIndex mCalibrationModeIndex;
    private int mCalibrationModeImageId;
    private int mCalibrationModeContentId;
    private String mCalibrationModePath;
    private boolean mIsAuxiliary;

    public J1CalibrationMode(J1CalibrationModeIndex calibrationModeIndex,
                             @DrawableRes int calibrationModeImageId,
                             @StringRes int calibrationModeContentId,
                             @RoutePath.Path String calibrationModePath,
                             boolean isAuxiliary) {
        mCalibrationModeIndex = calibrationModeIndex;
        mCalibrationModeImageId = calibrationModeImageId;
        mCalibrationModeContentId = calibrationModeContentId;
        mCalibrationModePath = calibrationModePath;
        mIsAuxiliary = isAuxiliary;
    }

    public J1CalibrationModeIndex getCalibrationModeIndex() {
        return mCalibrationModeIndex;
    }

    public int getCalibrationModeImageId() {
        return mCalibrationModeImageId;
    }

    public int getCalibrationModeContentId() {
        return mCalibrationModeContentId;
    }

    public String getCalibrationModePath() {
        return mCalibrationModePath;
    }

    public boolean isAuxiliary() {
        return mIsAuxiliary;
    }

    enum J1CalibrationModeIndex {
        HEATED_BED_LEVELING,
        Z_OFFSET_CALIBRATION,
        XY_OFFSET_CALIBRATION,
        CALIBRATION_CHECK,
        CALIBRATION_VIBRATION
    }
}
