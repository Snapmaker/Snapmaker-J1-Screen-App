package fabscreen.features.welcome.j1;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fabscreen.features.welcome.R;
import fabscreen.platform.core.ui.base.J1InputNameFragment;

public class WelcomeJ1NameFragment extends J1InputNameFragment {

    public static Fragment newInstance() {
        return new WelcomeJ1NameFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(getString(R.string.j1_welcome_set_name_title));
    }

    @Override
    protected void onSaveClicked() {
        ((WelcomeJ1Activity) requireActivity()).goToWiFiConfig();
    }
}
