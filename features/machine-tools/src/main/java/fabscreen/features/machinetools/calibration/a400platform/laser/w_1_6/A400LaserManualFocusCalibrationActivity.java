package fabscreen.features.machinetools.calibration.a400platform.laser.w_1_6;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_CALIBRATION_A400_LASER_FOCUS_CALIBRATION)
public class A400LaserManualFocusCalibrationActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        addFragment(R.id.fragment_container, CalibrationTouchMaterialFragment.newInstance());
    }

    public void goToSetOrigin() {
        replaceFragment(R.id.fragment_container, CalibrationSetXYOriginFragment.newInstance());
    }

    public void goToEngravingWork() {
        replaceFragment(R.id.fragment_container, CalibrationEngravingFragment.newInstance());
    }

    public void goToChooseLine() {
        replaceFragment(R.id.fragment_container, CalibrationChooseLineFragment.newInstance());
    }

    public void goToCalibrationComplete() {
        replaceFragment(R.id.fragment_container, ManualFocusCalibrationCompleteFragment.newInstance());
    }
}
