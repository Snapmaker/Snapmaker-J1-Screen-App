package fabscreen.features.settings.a350.about;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.common.WelcomeNameViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;

@Route(path = RoutePath.SETTINGS_NAME)
public class SettingsNameFragment extends BaseFragment {
    @BindView(R2.id.et_welcome_name_input)
    EditText mEtMachineName;
    @BindView(R2.id.tv_welcome_name_tip)
    TextView mTvMachineNameTip;
    @BindView(R2.id.btn_welcome_name_next)
    Button mBtnSave;
    private WelcomeNameViewModel mViewModel;

    public static SettingsNameFragment getInstance() {
        return new SettingsNameFragment();
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
                            mBtnSave.setEnabled(true);
                            break;
                        case WelcomeNameViewModel.TIP_EMPTY:
                            mTvMachineNameTip.setText(R.string.welcome_name_tip_empty);
                            mBtnSave.setEnabled(false);
                            break;
                    }
                });
    }

    @Override
    protected WelcomeNameViewModel getViewModel() {
        return getViewModelProvider().get(WelcomeNameViewModel.class);
    }

    @OnClick(R2.id.btn_welcome_name_next)
    void save() {
        playNormalClickSound();
        mViewModel.saveName();
        back();
    }
}
