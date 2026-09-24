package fabscreen.features.machinetools.control.a350.modules.toolhead._3dp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.R2;
import fabscreen.platform.core.ui.view.ActionButton;
import fabscreen.platform.base.view.FabConfirm;
import fabscreen.platform.core.ui.view.RulerView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public abstract class NozzleControlFragment extends BaseFragment {
    @BindView(R2.id.tv_widget_set_value_ruler_title)
    protected TextView mTvTitle;
    @BindView(R2.id.tv_widget_set_value_ruler_value_current)
    protected TextView mTvValueCurrent;
    @BindView(R2.id.tv_widget_set_value_ruler_value_slash)
    protected TextView mTvValueSlash;
    @BindView(R2.id.tv_widget_set_value_ruler_value_target)
    protected TextView mTvValueTarget;
    @BindView(R2.id.tv_widget_set_value_ruler_value_unit)
    protected TextView mTvValueUnit;
    @BindView(R2.id.rv_widget_set_value_ruler_ruler)
    protected RulerView mRvRuler;

    @BindView(R2.id.btn_widget_load_filament_unload)
    ActionButton mBtnFilamentUnload;
    @BindView(R2.id.btn_widget_load_filament_load)
    ActionButton mBtnFilamentLoad;

    private NozzleControlViewModel mViewModel;
    private BehaviorSubject<Integer> mTargetDegreeSubject = BehaviorSubject.createDefault(0);

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.d("vp fg lifecycle: setUserVisibleHint onCreate");
        mViewModel = getViewModel();
        initView();
    }

    private void initView() {
        mTvTitle.setText(fabscreen.platform.core.R.string.print_nozzle_temp);

        mTvValueCurrent.setVisibility(View.VISIBLE);
        mTvValueSlash.setVisibility(View.VISIBLE);
        mTvValueUnit.setVisibility(View.VISIBLE);
        mTvValueUnit.setText(R.string.all_unit_temperature);
        mRvRuler.setMaxValue(275);

        // changes
        mRvRuler.setOnValueChangedListener(value -> {
            mTargetDegreeSubject.onNext((int) value);
        });

        // init target temp.
        mTvValueCurrent.setText(formatValue(mViewModel.getMachineStatus().leftNozzleTemperature));
        mTvValueTarget.setText(formatValue(mViewModel.getMachineStatus().leftNozzleTargetTemperature));
        mRvRuler.setCurrentValue(mViewModel.getMachineStatus().leftNozzleTargetTemperature);

        // update current / target temp, send target temp
        mTargetDegreeSubject.skip(1)
                .debounce(200, TimeUnit.MILLISECONDS)
                .doOnNext(degree -> mViewModel.requestSetTargetDegree(degree))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(degree -> mTvValueTarget.setText(formatValue(degree)));

        mViewModel.getMachineStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(status -> mTvValueCurrent.setText(formatValue(status.leftNozzleTemperature)));

        // load filament button
        mViewModel.getReadyToLoadObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isReady -> {
                    mBtnFilamentUnload.setEnabled(isReady);
                    mBtnFilamentLoad.setEnabled(isReady);
                });

        mViewModel.getLoadingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isLoading -> mBtnFilamentLoad.setActivated(isLoading));

        mViewModel.getUnloadingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isUnloading -> mBtnFilamentUnload.setActivated(isUnloading));
    }

    @OnClick(R2.id.btn_widget_load_filament_load)
    void onClickLoadFilament() {
        playNormalClickSound();
        mViewModel.loadFilament();
    }

    @OnClick(R2.id.btn_widget_load_filament_unload)
    void onClickUnloadFilament() {
        playNormalClickSound();
        mViewModel.unloadFilament();
    }

    @Override
    protected NozzleControlViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(NozzleControlViewModel.class);
    }

    @Override
    protected abstract int getLayoutResID();

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Logger.d("vp fg lifecycle: setUserVisibleHint %s", isVisibleToUser);
        if (!isVisibleToUser) return;
        if (mViewModel.getMachineStatus().leftNozzleTargetTemperature == 0) {
            FabConfirm.create(getContext())
                    .setDescription(R.string.control_heat_warning)
                    .setConfirm(R.string.all_confirm, (dialog, which) -> {
                        dialog.dismiss();
                        mRvRuler.setCurrentValue(200f);
                    })
                    .setCancel(R.string.all_cancel, (dialog, which) -> dialog.dismiss())
                    .show();
        }
        lazyLoadPageData();
    }

    protected abstract void lazyLoadPageData();

    protected String formatValue(float value) {
        return String.format(Locale.US, "%d", (int) value);
    }
}
