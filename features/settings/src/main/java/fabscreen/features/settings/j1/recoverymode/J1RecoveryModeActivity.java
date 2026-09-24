package fabscreen.features.settings.j1.recoverymode;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.settings.R;
import fabscreen.features.settings.j1.J1SettingsFirmwareUpdateFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.J1_SETTINGS_RECOVERY_MODE)
public class J1RecoveryModeActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        replaceFragment(R.id.fragment_container, J1SettingsFirmwareUpdateFragment.newInstance(false));
    }
}
