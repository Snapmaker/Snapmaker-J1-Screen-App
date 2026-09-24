package fabscreen.platform.base.lib.api;

import com.orhanobut.logger.Logger;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public abstract class ApiObserver<T> implements Observer<T> {
    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(T t) {
        onSuccess(t);
    }

    @Override
    public void onError(Throwable e) {
        if (e instanceof UnknownHostException) {
            Logger.w("Unable to resolve API server, please check your network connectivity.");
        } else if (e instanceof SocketTimeoutException) {
            Logger.w("Socket timeout, please check your network is available.");
            // AndroidSchedulers.mainThread().scheduleDirect(this::showCheckFailDialog, 1000, TimeUnit.MILLISECONDS);
        } else {
            LogHelper.log(e);
        }
    }

    @Override
    public void onComplete() {

    }

    protected void onSuccess(T t){

    }
}
