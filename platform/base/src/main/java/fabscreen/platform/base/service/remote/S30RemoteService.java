package fabscreen.platform.base.service.remote;

import android.util.ArraySet;

import androidx.annotation.Keep;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.R;
import fabscreen.platform.base.instantiation.IServiceIdentifier;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.server.http.HTTPServer;
import fabscreen.platform.base.legacy.server.socket.DiscoverServer;
import fabscreen.platform.base.lib.file.FabLocalFile;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.parser.IGcodeParser;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.server.TCPServer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRemote;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.controller.NewPrintController;
import fabscreen.platform.base.service.machine.protocol.IProtocol;
import fabscreen.platform.base.service.machine.protocol.ScreenAsServer;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.RemoteFileStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.StringProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt16Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.base.service.remote.proxy.IScreenProxy;
import fabscreen.platform.base.service.remote.proxy.ScreenProxy;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.base.view.SuperToastHelper;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import okio.Buffer;

public class S30RemoteService implements IRemote, IServiceIdentifier {
    private final IMachine mMachine;
    private final IAppService mAppService;
    private IScreenProxy mProxy;
    private final IPreferences mPreferences;

    private TCPServer mScreenServer;
    private RemoteClient mAuthenticationClient = null;
    private RemoteClient mTempRemoteClient;
    private Set<String> remoteTokens;

    private RemoteConnectionController mRemoteConnectionController;
    private RemoteFileController mRemoteFileController;
    private RemoteLaserController mRemoteLaserController;

    private HTTPServer mHttpServer;

    Disposable subscribe3;
    private final CompositeDisposable mListeningAuthenticationDisposable = new CompositeDisposable();
    private final BehaviorSubject<Integer> mRemoteConnectionState = BehaviorSubject.createDefault(0);
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    private DecisionDialog mRemoveDecisionDialog;
    private DecisionDialog mDecisionDialog;

    @Keep
    public S30RemoteService(IMachine machine, IAppService appService, IPreferences preferences) {
        mMachine = machine;
        mAppService = appService;
        mPreferences = preferences;

        // Default UDP discover server. PORT 20054
        startDiscoverServer();

        // TCP Server for remote connection. SACP Protocol base, PORT 8888
        startScreenServer();

        startHttpServer();

        // Observe connection disconnected
        subscribe3 = mRemoteConnectionState
                .skip(1)
                .filter(integer -> integer == 0)
                .subscribe(integer -> clearConnectionConfiguration(), LogHelper::log);

        // Implemented by fdt.
        // Once the client has socket connection and screen has not confirmed yet,
        // throw random data to the socket pine to check if socket was closed.
        Disposable disposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .filter(integer -> mRemoveDecisionDialog != null && mRemoveDecisionDialog.isShowing())
                .filter(integer -> mTempRemoteClient != null)
                .subscribe(i -> {
                            try {
                                mTempRemoteClient.getSocket().getOutputStream().write('a');
                            } catch (Exception ignored) {
                                try {
                                    Logger.d("Remote connection lost, disconnecting…");
                                    mTempRemoteClient.disconnect();
                                    mTempRemoteClient = null;
                                } catch (IOException e) {
                                    LogHelper.log(e);
                                }
                            }
                        }
                        , LogHelper::log);
    }

    private void startScreenServer() {
        mScreenServer = new TCPServer(8888);
        Disposable subscribe = mScreenServer.getSocketSubjectObservable().subscribe(socket -> {
            // New socket connected and send authentication request
            if (mRemoteConnectionState.getValue() != 0) {
                Logger.d("Current connection status %d, refusing new request.", mRemoteConnectionState.getValue());
                return;
            }

            // New remoteClient registered, start checking connect hand shake by SACP protocol.
            // `remoteClient` will temporary keep this socket until the whole authentication procedure completes.
            RemoteClient remoteClient = new RemoteClient(socket, mPreferences);
            mListeningAuthenticationDisposable.add(
                    // Listen authentication
                    remoteClient.listeningAuthentication()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(clientRequest -> {
                                // TODO: Needs to refactor this method,
                                //  split out view business and clean up.
                                // A new client requesting connection.
                                ResponseStructure<IStructure> responseStructure = new ResponseStructure<>();
                                responseStructure.dataProp = new StringProp();
                                mRemoteConnectionState.onNext(1);
                                try {
                                    Buffer write = new Buffer().write(clientRequest.payload);
                                    // Get arguments from request
                                    remoteClient.setDeviceName(new StringProp().readBufferToValue(write));
                                    remoteClient.setConnectingClients(new StringProp().readBufferToValue(write));
                                    remoteClient.setToken(new StringProp().readBufferToValue(write));

                                    Logger.d("DeviceName：%s" +
                                                    "\tConnectingClients:%s" +
                                                    "\tToken:%s",
                                            remoteClient.getDeviceName(),
                                            remoteClient.getConnectingClients(),
                                            remoteClient.getToken());
                                } catch (Exception e) {
                                    LogHelper.log(e);
                                    // Return status 6??
                                    responseStructure.resultProp.setValue(6);
                                    remoteClient.clientProtocol.sendResponse(clientRequest.commandSet,
                                            clientRequest.commandId,
                                            IProtocol.CommunicationId.LUBAN,
                                            clientRequest.sequence,
                                            responseStructure);
                                    mRemoteConnectionState.onNext(0);
                                }

                                // Check if allow remote connection.
                                if (!mPreferences.getHelper().getRemoteAllowConnection()) {
                                    Logger.d("Remote connection not allowed, closing.");
                                    responseStructure.resultProp.setValue(201);
                                    remoteClient.clientProtocol.sendResponse(clientRequest.commandSet,
                                            clientRequest.commandId,
                                            IProtocol.CommunicationId.LUBAN,
                                            clientRequest.sequence, responseStructure);
                                    mRemoteConnectionState.onNext(0);
                                    return;
                                }

                                // If request token is null, then generate random UUID for token.
                                if (remoteClient.getToken() == null) {
                                    UUID uuid = UUID.randomUUID();
                                    remoteClient.setToken(uuid.toString());
                                }

                                // Get token from preference.
                                remoteTokens = mPreferences.getHelper().getRemoteTokens();
                                // If remote token equals current client, and verification set as once for ever.
                                if (remoteTokens != null
                                        && remoteTokens.contains(remoteClient.getToken())
                                        && mPreferences.getHelper().getConnectionVerification() == 0) {
                                    // Authentication complete, send response to the client.
                                    mRemoteConnectionState.onNext(2);
                                    mTempRemoteClient = remoteClient;
                                    onAuthenticationSuccess(remoteClient);

                                    mRemoteConnectionController.sendResponse(clientRequest.commandSet,
                                            clientRequest.commandId,
                                            clientRequest.sequence,
                                            responseStructure);
                                } else {
                                    // Asking for verification.
                                    mTempRemoteClient = remoteClient;
                                    DecisionDialog removeDecisionDialog = DecisionDialog.create(mAppService.getNowViewContext())
                                            .setPic(R.drawable.pic_setting_remote_connection_verification)
                                            .setWarmTv(remoteClient.getConnectingClients(), R.color.palette_grey_dim)
                                            .setTitle(R.string.all_connection_verification)
                                            .setContent(R.string.all_connection_verification_content)
                                            .setDialogStatus(DecisionDialog.BTN_TWO, true, true, true, false)
                                            .setFirstTv(R.string.all_refuse, R.color.select_dialog_white_txt, (dialog, i) -> {
                                                dialog.dismiss();
                                                mRemoteConnectionState.onNext(0);
                                                if (mTempRemoteClient == null) {
                                                    new SuperToastHelper.Builder()
                                                            .setDrawable(R.drawable.icon_tips_error_80x80)
                                                            .setMessage(mAppService.getNowViewContext().getResources().getString(R.string.j1_toast_remote_connection_unavailable))
                                                            .build()
                                                            .showToast(mAppService.getNowViewContext());
                                                } else {
                                                    // Refuse connection.
                                                    responseStructure.resultProp.setValue(200);
                                                    remoteClient.clientProtocol.sendResponse(clientRequest.commandSet,
                                                            clientRequest.commandId,
                                                            IProtocol.CommunicationId.LUBAN,
                                                            clientRequest.sequence, responseStructure);
                                                    try {
                                                        remoteClient.disconnect();
                                                    } catch (IOException e) {
                                                        LogHelper.log(e);
                                                    }
                                                }
                                            })
                                            .setSecondTv(R.string.all_connect, R.color.select_dialog_blue_txt, ((dialog, which) -> {
                                                dialog.dismiss();
                                                // FIXME: Temporary workaround.
                                                //  Using mTempRemoteClient as connection, which was unAuthenticated but still connecting.
                                                //  We need to manage connection which authentication procedure was not completed.
                                                if (mTempRemoteClient == null) {
                                                    mRemoteConnectionState.onNext(0);
                                                    new SuperToastHelper.Builder()
                                                            .setDrawable(R.drawable.icon_tips_error_80x80)
                                                            .setMessage(mAppService.getNowViewContext().getResources().getString(R.string.j1_toast_remote_connection_unavailable))
                                                            .build()
                                                            .showToast(mAppService.getNowViewContext());
                                                } else {
                                                    // Verification confirmed.
                                                    mRemoteConnectionState.onNext(2);
                                                    try {
                                                        onAuthenticationSuccess(remoteClient);
                                                    } catch (IOException e) {
                                                        LogHelper.log(e);
                                                        mRemoteConnectionState.onNext(0);
                                                        responseStructure.resultProp.setValue(200);
                                                    }
                                                    mRemoteConnectionController.sendResponse(clientRequest.commandSet,
                                                            clientRequest.commandId,
                                                            clientRequest.sequence,
                                                            responseStructure);
                                                }
                                            }));
                                    removeDecisionDialog.show();
                                    if (removeDecisionDialog.isShowing()) {
                                        if (mRemoveDecisionDialog != null && mRemoveDecisionDialog.isShowing()) {
                                            mRemoveDecisionDialog.dismiss();
                                        }
                                        mRemoveDecisionDialog = removeDecisionDialog;
                                    }
                                }
                            }, LogHelper::log));
        }, LogHelper::log);
    }

    @Override
    public Observable<Integer> getRemoteConnectedObservable() {
        return mRemoteConnectionState;
    }

    @Override
    public RemoteFileController getRemoteFilController() {
        return mRemoteFileController;
    }

    private void listenToRequests() {
        mDisposable.add(mRemoteConnectionController.listen().subscribe(this::handleMachineRequest, LogHelper::log));
    }

    private void onAuthenticationSuccess(RemoteClient remoteClient) throws IOException {
        Logger.d("Authentication successful.");
        new SuperToastHelper.Builder()
                .setDrawable(R.drawable.ic_pic_a400_success_68x68)
                .setMessage(mAppService.getNowViewContext().getString(R.string.j1_toast_connect_success, remoteClient.mDeviceName, remoteClient.mConnectingClients))
                .build()
                .showToast(mAppService.getNowViewContext());
        // Set verification token into preference.
        if (mPreferences.getHelper().getConnectionVerification() == 0) {
            if (remoteTokens == null) {
                remoteTokens = new ArraySet<>();
            }
            remoteTokens.add(remoteClient.getToken());
            mPreferences.getHelper().setRemoteTokens(remoteTokens);
        }

        mAuthenticationClient = remoteClient;
        mAuthenticationClient.setIsAuthentication(true);
        mAuthenticationClient.setRemoteConnectionState(mRemoteConnectionState);

        // This method will throw IOException.
        mAuthenticationClient.updateProtocol(mMachine.getConnectionController().getProxyDataSubject());
        // TODO: Reconsider RemoteConnectionController. What controller responsible for?
        mRemoteConnectionController = new RemoteConnectionController(mAuthenticationClient);
        if (mProxy != null) {
            mProxy.destroy();
        }
        // New proxy started.
        mProxy = new ScreenProxy(mMachine.getConnectionController(), mRemoteConnectionController, mRemoteConnectionState);
        mRemoteFileController = new RemoteFileController(mMachine, mRemoteConnectionController, mAppService, mPreferences);
        // FIXME: J1 don't need laser controller.
//        mRemoteLaserController = new RemoteLaserController(mMachine, mRemoteConnectionController, mRemoteFileController, mAppService, mPreferences);

        // Handle Remote Client response here.
        listenToRequests();
        mListeningAuthenticationDisposable.clear();

        if (mPreferences.getHelper().getRemoteSafeMode()) {
            // TODO: delay showing connection dialog after ToastActivity vanished.
            String device = remoteClient.getDeviceName().isEmpty() ? "Unknown Device" : remoteClient.getDeviceName();
            String client = remoteClient.getDeviceName().isEmpty() ? "Unknown Client" : remoteClient.getConnectingClients();
            mDecisionDialog = DecisionDialog.create(mAppService.getNowViewContext())
                    .setTitle(R.string.j1_remote_state_title)
                    .setContent(String.format("%s (%s)", device, client))
                    .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, true)
                    .setFirstTv(R.string.all_disconnect, R.color.select_dialog_red_txt, ((dialog, which) -> {
                        ServiceContainer.getInstance().getService(IRemote.class).ClearConnection();
                        dialog.dismiss();
                    }));
            mDecisionDialog.show();
        }
    }

    private void handleMachineRequest(ScreenAsServer.ClientRequest request) throws IOException {
//        Logger.d("Remote controller request %s, %s\ndata is %s\n sequence is %s", Integer.toHexString(request.commandSet), Integer.toHexString(request.commandId), ByteString.of(request.payload).hex(), Integer.toHexString(request.sequence));
        switch (request.commandSet * 0x100 + request.commandId) {
            case 0x0101:
                mRemoteConnectionController.sendResponse(0x01, 0x01, 0x01, new UInt8Prop(0));
                break;
            case 0xb000:
                RemoteFileStructure fileStructure = new RemoteFileStructure();
                fileStructure.readBuffer(new Buffer().write(request.payload));
//                Logger.d("file-transfer: %s", fileStructure.toString());
                mRemoteConnectionController.sendResponse(request.commandSet, request.commandId, request.sequence, new ResponseStructure<>());
                mRemoteFileController.requestStartSendFile(fileStructure);
                break;
            case 0xb001:
                BaseStructure structure = new BaseStructure() {
                    @Override
                    protected void init() {
                        addProp("md5", new StringProp());
                        addProp("index", new UInt16Prop());
                    }
                };
                structure.readBuffer(new Buffer().write(request.payload));
                mRemoteFileController.requestPackage(request.sequence, (Integer) structure.getProp("index").getValue());
                break;
            case 0xb002:
                BaseStructure finishStruct = new BaseStructure() {
                    @Override
                    protected void init() {
                        addProp("uploaded", new BoolProp());
                        addProp("name", new StringProp());
                        addProp("md5", new StringProp());
                    }
                };
                finishStruct.readBuffer(new Buffer().write(request.payload));
                mRemoteFileController.onSendFileFinish(request.commandSet, request.commandId, request.sequence, finishStruct);
                break;
            case 0xb003:
                // Remote request get camera calibration data
                mRemoteLaserController.requestGet10WCameraCalibrationData(request.commandSet, request.commandId, request.sequence, null);
                break;
            case 0xb004:
                // Remote request capture photo by move
                mRemoteLaserController.requestCapturePhotoByMove(request.commandSet, request.commandId, request.sequence, request.payload);
                break;
            case 0xb005:
                // Remote request get photo by index
                mRemoteLaserController.requestGetPhotoByIndex(request.commandSet, request.commandId, request.sequence, request.payload);
                break;
            case 0xb006:
                // Remote request get calibration data photo.
                mRemoteLaserController.requestCameraCalibrationPhoto(request.commandSet, request.commandId, request.sequence, request.payload);
                break;
            case 0xb007:
                // Remote request set camera calibration data
                mRemoteLaserController.requestSet10WCameraCalibrationData(request.commandSet, request.commandId, request.sequence, request.payload);
                break;
            case 0xb008:
                startPrint(request.commandSet, request.commandId, request.sequence, request.payload);
                break;
            case 0xb009:
                // laser - auto measure thickness
                mRemoteLaserController.requestAutoMeasureMaterialThickness(request.commandSet, request.commandId, request.sequence, request.payload);
                break;
            case 0xb090:
                mRemoteLaserController.requestSetTestCameraCalibrationTakePhotoVector(request.commandSet, request.commandId, request.sequence, request.payload);
                break;
            case 0xb091:
                mRemoteLaserController.requestGetTestCameraCalibrationTakePhotoVector(request.commandSet, request.commandId, request.sequence, request.payload);
                break;
            case 0x0106:
                mRemoteConnectionController.sendResponse(request.commandSet, request.commandId, request.sequence, new ResponseStructure<>());
                ClearConnection();
                break;
            default:
                break;
        }
    }

    // start print for test
    private void startPrint(int commandSet, int commandId, int sequence, byte[] requestPayload) throws IOException {
        int result = 0;
        if (ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().status != 0) {
            result = 13;
            Logger.d("debug RemoteLaserController payload is null");
            // Return result.
            ResponseStructure responseStructure = new ResponseStructure();
            responseStructure.resultProp = new UInt8Prop(result);
            mRemoteConnectionController.sendResponse(commandSet, commandId, sequence, responseStructure);
            return;
        }
        if (requestPayload == null) {
            result = 6;
            Logger.d("debug RemoteLaserController payload is null");
            // Return result.
            ResponseStructure responseStructure = new ResponseStructure();
            responseStructure.resultProp = new UInt8Prop(result);
            mRemoteConnectionController.sendResponse(commandSet, commandId, sequence, responseStructure);
        } else {
            Buffer buffer = new Buffer().write(requestPayload);
            int headType = new UInt8Prop().readBufferToValue(buffer);
            String filename = new StringProp().readBufferToValue(buffer);
            String md5 = new StringProp().readBufferToValue(buffer);

//            Logger.d("---FDT--- startPrint:\nheadType:%d,filename:%s,md5:%s", headType, filename, md5);
            File file = new File(mAppService.getFilesDir(), filename);

            // TODO: Verify that the header type and are machine-consistent?
            if (!file.exists() || filename.isEmpty()) {
                ResponseStructure responseStructure = new ResponseStructure();
                responseStructure.resultProp = new UInt8Prop(200);
                mRemoteConnectionController.sendResponse(commandSet, commandId, sequence, responseStructure);
            } else {
                IGcodeParser parser = ServiceContainer.getInstance().getService(IGcodeParser.class);
                parser.startParse(mAppService.getFilesDir() + "/" + filename, true, ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType);
                Disposable parserSub = parser.getParseProgressObservable()
                        .throttleLast(100, TimeUnit.MILLISECONDS)
                        .distinctUntilChanged()
                        .takeUntil(progress -> progress == 100)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(progress -> {
                            IPrintWorkspace workspace = ServiceContainer.getInstance().getService(IPrintWorkspace.class);
                            workspace.setPrintMode(parser.getPrintMode());
                            workspace.setPrintSource(1);
                            workspace.setFileTotalLineCount(parser.getTotalLinesCount());
                            workspace.setEstimatedTime(parser.getEstimatedTime());
                            workspace.setFileMD5Value(md5);
                            IFile file2 = new FabLocalFile(file);
                            NewPrintController printController = mMachine.getNewPrintController();
                            printController.setRemovePrintFlag(true);
                            Disposable sub = workspace.addFileToWorkspace(file2)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(success -> {
                                        if (success) {
                                            ServiceContainer.getInstance().getService(IRouter.class).routeToPrintPage().start(mAppService.getNowViewContext());
                                        } else {
                                            Logger.d("workspace failed");
                                            printController.setRemovePrintFlag(false);
                                            ResponseStructure responseStructure = new ResponseStructure();
                                            responseStructure.resultProp = new UInt8Prop(201);
                                            mRemoteConnectionController.sendResponse(commandSet, commandId, sequence, responseStructure);
                                        }
                                    }, e -> {
                                        LogHelper.log(e);
                                        printController.setRemovePrintFlag(false);
                                        ResponseStructure responseStructure = new ResponseStructure();
                                        responseStructure.resultProp = new UInt8Prop(202);
                                        mRemoteConnectionController.sendResponse(commandSet, commandId, sequence, responseStructure);
                                    });
                            mDisposable.add(sub);
                        }, e -> {

                        });
                mDisposable.add(parserSub);
            }
        }
    }

    private void startDiscoverServer() {
        new DiscoverServer(mAppService.getAppContext(), mPreferences.getHelper().getMachineName()).start();
    }

    private void startHttpServer() {
        mHttpServer = new HTTPServer();
        mHttpServer.startServer();
    }

    @Override
    public void ClearConnection() {
//        if (subscribe2 != null && !subscribe2.isDisposed()) subscribe2.dispose();
//        subscribe2 = Observable.timer(1, TimeUnit.SECONDS)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(times -> {
//                    Logger.d("---FDT--- Clearing a TCP Connection");
//                    mRemoteConnectionState.onNext(0);
//                    mDisposable.clear();
//                    mProxy.destroy();
//                    mRemoteLaserController = null;
//                    mRemoteFileController = null;
//                    mRemoteConnectionController = null;
//                    mAuthenticationClient.disconnect();
//                    mAuthenticationClient = null;
//                    mTempRemoteClient = null;
//                    su.dispose();
//                }, LogHelper::log);
        if (mAuthenticationClient != null) {
            try {
                mAuthenticationClient.disconnect();
            } catch (Exception ignored) {

            }
        }
        mRemoteConnectionState.onNext(0);
    }

    /**
     * Disconnect logic
     */
    private void clearConnectionConfiguration() {
        if (mRemoveDecisionDialog != null && mRemoveDecisionDialog.isShowing()) {
            mRemoveDecisionDialog.dismiss();
        }

        if (mDecisionDialog != null && mDecisionDialog.isShowing()) {
            mDecisionDialog.dismiss();
        }

        mDisposable.add(
                mMachine.getMachineController()
                        .requestBulkUnsubscribe(IProtocol.CommunicationId.LUBAN)
                        .subscribe(responseStructure -> {
                            if (!mDisposable.isDisposed()) {
                                mDisposable.clear();
                            }
                        }, LogHelper::log));

        if (mProxy != null) {
            mProxy.destroy();
        }

        mRemoteLaserController = null;
        mRemoteFileController = null;
        mRemoteConnectionController = null;
        mAuthenticationClient = null;
        mTempRemoteClient = null;
        mDisposable.clear();
    }

    @Override
    public Observable<ResponseStructure> disConnect() {
        return mRemoteConnectionController.request(0x01, 0x06, null, new ResponseStructure());
    }

    public RemoteClient getNowClient() {
        return mAuthenticationClient;
    }
}
