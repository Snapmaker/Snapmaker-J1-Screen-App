package fabscreen.platform.base.receiver;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import fabscreen.platform.base.helper.DPCHelper;


/**
 * Device Owner Receiver
 * <p>
 * Use DeviceOwnerReceiver to avoid error imports.
 */
public class DeviceOwnerReceiver extends DeviceAdminReceiver {
    private static final String TAG = "DeviceOwnerReceiver";

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);

        Log.d(TAG, "Device Owner enabled");

        DPCHelper.enableKioskMode(context);
    }
}
