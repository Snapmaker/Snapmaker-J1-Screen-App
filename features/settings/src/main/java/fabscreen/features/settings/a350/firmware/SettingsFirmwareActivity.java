package fabscreen.features.settings.a350.firmware;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.settings.R;
import fabscreen.features.settings.a350.firmware.update.UpdateFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.SETTINGS_FIRMWARE)
public class SettingsFirmwareActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_default);

        Fragment fragment = SettingsFirmwareFragment.getInstance();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoUpdateFirmware(String filePath, Boolean isLocal) {
        UpdateFragment fragment = new UpdateFragment();

        Bundle bundle = new Bundle();
        bundle.putString("file_path", filePath);
        bundle.putBoolean("is_local", isLocal);
        fragment.setArguments(bundle);

        addFragment(R.id.fragment_container, fragment);
    }
}
