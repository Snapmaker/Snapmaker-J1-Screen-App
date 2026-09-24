package fabscreen.features.settings.a400.maintenance.machineinfo;

import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_AIR_PURIFIER;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_DRY_BOX;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_ENCLOSURE;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ADDON_ENCLOSURE_A400;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP_DOUBLE_EXTRUDER;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_CNC;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_CNC_200W;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_LASER;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_LASER_10W;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.LINEAR_A400;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.ROTARY_MODULE;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import java.util.List;

import butterknife.BindView;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.entity.module.AirPurifier;
import fabscreen.platform.base.service.machine.entity.module.DryBox;
import fabscreen.platform.base.service.machine.entity.module.Enclosure;
import fabscreen.platform.base.service.machine.entity.parts.LinearLimit;
import fabscreen.platform.base.service.machine.entity.toolhead.CNCToolhead;
import fabscreen.platform.base.service.machine.entity.toolhead.FdmToolhead;
import fabscreen.platform.base.service.machine.entity.toolhead.LaserToolhead;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class A400MachineInfoFragment extends BaseFragment {
    private static final String TAG = "A400MachineInfoFragment";

    @BindView(R2.id.ll_module_dual_extrusion)
    LinearLayout mLlModuleDualExtrusion;
    @BindView(R2.id.miiv_dual_nozzle_temp_l)
    MachineInfoItemView mMiivNozzleTempL;
    @BindView(R2.id.miiv_dual_nozzle_temp_r)
    MachineInfoItemView mMiivNozzleTempR;
    @BindView(R2.id.miiv_filament_detect_l)
    MachineInfoItemView mMiivFilamentDetectL;
    @BindView(R2.id.miiv_filament_detect_r)
    MachineInfoItemView mMiivFilamentDetectR;
    @BindView(R2.id.miiv_cooling_l)
    MachineInfoItemView mMiivCoolingL;
    @BindView(R2.id.miiv_cooling_r)
    MachineInfoItemView mMiivCoolingR;
    @BindView(R2.id.miiv_heat_dissipation_dual)
    MachineInfoItemView mMiivHeatDissipationDual;
    @BindView(R2.id.ll_module_single_extrusion)
    LinearLayout mLlModuleSingleExtrusion;
    @BindView(R2.id.miiv_single_nozzle_temp)
    MachineInfoItemView mMiivSingleNozzleTemp;
    @BindView(R2.id.miiv_filament_detect)
    MachineInfoItemView mMiivFilamentDetect;
    @BindView(R2.id.miiv_cooling)
    MachineInfoItemView mMiivCooling;
    @BindView(R2.id.miiv_dissipation_single)
    MachineInfoItemView mMiivDissipationSingle;
    @BindView(R2.id.ll_module_laser)
    LinearLayout mLlModuleLaser;
    @BindView(R2.id.miiv_laser_power)
    MachineInfoItemView mMiivLaserPower;
    @BindView(R2.id.miiv_orientation_detect)
    MachineInfoItemView mMiivOrientationDetect;
    @BindView(R2.id.miiv_laser_emitter_temp)
    MachineInfoItemView mMiivLaserEmitterTemp;
    @BindView(R2.id.miiv_camera_connection)
    MachineInfoItemView mMiivCameraConnection;
    @BindView(R2.id.ll_module_cnc)
    LinearLayout mLlModuleCNC;
    @BindView(R2.id.miiv_cnc_speed)
    MachineInfoItemView mMiivCncSpeed;
    @BindView(R2.id.ll_module_rotary)
    LinearLayout mLlModuleRotary;
    @BindView(R2.id.ll_module_dryer)
    LinearLayout mLlModuleDryer;
    @BindView(R2.id.miiv_dryer_temp)
    MachineInfoItemView mMiivDryerTemp;
    @BindView(R2.id.miiv_dryer_rh)
    MachineInfoItemView mMiivDryerRH;
    @BindView(R2.id.miiv_dryer_fan)
    MachineInfoItemView mMiivDryerFan;
    @BindView(R2.id.miiv_dryer_power)
    MachineInfoItemView mMiivDryerIsDrying;
    @BindView(R2.id.ll_module_enclosure)
    LinearLayout mLlModuleEnclosure;
    @BindView(R2.id.miiv_enclosure_led)
    MachineInfoItemView mMiivEnclosureLed;
    @BindView(R2.id.miiv_enclosure_fan)
    MachineInfoItemView mMiivEnclosureFan;
    @BindView(R2.id.miiv_enclosure_door)
    MachineInfoItemView mMiivEnclosureDoor;
    @BindView(R2.id.ll_module_purifier)
    LinearLayout mLlModulePurifier;
    @BindView(R2.id.miiv_purifier_fan)
    MachineInfoItemView mMiivPurifierFan;
    @BindView(R2.id.miiv_purifier_cover)
    MachineInfoItemView mMiivPurifierCover;
    @BindView(R2.id.miiv_purifier_filter_detection)
    MachineInfoItemView mMiivPurifierFilterDetection;
    @BindView(R2.id.liv_module_x)
    LinearInfoView mLivModuleX;
    @BindView(R2.id.liv_module_y1)
    LinearInfoView mLivModuleY1;
    @BindView(R2.id.liv_module_y2)
    LinearInfoView mLivModuleY2;
    @BindView(R2.id.liv_module_z1)
    LinearInfoView mLivModuleZ1;
    @BindView(R2.id.liv_module_z2)
    LinearInfoView mLivModuleZ2;

    private A400MachineInfoViewModel mViewModel;

    public static Fragment newInstance() {
        return new A400MachineInfoFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_maintenance_machine_info;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getFragmentScopeViewModel(A400MachineInfoViewModel.class);
        initView();
    }

    private void initView() {
        setTitle("Machine Information");
        showModules();
    }

    private void showModules() {
        mViewModel.getModuleListObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::showDetectedModules, LogHelper::log);

        mViewModel.getFdmToolheadObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::refreshFdmToolheadStatus, LogHelper::log);

        mViewModel.getLaserToolheadObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::refreshLaserToolheadStatus, LogHelper::log);

        mViewModel.getLaserSafetyInfoObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(safetyInfo -> mMiivLaserEmitterTemp.setContent(safetyInfo.getTubeTemperature() + "℃"), LogHelper::log);

        mViewModel.getLaserCameraOnlineObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(online -> mMiivCameraConnection.setContent(online ? "Online" : "Offline"));

        mViewModel.getCncToolheadObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::refreshCncToolheadStatus, LogHelper::log);

        mViewModel.getDryerObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::refreshDryerStatus, LogHelper::log);

        mViewModel.getAirPurifierObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::refreshAirPurifierStatus, LogHelper::log);

        mViewModel.getEnclosureObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::refreshEnclosureStatus, LogHelper::log);

        mViewModel.getLinearLimitObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::refreshLinearStatus, LogHelper::log);
    }

    private void refreshLinearStatus(List<LinearLimit> linearLimits) {
        for (LinearLimit limit : linearLimits) {
            switch (limit.getIndex()) {
                case 0:
                    mLivModuleX.setAttributeContent(limit.getTrigger() ? "On" : "Off");
                    break;
                case 1:
                    mLivModuleY1.setAttributeContent(limit.getTrigger() ? "On" : "Off");
                    break;
                case 2:
                    mLivModuleZ1.setAttributeContent(limit.getTrigger() ? "On" : "Off");
                    break;
                case 4:
                    mLivModuleY2.setAttributeContent(limit.getTrigger() ? "On" : "Off");
                    break;
                case 5:
                    mLivModuleZ2.setAttributeContent(limit.getTrigger() ? "On" : "Off");
                    break;
            }
        }
    }

    private void refreshEnclosureStatus(Enclosure.EnclosureStatus status) {
        mMiivEnclosureLed.setContent(status.getLedValue() + "(PWM)");
        mMiivEnclosureFan.setContent(status.getFanSpeed() + "(PWM)");
        mMiivEnclosureDoor.setContent(status.isDoorOpen() ? "Open" : "Closed");
    }

    private void refreshAirPurifierStatus(AirPurifier.AirPurifierStatus status) {
        mMiivPurifierFan.setContent(status.getFanSpeedLevel() + "(PWM)");
        mMiivPurifierCover.setContent("Open(Mock)");
        mMiivPurifierFilterDetection.setContent("On(Mock)");
    }

    private void refreshDryerStatus(DryBox.DryBoxInfo info) {
        mMiivDryerTemp.setContent(info.getDryBoxStatus().getTempCurrentChamber() + "℃");
        mMiivDryerRH.setContent(info.getDryBoxStatus().getCurrentHumidity() + "%");
        mMiivDryerIsDrying.setContent(info.getDryBoxStatus().getDryState() == 1 ? "On" : "Off");
    }

    private void refreshCncToolheadStatus(CNCToolhead.CNCToolheadInfo info) {
        mMiivCncSpeed.setContent(info.getCurrentSpeed() + "rpm");
    }

    private void refreshLaserToolheadStatus(LaserToolhead.LaserToolheadInfo info) {
//        Logger.d("Laser tube info: %s", info);
        mMiivLaserPower.setContent(info.getLaserTube().getCurrentPower() + "(PWM)");
        mMiivOrientationDetect.setContent("On");
        mMiivCameraConnection.setContent("Online");
    }

    private void refreshFdmToolheadStatus(FdmToolhead.FdmToolheadStatus status) {
        // TODO: 2022/7/7 fan should get with type
        Logger.d("FDM status: %s", status);
        if (mLlModuleDualExtrusion.getVisibility() == View.VISIBLE) {
            mMiivNozzleTempL.setContent(status.getExtruderList().get(0).getTemperature() + "℃");
            mMiivNozzleTempR.setContent(status.getExtruderList().get(1).getTemperature() + "℃");
            mMiivFilamentDetectL.setContent(status.getExtruderList().get(0).getFilamentDetectionStatus() == 1 ? "On" : "Off");
            mMiivFilamentDetectR.setContent(status.getExtruderList().get(1).getFilamentDetectionStatus() == 1 ? "On" : "Off");
            mMiivCoolingL.setContent(status.getFanList().get(0).getSpeedLevel() + "(PWM)");
            mMiivCoolingR.setContent(status.getFanList().get(1).getSpeedLevel() + "(PWM)");
            mMiivHeatDissipationDual.setContent(status.getFanList().get(2).getSpeedLevel() + "(PWM)");
        } else if (mLlModuleSingleExtrusion.getVisibility() == View.VISIBLE) {
            mMiivSingleNozzleTemp.setContent(status.getExtruderList().get(0).getTemperature() + "℃");
            mMiivFilamentDetect.setContent(status.getExtruderList().get(0).getFilamentDetectionStatus() == 1 ? "On" : "Off");
            mMiivCooling.setContent(status.getFanList().get(0).getSpeedLevel() + "(PWM)");
            if (status.getFanList().size() > 1) {
                mMiivDissipationSingle.setContent(status.getFanList().get(1).getSpeedLevel() + "(PWM)");
            }
        }
    }

    private void showDetectedModules(List<Module> modules) {
        for (Module module : modules) {
            switch (module.getModuleInfo().getModuleId()) {
                case HEAD_3DP:
                    show3dpSingleStats();
                    break;
                case HEAD_3DP_DOUBLE_EXTRUDER:
                    show3dpDualStats();
                    break;
                case HEAD_LASER:
                case HEAD_LASER_10W:
                    showLaserStats();
                    break;
                case HEAD_CNC:
                case HEAD_CNC_200W:
                    showCncStats();
                    break;
                case ROTARY_MODULE:
                    mLlModuleRotary.setVisibility(View.VISIBLE);
                    break;
                case ADDON_DRY_BOX:
                    showDryerStats();
                    break;
                case ADDON_ENCLOSURE:
                case ADDON_ENCLOSURE_A400:
                    showEnclosureStats();
                    break;
                case ADDON_AIR_PURIFIER:
                    showAirPurifierStats();
                    break;
                case LINEAR_A400:
                    handleLinearModuleVisibility(module.getModuleInfo().getModuleIndex());
                    break;
            }
        }
    }

    private void showAirPurifierStats() {
        mLlModulePurifier.setVisibility(View.VISIBLE);
        mMiivPurifierFan.setTitle("Fan Speed");
        mMiivPurifierCover.setTitle("Cover");
        mMiivPurifierFilterDetection.setTitle("Filter Detection");
    }

    private void showEnclosureStats() {
        mLlModuleEnclosure.setVisibility(View.VISIBLE);
        mMiivEnclosureLed.setTitle("LED Strip");
        mMiivEnclosureFan.setTitle("Exhaust Fan");
        mMiivEnclosureDoor.setTitle("Enclosure Door");
    }

    private void showDryerStats() {
        mLlModuleDryer.setVisibility(View.VISIBLE);
        mMiivDryerTemp.setTitle("Temperature");
        mMiivDryerRH.setTitle("Relative Humidity (RH)");
        mMiivDryerFan.setTitle("Heat Circulation Fan");
        mMiivDryerIsDrying.setTitle("Drying");
    }

    private void showCncStats() {
        mLlModuleCNC.setVisibility(View.VISIBLE);
        mMiivCncSpeed.setTitle("Spindle Speed");
    }

    private void showLaserStats() {
        mLlModuleLaser.setVisibility(View.VISIBLE);
        mMiivLaserPower.setTitle("Laser Power");
        mMiivOrientationDetect.setTitle("Orientation Detect.");
        mMiivLaserEmitterTemp.setTitle("Laser Emitter Temp.");
        mMiivCameraConnection.setTitle("Camera Connection");
    }

    private void show3dpSingleStats() {
        mLlModuleSingleExtrusion.setVisibility(View.VISIBLE);
        mMiivSingleNozzleTemp.setTitle("Nozzle Temp.");
        mMiivFilamentDetect.setTitle("Filament Detect.");
        mMiivCooling.setTitle("Part-cooling Fan");
        mMiivDissipationSingle.setTitle("Heat Dissipation Fan");
    }

    private void show3dpDualStats() {
        mLlModuleDualExtrusion.setVisibility(View.VISIBLE);
        mMiivNozzleTempL.setTitle("Left Nozzle Temp.");
        mMiivNozzleTempR.setTitle("Right Nozzle Temp.");
        mMiivFilamentDetectL.setTitle("Filament Detect. (Nozzle L)");
        mMiivFilamentDetectR.setTitle("Filament Detect. (Nozzle R)");
        mMiivCoolingL.setTitle("Left Part-cooling Fan");
        mMiivCoolingR.setTitle("Right Part-cooling Fan");
        mMiivHeatDissipationDual.setTitle("Heat Dissipation Fan");
    }

    private void handleLinearModuleVisibility(int moduleIndex) {
        switch (moduleIndex) {
            case 0:
                mLivModuleX.setVisibility(View.VISIBLE);
                mLivModuleX.setLinearTitle("Linear Module X");
                break;
            case 1:
                mLivModuleY1.setVisibility(View.VISIBLE);
                mLivModuleY1.setLinearTitle("Linear Module Y1");
                break;
            case 2:
                mLivModuleZ1.setVisibility(View.VISIBLE);
                mLivModuleZ1.setLinearTitle("Linear Module Z1");
                break;
            case 4:
                mLivModuleY2.setVisibility(View.VISIBLE);
                mLivModuleY2.setLinearTitle("Linear Module Y2");
            case 5:
                mLivModuleZ2.setVisibility(View.VISIBLE);
                mLivModuleZ2.setLinearTitle("Linear Module Z2");
                break;
        }
    }
}
