package fabscreen.features.settings.a350.firmware.update;

import android.content.Context;
import android.content.Intent;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import fabscreen.features.settings.R;
import fabscreen.platform.base.FabException;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.FabFileInputStream;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.file.IPartition;
import fabscreen.platform.base.lib.update.FabUpdatePackage;
import fabscreen.platform.base.lib.update.FabUpdater;
import fabscreen.platform.base.model.system.DeprecatedMachineInfo;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

// TODO: 2021/8/24  viewModel must not acquire context.
public class UpdateViewModel extends BaseViewModel {
    private static final String TAG = "UpdateViewModel";

    private CompositeDisposable disposables = new CompositeDisposable();

    private Context mContext;
    private FabUpdater mUpdater;

    private String mControllerVersion;
    private int mPackageIndex = -1;

    private IFile mFirmwareFile;

    private PublishSubject<Boolean> mUpdateCompleteSubject = PublishSubject.create();
    private BehaviorSubject<String> mMessageSubject = BehaviorSubject.createDefault("");
    private BehaviorSubject<Float> mProgressSubject = BehaviorSubject.createDefault(0f);
    private BehaviorSubject<String> mProgressTextSubject = BehaviorSubject.createDefault("");

    private Disposable mUpdateCheckSubscription;

    UpdateViewModel(Context context) {
        super();
        mContext = context;
//
//        mUpdater = new FabUpdater();
//
//        initEvents();
    }

    boolean parse(IFile file) {
        try {
            return mUpdater.parse(file);
        } catch (Exception e) {
            LogHelper.log(e);
            return false;
        }
    }

    public Observable<IFile> startSearchingFile(IPartition fileManager, String filePath) {
        mMessageSubject.onNext(mContext.getResources().getString(R.string.settings_update_loading_update_file));
        try {
            IFile search = fileManager.search(filePath);
            mFirmwareFile = search;
            return Observable.just(search);
        } catch (Exception e) {
            return Observable.error(e);
        }
    }

    Observable<Boolean> update() {
        updateApp();

        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setHeartbeatEnabled(false);

        Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getObservable()
                .subscribe(machineInfo -> {
                    mControllerVersion = machineInfo.controllerFWVersion;

                    updatePackage(0);
                });
        disposables.add(sub);

        return mUpdateCompleteSubject;
    }

    private void initEvents() {

        Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().watchPacketIndexRequest()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::sendPackagePart);
        disposables.add(sub);
    }

    private void updatePackage(int pid) {
        mPackageIndex = pid;

        if (pid == mUpdater.getPacketCount()) {
            // set update flag
            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMachineUpdatedFlag(true);
            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLastUpdatePackageVersion(mUpdater.getPackageVersion());

            if (mUpdater.getScreenUpdate() != null) {
                updateApp();
            } else {
                mUpdateCompleteSubject.onNext(true);
            }
        } else {
            byte[] p = mUpdater.usePacket(pid);

            if (p[0] == FabUpdatePackage.UpdatePackageHeader.TYPE_CONTROLLER_FIRMWARE) {
                mMessageSubject.onNext(mContext.getString(R.string.settings_update_msg_updating_controller));
            } else if (p[0] == FabUpdatePackage.UpdatePackageHeader.TYPE_MODULE_FIRMWARE) {
                mMessageSubject.onNext(mContext.getString(R.string.settings_update_msg_updating_modules));
            }

            // Update Controller if update version is not equals with current version.
            if (p[0] == FabUpdatePackage.UpdatePackageHeader.TYPE_CONTROLLER_FIRMWARE
                    && mUpdater.getControllerVersion().equals(mControllerVersion)
                    && !mUpdater.isForceUpdate()) {
                // skip the package
                updatePackage(pid + 1);
            } else {
                // Start sending update packet to Controller.
                startUpdatePackage();
            }
        }
    }

    private File saveToFile(byte[] packet) {
        // TODO: refactor File into IFile
        FileOutputStream fos;
        File file;
        try {
            file = new File(ServiceContainer.getInstance().getService(IAppService.class).getCacheDir(), "update.apk");
            fos = new FileOutputStream(file);
            fos.write(packet, 0, packet.length);

            fos.flush();
            fos.close();
        } catch (IOException e) {
            LogHelper.log(e);
            file = null;
        }
        return file;
    }

    private File copyToCacheFile(File updateFile) {
        try {
            File file = new File(ServiceContainer.getInstance().getService(IAppService.class).getCacheDir(), "update.apk");
            FileInputStream inputStream = new FileInputStream(updateFile);
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateApp() {
        mMessageSubject.onNext(mContext.getString(R.string.settings_update_msg_updating_screen));
        File file = saveToFile(mUpdater.getScreenUpdate());
        sendInstallBroadcast(file);
    }

    public void updateAppByApkFile(File file) {
        if (!file.exists()) {
            return;
        }
        Scheduler.Worker worker = Schedulers.io().createWorker();
        worker.schedule(() -> {
            File cachedUpdateFile = copyToCacheFile(file);
            sendInstallBroadcast(cachedUpdateFile);
        });
    }

    private void sendInstallBroadcast(File file) {
        if (file != null) {
            // Send broadcast to update FabScreen itself.
            Intent updateIntent = new Intent("com.snapmaker.updateApkBroadcast");
            updateIntent.putExtra("URL", file.getPath());
            updateIntent.putExtra("OPERATION", "local_file");
            mContext.sendBroadcast(updateIntent);
        }
    }

    private void startUpdatePackage() {
        mProgressTextSubject.onNext("");
        Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().startUpdate()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    if (success) {
                        // do nothing wait for request
                    } else {
                        mUpdateCompleteSubject.onNext(false);
                    }
                }, e -> {
                    LogHelper.log(e);
                    mUpdateCompleteSubject.onNext(false);
                });
        disposables.add(sub);
    }

    private void sendPackagePart(int index) {
        if (index == mUpdater.getPartCount()) {
            ServiceContainer.getInstance().getService(IMachine.class).getMachineController().sendUpdatePackage((byte) 0x02, (short) index, null);
            mProgressSubject.onNext(1f);
            checkPackageUpdated();
        } else {
            final byte[] content = mUpdater.getPart(index);
            ServiceContainer.getInstance().getService(IMachine.class).getMachineController().sendUpdatePackage((byte) 0x01, (short) index, content);
            mProgressSubject.onNext(1f * (index + 1) / mUpdater.getPartCount());
            mProgressTextSubject.onNext(String.format(Locale.US, "%d / %d", index + 1, mUpdater.getPartCount()));
        }
    }

    private void checkPackageUpdated() {
        mMessageSubject.onNext(mContext.getString(R.string.settings_update_msg_finishing_up_controller_update));
        // check if controller is available
        mUpdateCheckSubscription = Observable.interval(5, TimeUnit.SECONDS)
                .delaySubscription(10, TimeUnit.SECONDS)
                .flatMap(t ->
                        ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue()
                                .requestMachineStatus()
                                .onErrorReturnItem(DeprecatedMachineInfo.getDefaultInstance())
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> {
                    if (!status.isDefault) {
                        mUpdateCheckSubscription.dispose();
                        mUpdateCheckSubscription = null;

                        updatePackage(mPackageIndex + 1);
                    }
                }, Throwable::printStackTrace);
//        disposables.add(mUpdateCheckSubscription);
    }

    public IFile getUpdateFile() {
        return mFirmwareFile;
    }

    public void setUpdateFile(IFile file) {
        mFirmwareFile = file;
    }

    public void saveUpdateFile(String folderPath, IFile file) {
        Logger.d("Start saving update file into cache…");
        File saveFolder = new File(folderPath);
        if (!saveFolder.exists()) {
            saveFolder.mkdir();
        }

        // clear last update files in folder
        File[] files = saveFolder.listFiles();
        if (files != null && files.length > 0) {
            for (File f : files) {
                f.delete();
            }
        }

        File saveFile = new File(saveFolder, "update.bin");
        BufferedSource bufferedSource = null;
        BufferedSink bufferedSink = null;
        FabFileInputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
            bufferedSource = Okio.buffer(Okio.source(inputStream.getInputStream()));
            bufferedSink = Okio.buffer(Okio.sink(new FileOutputStream(saveFile)));
            // copy file from source with buffer
            int len;
            byte[] buffer = new byte[1024 * 16];
            while ((len = bufferedSource.read(buffer)) != -1) {
                bufferedSink.write(buffer, 0, len);
            }
            bufferedSink.close();
            bufferedSource.close();
        } catch (IOException e) {
            LogHelper.log(e);
        } finally {
            try {
//                iPartition.deviceHang();
                if (bufferedSink != null) {
                    bufferedSink.close();
                }
                if (bufferedSource != null) {
                    bufferedSource.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                LogHelper.log(e);
            }
        }
    }

    Observable<String> getUpdateMessageObservable() {
        return mMessageSubject;
    }

    Observable<String> getUpdateProgressTextObservable() {
        return mProgressTextSubject;
    }

    Observable<Float> getUpdateProgressObservable() {
        return mProgressSubject;
    }

    public void dispose() {
        if (mUpdateCheckSubscription != null && !mUpdateCheckSubscription.isDisposed()) {
            mUpdateCheckSubscription.dispose();
        }
        disposables.clear();
    }
}
