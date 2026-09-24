package fabscreen.features.machinetools.control.a400;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.control.common.S30AirPurifierControlViewModel;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.view.ActionButton;
import fabscreen.platform.core.ui.view.RectEnergyBar.RectEnergyBar;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * 1. Power on/off;
 * 2. Tune fan speed;
 * 3. Display filter life.
 */
public class A400AirPurifierControlFragment extends BaseFragment {

    @BindView(R2.id.ab_purifier_power)
    ImageView mAbPower;
    @BindView(R2.id.sbg_fan_speed)
    SegmentedButtonGroup mSbgFanSpeed;
    @BindView(R2.id.pb_lifetime)
    ProgressBar mPbLifeTime;
    @BindView(R2.id.tv_no_power_bg)
    TextView mTvNotPower;
    @BindView(R2.id.ll_no_power_tip)
    LinearLayout mLlNotPower;
    private S30AirPurifierControlViewModel mViewModel;

    public static Fragment newInstance() {
        return new A400AirPurifierControlFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
    }

    private void initView() {
        mPbLifeTime.setMax(100);
        mSbgFanSpeed.setOnPositionChangedListener(position -> mViewModel.setPurifierFanSpeed(position + 1));
        mSbgFanSpeed.setPosition(1, false);
        mViewModel.getPowerStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isPowerOn -> {
                    mAbPower.setEnabled(isPowerOn);
                    mLlNotPower.setVisibility(isPowerOn ? View.GONE : View.VISIBLE);
                    mTvNotPower.setVisibility(isPowerOn ? View.GONE : View.VISIBLE);
                }, LogHelper::log);

        mViewModel.getFanOnOffObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isFanOn -> mAbPower.setActivated(isFanOn), LogHelper::log);

        mViewModel.getFanSpeedObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(level -> mSbgFanSpeed.setPosition(level - 1, true), LogHelper::log);

        mViewModel.getFilterLifeObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(life -> mPbLifeTime.setProgress(life == 3 ? 100 : life * 33), LogHelper::log);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_control_air_purifier;
    }

    @Override
    protected S30AirPurifierControlViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(S30AirPurifierControlViewModel.class);
    }

    @OnClick(R2.id.btn_more_settings)
    void onMoreSettingsClick() {
        playNormalClickSound();
        ((A400ControlActivity) requireActivity()).goToAirPurifierSettings();
    }

    @OnClick(R2.id.ab_purifier_power)
    void onPurifierPowerClicked() {
        playNormalClickSound();
        mViewModel.switchPurifierPower();
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.subscribePurifierStatus();
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.unsubscribePurifierStatus();
    }
}
