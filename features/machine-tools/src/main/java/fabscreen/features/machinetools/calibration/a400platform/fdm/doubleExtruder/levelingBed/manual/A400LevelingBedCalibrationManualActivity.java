package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingBed.manual;


import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingBed.A400LevelingBedCalibrationCompleteFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_CALIBRATION_A400_3DP_LEVELING_BED_MANUAL)
public class A400LevelingBedCalibrationManualActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            setContentView(R.layout.activity_default);
        }
        gotoLevelingBedCalibrationManual();
    }

    public void gotoLevelingBedCalibrationManual() {
        addFragment(R.id.fragment_container, A400LevelingBedCalibrationManualFragment.newInstance());
    }

    public void gotoLevelingBedCalibrationComplete() {
        addFragment(R.id.fragment_container, A400LevelingBedCalibrationCompleteFragment.newInstance());
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
