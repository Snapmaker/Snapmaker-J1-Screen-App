package fabscreen.features.machinetools.calibration.a400platform;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_CALIBRATION_A400_COMPLETE)
public class CalibrationCompleteActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        int type = getIntent().getIntExtra("calibration_type", 0x00);
        addFragment(R.id.fragment_container, CalibrationCompleteFragment.newInstance(type));
    }
}
