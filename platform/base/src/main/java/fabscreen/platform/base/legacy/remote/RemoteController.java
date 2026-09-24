package fabscreen.platform.base.legacy.remote;

import static fabscreen.platform.base.legacy.remote.SessionManager.NULL_SESSION;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.Constants;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.print.PrintListener;
import fabscreen.platform.base.service.Preferences;
import fabscreen.platform.base.service.machine.controller.PrintController;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

@Deprecated
public class RemoteController {
    public final String KEY_TOOL_HEAD_3DP_1 = "TOOLHEAD_3DPRINTING_1";
    public final String KEY_TOOL_HEAD_LASER_1 = "TOOLHEAD_LASER_1";
    public final String KEY_TOOL_HEAD_LASER_2 = "TOOLHEAD_LASER_2";
    public final String KEY_TOOL_HEAD_CNC_1 = "TOOLHEAD_CNC_1";
    private CompositeDisposable disposables = new CompositeDisposable();

    private PrintController mPrintController;
    private SessionManager sessionManager;

    private int mFileType = Constants.FILE_TYPE_UNKNOWN;
    private String mFileName;
    private IFile mFile;
    private int mTotalLines = 0;
    private float mEstimatedTime = 0;
    private int mEnclosureDoorCount = 0;

    private PublishSubject<Integer> mStartResultSubject = PublishSubject.create();
    private PublishSubject<Integer> mPauseResultSubject = PublishSubject.create();
    private PublishSubject<Integer> mResumeResultSubject = PublishSubject.create();
    private PublishSubject<Integer> mStopResultSubject = PublishSubject.create();

    private boolean mRemotePageFlag = false;
    private BehaviorSubject<Integer> mRemotePrintState = BehaviorSubject.createDefault(PrintController.STATE_IDLE);
    // print
    private PrintListener mListener = new PrintListener() {
        @Override
        public void onStartSuccess() {
            mStartResultSubject.onNext(0);
            mRemotePrintState.onNext(PrintController.STATE_PRINTING);
            mPrintController.getTickCounter().reset();
            mPrintController.getTickCounter().start();
        }

        @Override
        public void onStartFailed(int retCode) {
            mStartResultSubject.onNext(retCode);
        }

        @Override
        public void onPauseSuccess() {
            mPauseResultSubject.onNext(0);
            mRemotePrintState.onNext(PrintController.STATE_PAUSED);

            mPrintController.getTickCounter().stop();
        }

        @Override
        public void onPauseFailed(int retCode) {
            mPauseResultSubject.onNext(retCode);
        }

        @Override
        public void onResumeSuccess() {
            mResumeResultSubject.onNext(0);
            mRemotePrintState.onNext(PrintController.STATE_PRINTING);

            mPrintController.getTickCounter().start();
        }

        @Override
        public void onResumeFailed(int retCode) {
            mResumeResultSubject.onNext(retCode);
        }

        @Override
        public void onResumeFromPowerOutageSuccess() {
            // TODO: power-loss recovery on remote access
        }

        @Override
        public void onResumeFromPowerOutageFailed(int retCode) {

        }

        @Override
        public void onStopSuccess() {
            mStopResultSubject.onNext(0);
            mRemotePrintState.onNext(PrintController.STATE_IDLE);
            mPrintController.stop();
        }

        @Override
        public void onStopFailed(int retCode) {
            mStopResultSubject.onNext(retCode);
        }

        @Override
        public void onFinishSuccess() {
            mRemotePrintState.onNext(PrintController.STATE_COMPLETED);
            mPrintController.getTickCounter().stop();
        }

        @Override
        public void onFinishFailed(int retCode) {

        }
    };

    public RemoteController(File dataDir, PrintController printController) {
        mPrintController = printController;

        File sessionConfigFile = new File(dataDir, "session.json");
        sessionManager = new SessionManager(sessionConfigFile);

        bind();
    }

    private void bind() {
        Disposable sub = Observable.interval(5, TimeUnit.SECONDS)
                .subscribe(tick -> {
                    sessionManager.checkCurrentSessionActive();
                });
        disposables.add(sub);
    }

    //region Remote Auth

    public void connectPrintController() {
        mPrintController.setListener(mListener);

        // Sync print state nad arguments
        int printState = mPrintController.getPrintState();
        mRemotePrintState.onNext(printState);
        mTotalLines = mPrintController.getTotalLines();
    }

    public Observable<SessionManager.State> getRemoteStateObservable() {
        return sessionManager.getCurrentSessionObservable()
                .map(session -> {
                    if (session != NULL_SESSION) {
                        return SessionManager.State.STATE_ACTIVE;
                    } else {
                        return SessionManager.State.STATE_INACTIVE;
                    }
                });
    }

    /**
     * Create session
     */
    @NonNull
    public SessionManager.Session createSession() {
        return SessionManager.createSession("unknown");
    }

    /**
     * Get session.
     */
    @NonNull
    public SessionManager.Session getSession(Preferences preferences, @NonNull String token) {
        return sessionManager.getSession(preferences, token);
    }

    @NonNull
    public SessionManager.Session getCurrentSession() {
        return sessionManager.getCurrentSession();
    }

    public void setCurrentSession(@NonNull SessionManager.Session session) {
        sessionManager.setCurrentSession(session);
    }

    public Observable<SessionManager.Session> getCurrentSessionObservable() {
        return sessionManager.getCurrentSessionObservable();
    }

    public void accessCurrentSession() {
        sessionManager.accessCurrentSession();
    }

    public void grantCurrentSession() {
        sessionManager.grantCurrentSession();
    }

    //endregion

    public void denyCurrentSession() {
        sessionManager.denyCurrentSession();
    }

    public void setFileType(int fileType) {
        mFileType = fileType;
    }

    public String getFileName() {
        return mFileName != null ? mFileName : "";
    }

    public void setFileName(String fileName) {
        // one of print.gcode / print.nc / print.cnc
        mFileName = fileName;
    }

    public IFile getFile() {
        return mFile;
    }

    public void setFile(IFile file) {
        mFile = file;
    }

    public int getTotalLines() {
        return mTotalLines;
    }

    public void setTotalLines(int totalLines) {
        mTotalLines = totalLines;
    }

    public float getEstimatedTime() {
        return mEstimatedTime;
    }

    public void setEstimatedTime(float estimatedTime) {
        mEstimatedTime = estimatedTime;
    }

    public int getElapsedTIme() {
        return mPrintController.getTickCounter().getCount();
    }

    public double getRemainingTime() {
        float p = mPrintController.getProgress();

        // formula: remaining = (1 - p) * p * elapsed / p + (1 - p) * (1 - p) * ETA
        int elapsed = mPrintController.getTickCounter().getCount();
        return ((1 - p) * elapsed + (1 - p) * (1 - p) * mEstimatedTime);
    }

    // Progress Count
    public int getProgressCount() {
        return mPrintController.getProgressCount();
    }

    public float getProgress() {
        return mPrintController.getProgress();
    }

    // override
    public void reset() {
        mRemotePrintState.onNext(PrintController.STATE_IDLE);
        mPrintController.reset();
    }

    public void overrideNozzleTemperature(float nozzleTemperature) {
        mPrintController.setOverrideNozzleTemperature(nozzleTemperature);
    }

    public void overrideHeatedBedTemperature(float heatedBedTemperature) {
        mPrintController.setOverrideHeatedBedTemperature(heatedBedTemperature);
    }

    public void overrideZOffset(float offset) {
        mPrintController.setOverrideZOffset(offset);
    }

    public void overrideWorkSpeed(float workSpeed) {
        mPrintController.setOverrideFeedRate(workSpeed);
    }

    public void overrideLaserPower(float laserPower) {
        mPrintController.setOverrideLaserPower(laserPower);
    }

    public Observable<Integer> getRemotePrintStateObservable() {
        return mRemotePrintState.hide();
    }

    public int getRemotePrintState() {
        return mRemotePrintState.getValue();
    }

    public Observable<Integer> start() {
        if (mFile == null) {
            return Observable.just(200);
        }

        mPrintController.setFile(mFile);
        mPrintController.setTotalLines(mTotalLines);

        AndroidSchedulers.mainThread().scheduleDirect(() -> mPrintController.start(), 50, TimeUnit.MILLISECONDS);

        return mStartResultSubject;
    }

    public Observable<Integer> pause() {
        AndroidSchedulers.mainThread().scheduleDirect(() -> mPrintController.pause(), 50, TimeUnit.MILLISECONDS);

        return mPauseResultSubject;
    }

    public Observable<Integer> resume() {
        AndroidSchedulers.mainThread().scheduleDirect(() -> mPrintController.resume(), 50, TimeUnit.MILLISECONDS);

        return mResumeResultSubject;
    }

    public Observable<Integer> stop() {
        AndroidSchedulers.mainThread().scheduleDirect(() -> mPrintController.stop(), 50, TimeUnit.MILLISECONDS);

        return mStopResultSubject;
    }

    public void setFilamentOutPause() {
        mPrintController.pauseOnFilamentUsedOut();
    }

    public void setEnclosureDoorPause() {
        mPrintController.pauseOnEnclosureDoorDetected();
    }

    public int getEnclosureDoorCount() {
        return mEnclosureDoorCount;
    }

    public void setEnclosureDoorCount(int count) {
        mEnclosureDoorCount = count;
    }

    public boolean getRemotePageFlag() {
        return mRemotePageFlag;
    }

    // FIXME: Temporary workaround.
    public void setRemotePageFlag(boolean flag) {
        mRemotePageFlag = flag;
    }

    public void setPrintController(PrintController printController) {
        mPrintController = printController;
    }
}
