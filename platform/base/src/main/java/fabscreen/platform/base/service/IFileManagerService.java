package fabscreen.platform.base.service;

import fabscreen.platform.base.lib.file.FabLocalPartition;
import fabscreen.platform.base.lib.file.FabUsbPartition;
import fabscreen.platform.base.lib.file.IPartition;
import fabscreen.platform.lib.SubjectHolder;

public interface IFileManagerService {
    FabLocalPartition getFabLocalStorageDevice();

    FabUsbPartition getFabUsbDevice();

    /**
     * Returns the corresponding FileManager based on the input parameter
     *
     * @param isLocal
     * @return
     */
    IPartition getDevice(boolean isLocal);

    SubjectHolder<Boolean> getFileManagerStateSubjHolder();

    // Temporary interface method for close usb devices manually.
    // Normally we don't recognize where the files/devices came from, so it was a workaround method.
    void closeFabUsbDevices();

}
