package fabscreen.features.machinetools.calibration.s20.laser.rotary;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.s20.laser.CalibrationLaserActivity;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.model.LaserPattern;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.model.LaserFineTuneExecutor;
import fabscreen.platform.core.ui.common.LaserCalibrationViewModel;
import fabscreen.platform.core.ui.view.FabAlert;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class LaserCalibration4AxisAutoFocusStep2Fragment extends BaseFragment {
    private final static int STATUS_IDLE = 0;
    private final static int STATUS_LASER_TEST = 1;
    private final static int STATUS_PROCESSING = 2;
    private final static int STATUS_COMPLETE = 3;
    private final static int STATUS_ERROR = 4;
    @BindView(R2.id.top_bar_back)
    Button mBtnBack;
    @BindView(R2.id.tv_guide_content)
    TextView mTvContent;
    @BindView(R2.id.btn_guide_next)
    Button mBtnComplete;
    private LaserCalibrationViewModel mViewModel;
    private LaserPattern mPattern;
    private BehaviorSubject<Integer> mCalibrationStatusSubject = BehaviorSubject.createDefault(STATUS_IDLE);
    private BehaviorSubject<Integer> mCalibrationIndexSubject = BehaviorSubject.createDefault(-1);
    private float mInitialZ;
    private float mFocalLength = 0;

    public static LaserCalibration4AxisAutoFocusStep2Fragment newInstance() {
        return new LaserCalibration4AxisAutoFocusStep2Fragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = getViewModel();
        mPattern = mViewModel.getLaserPattern();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_guide_laser_auto_focus_step2;
    }

    @Override
    protected LaserCalibrationViewModel getViewModel() {
        return getViewModelProvider().get(LaserCalibrationViewModel.class);
    }

    private void initView() {
        mCalibrationStatusSubject
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(status -> {
                    switch (status) {
                        case STATUS_IDLE:
                        case STATUS_LASER_TEST:
                        case STATUS_PROCESSING:
                            mBtnBack.setVisibility(View.GONE);
                            mBtnComplete.setVisibility(View.VISIBLE);
                            mBtnComplete.setEnabled(false);
                            mBtnComplete.setText(R.string.laser_calibration_processing);
                            break;
                        case STATUS_COMPLETE:
                            mBtnBack.setVisibility(View.VISIBLE);
                            mBtnComplete.setVisibility(Button.VISIBLE);
                            mBtnComplete.setEnabled(true);
                            mBtnComplete.setText(R.string.all_complete);
                            break;
                        case STATUS_ERROR:
                            mBtnBack.setVisibility(View.VISIBLE);
                            mBtnComplete.setVisibility(Button.VISIBLE);
                            mBtnComplete.setEnabled(true);
                            mBtnComplete.setText(R.string.laser_calibration_failed);
                            mTvContent.setText(R.string.laser_calibration_auto_focus_failed);
                            break;
                    }
                });

        mCalibrationIndexSubject
                .filter(index -> index != -1)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(index -> {
                    if (index < 5) {
                        mTvContent.setText(R.string.laser_calibration_auto_focus_complete_1);
                    } else if (index <= 15) {
                        mTvContent.setText(R.string.laser_calibration_auto_focus_complete);
                    } else {
                        mTvContent.setText(R.string.laser_calibration_auto_focus_complete_2);
                    }
                });

        if (mPattern == null) {
            return;
        }
        start();
    }

    private void start() {
        // Assume we are at CS#1
        // Save initial z coordinate
        ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(machineStatus -> {
                    mInitialZ = machineStatus.currentPosition.getZ() - machineStatus.originOffset.getZ(); // calculate absolute Z
                    startCalibration();
                }, e -> {
                    Logger.e("Unable to get machine status.");
                    mCalibrationStatusSubject.onNext(STATUS_ERROR);
                });
    }

    /**
     * Start auto calibration:
     * 1. engraving laser pattern with LaserFineTuneExecutor
     * 2. Move tool head to take photo, process the photo to determine which line engraved thinnest.
     */
    private void startCalibration() {
        mCalibrationStatusSubject.onNext(STATUS_LASER_TEST);
        LaserFineTuneExecutor executor = new LaserFineTuneExecutor(disposables);
        executor.setLaserPattern(mViewModel.getLaserPattern());
        mCalibrationIndexSubject.onNext(-1);
        executor.startFineTune()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    if (success) {
                        mCalibrationStatusSubject.onNext(STATUS_PROCESSING);
                        cameraAidFocalDetection();
                    } else {
                        mCalibrationStatusSubject.onNext(STATUS_ERROR);
                    }
                }, e -> {
                    LogHelper.log(e);
                    mCalibrationStatusSubject.onNext(STATUS_ERROR);
                });
    }

    private void turnOffLight() {
        boolean cameraLightOn = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserCameraLightOn();
        if (cameraLightOn) {
            ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().setCameraAutoWhiteBalance(false)
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(success -> {
                        // do nothing
                    }, LogHelper::log);
        }
    }

    /**
     * Camera Aid Focal Detection.
     */
    private void cameraAidFocalDetection() {
//        final float bottomZ = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserBottomZ();
//        float cameraZ = bottomZ + 80.f;
//        Vector vector1 = new Vector();
//        vector1.setX(-Constants.LASER_CAMERA_OFFSET_X);
//        vector1.setY(-Constants.LASER_CAMERA_OFFSET_Y);
//        vector1.setZ(cameraZ - mInitialZ);
//        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().gotoRelativePosition(vector1)
//                .flatMap(success -> {
//                    Log.e("DEBUG", "move result:" + success);
//                    return ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().requestCapturePhoto();
//                })
//                .flatMap(success -> {
//                    Log.e("DEBUG", "capture result:" + success);
//                    return ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().watchPhotoReceive();
//                })
//                .subscribeOn(Schedulers.computation())
//                .map(bitmap -> {
//                    Logger.d("Capture image succeed.");
//                    String path = ServiceContainer.getInstance().getService(IAppService.class).getCacheDir() + "/capture.jpg";
//
//                    FileOutputStream out = new FileOutputStream(path);
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//
//                    turnOffLight();
//                    return LaserCalibrationProcess.process(getContext(), bitmap, LaserPattern.DIRECTION_Y);
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .as(bindToLifecycle())
//                .subscribe(index -> {
//                    if (index == -1) {
//                        mCalibrationStatusSubject.onNext(STATUS_ERROR);
//                        Logger.w("Detection failed");
//                    } else {
//                        float materialHeight = mViewModel.getWorkpieceDiameter() / 2f;
//                        float offset = (-10 * Constants.LASER_TEST_PATTERN_Z_DIFF + index * Constants.LASER_TEST_PATTERN_Z_DIFF);
//                        mFocalLength = mInitialZ + offset - materialHeight;
//                        Logger.d("mFocalLength is " + mFocalLength);
//
//                        mCalibrationIndexSubject.onNext(index);
//
//                        // Move to new height (focal length + material height)
//                        ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 X0 Y0 F3000")
//                                .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 Z" + offset + " F1800"))
//                                .flatMap(res -> {
//                                    Vector vector = new Vector();
//                                    vector.setZ(0);
//                                    return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector);
//                                })
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .as(bindToLifecycle())
//                                .subscribe(response -> mCalibrationStatusSubject.onNext(STATUS_COMPLETE), LogHelper::log);
//                    }
//                }, e -> {
//                    LogHelper.log(e);
//                    Logger.w("Capture image failed.");
//                    mCalibrationStatusSubject.onNext(STATUS_ERROR);
//                });
    }

    private void finish() {
        Logger.d("Laser Calibration finished.");
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @OnClick(R2.id.btn_guide_next)
    void onClickNext() {
        playNormalClickSound();
        if (mCalibrationStatusSubject.getValue() == STATUS_COMPLETE) {
            ServiceContainer.getInstance().getService(IMachine.class).getLaserController()
                    .setFocalLength(mFocalLength)
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(responseStructure -> {
                        if (responseStructure.isSuccess()) {
                            finish();
                        } else {
                            FabAlert.alert(getContext(), R.string.laser_alert_failed_to_get_focal_length);
                            finish();
                        }
                    }, e -> {
                        LogHelper.log(e);
                        FabAlert.alert(getContext(), R.string.laser_alert_failed_to_get_focal_length);
                        finish();
                    });
        } else {
            // Goto manual pick when failed
            if (getActivity() != null) {
                ((CalibrationLaserActivity) getActivity()).goto4AxisManualFineTunePick();
            }
        }
    }
}
