package fabscreen.platform.base.service;

import android.content.Context;
import android.media.SoundPool;

import java.io.File;

import fabscreen.platform.base.BaseApplication;
import fabscreen.platform.base.legacy.ISlaveComputer;
import fabscreen.platform.base.legacy.Workspace;
import fabscreen.platform.base.legacy.print.IPrintController;
import fabscreen.platform.base.legacy.remote.RemoteController;
import fabscreen.platform.base.legacy.version.VersionRequirementManager;
import fabscreen.platform.base.model.HTTPEventBus;
import fabscreen.platform.base.view.BaseActivity;

public interface IAppService {
    void onAppCreate(BaseApplication application);

    void onAppInitFinished();

    Context getAppContext();

    BaseApplication getApp();

    BaseActivity getCurrentActivity();

    // Use Service container api instead
    @Deprecated
    IPreferences getPreferences();

    @Deprecated
    ISlaveComputer getSlaveComputer();

    HTTPEventBus getHTTPEventBus();

    //    public ILaserCameraController getLaserCameraController();
//
//    public SSTPMachineController getMachineController();
    @Deprecated
    IPrintController getPrintController();

    @Deprecated
    RemoteController getRemoteController();

    @Deprecated
    Workspace getWorkspace();

    // use service container
    @Deprecated
    MultiLanguageManager getMultiLanguageManager();

    VersionRequirementManager getVersionRequirementManager();


    File getCacheDir();

    File getFilesDir();

    File getDataDir();

    File getVideDir();

    Context getNowViewContext();

    void enterContext(Context context);

    void leaveContext(Context context);

    void restart();

    void DataPipe(String data, boolean isShow);

    int getSoundIdByResourceId(int id);

    SoundPool getSoundPool();
}
