package fabscreen.features.settings.a350.index;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.orhanobut.logger.Logger;

import fabscreen.features.settings.R;
import fabscreen.features.settings.a350.advanced._3dp.Settings3DPCalibrationGridFragment;
import fabscreen.features.settings.a350.advanced._3dp.SettingsAdvanced3DPFragment;
import fabscreen.features.settings.a350.advanced.airpurifier.SettingsAdvanceAirPurifierFragment;
import fabscreen.features.settings.a350.advanced.laser.LaserFocusModificationFragment;
import fabscreen.features.settings.a350.advanced.laser.SettingsAdvancedLaserFragment;
import fabscreen.features.settings.a350.advanced.laser.SettingsCameraCalibrationStep1Fragment;
import fabscreen.features.settings.a350.advanced.laser.SettingsCameraCalibrationStep2Fragment;
import fabscreen.features.settings.a350.advanced.laser._10w.Settings10wCameraCalibrationStep1Fragment;
import fabscreen.features.settings.a350.advanced.laser._10w.Settings10wCameraCalibrationStep2Fragment;
import fabscreen.features.settings.a350.advanced.laser._10w.SettingsAdvancedLaser10WFragment;
import fabscreen.features.settings.a350.advanced.laser._10w.SettingsLaser10wToolheadFocusCalibrationFragment;
import fabscreen.features.settings.a350.advanced.laser._10w.SettingsLaser10wTouchPlatformFragment;
import fabscreen.features.settings.a350.wifi.SettingsWifiFragment;
import fabscreen.features.settings.common.SettingsTermsFragment;
import fabscreen.features.settings.language.S30SettingsLanguageFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;
import fabscreen.platform.core.ui.common.WelcomeWifiPasswordFragment;

@Route(path = RoutePath.SETTINGS_INDEX)
public class SettingsActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);

        gotoSettingsHome();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        Logger.d("settings activity changed");
    }

    private void gotoSettingsHome() {
//        if ("com.snapmaker.fabscreen".equals(getApplication().getPackageName())) {
//            SettingsHomeFragment fragment = new SettingsHomeFragment();
//            addFragment(R.id.fragment_container, fragment);
//        } else {
//            addFragment(R.id.fragment_container, WholeSettingsFragment.newInstance());
//        }
    }

    public void gotoWiFiList() {
        addFragment(R.id.fragment_container, SettingsWifiFragment.newInstance());
    }

    /**
     * Start PasswordFragment to obtain password.
     */
    public void startWifiPasswordFragment() {
        addFragment(R.id.fragment_container, WelcomeWifiPasswordFragment.newInstance());
    }

    public void gotoSettingsSecurity() {
        Fragment fragment = SettingsTermsFragment.newInstance();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoSettingsPreference() {
        Fragment fragment = SettingsPreferenceFragment.getInstance();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoSettingsLanguage() {
//        addFragment(R.id.fragment_container, SettingsLanguageFragment.getInstance());
        addFragment(R.id.fragment_container, S30SettingsLanguageFragment.newInstance());
    }

    public void gotoAdvanced3DP() {
        addFragment(R.id.fragment_container, SettingsAdvanced3DPFragment.getInstance());
    }

    public void goto3DPCalibrationGrid() {
        addFragment(R.id.fragment_container, Settings3DPCalibrationGridFragment.getInstance());
    }

    public void gotoAdvancedLaser() {
        addFragment(R.id.fragment_container, SettingsAdvancedLaserFragment.getInstance());
    }

    public void gotoAdvancedLaser10w() {
        addFragment(
                SettingsAdvancedLaser10WFragment.class.getSimpleName(),
                R.id.fragment_container,
                SettingsAdvancedLaser10WFragment.getInstance(),
                true);
    }

    public void gotoLaser10wToolheadFocusCalibration() {
        addFragment(
                SettingsLaser10wToolheadFocusCalibrationFragment.class.getSimpleName(),
                R.id.fragment_container,
                SettingsLaser10wToolheadFocusCalibrationFragment.getInstance(),
                true);
    }

    public void gotoLaser10wTouchPlatformFragment() {
        addFragment(R.id.fragment_container, SettingsLaser10wTouchPlatformFragment.getInstance());
    }

    public void gotoCameraCalibrationStep1() {
        addFragment(R.id.fragment_container, SettingsCameraCalibrationStep1Fragment.newInstance());
    }

    public void gotoCameraCalibrationStep2() {
        Fragment fragment = SettingsCameraCalibrationStep2Fragment.newInstance();
        addFragment(R.id.fragment_container, fragment);
    }

    public void startLaserFocusModificationFragment() {
        addFragment(R.id.fragment_container, LaserFocusModificationFragment.newInstance());
    }

    public void startAdvanceAirPurifierPage() {
        addFragment(R.id.fragment_container, SettingsAdvanceAirPurifierFragment.getInstance());
    }

    public void goto10wCameraCalibrationIntroFragment() {
        addFragment(R.id.fragment_container, Setting10wCameraCalibrationIntroFragment.newInstance());
    }

    public void goto10wCameraCalibrationStep1() {
        addFragment(R.id.fragment_container, Settings10wCameraCalibrationStep1Fragment.newInstance());
    }

    public void goto10wCameraCalibrationStep2() {
        Fragment fragment = Settings10wCameraCalibrationStep2Fragment.newInstance();
        addFragment(R.id.fragment_container, fragment);
    }

    public void popSettingsPage() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        final int fragmentCount = fragmentManager.getFragments().size();

        // Pop all fragments except the root one
        for (int i = 1; i < fragmentCount; i++) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    public void goToNameEditFragment() {
        Fragment fragment = (Fragment) ARouter.getInstance().build(RoutePath.SETTINGS_NAME).navigation();
        addFragment(R.id.fragment_container, fragment);
    }
}
