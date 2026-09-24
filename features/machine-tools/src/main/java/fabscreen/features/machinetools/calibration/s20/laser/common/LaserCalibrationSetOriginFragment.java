package fabscreen.features.machinetools.calibration.s20.laser.common;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.s20.laser.CalibrationLaserActivity;
import fabscreen.platform.base.Constants;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.SSTPPacketContent;
import fabscreen.platform.base.lib.parser.Position;
import fabscreen.platform.base.model.ModelBoundary;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.presenter.ControlXYZPanelWidgetPresenter;
import fabscreen.platform.core.ui.presenter.CoordinateXYZGeminiWidgetPresenter;
import fabscreen.platform.core.ui.presenter.SetOriginPagerPresenter;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class LaserCalibrationSetOriginFragment extends BaseFragment {
    @BindView(R2.id.btn_preview_laser_prepare_set_origin_next)
    Button mBtnNext;
    private CoordinateXYZGeminiWidgetPresenter mCoordinateXYZWidgetPresenter;
    private ControlXYZPanelWidgetPresenter mControlPanelPresenter;
    private SetOriginPagerPresenter mSetOriginPagerPresenter;
    private BehaviorSubject<Boolean> mMovingEventSubject = BehaviorSubject.createDefault(false);

    public static LaserCalibrationSetOriginFragment newInstance() {
        return new LaserCalibrationSetOriginFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_preview_laser_prepare_set_origin;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        setTitle(R.string.control_set_origin);
        mBtnNext.setText(R.string.all_next);

        mCoordinateXYZWidgetPresenter = new CoordinateXYZGeminiWidgetPresenter(disposables);
        mCoordinateXYZWidgetPresenter.bind(getView());
        mCoordinateXYZWidgetPresenter.connect();

        mControlPanelPresenter = new ControlXYZPanelWidgetPresenter(disposables);
        mControlPanelPresenter.bind(getView(), 0.1f, 1f, 10f);
        mControlPanelPresenter.connect();
        mControlPanelPresenter.disabledZ();

        mSetOriginPagerPresenter = new SetOriginPagerPresenter(getContext());
        mSetOriginPagerPresenter.bindView(getLifecycle(), getView());
        ModelBoundary boundary = new ModelBoundary();
        boundary.updateBoundary(new Position(-20, -5f, 0));
        boundary.updateBoundary(new Position(-20, 5f, 0));
        boundary.updateBoundary(new Position(20, -5f, 0));
        boundary.updateBoundary(new Position(20, 5f, 0));
        mSetOriginPagerPresenter.setBoundary(boundary);

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

        start();
    }

    private Observable<SSTPPacketContent.GcodeResponse> turnOnLaser() {
        // We use 1% as safe viable power for 10w laser module, 0.5% as default for 1.6w laser module.
        int headType = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().modelId;
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
        float thickness = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserMaterialThickness();

        Logger.d("Get laser focus ", laserFocus);

        return Math.max(laserFocus + thickness, bottomZ + 10 * Constants.LASER_TEST_PATTERN_Z_DIFF + 1);
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
                    return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector);
                })
                .flatMap(this::updateCoordinateSystem)
                .map(res -> true);
    }

    private void start() {
        int calibrationMode = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserCalibrationMode();
        if (calibrationMode == 0) {
            mMovingEventSubject.onNext(true);
            gotoInitialPosition()
                    .flatMap(this::setAsOrigin)
                    .flatMap(res -> turnOnLaser())
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(response -> {
                        mMovingEventSubject.onNext(false);
                    }, e -> {
                        Logger.e("Failed to prepare for set origin.");
                        LogHelper.log(e);
                        mMovingEventSubject.onNext(false);
                    });

            turnOnLight();
        } else {
            // Manual mode we don't move it anymore
            mMovingEventSubject.onNext(true);
            turnOnLaser()
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
    }

    private void turnOnLight() {
        boolean cameraLightOn = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserCameraLightOn();
        if (cameraLightOn) {
            ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().setCameraLighting(true)
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(success -> {
                        // do nothing
                    }, LogHelper::log);
        }
    }

    private void turnOffLight() {
        boolean cameraLightOn = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserCameraLightOn();
        if (cameraLightOn) {
            ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().setCameraLighting(false)
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(success -> {
                        // do nothing
                    }, LogHelper::log);
        }
    }

    @OnClick(R2.id.btn_preview_laser_prepare_set_origin_next)
    void onClickNext() {
        playNormalClickSound();
        mMovingEventSubject.onNext(true);

        turnOffLaser()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    mMovingEventSubject.onNext(false);
                    if (getActivity() != null) {
                        int calibrationMode = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserCalibrationMode();
                        if (calibrationMode == 0) {
                            ((CalibrationLaserActivity) getActivity()).startAutoFocusStep1Fragment();
                        } else {
                            ((CalibrationLaserActivity) getActivity()).startManualFocusStep1Fragment();
                        }
                    }
                }, e -> {
                    LogHelper.log(e);
                    mMovingEventSubject.onNext(false);
                });
    }

    @Override
    protected void back() {
        turnOffLight();

        turnOffLaser()
                .flatMap(res -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(response -> super.back(), LogHelper::log);
    }
}
