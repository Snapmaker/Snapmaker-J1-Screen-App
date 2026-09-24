package fabscreen.platform.base.service.remote;

import java.io.IOException;
import java.net.Socket;

import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.connection.IConnection;
import fabscreen.platform.base.service.machine.protocol.IProtocol;
import fabscreen.platform.base.service.machine.protocol.SACPProtocol;
import fabscreen.platform.base.service.machine.protocol.ScreenAsServer;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

public class RemoteClient {
    private final Socket mSocket;
    BehaviorSubject<byte[]> mProxyDataSubject;
    IConnection mConnection;
    IProtocol clientProtocol;
    boolean mIsAuthentication;
    String mDeviceName;
    String mConnectingClients;
    String mToken;
    Disposable subscribe;
    Disposable mTimeSubscribe;
    private BehaviorSubject<Integer> mRemoteConnectionState;


    public RemoteClient(Socket socket, IPreferences preferences) throws IOException {
        mSocket = socket;
        initAndRegisterConnection(preferences);
    }

    private void initAndRegisterConnection(IPreferences p) throws IOException {
        if (mSocket.isClosed()) return;
        mConnection = new TCPConnection(mSocket);

        mProxyDataSubject = BehaviorSubject.create();

        clientProtocol = new SACPProtocol(p, mProxyDataSubject);
        clientProtocol.bindConnection(mConnection);

        if (mTimeSubscribe != null && mTimeSubscribe.isDisposed()) {
            mTimeSubscribe.dispose();
        }
        mTimeSubscribe = mConnection.getConnectionStatusObservable()
                .distinctUntilChanged()
                .filter(aBoolean -> !aBoolean)
                .takeUntil(aBoolean -> !aBoolean)
                .subscribe(aBoolean -> disconnect(), LogHelper::log);
    }

    public Observable<ScreenAsServer.ClientRequest> listeningAuthentication() {
        return clientProtocol.listen()
                .filter(clientRequest -> clientRequest.commandSet == 0x01 && clientRequest.commandId == 0x05);

    }

    public void disconnect() throws IOException {
        if (clientProtocol != null) {
            clientProtocol.disConnection();
            clientProtocol = null;
        }
        if (mConnection != null) {
            mConnection.disConnection();
        }
        if (mRemoteConnectionState != null) {
            mRemoteConnectionState.onNext(0);
        }
        if (subscribe != null && !subscribe.isDisposed()) subscribe.dispose();
        if (mTimeSubscribe != null && !mTimeSubscribe.isDisposed()) mTimeSubscribe.dispose();
    }

    public void setIsAuthentication(boolean b) {
        mIsAuthentication = b;
    }

    public void updateProtocol(BehaviorSubject<byte[]> proxyDataObservable) throws IOException {
        if (mProxyDataSubject != null && !mProxyDataSubject.hasComplete()) {
            mProxyDataSubject.onComplete();
        }
        mProxyDataSubject = proxyDataObservable;
        clientProtocol.updateProxyDataObservable(mProxyDataSubject);
    }


    public String getDeviceName() {
        return mDeviceName;
    }

    public void setDeviceName(String deviceName) {
        this.mDeviceName = deviceName;
    }

    public String getConnectingClients() {
        return mConnectingClients;
    }

    public void setConnectingClients(String connectingClients) {
        mConnectingClients = connectingClients;
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        this.mToken = token;
    }

    public void setRemoteConnectionState(BehaviorSubject<Integer> remoteConnectionState) {
        mRemoteConnectionState = remoteConnectionState;
    }

    public Socket getSocket() {
        return mSocket;
    }

}
