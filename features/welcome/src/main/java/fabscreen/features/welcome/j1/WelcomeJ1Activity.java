package fabscreen.features.welcome.j1;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import com.alibaba.android.arouter.facade.annotation.Route;

import butterknife.BindView;
import fabscreen.features.welcome.R;
import fabscreen.features.welcome.R2;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.WELCOME_J1)
public class WelcomeJ1Activity extends BaseActivity {
    @BindView(R2.id.fcv_welcome)
    FragmentContainerView mFragmentContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_j1_welcome);
        goToLanguageChoosing();

        if (ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getNeedQueryMachineError()) {
            ServiceContainer.getInstance().getService(IMachine.class).getErrorController().queryException().as(bindToLifecycle()).subscribe();
            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setNeedQueryMachineError(false);
        }
    }

    public void goToLanguageChoosing() {
        addFragment(R.id.fcv_welcome, WelcomeJ1LanguageFragment.newInstance());
    }

    public void goToTerms() {
        addFragment(R.id.fcv_welcome, WelcomeJ1TermsFragment.newInstance());
    }

    public void goToMachineNaming() {
        addFragment(R.id.fcv_welcome, WelcomeJ1NameFragment.newInstance());
    }

    public void goToWiFiConfig() {
        addFragment(R.id.fcv_welcome, WelcomeJ1WifiFragment.newInstance());
    }

    public void goToComplete() {
        addFragment(R.id.fcv_welcome, WelcomeJ1CompleteFragment.newInstance());
    }

    public void goToGuide() {
        boolean showGuideClose = getIntent().getBooleanExtra("showGuideClose", true);
        finish();
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setGuideTemperatureSelfCheck(false);
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setGuideCalibration(false);
        mRouter.routeToGuide3DP(showGuideClose).start(this);
    }

    public void goToEnterPassword(String ssid) {
        Fragment fragment = WelcomeJ1PasswordFragment.newInstance(ssid);
        addFragment(R.id.fcv_welcome, fragment);
    }
}
