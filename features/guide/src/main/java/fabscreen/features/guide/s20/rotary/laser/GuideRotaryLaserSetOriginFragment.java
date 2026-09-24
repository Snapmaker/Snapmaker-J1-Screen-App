package fabscreen.features.guide.s20.rotary.laser;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.platform.base.Constants;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.SSTPPacketContent;
import fabscreen.platform.base.model.LaserPattern;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.common.LaserCalibrationViewModel;
import fabscreen.platform.core.ui.presenter.ControlBAxisPanelWidgetPresenter;
import fabscreen.platform.core.ui.presenter.ControlXYZPanelWidgetPresenter;
import fabscreen.platform.core.ui.presenter.CoordinateXYZBGeminiWidgetPresenter;
import fabscreen.platform.core.ui.presenter.Laser4AxisSetOriginPagerPresenter;
import fabscreen.platform.core.ui.view.ControlPanelAdapter;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class GuideRotaryLaserSetOriginFragment extends BaseFragment {
    @BindView(R2.id.btn_laser_calibration_4axis_set_origin_next)
    Button mBtnNext;
    @BindView(R2.id.vp_laser_calibration_4axis_control_panels)
    ViewPager mVpControlPanels;
    @BindView(R2.id.tl_laser_calibration_4axis_control_panel_indicator)
    TabLayout mTlControlPanel;
    private List<View> mViews;
    private ControlPanelAdapter mPanelAdapter;
    private LaserCalibrationViewModel mViewModel;
    private CoordinateXYZBGeminiWidgetPresenter mCoordinateWidgetPresenter;
    private ControlXYZPanelWidgetPresenter mControlWidgetPanelPresenter;
    private ControlBAxisPanelWidgetPresenter mControlRotaryPanelPresenter;
    private Laser4AxisSetOriginPagerPresenter mLaser4AxisSetOriginPagerPresenter;
    private BehaviorSubject<Boolean> mMovingEventSubject = BehaviorSubject.createDefault(false);

    public static GuideRotaryLaserSetOriginFragment newInstance() {
        return new GuideRotaryLaserSetOriginFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = getViewModel();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initPattern();
        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_laser_calibration_4axis_set_origin;
    }

    @Override
    protected LaserCalibrationViewModel getViewModel() {
        return getViewModelProvider().get(LaserCalibrationViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mLaser4AxisSetOriginPagerPresenter != null) {
            if (!mLaser4AxisSetOriginPagerPresenter.getBoundaryWarningFlag()) {
                mLaser4AxisSetOriginPagerPresenter.setBoundaryWarningFlag(true);
            }
        }
    }

    private void initPattern() {
        LaserPattern pattern = new LaserPattern(LaserPattern.SHAPE_RULER, LaserPattern.DIRECTION_Y, LaserPattern.ALIGNMENT_ENGRAVE_CENTER, 0.5f);
        mViewModel.setLaserPattern(pattern);
    }

    private void initView() {
        setTitle(R.string.control_set_origin);
        mBtnNext.setText(R.string.all_next);

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

        // XYZ control panel
        mControlWidgetPanelPresenter = new ControlXYZPanelWidgetPresenter(disposables);
        mControlWidgetPanelPresenter.bind(controlXYZPanel, 0.1f, 1f, 5f);
        mControlWidgetPanelPresenter.connect();
        mControlWidgetPanelPresenter.disabledZ();

        // B Axis control panel
        mControlRotaryPanelPresenter = new ControlBAxisPanelWidgetPresenter(disposables);
        mControlRotaryPanelPresenter.bind(controlBAxisPanel);
        mControlRotaryPanelPresenter.connect();

        // rotary set origin panel
        mLaser4AxisSetOriginPagerPresenter = new Laser4AxisSetOriginPagerPresenter(getContext());
        mLaser4AxisSetOriginPagerPresenter.bindView(getLifecycle(), getView(), false);
        mLaser4AxisSetOriginPagerPresenter.setBoundary(mViewModel.getLaserPattern().getPatternBoundary());
        mLaser4AxisSetOriginPagerPresenter.disabledZ();

        mControlWidgetPanelPresenter.getMovingEventObservable()
                .skip(1)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(movingEvent -> {
                    mMovingEventSubject.onNext(movingEvent);
                });

        mControlRotaryPanelPresenter.getMovingEventObservable()
                .skip(1)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(movingEvent -> {
                    mMovingEventSubject.onNext(movingEvent);
                });

        mLaser4AxisSetOriginPagerPresenter.getMovingEventObservable()
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
                    mControlWidgetPanelPresenter.setEnabled(!movingEvent);
                    mLaser4AxisSetOriginPagerPresenter.setEnabled(!movingEvent);
                    mControlRotaryPanelPresenter.setEnabled(!movingEvent);
                });

        start();
    }

    private Observable<SSTPPacketContent.GcodeResponse> turnOnLaser() {
        // We use 1% as safe viable power for 10w laser module, 0.5% as default for 1.6w laser module.
        int headType = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId();
        float laserSafePowerPercent = (headType == Module.ModuleType.HEAD_LASER_10W) ? 1.0f : 0.5f;
        return ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("M3 P " + laserSafePowerPercent);
    }

    private Observable<SSTPPacketContent.GcodeResponse> turnOffLaser() {
        return ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("M5");
    }

    private Observable<MachineStatus> updateCoordinateSystem(Object response) {
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem();
    }

    private float calculateWorkingZ(float laserFocus) {
        float bottomZ = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserBottomZ();

        Logger.d("Get laser focus ", laserFocus);

        return Math.max(laserFocus + mViewModel.getWorkpieceDiameter() / 2, bottomZ + 10 * Constants.LASER_TEST_PATTERN_Z_DIFF + 1);
    }

    private Observable<Boolean> gotoInitialPosition() {
        float laserFocus = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolHeadInfoValue().getLaserFocalLength();
        final float z = calculateWorkingZ(laserFocus);

        // Make sure we are at G53 before movement
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 Z%.2f F1800", z)))
                .map(res -> true);
    }

    private Observable<Boolean> setAsOrigin(boolean success) {
        // Switch to CS#1 and then mark current position as origin
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1)
                .flatMap(res -> {
                    Vector vector = new Vector();
                    vector.setX(0);
                    vector.setY(0);
                    vector.setZ(0);
                    vector.setB(0);
                    return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector);
                })
                .flatMap(this::updateCoordinateSystem)
                .map(res -> true);
    }

    private void start() {
        mMovingEventSubject.onNext(true);
        gotoInitialPosition()
                .flatMap(this::setAsOrigin)
                .flatMap(response -> turnOnLaser())
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(response -> {
                    mMovingEventSubject.onNext(false);
                }, e -> {
                    Logger.e("Failed to prepare for set origin.");
                    LogHelper.log(e);
                    mMovingEventSubject.onNext(false);
                });
    }

    @OnClick(R2.id.btn_laser_calibration_4axis_set_origin_next)
    void onClickNext() {
        playNormalClickSound();
        mMovingEventSubject.onNext(true);

        turnOffLaser()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    mMovingEventSubject.onNext(false);
                    if (getActivity() != null) {
                        ((GuideRotaryLaserActivity) getActivity()).startManualFocusStep1Fragment();
                    }
                }, e -> {
                    LogHelper.log(e);
                    mMovingEventSubject.onNext(false);
                });
    }

    @Override
    protected void back() {
        turnOffLaser()
                .flatMap(res -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(response -> super.back(), LogHelper::log);
    }
}
