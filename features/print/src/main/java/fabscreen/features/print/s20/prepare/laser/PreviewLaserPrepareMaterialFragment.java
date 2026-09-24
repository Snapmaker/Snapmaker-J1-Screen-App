package fabscreen.features.print.s20.prepare.laser;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.s20.preview.PreviewActivity;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.presenter.CoordinateSystemPresenter;
import fabscreen.platform.core.ui.presenter.MaterialThicknessWidgetPresenter;

public class PreviewLaserPrepareMaterialFragment extends BaseFragment {
    private CoordinateSystemPresenter mCoordinateSystemPresenter;
    private MaterialThicknessWidgetPresenter mMaterialThicknessWidgetPresenter;

    public static PreviewLaserPrepareMaterialFragment newInstance() {
        return new PreviewLaserPrepareMaterialFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_preview_laser_prepare_material;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        setTitle(R.string.laser_calibration_auto_focus);

        mMaterialThicknessWidgetPresenter = new MaterialThicknessWidgetPresenter(disposables);
        mMaterialThicknessWidgetPresenter.bind(getView());
        mMaterialThicknessWidgetPresenter.connectPreference();

        mCoordinateSystemPresenter = new CoordinateSystemPresenter(disposables);
        mCoordinateSystemPresenter.ensureCoordinate(1);
    }

    @OnClick(R2.id.btn_preview_laser_prepare_material_next)
    void onClickNext() {
        playNormalClickSound();
        final float thickness = mMaterialThicknessWidgetPresenter.getTargetValue();
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLaserMaterialThickness(thickness);
        Logger.d("Set material %.1f mm.", thickness);

        if (getActivity() != null) {
            ((PreviewActivity) getActivity()).gotoLaserPrepareSafetyGogglesFragment(true);
        }
    }
}
