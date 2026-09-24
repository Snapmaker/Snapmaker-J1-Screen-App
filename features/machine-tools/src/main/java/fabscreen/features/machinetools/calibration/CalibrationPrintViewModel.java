package fabscreen.features.machinetools.calibration;

import android.content.Context;

import androidx.annotation.RawRes;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.FabLocalFile;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.parser.IGcodeParser;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.lib.print.PrintListener;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.FDMController;
import fabscreen.platform.base.service.machine.controller.MachineOperationStatus;
import fabscreen.platform.base.service.machine.controller.NewPrintController;
import fabscreen.platform.base.service.machine.controller.PrintController;
import fabscreen.platform.base.service.machine.controller.PrintEvent;
import fabscreen.platform.base.service.machine.entity.module.HeatedBed;
import fabscreen.platform.base.service.machine.entity.toolhead.FdmToolhead;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

import static fabscreen.platform.base.service.machine.controller.MachineOperationStatus.SYSTEM_STATUS_COMPLETED;
import static fabscreen.platform.base.service.machine.controller.PrintController.STATE_COMPLETED;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP_DOUBLE_EXTRUDER;

public class CalibrationPrintViewModel extends BaseViewModel {
    private static final int STATUS_IDLE = 0;
    private static final int STATUS_PRINTING = 1;
    private static final int STATUS_PAUSED = 2;
    private static final int STATUS_COMPLETED = 3;

    private final IPrintWorkspace mWorkspace;
    public final PrintController mPrintController;
    public final NewPrintController mNewPrintController;
    private final IGcodeParser mParser;
    private final IMachine mMachine;
    private final IAppService mService;
    private final Context mContext;
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private Disposable sub;
    private IFile mFile;

    private final BehaviorSubject<Boolean> mWaitingSubject = BehaviorSubject.createDefault(false);
    private final BehaviorSubject<Integer> mProgress = BehaviorSubject.create();

    private float mEstimatedTime = 8263;
    private PrintListener mPrintListener;
    private final BehaviorSubject<String> mRemaining = BehaviorSubject.create();
    FDMController fdmController;

    public CalibrationPrintViewModel() {
        super();
        mMachine = ServiceContainer.getInstance().getService(IMachine.class);
        mWorkspace = ServiceContainer.getInstance().getService(IPrintWorkspace.class);
        mPrintController = mMachine.getPrintController();
        mNewPrintController = mMachine.getNewPrintController();
        fdmController = mMachine.getFDMController();
        mParser = ServiceContainer.getInstance().getService(IGcodeParser.class);
        mService = ServiceContainer.getInstance().getService(IAppService.class);
        mContext = mService.getAppContext();
    }

    public Observable<Integer> getProgress() {
        return mProgress.hide();
    }

    public Observable<String> getRemaining() {
        return mRemaining.hide();
    }

    public void startPrint(IFile iFile) {
        parseFile(iFile);
    }

    public void startPrint(@RawRes int rawFileId) {
        // Set calibration mode first to let mainboard know that this is a printwork for calibration.
        mWaitingSubject.onNext(true);
        mNewPrintController.reset();
        mMachine.getFDMController().setCalibrationMode(102)
                .as(bindToLifecycle())
                .subscribe(response -> {
                    if (response.isSuccess()) {
                        copyGcodeToCache(rawFileId);
                    } else {
                        Logger.e("Start cali print failed: set mode fail!");
                        mPrintListener.onStartFailed(257);
                    }
                }, e -> Logger.e("Start cali print failed: set mode fail!"));
    }

    private void copyGcodeToCache(int rawFileId) {
        if (rawFileId == 0) return;

        File file = new File(mContext.getCacheDir().getAbsoluteFile() + "/calibrationPrint.gcode");
        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }

        try (FileOutputStream outputStream = new FileOutputStream(file);
             InputStream is = mContext.getResources().openRawResource(rawFileId)) {
            int read;
            byte[] bytes = new byte[20480];
            while ((read = is.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (IOException e) {
            file = null;
        }

        if (file != null) {
            parseFile(new FabLocalFile(file));
        } else {
            Logger.d("Error preparing file for print");
            mPrintListener.onStartFailed(256);
        }
    }

    private void parseFile(IFile iFile) {
        mFile = iFile;
        mParser.startParse(mFile, IMachine.WorkType.LASER);
        mParser.getParseProgressObservable()
                .throttleLast(100, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(progress -> {
                    if (progress == 100) {
                        if (mParser.getFileType() == IMachine.WorkType.FDM) {
                            if (mParser.getHeaderType() == HEAD_3DP) {
                                mWorkspace.setWorkTemperature(new float[]{mParser.getNozzleTargetTemperature()});
                            } else if (mParser.getHeaderType() == HEAD_3DP_DOUBLE_EXTRUDER) {
                                mWorkspace.setWorkTemperature(new float[]{mParser.getNozzleTargetTemperature(), mParser.getNozzleTarget_1_Temperature()});
                            }
                        }
                        mWorkspace.setFileMD5Value("1234566789900");
                        mWorkspace.setPrintFile(iFile);
                        mWorkspace.setEstimatedTime(mParser.getEstimatedTime());
                        mWorkspace.setApplyMultiExtruder(mParser.isApplyMultiExtruder());
                        gotoPrint();
                    }
                });
    }

    private void gotoPrint() {
        mEstimatedTime = mWorkspace.getEstimatedTime();
        mMachine.getMachineStatusSubjectHolder()
                .getObservable()
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(machineStatus -> {
                    Logger.d("Machine work status " + machineStatus.status);
                }, LogHelper::log);
        mNewPrintController.getPrintStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(status -> {
                    Logger.d("Screen print state " + status);
                    if (status == MachineOperationStatus.SYSTEM_STATUS_COMPLETED.value()) {
                    }
                }, LogHelper::log);
        updateProgress();

        boolean isPrinting = MachineOperationStatus.isPrinting(mNewPrintController.getPrintState());
        if (isPrinting) {
            // TODO:ERROR: Already in print state?
        } else {
            AndroidSchedulers.mainThread().scheduleDirect(this::start, 300, TimeUnit.MILLISECONDS);
        }
    }

    private void updateProgress() {
        // 传递进度
        float p = mNewPrintController.getProgress();
        final int percentage = (int) (100 * p);
        mProgress.onNext(percentage);

        // formula: remaining = (1 - p) * p * elapsed / p + (1 - p) * (1 - p) * ETA
        int elapsed = mNewPrintController.getTickCounter().getCount();

        int remaining = (int) ((1 - p) * elapsed + (1 - p) * (1 - p) * mEstimatedTime);
        mRemaining.onNext(formatTime(remaining));
    }

    public static String formatTime(double time) {
        int hour = (int) (time) / 3600;
        int minute = ((int) (time) % 3600) / 60;
        int second = ((int) (time) % 60);

        if (hour < 1) {
            return ServiceContainer.getInstance().getService(IAppService.class).getApp().getString(fabscreen.platform.base.R.string.date_helper_format_time_minute_second, minute, second);
        } else {
            return ServiceContainer.getInstance().getService(IAppService.class).getApp().getString(fabscreen.platform.base.R.string.date_helper_format_time_hour_minute, hour, minute);
        }
    }

    public void start() {
        mNewPrintController.setFile(mFile);
        mNewPrintController.setTotalLines(mParser.getTotalLinesCount());
//        setPrintControllerListener(mPrintController);
        mNewPrintController.start();

        // Update
        Disposable sub = Observable.interval(2, TimeUnit.SECONDS)
                .takeUntil(tick -> mNewPrintController.getPrintState() == SYSTEM_STATUS_COMPLETED.value())
                .filter(tick -> MachineOperationStatus.isPrinting(mNewPrintController.getPrintState()))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(tick -> {
                    updateProgress();
                }, Throwable::printStackTrace);
        mCompositeDisposable.add(sub);
    }

    private void setPrintControllerListener(PrintController mPrintController) {
        mPrintController.setListener(new PrintListener() {
            @Override
            public void onStartSuccess() {
                mPrintController.getTickCounter().reset();
                mPrintController.getTickCounter().start();
                mPrintListener.onStartSuccess();
            }

            @Override
            public void onStartFailed(int retCode) {
                mPrintListener.onStartFailed(retCode);
            }

            @Override
            public void onPauseSuccess() {
                Logger.i("Print paused.");
                mPrintController.getTickCounter().stop();
                mPrintListener.onPauseSuccess();
            }

            @Override
            public void onPauseFailed(int retCode) {
                Logger.w(String.format("Unable to pause printing %d.", retCode));
                mPrintListener.onPauseFailed(retCode);
            }

            @Override
            public void onResumeSuccess() {
                Logger.i("Print resumed.");
                mPrintController.getTickCounter().start();
                mPrintListener.onResumeSuccess();
            }

            @Override
            public void onResumeFailed(int retCode) {
                Logger.i(String.format("Resume Failed %d.", retCode));
                switch (retCode) {
                    case 202: {
                        Logger.d("Filament used out, unable to resume printing.");
                        mPrintController.pauseOnFilamentUsedOut();
                        break;
                    }
                    case 203: {
                        mPrintController.pauseOnEnclosureDoorDetected();
                        break;
                    }
                    default: {
                        break;
                    }
                }
                mPrintListener.onResumeFailed(retCode);
            }

            @Override
            public void onResumeFromPowerOutageSuccess() {
                Logger.i("Print recovered.");
                mPrintController.setPowerOutageFlag(false);
                mPrintController.getTickCounter().load();
                mPrintController.getTickCounter().start();
                mPrintListener.onResumeFromPowerOutageSuccess();
            }

            @Override
            public void onResumeFromPowerOutageFailed(int retCode) {
                Logger.i(String.format("Resume From Power Outage Failed %d.", retCode));
                mPrintListener.onResumeFromPowerOutageFailed(retCode);
            }

            @Override
            public void onStopSuccess() {
                Logger.i("print stopped.");
                mPrintController.getTickCounter().stop();
                mPrintListener.onStopSuccess();
            }

            @Override
            public void onStopFailed(int retCode) {
                Logger.w("Unable to stop printing, ret code %d", retCode);
                mPrintListener.onStopFailed(retCode);
            }

            @Override
            public void onFinishSuccess() {
                Logger.i("Print Finished.");
                mPrintController.getTickCounter().stop();
                mPrintListener.onFinishSuccess();
            }

            @Override
            public void onFinishFailed(int retCode) {
                Logger.w("Unable to finish printing, ret code %d", retCode);
                mPrintListener.onFinishFailed(retCode);
            }
        });
    }

    public void setListener(PrintListener listener) {
        mPrintListener = listener;
    }

    public void stop() {
        mWaitingSubject.onNext(true);
        mNewPrintController.stop();
    }

    public Observable<FdmToolhead.FdmToolheadStatus> getToolheadStatusObservable(int index) {
        return fdmController.getToolheadStatusSubjectHolder(index).getObservable();
    }

    public Observable<HeatedBed.HeatedBedStatus> getHeatedBedObservable() {
        return mMachine.getMachineController().getHeatedBed()
                .getHeatedBedStatusSubjectHolder().getObservable();
    }

    public void pause() {
        mWaitingSubject.onNext(true);
        mNewPrintController.pause();
    }

    public void resume() {
        mWaitingSubject.onNext(true);
        mNewPrintController.resume();
    }

    public Observable<Integer> getPrintControllerStateObservable() {
        Observable<Integer> printStateObservable = mNewPrintController.getPrintStateObservable();
        printStateObservable
                .as(bindToLifecycle())
                .subscribe(integer -> mWaitingSubject.onNext(MachineOperationStatus.isPrintChange(integer)), LogHelper::log);
        return printStateObservable;
    }

    public Observable<PrintEvent> getPrintEventObservable() {
        Observable<PrintEvent> printEventObservable = mNewPrintController.getPrintEventObservable();
        printEventObservable
                .as(bindToLifecycle())
                .subscribe(printEvent -> mWaitingSubject.onNext(false), LogHelper::log);
        return printEventObservable;
    }

    public void unSubscribeTemperature() {
        mMachine.getFDMController().unSubscribeExtruderChange();
        mMachine.getMachineController().getHeatedBed().unsubscribeTemperatureChange();
    }

    public void subscribeTemperature() {
        mMachine.getFDMController().subscribeExtruderChange();
        mMachine.getMachineController().getHeatedBed().subscribeTemperatureChange();
    }

    public Observable<Boolean> getFilamentSubjectObservable() {
        return mNewPrintController.getFilamentSubjectObservable();
    }

    public void setFilament(boolean b) {
        mNewPrintController.setFilament(b);
    }

    public boolean getToolheadFilamentStatus(int index) {
        return fdmController.getToolheadStatusSubjectHolder(index).getValue().getExtruderList().get(0).getFilamentStatus();
    }

    public boolean getToolheadFilamentActive(int index) {
        return fdmController.getToolheadStatusSubjectHolder(index).getValue().isActive();
    }

    public void printComplete() {
        mNewPrintController.reset();
    }

    public Observable<Boolean> getWaitingObservable() {
        return mWaitingSubject.hide();
    }

    public boolean getWaitingValue() {
        return mWaitingSubject.getValue();
    }
}
