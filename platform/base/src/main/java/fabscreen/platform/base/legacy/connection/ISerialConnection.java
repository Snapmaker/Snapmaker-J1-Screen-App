package fabscreen.platform.base.legacy.connection;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import fabscreen.platform.base.BuildConfig;

public interface ISerialConnection {
    public static final int SSTP = 0;
    public static final int SACP = 1;

    public static ISerialConnection getInstance(@Protocol int protocol) {
        if (BuildConfig.DEBUG) {
            return MockSerialConnection.getInstance(protocol);
        } else {
            return DeprecatedSerialConnection.getInstance(protocol);
        }
    }

    void setConnectionListener(ConnectionListener listener);

    void setSerialDataListener(SerialDataListener listener);

    void connect(String device);

    void connect();

    void disconnect();

    void send(IPacket packet);

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SSTP, SACP})
    @interface Protocol {
    }

    public interface ConnectionListener {
        void onConnectionChanged(boolean connected);
    }

    public interface SerialDataListener {
        void onReceive(IPacket packet);
    }
}
