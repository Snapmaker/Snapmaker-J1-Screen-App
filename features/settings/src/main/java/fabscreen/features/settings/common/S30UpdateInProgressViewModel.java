package fabscreen.features.settings.common;

import androidx.annotation.NonNull;

import fabscreen.platform.base.lib.update.Updater;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class S30UpdateInProgressViewModel extends BaseViewModel {
    private final BehaviorSubject<String> mScreenUpdateNotifier = BehaviorSubject.create();
    private final BehaviorSubject<String> mUpdateDescSubject = BehaviorSubject.create();
    private final BehaviorSubject<Integer> mProgressSubject = BehaviorSubject.createDefault(0);
    private final Updater mUpdater;

    public S30UpdateInProgressViewModel() {
        mUpdater = new Updater();
        watchUpdateProgress();
    }

    public void update(String filePath, boolean isLocal) {
        // TODO: 2022/4/29 heartbeat will loss
        mUpdater.update(filePath, isLocal)
                .subscribeOn(Schedulers.io())
                .as(bindToLifecycle())
                .subscribe(t -> {
                    mScreenUpdateNotifier.onNext(t);
                    mProgressSubject.onNext(100);
                }, mScreenUpdateNotifier::onError);
    }

    public Observable<String> waitingForUpdateScreen() {
        return mScreenUpdateNotifier.hide();
    }

    public Observable<String> getUpdateDescObservable() {
        return mUpdateDescSubject.distinctUntilChanged();
    }

    public Observable<Integer> getProgressObservable() {
        return mProgressSubject.distinctUntilChanged();
    }

    /**
     * Watch the update progress.
     * Let the fragment know which module is updating, and the updating progress(0~100).
     */
    private void watchUpdateProgress() {
        mUpdater.getProgressObservable()
                .as(bindToLifecycle())
                .subscribe(progress -> {
                    mProgressSubject.onNext(getProcessedProgress(progress));
                    mUpdateDescSubject.onNext(getUpdateDesc(progress.type));
                }, mProgressSubject::onError);
    }

    private int getProcessedProgress(Updater.Progress progress) {
        if (progress == null) return 0;
        int type = progress.type;
        int fractionProgress = progress.progress;
        switch (type) {
            case -1:
                return (int) (0.4f * fractionProgress);
            case -5:
                return (int) (40 + 0.2f * fractionProgress);
            case 0:
                return (int) (60 + 0.2f * fractionProgress);
            case 2:
                return (int) (80 + 0.1f * fractionProgress);
            case 1:
                return (int) (90 + 0.1f * fractionProgress);
            default:
                return mProgressSubject.getValue();
        }
    }

    @NonNull
    private String getUpdateDesc(int type) {
//        switch (type) {
//            case -5:
//                return "Extracting SC file...";
//            case -4:
//                return "Extracting BT file...";
//            case -3:
//                return "Extracting EM file...";
//            case -2:
//                return "Extracting MC file...";
//            case -1:
//                return "Copying updating file...";
//            case 0:
//                return "Updating MC...";
//            case 1:
//                return "Updating EM...";
//            case 2:
//                return "Updating BT...";
//            default:
//                return "Updating...";
//        }
        return "Updating...";
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mUpdater.clear();
    }
}
