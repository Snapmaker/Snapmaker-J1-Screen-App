package fabscreen.features.welcome.j1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.welcome.R;
import fabscreen.features.welcome.R2;
import fabscreen.platform.base.view.BaseFragment;

public class WelcomeJ1TermsFragment extends BaseFragment {
    public static Fragment newInstance() {
        return new WelcomeJ1TermsFragment();
    }

    @BindView(R2.id.view_welcome_j1_terms_pop_up)
    View mViewPopUp;
    @BindView(R2.id.tv_welcome_j1_terms_title)
    TextView mTvTitle;

    @BindView(R2.id.btn_next)
    Button mBtnNext;
    @BindView(R2.id.cb_welcome_terms_accept)
    CheckBox mCbConfirm;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_welcome_j1_terms;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBtnNext.setEnabled(false);
        mViewPopUp.setVisibility(View.GONE);
        mTvTitle.setText(R.string.all_terms_and_conditions);
        mCbConfirm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mBtnNext.setEnabled(isChecked);
        });
    }

    @OnClick(R2.id.btn_next)
    void onStartClicked() {
        playNormalClickSound();
        mViewPopUp.setVisibility(View.VISIBLE);
    }


    @OnClick(R2.id.tv_welcome_j1_wifi_skip)
    void onClickSkip() {
        playNormalClickSound();
        ((WelcomeJ1Activity) requireActivity()).goToMachineNaming();
        mViewPopUp.setVisibility(View.GONE);
    }

    @OnClick(R2.id.tv_welcome_j1_confirm)
    void onClickConfirm() {
        playNormalClickSound();
        ((WelcomeJ1Activity) requireActivity()).goToMachineNaming();
        mViewPopUp.setVisibility(View.GONE);
    }

    @OnClick(R2.id.top_bar_back)
    void onClickBack() {
        playNormalClickSound();
        back();
    }


}
