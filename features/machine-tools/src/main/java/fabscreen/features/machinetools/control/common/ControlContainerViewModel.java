package fabscreen.features.machinetools.control.common;

import android.content.Context;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.control.a400.A400AirPurifierControlFragment;
import fabscreen.features.machinetools.control.a400.A400CNCControlFragment;
import fabscreen.features.machinetools.control.a400.A400DryBoxControlFragment;
import fabscreen.features.machinetools.control.a400.A400EnclosureControlFragment;
import fabscreen.features.machinetools.control.a400.A400FilamentControlFragment;
import fabscreen.features.machinetools.control.a400.A400HeatedBedControlFragment;
import fabscreen.features.machinetools.control.a400.A400JogControlFragment;
import fabscreen.features.machinetools.control.a400.A400LaserControlFragment;
import fabscreen.features.machinetools.control.j1.J1ControlLightingFragment;
import fabscreen.features.machinetools.control.j1.J1FilamentControlFragment;
import fabscreen.features.machinetools.control.j1.J1JogControlFragment;
import fabscreen.features.machinetools.control.j1.J1MotorControlFragment;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.common.leftsection.SectionItem;
import fabscreen.platform.core.ui.view.HelpBean;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class ControlContainerViewModel extends BaseViewModel {

    private final IMachine.WorkType mWorkType;
    private final MachineInfo mMachineInfo;

    private final BehaviorSubject<Boolean> mIsIdleSubj = BehaviorSubject.create();
    private final IMachine mMachine;
    private Context mContext;

    public ControlContainerViewModel() {
        mMachine = getServiceContainer().getService(IMachine.class);
        mMachineInfo = mMachine.getMachineInfoSubjectHolder().getValue();
        mWorkType = mMachineInfo.workType;
        mContext = getServiceContainer().getService(IAppService.class).getAppContext();
    }

    public List<HelpBean> getHelpList() {
        List<HelpBean> list = new ArrayList<>();
        list.add(new HelpBean(R.drawable.gif_help_content_1, R.string.j1_how_to_load_filament_step_1));
        list.add(new HelpBean(R.drawable.gif_help_content_2, R.string.j1_how_to_load_filament_step_2));
        list.add(new HelpBean(R.drawable.gif_help_content_3, R.string.j1_how_to_load_filament_step_3));
        list.add(new HelpBean(R.drawable.pic_help_content_4, R.string.j1_how_to_load_filament_step_4));
        list.add(new HelpBean(R.drawable.gif_help_content_5, R.string.j1_how_to_load_filament_step_5));
        list.add(new HelpBean(R.drawable.pic_help_content_6, R.string.j1_how_to_load_filament_step_6));
        list.add(new HelpBean(R.drawable.gif_help_content_7, R.string.j1_how_to_load_filament_step_7));
        return list;
    }

    public List<SectionItem> getLeftSections() {
        List<SectionItem> items = new ArrayList<>();

        if (mMachineInfo.seriesId == IMachine.MachineSeries.J) {
            // J series sections are fixed.
            items.add(new SectionItem(mContext.getString(R.string.j1_control_extruder_title), J1FilamentControlFragment.newInstance(0)));
            items.add(new SectionItem(mContext.getString(R.string.j1_control_move_title), J1JogControlFragment.newInstance()));
            items.add(new SectionItem(mContext.getString(R.string.j1_control_heated_bed_title), S30HeatedBedControlFragment.newInstance()));
            items.add(new SectionItem(mContext.getString(R.string.j1_control_motor_title), J1MotorControlFragment.newInstance()));
            items.add(new SectionItem(mContext.getString(R.string.j1_control_lighting_title), J1ControlLightingFragment.newInstance()));
        } else if (mMachineInfo.seriesId == IMachine.MachineSeries.A) {
            // A series section are dynamic.
            items.add(new SectionItem(mContext.getString(R.string.a400_control_move), R.drawable.select_control_move, A400JogControlFragment.newInstance()));

            if (mWorkType == IMachine.WorkType.FDM) {
                items.add(new SectionItem(mContext.getString(R.string.a400_control_filament), R.drawable.select_control_air_purifier, A400FilamentControlFragment.newInstance()));
            }

            if (mMachineInfo.isHeatedBedAvailable) {
                items.add(new SectionItem(mContext.getString(R.string.a400_control_heated_bed), R.drawable.select_control_heated_bed, A400HeatedBedControlFragment.newInstance()));
            }

            if (mMachineInfo.isEnclosureAvailable) {
                items.add(new SectionItem(mContext.getString(R.string.a400_control_enclosure), R.drawable.select_control_enclosure, A400EnclosureControlFragment.newInstance()));
            }

            if (mMachineInfo.isAirPurifierAvailable) {
                items.add(new SectionItem(mContext.getString(R.string.a400_control_air_purifier), R.drawable.select_control_air_purifier, A400AirPurifierControlFragment.newInstance()));
            }

            //todo missing normal pic
            if (mMachineInfo.isDryBoxAvailable) {
                items.add(new SectionItem(mContext.getString(R.string.a400_control_filament_dryer), R.drawable.select_control_filament_dryer, A400DryBoxControlFragment.newInstance()));
            }

            if (mWorkType == IMachine.WorkType.LASER) {
                items.add(new SectionItem(mContext.getString(R.string.a400_control_laser), R.drawable.select_control_laser, A400LaserControlFragment.newInstance()));
            }

            //todo missing normal pic
            if (mWorkType == IMachine.WorkType.CNC) {
                items.add(new SectionItem(mContext.getString(R.string.a400_control_spindle), R.drawable.select_control_spindle, A400CNCControlFragment.newInstance()));
            }
        }

        return items;
    }

    public Observable<MachineStatus> getMachineStatusObservable() {
        return mMachine.getMachineStatusSubjectHolder().getObservable();
    }

    public MachineStatus getMachineStatusValue() {
        return mMachine.getMachineStatusSubjectHolder().getValue();
    }

    public void stopWork() {
        try {
            mMachine.getPrintController().NonPrintStop().as(bindToLifecycle()).subscribe();
        } catch (Exception e) {
            LogHelper.log(e);
        }
    }

    @Override
    protected void onCleared() {
        mMachine.getMachineController().shutdownWorkingParts();
        super.onCleared();
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
