package fabscreen.features.settings.a350.index;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class Setting10wCameraCalibrationIntroFragment extends BaseFragment {

    public static Setting10wCameraCalibrationIntroFragment newInstance() {
        return new Setting10wCameraCalibrationIntroFragment();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(R.string.laser_camera_calibration);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_laser_10w_camera_calibration_intro;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }


    @OnClick(R2.id.btn_start_10w_camera_calibration_intro)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() == null) return;
        ((SettingsActivity) getActivity()).goto10wCameraCalibrationStep1();
    }

}
