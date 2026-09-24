package fabscreen.features.machinetools.calibration.a400platform.laser.w_1_6;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fabscreen.features.machinetools.calibration.a400platform.laser.BaseA400CalibrationCompleteFragment;

public class ManualFocusCalibrationCompleteFragment extends BaseA400CalibrationCompleteFragment {

    public static Fragment newInstance() {
        return new ManualFocusCalibrationCompleteFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setCompleteTitle("5-1  Completed");
        setCompleteContent("Congrats! You have completed the Manual Focus Calibration.");
        playProcedureCompleteSound();
    }
}
