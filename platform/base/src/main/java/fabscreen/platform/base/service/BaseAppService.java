package fabscreen.platform.base.service;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.media.SoundPool;
import android.os.Environment;
import android.util.SparseIntArray;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.BaseApplication;
import fabscreen.platform.base.R;
import fabscreen.platform.base.instantiation.IServiceIdentifier;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.ISlaveComputer;
import fabscreen.platform.base.legacy.Workspace;
import fabscreen.platform.base.legacy.print.IPrintController;
import fabscreen.platform.base.legacy.remote.RemoteController;
import fabscreen.platform.base.legacy.version.VersionRequirementManager;
import fabscreen.platform.base.lib.network.NetworkController;
import fabscreen.platform.base.model.HTTPEventBus;
import fabscreen.platform.base.receiver.InstallProcessReceiver;
import fabscreen.platform.base.view.BaseActivity;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;

public class BaseAppService implements IAppService, IServiceIdentifier {
    private BaseApplication mApp;
    private NetworkController mNetworkController;
    private BaseActivity mCurrentActivity;
    private RemoteController mRemoteController;
    private Workspace mWorkspace;

    private List<Context> mViewContextList = new ArrayList<>();
    private CompositeDisposable mDisposable = new CompositeDisposable();
    private AlertDialog alertDialog;
    private BehaviorSubject<Boolean> propBehaviorSubject = BehaviorSubject.create();
    private String mData;
    private SparseIntArray mSoundIds = new SparseIntArray();
    private SoundPool mSoundPool;

    @Override
    public void onAppCreate(BaseApplication application) {
        mApp = application;
    }

    @Override
    public void onAppInitFinished() {
        watchMachineError();
        initSounds();
        checkUpdatingAppInstalled(getApp());
    }

    @Override
    public Context getAppContext() {
        return mApp.getApplicationContext();
    }

    @Override
    public Context getNowViewContext() {
        if (mViewContextList.isEmpty()) return null;
        return mViewContextList.get(mViewContextList.size() - 1);
    }

    @Override
    public void enterContext(Context context) {
        mViewContextList.add(context);
    }

    @Override
    public void leaveContext(Context context) {
        mViewContextList.remove(context);
    }


    // FIXME:
    @Override
    public void DataPipe(String data, boolean isShow) {
        mData = data;
        propBehaviorSubject.onNext(isShow);
    }

    @Override
    public void restart() {
        Logger.d("App restarting...");
        mDisposable.add(Observable.timer(1000, TimeUnit.MILLISECONDS)
                .subscribe(aLong -> {
                    for (int i = 0; i < mViewContextList.size(); i++) {
                        if (mViewContextList.get(mViewContextList.size() - 1 - i) != null) {
                            ((Activity) mViewContextList.get(mViewContextList.size() - 1 - i)).finish();
                        }
                    }
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }));
    }

    @Override
    public BaseApplication getApp() {
        return mApp;
    }

    @Override
    public BaseActivity getCurrentActivity() {
        return mCurrentActivity;
    }

    // slaveComputer is null now!
    @Override
    public ISlaveComputer getSlaveComputer() {
        return null;
    }


    // Use Service container api instead
    @Deprecated
    public IPreferences getPreferences() {
        return ServiceContainer.getInstance().getService(IPreferences.class);
    }

    @Deprecated
    public HTTPEventBus getHTTPEventBus() {
        return HTTPEventBus.getInstance();
    }

    public NetworkController getNetworkController() {
        return mNetworkController;
    }

    public void setNetworkController(NetworkController nc) {
        mNetworkController = nc;
    }

    @Deprecated
    @Override
    public IPrintController getPrintController() {
        return null;
    }


    public RemoteController getRemoteController() {
        return mRemoteController;
    }

    public void setRemoteController(RemoteController rc) {
        mRemoteController = rc;
    }

    public Workspace getWorkspace() {
        return mWorkspace;
    }

    public void setWorkspace(Workspace ws) {
        mWorkspace = ws;
    }

    public MultiLanguageManager getMultiLanguageManager() {
        return null;
    }

    public VersionRequirementManager getVersionRequirementManager() {
        return null;
    }

    @Override
    public File getCacheDir() {
        return mApp.getCacheDir();
    }

    @Override
    public File getFilesDir() {
        return mApp.getFilesDir();
    }

    @Override
    public File getDataDir() {
        return mApp.getDataDir();
    }

    private void initSounds() {
        mSoundPool = new SoundPool.Builder().setMaxStreams(5).build();
        mSoundIds = new SparseIntArray();
        mSoundIds.put(R.raw.sound_click, mSoundPool.load(getAppContext(), R.raw.sound_click, 1));
        mSoundIds.put(R.raw.sound_emergency_stop, mSoundPool.load(getAppContext(), R.raw.sound_emergency_stop, 1));
        mSoundIds.put(R.raw.sound_procedure_complete, mSoundPool.load(getAppContext(), R.raw.sound_procedure_complete, 1));
        mSoundIds.put(R.raw.sound_work_complete, mSoundPool.load(getAppContext(), R.raw.sound_work_complete, 1));
        mSoundIds.put(R.raw.sound_toast_show, mSoundPool.load(getAppContext(), R.raw.sound_toast_show, 1));
        mSoundIds.put(R.raw.sound_rotate_button_se_move, mSoundPool.load(getAppContext(), R.raw.sound_rotate_button_se_move, 1));
        mSoundIds.put(R.raw.sound_switch, mSoundPool.load(getAppContext(), R.raw.sound_switch, 1));
        mSoundIds.put(R.raw.sound_dialog_tip, mSoundPool.load(getAppContext(), R.raw.sound_dialog_tip, 1));
        mSoundIds.put(R.raw.sound_dialog_warming, mSoundPool.load(getAppContext(), R.raw.sound_dialog_warming, 1));
        mSoundIds.put(R.raw.sound_dialog_error, mSoundPool.load(getAppContext(), R.raw.sound_dialog_error, 1));
    }

    private void checkUpdatingAppInstalled(Application application) {
        // Check if com.snapmaker.update is installed
        Intent updateIntent = new Intent(application.getApplicationContext(), InstallProcessReceiver.class);
        updateIntent.putExtra("OPERATION", "update");
        application.getApplicationContext().sendBroadcast(updateIntent);
    }

    public void setCurrentActivity(BaseActivity activity) {
        mCurrentActivity = activity;
    }

    public int getSoundIdByResourceId(int id) {
        return mSoundIds.get(id);
    }

    public SoundPool getSoundPool() {
        return mSoundPool;
    }

    private void watchMachineError() {
        mDisposable.add(propBehaviorSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isShow -> {
                            if (isShow) {
                                if (alertDialog != null) {
                                    alertDialog.setMessage(mData);
                                    alertDialog.show();
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getNowViewContext());
                                    builder.setTitle("机器异常");
                                    builder.setMessage(String.format(mData));
                                    builder.setPositiveButton("取消", (dialog, which) -> dialog.dismiss());
                                    alertDialog = builder.create();
                                    alertDialog.show();
                                }
                            } else {
                                if (alertDialog != null) {
                                    alertDialog.dismiss();
                                }
                            }
                        }
                ));
    }

    @Override
    public File getVideDir() {
        File externalCacheDir = mApp.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if (externalCacheDir == null) {
            Logger.w("Video externalCacheDir is null");
            return null;
        }
        String folderPath = externalCacheDir.getAbsolutePath() + "/video";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }
}
