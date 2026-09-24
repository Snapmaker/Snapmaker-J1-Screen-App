package fabscreen.platform.base.service.machine.protocol;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.connection.IConnection;
import io.reactivex.Observable;
import io.reactivex.subjects.Subject;
import okio.ByteString;

public class ScreenAsServer implements IServer<ScreenAsServer.ClientRequest> {

    private Subject<ClientRequest> mSubject;
    private final SACPProtocol mProtocol;

    public ScreenAsServer(SACPProtocol protocol) {
        mProtocol = protocol;
    }

    @Override
    public Observable<ClientRequest> listen(Subject<ClientRequest> serverSubject) {
        mSubject = serverSubject;
        return mSubject.hide();
    }

    @Override
    public void onRequest(IProtocol.Packet p) {
//        Logger.d("sc server received: %s", ByteString.of(p.rawBytes));
//        if (!(p.header.commandSet == 0xb0 && p.header.commandId == 0x01))
//            SACPLogger.logPacket(p);
        ClientRequest request = new ClientRequest();
        request.commandSet = p.header.commandSet;
        request.commandId = p.header.commandId;
        request.sequence = p.header.sequence;
        request.payload = p.payload;
        if (mSubject == null) {
            Logger.e("Listen subject is null.");
            return;
        }
        mSubject.onNext(request);
    }

    @Override
    public void sendResponse(int commandSet, int commandId, int receiverId, int sequence, IStructure payload) {
        IConnection connection = mProtocol.getConnection();
        if (connection == null) throw new IllegalStateException("Connection not available!");
        IProtocol.MessageHeader header = new IProtocol.MessageHeader();
        header.sequence = sequence;
        header.receiverId = receiverId;
        header.commandSet = commandSet;
        header.commandId = commandId;
        header.attribute = IProtocol.Attribute.ACK;
        byte[] encoded = mProtocol.encode(header, payload);
        connection.write(encoded);
    }

    public static class ClientRequest {
        public int commandSet;
        public int commandId;
        public int sequence;
        public byte[] payload;
    }
}
