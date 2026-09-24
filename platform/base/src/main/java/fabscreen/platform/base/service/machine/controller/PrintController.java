package fabscreen.platform.base.service.machine.controller;

import static fabscreen.platform.base.legacy.connection.MockConst.CAMERA_HEIGHT_OFFSET;
import static fabscreen.platform.base.legacy.connection.MockConst.H1_Z_POSITION;
import static fabscreen.platform.base.legacy.connection.MockConst.H2_Z_POSITION;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.data.imgprocess.LaserDistanceMeasureProcess;
import fabscreen.platform.base.instantiation.IServiceIdentifier;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.MockConst;
import fabscreen.platform.base.legacy.connection.SSTPPacketContent;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.print.GcodePlayer;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.lib.print.OnAirGcodeModifier;
import fabscreen.platform.base.lib.print.PrintListener;
import fabscreen.platform.base.lib.print.TickCounter;
import fabscreen.platform.base.model.ModelBoundary;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.ResultStructure;
import fabscreen.platform.base.service.machine.structure.SubscribeStructure;
import fabscreen.platform.base.service.machine.structure.print.BatchBufferInfo;
import fabscreen.platform.base.service.machine.structure.prop.ArrayProp;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.FloatProp;
import fabscreen.platform.base.service.machine.structure.prop.Int16Prop;
import fabscreen.platform.base.service.machine.structure.prop.StringProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt16Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt32Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import okio.Buffer;

public class PrintController implements IServiceIdentifier {
    public static int STATE_IDLE = 0;
    public static int STATE_PRINTING = 1;
    public static int STATE_PAUSED = 2;
    public static int STATE_COMPLETED = 3;

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
    private final BehaviorSubject<Integer> mockLineSubject = BehaviorSubject.createDefault(0);
    private final CompositeDisposable mMockDisposable = new CompositeDisposable();

    private boolean mRecoveryFlag = false;
    private boolean mRemovePrintFlag = false;

    private final PublishSubject<Boolean> mResumeSubject = PublishSubject.create();

    Disposable mPrintModeStatusSubscribe;
    Disposable mExtruderWorkSpeedSubscribe;
    Disposable mGetMachineStatusSubscribe;
    private BehaviorSubject<Integer> mMachineStatusSubject = BehaviorSubject.createDefault(0);
    private BehaviorSubject<Integer> mPrintStateSubject = BehaviorSubject.createDefault(STATE_IDLE);
    private BehaviorSubject<Boolean> mFilamentSubject = BehaviorSubject.createDefault(false);
    private BehaviorSubject<Boolean> mEmergencyStopSubject = BehaviorSubject.createDefault(false);
    private BehaviorSubject<Boolean> mEnclosureSubject = BehaviorSubject.createDefault(false);
    private BehaviorSubject<Float> mCurrentProgressSubject = BehaviorSubject.createDefault(0f);
    private BehaviorSubject<Integer> mPrintModeStatusSubject = BehaviorSubject.createDefault(-1);
    private BehaviorSubject<Integer> mExtruderWorkSpeedSubject = BehaviorSubject.createDefault(0);
    private final BehaviorSubject<Long> mCostTimeSubj = BehaviorSubject.createDefault(0L);
    // true: don't refresh print mode anymore; false: able to refresh
    private boolean mPrintModeLocked;
    //    private final BehaviorSubject<Boolean> mLockPrintModeSubj = BehaviorSubject.createDefault(false);
    MachineConnectionController mConnectionController;
    IMachine mMachine;

    TickCounter mTickCounter = new TickCounter();

    GcodePlayer mGcodePlayer = new GcodePlayer();

    OnAirGcodeModifier mOnAirGcodeModifier = new OnAirGcodeModifier();

    PrintListener mPrintListener;

    private ModelBoundary mModelBoundary;

    //模型最后的打印时间
    public long mPrintTime = 0;

    // TODO:  Unsorted
    private final PublishSubject<Integer> mPausePrintSubject = PublishSubject.create();

    private Disposable mWatchPrintGcodeLineDisposable;

    private Disposable mPrintDisposable;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public PrintController(IMachine mc, MachineConnectionController cc) {
        mMachine = mc;
        mConnectionController = cc;
        mGetMachineStatusSubscribe = mc.getMachineStatusSubjectHolder()
                .getObservable()
                .subscribe(machineStatus -> {
                    if (mMachineStatusSubject.getValue() != machineStatus.status) {
                        Logger.d("mMachineStatus is Change: old:%d\tnew:%d", mMachineStatusSubject.getValue(), machineStatus.status);
                        mMachineStatusSubject.onNext(machineStatus.status);
                    }
                });

        watchPrintCostTimeChange();
    }

    public Observable<Boolean> getFilamentSubjectObservable() {
        return mFilamentSubject.hide();
    }

    public void setFilament(Boolean confirm) {
        mFilamentSubject.onNext(!confirm);
    }

    public Observable<Boolean> getEmergencyStopSubjectObservable() {
        return mEmergencyStopSubject.hide();
    }

    // FIXME: Emergency recover.
    public void setEmergencyStop(Boolean confirm) {
        mEmergencyStopSubject.onNext(!confirm);
    }

    public Observable<Boolean> getEnclosureSubjectObservable() {
        return mEnclosureSubject.hide();
    }

    public void setEnclosure(Boolean confirm) {
        mEnclosureSubject.onNext(!confirm);
    }

    public TickCounter getTickCounter() {
        return mTickCounter;
    }

    Observable<IStructure> getActualPrintLineNum() {
        return null;
    }

    Observable<IStructure> getPrintFileName() {
        return null;
    }

    public Observable<Integer> getPrintStateObservable() {
        return mPrintStateSubject.hide();
    }

    public void setListener(PrintListener listener) {
        mPrintListener = listener;
    }

    public PrintListener getListener() {
        return mPrintListener;
    }

    public int getProgressCount() {
        return mGcodePlayer.getProgressCount();
    }

    /**
     * @return Print progress fraction, from 0 to 1.
     */
    public float getProgress() {
        return mCurrentProgressSubject.getValue();
//        return mGcodePlayer.getProgress();
    }

    public void setFile(IFile file) {
        mGcodePlayer.setGcodeFile(file);
    }

    public int getTotalLines() {
        return mGcodePlayer.getTotalCount();
    }

    public void setTotalLines(int lines) {
        mGcodePlayer.setTotalCount(lines);
    }

    /**
     * Pass in the Gcode file inputStream.
     * Apk built-in print files are entered as input streams
     * Network input streams are supported, but the correct number of lines cannot be displayed
     *
     * @param inputStream
     */
    public void setInputStream(InputStream inputStream) {
        mGcodePlayer.setInputStream(inputStream);
    }

    public ModelBoundary getModelBoundary() {
        return mModelBoundary;
    }

    public void setModelBoundary(ModelBoundary boundary) {
        mModelBoundary = boundary;
    }

    public void setResume() {
        mResumeSubject.onNext(true);
    }

    public void clearPowerOutageFlag() {

    }

    public Observable<Integer> resetErrorFlag() {
        return Observable.just(0);
    }

    public boolean getRecoveryFlag() {
        return mRecoveryFlag;
    }

    private void prepare() {
        compositeDisposable.clear();

        clearBatch();
    }

    public void start() {
        if (mPrintStateSubject.getValue() != STATE_IDLE) {
            Logger.w("Start state is not idle");
            mPrintListener.onStartFailed(1); // unexpected state
            return;
        }
        Logger.i("Start print.");
        mPrintModeLocked = false;
        prepare();
        // Fixme: Re-consider prepare print logic and adjust settings definition.
//        Disposable sub = mSlaveComputer.requestAdjustSettingLaserPower(getOverrideLaserPower()).subscribe();
//        setActionDisposable(sub);
        IPrintWorkspace workspace = ServiceContainer.getInstance().getService(IPrintWorkspace.class);
        String md5 = workspace.getFileMD5Value();
        String testFileName = workspace.getFileName();
        IMachine.WorkType workType = mMachine.getMachineInfoSubjectHolder().getValue().workType;
        int type = -1;
        switch (workType) {
            case FDM:
                type = 0;
                break;
            case CNC:
                type = 1;
                break;
            case LASER:
                type = 2;
                break;
            default:
                break;
        }
        int finalType = type;
        BaseStructure gcodeFileInfo = new BaseStructure() {
            @Override
            protected void init() {
                addProp("md5", new StringProp(md5));
                addProp("filename", new StringProp(testFileName));
                addProp("type", new UInt8Prop(finalType));
            }
        };

        Disposable sub = requestPrintStart(gcodeFileInfo)
                .flatMap(resultStructure -> {
                    // After the master control status changes successfully, request the master control to start batch printing
//                    if (resultStructure.isSuccess()) {
//                        return useBatchGcodeMode(1).onExceptionResumeNext(Observable.just(-1));
//                    } else {
//                        return Observable.just(resultStructure.resultProp.getValue());
//                    }
                    return Observable.just(resultStructure.resultProp.getValue());
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(retCode -> {
                    if (retCode == 0) {
                        // Clear the flag bit of remote start printing after the printing starts
                        mRemovePrintFlag = false;
                        mPrintStateSubject.onNext(STATE_PRINTING);
                        mPrintListener.onStartSuccess();
                        watchPrintingLineNo();
                    } else {
                        mPrintListener.onStartFailed(retCode);
                    }
                }, e -> {
                    LogHelper.log(e);
                    mPrintListener.onStartFailed(1);
                });
        setActionDisposable(sub);
    }

    public void setPrintTime(long time) {
        mPrintTime = time;
    }

    public long getPrintTime() {
        return mPrintTime;
    }

    public void pause() {
        if (mPrintStateSubject.getValue() != STATE_PRINTING) {
            mPrintListener.onPauseFailed(1); // unexpected state
            return;
        }

        Logger.i("Pause print.");
        Disposable sub = requestPrintPause()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resultStructure -> {
                    Logger.d("pause result " + resultStructure.resultProp);
                    if (resultStructure.isSuccess()) {
                        mPrintStateSubject.onNext(STATE_PAUSED);
                        mPrintListener.onPauseSuccess();
                    } else {
                        mPrintListener.onPauseFailed(resultStructure.resultProp.getValue());
                    }
                }, e -> {
                    LogHelper.log(e);
                    mPrintListener.onPauseFailed(1);
                });
        setActionDisposable(sub);
    }

    public void resume() {
        if (mPrintStateSubject.getValue() != STATE_PAUSED) {
            mPrintListener.onResumeFailed(1); // unexpected state
            return;
        }
        Logger.i("Resume print.");

        Disposable sub = requestPrintResume().flatMap(resultStructure -> {
            // After the master control status changes successfully, request the master control to start batch printing
//            if (resultStructure.isSuccess()) {
////                return useBatchGcodeMode(1).onExceptionResumeNext(Observable.just(-1));
//            } else {
            return Observable.just(resultStructure.resultProp.getValue());
//            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(retCode -> {
                    Logger.d("resume result " + retCode);
                    if (retCode == 0) {
                        mPrintStateSubject.onNext(STATE_PRINTING);
                        mPrintListener.onResumeSuccess();
                    } else {
                        mPrintListener.onResumeFailed(retCode);
                    }
                }, e -> {
                    LogHelper.log(e);
                    mPrintListener.onResumeFailed(1);
                });
        setActionDisposable(sub);
    }

    public Observable<ResponseStructure> NonPrintStop() {
        return requestPrintStop()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(resultStructure -> {
                    mPrintStateSubject.onNext(STATE_IDLE);
                    mCurrentProgressSubject.onNext(0f);
                    getTickCounter().stop();
                });
    }

    // FIXME:
    public void setPrintState(int state) {
        mPrintStateSubject.onNext(STATE_IDLE);
    }

    public void stop() {
        if (mPrintStateSubject.getValue() == STATE_IDLE) {
            mPrintListener.onStopFailed(1); // unexpected state
            return;
        }
        Logger.i("Stop print.");
        Disposable sub = requestPrintStop()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resultStructure -> {
                    if (resultStructure.isSuccess()) {
                        mPrintStateSubject.onNext(STATE_IDLE);
                        mPrintListener.onStopSuccess();
                        mCurrentProgressSubject.onNext(0f);
                        unWatchPrintingLineNo();
                    } else {
                        mPrintListener.onStopFailed(resultStructure.resultProp.getValue());
                    }
                }, e -> {
                    LogHelper.log(e);
                    mPrintListener.onStopFailed(1);
                });
        setActionDisposable(sub);
    }

    public void recover() {
        if (mPrintStateSubject.getValue() != STATE_IDLE) {
            mPrintListener.onResumeFromPowerOutageFailed(1); // unexpected state
            return;
        }
        Logger.i("Recover print.");
        prepare();
        watchPrintingLineNo();
        Disposable sub = requestPrintResumeFromPowerOutage().observeOn(AndroidSchedulers.mainThread())
                .flatMap(resultStructure -> {
//                    if (resultStructure.isSuccess()) {
//                        return useBatchGcodeMode(1).onExceptionResumeNext(Observable.just(-1));
//                    } else {
                    return Observable.just(resultStructure.resultProp.getValue());
//                    }
                })
                .subscribe(result -> {
                    if (result == 0) {
                        mPrintStateSubject.onNext(STATE_PRINTING);
                        mPrintListener.onResumeFromPowerOutageSuccess();
                    } else {
                        mPrintListener.onResumeFromPowerOutageFailed(result);
                    }
                }, e -> {
                    LogHelper.log(e);
                    mPrintListener.onResumeFromPowerOutageFailed(1);
                });
        setActionDisposable(sub);
    }

    public void finish() {
        if (mPrintStateSubject.getValue() != STATE_PRINTING) {
            mPrintListener.onFinishFailed(1); // unexpected state
            return;
        }

        mPrintStateSubject.onNext(STATE_COMPLETED);

        Logger.i("Finish print.");

        Disposable sub = requestPrintFinish()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resultStructure -> {
                    if (resultStructure.getStatus() == 0) {
                        mCurrentProgressSubject.onNext(0f);
                        mPrintListener.onFinishSuccess();
                        unWatchPrintingLineNo();
                    } else {
                        mPrintListener.onFinishFailed(resultStructure.getStatus());
                    }
                }, e -> {
                    LogHelper.log(e);
                    mPrintListener.onFinishFailed(1);
                });
        setActionDisposable(sub);
    }
    // TODO: Checkout these function for safe.

    public void disposeAll() {
        dispose(compositeDisposable);
        dispose(mPrintDisposable);
    }

    private void dispose(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    public void reset() {
        mCurrentProgressSubject.onNext(0f);
        mOnAirGcodeModifier.reset();

        if (mPrintDisposable != null && !mPrintDisposable.isDisposed()) {
            mPrintDisposable.dispose();
            mPrintDisposable = null;
        }

        if (mWatchPrintGcodeLineDisposable != null && !mWatchPrintGcodeLineDisposable.isDisposed()) {
            mWatchPrintGcodeLineDisposable.dispose();
        }
        mPrintStateSubject.onNext(STATE_IDLE);
        // Temporary mock reset
        mockLineSubject.onNext(0);
    }

    public void setActionDisposable(Disposable disposable) {
        if (mPrintDisposable != null && !mPrintDisposable.isDisposed()) {
            mPrintDisposable.dispose();
            mPrintDisposable = null;
        }
        mPrintDisposable = disposable;
    }

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

    /**
     * Actual next function, send G-code or resend G-code.
     * Query batch corresponding to the start line number of the output based on the line number
     * of the master control request.
     *
     * @param lineNo
     */
    private ResponseStructure<BaseStructure> doNext(int lineNo, int requestBatchLength) {
        mBatchCode = new BatchCode("", lineNo, lineNo - 1);
        StringBuilder linesBuffer = new StringBuilder();
        int batchLength = 0;

        /**
         *                    -->-->-->-->-->     Gcode line order     -->-->-->-->-->
         *     (lineNo < mStartBatchNo)     |       (mBatches[])        |   (lineNo > mEndBatchNo)
         */
        if (mGcodePlayer.gcodeIsEmpty()) {
            return buildGcodeBatchResponseStructure();
        } else if (lineNo > mEndBatchNo) {
            // Not starting at line 0 is supported
            if (mNowBatchNo == -1) {
                Logger.d("new batch coming in, getFile and start at %d line", lineNo);
                mGcodePlayer.setLineno(lineNo);
                mGcodePlayer.nextLine();
            }

            // Skipping lines until we reach the request line no.
            while (mGcodePlayer.getLineNo() < lineNo) {
                mGcodePlayer.nextLine();
            }
            // Failed to obtain Gcode, complete the task
            if (mGcodePlayer.getLine() == null) {
//                Logger.w("Could not get G-code at %d line, requesting line %d(%d Bytes)",
//                        mGcodePlayer.getLineNo(), lineNo, requestBatchLength);
                return buildGcodeBatchResponseStructure();
            }

            while (batchLength < requestBatchLength) {
                String line = mGcodePlayer.getLine();
                if (mGcodePlayer.getLine() == null) {
                    // Failed to obtain Gcode, complete the task, break
                    break;
                }

                // Replace comments in Gcode code
                int index = line.indexOf(";");
                if (index != -1) {
                    line = line.substring(0, index + 1);
                }
                line += "\n";
                batchLength += line.getBytes().length;
                if (batchLength < requestBatchLength) {
                    // Current line counts on, and we have more space to fill in.
                    // Continue reading next line.
                    linesBuffer.append(line);
                    mGcodePlayer.nextLine();
                }
            }

            // Get enough batch G-code(s) under requestBatchLength, start filling data.
            String tempBatch = linesBuffer.toString();
            mNowBatchNo = lineNo;
            mEndBatchNo = mGcodePlayer.getLineNo() - 1;
            // Fill data into mBatchCode
            mBatchCode = new BatchCode(tempBatch, mNowBatchNo, mEndBatchNo);

            // Cache batch into batches[]
            int batchIndex = (getBatch(lineNo - 1) + 1) % BATCHES_NUMS;
            mBatches[batchIndex] = mBatchCode;
        } else if (lineNo < mStartBatchNo) {
            Logger.d("Abnormal: LineNo %s < mStartBatchNo %s,mEndBatchNo:%s", lineNo, mStartBatchNo, mEndBatchNo);
            // Request line was way before our batches.
            // In order to return the "pointer", clean up batches from now and start over again.
            clearBatch();
            return doNext(lineNo, requestBatchLength);
        } else {
            //  Reissue or resend
            int batchIndex = getBatch(lineNo);
            // Failed to find the required batch
            if (batchIndex == -1) {
                Logger.d("Error: Batch can't find :" + lineNo);
                clearBatch();
                return doNext(lineNo, requestBatchLength);
            }
            BatchCode tempBatch = mBatches[batchIndex];
            String[] tempGcodes = tempBatch.getGcodes().split("\n");
            for (int i = lineNo - tempBatch.getStartNo(); i < tempGcodes.length; i++) {
                linesBuffer.append(tempGcodes[i]).append("\n");
            }
            mBatchCode = new BatchCode(linesBuffer.toString(), lineNo, tempBatch.getEndNo());
        }
        return buildGcodeBatchResponseStructure();
    }

    private ResponseStructure<BaseStructure> buildGcodeBatchResponseStructure() {
        mStartBatchNo = getStartBatchNo();
        long startLineNo = mBatchCode.getStartNo();
        long endLineNo = mBatchCode.getEndNo();
        ResponseStructure<BaseStructure> responseStructure = new ResponseStructure<>();
        responseStructure.dataProp = new BaseStructure() {
            @Override
            protected void init() {
                addProp("startLineNo", new UInt32Prop(startLineNo));
                addProp("endLineNo", new UInt32Prop(endLineNo));
                addProp("gcodeBatch", new StringProp(mBatchCode.getGcodes()));
            }
        };

        if (startLineNo > endLineNo) {
            // no more gcode lines
            mPrintModeLocked = true;
            responseStructure.resultProp = new UInt8Prop(201);
        }
//        Logger.d("---FDT--- sendGcode startLineNo:%d,endLineNo:%d,gcodeBatch length:%d", startLineNo, endLineNo, mBatchCode.getGcodes().length());
        return responseStructure;
    }

    public void pauseOnFilamentUsedOut() {
        // Controller is already in pause status automatically, don't send pause command.
        mPrintStateSubject.onNext(STATE_PAUSED);
    }

    public void pauseOnEnclosureDoorDetected() {
        if (mPrintStateSubject.getValue() == STATE_PRINTING) {
            // Controller is already in pause status automatically, don't send pause command.
            mPrintStateSubject.onNext(STATE_PAUSED);
        }
    }

    public void onEmergencyStop() {
        reset();
        if (mGcodePlayer != null) {
            mGcodePlayer.reset();
        }
    }

    // machine active pause print
    public void masterPause() {
        mPrintStateSubject.onNext(STATE_PAUSED);
        mPrintListener.onPauseSuccess();
    }

    private int getStartBatchNo() {
        int batchNo = -1;
        if (mBatches == null) return batchNo;
        for (int i = 0; i < mBatches.length; i++) {
            if (mBatches[i] == null) {
                continue;
            }
            batchNo = batchNo == -1 ? mBatches[i].getStartNo() : Math.min(batchNo, mBatches[i].getStartNo());
        }
        return batchNo;
    }

    private void clearBatch() {
        mBatches = new BatchCode[BATCHES_NUMS];
        mEndBatchNo = -1;
        mNowBatchNo = -1;
        mStartBatchNo = -1;
    }

    public Observable<Integer> getPauseState() {
        return mPausePrintSubject.hide();
    }

    public Observable<HeaderSecurity> getHeaderSecurityStatus() {
        // TODO: Request from connection

        HeaderSecurity security = new HeaderSecurity(((byte) 0x00));
        return Observable.just(security);
    }

    /**
     * Print Settings
     */
    public Observable<AdjustSettings> getAdjustSettingFeedRate() {
        return null;
    }

    public Observable<AdjustSettings> getAdjustSettingLaserPower() {
        return null;
    }

    public Observable<AdjustSettings> getAdjustSettingZOffset() {
        return null;
    }

    public Observable<Integer> requestAdjustSetting(int type, float value) {
        return null;
    }

    public Observable<Integer> requestAdjustSettingFeedRate(float value) {
        return null;
    }

    public Observable<Integer> requestAdjustSettingNozzleTemp(float value) {
        return null;
    }

    public Observable<Integer> requestAdjustSettingHeatedBedTemp(float value) {
        return null;
    }

    public Observable<Integer> requestAdjustSettingLaserPower(float value) {
        return null;
    }

    public Observable<Integer> requestAdjustSettingZOffset(float value) {
        return null;
    }

    public float getOverrideInitialHeatedBedTemperature() {
        return 0;
    }

    public void setOverrideInitialHeatedBedTemperature(float temp) {

    }

    public void setPowerOutageFlag(boolean flag) {
        mRecoveryFlag = flag;
    }

    public Observable<Boolean> getResumeObservable() {
        return mResumeSubject;
    }

    public int getPrintState() {
        return mPrintStateSubject.getValue();
    }

    public boolean getInitialM190Flag() {
        return mOnAirGcodeModifier.getInitialM190Marker();
    }

    public float getOverrideFeedRate() {
        return mOnAirGcodeModifier.getOverrideFeedRate();
    }

    public void setOverrideFeedRate(float feedRate) {
        mOnAirGcodeModifier.setOverrideFeedRate(feedRate);
        UpdateChange();
    }

    public float getOverrideInitialNozzleTemperature() {
        return 0;
    }

    public void setOverrideInitialNozzleTemperature(float temp) {
        mOnAirGcodeModifier.setOverrideInitialNozzleTemperature(temp);
        mIsOverride = true;
    }

    public boolean getInitialM109Flag() {
        return mOnAirGcodeModifier.getInitialM109Marker();
    }

    public float getOverrideZOffset() {
        return mOnAirGcodeModifier.getOverrideZOffset();
    }

    public void setOverrideZOffset(float zOffset) {
        mOnAirGcodeModifier.setOverrideZOffset(zOffset);
        UpdateChange();
    }

    public boolean getOverrideNozzleTemperatureDirty() {
        return mOnAirGcodeModifier.getOverrideNozzleTemperatureDirty();
    }

    public float getOverrideNozzleTemperature() {
        return mOnAirGcodeModifier.getOverrideNozzleTemperature();
    }

    public void setOverrideNozzleTemperature(float temp) {
        mOnAirGcodeModifier.setOverrideNozzleTemperature(temp);
        UpdateChange();
    }

    public boolean getOverrideHeatedBedTemperatureDirty() {
        return mOnAirGcodeModifier.getOverrideHeatedBedTemperatureDirty();
    }

    public float getOverrideHeatedBedTemperature() {
        return mOnAirGcodeModifier.getOverrideHeatedBedTemperature();
    }

    public void setOverrideHeatedBedTemperature(float temp) {
        mOnAirGcodeModifier.setOverrideHeatedBedTemperature(temp);
        UpdateChange();
    }

    public boolean getOverrideLaserPowerDirty() {
        return mOnAirGcodeModifier.getOverrideLaserPowerDirty();
    }

    public float getOverrideLaserPower() {
        return mOnAirGcodeModifier.getOverrideLaserPower();
    }

    public void setOverrideLaserPower(float power) {
        mOnAirGcodeModifier.setOverrideLaserPower(power);
        UpdateChange();
    }

    private void UpdateChange() {
//        Observable<Integer> action = mOnAirGcodeModifier.getModifyAction(null);
//        if (action != null) {
//            compositeDisposable.add(action.subscribe());
//        }
    }

    public void onMachineReportPrintIssue(int commandSet, int commandId, int sequence, UInt8Prop printIssue) {
        int issueCode = printIssue.getValue();
        Logger.d("Print Issue %d request", issueCode);

        switch (issueCode) {
            case 0:
                Logger.d("Print finished triggered.");
                // FIXME: Temporary work around. Server will return this issue no matter print job is finish by G-code end or G-code
                if (mPrintStateSubject.getValue() != STATE_IDLE) {
                    mPrintStateSubject.onNext(STATE_COMPLETED);
                    if (mPrintListener != null) {
                        mPrintListener.onFinishSuccess();
                    }
                }
                break;
            case 1:
                Logger.d("G-code paused event triggered.");
                // paused triggered by gcode
                mPrintStateSubject.onNext(STATE_PAUSED);
                mTickCounter.stop();
                break;
            case 2:
                Logger.d("G-code filament event triggered.");
                // filament paused triggered by gcode
                mPrintStateSubject.onNext(STATE_PAUSED);
                mTickCounter.stop();
                break;
            case 3:
                // filament runout event
                Logger.w("Filament runout event triggered.");
                mPrintStateSubject.onNext(STATE_PAUSED);
                mTickCounter.stop();
                mFilamentSubject.onNext(true);
                break;
            case 4:
                mPrintStateSubject.onNext(STATE_PAUSED);
                mTickCounter.stop();
                Logger.w("Extruder stuck safety triggered.");
                break;
            case 5:
                mPrintStateSubject.onNext(STATE_PAUSED);
                mTickCounter.stop();
                Logger.w("Print temperature abnormal triggered.");
                break;
            case 6:
                mPrintStateSubject.onNext(STATE_PAUSED);
                mTickCounter.stop();
                Logger.w("Print batch not match reported by controller.");
                break;
            case 7:
                mPrintStateSubject.onNext(STATE_PAUSED);
                mTickCounter.stop();
                Logger.w("Get G-code batch failed reported by controller.");
                break;
            case 8:
                Logger.w("Emergency Stop triggered reported by controller.");
                mEmergencyStopSubject.onNext(true);
                mTickCounter.stop();
                mPrintStateSubject.onNext(STATE_PAUSED);
                break;
            case 9:
                mPrintStateSubject.onNext(STATE_PAUSED);
                mTickCounter.stop();
                Logger.w("machine tool head recover failed reported by controller.");
                break;
            case 10:
                Logger.w("machine stop args failed reported by controller.");
                break;
            case 11:
                Logger.w("machine stop failed reported by controller.");
                break;
            case 12:
                Logger.w("client request stop reported by controller.");
                break;
            case 13:
                Logger.w("machine pause args failed reported by controller.");
                break;
            case 14:
                Logger.w("machine environment abnormal reported by controller.");
                break;
            case 15:
                Logger.w("machine pause failed reported by controller.");
                break;
            case 16:
                Logger.w("enclosure interrupted reported by controller.");
                mPrintStateSubject.onNext(STATE_PAUSED);
                mTickCounter.stop();
                mEnclosureSubject.onNext(true);
                break;
            case 20:
                mPrintStateSubject.onNext(STATE_PAUSED);
                mTickCounter.stop();
                Logger.w("Paused due to exception.");
                break;

            case 255:
                mPrintStateSubject.onNext(STATE_PAUSED);
                mTickCounter.stop();
                Logger.w("Controller report position failure");
            default:
                mPrintStateSubject.onNext(STATE_PAUSED);
                mTickCounter.stop();
                Logger.w("event triggered, issueCode %d", issueCode);
                break;
        }
        ResponseStructure responseStructure = new ResponseStructure();
        responseStructure.resultProp = new UInt8Prop(0);
        mConnectionController.sendResponse(commandSet, commandId, sequence, responseStructure);
    }

    public void setRemovePrintFlag(boolean removePrintFlag) {
        mRemovePrintFlag = removePrintFlag;
    }

    public boolean getRemovePrintFlag() {
        return mRemovePrintFlag;
    }

    public Observable<ResponseStructure> subscribePrintModeStatus() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0xac, 0xa1, 1000);
        return mConnectionController.request(0x01, 0x00, subscribeStructure, new ResponseStructure())
                .doOnNext(responseStructure -> {
                    if (mPrintModeStatusSubscribe != null && !mPrintModeStatusSubscribe.isDisposed()) {
                        mPrintModeStatusSubscribe.dispose();
                    }
                    mPrintModeStatusSubscribe = mConnectionController.watch(0xac, 0xa1, new ResponseStructure(new UInt8Prop()))
                            .subscribe(responseStructure1 -> {
                                mPrintModeStatusSubject.onNext(((UInt8Prop) responseStructure1.dataProp).getValue());
                            }, LogHelper::log);
                });
    }

    public Observable<ResponseStructure> unsubscribePrintModeStatus() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0xac, 0xa1, 0);
        return mConnectionController.request(0x01, 0x01, subscribeStructure, new ResponseStructure())
                .doOnNext(responseStructure -> {
                    mConnectionController.unWatch(0xac, 0xa1);
                    if (mPrintModeStatusSubscribe != null && !mPrintModeStatusSubscribe.isDisposed()) {
                        mPrintModeStatusSubscribe.dispose();
                    }
                });
    }

    public Observable<Integer> getPrintModeStatusObservable() {
        return mPrintModeStatusSubject.hide();
    }

    public Integer getPrintModeStatusValue() {
        return mPrintModeStatusSubject.getValue();
    }

    public Observable<ResponseStructure> subscribeExtruderWorkSpeedObservable() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0xac, 0xa2, 1000);
        return mConnectionController.request(0x01, 0x00, subscribeStructure, new ResponseStructure())
                .doOnNext(responseStructure -> {
                    if (mExtruderWorkSpeedSubscribe != null && !mExtruderWorkSpeedSubscribe.isDisposed()) {
                        mExtruderWorkSpeedSubscribe.dispose();
                    }
                    mExtruderWorkSpeedSubscribe = mConnectionController.watch(0xac, 0xa2, new ResponseStructure<>(new UInt16Prop()))
                            .subscribe(responseStructure1 -> {
                                mExtruderWorkSpeedSubject.onNext(responseStructure1.dataProp.getValue());
                            }, LogHelper::log);
                });
    }

    public Observable<ResponseStructure> unSubscribeExtruderWorkSpeedObservable() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0xac, 0xa2, 0);
        return mConnectionController.request(0x01, 0x01, subscribeStructure, new ResponseStructure())
                .doOnNext(responseStructure -> {
                    mConnectionController.unWatch(0xac, 0xa1);
                    if (mExtruderWorkSpeedSubscribe != null && !mExtruderWorkSpeedSubscribe.isDisposed()) {
                        mExtruderWorkSpeedSubscribe.dispose();
                    }
                });
    }

    public ResponseStructure<BaseStructure> onMachineRequestBatchBufferInfo(BatchBufferInfo info) {
        // Do business and send response.
        int requestLineNo = (int) info.getLineNo();
        int batchLength = (int) info.getBatchBufferLength();
        if (requestLineNo % 1000 == 0) {
            Logger.d("Request LineNo %s , batchLength %d", String.valueOf(requestLineNo), batchLength);
        }
        return doNext(requestLineNo, batchLength);
    }

    // TODO: Unsorted cls

    static class BatchCode {
        String gcodes;
        int mStartNo;
        int mEndNo;

        public BatchCode(String gcodes, int mStartNo, int mEndNo) {
            this.gcodes = gcodes;
            this.mStartNo = mStartNo;
            this.mEndNo = mEndNo;
        }

        public String getGcodes() {
            return gcodes;
        }

        public void setGcodes(String gcodes) {
            this.gcodes = gcodes;
        }

        public int getStartNo() {
            return mStartNo;
        }

        public void setStartNo(int mStartNo) {
            this.mStartNo = mStartNo;
        }

        public int getEndNo() {
            return mEndNo;
        }

        public void setEndNo(int mEndNo) {
            this.mEndNo = mEndNo;
        }

        public int getLentNum() {
            return mEndNo - mStartNo;
        }
    }

    public static class AdjustSettings {
        public byte retCode;
        public float value;

        @Nullable
        public static SSTPPacketContent.AdjustSettings parse(byte[] content) {
            Buffer buffer = new Buffer();
            buffer.write(content);
            SSTPPacketContent.AdjustSettings adjustSettings = new SSTPPacketContent.AdjustSettings();
            try {
                buffer.readByte();

                adjustSettings.retCode = buffer.readByte();
                adjustSettings.value = buffer.readInt() / 1000.0f;
            } catch (IOException e) {
                LogHelper.log(e);
                return null;
            }
            return adjustSettings;
        }
    }

    public static class HeaderSecurity {
        public static final int HEADER_SENSOR_STATUS = 1;
        public static final int HEADER_TEMPERATURE_ANOMALY = 1 << 1;
        public static final int HEADER_ROLL_ABNORMAL_ANGLE = 1 << 2;
        public byte status = -1;
        public byte temperature;
        public byte rollHigh;
        public byte rollLess;
        public byte pitchHigh;
        public byte pitchLess;

        public HeaderSecurity(byte status) {
            this.status = status;
        }

        public static SSTPPacketContent.HeaderSecurity parse(byte[] content) {
            Buffer buffer = new Buffer();
            buffer.write(content);

            SSTPPacketContent.HeaderSecurity headerSecurity = new SSTPPacketContent.HeaderSecurity((byte) -1);
            try {
                // skip operation id
                buffer.readByte();
                headerSecurity.status = buffer.readByte();
                headerSecurity.temperature = buffer.readByte();
                headerSecurity.rollHigh = buffer.readByte();
                headerSecurity.rollLess = buffer.readByte();
                headerSecurity.pitchHigh = buffer.readByte();
                headerSecurity.pitchLess = buffer.readByte();
            } catch (IOException e) {
                LogHelper.log(e);
            }
            return headerSecurity;
        }
    }

    private void watchPrintingLineNo() {
        if (mWatchPrintGcodeLineDisposable != null && !mWatchPrintGcodeLineDisposable.isDisposed()) {
            mWatchPrintGcodeLineDisposable.dispose();
            mWatchPrintGcodeLineDisposable = null;
        }

        SubscribeStructure subscribeStructure = new SubscribeStructure(0xac, 0xa0, 2000);
        mPrintDisposable = mConnectionController.request(0x01, 0x00, subscribeStructure, new ResponseStructure()).subscribe();
        ResponseStructure<BaseStructure> baseStructureResponseStructure = new ResponseStructure<>();

        BaseStructure watchGcodeLineNoStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("GcodeLineNo", new UInt32Prop());
            }
        };
        baseStructureResponseStructure.resultProp = new UInt8Prop();
        baseStructureResponseStructure.dataProp = watchGcodeLineNoStructure;
        mWatchPrintGcodeLineDisposable = mConnectionController.watch(0xac, 0xa0, baseStructureResponseStructure)
                .subscribe(responseStructure -> {
                    if (!responseStructure.isSuccess()) {
                        Logger.d("watch print response error, error code %d", responseStructure.resultProp.getValue());
                    } else {
                        BaseStructure baseStructure = responseStructure.dataProp;
                        long lineNo = ((UInt32Prop) baseStructure.getProp("GcodeLineNo")).getValue();
                        Logger.d("Print %s gcode line now...", String.valueOf(lineNo));
                        mCurrentProgressSubject.onNext(calculatePrintProgress(lineNo));
                    }
                }, LogHelper::log);
    }

    private void unWatchPrintingLineNo() {
        if (mWatchPrintGcodeLineDisposable != null && !mWatchPrintGcodeLineDisposable.isDisposed()) {
            mWatchPrintGcodeLineDisposable.dispose();
            mWatchPrintGcodeLineDisposable = null;
        }

        mConnectionController.unWatch(0xac, 0xa0);
        mConnectionController.request(0x01, 0x01, new SubscribeStructure(0xac, 0xa0, 0), new ResponseStructure()).subscribe();
    }

    float calculatePrintProgress(long lineNo) {
        // Handle boundary condition.

        if (lineNo < 0) return 0f;
        int totalCount = mGcodePlayer.getTotalCount();
        if (lineNo > totalCount) {
            return 1f;
        }

        return (1.0f * lineNo / totalCount) * 10 / 10.0f;
    }

    Observable<ResponseStructure> requestPrintFileInfo() {
        return mConnectionController.request(0xac, 0x00, null, new ResponseStructure());
    }

    Observable<ResponseStructure> requestPrintStart(BaseStructure gcodeFileInfo) {
        return mConnectionController.request(0xac, 0x03, gcodeFileInfo, new ResponseStructure());
//        return Observable.just(result);
    }

    Observable<ResponseStructure> requestPrintPause() {
        return mConnectionController.request(0xac, 0x04, null, new ResponseStructure());
    }

    Observable<ResponseStructure> requestPrintResume() {
        return mConnectionController.request(0xac, 0x05, null, new ResponseStructure());
    }

    Observable<ResponseStructure> requestPrintStop() {
        return mConnectionController.request(0xac, 0x06, null, new ResponseStructure());
    }

    public Observable<ResponseStructure> requestStopOneExtruder(int index) {
        int key = ServiceContainer.getInstance().getService(IMachine.class)
                .getFDMController()
                .getModuleIdFromIndex(index);

        BaseStructure requestStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop(key));
                addProp("enable", new BoolProp(false));
            }
        };
        return mConnectionController.request(0xac, 0x0d, requestStructure, new ResponseStructure());
    }

    public Observable<ResponseStructure> requestPowerOutageStatus() {
        ResponseStructure responseStructure = new ResponseStructure();
        BaseStructure gcodeFileInfo = new BaseStructure() {
            @Override
            protected void init() {
                addProp("md5", new StringProp(""));
                addProp("filename", new StringProp(""));
            }
        };
        responseStructure.resultProp = new UInt8Prop();
        responseStructure.dataProp = gcodeFileInfo;
        return mConnectionController.request(0xac, 0x07, null, responseStructure);
    }

    public Observable<ResponseStructure> requestPrintResumeFromPowerOutage() {
        IPrintWorkspace workspace = ServiceContainer.getInstance().getService(IPrintWorkspace.class);
        String md5 = workspace.getFileMD5Value();
        String filename = workspace.getFileName();
        BaseStructure gcodeFileInfo = new BaseStructure() {
            @Override
            protected void init() {
                addProp("md5", new StringProp(md5));
                addProp("filename", new StringProp(filename));
            }
        };
        return mConnectionController.request(0xac, 0x08, gcodeFileInfo, new ResponseStructure());
    }

    public Observable<ResponseStructure> requestPrintPowerLossClearMarker() {
        return mConnectionController.request(0xac, 0x09, null, new ResponseStructure());
    }

    public Observable<ResponseStructure> requestPrintModeStatus() {
        ResponseStructure<BaseStructure> responseStructure = new ResponseStructure<>();

        responseStructure.dataProp = new BaseStructure() {
            @Override
            protected void init() {
                addProp("printMode", new UInt8Prop());
            }
        };
        return mConnectionController.request(0xac, 0x0B, null, responseStructure);
    }

    public Observable<ResponseStructure> requestChangePrintMode(int printMode) {
        BaseStructure structure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("requestPrintMode", new UInt8Prop(printMode));
            }
        };
        return mConnectionController.request(0xac, 0x0A, structure, new ResponseStructure<>());
    }

    public Observable<ResponseStructure> requestPrintStartOffset(float xOffset, float yOffset, float zOffset) {
        BaseStructure structure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("xOffset", new FloatProp(xOffset));
                addProp("yOffset", new FloatProp(yOffset));
                addProp("zOffset", new FloatProp(zOffset));
            }
        };
        return mConnectionController.request(0xac, 0x0C, structure, new ResponseStructure());
    }

    public Observable<ResponseStructure> requestChangeWorkSpeed(int moduleId, int extruderIndex, float workSpeed) {
        int key = ServiceContainer.getInstance().getService(IMachine.class).getFDMController().getModuleIdFromIndex(moduleId);
        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop(key));
                addProp("extruderIndex", new UInt8Prop(extruderIndex));
                addProp("workSpeed", new FloatProp(workSpeed));
            }
        };
        return mConnectionController.request(0xac, 0x0E, baseStructure, new ResponseStructure());
    }

    public void handlePowerLossFile(ResponseStructure responseStructure) {
        BaseStructure gcodeFileInfo = (BaseStructure) responseStructure.dataProp;
        Logger.d("Power Loss Gcode %s, md5 %s", gcodeFileInfo.getProp("filename").getValue(), gcodeFileInfo.getProp("md5").getValue());
    }

    Observable<ResultStructure> requestPrintFinish() {
        ResultStructure result = new ResultStructure();
        return Observable.just(result);
    }

    //FIXME:  Set the working speed adjustment ratio
    public Observable<ResponseStructure> setExtruderWorkSpeed(int id, int extruderIndex, int speed) {
        FDMController fdmController = ServiceContainer.getInstance().getService(IMachine.class).getFDMController();
        int key = fdmController.getModuleIdFromIndex(id);
        BaseStructure fdmRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("extruderId", new UInt8Prop());
                addProp("speed", new Int16Prop());
            }
        };
        fdmRequest.getProp("key").setValue(key);
        fdmRequest.getProp("extruderId").setValue(extruderIndex);
        fdmRequest.getProp("speed").setValue(speed);
        return mConnectionController.request(0xac, 0x0e, fdmRequest, new ResponseStructure());
    }

    public Observable<ResponseStructure> getExtruderWorkSpeed(IMachine.WorkType workType, int id) {
        int key = -1;
        ResponseStructure responseStructure = new ResponseStructure();
        switch (workType) {
            case FDM:
                key = ServiceContainer.getInstance().getService(IMachine.class).getFDMController().getModuleIdFromIndex(id);
                break;
            case LASER:
                key = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolHeadInfoValue(id).getKey();
                break;
            case CNC:
                key = ServiceContainer.getInstance().getService(IMachine.class).getCNCController().getCncToolHeadInfoValue(id).getKey();
                break;
            case NONE:
            default:
                responseStructure.resultProp = new UInt8Prop(6);
                return Observable.just(responseStructure);
        }
        BaseStructure request = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
            }
        };
        request.getProp("key").setValue(key);

        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("workSpeed", new ArrayProp<>(new UInt16Prop()));
            }
        };
        responseStructure.dataProp = baseStructure;
        return mConnectionController.request(0xac, 0x0f, request, responseStructure);
    }

    public Observable<Integer> getExtruderWorkSpeedObservable() {
        return mExtruderWorkSpeedSubject.hide();
    }

    public Observable<ResponseStructure> setFDMFlowRate(int id, int extruderIndex, int flowRate) {
        FDMController fdmController = ServiceContainer.getInstance().getService(IMachine.class).getFDMController();
        int key = fdmController.getModuleIdFromIndex(id);
        BaseStructure fdmRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("extruderId", new UInt8Prop());
                addProp("flowRate", new Int16Prop());
            }
        };
        fdmRequest.getProp("key").setValue(key);
        fdmRequest.getProp("extruderId").setValue(extruderIndex);
        fdmRequest.getProp("flowRate").setValue(flowRate);
        return mConnectionController.request(0xac, 0x10, fdmRequest, new ResponseStructure());
    }

    public Observable<ResponseStructure> getFDMFlowRate(int id, int extruderIndex) {
        FDMController fdmController = ServiceContainer.getInstance().getService(IMachine.class).getFDMController();
        int key = fdmController.getModuleIdFromIndex(id);
        BaseStructure fdmRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("extruderId", new UInt8Prop());
            }
        };
        fdmRequest.getProp("key").setValue(key);
        fdmRequest.getProp("extruderId").setValue(extruderIndex);

        ResponseStructure responseStructure = new ResponseStructure();

        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("flowRateArray", new ArrayProp<>(new UInt16Prop()));
            }
        };

        responseStructure.resultProp = new UInt8Prop();
        responseStructure.dataProp = baseStructure;
        return mConnectionController.request(0xac, 0x11, fdmRequest, responseStructure);
    }

    public Observable<Float> getAutothickness(float x, float y, float z, int speed) {
        Vector vector = new Vector();
        vector.setX(x);
        vector.setY(y);
        vector.setZ(z);
        return mMachine.getMachineController().updateCoordinateSystem(0)
                .flatMap(success -> mMachine.getMachineController().gotoAbsolutePosition(vector, speed))
                .flatMap(success -> mMachine.getMachineController().updateCoordinateSystem(1))
                .flatMap(success -> mMachine.getLaserController().switchFocusAssistLight(1))
                .flatMap(success -> mMachine.getLaserController().getLaserCameraController().setExposeTime(2))
                .flatMap(success -> mMachine.getLaserController().getLaserCameraController().requestCapturePhoto())
                .flatMap(success -> mMachine.getLaserController().getLaserCameraController().watchPhotoReceive())
                .flatMap(bitmap -> {
                    float mAutothickness = -200;
                    FileOutputStream out = new FileOutputStream(ServiceContainer.getInstance().getService(IAppService.class).getCacheDir() + "/distance.png");
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    float spotX = LaserDistanceMeasureProcess.process(bitmap);
                    if (spotX < -200) {
                        return Observable.just(mAutothickness);
                    }
                    Logger.i("Detected spot x position is %s", spotX);
                    float mS1plus = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserThicknessS1Plus();
                    float mS2plus = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserThicknessS2Plus();
                    float h1 = H1_Z_POSITION + CAMERA_HEIGHT_OFFSET;
                    float h2 = H2_Z_POSITION + CAMERA_HEIGHT_OFFSET;
                    float h3 = h1 - h2;
                    mAutothickness = h1 - (h1 * ((h3 * mS1plus) + ((mS2plus * h2) - (mS1plus * h1))) / (h3 * spotX + ((mS2plus * h2) - (mS1plus * h1)))) + MockConst.LASER_MATERIAL_MEASURE_CALIBRATION_OBJECT_HEIGHT;
                    return Observable.just(mAutothickness);
                })
                .doOnNext(success -> mMachine.getLaserController().getLaserCameraController().setExposeTime(0))
                .doOnNext(success -> mMachine.getLaserController().switchFocusAssistLight(0));
    }

    public int getMachineStatus() {
        return mMachineStatusSubject.getValue();
    }

    public Observable<Integer> getMachineStatusObservable() {
        return mMachineStatusSubject.hide();
    }

    public Observable<ResponseStructure> getPrintHeadState() {
        ResponseStructure<IStructure> iStructureResponseStructure = new ResponseStructure<>();
        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("left_enable", new UInt8Prop());
                addProp("right_enable", new UInt8Prop());
            }
        };
        iStructureResponseStructure.dataProp = baseStructure;
        return mConnectionController.request(0xac, 0x19, null, iStructureResponseStructure);
    }

    public void subscribePrintCostTime() {
        mConnectionController.request(0x01, 0x00, new SubscribeStructure(0xac, 0xa5, 1000), new ResponseStructure<>()).subscribe();
    }

    public void unSubscribePrintCostTime() {
        mConnectionController.request(0x01, 0x01, new SubscribeStructure(0xac, 0xa5, 1000), new ResponseStructure<>()).subscribe();
    }

    public void watchPrintCostTimeChange() {
        Disposable subscribe = mConnectionController.watch(0xac, 0xa5, new ResponseStructure<>(new UInt32Prop()))
                .subscribe(response -> mCostTimeSubj.onNext(response.dataProp.getValue()), LogHelper::log);
    }

    public Observable<Long> getPrintCostTimeObservable() {
        return mCostTimeSubj.hide();
    }

    public boolean getPrintModeLocked() {
        return mPrintModeLocked;
    }
}
