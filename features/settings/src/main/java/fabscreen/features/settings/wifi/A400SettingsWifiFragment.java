package fabscreen.features.settings.wifi;

import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import java.util.List;

import fabscreen.features.settings.R;
import fabscreen.platform.base.lib.network.AccessPoint;
import fabscreen.platform.core.ui.common.wifi.adapter.A400APListAdapter;
import fabscreen.platform.core.ui.common.wifi.adapter.APListAdapter;
import fabscreen.platform.core.ui.view.FabInputDialog;

public class A400SettingsWifiFragment extends SettingsWifiFragment {

    public static Fragment newInstance() {
        return new A400SettingsWifiFragment();
    }

    @Override
    protected APListAdapter getAPListAdapter(List<AccessPoint> list) {
        return new A400APListAdapter(list);
    }

    @Override
    protected void goPassword(AccessPoint ap) {
        String selectedPassword = mViewModel.getSelectedPassword();
        FabInputDialog.create(requireContext())
                .setTitle("Enter password")
                .setEditText(selectedPassword)
                .setButton("Done", (dialog, which) -> {
                    dialog.dismiss();
                    mViewModel.setPassword(FabInputDialog.getsInstance().getEditTextContent());
                    mViewModel.connect();
                    mRvApList.scrollToPosition(0);
                })
                .setOnCancelListener(dialog -> {
                    Logger.d("wifi input dialog on cancel");
                    mViewModel.setSelected(null);
                })
                .show();

    }


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_settings_wifi;
    }
}
