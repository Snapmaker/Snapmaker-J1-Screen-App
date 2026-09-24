package fabscreen.features.machinetools.calibration.s20.laser._10w;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.common.Laser10wThicknessCalibrationViewModel;

public class SettingsLaser10wThicknessMeasureCalibrationCongratulateFragment extends BaseFragment {

    @BindView(R2.id.tv_guide_complete_content)
    TextView mTvDesc;
    @BindView(R2.id.btn_guide_complete_next)
    Button mBtnOK;
    private Laser10wThicknessCalibrationViewModel mViewModel;

    public static Fragment getInstance() {
        return new SettingsLaser10wThicknessMeasureCalibrationCongratulateFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        mTvDesc.setText(R.string.settings_laser_thickness_calibration_complete);
        mBtnOK.setText(R.string.all_ok);
    }

    @Override
    protected Laser10wThicknessCalibrationViewModel getViewModel() {
        return getViewModelProvider().get(Laser10wThicknessCalibrationViewModel.class);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_guide_complete_land;
    }

    @OnClick(R2.id.btn_guide_complete_next)
    void onClickOK() {
        playNormalClickSound();
        mViewModel.switchAFAssistLight(false);
        requireActivity().finish();
    }
}
