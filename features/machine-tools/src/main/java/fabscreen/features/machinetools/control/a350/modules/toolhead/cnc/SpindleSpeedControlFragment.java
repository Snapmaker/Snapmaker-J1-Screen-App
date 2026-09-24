package fabscreen.features.machinetools.control.a350.modules.toolhead.cnc;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.view.RulerView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class SpindleSpeedControlFragment extends BaseFragment {

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

    @BindView(R2.id.btn_control_cnc_page_spindle_switch)
    Button mBtnSwitch;
    private SpindleSpeedControlViewModel mViewModel;

    private BehaviorSubject<Float> mTargetValueSubject = BehaviorSubject.createDefault(0f);

    public static Fragment newInstance() {
        return new SpindleSpeedControlFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
    }

    private void initView() {
        mTvTitle.setText(fabscreen.platform.core.R.string.print_spindle_spend);

        mTvValueCurrent.setVisibility(View.GONE);
        mTvValueSlash.setVisibility(View.GONE);
        mTvValueUnit.setVisibility(View.VISIBLE);

        mTvValueUnit.setText(fabscreen.platform.core.R.string.all_unit_percentage);
        mRvRuler.setMinValue(50);
        mRvRuler.setMaxValue(100);

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
                .subscribe(value -> mViewModel.saveCNCPower(value));
    }

    @Override
    protected SpindleSpeedControlViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(SpindleSpeedControlViewModel.class);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_control_cnc_page_spindle;
    }

    @OnClick(R2.id.btn_control_cnc_page_spindle_switch)
    void onClickSwitch() {
        playNormalClickSound();
        if (mBtnSwitch.isActivated()) {
            mBtnSwitch.setActivated(false);
            mViewModel.switchOffSpindle();
        } else {
            mBtnSwitch.setActivated(true);
            if (mTargetValueSubject == null || mTargetValueSubject.getValue() == null) return;
            float targetValue = mTargetValueSubject.getValue();
            mViewModel.switchOnSpindle(targetValue);
        }
    }

    private String formatValue(float value) {
        return String.format(Locale.US, "%.1f", value);
    }
}
