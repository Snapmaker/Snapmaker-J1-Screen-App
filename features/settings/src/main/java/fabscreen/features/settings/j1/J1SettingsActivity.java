package fabscreen.features.settings.j1;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.settings.R;
import fabscreen.features.settings.j1.attendance.J1SettingsHotEndPIDAutoTuneFragment;
import fabscreen.features.settings.j1.attendance.J1SettingsPlatformHeightFineTuneFragment;
import fabscreen.features.settings.j1.attendance.J1SettingsVibrationCompensationFragment;
import fabscreen.features.settings.wifi.J1WifiPasswordFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.SETTINGS_J1_INDEX)
public class J1SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        // language settings will call recreate(), which will save instance state.
        if (savedInstanceState == null) {
            addFragment(R.id.fragment_container, J1SettingsContainerFragment.newInstance(getIntent().getIntExtra("destination", -1)));
        }
    }

    public void goToEnterPassword(String ssid) {
        Fragment fragment = J1WifiPasswordFragment.newInstance(ssid);
        addFragment(R.id.fragment_container, fragment);
    }

    public void goToNameInput() {
        addFragment(R.id.fragment_container, J1SettingsInputNameFragment.newInstance());
    }

    public void setDeveloperState(boolean isDeveloper) {
        mFloatWindow.hideView(isDeveloper);
    }

    public void goToVibrationSettings() {
        addFragment(R.id.fragment_container, J1SettingsVibrationCompensationFragment.newInstance());
    }

    public void goToPlatformHeightFineTuneSettings() {
        addFragment(R.id.fragment_container, J1SettingsPlatformHeightFineTuneFragment.newInstance());
    }

    public void goToHotEndPIDAutoTune() {
        addFragment(R.id.fragment_container, J1SettingsHotEndPIDAutoTuneFragment.newInstance());
    }
}
