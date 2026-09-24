package fabscreen.features.settings.a350.experiment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.settings.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.SETTINGS_EXPERIMENT)
public class ExperimentActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_default);

        gotoExperimentList();
    }

    public void gotoExperimentList() {
        ExperimentListFragment fragment = new ExperimentListFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoCustomView() {
        ExperimentFragment fragment = new ExperimentFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoBluetoothDemo() {
        ExperimentBluetoothFragment fragment = new ExperimentBluetoothFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoLaserCalibrationDetect() {
        ExperimentLaserCameraDetectFragment fragment = new ExperimentLaserCameraDetectFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoPreferenceFragment() {
        Fragment fragment = new ExperimentPreferenceFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoCopyTestFragment() {
        ExperimentCopyTestFragment fragment = new ExperimentCopyTestFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoCrashlyticsTestFragment() {
        ExperimentCrashlyticsTestFragment fragment = new ExperimentCrashlyticsTestFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoSocketTestFragment() {
//        SocketTestFragment fragment = new SocketTestFragment();
//        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoSelfInspectionFragment() {
        ExperimentSelfInpectionFragment fragment = new ExperimentSelfInpectionFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoSACPDebugFragment() {
        SACPDebugFragment fragment = SACPDebugFragment.newInstance();
        addFragment(R.id.fragment_container, fragment);
    }
}
