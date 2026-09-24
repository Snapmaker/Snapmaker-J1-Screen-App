package fabscreen.features.settings.a350.experiment;

import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseFragment;

public class ExperimentSelfInpectionFragment extends BaseFragment {

    @BindView(R2.id.tv_experiment_self_inspection_temperature)
    TextView mTvTemperature;

    int mProtectTemperature;
    int mRecoveryTemperature;


    @OnTextChanged(R2.id.ed_experiment_self_inspection_protect_temperature)
    void onProtectTemperatureChange(CharSequence protectTemperature) {
        try {
            mProtectTemperature = Integer.parseInt(protectTemperature.toString());
        } catch (Exception e) {

        }

    }

    @OnTextChanged(R2.id.ed_experiment_self_inspection_recovery_temperature)
    void onRecoveryTemperatureChange(CharSequence protectTemperature) {
        try {
            mRecoveryTemperature = Integer.parseInt(protectTemperature.toString());
        } catch (Exception e) {

        }

    }

    @OnClick(R2.id.btn_experiment_inspection_temperature_setting)
    void onSetting() {
        playNormalClickSound();
        if (mProtectTemperature > 0 && mProtectTemperature < 100 && mRecoveryTemperature > 0 && mRecoveryTemperature < 100) {
            ServiceContainer.getInstance().getService(IMachine.class).getLaserController().setTemperatureThreshold(mProtectTemperature, mRecoveryTemperature);
            mTvTemperature.setText(String.format("温度设置成功。保护温度：%s,恢复温度:%s", mProtectTemperature, mRecoveryTemperature));
        } else {
            mTvTemperature.setText(String.format("温度设置错误（0~100），请重新设置，保护温度：%s,恢复温度:%s", mProtectTemperature, mRecoveryTemperature));
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_experiment_self_inspection;
    }
}
