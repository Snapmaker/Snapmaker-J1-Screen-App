package fabscreen.features.machinetools.control.a350.modules.bed;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import fabscreen.features.machinetools.R;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.R2;
import fabscreen.platform.core.ui.view.RulerView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class HeatedBedControlFragment extends BaseFragment {

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
    private HeatedBedControlViewModel mViewModel;
    private BehaviorSubject<Integer> mTargetDegreeSubject = BehaviorSubject.createDefault(0);

    public static Fragment newInstance() {
        return new HeatedBedControlFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
    }

    private void initView() {
        mTvTitle.setText(R.string.print_heated_bed_temp);

        mTvValueCurrent.setVisibility(View.VISIBLE);
        mTvValueSlash.setVisibility(View.VISIBLE);
        mTvValueUnit.setVisibility(View.VISIBLE);

        mTvValueUnit.setText(R.string.all_unit_temperature);
        mRvRuler.setMaxValue(100);

        // changes
        mRvRuler.setOnValueChangedListener(value -> {
            mTargetDegreeSubject.onNext((int) value);
        });

        // init target temp.
        mTvValueCurrent.setText(formatValue(mViewModel.getMachineStatus().bedTemperature));
        mTvValueTarget.setText(formatValue(mViewModel.getMachineStatus().bedTargetTemperature));
        mRvRuler.setCurrentValue(mViewModel.getMachineStatus().bedTargetTemperature);

        // update current / target temp.
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
    }

    @Override
    protected HeatedBedControlViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(HeatedBedControlViewModel.class);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_control_3dp_page_heated_bed;
    }

    protected String formatValue(float value) {
        return String.format(Locale.US, "%d", (int) value);
    }
}
