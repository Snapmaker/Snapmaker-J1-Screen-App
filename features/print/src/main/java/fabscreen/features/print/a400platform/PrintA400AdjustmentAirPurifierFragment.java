package fabscreen.features.print.a400platform;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.a400platform.viewmodel.A400AirPurifierControlViewModel;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.view.ActionButton;
import fabscreen.platform.core.ui.view.RectEnergyBar.RectEnergyBar;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * 1. Power on/off;
 * 2. Tune fan speed;
 * 3. Display filter life.
 */
public class PrintA400AdjustmentAirPurifierFragment extends BaseFragment {

    @BindView(R2.id.ab_purifier_power)
    ActionButton mAbPower;
    @BindView(R2.id.sbg_fan_speed)
    SegmentedButtonGroup mSbgFanSpeed;
    @BindView(R2.id.reb_lifetime)
    RectEnergyBar mRebLifeTime;
    private A400AirPurifierControlViewModel mViewModel;

    public static Fragment newInstance() {
        return new PrintA400AdjustmentAirPurifierFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
    }

    private void initView() {
        mRebLifeTime.setMaxLevel(3);
        mRebLifeTime.initialize();
        mSbgFanSpeed.setOnClickedButtonListener(position -> mViewModel.setPurifierFanSpeed(position + 1));

        mViewModel.getPowerStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isPowerOn -> mAbPower.setEnabled(isPowerOn), LogHelper::log);

        mViewModel.getFanOnOffObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isFanOn -> mAbPower.setActivated(isFanOn), LogHelper::log);

        mViewModel.getFanSpeedObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(level -> mSbgFanSpeed.setPosition(level - 1), LogHelper::log);

        mViewModel.getFilterLifeObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(life -> mRebLifeTime.setPosition(life), LogHelper::log);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_print_adjustment_air_purifier;
    }

    @Override
    protected A400AirPurifierControlViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(A400AirPurifierControlViewModel.class);
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
