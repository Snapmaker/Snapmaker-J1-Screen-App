package fabscreen.platform.base.legacy.connection;

import com.orhanobut.logger.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.legacy.connection.fabpacket.sacp.SACPPacket;
import fabscreen.platform.base.legacy.connection.fabpacket.sacp.SACPPacketBuilder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import okio.Buffer;
import okio.ByteString;

public class MockSerialConnection implements ISerialConnection {

    private static MockSerialConnection mInstance;
    private static int delayTime = 0;
    private static Random mRandom = new Random(System.currentTimeMillis());
    private ConnectionListener mConnectionListener;
    private SerialDataListener mSerialDataListener;
    private CompositeDisposable disposables = new CompositeDisposable();
    private @Protocol
    int mProtocol;

    private MockSerialConnection(int protocol) {
        mProtocol = protocol;
    }

    public static MockSerialConnection getInstance(@Protocol int protocol) {
        if (mInstance == null) {
            synchronized (DeprecatedSerialConnection.class) {
                if (mInstance == null) {
                    mInstance = new MockSerialConnection(protocol);
                }
            }
        }
        return mInstance;
    }

    @Override
    public void setConnectionListener(ConnectionListener listener) {
        mConnectionListener = listener;
    }

    @Override
    public void setSerialDataListener(SerialDataListener listener) {
        mSerialDataListener = listener;
    }

    @Override
    public void connect(String device) {
        if (mConnectionListener == null) return;
        mConnectionListener.onConnectionChanged(true);
    }

    @Override
    public void connect() {
        if (mConnectionListener == null) return;
        mConnectionListener.onConnectionChanged(true);
    }

    @Override
    public void disconnect() {
        if (mConnectionListener == null) return;
        mConnectionListener.onConnectionChanged(false);
    }

    @Override
    public void send(IPacket packet) {
        if (packet instanceof SSTPPacket) {
            mockSSTPResponse((SSTPPacket) packet);
        } else {
            mockSACPResponse((SACPPacket) packet);
        }
    }

    private void mockSACPResponse(SACPPacket packet) {

        mSerialDataListener.onReceive(SACPPacketBuilder.getInstance().buildSubscribeHeartBeat(0x02));
    }

    /**
     * Mock response based on request packet, then use
     * {@link ISerialConnection.SerialDataListener}
     * to notify data change.
     *
     * @param sstpPacket The request packet.
     */
    private void mockSSTPResponse(SSTPPacket sstpPacket) {
        if (mSerialDataListener == null) return;
        byte eventId = sstpPacket.getEventId();
        byte subEventId = sstpPacket.getSubeventId();
        if (!(eventId == 7 && subEventId == 1 || eventId == 0x03)) {
            Logger.d("mock response: event id is %1$s, subEvent id is %2$s", ByteString.of(eventId).hex(), ByteString.of(subEventId).hex());
        }

        switch (eventId) {
            case 0x01:
                try {
                    Buffer buffer = new Buffer();
                    buffer.write(sstpPacket.getContent());
                    if (buffer.readInt() == 0) {
                        String str = buffer.readString(StandardCharsets.UTF_8);
                        Logger.d("Str " + str);
                        if ("G53".equals(str)) {
                            MockResponsePacketBuilder.getInstance().setCoordinateAligned((byte) 0);
                        } else if ("G54".equals(str)) {
                            MockResponsePacketBuilder.getInstance().setCoordinateAligned((byte) 1);
                        } else if (str.contains("M104 S")) {
                            MockResponsePacketBuilder.getInstance().setHeadTargetTemp(Short.parseShort(str.substring(6)));
                        } else if ("G28".equals(str)) {
                            MockResponsePacketBuilder.getInstance().setHomed((byte) 0);
                        } else if (str.contains("G0 ")) {
                            delayTime = mRandom.nextInt(5);
                        } else if (str.contains("G1 ")) {
                            delayTime = mRandom.nextInt(5);
                        }
                    }
                } catch (Exception e) {
                    LogHelper.log(e);
                    break;
                } finally {
                    if (delayTime == 0) {
                        mSerialDataListener.onReceive(MockResponsePacketBuilder.getInstance().buildGcodeResponse());
                    } else {
                        Disposable sub = Observable.timer(delayTime, TimeUnit.SECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(time -> {
                                    mSerialDataListener.onReceive(MockResponsePacketBuilder.getInstance().buildGcodeResponse());
                                    delayTime = 0;
                                });
                        disposables.add(sub);
                    }

                }
                break;
            case 0x03:
                mSerialDataListener.onReceive(MockResponsePacketBuilder.getInstance().buildPrintGcodeResponse());
                break;
            case 0x07:
                switch (subEventId) {
                    case 0x01:
                        mSerialDataListener.onReceive(MockResponsePacketBuilder.getInstance().buildMachineStatus());
                        break;
                    case 0x03:
                    case 0x04:
                    case 0x05:
                    case 0x06:
                    case 0x07:
                    case 0x0a:
                    case 0x0b:
                    case 0x12:
                        mSerialDataListener.onReceive(MockResponsePacketBuilder.getInstance().buildTrue(eventId, subEventId));
                        break;
                    case 0x0c:
                        break;
                    case 0x08:
                        mSerialDataListener.onReceive(MockResponsePacketBuilder.getInstance().buildPrintGcodeLine());
                        break;
                    case 0x0e:
                        mSerialDataListener.onReceive(MockResponsePacketBuilder.getInstance().buildCoordinateSystem());
                        break;
                    // TODO: 2021/12/20 other responses
                }
                break;
            // TODO: 2021/12/20 other responses
            case 0x0b:
                switch (subEventId) {
                    case 0x02:
                        mSerialDataListener.onReceive(MockResponsePacketBuilder.getInstance().buildTrue(eventId, subEventId));
                        break;
                }
                break;
            case 0x09:
                switch (subEventId) {
                    case 0x0b:
                        mSerialDataListener.onReceive(MockResponsePacketBuilder.getInstance().buildTrue(eventId, subEventId));
                        break;
                    case 0x14:
                        mSerialDataListener.onReceive(MockResponsePacketBuilder.getInstance().buildMachineSize());
                        break;
                }
                break;
            case SSTPPacket.MOCK_REQUEST_EVENT_ID:
                switch (subEventId) {
                    case 0x01:
                        mSerialDataListener.onReceive(MockResponsePacketBuilder.getInstance().buildMachineType());
                        break;
                }
                break;
            case SSTPPacket.ADD_ON_OPERATION_REQUEST_EVENT_ID:
                switch (subEventId) {
                    case 0x01:
                        mSerialDataListener.onReceive(MockResponsePacketBuilder.getInstance().buildEnclosureStatus());
                        break;
                    case 0x08:
                        mSerialDataListener.onReceive(MockResponsePacketBuilder.getInstance().buildRotaryModuleStatus());
                        break;
                    case 0x09:
                        mSerialDataListener.onReceive(MockResponsePacketBuilder.getInstance().buildAirPurifierStatus());
                        break;
                }
            default:
                break;
        }
    }

}
