package fabscreen.platform.core.ui.presenter;

import android.view.View;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.core.R;
import io.reactivex.disposables.CompositeDisposable;

public class MaterialThicknessWidgetPresenter extends SetValueRulerWidgetPresenter {
    public MaterialThicknessWidgetPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
    }

    @Override
    public void bind(View view) {
        super.bind(view);

        setPrecision(1);

        mTvTitle.setText(R.string.laser_set_material_thickness);

        mTvValueCurrent.setVisibility(View.GONE);
        mTvValueSlash.setVisibility(View.GONE);
        mTvValueUnit.setVisibility(View.VISIBLE);

        mTvValueUnit.setText(R.string.all_unit_mm);
        mRvRuler.setMaxValue(145);
        mRvRuler.setUnit(0.1f);
    }

    public void connectPreference() {
        final float thickness0 = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserMaterialThickness();
        setTargetValue(thickness0);
    }
}
