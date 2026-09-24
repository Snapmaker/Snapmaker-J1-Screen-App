package fabscreen.features.print.settings;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.print.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.PRINT_PREVIEW_SETTINGS)
public class PrintSettingsActivity extends BaseActivity {
    private static final String TAG = PrintSettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_default);

        int headType = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId();

        switch (headType) {
            case Module.ModuleType.HEAD_3DP:
                goto3DPPrintSettings();
                break;
            case Module.ModuleType.HEAD_LASER:
            case Module.ModuleType.HEAD_LASER_10W:
                gotoLaserPrintSettings();
                break;
            case Module.ModuleType.HEAD_CNC:
                gotoCNCPrintSettings();
                break;
        }
    }

    /**
     * 3DP Settings
     */
    public void goto3DPPrintSettings() {
        Fragment fragment = new PrintSettings3DPFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoLaserPrintSettings() {
        Fragment fragment = new PrintSettingsLaserFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoCNCPrintSettings() {
        Fragment fragment = new PrintSettingsCNCFragment();
        addFragment(R.id.fragment_container, fragment);
    }
}
