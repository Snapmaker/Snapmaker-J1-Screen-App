package fabscreen.features.settings.wifi;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.viewmodel.WifiConnectionViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class J1WifiPasswordFragment extends BaseFragment {

    private WifiConnectionViewModel mViewModel;

    public static Fragment newInstance(String ssid) {
        J1WifiPasswordFragment fragment = new J1WifiPasswordFragment();
        Bundle bundle = new Bundle();
        bundle.putString("ssid", ssid);
        fragment.setArguments(bundle);
        return fragment;
    }

    @BindView(R2.id.tv_ssid)
    TextView mTvSsid;
    @BindView(R2.id.et_password)
    EditText mEtPassword;
    @BindView(R2.id.btn_connect)
    Button mBtnConnect;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getActivityScopeViewModel(WifiConnectionViewModel.class);
        initView();
    }

    private void initView() {
        Bundle arguments = getArguments();
        String ssid = Objects.requireNonNull(arguments).getString("ssid", "Unknown SSID");
        mTvSsid.setText(ssid);
        mEtPassword.setText(mViewModel.getSelectedPassword());
        mViewModel.setPassword(mViewModel.getSelectedPassword());
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
                            mBtnConnect.setEnabled(true);
                            break;
                        case TIP_EMPTY:
                        case TIP_TOO_SHORT:
                            mBtnConnect.setEnabled(false);
                            break;
                    }
                });
    }

    @OnClick(R2.id.btn_connect)
    void onConnectClicked() {
        playNormalClickSound();
        mViewModel.connect();
        super.back();
    }

    @Override
    protected void back() {
        mViewModel.setSelected(null);
        super.back();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_wifi_password;
    }
}
