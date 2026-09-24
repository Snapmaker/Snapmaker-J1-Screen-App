package fabscreen.features.settings.a400.recoverymode;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.settings.R;
import fabscreen.features.settings.a400.update.A400SettingsFirmwareUpdateFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.A400_SETTINGS_RECOVERY_MODE)
public class A400RecoveryModeActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        replaceFragment(R.id.fragment_container, A400SettingsFirmwareUpdateFragment.newInstance());
    }
}
