package fabscreen.platform.base.legacy.connection.print;

import com.orhanobut.logger.Logger;

import java.io.InputStream;

import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.legacy.ISlaveComputer;
import fabscreen.platform.base.legacy.connection.SSTPPacketContent;
import fabscreen.platform.base.legacy.print.IPrintController;
import fabscreen.platform.base.lib.print.PrintListener;
import fabscreen.platform.base.lib.print.TickCounter;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.print.GcodePlayer;
import fabscreen.platform.base.lib.print.OnAirGcodeModifier;
import fabscreen.platform.base.model.ModelBoundary;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

//import fabscreen.libraries.core.connection.ISSTPComputer;

@Deprecated
public class DeprecatedBatchPrintController implements IPrintController {

    private final int BATCHES_NUMS = 3;
    // BATCHES_LENGTH = Maximum packet length - EventID - StartLine -EndLine
    private final int BATCHES_LENGTH = 512 - 1 - 4 - 4;
    private boolean mIsOverride = false;
    // Batch
    private BatchCode[] mBatches;
    private int mStartBatchNo;
    private int mEndBatchNo;
    private int mNowBatchNo;
    private BatchCode mBatchCode;
    private boolean mRecoveryFlag = false;
    private PublishSubject<Boolean> mResumeSubject = PublishSubject.create();

    private BehaviorSubject<Integer> mPrintStateSubject = BehaviorSubject.createDefault(STATE_IDLE);
    private OnAirGcodeModifier mGcodeModifier = new OnAirGcodeModifier();
    private Disposable mPrintDisposable;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private PrintListener mListener;
    private GcodePlayer mGcodePlayer = new GcodePlayer();
    private ModelBoundary mModelBoundary;
    private TickCounter mTickCounter;
    private ISlaveComputer mSlaveComputer;

    public DeprecatedBatchPrintController(ISlaveComputer slaveComputer, TickCounter tickCounter) {
        mSlaveComputer = slaveComputer;
        mTickCounter = tickCounter;
        reset();
    }

    public DeprecatedBatchPrintController(IPrintController IPrintController) {
        mTickCounter = IPrintController.getTickCounter();
        reset();
    }

    @Override
    public void reset() {
        mPrintStateSubject.onNext(STATE_IDLE);

        mGcodeModifier.reset();

        if (mPrintDisposable != null && !mPrintDisposable.isDisposed()) {
            mPrintDisposable.dispose();
            mPrintDisposable = null;
        }
    }

    private void prepare() {
        compositeDisposable.clear();
        clearBatch();
        compositeDisposable.add(
                mSlaveComputer.getBatchGcodeResponseSubject()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(batchGcodeResponse -> {
                            Logger.d("batchGcodeResponse Request LineNo:" + batchGcodeResponse.getLineNo());
                            doNext(batchGcodeResponse.getLineNo());
                        }, LogHelper::log)
        );

        compositeDisposable.add(
                mSlaveComputer.getMasterState()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(masterState -> {
//                            if (masterState.states != 0) return;
                            switch (masterState.mOperationId) {
                                case 0x04:
                                    if (masterState.states != 0) return;
                                    mPrintStateSubject.onNext(STATE_PAUSED);
                                    mListener.onPauseSuccess();
                                    break;
                                case 0x07:
                                    Logger.i("Finish print.");
                                    mPrintStateSubject.onNext(STATE_COMPLETED);
                                    if (masterState.states == 0) {
                                        mListener.onFinishSuccess();
                                    } else {
                                        mListener.onFinishFailed(masterState.states);
                                    }
                                    break;
                            }
                        }, LogHelper::log)
        );
    }

    /**
     * Start printing.
     * <p>
     * When start success, We empty the Batch cache and wait for the master request
     */
    @Override
    public void start() {

        if (mPrintStateSubject.getValue() != STATE_IDLE) {
            Logger.w("Start state is not idle");
            mListener.onStartFailed(1); // unexpected state
            return;
        }
        Logger.i("Start print.");

        prepare();
        // Fixme: Re-consider prepare print logic and adjust settings definition.
        Disposable sub = mSlaveComputer.requestAdjustSettingLaserPower(getOverrideLaserPower()).subscribe();
        setActionDisposable(sub);
        sub = mSlaveComputer.start()
                .flatMap(retCode -> {
                    // After the master control status changes successfully, request the master control to start batch printing
                    if (retCode == 0) {
                        return mSlaveComputer.useBatchGcodeMode(1).onExceptionResumeNext(Observable.just(-1));
                    } else {
                        return Observable.just(retCode);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(retCode -> {
                    if (retCode == 0) {
                        mPrintStateSubject.onNext(STATE_PRINTING);
                        mListener.onStartSuccess();
                    } else {
                        mListener.onStartFailed(retCode);
                    }
                }, e -> {
                    LogHelper.log(e);
                    mListener.onStartFailed(1);
                });
        setActionDisposable(sub);
    }

    /**
     * Resume printing.
     */
    @Override
    public void pause() {
        if (mPrintStateSubject.getValue() != STATE_PRINTING) {
            mListener.onPauseFailed(1); // unexpected state
            return;
        }

        Logger.i("Pause print.");
        Disposable sub = getSlaveComputer().pause()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(retCode -> {
                    if (retCode == 0) {
                        mPrintStateSubject.onNext(STATE_PAUSED);
                        mListener.onPauseSuccess();
                    } else {
                        mListener.onPauseFailed(retCode);
                    }
                }, e -> {
                    LogHelper.log(e);
                    mListener.onPauseFailed(1);
                });
        setActionDisposable(sub);
    }

    /**
     * Resume printing.m
     */
    @Override
    public void resume() {
        if (mPrintStateSubject.getValue() != STATE_PAUSED) {
            mListener.onResumeFailed(1); // unexpected state
            return;
        }
        Logger.i("Resume print.");

        Disposable sub = mSlaveComputer.resume().flatMap(retCode -> {
            // After the master control status changes successfully, request the master control to start batch printing
            if (retCode == 0) {
                return mSlaveComputer.useBatchGcodeMode(1).onExceptionResumeNext(Observable.just(-1));
            } else {
                return Observable.just(retCode);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(retCode -> {
                    if (retCode == 0) {
                        mPrintStateSubject.onNext(STATE_PRINTING);
                        mListener.onResumeSuccess();
                    } else {
                        mListener.onResumeFailed(retCode);
                    }
                }, e -> {
                    LogHelper.log(e);
                    mListener.onResumeFailed(1);
                });
        setActionDisposable(sub);
    }


    /**
     * Stop printing.
     */
    @Override
    public void stop() {
        if (mPrintStateSubject.getValue() == STATE_IDLE) {
            mListener.onStopFailed(1); // unexpected state
            return;
        }
        Logger.i("Stop print.");
        Disposable sub = mSlaveComputer.stop()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(retCode -> {
                    if (retCode == 0) {
                        mPrintStateSubject.onNext(STATE_IDLE);
                        mListener.onStopSuccess();
                    } else {
                        mListener.onStopFailed(retCode);
                    }
                }, e -> {
                    LogHelper.log(e);
                    mListener.onStopFailed(1);
                });
        setActionDisposable(sub);
    }


    /**
     * Recover print from Power-Loss.
     */
    @Override
    public void recover() {
        if (mPrintStateSubject.getValue() != STATE_IDLE) {
            mListener.onResumeFromPowerOutageFailed(1); // unexpected state
            return;
        }
        Logger.i("Recover print.");
        prepare();
        Disposable sub = mSlaveComputer.resumeFromPowerOutage().observeOn(AndroidSchedulers.mainThread())
                .flatMap(retCode -> {
                    if (retCode == 0) {
                        return mSlaveComputer.useBatchGcodeMode(1).onExceptionResumeNext(Observable.just(-1));
                    } else {
                        return Observable.just(retCode);
                    }
                })
                .subscribe(retCode -> {
                    if (retCode == 0) {
                        mPrintStateSubject.onNext(STATE_PRINTING);
                        mListener.onResumeFromPowerOutageSuccess();
                    } else {
                        mListener.onResumeFromPowerOutageFailed(retCode);
                    }
                }, e -> {
                    LogHelper.log(e);
                    mListener.onResumeFromPowerOutageFailed(1);
                });
        setActionDisposable(sub);
    }

    /**
     * Actual next function, send G-code or resend G-code.
     * Query batch corresponding to the start line number of the output based on the line number
     * of the master control request.
     *
     * @param lineNo
     */
    private void doNext(int lineNo) {
        if (mPrintStateSubject.getValue() != STATE_PRINTING) {
            return;
        }

        mBatchCode = new BatchCode("", lineNo, lineNo);
        StringBuffer linesBuffer = new StringBuffer();
        int batchLength = 0;

        if (lineNo > mEndBatchNo) {
            // Not starting at line 0 is supported
            if (mNowBatchNo == -1) {
                mGcodePlayer.setLineno(lineNo);
                mGcodePlayer.nextLine();
            }

            while (mGcodePlayer.getLineNo() < lineNo) {
                mGcodePlayer.nextLine();
            }
            // Failed to obtain Gcode, complete the task
            if (mGcodePlayer.getLine() == null) {
                send();
                return;
            }

            while (batchLength < BATCHES_LENGTH) {
                String line = mGcodePlayer.getLine();
                if (mGcodePlayer.getLine() == null) {
                    // Failed to obtain Gcode, complete the task, break
                    break;
                }

                // Rewrite the requirements only once
                if (mIsOverride) {
                    if (!mGcodeModifier.getIsOverride()) {
                        mIsOverride = false;
                    }
                    if (mIsOverride) {
                        line = mGcodeModifier.newOverride(line);
                    }
                }

                // Replace comments in Gcode code
                int index = line.indexOf(";");
                if (index != -1) {
                    line = line.substring(0, index + 1);
                }
                line += "\n";
                batchLength += line.getBytes().length;
                if (batchLength < BATCHES_LENGTH) {
                    linesBuffer.append(line);
                    mGcodePlayer.nextLine();
                }
            }

            String tempBatch = linesBuffer.toString();
            mNowBatchNo = lineNo;
            mEndBatchNo = mGcodePlayer.getLineNo() - 1;
            mBatchCode = new BatchCode(tempBatch, mNowBatchNo, mEndBatchNo);
            int batchIndex = (getBatch(lineNo - 1) + 1) % BATCHES_NUMS;
            mBatches[batchIndex] = mBatchCode;
        } else if (lineNo < mStartBatchNo) {
            Logger.d("Abnormal: LineNo %s < mStartBatchNo %s,mEndBatchNo:%s", lineNo, mStartBatchNo, mEndBatchNo);
            clearBatch();
            doNext(lineNo);
            return;
        } else {
            //  Reissue or resend
            int batchIndex = getBatch(lineNo);
            // Failed to find the required batch
            if (batchIndex == -1) {
                Logger.d("Error: Batch can't find :" + lineNo);
                send();
                return;
            }
            BatchCode tempBatch = mBatches[batchIndex];
            String[] tempGcodes = tempBatch.getGcodes().split("\n");
            for (int i = lineNo - tempBatch.getStartNo(); i < tempGcodes.length; i++) {
                linesBuffer.append(tempGcodes[i]).append("\n");
            }
            mBatchCode = new BatchCode(linesBuffer.toString(), lineNo, tempBatch.getEndNo());
            mBatches[batchIndex] = mBatchCode;
        }
        send();
    }

    private void clearBatch() {
        mBatches = new BatchCode[BATCHES_NUMS];
        mEndBatchNo = -1;
        mNowBatchNo = -1;
        mStartBatchNo = -1;
    }

    private void send() {
        mStartBatchNo = getStartBatchNo();
        mSlaveComputer.sendPrintBatchGcode(mBatchCode.getStartNo(), mBatchCode.getEndNo(), mBatchCode.getGcodes());
    }

    /**
     * Find the batch with the smallest current line number
     */
    private int getStartBatchNo() {
        int batchNo = -1;
        for (int i = 0; i < mBatches.length; i++) {
            if (mBatches[i] == null) {
                continue;
            }
            batchNo = batchNo == -1 ? mBatches[i].getStartNo() : Math.min(batchNo, mBatches[i].getStartNo());
        }
        return batchNo;
    }

    /**
     * Find the batch containing the line number based on the line number
     */
    private int getBatch(int lineNo) {
        for (int i = 0; i < mBatches.length; i++) {
            if (mBatches[i] == null) {
                continue;
            }
            if (mBatches[i].getStartNo() <= lineNo && mBatches[i].getEndNo() >= lineNo) {
                return i;
            }
        }
        return -1;
    }

    private void UpdateChange() {
//        Observable<Integer> action = mGcodeModifier.getModifyAction(mSlaveComputer);
//        if (action != null) {
//            compositeDisposable.add(action.subscribe());
//        }
    }

    /**
     * Set `disposable` as current active disposable.
     * <p>
     * In our new implementation, there is only one action disposable is allowed to be active.
     *
     * @param disposable Disposable to be set
     */

    @Override
    public void setActionDisposable(Disposable disposable) {
        if (mPrintDisposable != null && !mPrintDisposable.isDisposed()) {
            mPrintDisposable.dispose();
            mPrintDisposable = null;
        }
        mPrintDisposable = disposable;
    }

    @Override
    public ISlaveComputer getSlaveComputer() {
        return mSlaveComputer;
    }

    @Override
    public Observable<Integer> getPrintStateObservable() {
        return mPrintStateSubject.hide();
    }

    @Override
    public void setListener(PrintListener listener) {
        mListener = listener;
    }

    @Override
    public int getProgressCount() {
        return mGcodePlayer.getProgressCount();
    }

    @Override
    public float getProgress() {
        return mGcodePlayer.getProgress();
    }

    @Override
    public void setFile(IFile file) {
        mGcodePlayer.setGcodeFile(file);
    }

    @Override
    public void setInputStream(InputStream inputStream) {
        mGcodePlayer.setInputStream(inputStream);
    }

    /**
     * Pause when filament sensor triggered that filament used out.
     */
    @Override
    public void pauseOnFilamentUsedOut() {
        // Controller is already in pause status automatically, don't send pause command.
        mPrintStateSubject.onNext(STATE_PAUSED);
    }

    @Override
    public void pauseOnEnclosureDoorDetected() {
        if (mPrintStateSubject.getValue() == STATE_PRINTING) {
            // Controller is already in pause status automatically, don't send pause command.
            mPrintStateSubject.onNext(STATE_PAUSED);
        }
    }

    @Override
    public void onEmergencyStop() {
        reset();
        if (mGcodePlayer != null) {
            mGcodePlayer.reset();
        }
    }

    @Override
    public boolean getOverrideNozzleTemperatureDirty() {
        return mGcodeModifier.getOverrideNozzleTemperatureDirty();
    }

    @Override
    public float getOverrideNozzleTemperature() {
        return mGcodeModifier.getOverrideNozzleTemperature();
    }

    @Override
    public void setOverrideNozzleTemperature(float temp) {
        mGcodeModifier.setOverrideNozzleTemperature(temp);
        UpdateChange();
    }

    @Override
    public boolean getOverrideHeatedBedTemperatureDirty() {
        return mGcodeModifier.getOverrideHeatedBedTemperatureDirty();
    }

    @Override
    public float getOverrideHeatedBedTemperature() {
        return mGcodeModifier.getOverrideHeatedBedTemperature();
    }

    @Override
    public void setOverrideHeatedBedTemperature(float temp) {
        mGcodeModifier.setOverrideHeatedBedTemperature(temp);
        UpdateChange();
    }

    @Override
    public boolean getOverrideLaserPowerDirty() {
        return mGcodeModifier.getOverrideLaserPowerDirty();
    }

    @Override
    public float getOverrideLaserPower() {
        return mGcodeModifier.getOverrideLaserPower();
    }

    @Override
    public void setOverrideLaserPower(float power) {
        mGcodeModifier.setOverrideLaserPower(power);
        UpdateChange();
    }

    @Override
    public boolean getRecoveryFlag() {
        return mRecoveryFlag;
    }

    @Override
    public float getOverrideInitialHeatedBedTemperature() {
        return mGcodeModifier.getOverrideInitialHeatedBedTemperature();
    }

    @Override
    public void setOverrideInitialHeatedBedTemperature(float temp) {
        mGcodeModifier.setOverrideInitialHeatedBedTemperature(temp);
        mIsOverride = true;
    }

    @Override
    public void setPowerOutageFlag(boolean flag) {
        mRecoveryFlag = flag;
    }

    @Override
    public Observable<Boolean> getResumeObservable() {
        return mResumeSubject;
    }

    @Override
    public Integer getPrintState() {
        return mPrintStateSubject.getValue();
    }

    @Override
    public boolean getInitialM190Flag() {
        return mGcodeModifier.getInitialM190Marker();
    }

    @Override
    public float getOverrideFeedRate() {
        return mGcodeModifier.getOverrideFeedRate();
    }

    @Override
    public void setOverrideFeedRate(float feedRate) {
        mGcodeModifier.setOverrideFeedRate(feedRate);
        UpdateChange();
    }

    @Override
    public float getOverrideInitialNozzleTemperature() {
        return mGcodeModifier.getOverrideInitialNozzleTemperature();
    }

    @Override
    public void setOverrideInitialNozzleTemperature(float temp) {
        mGcodeModifier.setOverrideInitialNozzleTemperature(temp);
        mIsOverride = true;
    }

    @Override
    public boolean getInitialM109Flag() {
        return mGcodeModifier.getInitialM109Marker();
    }

    @Override
    public float getOverrideZOffset() {
        return mGcodeModifier.getOverrideZOffset();
    }

    @Override
    public void setOverrideZOffset(float zOffset) {
        mGcodeModifier.setOverrideZOffset(zOffset);
        UpdateChange();
    }

    @Override
    public ModelBoundary getModelBoundary() {
        return mModelBoundary;
    }

    @Override
    public void setModelBoundary(ModelBoundary boundary) {
        mModelBoundary = boundary;
    }

    @Override
    public void setResume() {
        mResumeSubject.onNext(true);
    }

    @Override
    public void disposeAll() {
        dispose(compositeDisposable);
        dispose(mPrintDisposable);
    }

    private void dispose(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    @Override
    public TickCounter getTickCounter() {
        return mTickCounter;
    }

    @Override
    public Observable<Integer> getPauseState() {
        return mSlaveComputer.watchPrintPauseState();
    }

    @Override
    public void masterPause() {
        mPrintStateSubject.onNext(STATE_PAUSED);
        mListener.onPauseSuccess();
    }

    @Override
    public Observable<SSTPPacketContent.HeaderSecurity> getHeaderSecurityStatus() {
        return mSlaveComputer.requestHeaderSecurityStatus();
    }

    @Override
    public int getTotalLines() {
        return mGcodePlayer.getTotalCount();
    }

    @Override
    public void setTotalLines(int lines) {
        mGcodePlayer.setTotalCount(lines);
    }

    /**
     * Finish printing (Hit to file end).
     */
    @Override
    public void finish() {
        if (mPrintStateSubject.getValue() != STATE_PRINTING) {
            mListener.onFinishFailed(1); // unexpected state
            return;
        }

        mPrintStateSubject.onNext(STATE_COMPLETED);

        Logger.i("Finish print.");

        Disposable sub = mSlaveComputer.finish()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(retCode -> {
                    if (retCode == 0) {
                        mListener.onFinishSuccess();
                    } else {
                        mListener.onFinishFailed(retCode);
                    }
                }, e -> {
                    LogHelper.log(e);
                    mListener.onFinishFailed(1);
                });
        setActionDisposable(sub);
    }
}
