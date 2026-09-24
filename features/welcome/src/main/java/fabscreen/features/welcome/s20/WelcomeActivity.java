package fabscreen.features.welcome.s20;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.welcome.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseActivity;
import fabscreen.platform.core.ui.common.WelcomeWifiPasswordFragment;

@Route(path = RoutePath.WELCOME_INDEX)
public class WelcomeActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            setContentView(R.layout.activity_default);
            // Hide language entry before copywriting has confirmed.
            if (ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineSetupLanguage()) {
                startHelloFragment();
            } else {
                startLanguageFragment();
            }
        }
    }

    /**
     * Language Page 0, Language
     */
    public void startLanguageFragment() {
        addFragment(R.id.fragment_container, WelcomeLanguageFragment.newInstance());
    }

    /**
     * Welcome page 1, hello
     */
    public void startHelloFragment() {
        addFragment(R.id.fragment_container, WelcomeHelloFragment.newInstance());
    }

    /**
     * Welcome page 2, terms
     * <p>
     * User should agree terms and conditions to use this app.
     */
    public void startTermsFragment() {
        addFragment(R.id.fragment_container, WelcomeTermsFragment.newInstance());
    }

    /**
     * Welcome page 3, name
     */
    public void startNameFragment() {
        addFragment(R.id.fragment_container, WelcomeNameFragment.newInstance());
    }

    /**
     * Welcome page 4, Wi-Fi
     */
    public void startWiFiFragment() {
        addFragment(R.id.fragment_container, WelcomeWifiFragment.newInstance());
    }

    /**
     * Welcome page 4, Wi-Fi password
     */
    public void startPasswordFragment() {
        addFragment(R.id.fragment_container, WelcomeWifiPasswordFragment.newInstance());
    }
}
