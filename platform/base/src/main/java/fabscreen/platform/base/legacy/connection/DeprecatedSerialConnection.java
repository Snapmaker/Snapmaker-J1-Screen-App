package fabscreen.platform.base.legacy.connection;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.legacy.connection.fabpacket.PacketInputStream;
import fabscreen.platform.base.legacy.connection.fabpacket.sacp.SACPPacketInputStream;
import fabscreen.platform.base.legacy.connection.fabpacket.sstp.SSTPPacketInputStream;
import fabscreen.platform.lib.serialport.SerialPort;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * A wrapper for serial port which encapsulate input and output byte stream as SSTPPacket.
 */
@Deprecated
public class DeprecatedSerialConnection implements ISerialConnection {
    private static final String TAG = DeprecatedSerialConnection.class.getSimpleName();
    private static DeprecatedSerialConnection instance;

    private SerialPort mSerialPort;

    private Disposable mReadWorker;

    private Subject<IPacket> mWriteSubject;
    private Subject<IPacket> mWriteSerializedSubject;
    private Disposable mWriteSubscription;

    private SerialDataListener mSerialDataListener;
    private ConnectionListener mConnectionListener;

    private @Protocol
    int mProtocol;

    private DeprecatedSerialConnection(int protocol) {
        mProtocol = protocol;
    }

    public static DeprecatedSerialConnection getInstance(@Protocol int protocol) {
        if (instance == null) {
            synchronized (DeprecatedSerialConnection.class) {
                if (instance == null) {
                    instance = new DeprecatedSerialConnection(protocol);
                }
            }
        }
        return instance;
    }


    public void setConnectionListener(ConnectionListener listener) {
        if (mConnectionListener != null) {
            Log.e(TAG, "ConnectionListener has been set!");
            return;
        }
        mConnectionListener = listener;
    }

    public void setSerialDataListener(SerialDataListener listener) {
        if (mSerialDataListener != null) {
            Log.e(TAG, "SerialDataListener has been set!");
            return;
        }
        mSerialDataListener = listener;
    }

    private boolean isConnected() {
        return mSerialPort != null;
    }


    /**
     * Connect to given serial port device.
     */
    public void connect(String device) {
        if (isConnected()) {
            disconnect();
        }

        try {
            mSerialPort = new SerialPort(new File(device), 115200);
        } catch (IOException | InterruptedException e) {
            Log.w(TAG, "Unable to connect to serial port " + device);

            if (mConnectionListener != null) {
                mConnectionListener.onConnectionChanged(false);
            }

            return;
        }

        // Observe write data and send them with IO scheduler
        mWriteSubject = PublishSubject.create();
        mWriteSerializedSubject = mWriteSubject.toSerialized();
        mWriteSubscription = mWriteSerializedSubject
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(p -> {
                    mSerialPort.getOutputStream().write(p.toByteArray());
                });

        // Read byte streams from serial port and parse as FabPacket
        Scheduler.Worker worker = Schedulers.io().createWorker();
        worker.schedule(() -> {
            InputStream in = mSerialPort.getInputStream();
            PacketInputStream is = getPacketInputStream(in);
            while (true) {
                try {
                    IPacket packet = is.readPacket();
                    if (packet != null && mSerialDataListener != null) {
                        mSerialDataListener.onReceive(packet);
                    }
                } catch (IOException e) {
                    LogHelper.log(e);
                    // disconnect when IOException raises
                    disconnect();

                    // close input stream
                    try {
                        is.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    break;
                }
            }
        });
        mReadWorker = worker;

        if (mConnectionListener != null) {
            mConnectionListener.onConnectionChanged(true);
        }
    }

    private PacketInputStream getPacketInputStream(InputStream in) {
        if (mProtocol == SSTP) {
            return new SSTPPacketInputStream(in);
        } else if (mProtocol == SACP) {
            return new SACPPacketInputStream(in);
        } else {
            throw new IllegalStateException("Protocol not matched!");
        }
    }

    public void connect() {
        connect("/dev/ttyHSL1");
    }

    /**
     * Disconnect from serial port.
     */
    public void disconnect() {
        if (isConnected()) {
            mReadWorker.dispose();
            mReadWorker = null;

            mWriteSubscription.dispose();
            mWriteSubscription = null;

            mWriteSerializedSubject = null;
            mWriteSubject = null;

            mSerialPort.close();
            mSerialPort = null;

            if (mConnectionListener != null) {
                mConnectionListener.onConnectionChanged(false);
            }
        }
    }

    /**
     * Send packet to serial port.
     *
     * @param packet packet to be sent.
     */
    public void send(IPacket packet) {
        if (!isConnected()) {
            Log.w(TAG, "Connect to serial port before send any data.");
            return;
        }
        if (mWriteSerializedSubject != null) {
            mWriteSerializedSubject.onNext(packet);
        }
    }
}
