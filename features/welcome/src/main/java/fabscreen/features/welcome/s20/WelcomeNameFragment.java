package fabscreen.features.welcome.s20;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.R;
import fabscreen.platform.core.R2;
import fabscreen.platform.core.ui.common.WelcomeNameViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class WelcomeNameFragment extends BaseFragment {
    @BindView(R2.id.et_welcome_name_input)
    EditText mEtMachineName;
    @BindView(R2.id.tv_welcome_name_tip)
    TextView mTvMachineNameTip;
    @BindView(R2.id.btn_welcome_name_next)
    Button mBtnNext;
    private WelcomeNameViewModel mViewModel;

    public static WelcomeNameFragment newInstance() {
        return new WelcomeNameFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = getViewModel();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_welcome_name;
    }

    @Override
    protected WelcomeNameViewModel getViewModel() {
        return getViewModelProvider().get(WelcomeNameViewModel.class);
    }

    private void initView() {
        mEtMachineName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mViewModel.updateName(s.toString());
            }
        });

        mViewModel.getNameObservable()
                .take(1)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(name -> mEtMachineName.setText(name));

        mViewModel.getNameTipObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(tip -> {
                    switch (tip) {
                        case WelcomeNameViewModel.TIP_OK:
                            mTvMachineNameTip.setText("");
                            mBtnNext.setEnabled(true);
                            break;
                        case WelcomeNameViewModel.TIP_EMPTY:
                            mTvMachineNameTip.setText(R.string.welcome_name_tip_empty);
                            mBtnNext.setEnabled(false);
                            break;
                    }
                });
    }

    @OnClick(R2.id.btn_welcome_name_next)
    void onClickSave() {
        playNormalClickSound();
        mViewModel.saveName();

        if (getActivity() != null) {
            ((WelcomeActivity) getActivity()).startWiFiFragment();
        }
    }
}
