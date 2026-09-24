package fabscreen.features.machinetools.calibration.s20.laser;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.calibration.s20.laser.autofocus.LaserCalibrationAutoFocusStep1Fragment;
import fabscreen.features.machinetools.calibration.s20.laser.autofocus.LaserCalibrationAutoFocusStep2Fragment;
import fabscreen.features.machinetools.calibration.s20.laser.common.LaserCalibrationMeasureHeightFragment;
import fabscreen.features.machinetools.calibration.s20.laser.common.LaserCalibrationPickFragment;
import fabscreen.features.machinetools.calibration.s20.laser.common.LaserCalibrationSafetyGogglesFragment;
import fabscreen.features.machinetools.calibration.s20.laser.common.LaserCalibrationSetMaterialThicknessFragment;
import fabscreen.features.machinetools.calibration.s20.laser.common.LaserCalibrationSetOriginFragment;
import fabscreen.features.machinetools.calibration.s20.laser.manualfocus.LaserCalibrationCoarseTuningFragment;
import fabscreen.features.machinetools.calibration.s20.laser.manualfocus.LaserCalibrationManualFocusStep1Fragment;
import fabscreen.features.machinetools.calibration.s20.laser.manualfocus.LaserCalibrationManualFocusStep2Fragment;
import fabscreen.features.machinetools.calibration.s20.laser.rotary.LaserCalibration4AxisAutoFocusStep1Fragment;
import fabscreen.features.machinetools.calibration.s20.laser.rotary.LaserCalibration4AxisAutoFocusStep2Fragment;
import fabscreen.features.machinetools.calibration.s20.laser.rotary.LaserCalibration4AxisCoarseTuningFragment;
import fabscreen.features.machinetools.calibration.s20.laser.rotary.LaserCalibration4AxisInstallMaterialFragment;
import fabscreen.features.machinetools.calibration.s20.laser.rotary.LaserCalibration4AxisManualFocusStep1Fragment;
import fabscreen.features.machinetools.calibration.s20.laser.rotary.LaserCalibration4AxisManualFocusStep2Fragment;
import fabscreen.features.machinetools.calibration.s20.laser.rotary.LaserCalibration4AxisMeasureHeightFragment;
import fabscreen.features.machinetools.calibration.s20.laser.rotary.LaserCalibration4AxisPickFragment;
import fabscreen.features.machinetools.calibration.s20.laser.rotary.LaserCalibration4AxisSetOriginFragment;
import fabscreen.features.machinetools.calibration.s20.laser.rotary.LaserCalibration4AxisSetWorkpieceFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseActivity;
import fabscreen.platform.core.ui.presenter.TextFragment;

@Route(path = RoutePath.TOOLS_CALIBRATION_S20_LASER)
public class CalibrationLaserActivity extends BaseActivity {
    MachineInfo mMachineInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            setContentView(R.layout.activity_default);
            mMachineInfo = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue();
//            int headType = mMachineInfo.workType;
//            initRootFragment(headType);
        }
    }

    private void initRootFragment(int headType) {
        Fragment fragment = null;
        switch (headType) {
            case Module.ModuleType.HEAD_UNPLUGGED: {
                TextFragment fragment1 = new TextFragment();
                fragment1.setTitle(getResources().getString(R.string.all_calibration));
                fragment1.setText(getResources().getString(R.string.calibration_tool_head_not_detected));
                fragment = fragment1;
                break;
            }
            case Module.ModuleType.HEAD_LASER: {
                if (mMachineInfo.isRotaryAvailable) {
                    gotoLaserCalibration4AxisSetWorkpiece();
                } else {
                    gotoSetMaterialThicknessFragment();
                }

                break;
            }
            case Module.ModuleType.HEAD_CNC: {
                TextFragment fragment1 = new TextFragment();
                fragment1.setTitle(getResources().getString(R.string.all_calibration));
                fragment1.setText(getResources().getString(R.string.cnc_calibration_description));
                fragment = fragment1;
                break;
            }
        }

        if (fragment != null) {
            replaceFragment(R.id.fragment_container, fragment);
        }
    }

    /*
     * Laser Focus
     * <p>
     * Auto Mode
     * <p>
     * Manual Mode:
     * 1. Set Material Thickness (move Z axis)
     * 2. Manual coarse tuning
     * 3. Safety Goggles (start engrave test)
     * 4. Manual pick line
     */

    /**
     * Set Material Thickness
     * <p>
     * Auto Mode step 1
     * Manual Mode step 1
     */
    public void gotoSetMaterialThicknessFragment() {
        addFragment(R.id.fragment_container, LaserCalibrationSetMaterialThicknessFragment.newInstance());
    }

    /**
     * Measure Height
     * <p>
     * Auto Mode step 2
     * Manual Mode step 2
     */
    public void gotoMeasureHeightFragment() {
        addFragment(R.id.fragment_container, LaserCalibrationMeasureHeightFragment.newInstance());
    }

    /**
     * Safety Goggles
     * <p>
     * Auto Mode step 3
     * Manual Mode step 3
     */
    public void gotoSafetyGogglesFragment() {
        addFragment(R.id.fragment_container, LaserCalibrationSafetyGogglesFragment.newInstance());
    }

    /**
     * Set Origin
     * <p>
     * Auto Mode step 4
     */
    public void gotoLaserCalibrationSetOriginFragment() {
        addFragment(R.id.fragment_container, LaserCalibrationSetOriginFragment.newInstance());
    }

    /**
     * Auto Fine Tune
     * <p>
     * Auto Mode step 5
     */
    public void startAutoFocusStep1Fragment() {
        addFragment(R.id.fragment_container, LaserCalibrationAutoFocusStep1Fragment.newInstance());
    }

    /**
     * Auto Fine Tune
     * <p>
     * Auto Mode step 6
     */
    public void startAutoFocusStep2Fragment() {
        addFragment(R.id.fragment_container, LaserCalibrationAutoFocusStep2Fragment.newInstance());
    }

    /**
     * Coarse Tuning
     * <p>
     * Manual Mode step 4
     */
    public void gotoCoarseTuningFragment() {
        addFragment(R.id.fragment_container, LaserCalibrationCoarseTuningFragment.newInstance());
    }

    public void startManualFocusStep1Fragment() {
        addFragment(R.id.fragment_container, LaserCalibrationManualFocusStep1Fragment.newInstance());
    }

    /**
     * Manual Fine Tune
     * <p>
     * Manual Mode step 5
     */
    public void gotoManualFocusStep2Fragment() {
        LaserCalibrationManualFocusStep2Fragment fragment = new LaserCalibrationManualFocusStep2Fragment();
        addFragment(R.id.fragment_container, fragment);
    }

    /**
     * Manual Fine Tune Pick
     * <p>
     * Manual Mode step 6
     */
    public void gotoLaserCalibrationManualFineTunePick() {
        LaserCalibrationPickFragment fragment = new LaserCalibrationPickFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    // 4Axis

    /**
     * 4Axis Set Workpiece
     * <p>
     * 4Axis Auto Mode step 1
     * 4Axis Manual Mode step 1
     */
    public void gotoLaserCalibration4AxisSetWorkpiece() {
        LaserCalibration4AxisSetWorkpieceFragment fragment = new LaserCalibration4AxisSetWorkpieceFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    /**
     * 4Axis Tighten Material
     * <p>
     * 4Axis Auto Mode step 2
     * 4Axis Manual Mode step 2
     */
    public void gotoLaser4AxisInstallMaterial() {
        LaserCalibration4AxisInstallMaterialFragment fragment = new LaserCalibration4AxisInstallMaterialFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoLaser4AxisMeasureHeight() {
        LaserCalibration4AxisMeasureHeightFragment fragment = new LaserCalibration4AxisMeasureHeightFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    /**
     * 4Axis Coarse Turning
     * <p>
     * 4Axis Manual Mode step 5
     */
    public void gotoLaser4AxisCoarseTurning() {
        addFragment(R.id.fragment_container, LaserCalibration4AxisCoarseTuningFragment.newInstance());
    }

    /**
     * 4Axis Set Origin
     * <p>
     * 4Axis Auto Mode step 5
     * 4Axis Manual Mode step 6
     */
    public void gotoLaser4AxisSetOrigin() {
        addFragment(R.id.fragment_container, LaserCalibration4AxisSetOriginFragment.newInstance());
    }

    /**
     * 4Axis Auto Fine Tune
     * <p>
     * 4Axis Auto Mode step 6
     */
    public void start4AxisAutoFocusStep1Fragment() {
        addFragment(R.id.fragment_container, LaserCalibration4AxisAutoFocusStep1Fragment.newInstance());
    }

    /**
     * 4Axis Auto Fine Tune
     * <p>
     * 4Axis Auto Mode step 7
     */
    public void start4AxisAutoFocusStep2Fragment() {
        addFragment(R.id.fragment_container, LaserCalibration4AxisAutoFocusStep2Fragment.newInstance());
    }


    public void start4AxisManualFocusStep1Fragment() {
        addFragment(R.id.fragment_container, LaserCalibration4AxisManualFocusStep1Fragment.newInstance());
    }

    /**
     * Manual Fine Tune
     * <p>
     * 4Axis Manual Mode step 5
     */
    public void goto4AxisManualFocusStep2Fragment() {
        LaserCalibration4AxisManualFocusStep2Fragment fragment = new LaserCalibration4AxisManualFocusStep2Fragment();
        addFragment(R.id.fragment_container, fragment);
    }

    /**
     * 4Axis Manual Pick
     * <p>
     * 4Axis Manual Mode step 8
     */
    public void goto4AxisManualFineTunePick() {
        LaserCalibration4AxisPickFragment fragment = new LaserCalibration4AxisPickFragment();
        addFragment(R.id.fragment_container, fragment);
    }
}
