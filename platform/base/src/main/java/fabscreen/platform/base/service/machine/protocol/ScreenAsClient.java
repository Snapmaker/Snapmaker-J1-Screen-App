package fabscreen.platform.base.service.machine.protocol;

import android.util.SparseArray;

import androidx.annotation.Nullable;

import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.connection.IConnection;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import okio.Buffer;

public class ScreenAsClient implements IClient {
    private final SACPProtocol mProtocol;
    private static final long REQUEST_TIMEOUT = 3000L;
    private final SparseArray<ResponseHolder> mResponseHolders = new SparseArray<>();

    public ScreenAsClient(SACPProtocol protocol) {
        mProtocol = protocol;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IStructure> Observable<T> request(int commandSet, int commandId, int receiverId, @Nullable IStructure requestBody, T responseStruct) {
        IConnection connection = mProtocol.getConnection();
        if (connection == null) throw new IllegalStateException("Connection not available!");
        IProtocol.MessageHeader header = new IProtocol.MessageHeader();
        header.sequence = mProtocol.getNormalSequence();
        header.commandSet = commandSet;
        header.commandId = commandId;
        header.receiverId = receiverId;
        BehaviorSubject<IStructure> responseSubject = BehaviorSubject.create();
        ResponseHolder responseHolder = new ResponseHolder(responseSubject, responseStruct);
        mResponseHolders.put(header.sequence, responseHolder);
        byte[] encoded = mProtocol.encode(header, requestBody);
//        SACPLogger.logScreenRequest(header, requestBody, encoded);
        connection.write(encoded);
        // TODO: Currently, the retransmission mechanism is handled only on the subscription interface
        return (Observable<T>) responseSubject.hide();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IStructure> Observable<T> watch(int commandSet, int commandId, T responseStruct) {
        int pushSequence = mProtocol.generatePushSequence(commandSet, commandId);
        ResponseHolder responseHolder = mResponseHolders.get(pushSequence);
        if (responseHolder != null) {
            return (Observable<T>) responseHolder.getResponseSubject().hide();
        }
        BehaviorSubject<IStructure> responseSubject = BehaviorSubject.create();
        responseHolder = new ResponseHolder(responseSubject, responseStruct);
        mResponseHolders.put(pushSequence, responseHolder);
        return (Observable<T>) responseSubject.hide();
    }

    @Override
    public void onResponse(IProtocol.Packet p) {
        Buffer payloadBuffer = new Buffer();
        payloadBuffer.write(p.payload);
        int sequence = mProtocol.getSequenceByHeader(p.header);
        ResponseHolder responseHolder = mResponseHolders.get(sequence);
        if (responseHolder == null) return;// No watcher yet.
        BehaviorSubject<IStructure> responseSubject = responseHolder.getResponseSubject();
        IStructure responseStructure = responseHolder.getResponseStructure();
        if (responseSubject == null || responseStructure == null) return;
        try {
            responseStructure.readBuffer(payloadBuffer);
            responseSubject.onNext(responseStructure);
//            SACPLogger.logStructurePacket(p, responseStructure);
        } catch (Exception e) {
            SACPLogger.logReadableErrors(p, responseStructure, e);
        }
        // Only remove request-response sequences.(?)
        if (!SACPProtocol.isPush(p.header.commandId)) {
            responseSubject.onComplete();
            mResponseHolders.remove(p.header.sequence);
        }
    }

    @Override
    public void unWatch(int commandSet, int commandId) {
        int pushSequence = mProtocol.generatePushSequence(commandSet, commandId);
        ResponseHolder responseHolder = mResponseHolders.get(pushSequence);
        if (responseHolder != null) {
            mResponseHolders.delete(pushSequence);
        }
    }
}
