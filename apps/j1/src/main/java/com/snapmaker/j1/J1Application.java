package com.snapmaker.j1;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.BaseApplication;
import fabscreen.platform.base.lib.network.NetworkController;
import fabscreen.platform.base.lib.parser.GcodeParser;
import fabscreen.platform.base.lib.parser.IGcodeParser;
import fabscreen.platform.base.lib.parser.J1GcodeParser;
import fabscreen.platform.base.lib.print.BasePrintWorkspace;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.service.FileManagerService;
import fabscreen.platform.base.service.HttpDownloadManager;
import fabscreen.platform.base.service.IFileManagerService;
import fabscreen.platform.base.service.IHttpDownloadManager;
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
import fabscreen.platform.core.router.J1Router;

public class J1Application extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d("J1 Start! \nBuild: %s Version %s %s", BuildConfig.BUILD_TYPE, getAppVersionName(), BuildConfig.BUILD_RELEASE_DATE);
    }

    @Override
    public String getAppVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public String getBuildTime() {
        return BuildConfig.BUILD_RELEASE_DATE;
    }

    @Override
    public String getSerialPortPath() {
        return "/dev/ttyHSL1";
    }

    // we can init special Service in different apps

    @Override
    public void initServiceContainer() {
        super.initServiceContainer();
    }

    @Override
    public void initAppService() {
        super.initAppService();
    }

    @Override
    public void initBaseServices() {
        mServiceContainer.registerService(IPreferences.class, Preferences.class);
        mServiceContainer.registerService(ILanguage.class, MultiLanguageManager.class);
        mServiceContainer.registerService(IFileManagerService.class, FileManagerService.class);
        mServiceContainer.registerService(INetwork.class, NetworkController.class);
        mServiceContainer.registerService(IRouter.class, J1Router.class);
        mServiceContainer.registerService(IMachine.class, BaseMachine.class);
        mServiceContainer.registerService(IRemote.class, S30RemoteService.class);
        mServiceContainer.registerService(IPrintWorkspace.class, BasePrintWorkspace.class);
        mServiceContainer.registerService(IGcodeParser.class, J1GcodeParser.class);
        mServiceContainer.registerService(IHttpDownloadManager.class, HttpDownloadManager.class);
    }

}
