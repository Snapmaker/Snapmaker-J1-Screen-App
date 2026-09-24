package fabscreen.platform.core.ui.presenter;

import android.content.Context;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class BasePresenter {
    private Context mContext;
    private CompositeDisposable mCompositeDisposable;

    BasePresenter(CompositeDisposable compositeDisposable) {
        mContext = ServiceContainer.getInstance().getService(IAppService.class).getAppContext();
        mCompositeDisposable = compositeDisposable;
    }

    protected Context getContext() {
        return mContext;
    }


    protected CompositeDisposable getCompositeDisposable() {
        return mCompositeDisposable;
    }

    protected void addDisposable(Disposable disposable) {
        mCompositeDisposable.add(disposable);
    }
}
