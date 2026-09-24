package fabscreen.features.machinetools.calibration.a400platform.laser.w_1_6.rotary;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_CALIBRATION_A400_LASER_CENTRAL_AXIS_SELECT_MATERIAL)
public class A400CentralAxisCalibrationMaterialSelectActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        Fragment fragment = A400CentralAxisCalibrationMaterialSelectFragment.newInstance();
        addFragment(R.id.fragment_container, fragment);
    }
}
