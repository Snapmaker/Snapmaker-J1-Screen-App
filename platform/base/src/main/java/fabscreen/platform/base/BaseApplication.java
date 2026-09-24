package fabscreen.platform.base;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.akexorcist.localizationactivity.core.LocalizationApplicationDelegate;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;

import fabscreen.platform.base.helper.Md5Util;
import fabscreen.platform.base.instantiation.IServiceContainer;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.crash.CrashCollectHandler;
import fabscreen.platform.base.lib.network.NetworkController;
import fabscreen.platform.base.service.BaseAppService;
import fabscreen.platform.base.service.BaseRouter;
import fabscreen.platform.base.service.FileManagerService;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IFileManagerService;
import fabscreen.platform.base.service.ILanguage;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.INetwork;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRemote;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.MultiLanguageManager;
import fabscreen.platform.base.service.Preferences;
import fabscreen.platform.base.service.machine.BaseMachine;
import fabscreen.platform.base.service.remote.S30RemoteService;
import fabscreen.platform.lib.LogHelper;

public abstract class BaseApplication extends Application {
    protected IServiceContainer mServiceContainer;
    private final LocalizationApplicationDelegate mLocalizationDelegate = new LocalizationApplicationDelegate();


//    private ViewSub mViewSub;
//    private HTTPServer mHTTPServer;
//    private DiscoverServer mDiscoverServer;
//    private FabScreenActivityManagement mFabScreenActivityManagement;


    public static String formatTime(double time) {
        int hour = (int) (time) / 3600;
        int minute = ((int) (time) % 3600) / 60;
        int second = ((int) (time) % 60);

        if (hour < 1) {
            return getInstance().getString(fabscreen.platform.base.R.string.date_helper_format_time_minute_second, minute, second);
        } else {
            return getInstance().getString(fabscreen.platform.base.R.string.date_helper_format_time_hour_minute, hour, minute);
        }
    }

    // all invoke should be removed
    @Deprecated
    public static BaseApplication getInstance() {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        LogHelper.configureLogger(this);
        Logger.d("app onCreate triggered......");
        initServiceContainer();
        initAppService();
        initBaseServices();
        CrashCollectHandler.getInstance().init(this);
        mServiceContainer.getService(IAppService.class).onAppInitFinished();
        moveAssets();
    }

    public void moveAssets() {
        AssetManager assets = getAssets();
        try {
            String[] assets1 = assets.list("video");
            if (assets1 == null || assets1.length == 0) {
                return;
            }
            byte[] mBytes = new byte[1024];
            File videDir = mServiceContainer.getService(IAppService.class).getVideDir();
            for (String fileName : assets1) {
                InputStream open = assets.open("video/" + fileName);
                File file = new File(videDir, fileName);
                if (!file.exists()) {
                    file.createNewFile();
                } else {
                    String s1 = Md5Util.fileToMD5(file.getAbsolutePath());
                    String s2 = Md5Util.inputStreamToMD5(assets.open("video/" + fileName));
                    if (s2 != null && s2.equals(s1)) {
                        Logger.d("Animation files checksum s1:%s s2:%s", s1, s2);
                        continue;
                    }
                }
                FileOutputStream fos = new FileOutputStream(file);
                int bt = 0;
                while ((bt = open.read(mBytes)) != -1) {
                    fos.write(mBytes, 0, bt);
                }
                fos.flush();
                open.close();
                fos.close();
            }
        } catch (Exception e) {
            LogHelper.log(e);
        }
//        assets.close();
    }

    public void initServiceContainer() {
        mServiceContainer = ServiceContainer.getInstance();
    }

    public void initAppService() {
        mServiceContainer.registerService(IAppService.class, BaseAppService.class);
        mServiceContainer.getService(IAppService.class).onAppCreate(this);
    }

    public void initBaseServices() {
        mServiceContainer.registerService(IPreferences.class, Preferences.class);
        mServiceContainer.registerService(ILanguage.class, MultiLanguageManager.class);
        mServiceContainer.registerService(IFileManagerService.class, FileManagerService.class);
        mServiceContainer.registerService(INetwork.class, NetworkController.class);
        mServiceContainer.registerService(IRouter.class, BaseRouter.class);
        mServiceContainer.registerService(IMachine.class, BaseMachine.class);
        mServiceContainer.registerService(IRemote.class, S30RemoteService.class);
    }


    public abstract String getAppVersionName();

    public abstract String getBuildTime();

    public abstract String getSerialPortPath();

    @Override
    protected void attachBaseContext(Context base) {
        mLocalizationDelegate.setDefaultLanguage(base, Locale.ENGLISH);
        super.attachBaseContext(mLocalizationDelegate.attachBaseContext(base));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mLocalizationDelegate.onConfigurationChanged(this);
    }

    @Override
    public Context getApplicationContext() {
        return mLocalizationDelegate.getApplicationContext(super.getApplicationContext());
    }

    @Override
    public Resources getResources() {
        return mLocalizationDelegate.getResources(getBaseContext(), super.getResources());
    }
}
