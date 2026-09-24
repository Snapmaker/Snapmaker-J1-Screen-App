package fabscreen.features.print.s20.prepare.laser;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.s20.preview.PreviewViewModel;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.SSTPPacketContent;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.controller.PrintController;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.presenter.ControlXYZPanelWidgetPresenter;
import fabscreen.platform.core.ui.presenter.CoordinateSystemPresenter;
import fabscreen.platform.core.ui.presenter.CoordinateXYZGeminiWidgetPresenter;
import fabscreen.platform.core.ui.presenter.SetOriginPagerPresenter;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class PreviewLaserPrepareSetOriginFragment extends BaseFragment {
    @BindView(R2.id.btn_preview_laser_prepare_set_origin_next)
    Button mBtnNext;

    private CoordinateXYZGeminiWidgetPresenter mCoordinateXYZWidgetPresenter;
    private ControlXYZPanelWidgetPresenter mControlPanelPresenter;
    private SetOriginPagerPresenter mSetOriginPagerPresenter;
    private int mHeadType = Module.ModuleType.HEAD_UNPLUGGED;
    private boolean mAutoMode = false;
    private PreviewViewModel mViewModel;
    private BehaviorSubject<Boolean> mMovingEventSubject = BehaviorSubject.createDefault(false);

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.control_set_origin);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_preview_laser_prepare_set_origin;
    }

    @Override
    protected PreviewViewModel getViewModel() {
        return getViewModelProvider().get(PreviewViewModel.class);
    }

    private void initView() {
        if (getArguments() != null) {
            mAutoMode = getArguments().getBoolean("auto_mode");
        }

        mCoordinateXYZWidgetPresenter = new CoordinateXYZGeminiWidgetPresenter(disposables);
        mCoordinateXYZWidgetPresenter.bind(getView());
        mCoordinateXYZWidgetPresenter.connect();

        mControlPanelPresenter = new ControlXYZPanelWidgetPresenter(disposables);
        mControlPanelPresenter.bind(getView(), 0.1f, 1f, 10f);
        mControlPanelPresenter.connect();

        mSetOriginPagerPresenter = new SetOriginPagerPresenter(getContext());
        mSetOriginPagerPresenter.bindView(getLifecycle(), getView());
        mSetOriginPagerPresenter.setBoundary(ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getModelBoundary());

        if (mAutoMode || mHeadType == Module.ModuleType.HEAD_LASER_10W) {
            mControlPanelPresenter.disabledZ();
            mSetOriginPagerPresenter.disabledZ();
        }

        mControlPanelPresenter.getMovingEventObservable()
                .skip(1)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(movingEvent -> {
                    mMovingEventSubject.onNext(movingEvent);
                });
        mSetOriginPagerPresenter.getMovingEventObservable()
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
                    mControlPanelPresenter.setEnabled(!movingEvent);
                    mSetOriginPagerPresenter.setEnabled(!movingEvent);
                });

        CoordinateSystemPresenter coordinateSystemPresenter = new CoordinateSystemPresenter(disposables);
        coordinateSystemPresenter.ensureCoordinate(1);

        mHeadType = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId();
        if (mHeadType == Module.ModuleType.HEAD_LASER || mHeadType == Module.ModuleType.HEAD_LASER_10W) {
            startLaser();
            if (mHeadType == Module.ModuleType.HEAD_LASER_10W) {
                mBtnNext.setText(R.string.all_next);
            }
        }

        mViewModel = getViewModel();
        // We need to back into select mode notify page if using 10w Laser.
        // So pop back other fragments until PreviewLaserPrepareModeNoteFragment.
        mViewModel.getResultBackObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(settled -> {
                    mMovingEventSubject.onNext(false);
                    if (!settled) return;
                    requireFragmentManager().popBackStack(
                            PreviewLaserMeasureThicknessFragment.class.getSimpleName(),
                            0);
                });
    }

    private Observable<SSTPPacketContent.GcodeResponse> turnOnLaser() {
        // We use 1% as safe viable power for 10w laser module, 0.5% as default for 1.6w laser module.
        float laserSafePowerPercent = (mHeadType == Module.ModuleType.HEAD_LASER_10W) ? 1.0f : 0.5f;
        return ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("M3 P " + laserSafePowerPercent);
    }

    private Observable<SSTPPacketContent.GcodeResponse> turnOffLaser() {
        return ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("M5");
    }

    private Observable<MachineStatus> updateCoordinateSystem(Object response) {
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem();
    }

    private Observable<Boolean> gotoInitialPosition() {
        if (mHeadType == Module.ModuleType.HEAD_LASER_10W) {
            // already at right position
            return Observable.just(true);
        }
        float laserFocus = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolHeadInfoValue().getLaserFocalLength();
        float thickness = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserMaterialThickness();
        final float z = laserFocus + thickness;
        final float sizeX = (float) ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getX();
        final float sizeY = (float) ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getY();
        Logger.d("Get laser focus %.2f", laserFocus);

        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 X%.2f Y%.2f F3000", sizeX / 2f, sizeY / 2f)))
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

    @OnClick(R2.id.btn_preview_laser_prepare_set_origin_next)
    void onClickStart() {
        playNormalClickSound();
        mMovingEventSubject.onNext(true);
        turnOffLaser()
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(success ->
                        (mHeadType == Module.ModuleType.HEAD_LASER_10W) ?
                                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getHeaderSecurityStatus() : Observable.just(new PrintController.HeaderSecurity((byte) 0))
                )
                .as(bindToLifecycle())
                .subscribe(success -> {
                    mMovingEventSubject.onNext(false);
                    if (success.status == 0) {
                        Bundle arguments = getArguments();
                        if (arguments == null) {
                            return;
                        }
                        ServiceContainer.getInstance().getService(IRouter.class).routeToPrintPage().start(getContext());
                    }

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
                .subscribe(response -> {
                    if (ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId() == Module.ModuleType.HEAD_LASER_10W) {
                        if (mAutoMode) {
                            mMovingEventSubject.onNext(true);
                            mViewModel.initCameraPosition(true);
                        } else {
                            requireFragmentManager().popBackStack(
                                    PreviewLaserPrepareModeNoteFragment.class.getSimpleName(),
                                    0);
                        }

                    } else {
                        // Call back() directly to back into previous page.
                        super.back();
                    }
                }, LogHelper::log);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
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
}
