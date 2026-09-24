package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingZ.sensor;


import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingZ.A400LevelingZCalibrationCompleteFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_CALIBRATION_A400_3DP_LEVELING_Z_SENSOR)
public class A400LevelingZCalibrationSensorActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            setContentView(R.layout.activity_default);
        }
        sensorCalibration();
    }

    public void sensorCalibration() {
        addFragment(R.id.fragment_container, A400LevelingZCalibrationSensor1Fragment.newInstance());
    }

    public void initialHeightCalibration() {
        addFragment(R.id.fragment_container, A400LevelingZCalibrationSensor2Fragment.newInstance());
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
