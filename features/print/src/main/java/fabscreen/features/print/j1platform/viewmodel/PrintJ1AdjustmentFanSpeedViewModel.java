package fabscreen.features.print.j1platform.viewmodel;

import java.util.ArrayList;
import java.util.List;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.controller.FDMController;
import fabscreen.platform.base.service.machine.entity.parts.Extruder;
import fabscreen.platform.base.service.machine.entity.toolhead.FdmToolhead;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.Observable;

public class PrintJ1AdjustmentFanSpeedViewModel extends BaseViewModel {
    private final FDMController mFdmController;
    private final List<FdmToolhead.FdmToolheadStatus> mToolheadStatusList = new ArrayList<>();
    private final MachineInfo mMachineInfo;

    public PrintJ1AdjustmentFanSpeedViewModel() {
        IMachine machine = ServiceContainer.getInstance().getService(IMachine.class);
        mFdmController = machine.getFDMController();
        mMachineInfo = machine.getMachineInfoSubjectHolder().getValue();
    }

    public void subscribeFanChange() {
        mFdmController.subscribeFanChange();
    }

    public void unSubscribeFanChange() {
        mFdmController.unSubscribeFanChange();
    }

    /**
     * Get all extruders on the machine.
     *
     * @return all extruders on the machine
     * .e.g. Toolhead0 has one extruder, toolhead1 has one extruder, we will have two extruders in total.
     */
    public Observable<List<Extruder>> getExtruderListObservable() {
        if (isJ1()) {
            // J1 dual head.
            Observable<FdmToolhead.FdmToolheadStatus> toolhead1Observable = mFdmController.getToolheadStatusSubjectHolder(0).getObservable();
            Observable<FdmToolhead.FdmToolheadStatus> toolhead2Observable = mFdmController.getToolheadStatusSubjectHolder(1).getObservable();
            return Observable.zip(toolhead1Observable, toolhead2Observable, (toolhead0Status, toolhead1Status) -> {

                if (mToolheadStatusList.size() == 0) {
                    mToolheadStatusList.add(0, toolhead0Status);
                    mToolheadStatusList.add(1, toolhead1Status);
                } else {
                    mToolheadStatusList.set(0, toolhead0Status);
                    mToolheadStatusList.set(1, toolhead1Status);
                }

                List<Extruder> extruderList = new ArrayList<>();
                extruderList.addAll(toolhead0Status.getExtruderList());
                extruderList.addAll(toolhead1Status.getExtruderList());

                return extruderList;
            });
        } else {
            // Single head.
            return mFdmController.getToolheadStatusSubjectHolder().getObservable()
                    .flatMap(fdmToolheadStatus -> {
                                mToolheadStatusList.add(fdmToolheadStatus);
                                return Observable.just(fdmToolheadStatus.getExtruderList());
                            }
                    );

        }
    }

    public Observable<Boolean> setFanSpeed(int toolHeadIndex, int extruderIndex, int speedLevel) {
        return mFdmController.setFanSpeed(toolHeadIndex, extruderIndex, speedLevel).flatMap(responseStructure -> Observable.just(responseStructure.resultProp.getValue() == 0));
    }

    public Observable<Boolean> switchExtruder(int toolheadIndex, int extruderIndex) {
        FdmToolhead.FdmToolheadStatus toolheadStatus = mToolheadStatusList.get(toolheadIndex);
        // Toolhead and extruder already activated, just return true.
        if (toolheadStatus.isActive() && toolheadStatus.getExtruderList().get(extruderIndex).getState() == 1) {
            return Observable.just(true);
        }
        // Do switch.
        return mFdmController.switchExtruder(toolheadStatus.getId(), extruderIndex).flatMap(responseStructure -> Observable.just(responseStructure.resultProp.getValue() == 0));
    }

    public Observable<Boolean> setToolHeadFanSpeed(int toolHeadIndex, int extruderIndex) {
        FdmToolhead.FdmToolheadStatus toolHeadStatus = mToolheadStatusList.get(toolHeadIndex);
        return null;
    }

    public boolean isJ1() {
        return mMachineInfo.seriesId == IMachine.MachineSeries.J && mMachineInfo.modelId == IMachine.MachineModel.J1;
    }
}
