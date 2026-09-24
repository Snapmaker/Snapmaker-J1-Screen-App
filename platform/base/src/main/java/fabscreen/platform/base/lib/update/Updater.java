package fabscreen.platform.base.lib.update;

import static fabscreen.platform.base.lib.update.UpdateFileParser.UpdateFile.BT;
import static fabscreen.platform.base.lib.update.UpdateFileParser.UpdateFile.EM;
import static fabscreen.platform.base.lib.update.UpdateFileParser.UpdateFile.MC;
import static fabscreen.platform.base.lib.update.UpdateFileParser.UpdateFile.SC;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.helper.FileHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.IPartition;
import fabscreen.platform.base.lib.update.UpdateFileParser.UpdateFile.FileType;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IFileManagerService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.controller.UpdateController;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.lib.SubjectHolder;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.SingleSubject;

public class Updater {
    private static final String TAG = "Updater";
    private final UpdateFileParser mParser;
    private final IAppService mAppService;
    private final UpdateController mUpdateController;
    private final BehaviorSubject<Progress> mProgressSubject = BehaviorSubject.create();
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private final IMachine mMachine;
    private final IPreferences.Helper mHelper;
    private Map<Integer, String> mUpdateFiles;

    public static class Progress {
        //-1 copy bin file, -2 extract mc, -3 extract em, -4 extract bt, -5 extract sc
        // 0 update mc, 1 update em, 2 update bt
        public int type;
        // 0~100
        public int progress;

        public Progress() {
        }

        public Progress(int type, int progress) {
            this.type = type;
            this.progress = progress;
        }
    }

    public Updater() {
        mMachine = ServiceContainer.getInstance().getService(IMachine.class);
        mAppService = ServiceContainer.getInstance().getService(IAppService.class);
        mParser = UpdateFileParser.getInstance();
        mHelper = ServiceContainer.getInstance().getService(IPreferences.class).getHelper();
        mUpdateController = mMachine.getUpdateController();
        mDisposable.add(mParser.getParseProgressObservable().subscribe(mProgressSubject::onNext, mProgressSubject::onError));
        mDisposable.add(mUpdateController.getProgressObservable().subscribe(mProgressSubject::onNext, mProgressSubject::onError));
    }

    /**
     * update machine -> update screen
     *
     * @param bigBinPath File path of "the big bin".
     * @param isLocal    Whether the file is in Screen device or in a remote USB device.
     * @return The path of the screen Update File. We always update screen at last.
     */
    public Observable<String> update(String bigBinPath, boolean isLocal) {
        mProgressSubject.onNext(new Progress(-1, 0));
        return saveOldVersionInfo()
                .flatMap(saved -> freeUpOccupiedSpace())
                .flatMap(saved -> mParser.parseFile(bigBinPath, isLocal))
                .doOnNext(files -> mUpdateFiles = files)
                .flatMap(this::updateMachine)
                .flatMap(success -> mMachine.getMachineController().restartMachine())
                .flatMap(response -> response.isSuccess() ? mMachine.onRestart() : Observable.just(false))
                .flatMap(rebooted -> checkModuleListUntilGot())
                .flatMap(success -> {
                    mHelper.setLastUpdatePackageVersion(UpdateFileParser.getBigBinVersion(mAppService.getAppContext()));
                    String scPath = mUpdateFiles.get(SC);
                    if (scPath == null) {
                        throw new IllegalArgumentException("No screen update file");
                    } else {
                        return Observable.just(scPath);
                    }
                });
    }

    private Observable<Boolean> freeUpOccupiedSpace() {
        return Observable.fromCallable(() -> new File(FileHelper.getPersistUpdateFilesDir(mAppService.getAppContext()), "occupied").delete())
                .doOnNext(result -> {
                    IFileManagerService fileManagerService = ServiceContainer.getInstance().getService(IFileManagerService.class);
                    IPartition device = fileManagerService.getDevice(true);
                    Logger.d("Free up space " + result
                            + ", available " + device.getFreeSpace()
                            + " / total " + device.getTotalSpace());
                });
    }

    public Observable<Boolean> updateEM(String emPath) {
        return saveOldVersionInfo()
                .flatMap(saved -> doUpdate(EM, emPath))
                .retry(3)
                .flatMap(success -> mMachine.getMachineController().restartMachine())
                .flatMap(response -> response.isSuccess() ? mMachine.onRestart() : Observable.just(false))
                .filter(rebooted -> rebooted)
                .flatMap(rebooted -> checkModuleListUntilGot());
    }

    private Observable<Boolean> checkModuleListUntilGot() {
        Logger.d("Checking module list...");
        SubjectHolder<MachineInfo> holder = mMachine.getMachineInfoSubjectHolder();
        return holder.getObservable()
                .takeUntil(info -> info.moduleList != null)
                .filter(info -> info.moduleList != null)
                .map(Objects::nonNull);
    }

    private Observable<Boolean> saveOldVersionInfo() {
        return Observable.create(emitter -> {
            MachineInfo machineInfo = mMachine.getMachineInfoSubjectHolder().getValue();
            MachineInfoLite machineVersion = new MachineInfoLite();
            machineVersion.controllerFWVersion = machineInfo.controllerFWVersion;
            machineVersion.moduleVersionList = new ArrayList<>();
            if (machineInfo.moduleList == null || machineInfo.moduleList.isEmpty()) {
                emitter.onNext(true);
                emitter.onComplete();
                return;
            }
            for (Module module : machineInfo.moduleList) {
                Module.ModuleInfo moduleInfo = module.getModuleInfo();
                machineVersion.moduleVersionList.add(new MachineInfoLite.ModuleInfoLite(moduleInfo.getModuleId(), moduleInfo.getFirmwareVersion(), module.getDisplayName()));
            }

            FileHelper.saveJSONToFile(new Gson().toJson(machineVersion), FileHelper.getMachineVersionFile(mAppService.getAppContext()));
            emitter.onNext(true);
            emitter.onComplete();
        });
    }

    /**
     * Update machine.
     * 1. update controller;
     * 2. update modules.
     *
     * @return any value represents success, onError if fail.
     */
    private Observable<Boolean> updateMachine(Map<Integer, String> files) {
        return doUpdate(MC, files.get(MC))
                // Wait for mc reboot. Need to check the module status to start next updating.
                .flatMap(success -> doUpdate(BT, files.get(BT)))
                .retry(3)
                .flatMap(success -> doUpdate(EM, files.get(EM)))
                .retry(3);
    }

    private Observable<Boolean> doUpdate(@FileType int type, String path) {
        SingleSubject<Boolean> subject = SingleSubject.create();
        int waitTime = 0;
        if (type == MC) {
            waitTime = 20;
        } else if (type == BT) {
            waitTime = 10;
        }

        if (path != null) {
            startUpdate(subject, type, path, waitTime);
        } else {
            Logger.d("Dev concern, no update file, inner type is %d", type);
            subject.onSuccess(false);
        }
        return subject.toObservable();
    }

    private void startUpdate(SingleSubject<Boolean> subject, @FileType int type, String path, int waitSeconds) {
        mProgressSubject.onNext(new Progress(type, 0));
        mDisposable.add(mUpdateController.startUpdate(new UpdateFileParser.UpdateFile(type, path))
                .doOnSubscribe(disposable -> Logger.d("updater start update, type is %1$s, path is %2$s", type, path))
                .subscribe(result -> {
                    if (result == 0 || result == 10) {
                        // As for em.bin:
                        // result == 0, every module has been updated successfully.
                        // result == 10, module initiation fail, but don't break updating process.
                        if (result == 10) {
                            Logger.d("No module detected when update \"%s\", skipping...", path);
                        }

                        mDisposable.add(Observable.timer(waitSeconds, TimeUnit.SECONDS).subscribe(tick -> {
                            subject.onSuccess(true);
                            mProgressSubject.onNext(new Progress(type, 100));
                        }));
                    } else {
                        subject.onError(new IllegalStateException("Update fail, path " + path));
                    }
                }, mProgressSubject::onError));
    }

    public Observable<Progress> getProgressObservable() {
        return mProgressSubject.hide();
    }

    public void clear() {
        mDisposable.clear();
    }
}
