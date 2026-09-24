package fabscreen.platform.base.lib.fabserver;

import com.orhanobut.logger.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class RetryWithDelay implements Function<Observable<? extends Throwable>, Observable<?>> {

    private int mMaxRetries;
    private long mRetryDelayMillis = -1;
    private int mRetryCount;

    public RetryWithDelay(int maxRetries, long retryDelayMillis) {
        this.mMaxRetries = maxRetries;
        mRetryDelayMillis = retryDelayMillis;
    }

    public RetryWithDelay(int maxRetries) {
        this.mMaxRetries = maxRetries;
    }

    public RetryWithDelay() {
    }

    @Override
    public Observable<?> apply(Observable<? extends Throwable> attempts) {
        return attempts.flatMap(e -> {
            if (e instanceof TimeoutException) {
                ++mRetryCount;
                Logger.d("Retry with delay number:" + mRetryCount);
                if (mMaxRetries == 0) {
                    return Observable.timer(mRetryCount, TimeUnit.SECONDS);
                } else {
                    if (mRetryCount <= mMaxRetries) {
                        return Observable.timer(mRetryDelayMillis == -1 ? mRetryCount : mRetryDelayMillis, TimeUnit.MILLISECONDS);
                    }
                    return Observable.error(new Exception(String.format("请求接口 %d 次超时", mMaxRetries)));
                }
            }
            return Observable.error(e);
        });
    }
}
