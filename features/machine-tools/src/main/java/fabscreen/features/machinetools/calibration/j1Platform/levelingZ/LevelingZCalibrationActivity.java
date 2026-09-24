package fabscreen.features.machinetools.calibration.j1Platform.levelingZ;


import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.calibration.j1Platform.CalibrationSuccessfullyFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_CALIBRATION_J1_3DP_LEVELING_Z)
public class LevelingZCalibrationActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            setContentView(R.layout.activity_default);
        }

        initRootFragment();
    }

    public void initRootFragment() {
        addFragment(R.id.fragment_container, LevelingZCalibrationInfoFragment.newInstance());
    }

    public void gotoLevelingZCalibrationLInstructions() {
        addFragment(R.id.fragment_container, LevelingZCalibrationLInstructionsFragment.newInstance());
    }

    public void gotoLevelingZCalibrationL() {
        addFragment(R.id.fragment_container, LevelingZCalibrationLFragment.newInstance());
    }

    public void gotoLevelingZCalibrationRInstructions() {
        addFragment(R.id.fragment_container, LevelingZCalibrationRInstructionsFragment.newInstance());
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
