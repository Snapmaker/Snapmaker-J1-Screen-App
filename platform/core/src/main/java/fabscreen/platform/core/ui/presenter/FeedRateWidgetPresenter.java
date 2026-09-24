package fabscreen.platform.core.ui.presenter;

import android.view.View;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.core.R;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class FeedRateWidgetPresenter extends SetValueRulerWidgetPresenter {
    public FeedRateWidgetPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
    }

    public void bind(View view) {
        super.bind(view);

        mTvTitle.setText(R.string.print_feed_rate);

        mTvValueCurrent.setVisibility(View.GONE);
        mTvValueSlash.setVisibility(View.GONE);
        mTvValueUnit.setVisibility(View.VISIBLE);

        mTvValueUnit.setText(R.string.all_unit_percentage);
        mRvRuler.setMaxValue(500);
    }

    public void connectPrintSettings() {
        float initialValue = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getOverrideFeedRate();
        setTargetValue(initialValue);

        Disposable sub = getTargetValueObservable()
                .skip(1)
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setOverrideFeedRate(value));
        addDisposable(sub);
    }

    public void connectPrint() {
        float initialValue = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getOverrideFeedRate();
        connectPrint(initialValue);
    }

    public void connectPrint(float zOffset) {
        setTargetValue(zOffset);

        Disposable sub = getTargetValueObservable()
                .skip(1)
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setOverrideFeedRate(value));
        addDisposable(sub);
    }

    @Override
    protected String formatValue(float value) {
        return String.format(Locale.US, "%d", (int) value);
    }
}
