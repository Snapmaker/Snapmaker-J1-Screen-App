package fabscreen.platform.base.legacy.server.socket;


import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.lib.SubjectHolder;
import okio.Buffer;
import okio.ByteString;


/**
 * Discover Server: Socket server for device discovery.
 * <p>
 * Start server:
 * discoverServer = new DiscoverServer(context, "Snapmaker");
 * discoverServer.start()
 */
public class DiscoverServer extends Thread {
    private static final int BIND_PORT = 20054;
    private static final String DISCOVER_MESSAGE = "discover";

    private final WifiManager mWifiManager;
    private DatagramSocket mSocket;

    private String mDisplayName = "A400-DEBUG";
    private long mLastMills = 0;

    /**
     * DiscoverServer: bind socket and wait for client to search
     *
     * @param context Use for creating WifiManager to get current ip
     */
    public DiscoverServer(Context context, String name) {
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mDisplayName = name;
    }

    private static String intToIp(int ipAddress) {
        return (ipAddress & 0xFF) + "." +
                ((ipAddress >> 8) & 0xFF) + "." +
                ((ipAddress >> 16) & 0xFF) + "." +
                (ipAddress >> 24 & 0xFF);
    }

    public void run() {
        try {
            mSocket = new DatagramSocket(BIND_PORT);
            mSocket.setSoTimeout(2000);
            mSocket.setReuseAddress(true);
            Logger.d("Discover server running on %s:%s...", getHostAddress(), BIND_PORT);

            byte[] data = new byte[64];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            while (true) {
                try {
                    mSocket.receive(packet);
                } catch (SocketTimeoutException e) {
                    continue;
                }

                String message = new String(data, 0, packet.getLength(), StandardCharsets.UTF_8);

                if (message.equals(DISCOVER_MESSAGE)) {
                    long currentMills = SystemClock.elapsedRealtime();
                    byte[] bytes = getResponse();
                    SocketAddress address = packet.getSocketAddress();
                    DatagramPacket sendPacket = new DatagramPacket(bytes, bytes.length, address);
                    mSocket.send(sendPacket);
                    if ((currentMills - mLastMills) > 1000 || mLastMills == 0) {
                        Logger.d("Discover request, response sent.");
                    } else {
                        continue;
                    }
                    mLastMills = currentMills;
                }
            }
        } catch (SocketException e) {
            LogHelper.log(e);
        } catch (IOException e) {
            Logger.d("Discover server closed.");
            LogHelper.log(e);
        } finally {
            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
                Logger.d("Discover server closed.");
            }
        }
    }

    @Override
    public void interrupt() {
        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
            Logger.d("Discover server closed.");
        }

        super.interrupt();
    }

    private byte[] getResponse() {

//        final String machineName = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineName();
        String machineModal = "NaN";
        final SubjectHolder<MachineInfo> machineInfoSubjectHolder = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder();
        if (machineInfoSubjectHolder != null) {
            machineModal = machineInfoSubjectHolder.getValue().getModelName();
        }

        // Build response string
        // {name}@{ip}|model:{model}|status:{status}
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("%s@%s", mDisplayName, getHostAddress()));
        stringBuilder.append(String.format("|%s:%s", "model", machineModal));
        stringBuilder.append(String.format("|%s:%s", "SACP", "1"));


        String description = stringBuilder.toString();

        Buffer buffer = new Buffer();
        buffer.write(ByteString.encodeUtf8(description));
        return buffer.readByteArray();
    }

    private String getHostAddress() {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        return intToIp(wifiInfo.getIpAddress());
    }
}
