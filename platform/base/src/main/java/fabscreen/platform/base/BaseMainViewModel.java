package fabscreen.platform.base;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.helper.FileHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.file.IPartition;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IFileManagerService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.structure.MachineProductInfo;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public abstract class BaseMainViewModel extends BaseViewModel {

    protected final IAppService mAppService;
    protected final IMachine mMachine;
    private final CompositeDisposable mDisposables = new CompositeDisposable();
    private final BehaviorSubject<InitStatus> mInitStatusSubj = BehaviorSubject.create();

    public BaseMainViewModel() {
        mAppService = getServiceContainer().getService(IAppService.class);
        mMachine = getServiceContainer().getService(IMachine.class);
        observeMemoryUse();

        mInitStatusSubj
                .as(bindToLifecycle())
                .subscribe(status -> {
                    if (status == InitStatus.UPDATE_CONFIRMED || status == InitStatus.UPDATE_IDLE) {
                        observeMachineInit();
                    }
                });
    }

    private void observeMachineInit() {
        Logger.d("Starting timeout countdown...");
        mDisposables.add(Observable.timer(1, TimeUnit.MINUTES)
                .flatMap(time -> {
                    Logger.d("Machine not response in 1 minute, timeout.");
                    if (mMachine.getMachineStatusSubjectHolder().getValue().connected) {
                        mInitStatusSubj.onNext(InitStatus.MAINBOARD_CONNECTED);
                        return checkModuleVersions()
                                .flatMap(versionsOK -> {
                                    Logger.d("EM version checked, %s.", versionsOK ? "all pass" : "outdated");
                                    mInitStatusSubj.onNext(versionsOK ? InitStatus.FINISH : InitStatus.MODULE_FW_OUTDATED);
                                    return Observable.just(true);
                                });
                    } else {
                        return Observable.just(false);
                    }
                })
                .as(bindToLifecycle())
                .subscribe(tick -> {
                    if (tick) {
                        return;
                    }
                    mInitStatusSubj.onNext(InitStatus.MAINBOARD_TIMEOUT);
                }));

        Logger.d("Starting detect boot every 5 seconds...");
        mDisposables.add(mMachine.getConnectionController().request(0x01, 0x21, null, new ResponseStructure<>(new MachineProductInfo()))
                .timeout(5, TimeUnit.SECONDS)
                .retry()
                .takeUntil(response -> mInitStatusSubj.getValue() == InitStatus.MAINBOARD_CONNECTED)
                .as(bindToLifecycle())
                .subscribe(response -> {
                    if (response == null) return;
                    MachineProductInfo dataProp = response.dataProp;
                    String fwVersion = dataProp.getControllerFWVersion();

                    // seriesId is necessary for updating
                    mMachine.getMachineInfoSubjectHolder().getValue().seriesId = dataProp.getBrand();

                    if (!TextUtils.isEmpty(fwVersion) && fwVersion.contains("boot")) {
                        Logger.w("\"boot\" detected, screen will start updating process.");
                        mInitStatusSubj.onNext(InitStatus.MAINBOARD_BOOT_MODE);
                    }
                }));

        Logger.d("Starting observe heartbeat...");
        mDisposables.add(mMachine.getMachineStatusSubjectHolder().getObservable()
                .filter(machineStatus -> machineStatus.connected)
                .take(1)
                .doOnNext(status -> {
                    Logger.d("Serial connected.");
                    mInitStatusSubj.onNext(InitStatus.MAINBOARD_CONNECTED);
                })
                .flatMap(status -> checkModuleVersions())
                .as(bindToLifecycle())
                .subscribe(versionsOK -> {
                    Logger.d("EM version checked, %s.", versionsOK ? "all pass" : "outdated");
                    mInitStatusSubj.onNext(versionsOK ? InitStatus.FINISH : InitStatus.MODULE_FW_OUTDATED);
                }, LogHelper::log));
    }

    protected abstract Observable<Boolean> checkModuleVersions();

    private void observeMemoryUse() {
        Observable.interval(3, TimeUnit.MINUTES).as(bindToLifecycle()).subscribe(t -> {
            ActivityManager activityManager = (ActivityManager) mAppService.getAppContext().getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memInfo);
            Logger.d("Memory Use:\n available: %d, total: %d, lowMemory: %b, threshold %d",
                    memInfo.availMem / (1024 * 1024),
                    memInfo.totalMem / (1024 * 1024),
                    memInfo.lowMemory,
                    memInfo.threshold / (1024 * 1024));
        }, LogHelper::log);
    }

    public Observable<InitStatus> getInitStatusObservable() {
        // once onNext() triggered, all observers will be disposed.
        return mInitStatusSubj.hide().doOnNext(status -> {
            if (status == InitStatus.FINISH || status == InitStatus.MAINBOARD_BOOT_MODE || status == InitStatus.MAINBOARD_TIMEOUT) {
                // After FINISH, no more events are needed.
                mDisposables.clear();
            }
        });
    }

    public InitStatus getInitStatus() {
        return mInitStatusSubj.getValue();
    }

    /**
     * Move cached update files to persist directory.
     */
    private void moveAndCleanCachedUpdateBin() {
        Scheduler.Worker worker = Schedulers.io().createWorker();
        worker.schedule(() -> {
            Logger.d("Trying to start copying update files...");
            File cachedDir = FileHelper.getCachedUpdateFilesDir(mAppService.getAppContext());
            File persistDir = FileHelper.getPersistUpdateFilesDir(mAppService.getAppContext());
            if (!cachedDir.exists()) {
                return;
            }

            // Delete apk and big bin, don't need these big boys anymore.
            File apkFile = new File(cachedDir, "sc.apk");
            File updateBinFile = new File(cachedDir, "update.bin");
            if (!apkFile.delete() || !updateBinFile.delete()) {
                Logger.w("Delete cache update file failed, exiting..");
                mInitStatusSubj.onNext(InitStatus.UPDATE_FINISHED);
                return;
            }

            if (!persistDir.exists() && !persistDir.mkdir()) {
                Logger.w("Could not find or create persistDir, exiting...");
                mInitStatusSubj.onNext(InitStatus.UPDATE_FINISHED);
                return;
            }
            if (!FileHelper.deleteFilesUnder(persistDir)) {
                Logger.w("Could not delete persistDir, exiting...");
                mInitStatusSubj.onNext(InitStatus.UPDATE_FINISHED);
                return;
            }
            if (!FileHelper.moveFiles(cachedDir, persistDir)) {
                Logger.w("Could not move persistDir, exiting...");
                mInitStatusSubj.onNext(InitStatus.UPDATE_FINISHED);
                return;
            }
            mInitStatusSubj.onNext(InitStatus.UPDATE_FINISHED);
            Logger.d("Cached update file(s) moved from \"%1$s\" to \"%2$s\"", cachedDir, persistDir);
        });
    }

    @NonNull
    public File getCachedBigBinFile() {
        return new File(FileHelper.getCachedUpdateFilesDir(mAppService.getAppContext()), "update.bin");
    }

    public void checkFinishUpdating(boolean newApkInstalled) {
        occupySpaceForNextUpdate();
        Logger.d("Ensure space complete.");

        if (newApkInstalled) {
            Logger.d("Finishing update...");
            mInitStatusSubj.onNext(InitStatus.UPDATE_FINISHING);
            moveAndCleanCachedUpdateBin();
        } else {
            Logger.d("No pending update.");
            mInitStatusSubj.onNext(InitStatus.UPDATE_IDLE);
        }
    }

    /**
     * Reserve 200MB space for updates.
     */
    private void occupySpaceForNextUpdate() {
        Logger.d("Ensuring occupied space...");
        IFileManagerService fileManagerService = ServiceContainer.getInstance().getService(IFileManagerService.class);
        IPartition device = fileManagerService.getDevice(true);

        long expectSpace = getOccupiedSpaceInMegaByte() * 1024 * 1024;

        if (device.getFreeSpace() < expectSpace) {
            Logger.w("Not enough free space for next update, start free up files for extra space.");
            try {
                if (expectSpace >= device.getFreeSpace() - 30 * 1024 * 1024) {
                    while (expectSpace >= device.getFreeSpace() - 30 * 1024 * 1024) {
                        ArrayList<IFile> iFiles = device.getRootFile().listFiles();
                        iFiles.sort(Comparator.comparingLong(IFile::lastModified));
                        iFiles.get(0).removeFile();
                    }
                    Logger.d("Free up space done, available " + device.getFreeSpace());
                }
            } catch (IOException e) {
                Logger.e("Clear local file error!");
                LogHelper.log(e);
            }
        }

        File occupiedSpaceFile = new File(FileHelper.getPersistUpdateFilesDir(mAppService.getAppContext()), "occupied");
        if (occupiedSpaceFile.exists()) return;

        Scheduler.Worker worker = Schedulers.io().createWorker();
        worker.schedule(() -> {
            try {
                byte[] x = new byte[10240];
                RandomAccessFile file = new RandomAccessFile(occupiedSpaceFile, "rw");
                while (file.length() != getOccupiedSpaceInMegaByte() * 1024 * 1024) {
                    file.write(x);
                }
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
                //noinspection ResultOfMethodCallIgnored
                occupiedSpaceFile.delete();
            }
        });
    }

    protected abstract long getOccupiedSpaceInMegaByte();

    public void confirmUpdate() {
        mInitStatusSubj.onNext(InitStatus.UPDATE_CONFIRMED);
    }

    public enum InitStatus {
        // updating
        UPDATE_IDLE,
        UPDATE_FINISHING,
        UPDATE_FINISHED,
        UPDATE_CONFIRMED,

        // connecting mainboard
        MAINBOARD_CONNECTED,
        MAINBOARD_BOOT_MODE,
        MAINBOARD_TIMEOUT,

        // checking module FW
        MODULE_FW_OUTDATED,

        // all done
        FINISH
    }
}
