package fabscreen.features.settings.a400.remote;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.MenuAdapter;
import fabscreen.platform.core.ui.common.A400SwitchCompat;
import fabscreen.platform.core.ui.view.PullDownMenu;

public class A400SettingsRemoteFragment extends BaseFragment {
    @BindView(R2.id.sw_setting_remote_allow_connection)
    A400SwitchCompat mSwAllowConnection;
    @BindView(R2.id.sw_setting_remote_safe_mode)
    A400SwitchCompat mSwSafeMode;
    @BindView(R2.id.tv_setting_remote_connection_verification)
    TextView mTvConnectionVerification;
    @BindView(R2.id.lr_setting_remote_connection_verification)
    RelativeLayout mRlConnectionVerification;
    IPreferences.Helper mPreferencesHelper;
    String[] stringArray;
    private MenuAdapter mMenuAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stringArray = getResources().getStringArray(R.array.setting_remote_connection_verification_mode);
        initMenu();
        mPreferencesHelper = getServiceContainer().getService(IPreferences.class).getHelper();
    }

    private void initMenu() {

        ArrayList<String> menuItems = new ArrayList<>(Arrays.asList(stringArray));
        mMenuAdapter = new MenuAdapter(getContext(), menuItems);
        mMenuAdapter.setOnItemClickListener((view, position) -> {
            playNormalClickSound();
            mPreferencesHelper.setConnectionVerification(position);
            mTvConnectionVerification.setText(stringArray[position]);
            PullDownMenu.dismiss();
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        mSwAllowConnection.setChecked(mPreferencesHelper.getRemoteAllowConnection());
        mSwSafeMode.setChecked(mPreferencesHelper.getRemoteSafeMode());
        mMenuAdapter.setSelectPosition(mPreferencesHelper.getConnectionVerification());
        mTvConnectionVerification.setText(stringArray[mPreferencesHelper.getConnectionVerification()]);
    }

    @OnCheckedChanged(R2.id.sw_setting_remote_allow_connection)
    public void onRemoteAllowConnectionCheckChange(CompoundButton view, boolean isCheck) {
        playSwitchSound();
        mPreferencesHelper.setRemoteAllowConnection(isCheck);
    }

    @OnCheckedChanged(R2.id.sw_setting_remote_safe_mode)
    public void onRemoteRemoteSafeModeCheckChange(CompoundButton view, boolean isCheck) {
        playSwitchSound();
        mPreferencesHelper.setRemoteSafeMode(isCheck);
    }

    @OnClick(R2.id.lr_setting_remote_connection_verification)
    void onConnectionVerificationChanged() {
        playNormalClickSound();
        PullDownMenu.create(getContext(), mMenuAdapter)
                .showBelowView(mRlConnectionVerification, -mRlConnectionVerification.getMeasuredWidthAndState() - (int) DimensUtils.dp2px(60), -mRlConnectionVerification.getHeight());
    }

    public static Fragment newInstance() {
        return new A400SettingsRemoteFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_settings_remote;
    }
}
