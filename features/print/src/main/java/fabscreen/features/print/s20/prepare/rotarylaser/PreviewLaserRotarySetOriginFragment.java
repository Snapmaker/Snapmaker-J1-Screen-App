package fabscreen.features.print.s20.prepare.rotarylaser;

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
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.MockConst;
import fabscreen.platform.base.legacy.connection.SSTPPacketContent;
import fabscreen.platform.base.model.LaserPattern;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.common.LaserCalibrationViewModel;
import fabscreen.platform.core.ui.presenter.ControlBAxisPanelWidgetPresenter;
import fabscreen.platform.core.ui.presenter.ControlXYZPanelWidgetPresenter;
import fabscreen.platform.core.ui.presenter.CoordinateSystemPresenter;
import fabscreen.platform.core.ui.presenter.CoordinateXYZBGeminiWidgetPresenter;
import fabscreen.platform.core.ui.presenter.Laser4AxisSetOriginPagerPresenter;
import fabscreen.platform.core.ui.view.ControlPanelAdapter;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class PreviewLaserRotarySetOriginFragment extends BaseFragment {
    @BindView(R2.id.btn_laser_calibration_4axis_set_origin_next)
    Button mBtnNext;

    @BindView(R2.id.vp_laser_calibration_4axis_control_panels)
    ViewPager mVpControlPanels;
    @BindView(R2.id.tl_laser_calibration_4axis_control_panel_indicator)
    TabLayout mTlControlPanel;

    private List<View> mViews;
    private ControlPanelAdapter mPanelAdapter;

    private boolean mAutoMode = false;

    private CoordinateXYZBGeminiWidgetPresenter mCoordinateWidgetPresenter;

    private ControlXYZPanelWidgetPresenter mControlWidgetPanelPresenter;
    private ControlBAxisPanelWidgetPresenter mControlRotaryPanelPresenter;

    private Laser4AxisSetOriginPagerPresenter mLaser4AxisSetOriginPagerPresenter;

    private CoordinateSystemPresenter mCoordinateSystemPresenter;

    private LaserCalibrationViewModel mViewModel;
    private BehaviorSubject<Boolean> mMovingEventSubject = BehaviorSubject.createDefault(false);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = getViewModel();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.control_set_origin);

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

    public void initPattern() {
        LaserPattern pattern = new LaserPattern(LaserPattern.SHAPE_RULER, LaserPattern.DIRECTION_Y, LaserPattern.ALIGNMENT_ENGRAVE_CENTER, 0.5f);
        mViewModel.setLaserPattern(pattern);
    }

    private void initView() {
        if (getArguments() != null) {
            mAutoMode = getArguments().getBoolean("auto_mode");
        }

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

        // xyz control panel
        mControlWidgetPanelPresenter = new ControlXYZPanelWidgetPresenter(disposables);
        mControlWidgetPanelPresenter.bind(controlXYZPanel, 0.1f, 1, 5f);
        mControlWidgetPanelPresenter.connect();
        if (mAutoMode) {
            mControlWidgetPanelPresenter.disabledZ();
        }

        // b axis control panel
        mControlRotaryPanelPresenter = new ControlBAxisPanelWidgetPresenter(disposables);
        mControlRotaryPanelPresenter.bind(controlBAxisPanel);
        mControlRotaryPanelPresenter.connect();

        // rotary set origin panel
        mLaser4AxisSetOriginPagerPresenter = new Laser4AxisSetOriginPagerPresenter(getContext());
        mLaser4AxisSetOriginPagerPresenter.bindView(getLifecycle(), getView(), false);
        if (mAutoMode) {
            mLaser4AxisSetOriginPagerPresenter.disabledZ();
        }
        mLaser4AxisSetOriginPagerPresenter.setBoundary(ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getModelBoundary());

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

        mCoordinateSystemPresenter = new CoordinateSystemPresenter(disposables);
        mCoordinateSystemPresenter.ensureCoordinate(1);


        startLaser();
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

    private Observable<Boolean> gotoInitialPosition() {
        if (ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId() == Module.ModuleType.HEAD_LASER_10W) {
            // We assure that position was already initialized when enter in 10w laser workflow.
            return Observable.just(true);
        }

        float laserFocus = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolHeadInfoValue().getLaserFocalLength();
//        float thickness = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserMaterialThickness();
        final float z = laserFocus + getViewModel().getWorkpieceDiameter() / 2;
        final float sizeX = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getX();
        // TODO: getY
        final float sizeY = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getY();
        float initialY = Math.min(sizeY - MockConst.ROTARY_MOCK_CHUCK_LENGTH - (2 * 10 + 1), sizeY - MockConst.ROTARY_MOCK_CHUCK_LENGTH - mViewModel.getWorkpieceLength());
        Logger.d("Get laser focus %.2f", laserFocus);

        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 X%.2f Y%.2f F3000", sizeX / 2f - 8, initialY)))
                .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 Z%.2f F1800", z)))
                .map(res -> true);
    }

    private Observable<Boolean> setAsOrigin(boolean success) {
        // Switch to CS#1 and then mark current position as origin
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1)
                .flatMap(result -> {
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

    private void startLaser() {
        if (mAutoMode) {
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
        } else {
            turnOnLaser()
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(response -> {
                        // do nothing
                    }, e -> {
                        Logger.e("Failed to turn on laser.");
                        LogHelper.log(e);
                    });
        }
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
                    Bundle arguments = getArguments();
                    if (arguments == null) {
                        return;
                    }
                    ServiceContainer.getInstance().getService(IRouter.class)
                            .routeToPrintPage()
                            .start(getContext());
                }, e -> {
                    LogHelper.log(e);
                    mMovingEventSubject.onNext(false);
                });
    }

    @Override
    protected void back() {
        turnOffLaser()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(response -> super.back(), LogHelper::log);
    }
}
