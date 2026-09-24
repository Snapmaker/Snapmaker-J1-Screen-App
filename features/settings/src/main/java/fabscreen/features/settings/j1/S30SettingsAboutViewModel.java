package fabscreen.features.settings.j1;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import fabscreen.features.settings.R;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.FabFileOutputStream;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.file.IPartition;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IFileManagerService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.INetwork;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import kotlin.io.FilesKt;

public class S30SettingsAboutViewModel extends BaseViewModel {

    private final IPreferences.Helper mPrefHelper;
    private final IMachine mMachine;
    private final IAppService mAppService;
    private final IFileManagerService mFileManager;
    private final INetwork mNetwork;
    private Disposable mExportDisposable;
    private final PublishSubject<ExportState> mExportStateSubj = PublishSubject.create();
    private Context mContext;

    public S30SettingsAboutViewModel() {
        mAppService = getServiceContainer().getService(IAppService.class);
        mFileManager = getServiceContainer().getService(IFileManagerService.class);
        mPrefHelper = getServiceContainer().getService(IPreferences.class).getHelper();
        mMachine = getServiceContainer().getService(IMachine.class);
        mNetwork = getServiceContainer().getService(INetwork.class);
        mContext = getServiceContainer().getService(IAppService.class).getAppContext();
    }

    public Observable<ExportState> getExportStateObservable() {
        return mExportStateSubj.hide();
    }

    public String getUserMachineName() {
        return mPrefHelper.getMachineName();
    }

    public String getMachineModelName() {
        return mMachine.getMachineInfoSubjectHolder().getValue().getModelName();
    }

    public String getWorkArea() {
        MachineInfo info = mMachine.getMachineInfoSubjectHolder().getValue();
        if (info.productId == IMachine.Product.J1) {
            return "300 x 200 x 200" + mContext.getString(R.string.all_unit_mm);
        } else if (info.productId == IMachine.Product.A400) {
            return "400 x 400 x 400" + mContext.getString(R.string.all_unit_mm);
        } else {
            return "Unknown";
        }
    }

    public String getIPAddress() {
        // check ip address
        String addressString = "Not Connected";
        try {
            List<NetworkInterface> interfaceList = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaceList) {
                List<InetAddress> addresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress address : addresses) {
                    if (!address.isLoopbackAddress()) {
                        String sAddr = address.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (isIPv4) {
                            addressString = sAddr;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            LogHelper.log(e);
        }
        return addressString;
    }

    public String getMacAddr() {
        return mNetwork.getMacAddress();
    }

    public String getStorageUsage() {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        return formatStorageUsage(stat.getTotalBytes(), stat.getAvailableBytes());
    }

    private String formatStorageUsage(long totalBytes, long availableBytes) {
        String available = availableBytes / 1024 / 1024 + "";
        String total = totalBytes / 1024 / 1024 + "";
        return mContext.getString(R.string.all_unit_m_has_diagonal, available)
                + mContext.getString(R.string.all_unit_m, total);
    }

    public Observable<Boolean> clearCache() {
        return Observable.create(emitter -> {
                    ServiceContainer.getInstance().getService(INetwork.class).removeOrDisableAllWifi();
                    ServiceContainer.getInstance().getService(IPrintWorkspace.class).clearAllWorkSpaceFiles();
                    File cacheDir = getServiceContainer().getService(IAppService.class).getAppContext().getCacheDir();
                    FilesKt.deleteRecursively(cacheDir);
                    emitter.onNext(true);
                })
                .concatMap(success -> resetSP().retry(10));
    }

    private Observable<Boolean> resetSP() {
        Logger.d("Resetting SP...");
        return Observable.create(emitter -> {
            if (ServiceContainer.getInstance().getService(IPreferences.class).getHelper().reset()) {
                emitter.onNext(true);
            } else {
                emitter.onError(new IllegalStateException("Reset SP fail!"));
            }
        });
    }

    public void exportLogsToUDisk() {
        if (mFileManager.getFabUsbDevice() == null) {
            mExportStateSubj.onNext(ExportState.ON_FAIL_NO_U_DISK);
            return;
        }
        mExportStateSubj.onNext(ExportState.ON_START);
        Scheduler.Worker worker = Schedulers.io().createWorker();
        mExportDisposable = worker.schedule(() -> {
            boolean scSucceed = exportToUDisk("SC");
            boolean fwSucceed = exportToUDisk("FW");
            mExportStateSubj.onNext(scSucceed && fwSucceed ? ExportState.ON_SUCCESS : ExportState.ON_FAIL_OTHER);
        });
    }

    /**
     * Export logs from latest to oldest, at most 3 files.
     *
     * @return true if success, false if fail.
     */
    private boolean exportToUDisk(String filePrefix) {
        String diskPath = mAppService.getAppContext().getCacheDir().getAbsolutePath();
        String folderPath = diskPath + File.separatorChar + "log";
        File folder = new File(folderPath);

        // Find max log file suffix number.
        int maxSuffix = 0;
        for (String fileName : Arrays.stream(folder.listFiles()).map(File::getName).collect(Collectors.toList())) {
            Logger.d("File found, name is %s", fileName);
            if (!fileName.contains("_") || !fileName.contains(filePrefix)) continue;
            int tempMax;
            try {
                tempMax = Integer.parseInt(fileName.substring(fileName.indexOf("_") + 1, fileName.indexOf(".")));
            } catch (NumberFormatException e) {
                // Not a log file.
                Logger.d("File \"%s\" is not a log file.", fileName);
                continue;
            }
            if (tempMax > maxSuffix) {
                maxSuffix = tempMax;
            }
        }

        // Copy 3 latest log files.
        int fileNo = maxSuffix;
        int copyCount = 0;
        File file;

        while (true) {
            String name = String.format("%s_%s.log", filePrefix, fileNo);
            Logger.d("Start copying file \"%s\"", name);
            file = new File(folderPath, name);

            if (!file.exists() || copyCount >= 20) {
                return true;
            }

            if (file.length() == 0) {
                fileNo--;
                continue;
            }

            Logger.d("Copying file \"%s\"", name);
            try {
                IPartition usbDevice = mFileManager.getFabUsbDevice();
                while (!"/".equals(usbDevice.getCurrentDirectory().getAbsolutePath())) {
                    Logger.d("Not root, popping...");
                    usbDevice.popDirectory();
                }
                IFile rootFile = usbDevice.getCurrentDirectory();

                IFile logFile = usbDevice.search(rootFile.getAbsolutePath() + name);
                if (logFile != null) {
                    Logger.d("Found existing file, removing...");
                    logFile.removeFile();
                }
                Logger.d("Creating new log file on U disk...");
                logFile = usbDevice.createFile(rootFile, name);
                try (OutputStream outputStream = new FabFileOutputStream(logFile.getOutputStream().getOutputStream()).getOutputStream();
                     InputStream inputStream = new FileInputStream(file)) {

                    byte[] buf = new byte[102400];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }

                    fileNo--;
                    copyCount++;
                }
            } catch (Throwable e) {
                LogHelper.log(e);
                return false;
            }
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (mExportDisposable != null) {
            mExportDisposable.dispose();
        }
    }

    public void exportLogsToRemote() {
        // TODO: 2022/6/18 export to luban/lava
        mExportStateSubj.onNext(ExportState.ON_START);
        mExportStateSubj.onNext(ExportState.ON_FAIL_NO_REMOTE);
    }

    public boolean isRemoteAvailable() {
        return false;
    }

    public void restartApp() {
        mAppService.restart();
    }

    public enum ExportState {
        ON_START,
        ON_SUCCESS,
        ON_FAIL_NO_LOGS,
        ON_FAIL_NO_U_DISK,
        ON_FAIL_NO_REMOTE,
        ON_FAIL_OTHER
    }
}
