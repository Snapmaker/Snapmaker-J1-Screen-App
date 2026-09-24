package fabscreen.features.machinetools.calibration.a400platform.laser.w_10.cameraCalibration;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_CALIBRATION_A400_LASER_10W_CAMERA_CALIBRATION)
public class A400CameraCalibration10wActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            setContentView(R.layout.activity_default);
        }
        gotoCalibrationLaserInfo();
    }

    public void gotoCalibrationLaserInfo() {
        addFragment(R.id.fragment_container, A400CameraCalibration10wFragment.newInstance());
    }

    public void gotoCalibrationLaser() {
        addFragment(R.id.fragment_container, A400CameraCalibration10w2Fragment.newInstance());
    }
}
