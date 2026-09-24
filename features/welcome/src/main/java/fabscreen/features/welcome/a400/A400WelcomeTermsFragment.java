package fabscreen.features.welcome.a400;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.welcome.R;
import fabscreen.features.welcome.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class A400WelcomeTermsFragment extends BaseFragment {

    public static A400WelcomeTermsFragment newInstance() {
        return new A400WelcomeTermsFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_welcome_terms;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_welcome_terms_agree)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((A400WelcomeActivity) getActivity()).startLanguageFragment();
        }
    }
}
