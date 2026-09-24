package fabscreen.features.machinetools.calibration.s20.laser._10w.camera;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.view.BaseFragment;

public class Laser10wLaserCameraCalibrationIntroFragment extends BaseFragment {
    public static Laser10wLaserCameraCalibrationIntroFragment newInstance() {
        return new Laser10wLaserCameraCalibrationIntroFragment();
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


    @OnClick(R2.id.btn_start_10w_camera_calibration_intro)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() == null) return;
        ((Laser10wCameraCalibrationActivity) getActivity()).gotoCameraCalibrationStep1();
    }

}
