package fabscreen.platform.lib;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;

public class SubjectHolder<T> {
    private BehaviorSubject<T> mSubject;

    public SubjectHolder(BehaviorSubject<T> initialValue) {
        mSubject = initialValue;
    }

    public Observable<T> getObservable() {
        return mSubject.hide();
    }

    public T getValue() {
        return mSubject.getValue();
    }

    public <R> Disposable subscribeOnUIThread(LifecycleOwner owner, Consumer<? super T> onNext) {
        return mSubject.hide()
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(owner, Lifecycle.Event.ON_DESTROY)))
                .subscribe(onNext);
    }


}
