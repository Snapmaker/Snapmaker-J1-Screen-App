package fabscreen.platform.base.service.machine.controller;

import static fabscreen.platform.base.legacy.connection.MockConst.CAMERA_HEIGHT_OFFSET;
import static fabscreen.platform.base.legacy.connection.MockConst.H1_Z_POSITION;
import static fabscreen.platform.base.legacy.connection.MockConst.H2_Z_POSITION;
import static fabscreen.platform.base.service.machine.controller.PrintEventState.FINISH_FAIL;
import static fabscreen.platform.base.service.machine.controller.PrintEventState.FINISH_SUCCESS;
import static fabscreen.platform.base.service.machine.controller.PrintEventState.OPEN_DOOR_PAUSE;
import static fabscreen.platform.base.service.machine.controller.PrintEventState.PAUSE_FAIL;
import static fabscreen.platform.base.service.machine.controller.PrintEventState.PAUSE_SUCCESS;
import static fabscreen.platform.base.service.machine.controller.PrintEventState.POWER_LOSS_RESUME_FAIL;
import static fabscreen.platform.base.service.machine.controller.PrintEventState.POWER_LOSS_RESUME_SUCCESS;
import static fabscreen.platform.base.service.machine.controller.PrintEventState.RESUME_FAIL;
import static fabscreen.platform.base.service.machine.controller.PrintEventState.RESUME_SUCCESS;
import static fabscreen.platform.base.service.machine.controller.PrintEventState.START_FAIL;
import static fabscreen.platform.base.service.machine.controller.PrintEventState.START_SUCCESS;
import static fabscreen.platform.base.service.machine.controller.PrintEventState.STOP_FAIL;
import static fabscreen.platform.base.service.machine.controller.PrintEventState.STOP_SUCCESS;

import android.content.Context;
import android.graphics.Bitmap;

import com.orhanobut.logger.Logger;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import fabscreen.platform.base.data.imgprocess.LaserDistanceMeasureProcess;
import fabscreen.platform.base.instantiation.IServiceIdentifier;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.MockConst;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.print.GcodePlayer;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.lib.print.PrintListener;
import fabscreen.platform.base.lib.print.TickCounter;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.entity.module.Enclosure;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
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
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class NewPrintController implements IServiceIdentifier {
    private final int BATCHES_NUMS = 3;
    MachineConnectionController mConnectionController;
    IMachine mMachine;
    GcodePlayer mGcodePlayer = new GcodePlayer();
    TickCounter mTickCounter = new TickCounter();
    PublishSubject<PrintEvent> mPrintEventSubject = PublishSubject.create();
    PublishSubject<PrintEvent> mPrintSuccessSubject = PublishSubject.create();
    @Deprecated
    PrintListener mPrintListener;
    Disposable mExtruderWorkSpeedSubscribe;
    Disposable mPrintModeStatusSubscribe;
    PublishSubject<ResponseStructure> requestPrintStartResultSubject;
    PublishSubject<ResponseStructure> requestPrintPauseResultSubject;
    PublishSubject<ResponseStructure> requestPrintResumeResultSubject;
    PublishSubject<ResponseStructure> requestPrintStopResultSubject;
    PublishSubject<ResponseStructure> requestPrintResumeFromPowerOutageResultSubject;
    Disposable mGetMachineStatusSubscribe;
    Disposable watchPrintingLineNoSubscribe;
    private BatchCode[] mBatches;
    private int mStartBatchNo;
    private int mEndBatchNo;
    private int mNowBatchNo;
    private BatchCode mBatchCode;
    private Disposable mPrintDisposable;
    private Disposable mWatchPrintGcodeLineDisposable;
    private boolean mRecoveryFlag = false;
    private boolean mRemovePrintFlag = false;
    private BehaviorSubject<Integer> mMachineStatusSubject = BehaviorSubject.createDefault(0);
    private BehaviorSubject<Boolean> mEnclosureSubject = BehaviorSubject.createDefault(false);
    private BehaviorSubject<Integer> mExtruderWorkSpeedSubject = BehaviorSubject.createDefault(0);
    private BehaviorSubject<Float> mCurrentProgressSubject = BehaviorSubject.createDefault(0f);
    private BehaviorSubject<Integer> mPrintModeStatusSubject = BehaviorSubject.createDefault(-1);
    private BehaviorSubject<Boolean> mFilamentSubject = BehaviorSubject.createDefault(false);
    private BehaviorSubject<ArrayList<Integer>> mTookHeadSpeedSubject = BehaviorSubject.createDefault(new ArrayList<>());

    private final BehaviorSubject<Long> mCostTimeSubj = BehaviorSubject.createDefault(0L);
    private String mFileName = "";

    //模型最后的打印时间
    public long mPrintTime = 0;

    // true: don't refresh print mode anymore; false: able to refresh
    private boolean mPrintModeLocked;

    public NewPrintController(IMachine mc, MachineConnectionController cc) {
        mMachine = mc;
        mConnectionController = cc;

        mGetMachineStatusSubscribe = mc.getMachineStatusSubjectHolder()
                .getObservable()
                .subscribe(machineStatus -> {
                    if (mMachineStatusSubject.getValue() != machineStatus.status) {
                        mMachineStatusSubject.onNext(machineStatus.status);
                    }
                });

        Disposable subscribe = mConnectionController.watch(0xac, 0xa4, new ResponseStructure(new ArrayProp<>(new UInt16Prop())))
                .subscribe(requestPrintPauseResultSubject -> {
                    List<UInt16Prop> uInt16PropList = ((ArrayProp<UInt16Prop>) requestPrintPauseResultSubject.dataProp).getValue();
                    ArrayList<Integer> speeds = new ArrayList<>();
                    for (int i = 0; i < uInt16PropList.size(); i++) {
                        speeds.add(uInt16PropList.get(i).getValue());
                    }
                    mTookHeadSpeedSubject.onNext(speeds);
                });

        // Print succcess event?
        Disposable sub = mPrintSuccessSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(printEvent -> {
                    if (FINISH_SUCCESS == printEvent.getPrintEventState()) {
                        Context nowViewContext = ServiceContainer.getInstance().getService(IAppService.class).getNowViewContext();
                        if (nowViewContext != null) return;
//                        ((BaseActivity) nowViewContext).onFinishSuccess(mFileName, getTickCounter().getCount());
                    }
                });

        watchPrintCostTimeChange();
    }

    public Observable<ArrayList<Integer>> getTookHeadSpeedObservable() {
        return mTookHeadSpeedSubject.hide();
    }

    public void subscribeTookHeadSpeed() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0xac, 0xa4, 1000);
        Disposable subscribe = mConnectionController.request(0x01, 0x00, subscribeStructure, new ResponseStructure()).subscribe(responseStructure -> {
        }, LogHelper::log);
    }

    public void unSubscribeTookHeadSpeed() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0xac, 0xa4, 0);
        Disposable subscribe = mConnectionController.request(0x01, 0x01, subscribeStructure, new ResponseStructure()).subscribe(responseStructure -> {
        }, LogHelper::log);
    }

    public void onMachineReportPrintIssue(int commandSet, int commandId, int sequence, UInt8Prop printIssue) {
        int issueCode = printIssue.getValue();
        Logger.d("Print Issue %d request", issueCode);
        switch (issueCode) {
            case 0:
                Logger.d("Print finished triggered.");
                mPrintEventSubject.onNext(new PrintEvent(FINISH_SUCCESS, 0));
                mPrintSuccessSubject.onNext(new PrintEvent(FINISH_SUCCESS, 0));
                if (mPrintListener != null) {
                    Logger.i("Print Finished.");
                    mPrintListener.onFinishSuccess();
                }
                break;
            case 1:
                Logger.d("G-code paused event triggered.");
                // paused triggered by gcode
                break;
            case 2:
                Logger.d("G-code filament event triggered.");
                // filament paused triggered by gcode
                break;
            case 3:
                // filament runout event
                Logger.w("Filament runout event triggered.");
                mFilamentSubject.onNext(true);
                break;
            case 4:
                Logger.w("Extruder stuck safety triggered.");
                break;
            case 5:
                Logger.w("Print temperature abnormal triggered.");
                break;
            case 6:
                Logger.w("Print batch not match reported by controller.");
                break;
            case 7:
                Logger.w("Get G-code batch failed reported by controller.");
                break;
            case 8:
                Logger.w("Emergency Stop triggered reported by controller.");
                break;
            case 9:
                Logger.w("machine tool head recover failed reported by controller.");
                break;
            case 10:
                mPrintEventSubject.onNext(new PrintEvent(STOP_FAIL, 0));
                Logger.w("machine stop args failed reported by controller.");
                break;
            case 11:
                mPrintEventSubject.onNext(new PrintEvent(STOP_FAIL, 0));
                Logger.w("machine stop failed reported by controller.");
                break;
            case 12:
                mPrintEventSubject.onNext(new PrintEvent(STOP_SUCCESS, 0));
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
                mPrintEventSubject.onNext(new PrintEvent(OPEN_DOOR_PAUSE, 0));
                Logger.w("enclosure interrupted reported by controller.");
                break;
            case 21:
                Logger.i("Print resumed.");
                mPrintEventSubject.onNext(new PrintEvent(RESUME_SUCCESS, 0));
                if (mPrintListener != null) {
                    mPrintListener.onResumeSuccess();
                }
                mTickCounter.start();
                break;
            // Because of the abnormality, the master control stopped actively
            case 22:
                Logger.i("Print Finished.");
                mPrintEventSubject.onNext(new PrintEvent(STOP_SUCCESS, 0));
                mCurrentProgressSubject.onNext(0f);
                unWatchPrintingLineNo();
                mTickCounter.stop();
                break;
            case 255:
                Logger.w("Controller report position failure");
            default:
                Logger.w("event triggered, issueCode %d", issueCode);
                break;
        }
        ResponseStructure responseStructure = new ResponseStructure();
        responseStructure.resultProp = new UInt8Prop(0);
        mConnectionController.sendResponse(commandSet, commandId, sequence, responseStructure);
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

    public void start() {
        mPrintModeLocked = false;
        prepare();
        Logger.i("Start print.");
        IPrintWorkspace workspace = ServiceContainer.getInstance().getService(IPrintWorkspace.class);
        String md5 = workspace.getFileMD5Value();
        mFileName = workspace.getFileName();
        int workTypeIndex = mMachine.getMachineInfoSubjectHolder().getValue().workType.ordinal() - 1;
        BaseStructure gcodeFileInfo = new BaseStructure() {
            @Override
            protected void init() {
                addProp("md5", new StringProp(md5));
                addProp("filename", new StringProp(mFileName));
                addProp("type", new UInt8Prop(workTypeIndex));
            }
        };

        Disposable sub = requestPrintStart(gcodeFileInfo)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        Logger.i("Print started.");
                        // Clear the flag bit of remote start printing after the printing starts
                        mRemovePrintFlag = false;
                        mPrintEventSubject.onNext(new PrintEvent(START_SUCCESS, 0));
                        if (mPrintListener != null) {
                            mPrintListener.onStartSuccess();
                        }
                        watchPrintingLineNo();
                        mTickCounter.reset();
                        mTickCounter.start();
                    } else {
                        Logger.i("Print started Error." + responseStructure);
                        mPrintEventSubject.onNext(new PrintEvent(START_FAIL, responseStructure.resultProp.getValue()));
                        if (mPrintListener != null) {
                            mPrintListener.onStartFailed(responseStructure.resultProp.getValue());
                        }
                    }
                }, e -> {
                    LogHelper.log(e);
                    // If 0 is launched, it means that there is a problem with the screen program,
                    // which has nothing to do with the nature of the machine itself
                    mPrintEventSubject.onNext(new PrintEvent(FINISH_FAIL, 0));
                    if (mPrintListener != null) {
                        mPrintListener.onStartFailed(0);
                    }
                });
        setActionDisposable(sub);
    }

    public void pause() {
        Logger.i("Pause print.");
        Disposable sub = requestPrintPause()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resultStructure -> {
                    if (resultStructure.isSuccess()) {
                        mPrintEventSubject.onNext(new PrintEvent(PAUSE_SUCCESS, 0));
                        if (mPrintListener != null) {
                            mPrintListener.onPauseSuccess();
                        }
                        mTickCounter.stop();
                    } else {
                        Logger.i("Print paused Error.");
                        mPrintEventSubject.onNext(new PrintEvent(PAUSE_FAIL, resultStructure.resultProp.getValue()));
                        if (mPrintListener != null) {
                            mPrintListener.onPauseFailed(resultStructure.resultProp.getValue());
                        }
                    }
                }, e -> {
                    LogHelper.log(e);
                    // If 0 is launched, it means that there is a problem with the screen program,
                    // which has nothing to do with the nature of the machine itself
                    mPrintEventSubject.onNext(new PrintEvent(PAUSE_FAIL, 0));
                    if (mPrintListener != null) {
                        mPrintListener.onPauseFailed(0);
                    }
                });
        setActionDisposable(sub);
    }

    public void resume() {
        Logger.i("Resume print.");
        Disposable sub = requestPrintResume()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        Logger.i("Print resumed.");
                        mPrintEventSubject.onNext(new PrintEvent(RESUME_SUCCESS, 0));
                        if (mPrintListener != null) {
                            mPrintListener.onResumeSuccess();
                        }
                        mTickCounter.start();
                    } else {
                        Logger.i("Print resumed Error.");
                        mPrintEventSubject.onNext(new PrintEvent(RESUME_FAIL, responseStructure.resultProp.getValue()));
                        if (mPrintListener != null) {
                            mPrintListener.onResumeFailed(responseStructure.resultProp.getValue());
                        }
                    }
                }, e -> {
                    LogHelper.log(e);
                    // If 0 is launched, it means that there is a problem with the screen program,
                    // which has nothing to do with the nature of the machine itself
                    mPrintEventSubject.onNext(new PrintEvent(RESUME_FAIL, 0));
                    if (mPrintListener != null) {
                        mPrintListener.onResumeFailed(0);
                    }
                });
        setActionDisposable(sub);
    }

    public void stop() {
        Logger.i("Stop print.");
        Disposable sub = requestPrintStop()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resultStructure -> {
                    if (resultStructure.isSuccess()) {
                        Logger.i("Print Finished.");
                        mPrintEventSubject.onNext(new PrintEvent(STOP_SUCCESS, 0));
                        if (mPrintListener != null) {
                            mPrintListener.onStopSuccess();
                        }
                        mCurrentProgressSubject.onNext(0f);
                        unWatchPrintingLineNo();
                        mTickCounter.stop();
                    } else {
//                        Logger.w("Unable to finish printing, ret code %d", retCode);
                        mPrintEventSubject.onNext(new PrintEvent(STOP_FAIL, resultStructure.resultProp.getValue()));
                        if (mPrintListener != null) {
                            mPrintListener.onStopFailed(resultStructure.resultProp.getValue());
                        }
                    }
                }, e -> {
                    LogHelper.log(e);
                    // If 0 is launched, it means that there is a problem with the screen program,
                    // which has nothing to do with the nature of the machine itself
                    mPrintEventSubject.onNext(new PrintEvent(STOP_FAIL, 0));
                    if (mPrintListener != null) {
                        mPrintListener.onStopFailed(0);
                    }
                });
        setActionDisposable(sub);
    }

    public void recover() {
        Logger.i("Recover print.");
        prepare();
        IPrintWorkspace workspace = ServiceContainer.getInstance().getService(IPrintWorkspace.class);
        String md5 = workspace.getFileMD5Value();
        mFileName = workspace.getFileName();
        BaseStructure gcodeFileInfo = new BaseStructure() {
            @Override
            protected void init() {
                addProp("md5", new StringProp(md5));
                addProp("filename", new StringProp(mFileName));
            }
        };
        Disposable sub = requestPrintResumeFromPowerOutage()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        Logger.i("Print recovered.");
                        mPrintEventSubject.onNext(new PrintEvent(POWER_LOSS_RESUME_SUCCESS, 0));
                        if (mPrintListener != null) {
                            mPrintListener.onResumeFromPowerOutageSuccess();
                        }
                        watchPrintingLineNo();
                        mTickCounter.load();
                        mTickCounter.start();
                    } else {
                        mPrintEventSubject.onNext(new PrintEvent(POWER_LOSS_RESUME_FAIL, responseStructure.resultProp.getValue()));
                        if (mPrintListener != null) {
                            mPrintListener.onResumeFromPowerOutageFailed(responseStructure.resultProp.getValue());
                        }
                    }
                }, e -> {
                    LogHelper.log(e);
                    // If 0 is launched, it means that there is a problem with the screen program,
                    // which has nothing to do with the nature of the machine itself
                    mPrintEventSubject.onNext(new PrintEvent(POWER_LOSS_RESUME_FAIL, 0));
                    if (mPrintListener != null) {
                        mPrintListener.onResumeFromPowerOutageFailed(0);
                    }
                });
        setActionDisposable(sub);
    }

    public void reset() {
        Logger.d("print controller reset.");
        mCurrentProgressSubject.onNext(0f);
        mGcodePlayer.reset();
        mPrintEventSubject.onComplete();
        mPrintEventSubject = PublishSubject.create();

        mFilamentSubject.onNext(false);
        if (mPrintDisposable != null && !mPrintDisposable.isDisposed()) {
            mPrintDisposable.dispose();
            mPrintDisposable = null;
        }
    }
    /* ------------------ 内部功能实现 ----------------------- */

    private void prepare() {
        clearBatch();
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

            while (mGcodePlayer.getLineNo() < lineNo) {
                mGcodePlayer.nextLine();
            }
            // Failed to obtain Gcode, complete the task
            if (mGcodePlayer.getLine() == null) {
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
        Logger.d("sendGcode startLineNo:%d,endLineNo:%d,gcodeBatch length:%d", startLineNo, endLineNo, mBatchCode.getGcodes().length());
        return responseStructure;
    }

    private void dispose(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    private void setActionDisposable(Disposable disposable) {
        if (mPrintDisposable != null && !mPrintDisposable.isDisposed()) {
            mPrintDisposable.dispose();
        }
        mPrintDisposable = disposable;
    }

    private void clearBatch() {
        mBatches = new BatchCode[BATCHES_NUMS];
        mEndBatchNo = -1;
        mNowBatchNo = -1;
        mStartBatchNo = -1;
    }

    private int getStartBatchNo() {
        int batchNo = -1;
        if (mBatches == null || mBatches.length == 0) return batchNo;
        for (int i = 0; i < mBatches.length; i++) {
            if (mBatches[i] == null) {
                continue;
            }
            batchNo = batchNo == -1 ? mBatches[i].getStartNo() : Math.min(batchNo, mBatches[i].getStartNo());
        }
        return batchNo;
    }

    float calculatePrintProgress(long lineNo) {
        // Handle boundary condition.
        int totalCount = mGcodePlayer.getTotalCount();
        if (lineNo < 0) {
            return 0f;
        } else if (lineNo > totalCount) {
            return 1f;
        } else {
            return (1.0f * lineNo / totalCount) * 10 / 10.0f;
        }
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

    private void watchPrintingLineNo() {
        Logger.d("start watch line No...");
        if (mWatchPrintGcodeLineDisposable != null && !mWatchPrintGcodeLineDisposable.isDisposed()) {
            mWatchPrintGcodeLineDisposable.dispose();
            mWatchPrintGcodeLineDisposable = null;
        }
        if (watchPrintingLineNoSubscribe != null && !watchPrintingLineNoSubscribe.isDisposed()) {
            watchPrintingLineNoSubscribe.dispose();
            watchPrintingLineNoSubscribe = null;
        }

        SubscribeStructure subscribeStructure = new SubscribeStructure(0xac, 0xa0, 2000);
        watchPrintingLineNoSubscribe = mConnectionController.request(0x01, 0x00, subscribeStructure, new ResponseStructure())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        ResponseStructure<UInt32Prop> baseStructureResponseStructure = new ResponseStructure<>(new UInt32Prop());
                        mWatchPrintGcodeLineDisposable = mConnectionController.watch(0xac, 0xa0, baseStructureResponseStructure)
                                .subscribe(responseStructure1 -> {
                                    if (!responseStructure1.isSuccess()) {
                                        Logger.d("watch print response error, error code %d", responseStructure.resultProp.getValue());
                                    } else {
                                        long lineNo = responseStructure1.dataProp.getValue();
                                        Logger.d("Print %s gcode line now...", String.valueOf(lineNo));
                                        mCurrentProgressSubject.onNext(calculatePrintProgress(lineNo));
                                    }
                                }, LogHelper::log);
                    } else {
                        Logger.e("Subscribe print G-code line event failed, " + responseStructure);
                    }
                }, LogHelper::log);
    }

    private void unWatchPrintingLineNo() {
        if (mWatchPrintGcodeLineDisposable != null && !mWatchPrintGcodeLineDisposable.isDisposed()) {
            mWatchPrintGcodeLineDisposable.dispose();
            mWatchPrintGcodeLineDisposable = null;
        }
        if (watchPrintingLineNoSubscribe != null && !watchPrintingLineNoSubscribe.isDisposed()) {
            watchPrintingLineNoSubscribe.dispose();
            watchPrintingLineNoSubscribe = null;
        }
        mConnectionController.request(0x01, 0x01, new SubscribeStructure(0xac, 0xa0, 0), new ResponseStructure());
        mConnectionController.unWatch(0xac, 0xa0);
    }

    /* ------------------ 提供外部数据接口 ----------------------- */
    public Observable<Integer> getExtruderWorkSpeedObservable() {
        return mExtruderWorkSpeedSubject.hide();
    }

    public Observable<Integer> getPrintModeStatusObservable() {
        return mPrintModeStatusSubject.hide();
    }

    public Integer getPrintModeStatusValue() {
        return mPrintModeStatusSubject.getValue();
    }

    public boolean getRemovePrintFlag() {
        return mRemovePrintFlag;
    }

    public void setRemovePrintFlag(boolean removePrintFlag) {
        mRemovePrintFlag = removePrintFlag;
    }

    public int getPrintState() {
        return mMachineStatusSubject.getValue();
    }

    public Observable<Integer> getPrintStateObservable() {
        return mMachineStatusSubject.hide();
    }

    public Observable<Boolean> getEnclosureSubjectObservable() {
        return mEnclosureSubject.hide();
    }

    public void setEnclosure(Boolean confirm) {
        mEnclosureSubject.onNext(!confirm);
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

    public boolean getRecoveryFlag() {
        return mRecoveryFlag;
    }

    public void setPowerOutageFlag(boolean flag) {
        mRecoveryFlag = flag;
    }

    @Deprecated
    public void setListener(PrintListener listener) {
        mPrintListener = listener;
    }

    public Observable<PrintEvent> getPrintEventObservable() {
        return mPrintEventSubject.hide();
    }

    public TickCounter getTickCounter() {
        return mTickCounter;
    }

    public float getProgress() {
        return mCurrentProgressSubject.getValue();
    }

    public Observable<Boolean> getFilamentSubjectObservable() {
        return mFilamentSubject.hide();
    }

    public void setFilament(Boolean confirm) {
        mFilamentSubject.onNext(!confirm);
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

    /* ------------------ 流程区 ----------------------- */

    /**
     * 激光自动测厚流程
     * 返回 -200 为识别错误
     *
     * @return
     */
    public Observable<Float> getAutoThickness(float x, float y, float z, int speed) {
        Vector vector = new Vector();
        vector.setX(x);
        vector.setY(y);
        vector.setZ(z);
        if (mMachine.getMachineInfoSubjectHolder().getValue().isEnclosureAvailable) {
            Enclosure enclosure = mMachine.getMachineController().getEnclosure();
            return enclosure.setEnclosureLedLevel(0).flatMap(responseStructure -> getAutoThickness(vector, speed, enclosure.getEnclosureStatusValue().isLedOn() ? 1 : 0));
        } else {
            return getAutoThickness(vector, speed, -1);
        }
    }

    private Observable<Float> getAutoThickness(Vector vector, int speed, int enclosureAvailable) {
        return mMachine.getMachineController().updateCoordinateSystem(0)
                .flatMap(success -> mMachine.getMachineController().gotoAbsolutePosition(vector, speed))
                .flatMap(success -> mMachine.getMachineController().updateCoordinateSystem(1))
                .flatMap(success -> mMachine.getLaserController().switchFocusAssistLight(1))
                .flatMap(success -> mMachine.getLaserController().getLaserCameraController().setExposeTime(2))
                .flatMap(success -> mMachine.getLaserController().getLaserCameraController().requestCapturePhoto())
                .flatMap(success -> mMachine.getLaserController().getLaserCameraController().watchPhotoReceive())
                .flatMap(bitmap -> {
                    float autoThickness = -200;
                    FileOutputStream out = new FileOutputStream(ServiceContainer.getInstance().getService(IAppService.class).getCacheDir() + "/distance.png");
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    float spotX = LaserDistanceMeasureProcess.process(bitmap);
                    if (spotX < -200) {
                        return Observable.just(autoThickness);
                    }
                    Logger.i("Detected spot x position is %s", spotX);
                    float mS1plus = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserThicknessS1Plus();
                    float mS2plus = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserThicknessS2Plus();
                    float h1 = H1_Z_POSITION + CAMERA_HEIGHT_OFFSET;
                    float h2 = H2_Z_POSITION + CAMERA_HEIGHT_OFFSET;
                    float h3 = h1 - h2;
                    autoThickness = h1 - (h1 * ((h3 * mS1plus) + ((mS2plus * h2) - (mS1plus * h1))) / (h3 * spotX + ((mS2plus * h2) - (mS1plus * h1)))) + MockConst.LASER_MATERIAL_MEASURE_CALIBRATION_OBJECT_HEIGHT;
                    return Observable.just(autoThickness);
                })
                .flatMap(success -> {
                    if (enclosureAvailable == 1) {
                        return mMachine.getMachineController().getEnclosure().setEnclosureLedLevel(100).flatMap(aBoolean -> Observable.just(success));
                    } else {
                        return Observable.just(success);
                    }
                })
                .flatMap(success -> mMachine.getLaserController().getLaserCameraController().setExposeTime(0).flatMap(aBoolean -> Observable.just(success)))
                .flatMap(success -> mMachine.getLaserController().switchFocusAssistLight(0).flatMap(aBoolean -> Observable.just(success)));
    }

    /* ------------------ 接口区 ----------------------- */
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

//    Observable<ResponseStructure> requestPrintStart(BaseStructure gcodeFileInfo) {
//        if (requestPrintStartResultSubject != null) {
//            requestPrintStartResultSubject.onComplete();
//        }
//        return mConnectionController.request(0xac, 0x03, gcodeFileInfo, new ResponseStructure())
//                .flatMap(responseStructure -> {
//                            if (responseStructure.isSuccess()) {
//                                requestPrintStartResultSubject = PublishSubject.create();
//                                return requestPrintStartResultSubject.hide();
//                            } else {
//                                return Observable.just(responseStructure);
//                            }
//                        }
//                );
//    }
//
//    public void onRequestPrintStartResult(int commandSet, int commandId, int sequence, ResponseStructure value) {
//        if (requestPrintStartResultSubject == null) return;
//        mConnectionController.sendResponse(commandSet, commandId, sequence, new UInt8Prop(0));
//        requestPrintStartResultSubject.onNext(value);
//    }
//
//    Observable<ResponseStructure> requestPrintPause() {
//        if (requestPrintPauseResultSubject != null) {
//            requestPrintPauseResultSubject.onComplete();
//        }
//        return mConnectionController.request(0xac, 0x04, null, new ResponseStructure())
//                .flatMap(responseStructure -> {
//                            if (responseStructure.isSuccess()) {
//                                requestPrintPauseResultSubject = PublishSubject.create();
//                                return requestPrintPauseResultSubject.hide();
//                            } else {
//                                return Observable.just(responseStructure);
//                            }
//                        }
//                );
//    }
//
//    public void onRequestPrintPauseResult(int commandSet, int commandId, int sequence, ResponseStructure value) {
//        if (requestPrintPauseResultSubject == null) return;
//        mConnectionController.sendResponse(commandSet, commandId, sequence, new UInt8Prop(0));
//        requestPrintPauseResultSubject.onNext(value);
//    }
//
//    Observable<ResponseStructure> requestPrintResume() {
//        if (requestPrintResumeResultSubject != null) {
//            requestPrintResumeResultSubject.onComplete();
//        }
//        return mConnectionController.request(0xac, 0x05, null, new ResponseStructure())
//                .flatMap(responseStructure -> {
//                            if (responseStructure.isSuccess()) {
//                                requestPrintResumeResultSubject = PublishSubject.create();
//                                return requestPrintResumeResultSubject.hide();
//                            } else {
//                                return Observable.just(responseStructure);
//                            }
//                        }
//                );
//    }
//
//    public void onRequestPrintResumeResult(int commandSet, int commandId, int sequence, ResponseStructure value) {
//        if (requestPrintResumeResultSubject == null) return;
//        mConnectionController.sendResponse(commandSet, commandId, sequence, new UInt8Prop(0));
//        requestPrintResumeResultSubject.onNext(value);
//    }
//
//    Observable<ResponseStructure> requestPrintStop() {
//        if (requestPrintStopResultSubject != null) {
//            requestPrintStopResultSubject.onComplete();
//        }
//        return mConnectionController.request(0xac, 0x06, null, new ResponseStructure())
//                .flatMap(responseStructure -> {
//                            if (responseStructure.isSuccess()) {
//                                requestPrintStopResultSubject = PublishSubject.create();
//                                return requestPrintStopResultSubject.hide();
//                            } else {
//                                return Observable.just(responseStructure);
//                            }
//                        }
//                );
//    }
//
//    public void onRequestPrintStopResult(int commandSet, int commandId, int sequence, ResponseStructure value) {
//        if (requestPrintStopResultSubject == null) return;
//        mConnectionController.sendResponse(commandSet, commandId, sequence, new UInt8Prop(0));
//        requestPrintStopResultSubject.onNext(value);
//    }
//
//    public Observable<ResponseStructure> requestPowerOutageStatus() {
//        ResponseStructure responseStructure = new ResponseStructure();
//        BaseStructure gcodeFileInfo = new BaseStructure() {
//            @Override
//            protected void init() {
//                addProp("md5", new StringProp(""));
//                addProp("filename", new StringProp(""));
//            }
//        };
//        responseStructure.resultProp = new UInt8Prop();
//        responseStructure.dataProp = gcodeFileInfo;
//        return mConnectionController.request(0xac, 0x07, null, responseStructure);
//    }
//
//    Observable<ResponseStructure> requestPrintResumeFromPowerOutage(BaseStructure gcodeFileInfo) {
//        if (requestPrintResumeFromPowerOutageResultSubject != null) {
//            requestPrintResumeFromPowerOutageResultSubject.onComplete();
//        }
//        return mConnectionController.request(0xac, 0x08, gcodeFileInfo, new ResponseStructure())
//                .flatMap(responseStructure -> {
//                            if (responseStructure.isSuccess()) {
//                                requestPrintResumeFromPowerOutageResultSubject = PublishSubject.create();
//                                return requestPrintResumeFromPowerOutageResultSubject.hide();
//                            } else {
//                                return Observable.just(responseStructure);
//                            }
//                        }
//                );
//    }
//
//    public void onRequestPrintResumeFromPowerOutageResult(int commandSet, int commandId, int sequence, ResponseStructure value) {
//        if (requestPrintResumeFromPowerOutageResultSubject == null) return;
//        mConnectionController.sendResponse(commandSet, commandId, sequence, new UInt8Prop(0));
//        requestPrintResumeFromPowerOutageResultSubject.onNext(value);
//    }

    public Observable<ResponseStructure> requestPrintPowerLossClearMarker() {
        return mConnectionController.request(0xac, 0x09, null, new ResponseStructure());
    }

    public Observable<ResponseStructure> requestChangePrintMode(int printMode) {
        BaseStructure structure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("requestPrintMode", new UInt8Prop(printMode));
            }
        };
        return mConnectionController.request(0xac, 0x0a, structure, new ResponseStructure<>());
    }

    public Observable<ResponseStructure> requestPrintModeStatus() {
        ResponseStructure<BaseStructure> responseStructure = new ResponseStructure<>();

        responseStructure.dataProp = new BaseStructure() {
            @Override
            protected void init() {
                addProp("printMode", new UInt8Prop());
            }
        };
        return mConnectionController.request(0xac, 0x0b, null, responseStructure);
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
        return mConnectionController.request(0xac, 0x0c, structure, new ResponseStructure());
    }

//    //FIXME:  Set the working speed adjustment ratio
//    public Observable<ResponseStructure> setExtruderWorkSpeed(int id, int extruderIndex, int speed) {
//        FDMController fdmController = ServiceContainer.getInstance().getService(IMachine.class).getFDMController();
//        int key = fdmController.getModuleIdFromIndex(id);
//        BaseStructure fdmRequest = new BaseStructure() {
//            @Override
//            protected void init() {
//                addProp("key", new UInt8Prop());
//                addProp("extruderId", new UInt8Prop());
//                addProp("speed", new Int16Prop());
//            }
//        };
//        fdmRequest.getProp("key").setValue(key);
//        fdmRequest.getProp("extruderId").setValue(extruderIndex);
//        fdmRequest.getProp("speed").setValue(speed);
//        return mConnectionController.request(0xac, 0x0e, fdmRequest, new ResponseStructure());
//    }

    // Clone from artisan
    public Observable<ResponseStructure> setExtruderWorkSpeed(IMachine.WorkType workType, int id, int extruderIndex, int speed) {
        int key = -1;
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
        }
        BaseStructure fdmRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("extruderId", new UInt8Prop(extruderIndex));
                addProp("speed", new Int16Prop(speed));
            }
        };
        fdmRequest.getProp("key").setValue(key);
        return mConnectionController.request(0xac, 0x0e, fdmRequest, new ResponseStructure());
    }

    // Original method from PrintController
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

    /**
     * 获取 FDM 执行头流量
     * 后续需修改为订阅接口
     *
     * @param id
     * @param extruderIndex
     *
     * @return
     */
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

    // J1 需要单独订阅下 风扇更新数据
    public Observable<ResponseStructure> subscribeExtruderWorkSpeedObservable() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0xac, 0xa2, 1000);
        return mConnectionController.request(0x01, 0x00, subscribeStructure, new ResponseStructure())
                .doOnNext(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        if (mExtruderWorkSpeedSubscribe != null && !mExtruderWorkSpeedSubscribe.isDisposed()) {
                            mExtruderWorkSpeedSubscribe.dispose();
                        }
                        mExtruderWorkSpeedSubscribe = mConnectionController.watch(0xac, 0xa2, new ResponseStructure<>(new UInt8Prop()))
                                .subscribe(responseStructure1 -> {
                                    mExtruderWorkSpeedSubject.onNext(responseStructure1.dataProp.getValue());
                                }, LogHelper::log);
                    }
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

    public void updatePrintState() {
        mMachineStatusSubject.onNext(mMachineStatusSubject.getValue());
    }

    // from original j1 print controller

    public Observable<Long> getPrintCostTimeObservable() {
        return mCostTimeSubj.hide();
    }

    public void setPrintTime(long time) {
        mPrintTime = time;
    }

    public long getPrintTime() {
        return mPrintTime;
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

    public boolean getPrintModeLocked() {
        return mPrintModeLocked;
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

    public int getMachineStatus() {
        return mMachineStatusSubject.getValue();
    }

    public Observable<Integer> getMachineStatusObservable() {
        return mMachineStatusSubject.hide();
    }

    public Observable<ResponseStructure> getPrintPerformanceOption() {
        ResponseStructure<IStructure> responseStructure = new ResponseStructure<>();
        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("performance_option", new UInt8Prop());
            }
        };
        responseStructure.dataProp = baseStructure;
        return mConnectionController.request(0xac, 0x1d, null, responseStructure);
    }

    public Observable<ResponseStructure> requestPrintPerformanceOption(int option) {
        return mConnectionController.request(0xac, 0x1c, new UInt8Prop(option), new ResponseStructure<>());
    }
}
