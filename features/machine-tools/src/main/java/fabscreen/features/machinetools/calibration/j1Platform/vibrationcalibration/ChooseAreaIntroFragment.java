package fabscreen.features.machinetools.calibration.j1Platform.vibrationcalibration;

import androidx.fragment.app.Fragment;

import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.J1CalibrationBaseFragment;

public class ChooseAreaIntroFragment extends J1CalibrationBaseFragment {
    public static Fragment newInstance() {
        return new ChooseAreaIntroFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_calibration_vibration_choose_area_intro;
    }

    @OnClick(R2.id.btn_next)
    void onNextClicked() {
        if (requireActivity() instanceof VibrationCalibrationActivity) {
            ((VibrationCalibrationActivity) requireActivity()).goToChooseYArea();
        }
    }
}
