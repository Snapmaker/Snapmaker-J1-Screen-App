package fabscreen.features.settings.a350.advanced.laser;

import android.view.View;

import fabscreen.features.settings.R;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.core.ui.presenter.SetValueRulerWidgetPresenter;
import io.reactivex.disposables.CompositeDisposable;

public class LaserFocusPresenter extends SetValueRulerWidgetPresenter {
    public LaserFocusPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
    }

    @Override
    public void bind(View view) {
        super.bind(view);

        setPrecision(1);

        mTvTitle.setText(R.string.all_laser_height);

        mTvValueCurrent.setVisibility(View.GONE);
        mTvValueSlash.setVisibility(View.GONE);
        mTvValueUnit.setVisibility(View.VISIBLE);

        mTvValueUnit.setText(R.string.all_unit_mm);
        mRvRuler.setUnit(0.1f);
        mRvRuler.setMinValue(0);
        mRvRuler.setMaxValue(40);
    }

    public void connect() {
        final float laserFocus = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolHeadInfoValue().getLaserFocalLength();
        setTargetValue(laserFocus);
    }
}
