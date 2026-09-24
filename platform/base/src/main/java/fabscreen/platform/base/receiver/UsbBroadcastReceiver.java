package fabscreen.platform.base.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import java.io.IOException;

import fabscreen.platform.lib.LogHelper;

public class UsbBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_USB_PERMISSION = "com.snapmaker.USB_PERMISSION";
    private UsbListener mUsbListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        switch (action) {
            case ACTION_USB_PERMISSION: {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (usbDevice != null) {
                        if (mUsbListener != null) {
                            mUsbListener.devicePermissionGranted(usbDevice);
                        }
                    }
                } else {
                    if (mUsbListener != null) {
                        mUsbListener.devicePermissionDenied(usbDevice);
                    }
                }
                break;
            }

            case UsbManager.ACTION_USB_DEVICE_ATTACHED: {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    if (mUsbListener != null) {
                        try {
                            mUsbListener.deviceAttached(device);
                        } catch (Exception e) {
                            LogHelper.log(e);
                        }
                    }
                }
                break;
            }

            case UsbManager.ACTION_USB_DEVICE_DETACHED: {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    if (mUsbListener != null) {
                        mUsbListener.deviceDetached(device);
                    }
                }
                break;
            }

            default:
                break;
        }
    }

    public void setUsbListener(UsbListener mUsbListener) {
        this.mUsbListener = mUsbListener;
    }

    public interface UsbListener {
        void deviceAttached(UsbDevice usbDevice) throws IOException;

        void deviceDetached(UsbDevice usbDevice);

        void devicePermissionGranted(UsbDevice usbDevice);

        void devicePermissionDenied(UsbDevice usbDevice);
    }
}
