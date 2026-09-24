package fabscreen.features.print.j1platform.viewmodel;

import static fabscreen.platform.base.service.machine.controller.MachineOperationStatus.SYSTEM_STATUS_COMPLETED;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP_DOUBLE_EXTRUDER;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_CNC_200W;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.controller.MachineOperationStatus;
import fabscreen.platform.base.service.machine.controller.NewPrintController;
import fabscreen.platform.base.service.machine.controller.PrintEvent;
import fabscreen.platform.base.service.machine.entity.module.HeatedBed;
import fabscreen.platform.base.service.machine.entity.toolhead.CNCToolhead;
import fabscreen.platform.base.service.machine.entity.toolhead.FdmToolhead;
import fabscreen.platform.base.service.machine.entity.toolhead.LaserToolhead;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.data.FilamentState;
import fabscreen.platform.core.ui.data.PrintProgress;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.lib.SubjectHolder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

public class J1PrintViewModel extends BaseViewModel {
    private final BehaviorSubject<Boolean> mWaitingSubject = BehaviorSubject.createDefault(false);
    private final BehaviorSubject<PrintProgress> mPrintProgressSubject = BehaviorSubject.createDefault(new PrintProgress());
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private IMachine mJ1Machine;
    private NewPrintController mNewPrintController;
    private IPrintWorkspace mWorkspace;
    private IMachine.WorkType mWorkType;
    private BehaviorSubject<FilamentState> mFilamentStateSubject = BehaviorSubject.createDefault(new FilamentState());

    private boolean mIsRecover = false;
    private float mEstimatedTime = 0f;

    public J1PrintViewModel() {
        super();
        mJ1Machine = ServiceContainer.getInstance().getService(IMachine.class);
        mWorkspace = ServiceContainer.getInstance().getService(IPrintWorkspace.class);
        mNewPrintController = mJ1Machine.getNewPrintController();
        mWorkType = mJ1Machine.getMachineInfoSubjectHolder().getValue().workType;
    }

    public IMachine.WorkType getWorkType() {
        return mWorkType;
    }

    public void onPause() {
//        mNewPrintController.unSubscribeTookHeadSpeed();
        mNewPrintController.unSubscribeExtruderWorkSpeedObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                }, LogHelper::log);

        mJ1Machine.getFDMController().unSubscribeExtruderChange();
        mJ1Machine.getFDMController().unSubscribeFanChange();
        mJ1Machine.getMachineController().getHeatedBed().unsubscribeTemperatureChange();
        mNewPrintController.unsubscribePrintModeStatus()
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                }, LogHelper::log);
    }

    public void onResume() {
        mNewPrintController.subscribeTookHeadSpeed();
        if (mJ1Machine.getMachineInfoSubjectHolder().getValue().isEnclosureAvailable) {
            mJ1Machine.getMachineController().getEnclosure().subscribeEnclosureInfo();
        }
        if (mJ1Machine.getMachineInfoSubjectHolder().getValue().isAirPurifierAvailable) {
            mJ1Machine.getMachineController().getAirPurifier().subscribeAirPurifierStatusChange();
        }
        switch (mWorkType) {
            case FDM:
                mJ1Machine.getFDMController().subscribeExtruderChange();
                mJ1Machine.getMachineController().getHeatedBed().subscribeTemperatureChange();
                break;
            case LASER:
                mJ1Machine.getLaserController().subscribeLaserTubeStatus().as(bindToLifecycle()).subscribe(responseStructure -> {
                }, LogHelper::log);
                break;
            case CNC:
                mJ1Machine.getCNCController().subscribeCNCInfo();
                break;
            default:
                break;
        }
    }

    public void initPrint() {
        mEstimatedTime = mWorkspace.getEstimatedTime();
        boolean isPrinting = MachineOperationStatus.isPrinting(mNewPrintController.getPrintState());
        if (isPrinting) {
            // Initializing from last printing
            setTimeToUpdateProgress();
        } else {
            // If mode is change
            float printModeXOffset = ServiceContainer.getInstance().getService(IPrintWorkspace.class).getPrintModeXOffset();
            if (printModeXOffset != 0) {
                Logger.d("requesting Print Start Offset... %.2f", printModeXOffset);
                mNewPrintController.requestPrintStartOffset(printModeXOffset, 0, 0)
                        .observeOn(AndroidSchedulers.mainThread())
                        .as(bindToLifecycle())
                        .subscribe(responseStructure -> {
                            Logger.d("Set Print Start Offset " + responseStructure.isSuccess());
                        }, LogHelper::log);
            }

            mNewPrintController.reset();
            IFile file1 = mWorkspace.getPrintFile();
            mNewPrintController.setFile(file1);
            mNewPrintController.setTotalLines(mWorkspace.getFileTotalLineCount());

            startPrint();
        }
        updateProgress();
    }

    public void startPrint() {
        mWaitingSubject.onNext(true);
        mCompositeDisposable.clear();
        // Power Panic
        boolean powerOutageFlag = mNewPrintController.getRecoveryFlag();
        if (powerOutageFlag) {
            Logger.d("Try Power Loss recovering..");
            mNewPrintController.recover();
            mIsRecover = true;
        } else {
            mIsRecover = true;
            int printMode = mWorkspace.getPrintMode();
            mNewPrintController.requestChangePrintMode(printMode)
                    .as(bindToLifecycle())
                    .subscribe(responseStructure -> {
                        mNewPrintController.start();
                        Logger.d("requesting print mode... " + responseStructure.isSuccess());
                    }, LogHelper::log);
        }
        // Update
        setTimeToUpdateProgress();
    }

    public void requestMachineResume() {
        mWaitingSubject.onNext(true);
        mNewPrintController.resume();
    }

    public void requestMachineStop() {
        mWaitingSubject.onNext(true);
        mNewPrintController.stop();
    }

    public void requestMachinePause() {
        mWaitingSubject.onNext(true);
        mNewPrintController.pause();
    }

    private void setTimeToUpdateProgress() {
        Disposable subscribe = Observable.interval(0, 2, TimeUnit.SECONDS)
                .takeUntil(tick -> mNewPrintController.getPrintState() == SYSTEM_STATUS_COMPLETED.value())
                .filter(tick -> MachineOperationStatus.isPrinting(mNewPrintController.getPrintState()))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(tick -> {
                    updateProgress();
                }, Throwable::printStackTrace);
        mCompositeDisposable.add(subscribe);
    }

    public void updateProgress() {
        float p = mNewPrintController.getProgress();
        // formula: remaining = (1 - p) * p * elapsed / p + (1 - p) * (1 - p) * ETA
        int elapsed = mNewPrintController.getTickCounter().getCount();
        int remaining = (int) ((1 - p) * elapsed + (1 - p) * (1 - p) * mEstimatedTime);
        final int percentage = (int) (100 * p);
        mPrintProgressSubject.onNext(new PrintProgress(remaining, percentage));
    }

    public Observable<ResponseStructure<IStructure>> setLaserPower(int i, float parseInt) {
        return mJ1Machine.getLaserController().setLaserPower(i, parseInt);
    }

    public Observable<LaserToolhead.LaserToolheadInfo> getLaserToolHeadInfoObservable(int i) {
        return mJ1Machine.getLaserController().getLaserToolHeadInfoObservable(i);
    }

    public Observable<ResponseStructure> setExtruderWorkSpeed(IMachine.WorkType workType, int headIndex, int extruderIndex, int workspeed) {
        return mNewPrintController.setExtruderWorkSpeed(workType, headIndex, extruderIndex, workspeed);
    }

    public Observable<ArrayList<Integer>> getTookHeadSpeedObservable() {
        return mNewPrintController.getTookHeadSpeedObservable();
    }

    public Observable<CNCToolhead.CNCToolheadInfo> getCncToolHeadInfoObservable(int index) {
        return mJ1Machine.getCNCController().getCncToolHeadInfoObservable(index);
    }

    public Observable<ResponseStructure> setCNCTarget(int index, int speed) {
        return is200WattCNC() ? mJ1Machine.getCNCController().setTargetSpeed(index, speed) : mJ1Machine.getCNCController().setSpindlePower(index, speed);
    }

    public boolean is200WattCNC() {
        return mJ1Machine.getCNCController().getHeadType() == HEAD_CNC_200W;
    }

    public boolean isDoubleExtruder() {
        return mJ1Machine.getFDMController().getHeadType() == HEAD_3DP_DOUBLE_EXTRUDER;
    }

    public Observable<ResponseStructure> setExtruderTemperature(int toolheadIndex, int extruderIndex, int temperature) {
        return mJ1Machine.getFDMController().setExtruderTemperature(toolheadIndex, extruderIndex, temperature);
    }

    public SubjectHolder<FdmToolhead.FdmToolheadStatus> getToolheadStatusSubjectHolder(int toolheadIndex) {
        return mJ1Machine.getFDMController().getToolheadStatusSubjectHolder(toolheadIndex);
    }


    public SubjectHolder<HeatedBed.HeatedBedStatus> getHeatedBedStatusSubjectHolder() {
        return mJ1Machine.getMachineController()
                .getHeatedBed()
                .getHeatedBedStatusSubjectHolder();
    }

    public Observable<Integer> getPrintStateObservable() {
        Observable<Integer> printStateObservable = mNewPrintController.getPrintStateObservable();
        printStateObservable
                .as(bindToLifecycle())
                .subscribe(integer -> mWaitingSubject.onNext(MachineOperationStatus.isPrintChange(integer)), LogHelper::log);
        return printStateObservable;
    }

    public Integer getPrintStateValue() {
        mWaitingSubject.onNext(MachineOperationStatus.isPrintChange(mNewPrintController.getPrintState()));
        return mNewPrintController.getPrintState();
    }

    public void updatePrintState() {
        mNewPrintController.updatePrintState();
    }

    public Observable<Boolean> getWaitingObservable() {
        return mWaitingSubject.hide();
    }

    public boolean getWaitingValue() {
        return mWaitingSubject.getValue();
    }

    public Observable<Boolean> getFilamentSubjectObservable() {
        return mNewPrintController.getFilamentSubjectObservable();
    }

    public void setFilament(boolean state) {
        mNewPrintController.setFilament(state);
    }

    public Observable<Boolean> getEnclosureSubjectObservable() {
        return mNewPrintController.getEnclosureSubjectObservable();
    }

    public void setEnclosure(boolean state) {
        mNewPrintController.setEnclosure(state);
    }

    public Observable<PrintProgress> getUpdateProgressObservable() {
        return mPrintProgressSubject.hide();
    }

    public FilamentState getFilamentStateValue() {
        return mFilamentStateSubject.getValue();
    }

    public void setFilamentState(FilamentState filamentState) {
        mFilamentStateSubject.onNext(filamentState);
    }

    public Observable<FilamentState> getFilamentStateObservable() {
        return mFilamentStateSubject.hide();
    }

    public Observable<FilamentState> checkoutExtruderTemperature() {
        FilamentState value = mFilamentStateSubject.getValue();
        // Product definition: if the current temperature is 0, it will be heated to 210 (default value) when resuming heating after cutting off material
        return setExtruderTemperature(0, 0, (int) (((int) value.getLeftTarget()) == 0 ? 210 : value.getLeftTarget()))
                .flatMap(structure -> value.getExtruderNum() == 2 ?
                        setExtruderTemperature(0, 1, (int) (((int) value.getRightTarget()) == 0 ? 210 : value.getRightTarget()))
                        : Observable.just(structure))
                .flatMap(structure -> getFilamentStateObservable());
    }

    public Observable<ResponseStructure> requestActivatedExtrusion(int type, float lengthIn, float speedIn, float lengthOut, float speedOut) {
        return mJ1Machine.getFDMController().requestActivatedExtrusion(type, lengthIn, speedIn, lengthOut, speedOut);
    }

    public Observable<PrintEvent> getPrintEventObservable() {
        Observable<PrintEvent> printEventObservable = mNewPrintController.getPrintEventObservable();
        printEventObservable
                .as(bindToLifecycle())
                .subscribe(printEvent -> mWaitingSubject.onNext(false), LogHelper::log);
        return printEventObservable;
    }

    public void setPowerOutageFlag(boolean flag) {
        mNewPrintController.setPowerOutageFlag(flag);
    }


    // clone from PrintJ1Fragment
    private boolean getRecoverFlag() {
        return mIsRecover;
    }

    public Observable<ResponseStructure> getPrintHeadState() {
        return mNewPrintController.getPrintHeadState();
    }

}
