package fabscreen.features.machinetools.calibration.j1Platform.LevelingXY;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_CALIBRATION_J1_3DP_LEVELING_XY)
public class LevelingXYCalibrationActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        initRootFragment();
    }

    public void initRootFragment() {
        addFragment(R.id.fragment_container, LevelingXYCalibrationInfoFragment.newInstance());
    }

    public void gotoLevelingXYPrintCalibration() {
        addFragment(R.id.fragment_container, LevelingXYCalibrationPrintFragment.newInstance());
    }

    public void gotoLevelingXYCalibration2() {
        addFragment(R.id.fragment_container, LevelingXYCalibration2Fragment.newInstance());
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
