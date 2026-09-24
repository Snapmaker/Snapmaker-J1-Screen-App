package fabscreen.features.print.a400platform;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.a400platform.viewmodel.PrintReadyViewModel;
import fabscreen.platform.base.view.BaseFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class A400LaserAutoThickMeasureFragment extends BaseFragment {
    @BindView(R2.id.temp_auto_thickness_measure)
    TextView mAutoThickness;
    @BindView(R2.id.tv_auto_thickness_measure_details)
    TextView mTvAutoThicknessContent;

    PrintReadyViewModel mViewModel;


    public static Fragment newInstance() {
        return new A400LaserAutoThickMeasureFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = getViewModel();
        mViewModel.getThicknessMeasure()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(f -> mAutoThickness.setText("材料厚度为：" + f));


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        mTvAutoThicknessContent.setText(R.string.print_automatic_thickness_measurement_message);
    }


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_auto_thickness_measure;
    }

    @Override
    protected PrintReadyViewModel getViewModel() {
        return getViewModelProvider().get(PrintReadyViewModel.class);
    }
}
