package fabscreen.features.guide.s20.rotary.laser;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.presenter.ControlBAxisPanelWidgetPresenter;
import fabscreen.platform.core.ui.presenter.ControlXYZPanelWidgetPresenter;
import fabscreen.platform.core.ui.view.ControlPanelAdapter;

public class GuideRotaryLaserMeasureHeightFragment extends BaseFragment {
    @BindView(R2.id.vp_laser_calibration_4axis_control_panels)
    ViewPager mVpControlPanels;
    @BindView(R2.id.tl_laser_calibration_4axis_control_panel_indicator)
    TabLayout mTlControlPanel;
    private List<View> mViews;
    private ControlPanelAdapter mPanelAdapter;
    // linear control
    private ControlXYZPanelWidgetPresenter mControlPanelPresenter;
    // rotary control
    private ControlBAxisPanelWidgetPresenter mControlRotaryPanelPresenter;

    public static GuideRotaryLaserMeasureHeightFragment newInstance() {
        return new GuideRotaryLaserMeasureHeightFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.guide_laser_measure_height_title);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_laser_calibration_4axis_measure_height;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        LayoutInflater inflater = getLayoutInflater();
        View controlXYZPanel = inflater.inflate(R.layout.widget_control_panel_xyz_axes_for_4axis, null);
        View controlBAxisPanel = inflater.inflate(R.layout.widget_control_panel_b_axis, null);
        mViews = new ArrayList<>();
        mViews.add(controlXYZPanel);
        mViews.add(controlBAxisPanel);

        mPanelAdapter = new ControlPanelAdapter(mViews);
        mVpControlPanels.setAdapter(mPanelAdapter);
        mTlControlPanel.setupWithViewPager(mVpControlPanels);
        mTlControlPanel.setEnabled(false);

        mControlPanelPresenter = new ControlXYZPanelWidgetPresenter(disposables);
        mControlPanelPresenter.bind(controlXYZPanel, 0.1f, 1, 5f);
        mControlPanelPresenter.connect();

        // rotary control panel
        mControlRotaryPanelPresenter = new ControlBAxisPanelWidgetPresenter(disposables);
        mControlRotaryPanelPresenter.bind(controlBAxisPanel);
        mControlRotaryPanelPresenter.connect();
    }

    @OnClick(R2.id.btn_laser_calibration_4axis_next)
    void onClickNext() {
        playNormalClickSound();
        MachineStatus status = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue();
        float bottomZ = status.currentPosition.getZ();
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLaserBottomZ(bottomZ);

        Logger.d("Set bottomZ " + bottomZ);

        if (getActivity() != null) {
            ((GuideRotaryLaserActivity) getActivity()).startSafetyGogglesFragment();
        }
    }
}
