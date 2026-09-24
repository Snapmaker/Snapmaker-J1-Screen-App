package fabscreen.platform.base.receiver;

import android.app.admin.DeviceAdminReceiver;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import com.orhanobut.logger.Logger;

/**
 * Device Owner Receiver
 * <p>
 * Use DeviceOwnerReceiver to avoid error imports.
 */
public class BluetoothDiscoverReceiver extends DeviceAdminReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        // Silence pair target device, directly confirm pair information
        // Stop broadcast to avoid pop up system dialog
        if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            try {
                device.setPairingConfirmation(true);
                abortBroadcast();
            } catch (Exception e) {
//                e.printStackTrace();
                Logger.d("---FDT--- BLUETOOTH_PRIVILEGED" + e);
            }
        }
    }
}
