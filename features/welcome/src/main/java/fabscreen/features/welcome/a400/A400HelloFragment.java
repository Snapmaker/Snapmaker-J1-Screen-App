package fabscreen.features.welcome.a400;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;


import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.welcome.R;
import fabscreen.features.welcome.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class A400HelloFragment extends BaseFragment {

    @BindView(R2.id.cb_hello)
    CheckBox mCb;
    @BindView(R2.id.btn_welcome_hello_start)
    Button mBtnStart;
    @BindView(R2.id.cl_hello)
    ConstraintLayout mClHello;
    @BindView(R2.id.cl_terms_and_conditions)
    ConstraintLayout mClTermsAndConditions;

    public static A400HelloFragment newInstance() {
        return new A400HelloFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        mBtnStart.setEnabled(false);
        mCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                playNormalClickSound();
                mBtnStart.setEnabled(isChecked);
            }
        });
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_hello;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_welcome_hello_start)
    void onClickStart() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((A400WelcomeActivity) getActivity()).startNameFragment();
        }
    }

    @OnClick(R2.id.textView4)
    void onClickTermsAndConditions() {
        playNormalClickSound();
        mClHello.setVisibility(View.GONE);
        mClTermsAndConditions.setVisibility(View.VISIBLE);

    }

    @OnClick(R2.id.btn_terms_and_conditions_agree)
    void onClickAgree() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((A400WelcomeActivity) getActivity()).startNameFragment();
        }
    }

    @OnClick(R2.id.iv_terms_and_conditions_close)
    void onClickClose() {
        playNormalClickSound();
        mClHello.setVisibility(View.VISIBLE);
        mClTermsAndConditions.setVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        IjkMediaPlayer.native_profileEnd();
    }
}

