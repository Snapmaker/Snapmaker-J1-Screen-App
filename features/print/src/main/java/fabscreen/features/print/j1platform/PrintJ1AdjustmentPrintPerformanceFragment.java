package fabscreen.features.print.j1platform;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.j1platform.viewmodel.PrintJ1AdjustmentPerformanceViewModel;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class PrintJ1AdjustmentPrintPerformanceFragment extends BaseFragment {

    @BindView(R2.id.tv_j1_print_performance_status)
    TextView mTvPerformanceControlStatus;
    @BindView(R2.id.ll_j1_print_adjustment_print_performance_widget)
    View mViewPrintPerformance;

    @BindView(R2.id.iv_j1_print_performance_silent_pic)
    ImageView mIvOptionSilent;
    @BindView(R2.id.iv_j1_print_performance_default_pic)
    ImageView mIvOptionDefault;
    @BindView(R2.id.iv_j1_print_performance_fast_pic)
    ImageView mIvOptionFast;

    private PrintJ1AdjustmentPerformanceViewModel mViewModel;

    public static Fragment newInstance() {
        return new PrintJ1AdjustmentPrintPerformanceFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = getFragmentScopeViewModel(PrintJ1AdjustmentPerformanceViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        mViewModel.getPerformanceOptionObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::updateOptionView);

        // Get performance option when initialized.
        mViewModel.getPerformanceOption()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {/**/}, LogHelper::log);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_print_adjustment_print_performance;
    }

    private void updateOptionView(int option) {
        switch (option) {
            case 0:
                // reserved
                break;
            case 1:
                mTvPerformanceControlStatus.setText(R.string.j1_print_adjus_print_performance_default);
                mViewPrintPerformance.setBackgroundResource(R.drawable.pic_tab_horizontal_center_656x104);
                mIvOptionSilent.setImageResource(R.drawable.icon_speed_silent_normal_64x64);
                mIvOptionDefault.setImageResource(R.drawable.icon_speed_normal_checked_64x64);
                mIvOptionFast.setImageResource(R.drawable.icon_speed_fast_normal_64x64);
                break;
            case 2:
                mTvPerformanceControlStatus.setText(R.string.j1_print_adjust_print_performance_silent);
                mViewPrintPerformance.setBackgroundResource(R.drawable.pic_tab_horizontal_left_656x104);
                mIvOptionSilent.setImageResource(R.drawable.icon_speed_silent_checked_64x64);
                mIvOptionDefault.setImageResource(R.drawable.icon_speed_normal_normal_64x64);
                mIvOptionFast.setImageResource(R.drawable.icon_speed_fast_normal_64x64);
                break;
            case 3:
                mTvPerformanceControlStatus.setText(R.string.j1_print_adjust_print_performance_fast);
                mViewPrintPerformance.setBackgroundResource(R.drawable.pic_tab_horizontal_right_656x104);
                mIvOptionSilent.setImageResource(R.drawable.icon_speed_silent_normal_64x64);
                mIvOptionDefault.setImageResource(R.drawable.icon_speed_normal_normal_64x64);
                mIvOptionFast.setImageResource(R.drawable.icon_speed_fast_checked_64x64);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @OnClick(R2.id.iv_j1_print_performance_silent_pic)
    void onClickSilent() {
        playNormalClickSound();
        mViewModel.requestPerformanceOption(2)
                .flatMap(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        return mViewModel.getPerformanceOption()
                                .flatMap(responseStructure1 -> Observable.just(responseStructure1.isSuccess()));
                    } else {
                        return Observable.just(false);
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(result -> {}, LogHelper::log);
    }

    @OnClick(R2.id.iv_j1_print_performance_default_pic)
    void onCLickNormal() {
        playNormalClickSound();
        mViewModel.requestPerformanceOption(1)
                .flatMap(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        return mViewModel.getPerformanceOption()
                                .flatMap(responseStructure1 -> Observable.just(responseStructure1.isSuccess()));
                    } else {
                        return Observable.just(false);
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(result -> {}, LogHelper::log);
    }

    @OnClick(R2.id.iv_j1_print_performance_fast_pic)
    void onClickFast() {
        playNormalClickSound();
        mViewModel.requestPerformanceOption(3)
                .flatMap(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        return mViewModel.getPerformanceOption()
                                .flatMap(responseStructure1 -> Observable.just(responseStructure1.isSuccess()));
                    } else {
                        return Observable.just(false);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(result -> {}, LogHelper::log);
    }

}
