package fabscreen.features.welcome.s20;

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

public class WelcomeTermsFragment extends BaseFragment {
    @BindView(R2.id.cb_welcome_terms_accept)
    CheckBox mCbConfirm;
    @BindView(R2.id.btn_welcome_terms_agree)
    Button mBtnNext;

    public static WelcomeTermsFragment newInstance() {
        return new WelcomeTermsFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBtnNext.setEnabled(false);

        mCbConfirm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mBtnNext.setEnabled(isChecked);
        });
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_welcome_terms;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_welcome_terms_agree)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((WelcomeActivity) getActivity()).startNameFragment();
        }
    }
}
