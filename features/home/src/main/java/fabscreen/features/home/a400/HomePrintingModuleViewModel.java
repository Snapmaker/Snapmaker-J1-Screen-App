package fabscreen.features.home.a400;


import static fabscreen.platform.base.service.machine.controller.PrintController.STATE_COMPLETED;
import static fabscreen.platform.base.service.machine.controller.PrintController.STATE_PRINTING;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.orhanobut.logger.Logger;

import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.parser.IGcodeParser;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.lib.print.PrintListener;
import fabscreen.platform.base.lib.print.TickCounter;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.PrintController;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class HomePrintingModuleViewModel extends BaseViewModel {
    private final BehaviorSubject<PrintProgress> mPrintProgressSubj = BehaviorSubject.createDefault(new PrintProgress(0, ""));
    private final BehaviorSubject<Integer> mPrintStateSubj = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> mProcessingSubj = BehaviorSubject.createDefault(false);
    private final PublishSubject<PrintEvent> mPrintEventSubj = PublishSubject.create();

    private final PrintController mPrintController;
    private final IPrintWorkspace mWorkspace;
    private final IGcodeParser mGcodeParser;

    public HomePrintingModuleViewModel() {
        IMachine machine = getServiceContainer().getService(IMachine.class);
        mPrintController = machine.getPrintController();
        mWorkspace = getServiceContainer().getService(IPrintWorkspace.class);
        mGcodeParser = getServiceContainer().getService(IGcodeParser.class);
        observePrintProgress();
        observePrintState();
        setPrintListener();
    }

    public PrintModelInfo getPrintModelInfo() {
        return new PrintModelInfo(mWorkspace.getFileName(), mGcodeParser.getGcodeThumbnail());
    }

    public Observable<PrintProgress> getPrintProgressObservable() {
        return mPrintProgressSubj.hide();
    }

    public Observable<Integer> getPrintStateObservable() {
        return mPrintStateSubj.hide();
    }

    public Observable<Boolean> getProcessingObservable() {
        return mProcessingSubj.hide();
    }

    public Observable<PrintEvent> getPrintEventObservable() {
        return mPrintEventSubj.hide();
    }

    public void resumePrint() {
        mProcessingSubj.onNext(true);
        mPrintController.resume();
    }

    public void pausePrint() {
        mProcessingSubj.onNext(true);
        mPrintController.pause();
    }

    public void stopPrint() {
        mProcessingSubj.onNext(true);
        mPrintController.stop();
    }

    private void observePrintProgress() {
        Observable.interval(2, TimeUnit.SECONDS)
                .takeUntil(tick -> mPrintController.getPrintState() == STATE_COMPLETED)
                .filter(tick -> mPrintController.getPrintState() == STATE_PRINTING)
                .as(bindToLifecycle())
                .subscribe(tick -> {
                    // formula: remaining = (1 - p) * p * elapsed / p + (1 - p) * (1 - p) * ETA
                    updatePrintProgress();
                });
    }

    private void updatePrintProgress() {
        float p = mPrintController.getProgress();
        int elapsed = mPrintController.getTickCounter().getCount();
        int remain = (int) ((1 - p) * elapsed + (1 - p) * (1 - p) * mWorkspace.getEstimatedTime());
        mPrintProgressSubj.onNext(new PrintProgress((int) (p * 100), formatRemainTime(remain)));
    }

    private void observePrintState() {
        mPrintController.getPrintStateObservable()
                .as(bindToLifecycle())
                .subscribe(state -> {
                    if (state == STATE_COMPLETED) {
                        updatePrintProgress();
                    }
                    mPrintStateSubj.onNext(state);
                }, LogHelper::log);
    }

    private void setPrintListener() {
        final TickCounter tickCounter = mPrintController.getTickCounter();
        mPrintController.setListener(new PrintListener() {
            @Override
            public void onStartSuccess() {
                // oh we started
                Logger.i("Print started.");
                mProcessingSubj.onNext(false);
                tickCounter.reset();
                tickCounter.start();
            }

            @Override
            public void onStartFailed(int retCode) {
                switch (retCode) {
                    case 202: {
                        Logger.w("Filament used out, unable to start printing.");
                        break;
                    }
                    case 203: {
                        Logger.w("Unable to start printing, enclosure door open detected.");
                        break;
                    }
                    default: {
                        mPrintEventSubj.onNext(PrintEvent.START_FAIL);
                        Logger.w("Unable to start printing, ret code %d", retCode);
                        break;
                    }
                }
                mProcessingSubj.onNext(false);
            }

            @Override
            public void onPauseSuccess() {
                Logger.i("Print paused.");
                mProcessingSubj.onNext(false);
                tickCounter.stop();
            }

            @Override
            public void onPauseFailed(int retCode) {
                Logger.w("Unable to pause printing.");
                mProcessingSubj.onNext(false);
                mPrintEventSubj.onNext(PrintEvent.PAUSE_FAIL);
            }

            @Override
            public void onResumeSuccess() {
                Logger.i("Print resumed.");
                mProcessingSubj.onNext(false);
                tickCounter.start();
            }

            @Override
            public void onResumeFailed(int retCode) {
                mProcessingSubj.onNext(false);
                switch (retCode) {
                    case 202: {
                        Logger.w("Filament used out, unable to resume printing.");
                        mPrintController.pauseOnFilamentUsedOut();
//                        handleFilamentRunOut(null);
                        break;
                    }
                    case 203: {
                        mPrintController.pauseOnEnclosureDoorDetected();
//                        handleEnclosureDoorPaused();
                        break;
                    }
                    default: {
                        Logger.w("Unable to resume printing.");
                        mPrintEventSubj.onNext(PrintEvent.RESUME_FAIL);
                        break;
                    }
                }
            }

            @Override
            public void onResumeFromPowerOutageSuccess() {
                mProcessingSubj.onNext(false);
                // we resumed from power outage
                mPrintController.setPowerOutageFlag(false);
                Logger.i("Print recovered.");

                // clear flag when resume success
                mPrintController.resetErrorFlag()
                        .observeOn(AndroidSchedulers.mainThread())
                        .as(bindToLifecycle())
                        .subscribe(success -> {
                            Logger.d("Error flag removed.");
                            mPrintController.clearPowerOutageFlag();
                        }, LogHelper::log);

                tickCounter.load();
                tickCounter.start();
            }

            @Override
            public void onResumeFromPowerOutageFailed(int retCode) {
                mProcessingSubj.onNext(false);
                mPrintEventSubj.onNext(PrintEvent.POWER_LOSS_RESUME_FAIL);
                switch (retCode) {
                    case 202: {
                        Logger.w("Filament used out, failed to recover from power loss.");
                        break;
                    }
                    case 203: {
//                        handleEnclosureDoorPaused();
                        break;
                    }
                    default: {
                        Logger.w("Failed to recover from power loss.");
                        break;
                    }
                }
            }

            @Override
            public void onStopSuccess() {
                mProcessingSubj.onNext(false);
                Logger.i("print stopped.");
                tickCounter.stop();
                mPrintEventSubj.onNext(PrintEvent.STOP_SUCCESS);
            }

            @Override
            public void onStopFailed(int retCode) {
                Logger.w("Unable to stop printing, ret code %d", retCode);
                mProcessingSubj.onNext(false);
                mPrintEventSubj.onNext(PrintEvent.STOP_FAIL);
            }

            @Override
            public void onFinishSuccess() {
                Logger.i("Print Finished.");
                mProcessingSubj.onNext(false);
                tickCounter.stop();
                Logger.d("Print job costs %s.", formatTime(ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().getCount()));
                mPrintEventSubj.onNext(PrintEvent.FINISH_SUCCESS);
            }

            @Override
            public void onFinishFailed(int retCode) {
                Logger.w("Unable to finish printing, ret code %d", retCode);
                mProcessingSubj.onNext(false);
                mPrintEventSubj.onNext(PrintEvent.FINISH_FAIL);
            }
        });
    }

    private String formatRemainTime(int time) {
        //Remaining Time: 20d 13h 20min
        return String.format("Remaining Time: %s", formatTime(time));
    }

    @NonNull
    private String formatTime(int time) {
        int hour = time / 3600;
        int minute = (time % 3600) / 60;
        int second = (time % 60);

        String remainTime;

        if (hour < 1) {
            remainTime = ServiceContainer.getInstance().getService(IAppService.class).getApp().getString(fabscreen.platform.base.R.string.date_helper_format_time_minute_second, minute, second);
        } else {
            remainTime = ServiceContainer.getInstance().getService(IAppService.class).getApp().getString(fabscreen.platform.base.R.string.date_helper_format_time_hour_minute, hour, minute);
        }
        return remainTime;
    }

    public static class PrintProgress {
        public int percentage;
        public String remainDesc;

        public PrintProgress(int percentage, @NonNull String remainDesc) {
            this.percentage = percentage;
            this.remainDesc = remainDesc;
        }
    }

    public static class PrintModelInfo {
        public String fileName;
        public Bitmap thumbnail;

        public PrintModelInfo(String name, Bitmap thumbnail) {
            this.fileName = name;
            this.thumbnail = thumbnail;
        }
    }

    public enum PrintEvent {
        START_FAIL,
        PAUSE_FAIL,
        RESUME_FAIL,
        POWER_LOSS_RESUME_FAIL,
        STOP_SUCCESS,
        STOP_FAIL,
        FINISH_SUCCESS,
        FINISH_FAIL
    }
}
