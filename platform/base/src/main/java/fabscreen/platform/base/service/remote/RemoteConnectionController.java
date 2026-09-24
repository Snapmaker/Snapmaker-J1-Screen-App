package fabscreen.platform.base.service.remote;

import androidx.annotation.Nullable;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.connection.IConnection;
import fabscreen.platform.base.service.machine.protocol.IProtocol;
import fabscreen.platform.base.service.machine.protocol.ScreenAsServer;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class RemoteConnectionController {
    private final IConnection mConnection;
    private final IProtocol mProtocol;

    private final BehaviorSubject<byte[]> mProxyDataSubject;

    public RemoteConnectionController(RemoteClient remoteClient) {
        mConnection = remoteClient.mConnection;
        mProtocol = remoteClient.clientProtocol;
        mProxyDataSubject = remoteClient.mProxyDataSubject;
    }

    public <T extends IStructure> Observable<T> request(int commandSet, int commandId, @Nullable IStructure payload, T retStruct) {
        return mProtocol.request(commandSet, commandId, 0x00, payload, retStruct);
    }

    public Observable<ScreenAsServer.ClientRequest> listen() {
        return mProtocol.listen();
    }

    public void sendResponse(int commandSet, int commandId, int sequence, IStructure payload) {
        mProtocol.sendResponse(commandSet, commandId, IProtocol.CommunicationId.LUBAN, sequence, payload);
    }

    public void proxySendRaw(byte[] bytes) {
        mConnection.write(bytes);
    }

    public Observable<byte[]> getProxyDataObservable() {
        return mProxyDataSubject.hide();
    }

}
