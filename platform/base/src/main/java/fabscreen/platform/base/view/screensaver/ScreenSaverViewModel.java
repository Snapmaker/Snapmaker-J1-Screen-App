package fabscreen.platform.base.view.screensaver;

import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.NewPrintController;
import fabscreen.platform.base.service.machine.controller.PrintController;
import fabscreen.platform.base.service.machine.controller.PrintEvent;
import fabscreen.platform.base.service.machine.controller.PrintEventState;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;

public class ScreenSaverViewModel extends BaseViewModel {

    private final PrintController mPrintController;
    private final NewPrintController mNewPrintController;
    private final IPrintWorkspace mWorkspace;
    private long mTimeCost;
    private boolean mIsPrintFinished = false;

    public ScreenSaverViewModel() {
        mPrintController = getServiceContainer().getService(IMachine.class).getPrintController();
        mNewPrintController = getServiceContainer().getService(IMachine.class).getNewPrintController();
        mWorkspace = ServiceContainer.getInstance().getService(IPrintWorkspace.class);
        mNewPrintController.subscribePrintCostTime();
        watchPrintCostTime();

        mNewPrintController.getPrintEventObservable()
                .distinctUntilChanged()
                .as(bindToLifecycle())
                .subscribe(printEvent -> {
                    if (printEvent.getPrintEventState() == PrintEventState.FINISH_SUCCESS) {
                        mIsPrintFinished = true;
                    }
                }, LogHelper::log);
    }

    private void watchPrintCostTime() {
        mNewPrintController.getPrintCostTimeObservable()
                .as(bindToLifecycle())
                .subscribe(aLong -> {
                    if (aLong > 0) {
                        mTimeCost = aLong;
                    }
                });
    }

    public Observable<Integer> getPrintStateObservable() {
        return mNewPrintController.getPrintStateObservable();
    }

    public Observable<Long> getTimerObservable() {
        return Observable.interval(0, 2, TimeUnit.SECONDS);
    }

    public int getPrintState() {
        return mNewPrintController.getPrintState();
    }

    /**
     * @return Printing progress, 0~100
     */
    public int getPrintProgress() {
        int progress;
        if (mIsPrintFinished) {
            progress = 100;
        } else {
            progress = (int) (mNewPrintController.getProgress() * 100);
        }
        return progress;
    }

    public long getTimeCost() {
        return mTimeCost;
    }

    public long getRemainTime() {
        float p = mNewPrintController.getProgress();
//        int i = (int) ((1 - p) * mTimeCost + (1 - p) * (1 - p) * mWorkspace.getEstimatedTime());
        int elapsed = mNewPrintController.getTickCounter().getCount();
        int i = (int) ((1 - p) * elapsed + (1 - p) * (1 - p) * mWorkspace.getEstimatedTime());
//        Logger.d("---FDT--- getRemainTime\tremaining:%d,mTimeCost:%d,mEstimatedTime:%f,p:%f", i, mTimeCost, mWorkspace.getEstimatedTime(), p);
        return i;
    }

    @Override
    protected void onCleared() {
        mNewPrintController.unSubscribePrintCostTime();
        super.onCleared();
    }

    public long getAllTime() {
        return mNewPrintController.getPrintTime();
    }

    public boolean getPrintFinished() {
        return mIsPrintFinished;
    }

}
