package fabscreen.features.machinetools.calibration.a400platform.cnc.manualTool.advanced;


import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.calibration.a400platform.cnc.manualTool.A400CncManualToolCompleteFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_CALIBRATION_A400_CNC_MANUAL_TOOL_ADVANCED)
public class A400CncManualToolAdvancedActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            setContentView(R.layout.activity_default);
        }
        gotoCncManualToolAdvanced();
    }


    public void gotoCncManualToolAdvanced() {
        addFragment(R.id.fragment_container, A400CncManualToolAdvancedFragment.newInstance());
    }

    public void gotoCncManualToolComplete() {
        addFragment(R.id.fragment_container, A400CncManualToolCompleteFragment.newInstance());
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
