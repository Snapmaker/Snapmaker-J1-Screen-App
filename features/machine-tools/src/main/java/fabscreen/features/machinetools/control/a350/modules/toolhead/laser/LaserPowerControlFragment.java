package fabscreen.features.machinetools.control.a350.modules.toolhead.laser;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.FabConfirm;
import fabscreen.platform.core.ui.view.RulerView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class LaserPowerControlFragment extends BaseFragment {

    @Nullable
    @BindView(R2.id.tv_widget_set_value_ruler_title)
    protected TextView mTvTitle;

    @Nullable
    @BindView(R2.id.tv_widget_set_value_ruler_value_current)
    protected TextView mTvValueCurrent;

    @Nullable
    @BindView(R2.id.tv_widget_set_value_ruler_value_slash)
    protected TextView mTvValueSlash;

    @Nullable
    @BindView(R2.id.tv_widget_set_value_ruler_value_target)
    protected TextView mTvValueTarget;

    @Nullable
    @BindView(R2.id.tv_widget_set_value_ruler_value_unit)
    protected TextView mTvValueUnit;

    @Nullable
    @BindView(R2.id.rv_widget_set_value_ruler_ruler)
    protected RulerView mRvRuler;

    @Nullable
    @BindView(R2.id.btn_control_laser_page_power_switch)
    Button mBtnPowerSwitch;

    @Nullable
    @BindView(R2.id.btn_control_10w_laser_page_power_switch)
    Button mBtn10wPowerSwitch;

    @Nullable
    @BindView(R2.id.iv_control_laser_page_power)
    ImageView mIvPower;

    private Button mBtnSwitch;

    private LaserPowerControlViewModel mViewModel;

    private BehaviorSubject<Float> mTargetValueSubject = BehaviorSubject.createDefault(0f);

    public static Fragment newInstance() {
        return new LaserPowerControlFragment();
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

    private void initView() {
        if (mViewModel.is10wLaserNormalMode()) {
            mBtnSwitch = mBtn10wPowerSwitch;
            return;
        }

        mBtnSwitch = mBtnPowerSwitch;

        // These fields won't be null when it comes to low power laser, check it to avoid lint
        if (mTvTitle == null
                || mTvValueCurrent == null
                || mTvValueSlash == null
                || mTvValueUnit == null
                || mRvRuler == null
                || mTvValueTarget == null) {
            return;
        }
        mTvTitle.setText(fabscreen.platform.core.R.string.print_laser_power);

        mTvValueCurrent.setVisibility(View.GONE);
        mTvValueSlash.setVisibility(View.GONE);
        mTvValueUnit.setVisibility(View.VISIBLE);

        mTvValueUnit.setText(fabscreen.platform.core.R.string.all_unit_percentage);
        mRvRuler.setMaxValue(100);
        mRvRuler.setUnit(0.5f);
        mRvRuler.setCurrentValue(mViewModel.getInitialLaserPower());

        // changes
        mRvRuler.setOnValueChangedListener(value -> {
            mTargetValueSubject.onNext(value);
        });

        // update current / target temp.
        mTargetValueSubject.skip(1)
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(value -> mTvValueTarget.setText(formatValue(value)));

        // save the changed value
        mTargetValueSubject.debounce(1000, TimeUnit.MILLISECONDS)
                .as(bindToLifecycle())
                .subscribe(value -> mViewModel.saveLaserPower(value));
    }

    @Override
    protected LaserPowerControlViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(LaserPowerControlViewModel.class);
    }

    @Override
    protected int getLayoutResID() {
        return mViewModel.is10wLaserNormalMode() ? R.layout.fragment_control_10w_laser_page_power : R.layout.fragment_control_laser_page_power;
    }

    private String formatValue(float value) {
        return String.format(Locale.US, "%.1f", value);
    }

    @Optional
    @OnClick(R2.id.btn_control_laser_page_power_switch)
    void onLaserPowerSwitchCLicked() {
        playNormalClickSound();
        onSwitchClicked();
    }

    @Optional
    @OnClick(R2.id.btn_control_10w_laser_page_power_switch)
    void on10wLaserPowerSwitchClicked() {
        playNormalClickSound();
        onSwitchClicked();
    }

    private void onSwitchClicked() {
        if (mBtnSwitch.isActivated()) {
            mBtnSwitch.setActivated(false);
            mViewModel.switchOffLaser();
            if (mViewModel.is10wLaserNormalMode()) {
                if (mIvPower == null) return;
                mIvPower.setImageResource(R.drawable.ic_control_10w_laser_page_power_off);
            }
        } else {
            if (mViewModel.is10wLaserNormalMode()) {
                mViewModel.getHeaderSecurityStatus()
                        .observeOn(AndroidSchedulers.mainThread())
                        .as(bindToLifecycle())
                        .subscribe(headerSecurity -> {
                            if (headerSecurity.status == 0) {
                                show10wLaserWarning();
                            }
                        }, LogHelper::log);
            } else {
                mBtnSwitch.setActivated(true);
                if (mTargetValueSubject == null || mTargetValueSubject.getValue() == null) return;
                float targetValue = mTargetValueSubject.getValue();
                mViewModel.switchOnLaser(targetValue);
            }
        }
    }

    private void show10wLaserWarning() {
        FabConfirm.create(getContext())
                .setCanceledOnTouchOutSide(false)
                .setIcon(R.drawable.pic_dialog_warning_72x72)
                .setDescription(R.string.controller_laser_safety_goggles_message)
                .setConfirm(R.string.all_ok, (dialog, which) -> {
                    dialog.dismiss();
                    mBtnSwitch.setActivated(true);
                    if (mIvPower == null) return;
                    mIvPower.setImageResource(R.drawable.ic_control_10w_laser_page_power_on);
                    mViewModel.switchOn10wLaser();
                }).show();
    }

}
