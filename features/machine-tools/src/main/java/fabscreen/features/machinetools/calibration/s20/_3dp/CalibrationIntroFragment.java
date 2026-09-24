package fabscreen.features.machinetools.calibration.s20._3dp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.common.CalibrationViewModel;

public class CalibrationIntroFragment extends BaseFragment {
    @BindView(R2.id.tv_calibration_intro_title)
    TextView mTvIntroTitle;
    @BindView(R2.id.tv_calibration_intro_content)
    TextView mTvIntroContent;
    private CalibrationViewModel mViewModel;

    public static CalibrationIntroFragment newInstance() {
        return new CalibrationIntroFragment();
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

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_calibration_intro;
    }

    private void initView() {
        boolean isAutoMode = mViewModel.isAutoMode();
        setTitle(isAutoMode ? R.string.calibration_auto_leveling : R.string.calibration_manual_leveling);
        mTvIntroTitle.setText(R.string.calibration_intro_calibrate_the_bed);
        mTvIntroContent.setText(isAutoMode ? R.string.calibration_intro_auto_leveling_calibrate_the_bed_desc
                : R.string.calibration_intro_manual_leveling_calibrate_the_bed_desc);
    }

    @Override
    protected CalibrationViewModel getViewModel() {
        return getViewModelProvider().get(CalibrationViewModel.class);
    }

    @OnClick(R2.id.btn_calibration_intro_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((Calibration3DPActivity) getActivity()).startCalibrationFragment();
        }
    }
}
