package fabscreen.features.guide.s20.laser._10w;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.guide.R;
import fabscreen.features.guide.s20.laser._10w.cameralibration.Guide10wLaserCameraCalibrationIntroFragment;
import fabscreen.features.guide.s20.laser._10w.cameralibration.Guide10wLaserCameraCalibrationStep1Fragment;
import fabscreen.features.guide.s20.laser._10w.cameralibration.Guide10wLaserCameraCalibrationStep2Fragment;
import fabscreen.features.guide.s20.laser._10w.complete.Guide10wLaserCompleteFragment;
import fabscreen.features.guide.s20.laser._10w.getstarted.Guide10wLaserGetStartedFragment;
import fabscreen.features.guide.s20.laser._10w.thicknessmeasurementcalibration.Guide10wThicknessMeasureCalibrationMeasureFragment;
import fabscreen.features.guide.s20.laser._10w.thicknessmeasurementcalibration.Guide10wThicknessMeasureCalibrationPointsFragment;
import fabscreen.features.guide.s20.laser._10w.thicknessmeasurementcalibration.Guide10wThicknessMeasurementCalibrationIntroFragment;
import fabscreen.features.guide.s20.laser._10w.touchplatform.Guide10wLaserTouchPlatformFragment;
import fabscreen.features.guide.s20.laser._10w.touchplatform.Guide10wLaserTouchPlatformIntroFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.GUIDE_10W_LASER)
public class Guide10wLaserActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            setContentView(R.layout.activity_default);

            startGetStartedFragment();
        }
    }

    public void startGetStartedFragment() {
        addFragment(R.id.fragment_container, Guide10wLaserGetStartedFragment.newInstance());
    }

    /**
     * Touch Platform
     */
    public void startTouchPlatformFragmentIntroFragment() {
        addFragment(
                Guide10wLaserTouchPlatformIntroFragment.class.getSimpleName(),
                R.id.fragment_container,
                Guide10wLaserTouchPlatformIntroFragment.newInstance(),true);
    }

    public void startTouchPlatformFragment() {
        addFragment(R.id.fragment_container, Guide10wLaserTouchPlatformFragment.newInstance());
    }

    /**
     * Thickness Measurement Calibration
     */
    public void startThicknessMeasurementCalibrationIntroFragment() {
        addFragment(Guide10wThicknessMeasurementCalibrationIntroFragment.class.getSimpleName(),
                R.id.fragment_container,
                Guide10wThicknessMeasurementCalibrationIntroFragment.newInstance(), true);
    }

    public void Guide10wThicknessMeasureCalibrationPointsFragment() {
        addFragment(R.id.fragment_container, Guide10wThicknessMeasureCalibrationPointsFragment.newInstance());
    }

    public void Guide10wThicknessMeasureCalibrationMeasureFragment() {
        addFragment(R.id.fragment_container, Guide10wThicknessMeasureCalibrationMeasureFragment.newInstance());
    }

    /**
     * Camera Calibration
     */
    public void start10wCameraCalibrationIntroFragment() {
        addFragment(R.id.fragment_container, Guide10wLaserCameraCalibrationIntroFragment.newInstance());
    }

    public void start10wCameraCalibrationStep1Fragment() {
        addFragment(R.id.fragment_container, Guide10wLaserCameraCalibrationStep1Fragment.newInstance());
    }

    public void start10wCameraCalibrationStep2Fragment() {
        addFragment(R.id.fragment_container, Guide10wLaserCameraCalibrationStep2Fragment.newInstance());
    }

    /**
     * Complete
     */
    public void startCompleteFragment() {
        addFragment(R.id.fragment_container, Guide10wLaserCompleteFragment.newInstance());
    }

}
