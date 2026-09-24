package fabscreen.features.settings.a400.update;

import java.io.File;

import fabscreen.platform.base.helper.FileHelper;
import fabscreen.platform.base.lib.update.Updater;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class UpdateModulesViewModel extends BaseViewModel {

    private final Updater mUpdater;
    private final BehaviorSubject<Integer> mProgressSubj = BehaviorSubject.createDefault(0);
    private final BehaviorSubject<Boolean> mUpdateResultSubj = BehaviorSubject.create();
    private final IAppService mAppService;

    public UpdateModulesViewModel() {
        mAppService = getServiceContainer().getService(IAppService.class);
        mUpdater = new Updater();

        mUpdater.getProgressObservable()
                .subscribeOn(Schedulers.io())
                .as(bindToLifecycle())
                .subscribe(progress -> mProgressSubj.onNext(progress.progress), LogHelper::log);
    }

    public void update(String emPath) {
        mUpdater.updateEM(emPath)
                .subscribeOn(Schedulers.io())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    mUpdateResultSubj.onNext(success);
                    if (!success) {
                        boolean ignore = new File(FileHelper.getPersistUpdateFilesDir(mAppService.getAppContext()), "em.bin").delete();
                    }
                }, LogHelper::log);
    }

    public Observable<Integer> getProgressObservable() {
        return mProgressSubj.distinctUntilChanged();
    }

    public Observable<Boolean> getUpdateResultObservable() {
        return mUpdateResultSubj.hide();
    }
}
