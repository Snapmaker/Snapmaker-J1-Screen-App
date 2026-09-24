package fabscreen.features.machinetools.calibration.a400platform.cnc.toolchange;


import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_CALIBRATION_A400_CNC_CHANGE_ASSISTANT)
public class A400CncToolChangeAssistantActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            setContentView(R.layout.activity_default);
        }
        gotoCncToolChange();
    }

    public void gotoCncToolChange() {
        addFragment(R.id.fragment_container, A400CncToolChangeFragment.newInstance());
    }

    public void gotoCncReplacement() {
        addFragment(R.id.fragment_container, A400CncToolReplacementFragment.newInstance());
    }

    public void gotoSetZ() {
        addFragment(R.id.fragment_container, A400CncToolChange2Fragment.newInstance());
    }

    public void gotoComplete() {
        addFragment(R.id.fragment_container, A400CncToolChangeCompleteFragment.newInstance());
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
