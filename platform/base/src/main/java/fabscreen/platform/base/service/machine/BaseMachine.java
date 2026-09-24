package fabscreen.platform.base.service.machine;

import androidx.annotation.Keep;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.BuildConfig;
import fabscreen.platform.base.instantiation.IServiceContainer;
import fabscreen.platform.base.instantiation.IServiceIdentifier;
import fabscreen.platform.base.lib.fabserver.RetryWithDelay;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.controller.CNCController;
import fabscreen.platform.base.service.machine.controller.ErrorController;
import fabscreen.platform.base.service.machine.controller.FDMController;
import fabscreen.platform.base.service.machine.controller.LaserController;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.controller.NewPrintController;
import fabscreen.platform.base.service.machine.controller.PrintController;
import fabscreen.platform.base.service.machine.controller.UpdateController;
import fabscreen.platform.base.service.machine.protocol.ScreenAsServer;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.BluetoothMacStructure;
import fabscreen.platform.base.service.machine.structure.MachineFault;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.print.BatchBufferInfo;
import fabscreen.platform.base.service.machine.structure.prop.ArrayProp;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.base.service.machine.structure.update.RequestPackageParam;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.lib.SubjectHolder;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import okio.Buffer;
import okio.ByteString;

public class BaseMachine implements IMachine, IServiceIdentifier {
    protected boolean mMockMode = BuildConfig.DEBUG;

    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private final BehaviorSubject<MachineInfo> mMachineInfoSubject = BehaviorSubject.createDefault(MachineInfo.initialValue);
    protected SubjectHolder<MachineInfo> mMachineInfoSubjectHolder = new SubjectHolder<>(mMachineInfoSubject);
    private final BehaviorSubject<MachineStatus> mMachineStatusSubject = BehaviorSubject.createDefault(MachineStatus.initialValue);
    protected SubjectHolder<MachineStatus> mMachineStatusSubjectHolder = new SubjectHolder<>(mMachineStatusSubject);

    private final MachineController mMachineController;
    private final FDMController mFDMController;
    private final LaserController mLaserController;
    private final CNCController mCNCController;
    private final PrintController mPrintController;
    private final NewPrintController mNewPrintController;
    private final ErrorController mErrorController;

    protected MachineConnectionController mConnectionController;
    protected IServiceContainer mServiceContainer;
    protected IAppService mAppService;
    protected IPreferences mPreferences;
    protected IRouter mRouter;
    private final UpdateController mUpdateController;
    Disposable sub;
    private long mGcodeSequence;
    private ResponseStructure<BaseStructure> mGcodeCacheResponse;

    @Keep
    public BaseMachine(IServiceContainer sc, IAppService appService, IPreferences preferences, IRouter router) {
        mServiceContainer = sc;
        mAppService = appService;
        mPreferences = preferences;
        mRouter = router;
        mConnectionController = new MachineConnectionController(mMachineStatusSubject, preferences, mAppService);
        mFDMController = new FDMController(this, mConnectionController);
        mLaserController = new LaserController(this, mConnectionController, appService, preferences);
        mCNCController = new CNCController(this, mConnectionController);
        mPrintController = new PrintController(this, mConnectionController);
        mNewPrintController = new NewPrintController(this, mConnectionController);
        mErrorController = new ErrorController(this, mAppService, mConnectionController, mRouter, mFDMController, mLaserController, mCNCController);
        mMachineController = new MachineController(this, mConnectionController, mMachineInfoSubject, mMachineStatusSubject, appService);
        mUpdateController = new UpdateController(mConnectionController);
        listenControllerRequests();
    }

    private void listenControllerRequests() {
        mDisposable.add(mConnectionController.listen().subscribe(this::handleMachineRequest, LogHelper::log));
    }

    @Override
    public SubjectHolder<MachineInfo> getMachineInfoSubjectHolder() {
        return mMachineInfoSubjectHolder;
    }

    @Override
    public SubjectHolder<MachineStatus> getMachineStatusSubjectHolder() {
        return mMachineStatusSubjectHolder;
    }

    @Override
    public MachineController getMachineController() {
        return mMachineController;
    }

    @Override
    public FDMController getFDMController() {
        return mFDMController;
    }

    @Override
    public LaserController getLaserController() {
        return mLaserController;
    }

    @Override
    public CNCController getCNCController() {
        return mCNCController;
    }

    @Override
    public PrintController getPrintController() {
        return mPrintController;
    }

    @Override
    public NewPrintController getNewPrintController() {
        return mNewPrintController;
    }

    @Override
    public ErrorController getErrorController() {
        return mErrorController;
    }

    public UpdateController getUpdateController() {
        return mUpdateController;
    }

    @Override
    public MachineConnectionController getConnectionController() {
        return mConnectionController;
    }

    @Override
    public void setMockModeEnabled(boolean enabled) {
        mPreferences.getHelper().setMockEnabled(enabled);
        mAppService.restart();
    }

    @Override
    public boolean getMockModeEnabled() {
        return mPreferences.getHelper().getMockEnabled();
    }

    @Override
    public void setMockMachineSeriesModel(int series, int model, Set<String> debugModuleList) {
        mPreferences.getHelper().debugSetMachineSeries(series);
        mPreferences.getHelper().debugSetMachineModel(model);
        mPreferences.getHelper().setDebugModuleList(debugModuleList);
        mAppService.restart();
    }

    @Override
    public Observable<Boolean> onRestart() {
        return Observable.create(emitter -> {
            Logger.d("on machine restart... do cleaning...");
            mMachineInfoSubjectHolder.getValue().reset();
            mMachineStatusSubjectHolder.getValue().reset();
            mMachineInfoSubject.onNext(MachineInfo.initialValue);
            mMachineStatusSubject.onNext(MachineStatus.initialValue);
            mFDMController.reset();
            mLaserController.reset();
            mCNCController.reset();
            emitter.onNext(true);
            emitter.onComplete();
        })
                .delay(10, TimeUnit.SECONDS)
                .flatMap(rebooted -> mConnectionController.requestHeartbeat())
                .retryWhen(new RetryWithDelay(5, 2000))
                .flatMap(response -> Observable.just(response.isSuccess()));

    }

    private void handleMachineRequest(ScreenAsServer.ClientRequest request) throws IOException {
//        Logger.d("controller request %s, %s\ndata is %s\n sequence is %s", Integer.toHexString(request.commandSet), Integer.toHexString(request.commandId), ByteString.of(request.payload).hex(), Integer.toHexString(request.sequence));
        switch (request.commandSet * 0x100 + request.commandId) {
            case 0x0136:
                UInt8Prop homeResult = new UInt8Prop();
                homeResult.readBuffer(new Buffer().write(request.payload));
                mMachineController.onHomeResult(request.commandSet, request.commandId, request.sequence, homeResult);
                break;
            case 0xac01:
                UInt8Prop issueResult = new UInt8Prop();
                issueResult.readBuffer(new Buffer().write(request.payload));
//                mPrintController.onMachineReportPrintIssue(request.commandSet, request.commandId, request.sequence, issueResult);
                mNewPrintController.onMachineReportPrintIssue(request.commandSet, request.commandId, request.sequence, issueResult);
                break;
            case 0xac02:
                if (mGcodeSequence != request.sequence) {
                    mGcodeSequence = request.sequence;
                    mGcodeCacheResponse = null;
                    BatchBufferInfo info = new BatchBufferInfo();
                    info.readBuffer(new Buffer().write(request.payload));
//                    mGcodeCacheResponse = mPrintController.onMachineRequestBatchBufferInfo(info);
                    mGcodeCacheResponse = mNewPrintController.onMachineRequestBatchBufferInfo(info);
                    mConnectionController.sendResponse(0xac, 0x02, request.sequence, mGcodeCacheResponse);
                } else if (mGcodeCacheResponse != null) {
                    Logger.d("Send same sequence:%d", mGcodeSequence);
                    mConnectionController.sendResponse(0xac, 0x02, request.sequence, mGcodeCacheResponse);
                }
                break;
            case 0x1206:
                BluetoothMacStructure bluetooth = new BluetoothMacStructure();
                bluetooth.readBuffer(new Buffer().write(request.payload));
                mLaserController.onGetBluetoothMacAddress(bluetooth);
                mConnectionController.sendResponse(request.commandSet, request.commandId, request.sequence, new ResponseStructure<>());
                break;
            // TODO:Prepare deprecated
            case 0x013b:
                mConnectionController.sendResponse(request.commandSet, request.commandId, request.sequence, new ResponseStructure<>());
                mErrorController.onEmergencyStop(new BoolProp().readBufferToValue(new Buffer().write(request.payload)));
                break;
            // 0x0400:Report the abnormal
            case 0x0400:
                Buffer buffer = new Buffer().write(request.payload);
                MachineFault machineFault = new MachineFault().readBufferToValue(buffer);
                List<UInt8Prop> uInt8Props = new ArrayProp<>(new UInt8Prop()).readBufferToValue(buffer);
                List<Integer> machineBehaviors = new ArrayList<>();
                for (int i = 0; i < uInt8Props.size(); i++) {
                    machineBehaviors.add(uInt8Props.get(i).getValue());
                }
                mErrorController.onAbnormalTrigger(machineFault, machineBehaviors);
                mConnectionController.sendResponse(request.commandSet, request.commandId, request.sequence, new ResponseStructure<>());
                break;
            // 0x0400:Clear the exception
            case 0x0401:
                Buffer buffer1 = new Buffer().write(request.payload);
                List<MachineFault> exceptionInfos = new ArrayProp<>(new MachineFault()).readBufferToValue(buffer1);
                List<UInt8Prop> machineBehaviorStates = new ArrayProp<>(new UInt8Prop()).readBufferToValue(buffer1);
                List<Integer> machineBehaviors1 = new ArrayList<>();
                for (int i = 0; i < machineBehaviorStates.size(); i++) {
                    machineBehaviors1.add(machineBehaviorStates.get(i).getValue());
                }
                for (int i = 0; i < exceptionInfos.size(); i++) {
                    mErrorController.onAbnormalReturn(exceptionInfos.get(i), machineBehaviors1);
                }
                mConnectionController.sendResponse(request.commandSet, request.commandId, request.sequence, new ResponseStructure<>());
                break;
            case 0x0103:
                Logger.d("Screen restart request received.");
                mConnectionController.sendResponse(request.commandSet, request.commandId, request.sequence, new ResponseStructure<>());
                mAppService.restart();
                break;
            case 0xa801:
                UInt8Prop startResult = new UInt8Prop();
                startResult.readBuffer(new Buffer().write(request.payload));
                mLaserController.onGetStartLaserFocusCalibrationResult(request.commandSet, request.commandId, request.sequence, startResult.getValue());
                break;
            case 0xa805:
                UInt8Prop fineTuneResult = new UInt8Prop();
                fineTuneResult.readBuffer(new Buffer().write(request.payload));
                mLaserController.onGetStartFineTuneResult(request.commandSet, request.commandId, request.sequence, fineTuneResult.getValue());
                break;
            case 0xad02:
                // controller request update bin
                RequestPackageParam param = new RequestPackageParam();
                param.readBuffer(new Buffer().write(request.payload));
                mUpdateController.onRequestUpdatePackage(request.commandSet, request.commandId, request.sequence, param);
                break;
            case 0xad03:
                // controller notify update complete
                UInt8Prop result = new UInt8Prop();
                result.readBuffer(new Buffer().write(request.payload));
                mUpdateController.onNotifyUpdateResult(request.commandSet, request.commandId, request.sequence, result);
                break;
            case 0x0104:
                mConnectionController.sendResponse(request.commandSet, request.commandId, request.sequence, new ResponseStructure<>());
                mMachineController.setRestartMachineResult(new ResponseStructure<>());
                break;
            default:
                ResponseStructure<IStructure> iStructureResponseStructure = new ResponseStructure<>();
                iStructureResponseStructure.resultProp.setValue(4);
                mConnectionController.sendResponse(request.commandSet, request.commandId, request.sequence, iStructureResponseStructure);
                break;
        }
    }
}
