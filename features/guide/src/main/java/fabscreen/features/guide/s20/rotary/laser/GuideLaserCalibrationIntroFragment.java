package fabscreen.features.guide.s20.rotary.laser;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class GuideLaserCalibrationIntroFragment extends BaseFragment {
    public static GuideLaserCalibrationIntroFragment newInstance() {
        return new GuideLaserCalibrationIntroFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_guide_laser_calibration_intro;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_guide_laser_calibration_intro_get_started)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((GuideRotaryLaserActivity) getActivity()).startSetWorkpieceFragment();
        }
    }
}
