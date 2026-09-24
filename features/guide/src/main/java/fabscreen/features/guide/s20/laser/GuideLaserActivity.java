package fabscreen.features.guide.s20.laser;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.guide.R;
import fabscreen.features.guide.s20.laser.autofocus.GuideLaserAutoFocusPickFragment;
import fabscreen.features.guide.s20.laser.autofocus.GuideLaserAutoFocusStep1Fragment;
import fabscreen.features.guide.s20.laser.autofocus.GuideLaserAutoFocusStep2Fragment;
import fabscreen.features.guide.s20.laser.camearcalibration.GuideLaserCameraCalibrationIntroFragment;
import fabscreen.features.guide.s20.laser.camearcalibration.GuideLaserCameraCalibrationStep1Fragment;
import fabscreen.features.guide.s20.laser.camearcalibration.GuideLaserCameraCalibrationStep2Fragment;
import fabscreen.features.guide.s20.laser.complete.GuideLaserCompleteFragment;
import fabscreen.features.guide.s20.laser.getstarted.GuideLaserGetStartedFragment;
import fabscreen.features.guide.s20.laser.measureheight.GuideLaserMeasureHeightFragment;
import fabscreen.features.guide.s20.laser.measureheight.GuideLaserMeasureHeightIntroFragment;
import fabscreen.features.guide.s20.laser.preparematerial.GuideLaserPrepareMaterialFragment;
import fabscreen.features.guide.s20.laser.preparematerial.GuideLaserPrepareMaterialIntroFragment;
import fabscreen.features.guide.s20.laser.safety.GuideLaserSafetyGogglesFragment;
import fabscreen.features.guide.s20.laser.setorigin.GuideLaserSetOriginFragment;
import fabscreen.features.guide.s20.laser.setorigin.GuideLaserSetOriginIntroFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.GUIDE_LASER)
public class GuideLaserActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            setContentView(R.layout.activity_default);

            startGetStartedFragment();
        }
    }

    public void startGetStartedFragment() {
        addFragment(R.id.fragment_container, GuideLaserGetStartedFragment.newInstance());
    }

    /**
     * Prepare Material
     */
    public void startPrepareMaterialIntroFragment() {
        addFragment(R.id.fragment_container, GuideLaserPrepareMaterialIntroFragment.newInstance());
    }

    public void startPrepareMaterialFragment() {
        addFragment(R.id.fragment_container, GuideLaserPrepareMaterialFragment.newInstance());
    }

    /**
     * Measure Height
     * <p>
     * onNext -> Move the head to center of plat and proper height.
     */
    public void startMeasureHeightIntroFragment() {
        addFragment(R.id.fragment_container, GuideLaserMeasureHeightIntroFragment.newInstance());
    }

    public void startMeasureHeightFragment() {
        addFragment(R.id.fragment_container, GuideLaserMeasureHeightFragment.newInstance());
    }

    /**
     * Safety Goggles
     */
    public void startSafetyGogglesFragment() {
        addFragment(R.id.fragment_container, GuideLaserSafetyGogglesFragment.newInstance());
    }

    /**
     * Work Origin
     */
    public void startSetOriginIntroFragment() {
        addFragment(R.id.fragment_container, GuideLaserSetOriginIntroFragment.newInstance());
    }

    public void startSetOriginFragment() {
        addFragment(R.id.fragment_container, GuideLaserSetOriginFragment.newInstance());
    }

    /**
     * Auto Focus
     */
    public void startAutoFocusStep1Fragment() {
        addFragment(R.id.fragment_container, GuideLaserAutoFocusStep1Fragment.newInstance());
    }

    public void startAutoFocusStep2Fragment() {
        addFragment(R.id.fragment_container, GuideLaserAutoFocusStep2Fragment.newInstance());
    }

    public void startAutoFocusPickFragment() {
        addFragment(R.id.fragment_container, GuideLaserAutoFocusPickFragment.newInstance());
    }

    /**
     * Camera Calibration
     */
    public void startCameraCalibrationIntroFragment() {
        addFragment(R.id.fragment_container, GuideLaserCameraCalibrationIntroFragment.newInstance());
    }

    public void startCameraCalibrationStep1Fragment() {
        addFragment(R.id.fragment_container, GuideLaserCameraCalibrationStep1Fragment.newInstance());
    }

    public void startCameraCalibrationStep2Fragment() {
        addFragment(R.id.fragment_container, GuideLaserCameraCalibrationStep2Fragment.newInstance());
    }

    /**
     * Complete
     */
    public void startCompleteFragment() {
        addFragment(R.id.fragment_container, GuideLaserCompleteFragment.newInstance());
    }
}
