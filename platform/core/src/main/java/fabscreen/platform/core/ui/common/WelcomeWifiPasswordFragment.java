package fabscreen.platform.core.ui.common;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.platform.base.lib.network.AccessPoint;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.R;
import fabscreen.platform.core.R2;
import fabscreen.platform.core.ui.viewmodel.WifiConnectionViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class WelcomeWifiPasswordFragment extends BaseFragment {
    @BindView(R2.id.tv_welcome_wifi_password_subtitle)
    TextView mTvSubtitle;
    @BindView(R2.id.et_welcome_wifi_password_input)
    EditText mEtPassword;
    @BindView(R2.id.btn_welcome_wifi_password_visibility)
    Button mBtnPasswordVisibility;
    @BindView(R2.id.tv_welcome_wifi_password_tip)
    TextView mTvPasswordTip;
    @BindView(R2.id.btn_welcome_wifi_password_next)
    Button mBtnNext;
    private WifiConnectionViewModel mViewModel;

    public static WelcomeWifiPasswordFragment newInstance() {
        return new WelcomeWifiPasswordFragment();
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
        return R.layout.fragment_welcome_wifi_password;
    }

    @Override
    protected WifiConnectionViewModel getViewModel() {
        return getViewModelProvider().get(WifiConnectionViewModel.class);
    }

    private void initView() {
        AccessPoint selected = mViewModel.getSelected();
        if (selected != null) {
            mTvSubtitle.setText(selected.getSSID());
        }

        mEtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mViewModel.setPassword(s.toString());
            }
        });

        mViewModel.getPasswordTipObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(tip -> {
                    switch (tip) {
                        case TIP_OK:
                            mTvPasswordTip.setText("");
                            mBtnNext.setEnabled(true);
                            break;
                        case TIP_EMPTY:
                            mTvPasswordTip.setText(R.string.welcome_wifi_password_tip_empty);
                            mBtnNext.setEnabled(false);
                            break;
                        case TIP_TOO_SHORT:
                            mTvPasswordTip.setText(R.string.welcome_wifi_password_tip_too_short);
                            mBtnNext.setEnabled(false);
                            break;
                    }
                });

        mEtPassword.requestFocus();
        mEtPassword.setText(mViewModel.getSelectedPassword());
        mEtPassword.setSelection(mViewModel.getSelectedPassword().length());
        InputMethodManager im = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (im != null && im.isActive()) {
            im.toggleSoftInputFromWindow(getView().getApplicationWindowToken(), 0, 0);
        }
    }

    private void hideKeyboard() {
        if (getView() == null) return;
        if (getContext() == null) return;

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.isActive()) {
            imm.hideSoftInputFromWindow(getView().getApplicationWindowToken(), 0);
        }
    }

    @Override
    protected void back() {
        mEtPassword.getText().clear();
        hideKeyboard();
        mViewModel.setSelected(null);
        super.back();
    }

    @OnClick(R2.id.btn_welcome_wifi_password_visibility)
    void onClickPasswordLock() {
        playNormalClickSound();
        final int cursorPosition = mEtPassword.getSelectionStart();
        int inputType = mEtPassword.getInputType();
        if (inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            mEtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mBtnPasswordVisibility.setBackgroundResource(R.drawable.ic_settings_password_hide_136x136);
        } else {
            mEtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            mBtnPasswordVisibility.setBackgroundResource(R.drawable.ic_settings_password_show_136x136);
        }
        mEtPassword.setSelection(cursorPosition);
    }

    @OnClick(R2.id.btn_welcome_wifi_password_next)
    void onClickNext() {
        playNormalClickSound();
        if (getView() == null) return;

        InputMethodManager imm = (InputMethodManager) getView().getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.isActive()) {
            imm.hideSoftInputFromWindow(getView().getApplicationWindowToken(), 0);
        }

        mViewModel.notifyConnect();
        hideKeyboard();
        super.back();
    }
}
