package com.snapmaker.j1.modules.home;

import androidx.annotation.RawRes;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.VersionChangeLog;
import fabscreen.platform.base.lib.VersionResponse;
import fabscreen.platform.base.lib.api.ApiClient;
import fabscreen.platform.base.lib.api.ApiObserver;
import fabscreen.platform.base.lib.update.UpdateFileParser;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.view.UpdateDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class HomeViewModel extends BaseViewModel {
    private final IPreferences.Helper mPrefHelper;
    private IAppService mAppService;
    private final BehaviorSubject<Boolean> mUpdateSubj = BehaviorSubject.create();
    private BehaviorSubject<VersionInfo> mVersionInfoSubj = BehaviorSubject.create();

    public HomeViewModel() {
        mPrefHelper = ServiceContainer.getInstance().getService(IPreferences.class).getHelper();
        mAppService = ServiceContainer.getInstance().getService(IAppService.class);
        getLatestVersion();
    }

    private void getLatestVersion() {
        ApiClient client = new ApiClient(mPrefHelper.getApiHost());
        // FIXME: 2022/5/17 wifi may not connected?
        client.getLatestVersion()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(new ApiObserver<VersionResponse>() {
                    @Override
                    protected void onSuccess(VersionResponse response) {
                        if (response.code != 0) {
                            Logger.w("API server response not ok: %s", response.code);
                            return;
                        }
                        UpdateFileParser.cacheVersionInfoToDisk(response.data.new_version);
                        String newVersion = response.data.new_version.version;
                        Logger.d("new version is %s", newVersion);
                        if (!newVersion.equals(mPrefHelper.getLastUpdatePackageVersion())) {
                            processNewVersionInfo(response.data.new_version);
                            Logger.d("New firmware available " + response.data.new_version.version);
                            mUpdateSubj.onNext(true);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                });
    }

    public Observable<Boolean> getUpdateObservable() {
        return mUpdateSubj.hide();
    }

    private void processNewVersionInfo(VersionResponse.NewVersionData newVersion) {
        VersionInfo versionInfo = new VersionInfo();
        versionInfo.name = newVersion.version.split("_")[1];
        versionInfo.fileSize = (int) (newVersion.package_size / 1024f / 1024f);
        versionInfo.releaseTime = "01-27-2022(MOCK)";
        versionInfo.changelogs = getMergedChangelogs(newVersion.change_log);
        mVersionInfoSubj.onNext(versionInfo);
    }

    private List<UpdateDialog.ChangelogItem> getMergedChangelogs(VersionChangeLog changeLog) {

        List<String> features = changeLog.getFeatures();
        List<String> improvements = changeLog.getImprovement();
        List<String> bugFixes = changeLog.getBugFixes();

        List<UpdateDialog.ChangelogItem> changeLogList = new ArrayList<>();
        changeLogList.add(new UpdateDialog.ChangelogItem("Features", UpdateDialog.ChangelogItem.ChangelogType.TITLE));
        for (String feature : features) {
            changeLogList.add(new UpdateDialog.ChangelogItem(feature, UpdateDialog.ChangelogItem.ChangelogType.DESC));
        }
        changeLogList.add(new UpdateDialog.ChangelogItem("Improvements", UpdateDialog.ChangelogItem.ChangelogType.TITLE));
        for (String improvement : improvements) {
            changeLogList.add(new UpdateDialog.ChangelogItem(improvement, UpdateDialog.ChangelogItem.ChangelogType.DESC));
        }
        changeLogList.add(new UpdateDialog.ChangelogItem("Bug Fixes", UpdateDialog.ChangelogItem.ChangelogType.TITLE));
        for (String bugFix : bugFixes) {
            changeLogList.add(new UpdateDialog.ChangelogItem(bugFix, UpdateDialog.ChangelogItem.ChangelogType.DESC));
        }

        return changeLogList;
    }


    public static class VersionInfo {
        public String name;
        // Assume bin file always larger than 1M.
        public int fileSize;
        public String releaseTime;
        public List<UpdateDialog.ChangelogItem> changelogs;
    }

    public VersionInfo getNewVersionInfo() {
        return mVersionInfoSubj.getValue();
    }

    private void copyInternalPrintFile(String fileName, @RawRes int resId) {
        InputStream is = mAppService.getAppContext().getResources().openRawResource(resId);
        File file = null;
        try {
            file = new File(mAppService.getAppContext().getFilesDir().getAbsoluteFile() + "/" + fileName);
            if (file.exists() && file.length() != 0) {
                Logger.d("%s checked.", fileName);
                // Stop copy, return.
                return;
            }
            Logger.d("Start preparing %s...", fileName);
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                int read;
                byte[] bytes = new byte[1024];
                while ((read = is.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            }
        } catch (Exception e) {
            file = null;
            LogHelper.log(e);
        } finally {
            try {
                is.close();
            } catch (Exception ignored) {

            }
        }
    }

    public void copyInternalFiles() {
        Logger.d("Start checking internal print files...");

        copyInternalPrintFile(mAppService.getAppContext().getString(fabscreen.platform.base.R.string.j1_internal_print_file_test_3dbenchy), fabscreen.platform.base.R.raw.test_3dbenchy);
        copyInternalPrintFile(mAppService.getAppContext().getString(fabscreen.platform.base.R.string.j1_internal_print_file_xy_belt_calibrator), fabscreen.platform.base.R.raw.xy_belt_calibrator);
        copyInternalPrintFile(mAppService.getAppContext().getString(fabscreen.platform.base.R.string.j1_internal_print_file_two_color_shark_by_mcgybeer), fabscreen.platform.base.R.raw.two_color_shark_by_mcgybeer);
    }

}
