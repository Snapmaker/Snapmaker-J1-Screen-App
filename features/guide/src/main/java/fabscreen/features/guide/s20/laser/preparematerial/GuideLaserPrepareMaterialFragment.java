package fabscreen.features.guide.s20.laser.preparematerial;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.features.guide.s20.laser.GuideLaserActivity;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.presenter.MaterialThicknessWidgetPresenter;

public class GuideLaserPrepareMaterialFragment extends BaseFragment {
    private MaterialThicknessWidgetPresenter mMaterialThicknessWidgetPresenter;

    public static GuideLaserPrepareMaterialFragment newInstance() {
        return new GuideLaserPrepareMaterialFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.laser_calibration_material_thickness);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_guide_laser_prepare_material;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        mMaterialThicknessWidgetPresenter = new MaterialThicknessWidgetPresenter(disposables);
        mMaterialThicknessWidgetPresenter.bind(getView());
        mMaterialThicknessWidgetPresenter.connectPreference();
    }

    @OnClick(R2.id.btn_guide_laser_prepare_material_next)
    void onClickNext() {
        playNormalClickSound();
        final float thickness = mMaterialThicknessWidgetPresenter.getTargetValue();
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLaserMaterialThickness(thickness);

        Logger.d("Set material %.1f mm.", thickness);

        if (getActivity() == null) return;
        ((GuideLaserActivity) getActivity()).startMeasureHeightIntroFragment();
    }
}
