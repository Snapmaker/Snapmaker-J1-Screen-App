package fabscreen.features.welcome.j1;

import androidx.fragment.app.Fragment;

import butterknife.OnClick;
import fabscreen.features.welcome.R;
import fabscreen.features.welcome.R2;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;

public class WelcomeJ1CompleteFragment extends BaseFragment {
    public static Fragment newInstance() {
        return new WelcomeJ1CompleteFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_welcome_j1_complete;
    }

    @OnClick(R2.id.btn_next)
    void onStartClicked() {
        playNormalClickSound();
        ((WelcomeJ1Activity) requireActivity()).goToGuide();
        getServiceContainer().getService(IPreferences.class).getHelper().setMachineSetupFlag(true);
    }

    @OnClick(R2.id.top_bar_back)
    void onClickBack() {
        playNormalClickSound();
        back();
    }

}
