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

import java.util.List;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.entity.module.AirPurifier;
import fabscreen.platform.base.service.machine.entity.module.DryBox;
import fabscreen.platform.base.service.machine.entity.module.Enclosure;
import fabscreen.platform.base.service.machine.entity.parts.LinearLimit;
import fabscreen.platform.base.service.machine.entity.toolhead.CNCToolhead;
import fabscreen.platform.base.service.machine.entity.toolhead.FdmToolhead;
import fabscreen.platform.base.service.machine.entity.toolhead.LaserToolhead;
import fabscreen.platform.base.service.machine.structure.LaserSafetyStateStructure;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class A400MachineInfoViewModel extends BaseViewModel {

    private final IMachine mMachine;
    private final BehaviorSubject<List<Module>> mModuleListSubj = BehaviorSubject.create();
    private final PublishSubject<FdmToolhead.FdmToolheadStatus> mFdmToolheadStatusSubj = PublishSubject.create();
    private final PublishSubject<LaserToolhead.LaserToolheadInfo> mLaserToolheadInfoSubj = PublishSubject.create();
    private final PublishSubject<LaserSafetyStateStructure> mLaserSafetyInfoSubj = PublishSubject.create();
    private final PublishSubject<CNCToolhead.CNCToolheadInfo> mCncToolheadInfoSubj = PublishSubject.create();
    private final PublishSubject<Enclosure.EnclosureStatus> mEnclosureStatusSubj = PublishSubject.create();
    private final PublishSubject<AirPurifier.AirPurifierStatus> mAirPurifierStatusSubj = PublishSubject.create();
    private final PublishSubject<DryBox.DryBoxInfo> mDryBoxInfoSubj = PublishSubject.create();

    public A400MachineInfoViewModel() {
        mMachine = getServiceContainer().getService(IMachine.class);
        watchMachineInfo();
    }

    public Observable<List<Module>> getModuleListObservable() {
        return mModuleListSubj.hide();
    }

    public Observable<FdmToolhead.FdmToolheadStatus> getFdmToolheadObservable() {
        return mFdmToolheadStatusSubj.hide();
    }

    public Observable<LaserToolhead.LaserToolheadInfo> getLaserToolheadObservable() {
        return mLaserToolheadInfoSubj.hide();
    }

    public Observable<Boolean> getLaserCameraOnlineObservable() {
        return mMachine.getLaserController().getLaserCameraController().getBluetoothConnectedObservable();
    }

    public Observable<LaserSafetyStateStructure> getLaserSafetyInfoObservable() {
        return mLaserSafetyInfoSubj.hide();
    }

    public Observable<CNCToolhead.CNCToolheadInfo> getCncToolheadObservable() {
        return mCncToolheadInfoSubj.hide();
    }

    public Observable<Enclosure.EnclosureStatus> getEnclosureObservable() {
        return mEnclosureStatusSubj.hide();
    }

    public Observable<AirPurifier.AirPurifierStatus> getAirPurifierObservable() {
        return mAirPurifierStatusSubj.hide();
    }

    public Observable<DryBox.DryBoxInfo> getDryerObservable() {
        return mDryBoxInfoSubj.hide();
    }

    public Observable<List<LinearLimit>> getLinearLimitObservable() {
        return mMachine.getMachineController().getLinearLimitStateObservable();
    }

    private void watchMachineInfo() {
        // Get module list. The list determines what the layout will display
        MachineInfo cachedMachineInfo = mMachine.getMachineInfoSubjectHolder().getValue();
        List<Module> moduleList = cachedMachineInfo.moduleList;
        mModuleListSubj.onNext(moduleList);

        for (Module module : moduleList) {
            watchModuleData(module);
        }

        subscribeLinearLimits();
    }

    private void subscribeLinearLimits() {
        mMachine.getMachineController().subscribeLinearLimitStatus();
    }

    private void watchModuleData(Module module) {
        switch (module.getModuleInfo().getModuleId()) {
            case HEAD_3DP:
            case HEAD_3DP_DOUBLE_EXTRUDER:
                watchFdmData();
                mMachine.getFDMController().subscribeExtruderChange();
                mMachine.getFDMController().subscribeFanChange();
                break;
            case HEAD_LASER:
                watchLaserData();
                mMachine.getLaserController().subscribeLaserTubeStatus();
                break;
            case HEAD_LASER_10W:
                watchLaserData();
                mMachine.getLaserController().subscribeLaserTubeStatus();
                mMachine.getLaserController().subscribeLaserSafetyState();
                break;
            case HEAD_CNC:
            case HEAD_CNC_200W:
                watchCncData();
                mMachine.getCNCController().subscribeCNCInfo();
                break;
            case ADDON_ENCLOSURE:
            case ADDON_ENCLOSURE_A400:
                watchEnclosureData();
                mMachine.getMachineController().getEnclosure().subscribeEnclosureInfo();
                break;
            case ADDON_AIR_PURIFIER:
                watchPurifierData();
                mMachine.getMachineController().getAirPurifier().subscribeAirPurifierStatusChange();
                break;
            case ADDON_DRY_BOX:
                watchDryerData();
                mMachine.getMachineController().getDryBox().subscribeStatus();
                break;
            case LINEAR_A400:
//                mMachine.getMachineController().getLinearModule()
                break;

        }
    }

    private void watchDryerData() {
        mMachine.getMachineController().getDryBox().getDryBoxStatusHolder().getObservable()
                .as(bindToLifecycle())
                .subscribe(dryBoxInfo -> {
                    if (dryBoxInfo == null) return;
                    mDryBoxInfoSubj.onNext(dryBoxInfo);
                }, LogHelper::log);
    }

    private void watchPurifierData() {
        mMachine.getMachineController().getAirPurifier().getAirPurifierStatusObservable()
                .as(bindToLifecycle())
                .subscribe(airPurifierStatus -> {
                    if (airPurifierStatus == null) return;
                    mAirPurifierStatusSubj.onNext(airPurifierStatus);
                }, LogHelper::log);
    }

    private void watchEnclosureData() {
        mMachine.getMachineController().getEnclosure().getEnclosureStatusObservable()
                .as(bindToLifecycle())
                .subscribe(enclosureStatus -> {
                    if (enclosureStatus == null) return;
                    mEnclosureStatusSubj.onNext(enclosureStatus);
                }, LogHelper::log);
    }

    private void watchCncData() {
        mMachine.getCNCController().getCncToolHeadInfoObservable()
                .as(bindToLifecycle())
                .subscribe(cncToolheadInfo -> {
                    if (cncToolheadInfo == null) return;
                    mCncToolheadInfoSubj.onNext(cncToolheadInfo);
                }, LogHelper::log);
    }

    private void watchLaserData() {
        mMachine.getLaserController().getLaserToolHeadInfoObservable()
                .as(bindToLifecycle())
                .subscribe(toolheadInfo -> {
                    if (toolheadInfo == null) return;
                    mLaserToolheadInfoSubj.onNext(toolheadInfo);
                }, LogHelper::log);

        mMachine.getLaserController().getLaserToolhead().getLaserSafetyStateObservable()
                .as(bindToLifecycle())
                .subscribe(safetyInfo -> {
                    if (safetyInfo == null) return;
                    mLaserSafetyInfoSubj.onNext(safetyInfo);
                });
    }

    private void watchFdmData() {
        mMachine.getFDMController().getToolheadInfoObservable(0)
                .as(bindToLifecycle())
                .subscribe(toolHeadResponse -> {
                    if (toolHeadResponse == null) return;
                    FdmToolhead.FdmToolheadStatus dataProp = toolHeadResponse.dataProp;
                    if (dataProp == null) return;
                    mFdmToolheadStatusSubj.onNext(dataProp);
                }, LogHelper::log);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // TODO: 2022/7/14 Really need a subscriber to describe the subscribe and do the unsubscribe!
        // unsubscribe
        try {
            mMachine.getFDMController().unSubscribeExtruderChange();
        } catch (NullPointerException ignore) {
        }

        try {
            mMachine.getFDMController().unSubscribeFanChange();
        } catch (NullPointerException ignore) {
        }

        try {
            mMachine.getLaserController().unSubscribeLaserTubeStatus();
        } catch (NullPointerException ignore) {
        }

        try {
            mMachine.getLaserController().unSubscribeLaserSafetyState();
        } catch (NullPointerException ignore) {
        }

        try {
            mMachine.getCNCController().unSubscribeCNCInfo();
        } catch (NullPointerException ignore) {
        }

        try {
            mMachine.getMachineController().getEnclosure().unsubscribeEnclosureInfo();
        } catch (NullPointerException ignore) {
        }

        try {
            mMachine.getMachineController().getAirPurifier().unsubscribeAirPurifierStatusChange();
        } catch (NullPointerException ignore) {
        }

        try {
            mMachine.getMachineController().getDryBox().unSubscribeStatus();
        } catch (NullPointerException ignore) {
        }

        try {
            mMachine.getMachineController().unsubscribeLinearLimitStatus();
        } catch (NullPointerException ignore) {
        }
    }
}
