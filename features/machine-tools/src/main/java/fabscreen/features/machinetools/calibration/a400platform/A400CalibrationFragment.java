package fabscreen.features.machinetools.calibration.a400platform;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.calibration.a400platform.cnc.manualTool.A400ManualToolFragment;
import fabscreen.features.machinetools.calibration.a400platform.cnc.originAssistant.new_ui.A400OriginAssistantInfoFragment;
import fabscreen.features.machinetools.calibration.a400platform.cnc.toolchange.A400ToolChangeAssistantInfoFragment;
import fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingBed.A400LevelingBedCalibrationInfoFragment;
import fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingXY.A400LevelingXYCalibrationInfoFragment;
import fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingZ.A400LevelingZCalibrationInfoFragment;
import fabscreen.features.machinetools.calibration.a400platform.laser.w_10.cameraCalibration.A400CameraCalibration10wInfoFragment;
import fabscreen.features.machinetools.calibration.a400platform.laser.w_10.platformHeight.A400PlatformHeightInfoFragment;
import fabscreen.features.machinetools.calibration.a400platform.laser.w_10.thicknessMeasurement.A400ThicknessMeasurementInfoFragment;
import fabscreen.features.machinetools.calibration.a400platform.laser.w_1_6.ManualFocusCalibrationIntroFragment;
import fabscreen.features.machinetools.calibration.a400platform.laser.w_1_6.rotary.A400CentralAxisCalibrationIntroFragment;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.service.machine.controller.PrintController;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.core.R2;
import fabscreen.platform.core.ui.common.leftsection.A400LeftSectionsAdapter;
import fabscreen.platform.core.ui.common.leftsection.LeftSectionsAdapter;
import fabscreen.platform.core.ui.common.leftsection.SectionAndDetailContainerFragment;
import fabscreen.platform.core.ui.common.leftsection.SectionItem;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class A400CalibrationFragment extends SectionAndDetailContainerFragment {
    private static final int STATUS_IDLE = 0;

    @BindView(R2.id.view_transparent_mask)
    public View mViewTransparentMask;

    IMachine service;
    IMachine.WorkType workType;
    private int mHeadType;

    public static Fragment newInstance() {
        return new A400CalibrationFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ServiceContainer.getInstance().getService(IMachine.class)
                .getMachineStatusSubjectHolder()
                .getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(machineStatus -> {
                    mViewTransparentMask.setVisibility(machineStatus.status == STATUS_IDLE ? View.INVISIBLE : View.VISIBLE);
                }, LogHelper::log);
        service = getServiceContainer().getService(IMachine.class);
        workType = service.getMachineInfoSubjectHolder().getValue().workType;
        switch (workType) {
            case FDM:
                mHeadType = service.getFDMController().getHeadType();
                break;
            case CNC:
                mHeadType = service.getCNCController().getHeadType();
                break;
            case LASER:
                mHeadType = service.getLaserController().getHeadType();
                break;
            case NONE:
            default:
                mHeadType = -1;
        }
    }

    @Override
    protected List<SectionItem> getLeftSections() {
        List<SectionItem> items = new ArrayList<>();
        switch (workType) {
            case FDM:
                switch (mHeadType) {
                    case Module.ModuleType.HEAD_3DP:
                        items.add(new SectionItem(requireContext(), R.drawable.select_a400_calibration_heated_bed_leveing, R.string.calibration_heated_bed_leveing_title, A400LevelingBedCalibrationInfoFragment.newInstance()));
                        break;
                    case Module.ModuleType.HEAD_3DP_DOUBLE_EXTRUDER:
                        items.add(new SectionItem(requireContext(), R.drawable.select_a400_calibration_heated_bed_leveing, R.string.calibration_heated_bed_leveing_title, A400LevelingBedCalibrationInfoFragment.newInstance()));
                        items.add(new SectionItem(requireContext(), R.drawable.select_a400_calibration_z_offset_calibration, R.string.calibration_Z_offset_calibration_title, A400LevelingZCalibrationInfoFragment.newInstance()));
                        items.add(new SectionItem(requireContext(), R.drawable.select_a400_calibration_xy_offset_calibration, R.string.calibration_XY_offset_calibration_title, A400LevelingXYCalibrationInfoFragment.newInstance()));
                        break;
                    default:
                }
                break;
            case CNC:
                setTitle(R.string.all_tool);
                switch (mHeadType) {
                    case Module.ModuleType.HEAD_CNC:
                    case Module.ModuleType.HEAD_CNC_200W:
                        if (service.getMachineInfoSubjectHolder().getValue().isRotaryAvailable) {
                            items.add(new SectionItem(requireContext(), R.drawable.select_a400_calibration_origin, R.string.calibration_cnc_origin_assistant, A400OriginAssistantInfoFragment.newInstance()));
                        } else {
                            items.add(new SectionItem(requireContext(), R.drawable.select_a400_calibration_cnc_manual_tool, R.string.calibration_cnc_manual_tool, A400ManualToolFragment.newInstance()));
                        }
                        items.add(new SectionItem(requireContext(), R.drawable.select_a400_calibration_cnc_tool_change, R.string.calibration_cnc_tool_change, A400ToolChangeAssistantInfoFragment.newInstance()));
                        break;
                    default:
                }
                break;
            case LASER:
                switch (mHeadType) {
                    case Module.ModuleType.HEAD_LASER:
                        if (service.getMachineInfoSubjectHolder().getValue().isRotaryAvailable) {
                            items.add(new SectionItem(requireContext(), R.drawable.select_a400_calibration_central_axis, R.string.calibration_central_axis_title, A400CentralAxisCalibrationIntroFragment.newInstance()));
                            items.add(new SectionItem(requireContext(), R.string.calibration_manual_focus_calibration_title, ManualFocusCalibrationIntroFragment.newInstance()));
                        } else {
                            items.add(new SectionItem(requireContext(), R.string.calibration_platfrom_height_title, A400PlatformHeightInfoFragment.newInstance()));
                            items.add(new SectionItem(requireContext(), R.string.calibration_camera_calibration_title, A400CameraCalibration10wInfoFragment.newInstance()));
                            items.add(new SectionItem(requireContext(), R.string.calibration_manual_focus_calibration_title, ManualFocusCalibrationIntroFragment.newInstance()));
                        }
                        break;
                    case Module.ModuleType.HEAD_LASER_10W:
                        if (service.getMachineInfoSubjectHolder().getValue().isRotaryAvailable) {
                            items.add(new SectionItem(requireContext(), R.drawable.select_a400_calibration_central_axis, R.string.calibration_central_axis_title, A400CentralAxisCalibrationIntroFragment.newInstance()));
                        } else {
                            items.add(new SectionItem(requireContext(), R.drawable.select_a400_thickness_measurement, R.string.calibration_thickness_measurement_title_abbreviation, A400ThicknessMeasurementInfoFragment.newInstance()));
                            items.add(new SectionItem(requireContext(), R.drawable.select_a400_platfrom_height, R.string.calibration_platfrom_height_title, A400PlatformHeightInfoFragment.newInstance()));
                            items.add(new SectionItem(requireContext(), R.drawable.select_a400_camera_calibration, R.string.calibration_camera_calibration_title, A400CameraCalibration10wInfoFragment.newInstance()));
                        }
                        break;
                    default:
                }
                break;
            case NONE:
            default:
                mHeadType = -1;
        }

        return items;
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_section_and_detail_container;
    }

    @Override
    protected LeftSectionsAdapter getSectionsAdapter(List<SectionItem> sectionItems) {
        return new A400LeftSectionsAdapter(sectionItems);
    }

    @Override
    protected String getTitle() {
        return workType == IMachine.WorkType.CNC ? getString(R.string.all_tool) : getString(R.string.all_calibration);
    }

    @OnClick(R2.id.view_transparent_mask)
    public void onClickMask() {
        playNormalClickSound();
        MachineStatus status = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue();
        boolean isIdle = status.status == PrintController.STATE_IDLE;
        boolean isPrint = status.status <= 10;
        boolean is3DP = getServiceContainer().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType == IMachine.WorkType.FDM;
        String workName = "";
        if (isPrint) {
            workName = getServiceContainer().getService(IPrintWorkspace.class).getFileName();
        } else {
            workName = getString(R.string.all_calibration);
        }
        DecisionDialog.create(getContext())
                .setType(DecisionDialog.WARMING_TYPE)
                .setTitle(getString(R.string.all_stop) + getString(isPrint && is3DP ? R.string.printing_toast_3dp_print : R.string.printing_toast_other))
                .setContent(getString(R.string.assistant_back_notice, workName))
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, true)
                .setPic(R.drawable.pic_a400_dialog_failed_72x72)
                .setFirstTv(getContext().getResources().getString(R.string.all_cancel), R.color.select_dialog_white_txt, ((dialog, which) -> {
                    dialog.dismiss();
                }))
                .setSecondTv(getContext().getResources().getString(R.string.all_yes), R.color.select_dialog_red_txt, ((dialog, which) -> {
                    ServiceContainer.getInstance().getService(IMachine.class).getPrintController().NonPrintStop().as(bindToLifecycle()).subscribe();
                    exitCalibration();
                    dialog.dismiss();
                })).show();
    }

    public void exitCalibration() {
        try {
            Observable<ResponseStructure> responseStructureObservable = null;
            IMachine.WorkType workType = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType;
            switch (workType) {
                case FDM:
                    responseStructureObservable = ServiceContainer.getInstance().getService(IMachine.class).getFDMController().exitCalibration(false);
                    break;
                case LASER:
                    responseStructureObservable = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().exitCalibration(false);
                    break;
                case CNC:
                    responseStructureObservable = ServiceContainer.getInstance().getService(IMachine.class).getCNCController().exitCalibration(false);
                    break;
            }
            if (responseStructureObservable == null) return;
            responseStructureObservable
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(success -> {
                        if (!success.isSuccess()) {
                            Logger.d("Exit Calibration: " + success);
                        }
                    }, LogHelper::log);
        } catch (Exception e) {
            LogHelper.log(e);
        }
    }
}
