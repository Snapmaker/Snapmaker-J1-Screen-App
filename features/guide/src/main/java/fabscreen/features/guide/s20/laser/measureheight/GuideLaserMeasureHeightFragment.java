package fabscreen.features.guide.s20.laser.measureheight;

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
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.presenter.ControlXYZPanelWidgetPresenter;

public class GuideLaserMeasureHeightFragment extends BaseFragment {
    private ControlXYZPanelWidgetPresenter mControlPanelPresenter;

    public static GuideLaserMeasureHeightFragment newInstance() {
        return new GuideLaserMeasureHeightFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.laser_calibration_measure_height);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_laser_calibration_measure_height;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        mControlPanelPresenter = new ControlXYZPanelWidgetPresenter(disposables);
        mControlPanelPresenter.bind(getView(), 0.1f, 1f, 10f);
        mControlPanelPresenter.connect();
    }

    @OnClick(R2.id.btn_laser_calibration_next)
    void onClickNext() {
        playNormalClickSound();
        MachineStatus status = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue();
        float bottomZ = status.currentPosition.getZ();
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLaserBottomZ(bottomZ);

        Logger.d("Set bottomZ " + bottomZ);

        if (getActivity() != null) {
            ((GuideLaserActivity) getActivity()).startSafetyGogglesFragment();
        }
    }
}
