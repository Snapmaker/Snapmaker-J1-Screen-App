package fabscreen.features.machinetools.calibration.j1Platform.levelingZ;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.calibration.j1Platform.CalibrationSuccessfullyFragment;
import fabscreen.features.machinetools.calibration.j1Platform.J1CalibrationCleanNozzleFragment;
import fabscreen.features.machinetools.calibration.j1Platform.J1CalibrationNozzleBedHeatingFragment;
import fabscreen.features.machinetools.calibration.j1Platform.J1CalibrationRemoveGlassFragment;
import fabscreen.features.machinetools.calibration.j1Platform.J1CalibrationRestoringMachineFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_CALIBRATION_J1_3DP_LEVELING_Z_AUXILIARY)
public class LevelingZAuxiliaryCalibrationActivity extends BaseActivity {
    private boolean isGuide = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        Intent intent = getIntent();
        isGuide = intent.getBooleanExtra("bool", false);
        gotoRemoveGlassPlate();
    }

    public void gotoRemoveGlassPlate() {
        if (isGuide) {
            gotoLoosenScrews();
        } else {
            addFragment(R.id.fragment_container, J1CalibrationRemoveGlassFragment.newInstance());
        }

    }

    public void gotoLoosenScrews() {
        addFragment(R.id.fragment_container, LevelingZCalibrationLoosenScrewsFragment.newInstance());
    }

    public void gotoNozzleBedHeating() {
        addFragment(R.id.fragment_container, J1CalibrationNozzleBedHeatingFragment.newInstance());
    }

    public void gotoCleanNozzle() {
        addFragment(R.id.fragment_container, J1CalibrationCleanNozzleFragment.newInstance());
    }

    public void gotoLevelingZAuxiliaryCalibration() {
        addFragment(R.id.fragment_container, LevelingZAuxiliaryCalibrationFragment.newInstance());
    }

    public void gotoRestoringMachine() {
        addFragment(R.id.fragment_container, J1CalibrationRestoringMachineFragment.newInstance());
    }

    public void gotoCalibrationSuccess() {
        if (isGuide) {
            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setGuideLevelingZ(true);
        }
        addFragment(R.id.fragment_container, CalibrationSuccessfullyFragment.newInstance());
    }

    @Override
    public void addFragment(int containerId, @NonNull Fragment fragment) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("is_guide", isGuide);
        fragment.setArguments(bundle);
        addFragment(null, containerId, fragment, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
    }


}
