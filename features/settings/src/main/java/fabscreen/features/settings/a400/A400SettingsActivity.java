package fabscreen.features.settings.a400;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.settings.R;
import fabscreen.features.settings.a400.maintenance.configparams.MaintainConfigParamsFragment;
import fabscreen.features.settings.a400.maintenance.machineinfo.A400MachineInfoFragment;
import fabscreen.features.settings.wifi.J1WifiPasswordFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.SETTINGS_A400_INDEX)
public class A400SettingsActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        if (savedInstanceState == null) {
            addFragment(R.id.fragment_container, A400SettingsContainerFragment.newInstance(getIntent().getIntExtra("destination", -1)));
        }
    }

    public void goToEnterPassword(String ssid) {
        Fragment fragment = J1WifiPasswordFragment.newInstance(ssid);
        addFragment(R.id.fragment_container, fragment);
    }

    public void goToMaintainConfigParams() {
        addFragment(R.id.fragment_container, MaintainConfigParamsFragment.newInstance());
    }

    public void goToLongTextDisplay(int titleRes, int contentRes) {
        addFragment(R.id.fragment_container, A400LongTextDisplayFragment.newInstance(titleRes, contentRes));
    }

    public void goToMachineInfo() {
        addFragment(R.id.fragment_container, A400MachineInfoFragment.newInstance());
    }

    public void setDeveloperState(boolean isDeveloper) {
        mFloatWindow.hideView(isDeveloper);
    }
}
