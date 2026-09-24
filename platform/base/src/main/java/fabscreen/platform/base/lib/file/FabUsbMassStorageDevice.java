package fabscreen.platform.base.lib.file;

import android.content.Context;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.partition.Partition;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.view.FabScreenDialog;
import fabscreen.platform.lib.LogHelper;

// Warp UsbMassStorageDevice to implement auto hang feature
public class FabUsbMassStorageDevice {
    private Context mContext;
    private UsbMassStorageDevice mDevice;

    private boolean isPermissionGranted = false;
    private boolean isOpen = false;
    private boolean isError = false;

    private boolean isHang;

    public FabUsbMassStorageDevice(Context context, UsbMassStorageDevice device) {
        mDevice = device;
        mContext = context;
        try {
            mDevice.init();
            isOpen = true;
        } catch (Exception e) {
            isOpen = false;
            LogHelper.log(e);
//            e.printStackTrace();
        }
        isHang = false;
    }

    public boolean isHang() {
        return isHang;
    }

    public void setHang(boolean hang) {
        isHang = hang;
    }

    public void setPermissionGranted(boolean permissionGranted) {
        isPermissionGranted = permissionGranted;
        // we can list file here when permissionGranted;
    }

    public void close() {
        isOpen = false;
        // TODO: 2022/6/29 Something need to be done here.
    }

    public List<IPartition> getPartitions() {
        List<IPartition> partitions = new ArrayList<>();
        mDevice.getPartitions().forEach((p) -> {
            partitions.add(new FabUsbPartition(mContext, this, p));
        });
        return partitions;
    }

    public FileSystem getFileSystem(Partition mPartition) {
        if (!isOpen) {
            try {
                mDevice.init();
                isOpen = true;
                return mPartition.getFileSystem();
            } catch (IOException | IllegalStateException e) {
                if (!isError) {
                    isError = true;
                    Context nowViewContext = ServiceContainer.getInstance().getService(IAppService.class).getNowViewContext();
                    if (nowViewContext != null) {
                        FabScreenDialog.create(nowViewContext)
                                .setTitle("无法识别的USB设备")
                                .setConfirm("USB设备异常或设备文件系统非FAT32", (dialog, which) -> {
                                    dialog.dismiss();
                                })
                                .show();
                    }


                }

                e.printStackTrace();
            }
        }
        return mPartition.getFileSystem();
    }

    public void closeDevices() {
        try {
            mDevice.close();
            isOpen = false;
        } catch (Exception e) {
            Logger.d("USB close Error:" + e);
        }

    }

    public void restart() {
        try {
            mDevice.close();
            isOpen = false;
            mDevice.init();
            isOpen = true;
        } catch (Exception e) {
            Logger.d("USB close Error:" + e);
        }
    }
}
