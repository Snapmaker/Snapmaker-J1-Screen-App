package fabscreen.platform.base.service;

import io.reactivex.Observable;

public interface IHttpDownloadManager {
    void start(String url, String savePath);

    void cancel();

    Observable<Integer> getProgressObservable();

    boolean isDownloading();
}
