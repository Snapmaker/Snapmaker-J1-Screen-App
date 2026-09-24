package fabscreen.platform.core.ui.presenter;

import android.view.View;

import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.core.R;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class SpindleWidgetPresenter extends SetValueRulerWidgetPresenter {
    public SpindleWidgetPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
    }

    @Override
    public void bind(View view) {
        super.bind(view);

        mTvTitle.setText(R.string.print_spindle_spend);

        mTvValueCurrent.setVisibility(View.GONE);
        mTvValueSlash.setVisibility(View.GONE);
        mTvValueUnit.setVisibility(View.VISIBLE);

        mTvValueUnit.setText(R.string.all_unit_percentage);
        mRvRuler.setMinValue(50);
        mRvRuler.setMaxValue(100);
    }

    public void connectControl() {
        // TODO: Use spindle's own preference
        float initialValue = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserControlPower();
        setTargetValue(initialValue);

        // Save the changed value to preferences
        Disposable sub = getTargetValueObservable()
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(value -> ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLaserControlPower(value));
        addDisposable(sub);
    }
}
