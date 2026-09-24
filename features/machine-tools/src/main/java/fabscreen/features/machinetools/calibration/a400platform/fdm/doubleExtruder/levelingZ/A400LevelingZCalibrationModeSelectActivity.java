package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingZ;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_CALIBRATION_A400_3DP_LEVELING_Z_CHECK_MODE)
public class A400LevelingZCalibrationModeSelectActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        Fragment fragment = A400CalibrationLevelingZSelectionFragment.newInstance();
        addFragment(R.id.fragment_container, fragment);
    }
}
