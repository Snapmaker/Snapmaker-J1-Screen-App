package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingZ.auto;


import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingZ.A400LevelingZCalibrationCompleteFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_CALIBRATION_A400_3DP_LEVELING_Z_AUTOMATIC)
public class A400LevelingZCalibrationAutoActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            setContentView(R.layout.activity_default);
        }

        initRootFragment();
    }

    public void initRootFragment() {
        addFragment(R.id.fragment_container, A400LevelingZCalibrationAutoFragment.newInstance());
    }

    public void gotoLevelingZCalibrationComplete() {
        addFragment(R.id.fragment_container, A400LevelingZCalibrationCompleteFragment.newInstance());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
