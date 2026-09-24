package fabscreen.features.machinetools.calibration.s20.laser._10w.camera;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_CALIBRATION_S20_10W_CAMERA_CALIBRATION)
public class Laser10wCameraCalibrationActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_default);

        goto10wCameraCalibrationIntroFragment();
    }

    public void goto10wCameraCalibrationIntroFragment() {
        addFragment(R.id.fragment_container, Laser10wLaserCameraCalibrationIntroFragment.newInstance());
    }

    public void gotoCameraCalibrationStep1() {
        addFragment(R.id.fragment_container, Laser10wCameraCalibrationStep1Fragment.newInstance());
    }

    public void gotoCameraCalibrationStep2() {
        addFragment(R.id.fragment_container, Laser10wCameraCalibrationStep2Fragment.newInstance());
    }

}
