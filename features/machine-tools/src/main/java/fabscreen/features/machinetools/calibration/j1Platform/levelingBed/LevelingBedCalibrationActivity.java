package fabscreen.features.machinetools.calibration.j1Platform.levelingBed;


import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.calibration.j1Platform.CalibrationSuccessfullyFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_CALIBRATION_J1_3DP_LEVELING_BED)
public class LevelingBedCalibrationActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            setContentView(R.layout.activity_default);
        }

        initRootFragment();
    }

    public void initRootFragment() {
        addFragment(R.id.fragment_container, LevelingBedCalibrationInfoFragment.newInstance());
    }

    public void gotoLevelingBedCalibration1Instructions() {
        addFragment(R.id.fragment_container, LevelingBedCalibration1InstructionsFragment.newInstance());
    }

    public void gotoLevelingBedCalibration1() {
        addFragment(R.id.fragment_container, LevelingBedCalibration1Fragment.newInstance());
    }

    public void gotoLevelingBedCalibration2() {
        addFragment(R.id.fragment_container, LevelingBedCalibration2Fragment.newInstance());
    }

    public void gotoCalibrationSuccess() {
        addFragment(R.id.fragment_container, CalibrationSuccessfullyFragment.newInstance());
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
