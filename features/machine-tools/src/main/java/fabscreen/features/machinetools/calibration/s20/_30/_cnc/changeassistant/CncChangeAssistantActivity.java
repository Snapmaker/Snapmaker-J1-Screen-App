package fabscreen.features.machinetools.calibration.s20._30._cnc.changeassistant;

import android.os.Bundle;

import androidx.annotation.Nullable;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.view.BaseActivity;

//@Route(path = RoutePath.TOOLS_CALIBRATION_A400_CNC_CHANGE_ASSISTANT)
public class CncChangeAssistantActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            setContentView(R.layout.activity_default);
        }

        initRootFragment();
    }

    public void initRootFragment() {
        addFragment(R.id.fragment_container, CncChangeAssistant11Fragment.newInstance());
    }

    public void gotToCncChangeAssistant2() {
        addFragment(R.id.fragment_container, CncChangeAssistant21Fragment.newInstance());
    }

    public void gotToCncChangeAssistant3() {
        addFragment(R.id.fragment_container, CncChangeAssistant31Fragment.newInstance());
    }

    public void gotToCncChangeAssistant4() {
        addFragment(R.id.fragment_container, CncChangeAssistantCompleteFragment.newInstance());
    }

}
