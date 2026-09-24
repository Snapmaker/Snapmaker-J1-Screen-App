package fabscreen.features.machinetools.calibration.a400platform.laser.w_1_6.rotary;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fabscreen.features.machinetools.calibration.a400platform.laser.BaseA400CalibrationCompleteFragment;

public class CentralAxisCalibrationCompleteFragment extends BaseA400CalibrationCompleteFragment {
    public static Fragment newInstance() {
        return new CentralAxisCalibrationCompleteFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setCompleteTitle("3-1 Completed");
        setCompleteContent("Congrats! You have completed the Central Axis Calibration.");
        playProcedureCompleteSound();
    }
}
