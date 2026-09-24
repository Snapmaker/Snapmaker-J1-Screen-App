package fabscreen.features.print.s20.prepare.rotarycnc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.s20.preview.PreviewActivity;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.presenter.CNC4AxisSetOriginPagerPresenter;
import fabscreen.platform.core.ui.presenter.ControlBAxisPanelWidgetPresenter;
import fabscreen.platform.core.ui.presenter.ControlXYZPanelWidgetPresenter;
import fabscreen.platform.core.ui.presenter.CoordinateSystemPresenter;
import fabscreen.platform.core.ui.presenter.CoordinateXYZBGeminiWidgetPresenter;
import fabscreen.platform.core.ui.view.ControlPanelAdapter;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class PreviewCNCPrepareRotarySetOriginFragment extends BaseFragment {
    @BindView(R2.id.btn_preview_cnc_prepare_set_origin_next)
    Button mBtnNext;

    @BindView(R2.id.vp_prepare_control_4axis_panels)
    ViewPager mVpControlPanels;
    @BindView(R2.id.tl_prepare_control_4axis_panel_indicator)
    TabLayout mTlControlPanel;

    private List<View> mViews;
    private ControlPanelAdapter mPanelAdapter;

    private CoordinateXYZBGeminiWidgetPresenter mCoordinateWidgetPresenter;
    private ControlXYZPanelWidgetPresenter mControlXYZPanelPresenter;
    private ControlBAxisPanelWidgetPresenter mControlBAxisPanelPresenter;

    private CNC4AxisSetOriginPagerPresenter mCNC4AxisSetOriginPagerPresenter;

    private CoordinateSystemPresenter mCoordinateSystemPresenter;

    private BehaviorSubject<Boolean> mMovingEventSubject = BehaviorSubject.createDefault(false);

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.control_set_origin);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_preview_cnc_prepare_4axis_set_origin;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCNC4AxisSetOriginPagerPresenter != null) {
            if (!mCNC4AxisSetOriginPagerPresenter.getBoundaryWarningFlag()) {
                mCNC4AxisSetOriginPagerPresenter.setBoundaryWarningFlag(true);
            }
        }
    }

    private void initView() {
        LayoutInflater inflater = getLayoutInflater();
        View controlXYZPanel = inflater.inflate(R.layout.widget_control_panel_xyz_axes_mini_for_4axis, null);
        View controlBAxisPanel = inflater.inflate(R.layout.widget_control_panel_b_axis, null);
        mViews = new ArrayList<>();
        mViews.add(controlXYZPanel);
        mViews.add(controlBAxisPanel);

        mPanelAdapter = new ControlPanelAdapter(mViews);
        mVpControlPanels.setAdapter(mPanelAdapter);
        mTlControlPanel.setupWithViewPager(mVpControlPanels);
        mTlControlPanel.setEnabled(false);

        // 4th coordinate panel
        mCoordinateWidgetPresenter = new CoordinateXYZBGeminiWidgetPresenter(disposables);
        mCoordinateWidgetPresenter.bind(getView());
        mCoordinateWidgetPresenter.connect();

        // control xyz panel
        mControlXYZPanelPresenter = new ControlXYZPanelWidgetPresenter(disposables);
        mControlXYZPanelPresenter.bind(controlXYZPanel, 0.1f, 1f, 5f);
        mControlXYZPanelPresenter.connect();

        // control b panel
        mControlBAxisPanelPresenter = new ControlBAxisPanelWidgetPresenter(disposables);
        mControlBAxisPanelPresenter.bind(controlBAxisPanel);
        mControlBAxisPanelPresenter.connect();

        // rotary set origin panel
        mCNC4AxisSetOriginPagerPresenter = new CNC4AxisSetOriginPagerPresenter(getContext());
        mCNC4AxisSetOriginPagerPresenter.bindView(getLifecycle(), getView());
        mCNC4AxisSetOriginPagerPresenter.setBoundary(ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getModelBoundary());

        mControlXYZPanelPresenter.getMovingEventObservable()
                .skip(1)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(movingEvent -> {
                    mMovingEventSubject.onNext(movingEvent);
                });

        mControlBAxisPanelPresenter.getMovingEventObservable()
                .skip(1)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(movingEvent -> {
                    mMovingEventSubject.onNext(movingEvent);
                });

        mCNC4AxisSetOriginPagerPresenter.getMovingEventObservable()
                .skip(1)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(movingEvent -> {
                    mMovingEventSubject.onNext(movingEvent);
                });

        mMovingEventSubject
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(movingEvent -> {
                    mBtnNext.setEnabled(!movingEvent);
                    mControlXYZPanelPresenter.setEnabled(!movingEvent);
                    mCNC4AxisSetOriginPagerPresenter.setEnabled(!movingEvent);
                    mControlBAxisPanelPresenter.setEnabled(!movingEvent);
                });

        mCoordinateSystemPresenter = new CoordinateSystemPresenter(disposables);
        mCoordinateSystemPresenter.ensureCoordinate(1);
    }

    private Observable<MachineStatus> updateCoordinateSystem(Object response) {
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem();
    }

    private Observable<Boolean> setAsOrigin(boolean success) {
        // Switch to CS#1 and then mark current position as origin
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1)
                .flatMap(result -> {
                    Vector vector = new Vector();
                    vector.setX(0);
                    vector.setY(0);
                    vector.setZ(0);
                    return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector);
                })
                .flatMap(this::updateCoordinateSystem)
                .map(res -> true);
    }

    @OnClick(R2.id.btn_preview_cnc_prepare_set_origin_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((PreviewActivity) getActivity()).gotoCNCRotaryInstallTailstockFragment();
        }
    }

    @Override
    protected void back() {
        super.back();
    }
}
