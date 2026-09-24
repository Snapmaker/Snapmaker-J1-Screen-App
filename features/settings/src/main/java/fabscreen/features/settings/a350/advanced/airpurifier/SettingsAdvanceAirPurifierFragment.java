package fabscreen.features.settings.a350.advanced.airpurifier;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class SettingsAdvanceAirPurifierFragment extends BaseFragment {
    @BindView(R2.id.btn_settings_air_purifier_auto_turn_on)
    Button mBtnAirPurifierAutoTurnOn;
    @BindView(R2.id.btn_settings_air_purifier_auto_turn_off)
    Button mBtnAirPurifierAutoTurnOff;
    private boolean mAutoTurnOnMode;
    private boolean mAutoTurnOffMode;
    private int mHeadType = Module.ModuleType.HEAD_UNPLUGGED;

    public static SettingsAdvanceAirPurifierFragment getInstance() {
        return new SettingsAdvanceAirPurifierFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.settings_air_purifier);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_settings_advance_air_purifier;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        mHeadType = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId();
        switch (mHeadType) {
            case Module.ModuleType.HEAD_3DP:
                mAutoTurnOnMode = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getAirPurifier3DPAutoFlag();
                break;
            case Module.ModuleType.HEAD_LASER:
            case Module.ModuleType.HEAD_LASER_10W:
                mAutoTurnOnMode = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getAirPurifierLaserAutoFlag();
                break;
            case Module.ModuleType.HEAD_CNC:
                mAutoTurnOnMode = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getAirPurifierCNCAutoTurnOnFlag();
                break;
            case Module.ModuleType.HEAD_UNPLUGGED:
            default:
                // unknown
                mAutoTurnOnMode = false;
                break;
        }
        mBtnAirPurifierAutoTurnOn.setActivated(mAutoTurnOnMode);

        mAutoTurnOffMode = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getAirPurifierAutoTurnOffFlag();
        mBtnAirPurifierAutoTurnOff.setActivated(mAutoTurnOffMode);
    }

    private void setAutoTurnOnMode(boolean autoMode) {
        switch (mHeadType) {
            case Module.ModuleType.HEAD_3DP:
                ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setAirPurifier3DPAutoFlag(autoMode);
                break;
            case Module.ModuleType.HEAD_LASER:
            case Module.ModuleType.HEAD_LASER_10W:
                ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setAirPurifierLaserAutoFlag(autoMode);
                break;
            case Module.ModuleType.HEAD_CNC:
                ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setAirPurifierCNCAutoTurnOnFlag(autoMode);
                break;
            case Module.ModuleType.HEAD_UNPLUGGED:
            default:
                // unknown
                break;
        }

    }

    @OnClick(R2.id.btn_settings_air_purifier_auto_turn_on)
    void onClickAirPurifierAutoTurnOnMode() {
        playNormalClickSound();
        mBtnAirPurifierAutoTurnOn.setEnabled(false);
        mAutoTurnOnMode = !mAutoTurnOnMode;
        setAutoTurnOnMode(mAutoTurnOnMode);
        mBtnAirPurifierAutoTurnOn.setActivated(mAutoTurnOnMode);
        mBtnAirPurifierAutoTurnOn.setEnabled(true);
    }

    @OnClick(R2.id.btn_settings_air_purifier_auto_turn_off)
    void onClickAirPurifierAutoTurnOffMode() {
        playNormalClickSound();
        mBtnAirPurifierAutoTurnOff.setEnabled(false);
        mAutoTurnOffMode = !mAutoTurnOffMode;
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setAirPurifierAutoTurnOffFlag(mAutoTurnOffMode);
        mBtnAirPurifierAutoTurnOff.setActivated(mAutoTurnOffMode);
        mBtnAirPurifierAutoTurnOff.setEnabled(true);
    }

}
