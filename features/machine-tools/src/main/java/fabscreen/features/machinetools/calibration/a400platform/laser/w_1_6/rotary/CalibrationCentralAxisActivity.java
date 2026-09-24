package fabscreen.features.machinetools.calibration.a400platform.laser.w_1_6.rotary;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_CALIBRATION_A400_LASER_CENTRAL_AXIS)
public class CalibrationCentralAxisActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        addFragment(R.id.fragment_container, CentralAxisInputDiameterFragment.newInstance());
    }

    public void goToTouchMaterial() {
        replaceFragment(R.id.fragment_container, CentralAxisTouchMaterialFragment.newInstance());
    }

    /*public void goToCalibrationComplete() {
        replaceFragment(R.id.fragment_container,CentralAxisCalibrationCompleteFragment.newInstance());
    }*/
}
