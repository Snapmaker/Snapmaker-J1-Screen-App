package fabscreen.platform.base.service.machine.protocol;

import androidx.annotation.Nullable;

import java.io.IOException;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.connection.IConnection;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public interface IProtocol {
    byte[] encode(MessageHeader header, IStructure resultStructure);

    Packet decode(byte[] data);

    void unWatch(int commandSet, int commandId);

    interface HeartBeatListener {
        void onHeartbeatChanged(boolean active, int machineState);
    }

    void bindConnection(IConnection connection) throws IOException;

    void disConnection();

    Observable<ResponseStructure<IStructure>> enableHeartbeat();

    void setHeartbeatListener(HeartBeatListener listener);

    // request is implemented by Protocol
    // protocol need to find response from massive channelData
    // protocol implement timeout
    /*
     * 1. encode requestBody to bytes
     * 2. wrap bytes
     * 3. connection write bytes
     */
    <T extends IStructure> Observable<T> request(int commandSet, int commandId, int receiverId, @Nullable IStructure requestBody, T responseStruct) throws IllegalStateException;

    <T extends IStructure> Observable<T> watch(int commandSet, int commandId, T responseStruct);

    void updateProxyDataObservable(BehaviorSubject<byte[]> proxyDataObservable);

    Observable<ScreenAsServer.ClientRequest> listen();

    void sendResponse(int commandSet, int commandId, int receiverId, int sequence, IStructure payload);

    class CommunicationId {
        public static final int LUBAN = 0;
        public static final int CONTROLLER = 1;
        public static final int SCREEN = 2;
    }

    class Attribute {
        public static final int REQUEST = 0;
        public static final int ACK = 1;
    }

    class MessageHeader {
        public int receiverId = CommunicationId.CONTROLLER;
        public int senderId = CommunicationId.SCREEN;
        public int attribute = Attribute.REQUEST;
        public int sequence;
        public int commandSet;
        public int commandId;
    }

    class Packet {
        public MessageHeader header;
        public byte[] payload;
        public byte[] rawBytes;
    }
}
