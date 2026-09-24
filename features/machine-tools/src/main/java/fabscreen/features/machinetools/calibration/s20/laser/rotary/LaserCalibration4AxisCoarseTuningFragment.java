package fabscreen.features.machinetools.calibration.s20.laser.rotary;

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
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.common.LaserCalibrationViewModel;
import fabscreen.platform.core.ui.presenter.ControlXYZPanelWidgetPresenter;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class LaserCalibration4AxisCoarseTuningFragment extends BaseFragment {
    @BindView(R2.id.top_bar_back)
    Button mBtnBack;
    @BindView(R2.id.btn_laser_calibration_4axis_coarse_turning_next)
    Button mBtnNext;
    private LaserCalibrationViewModel mViewModel;
    private ControlXYZPanelWidgetPresenter mControlPanelPresenter;
    private BehaviorSubject<Boolean> mIsMovingSubject = BehaviorSubject.createDefault(false);

    public static LaserCalibration4AxisCoarseTuningFragment newInstance() {
        return new LaserCalibration4AxisCoarseTuningFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = getViewModel();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_laser_calibration_4axis_coarse_tuning;
    }

    @Override
    protected LaserCalibrationViewModel getViewModel() {
        return getViewModelProvider().get(LaserCalibrationViewModel.class);
    }

    private void initView() {
        setTitle(R.string.laser_calibration_set_reference_points);

        mControlPanelPresenter = new ControlXYZPanelWidgetPresenter(disposables);
        mControlPanelPresenter.bind(getView(), 0.1f, 1f, 5f);
        mControlPanelPresenter.connect();

        mIsMovingSubject
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> {
                    mBtnBack.setEnabled(!isMoving);
                    mBtnNext.setEnabled(!isMoving);
                    mControlPanelPresenter.setEnabled(!isMoving);
                });

        start();
    }

    private Observable<SSTPPacketContent.GcodeResponse> turnOnLaser() {
        Logger.d("turn on laser");
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
                }).flatMap(this::updateCoordinateSystem)
                .map(res -> true);
    }

    private void start() {
        // move to initial position
        mIsMovingSubject.onNext(true);

        gotoInitialPosition()
                .flatMap(this::setAsOrigin)
                .flatMap(res -> turnOnLaser())
                .as(bindToLifecycle())
                .subscribe(response -> {
                    mIsMovingSubject.onNext(false);
                }, e -> {
                    mIsMovingSubject.onNext(false);
                    LogHelper.log(e);
                });
    }

    @Override
    protected void back() {
        turnOffLaser()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(ret -> super.back());
    }

    @OnClick(R2.id.btn_laser_calibration_4axis_coarse_turning_next)
    void onClickNext() {
        playNormalClickSound();
        float bottomZ = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserBottomZ();
        float bottomWorkZ = bottomZ + 10 * Constants.LASER_TEST_PATTERN_Z_DIFF + 1;

        mIsMovingSubject.onNext(true);
        ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getObservable()
                .flatMap(machineStatus -> {
                    final float offsetZ = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getZ();
                    float z = machineStatus.currentPosition.getZ() - offsetZ;
                    if (z < bottomWorkZ) {
                        Vector vector = new Vector();
                        vector.setZ(bottomWorkZ - z);
                        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().gotoRelativePosition(vector);
                    } else {
                        return Observable.just(true);
                    }
                })
                .flatMap(res -> turnOffLaser())
                .flatMap(res -> {
                    Vector vector = new Vector();
                    vector.setX(0);
                    vector.setY(0);
                    vector.setZ(0);
                    return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector);
                })
                .flatMap(this::updateCoordinateSystem)
                .as(bindToLifecycle())
                .subscribe(success -> {
                    mIsMovingSubject.onNext(false);

                    CalibrationLaserActivity activity = (CalibrationLaserActivity) getActivity();
                    if (activity != null) {
                        activity.gotoLaser4AxisSetOrigin();
                    }
                }, e -> {
                    mIsMovingSubject.onNext(false);
                    LogHelper.log(e);
                });
    }
}
