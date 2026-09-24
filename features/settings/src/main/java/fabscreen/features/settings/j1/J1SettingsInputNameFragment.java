package fabscreen.features.settings.j1;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fabscreen.features.settings.R;
import fabscreen.platform.core.ui.base.J1InputNameFragment;

public class J1SettingsInputNameFragment extends J1InputNameFragment {
    public static final String REQUEST_NAME_KEY = "requestName";
    public static final String MACHINE_NAME_KEY = "machineName";

    public static Fragment newInstance() {
        return new J1SettingsInputNameFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(R.string.j1_setting_about_machine_name);
    }

    @Override
    protected void onSaveClicked() {
        Bundle result = new Bundle();
        result.putString(MACHINE_NAME_KEY, editName.getText().toString().trim());
        // This fragment is managed by SettingsActivity's FragmentManager.
        getParentFragmentManager().setFragmentResult(REQUEST_NAME_KEY, result);
        back();
    }
}
