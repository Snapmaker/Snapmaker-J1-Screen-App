package fabscreen.features.machinetools.calibration.s20._30._cnc.setorigin;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_CALIBRATION_A400_CNC_SET_ORIGIN)
public class CncSetOriginActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            setContentView(R.layout.activity_default);
        }

        initRootFragment();
    }

    public void initRootFragment() {
        addFragment(R.id.fragment_container, CncSetOriginFragment.newInstance());
    }

    public void gotToTCncSetOriginSuccess() {
        addFragment(R.id.fragment_container, CncSetOriginSuccessFragment.newInstance());
    }


}
