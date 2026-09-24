package fabscreen.features.settings.wifi;

import androidx.fragment.app.Fragment;

import java.util.List;

import fabscreen.features.settings.R;
import fabscreen.features.settings.j1.J1SettingsActivity;
import fabscreen.platform.base.lib.network.AccessPoint;
import fabscreen.platform.core.ui.common.wifi.adapter.APListAdapter;
import fabscreen.platform.core.ui.common.wifi.adapter.J1APListAdapter;

public class J1SettingsWifiFragment extends SettingsWifiFragment {
    public static Fragment newInstance() {
        return new J1SettingsWifiFragment();
    }

    @Override
    protected APListAdapter getAPListAdapter(List<AccessPoint> list) {
        return new J1APListAdapter(list);
    }

    @Override
    protected void goPassword(AccessPoint ap) {
        if (requireActivity() instanceof J1SettingsActivity) {
            ((J1SettingsActivity) requireActivity()).goToEnterPassword(ap.getSSID());
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_settings_wifi;
    }
}
