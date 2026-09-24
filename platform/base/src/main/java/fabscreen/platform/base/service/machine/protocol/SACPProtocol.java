package fabscreen.platform.base.service.machine.protocol;

import android.os.SystemClock;

import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import fabscreen.platform.base.legacy.connection.fabpacket.PacketInputStream;
import fabscreen.platform.base.legacy.connection.fabpacket.sacp.SACPPacketInputStream;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.connection.IConnection;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.SubscribeStructure;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.lib.ChecksumUtils;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import okio.Buffer;
import okio.ByteString;

public class SACPProtocol implements IProtocol {

    private IConnection mConnection;
    private static final ByteString MARKER = ByteString.decodeHex("aa55");
    private static final int PROTO_VERSION = 1;
    private final AtomicInteger mSequence = new AtomicInteger(-1);
    private long mLastHeartBeatTime = 0;
    public static final int HEARTBEAT_INTERVAL = 5000;
    private HeartBeatListener mHeartBeatListener;
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private final IPreferences mPreferences;
    private final IClient mClient;
    private final IServer<ScreenAsServer.ClientRequest> mServer;
    private BehaviorSubject<byte[]> mProxyDataSubject;
    private Scheduler.Worker worker;
    private boolean isBind = false;

    public SACPProtocol(IPreferences preferences, BehaviorSubject<byte[]> proxyDataSubject) {
        mPreferences = preferences;
        mProxyDataSubject = proxyDataSubject;
        mClient = new ScreenAsClient(this);
        mServer = new ScreenAsServer(this);
    }

    @Override
    public <T extends IStructure> Observable<T> request(int commandSet, int commandId, int receiverId, @Nullable IStructure requestBody, T responseStruct) {
        return mClient.request(commandSet, commandId, receiverId, requestBody, responseStruct);
    }

    @Override
    public <T extends IStructure> Observable<T> watch(int commandSet, int commandId, T responseStruct) {
        return mClient.watch(commandSet, commandId, responseStruct);
    }

    @Override
    public Observable<ScreenAsServer.ClientRequest> listen() {
        BehaviorSubject<ScreenAsServer.ClientRequest> subject = BehaviorSubject.create();
        return mServer.listen(subject);
    }

    @Override
    public void sendResponse(int commandSet, int commandId, int receiverId, int sequence, IStructure payload) {
        mServer.sendResponse(commandSet, commandId, receiverId, sequence, payload);
    }


    @Override
    public void updateProxyDataObservable(BehaviorSubject<byte[]> proxyDataObservable) {
        if (mProxyDataSubject != null && !mProxyDataSubject.hasComplete()) {
            mProxyDataSubject.onComplete();
        }
        mProxyDataSubject = proxyDataObservable;
    }

    /**
     * Decode read data and handle it.
     *
     * @param bytes byte[] data read.
     */
    private void onPacketReceived(byte[] bytes) {
        if (bytes == null) return;
        Packet p = decode(bytes);
        if (p == null) return;
        int attribute = p.header.attribute;
        int receiverId = p.header.receiverId;
//        Logger.d("---FDT--- onPacketReceived %d data:%s",receiverId,ByteString.of(bytes).hex());
        if (receiverId != CommunicationId.SCREEN) {
            if (!mProxyDataSubject.hasComplete()) {
                // pass-through
                mProxyDataSubject.onNext(bytes);
            }
        } else {
            if (attribute == Attribute.ACK) {
                mClient.onResponse(p);
            } else if (attribute == Attribute.REQUEST) {
                mServer.onRequest(p);
            }
        }
    }

    @Override
    public void setHeartbeatListener(HeartBeatListener listener) {
        mHeartBeatListener = listener;
    }


    public byte[] encode(MessageHeader header, @Nullable IStructure struct) {
        byte[] payload = struct == null ? null : struct.toByteArray();
        int length = payload == null ? 8 : 8 + payload.length;
        Buffer buffer = new Buffer();
        buffer.write(MARKER);
        buffer.writeShortLe(length);
        buffer.writeByte(PROTO_VERSION);
        buffer.writeByte(header.receiverId);
        byte crc8 = 0;
        int checksum = 0;
        buffer.writeByte(crc8);
        buffer.writeByte(header.senderId);
        buffer.writeByte(header.attribute);
        buffer.writeShortLe(header.sequence);
        buffer.writeByte(header.commandSet);
        buffer.writeByte(header.commandId);
        if (payload != null) {
            buffer.write(payload);
        }
        buffer.writeShort(checksum);
        byte[] bytes = buffer.readByteArray();
        // Replace crc witch calculated one.
        bytes[6] = ChecksumUtils.calculateCRC8(bytes, 0, 6);
        checksum = ChecksumUtils.calculateChecksum(bytes, 7, length - 2);
        // Replace checksum witch calculated one.
        bytes[bytes.length - 1] = (byte) (checksum >> 8 & 0xff);
        bytes[bytes.length - 2] = (byte) (checksum & 0xff);
        return bytes;
    }

    public Packet decode(byte[] bytes) {
        Packet packet = new Packet();
        packet.header = new SACPProtocol.MessageHeader();
        packet.header.receiverId = bytes[5];
        packet.header.senderId = bytes[7];
        packet.header.attribute = bytes[8];
        packet.header.sequence = (bytes[9] & 0xff) | ((bytes[10] & 0xff) << 8);
        packet.header.commandSet = bytes[11] & 0xff;
        packet.header.commandId = bytes[12] & 0xff;
        byte[] payload = new byte[bytes.length - 15];
        System.arraycopy(bytes, 13, payload, 0, payload.length);
        packet.payload = payload;
        packet.rawBytes = bytes;
        return packet;
    }

    @Override
    public void unWatch(int commandSet, int commandId) {
        mClient.unWatch(commandSet, commandId);
    }

    @Override
    public void bindConnection(IConnection connection) {
        mConnection = connection;
        isBind = true;
        mDisposable.add(
                connection
                        .getInputStreamObservable()
                        .subscribe(inputStream -> {
//                            Logger.d("sacp: input stream got!");
                            readPacket(inputStream);
                        }, LogHelper::log));
    }

    @Override
    public void disConnection() {
        isBind = false;
        if (worker != null && !worker.isDisposed()) {
            worker.dispose();
        }
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    private void readPacket(InputStream inputStream) {
        if (inputStream == null) return;
        worker = Schedulers.io().createWorker();
        worker.schedule(() -> {
            PacketInputStream packetInputStream = new SACPPacketInputStream(inputStream);
            while (isBind) {
                try {
                    onPacketReceived(packetInputStream.readRawPacket());
                } catch (SocketException e) {
                    try {
                        inputStream.close();
                    } catch (Exception e1) {
                        LogHelper.log(e);
                    }
                    Logger.e("---FDT--- readPacket inputStream is close " + e);
                    disConnection();
                } catch (InterruptedIOException e) {
                    try {
                        inputStream.close();
                    } catch (Exception e1) {
                        LogHelper.log(e);
                    }
                    disConnection();
                } catch (IOException e) {
                    LogHelper.log(e);
                    break;
                }
            }
        });
    }

    @Override
    public Observable<ResponseStructure<IStructure>> enableHeartbeat() {
        Logger.d("sacp-debug: enabling heartbeat");
        startCheckHeartbeat();
        return Observable.create((ObservableOnSubscribe<ResponseStructure<IStructure>>) emitter -> {
            SubscribeStructure subscribeStructure = new SubscribeStructure(0x01, 0xa0, 500);
            mDisposable.add(
                    request(0x01, 0x00, 0x01, subscribeStructure, new ResponseStructure<>())
                            .subscribe(response -> {
                                if (response.isSuccess()) {
                                    emitter.onNext(response);
                                    emitter.onComplete();
                                }
                            }));
        }).timeout(6, TimeUnit.SECONDS);
    }

    private void startCheckHeartbeat() {
        ResponseStructure<UInt8Prop> baseStructureResponseStructure = new ResponseStructure<>(new UInt8Prop());
        mDisposable.add(watch(0x01, 0xa0, baseStructureResponseStructure)
                .subscribe(heartBeatResponse -> {
                    long currentTime = SystemClock.elapsedRealtime();
                    // TODO: 2022/3/26  How many seconds of heartbeat delay can be a acceptable?
                    if (mLastHeartBeatTime == 0 || currentTime - mLastHeartBeatTime < HEARTBEAT_INTERVAL * 2) {
                        mHeartBeatListener.onHeartbeatChanged(true, heartBeatResponse.dataProp.getValue());
                    }
                    mLastHeartBeatTime = currentTime;
                }));

        mDisposable.add(Observable.interval(HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS)
                .subscribe(aLong -> {
                    long currentTime = SystemClock.elapsedRealtime();
                    if (currentTime - mLastHeartBeatTime > HEARTBEAT_INTERVAL * 2) {
                        // FIXME:Disconnection status of machine to be determined
                        mHeartBeatListener.onHeartbeatChanged(false, 0);
                        // TODO: Should be zero after disconnection occurs?
//                        mLastHeartBeatTime = 0;
                    }
                }));
    }

    // Request-response sequence begins at 0 ends at 65535;
    // Let's start from 65536.
    int generatePushSequence(int commandSet, int commandId) {
        return 0xffff + 1 + commandSet * 1000 + commandId;
    }

    // Get sequence of request-response
    int getNormalSequence() {
        if (mSequence.get() < 0xffff) {
            mSequence.incrementAndGet();
        } else {
            mSequence.set(0);
        }
        return mSequence.get();
    }

    public static boolean isPush(int cmdId) {
        return cmdId >= 0xa0;
    }

    public int getSequenceByHeader(MessageHeader header) {
        if (isPush(header.commandId)) {
            return generatePushSequence(header.commandSet, header.commandId);
        } else {
            return header.sequence;
        }
    }

    public IConnection getConnection() {
        return mConnection;
    }
}
