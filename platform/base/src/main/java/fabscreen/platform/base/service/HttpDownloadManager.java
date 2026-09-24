package fabscreen.platform.base.service;

import androidx.annotation.Keep;

import com.orhanobut.logger.Logger;

import java.net.SocketTimeoutException;

import fabscreen.platform.base.instantiation.IServiceIdentifier;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.HttpDownloader;
import fabscreen.platform.base.lib.api.ApiObserver;
import fabscreen.platform.base.lib.update.UpdateFileParser;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import okhttp3.ResponseBody;

public class HttpDownloadManager implements IHttpDownloadManager, IServiceIdentifier {
    private final CompositeDisposable mDisposables = new CompositeDisposable();
    private final BehaviorSubject<Integer> mProgressSubj = BehaviorSubject.create();
    private final IAppService mAppService;

    @Keep
    public HttpDownloadManager() {
        mAppService = ServiceContainer.getInstance().getService(IAppService.class);
    }

    @Override
    public void start(String url, String savePath) {
        if (url == null) return;
        mProgressSubj.onNext(0);
        HttpDownloader downloader = new HttpDownloader(UpdateFileParser.getBigBinPath(mAppService.getAppContext()));

        mDisposables.add(downloader.getDownloadProgressObservable()
                .takeUntil(progress -> progress == 100)
                .subscribe(t -> {
                    if (mProgressSubj.getValue() == -1) return;
                    mProgressSubj.onNext(t);
                }, e -> {
                    mProgressSubj.onNext(-1);
                    LogHelper.log(e);
                }));

        downloader.startDownload(url)
                .doOnSubscribe(mDisposables::add)
                .subscribeOn(Schedulers.io())
                .subscribe(new ApiObserver<ResponseBody>() {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if (e instanceof SocketTimeoutException) {
                            Logger.d("Socket timeout while downloading, please check your network is available.");
                        }
                        mProgressSubj.onNext(-1);
                    }
                });

    }

    @Override
    public void cancel() {
        Logger.d("Download task canceled!");
        mDisposables.clear();
        mProgressSubj.onNext(-2);
    }

    @Override
    public Observable<Integer> getProgressObservable() {
        return mProgressSubj.hide();
    }

    @Override
    public boolean isDownloading() {
        return mProgressSubj.getValue() != null && mProgressSubj.getValue() >= 0 && mProgressSubj.getValue() < 100;
    }
}
