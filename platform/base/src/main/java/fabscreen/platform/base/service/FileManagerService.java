package fabscreen.platform.base.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import androidx.annotation.Keep;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fabscreen.platform.base.R;
import fabscreen.platform.base.helper.FileHelper;
import fabscreen.platform.base.instantiation.IServiceIdentifier;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.FabLocalPartition;
import fabscreen.platform.base.lib.file.FabUsbMassStorageDevice;
import fabscreen.platform.base.lib.file.FabUsbPartition;
import fabscreen.platform.base.lib.file.IPartition;
import fabscreen.platform.base.receiver.UsbBroadcastReceiver;
import fabscreen.platform.base.view.SuperToastHelper;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.lib.SubjectHolder;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class FileManagerService implements IFileManagerService, UsbBroadcastReceiver.UsbListener, IServiceIdentifier {
    private Context mContext;
    private UsbBroadcastReceiver mReceiver;

    private Map<UsbDevice, FabUsbMassStorageDevice> mDevices = new HashMap<>();
    private List<IPartition> mFabUsbPartitions = new ArrayList<>();

    // TODO:
    private PublishSubject<Boolean> mDeviceAccessibleEvents = PublishSubject.create();
    private BehaviorSubject<Boolean> mUSBStorageStateEvents = BehaviorSubject.createDefault(false);
    private SubjectHolder<Boolean> mUSBStateEventsSubject = new SubjectHolder<>(mUSBStorageStateEvents);

    @Keep
    public FileManagerService(IPreferences preferences) {
        mContext = ServiceContainer.getInstance().getService(IAppService.class).getAppContext();
        if (!preferences.getHelper().getUSBFactoryModeOn()) {
            registerReceiver();
            initUsbDevices();
        }
    }

    private void registerReceiver() {
        mReceiver = new UsbBroadcastReceiver();
        mReceiver.setUsbListener(this);

        IntentFilter usbDeviceStateFilter = new IntentFilter();
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        usbDeviceStateFilter.addAction(UsbBroadcastReceiver.ACTION_USB_PERMISSION);
        mContext.registerReceiver(mReceiver, usbDeviceStateFilter);
    }

    private void initUsbDevices() {
        FabUsbMassStorageDevice fabDevice = null;
        UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(mContext);
        for (UsbMassStorageDevice device : devices) {
            try {
                UsbMassStorageDevice usbMassStorageDevice = checkPermissions(device);
                if (usbMassStorageDevice != null) {
                    fabDevice = new FabUsbMassStorageDevice(mContext, usbMassStorageDevice);
                }
                //FIXME: Get usbdevices?
                if (fabDevice != null) {
                    mUSBStorageStateEvents.onNext(true);
//                    mDevices.put(usbDevice, fabDevice);
                    mFabUsbPartitions.addAll(fabDevice.getPartitions());
                    fabDevice.setHang(true);
                    fabDevice.close();
                }
            } catch (Exception e) {
                LogHelper.log(e);
            }
        }
    }

    @Override
    public FabLocalPartition getFabLocalStorageDevice() {
        return new FabLocalPartition(mContext.getFilesDir().getPath());
    }

    // should use IPartition
    @Override
    public FabUsbPartition getFabUsbDevice() {
        Logger.d("getFabUsbDevice");
        if (mFabUsbPartitions == null || mFabUsbPartitions.size() == 0) {
            return null;
        } else {
            return (FabUsbPartition) mFabUsbPartitions.get(0);
        }
    }

    @Override
    public IPartition getDevice(boolean isLocal) {
        return isLocal ? getFabLocalStorageDevice() : getFabUsbDevice();
    }

    @Override
    public void deviceAttached(UsbDevice usbDevice) throws IOException {
        //TODO: hook to show the device-attached dialog
        showDeviceState(true);
        Logger.d("USB deviceAttached: " + usbDevice);
        FabUsbMassStorageDevice fabDevice = mDevices.get(usbDevice);
        if (fabDevice == null) {
            UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(mContext);
            for (UsbMassStorageDevice device : devices) {
                if (device.getUsbDevice().equals(usbDevice)) {
                    UsbMassStorageDevice usbMassStorageDevice = checkPermissions(device);
                    if (usbMassStorageDevice != null) {
                        fabDevice = new FabUsbMassStorageDevice(mContext, usbMassStorageDevice);
                    }
                }
            }
            if (fabDevice != null) {
                mUSBStorageStateEvents.onNext(true);
                mDevices.put(usbDevice, fabDevice);
                mFabUsbPartitions.addAll(fabDevice.getPartitions());
                fabDevice.setHang(true);
                fabDevice.close();
            }
        }
    }

    private UsbMassStorageDevice checkPermissions(UsbMassStorageDevice usbDevice) throws IOException {
        UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        if (usbManager == null) {
            return null;
        }
        if (usbManager.hasPermission(usbDevice.getUsbDevice())) {
            usbDevice.init();
            return usbDevice;
        } else {
            // Request permission
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(UsbBroadcastReceiver.ACTION_USB_PERMISSION), 0);
            Logger.d("Requesting USB permission...");
            usbManager.requestPermission(usbDevice.getUsbDevice(), pendingIntent);
            return null;
        }
    }

    @Override
    public void deviceDetached(UsbDevice usbDevice) {
        try {
            Logger.d("Device detached " + usbDevice);
            FabUsbMassStorageDevice fabDevice = mDevices.get(usbDevice);
            // TODO: Remove comments.
//        if (fabDevice != null && !fabDevice.isHang()) {
//            if (fabDevice.isHang()) {
//                fabDevice.setHang(false);
//            } else {
            if (fabDevice != null)
                fabDevice.closeDevices();
            mDevices.remove(usbDevice);
            mFabUsbPartitions.clear();
            mUSBStorageStateEvents.onNext(false);
            showDeviceState(false);
            clearThumbnailToCache();
        } catch (Exception e) {
            LogHelper.log(e);
        }
    }

    private void clearThumbnailToCache() {
        File cacheDir = ServiceContainer.getInstance().getService(IAppService.class).getAppContext().getCacheDir();
        String folderPath = cacheDir.getAbsolutePath() + "/gcode_thumbnail/USB";
        File file = new File(folderPath);
        if (file.exists()) {
            FileHelper.removeFile(file);
        }
    }

    private void showDeviceState(boolean isAttach) {
        Context nowViewContext = ServiceContainer.getInstance().getService(IAppService.class).getNowViewContext();

        int mSeriesId = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().seriesId;
        if (IMachine.MachineSeries.J == mSeriesId) {
            new SuperToastHelper.Builder()
                    .setToastForSingleLogo(isAttach ? R.drawable.pic_j1_usb_insert_normal_160x160 : R.drawable.pic_j1_usb_pull_out_normal_160x160)
                    .build()
                    .showToast(nowViewContext);

        } else {
            new SuperToastHelper.Builder()
                    .setDrawable(isAttach ? R.drawable.ic_usb_detected : R.drawable.ic_usb_unplugged)
                    .setMessage(nowViewContext.getString(isAttach ? R.string.toast_usb_device_detected : R.string.toast_usb_device_unpuggled))
                    .build()
                    .showToast(nowViewContext);
        }
    }

    @Override
    public void devicePermissionGranted(UsbDevice usbDevice) {
        Logger.d("On devicePermissionGranted...");
        try {
            FabUsbMassStorageDevice fabDevice = null;
            UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(mContext);
            for (UsbMassStorageDevice device : devices) {
                if (device.getUsbDevice().equals(usbDevice)) {
                    fabDevice = new FabUsbMassStorageDevice(mContext, device);
                }
            }
            if (fabDevice != null) {
                mUSBStorageStateEvents.onNext(true);
                mDevices.put(usbDevice, fabDevice);
                fabDevice.setPermissionGranted(true);
                // TODO:
                mFabUsbPartitions.addAll(fabDevice.getPartitions());
                fabDevice.setHang(true);
                fabDevice.close();
            }
//        // TODO: set our device AccessibleStatus to True
////        if (mUsbDevice != null && mUsbDevice.getUsbDevice().getDeviceId() == usbDevice.getDeviceId()) {
////            mDeviceAccessibleEvents.onNext(true);
////        }
        } catch (Exception e) {
            LogHelper.log(e);
        }

    }

    @Override
    public void devicePermissionDenied(UsbDevice usbDevice) {
        //FIXME:NOT TO DO EVENING
        Logger.d("On USB devicePermissionDenied...");
//        FabUsbMassStorageDevice fabDevice = mDevices.get(usbDevice);
//        // add if null
//        if (fabDevice != null) {
//            fabDevice.setPermissionGranted(false);adb
//        }
    }

    //TODO: redefine filemanagerstate
    @Override
    public SubjectHolder<Boolean> getFileManagerStateSubjHolder() {
        return mUSBStateEventsSubject;
    }

    @Override
    public void closeFabUsbDevices() {
        try {
            mContext.unregisterReceiver(mReceiver);
            for (FabUsbMassStorageDevice device : mDevices.values()) {
                device.closeDevices();
            }
        } catch (Exception e) {
            LogHelper.log(e);
        }

    }
}

