package fabscreen.features.print.print;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.BaseApplication;
import fabscreen.platform.base.Constants;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.print.PrintListener;
import fabscreen.platform.base.model.system.MachineStatusManager;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.controller.PrintController;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.base.view.CircularProgressView;
import fabscreen.platform.base.view.FabConfirm;
import fabscreen.platform.core.ui.presenter.PrintDetailPanelWidgetPresenter;
import fabscreen.platform.core.ui.view.FabFullScreenDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.SingleSubject;

public class PrintFragment extends BaseFragment {
    private static final int STATUS_IDLE = 0;
    private static final int STATUS_PRINTING = 1;
    private static final int STATUS_PAUSED = 2;
    private static final int STATUS_COMPLETED = 3;
    // progress
    @BindView(R2.id.cpv_print_progress)
    CircularProgressView mCpvProgress;
    @BindView(R2.id.tv_print_progress)
    TextView mTvProgress;
    @BindView(R2.id.tv_print_remaining_time_value)
    TextView mTvRemainingTime;
    // basic info
    @BindView(R2.id.tv_print_file_name)
    TextView mTvFilename;
    // detail panel
    @BindView(R2.id.widget_detail_panel_3dp)
    View mViewDetailPanel3DP;
    @BindView(R2.id.widget_detail_panel_laser)
    View mViewDetailPanelLaser;
    @BindView(R2.id.widget_detail_print_cnc)
    View mViewDetailPanelCNC;
    // buttons
    @BindView(R2.id.btn_print_start)
    Button mBtnStart;
    @BindView(R2.id.btn_print_pause)
    Button mBtnPause;
    @BindView(R2.id.btn_print_stop)
    Button mBtnStop;
    @BindView(R2.id.btn_print_complete)
    Button mBtnComplete;
    private PrintDetailPanelWidgetPresenter mDetailPanelWidgetPresenter;
    private String mFilename;
    private int mHeadType;
    private float mEstimatedTime = 0;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    // indicates the the machine is moving, hence buttons should be disabled temporarily.
    private BehaviorSubject<Boolean> mWaitingSubject = BehaviorSubject.createDefault(false);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mDetailPanelWidgetPresenter = new PrintDetailPanelWidgetPresenter();
        mDetailPanelWidgetPresenter.bind(view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // get parameters
        mFilename = ServiceContainer.getInstance().getService(IAppService.class).getWorkspace().getFileName();
        mEstimatedTime = ServiceContainer.getInstance().getService(IAppService.class).getWorkspace().getEstimatedTime();

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_print;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @Override
    protected void back() {
        mCompositeDisposable.clear();
        super.back();
    }

    @Override
    protected void backToHome(Class clazz) {
        mCompositeDisposable.clear();
        super.backToHome(clazz);
    }

    private void initView() {
        mHeadType = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId();

        // file name
        mTvFilename.setSelected(true);
        mTvFilename.setText(mFilename);

        // display views according to head type
        switch (mHeadType) {
            case Module.ModuleType.HEAD_3DP: {
                mViewDetailPanel3DP.setVisibility(View.VISIBLE);
                mViewDetailPanelLaser.setVisibility(View.GONE);
                mViewDetailPanelCNC.setVisibility(View.GONE);
                break;
            }
            case Module.ModuleType.HEAD_LASER:
            case Module.ModuleType.HEAD_LASER_10W: {
                mViewDetailPanel3DP.setVisibility(View.GONE);
                mViewDetailPanelLaser.setVisibility(View.VISIBLE);
                mViewDetailPanelCNC.setVisibility(View.GONE);
                break;
            }
            case Module.ModuleType.HEAD_CNC: {
                mViewDetailPanel3DP.setVisibility(View.GONE);
                mViewDetailPanelLaser.setVisibility(View.GONE);
                mViewDetailPanelCNC.setVisibility(View.VISIBLE);
                break;
            }
            default:
                break;
        }

        mDetailPanelWidgetPresenter.useElapsedTime();
        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().getCountObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(count -> {
                    mDetailPanelWidgetPresenter.setEstimatedTime(count);
                });

        // watch machine status and update detail panel
        MachineStatusManager.getMachineInfoHolder().getObservable()
                .throttleFirst(2000, Constants.THROTTLE_TIME_UNIT)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(status -> {
                    switch (mHeadType) {
                        case Module.ModuleType.HEAD_3DP: {
                            if (ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getOverrideNozzleTemperatureDirty()) {
                                float target = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getOverrideNozzleTemperature();
                                mDetailPanelWidgetPresenter.setNozzleTemp(status.leftNozzleTemperature, target);
                            } else {
                                mDetailPanelWidgetPresenter.setNozzleTemp(status.leftNozzleTemperature, status.leftNozzleTargetTemperature);
                            }
                            if (ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getOverrideHeatedBedTemperatureDirty()) {
                                float target = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getOverrideHeatedBedTemperature();
                                mDetailPanelWidgetPresenter.setHeatedBedTemp(status.bedTemperature, target);
                            } else {
                                mDetailPanelWidgetPresenter.setHeatedBedTemp(status.bedTemperature, status.bedTargetTemperature);
                            }
                            mDetailPanelWidgetPresenter.setFeedRatePerSecond(status.feedRate / 60);
                            break;
                        }
                        case Module.ModuleType.HEAD_LASER:
                        case Module.ModuleType.HEAD_LASER_10W: {
                            if (ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getOverrideLaserPowerDirty()) {
                                float target = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getOverrideLaserPower();
                                mDetailPanelWidgetPresenter.setLaserPower(target);
                            } else {
                                mDetailPanelWidgetPresenter.setLaserPower((float) status.laserPower);
                            }
                            mDetailPanelWidgetPresenter.setFeedRate(status.feedRate);
                            break;
                        }
                        case Module.ModuleType.HEAD_CNC: {
                            mDetailPanelWidgetPresenter.setFeedRate(status.feedRate);
                            mDetailPanelWidgetPresenter.setSpindleSpeed(status.spindleSpeed);
                            break;
                        }
                        default:
                            break;
                    }
                }, Throwable::printStackTrace);

        // buttons
        ServiceContainer.getInstance().getService(IMachine.class).getPrintController()
                .getPrintStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(status -> {
                    mBtnStart.setVisibility(status == STATUS_IDLE ? Button.VISIBLE : Button.GONE);
                    mBtnPause.setVisibility((status != STATUS_IDLE && status != STATUS_COMPLETED) ? Button.VISIBLE : Button.GONE);
                    mBtnStop.setVisibility((status != STATUS_IDLE && status != STATUS_COMPLETED) ? Button.VISIBLE : Button.GONE);
                    mBtnComplete.setVisibility(status == STATUS_COMPLETED ? Button.VISIBLE : Button.GONE);

                    if (status == STATUS_COMPLETED) {
                        updateProgress();
                    }
                }, LogHelper::log);

        // enable or disable buttons
        mWaitingSubject
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(waiting -> {
                    mBtnStart.setEnabled(!waiting);
                    mBtnPause.setEnabled(!waiting);
                    mBtnStop.setEnabled(!waiting);
                });

        // change pause button text when machine responded and not moving
        PrintController printController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();
        Observable
                .combineLatest(printController.getPrintStateObservable(), mWaitingSubject, (state, isMoving) -> {
                    if (state == STATUS_PAUSED && !isMoving) {
                        return TextStatus.CHANGE_TO_RESUME;
                    } else if (state == STATUS_PRINTING && !isMoving) {
                        return TextStatus.CHANGE_TO_PAUSE;
                    } else {
                        return TextStatus.NOT_CHANGE;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(status -> {
                    switch (status) {
                        case CHANGE_TO_RESUME: {
                            mBtnPause.setText(R.string.all_resume);
                            break;
                        }
                        case CHANGE_TO_PAUSE: {
                            mBtnPause.setText(R.string.all_pause);
                            break;
                        }
                        case NOT_CHANGE: {
                            break;
                        }
                    }
                }, Throwable::printStackTrace);

        // Wait 300ms until fragment is shown, then we can start printing
        final IFile printFile = ServiceContainer.getInstance().getService(IAppService.class).getWorkspace().getPrintFile();
        if (printFile == null || !printFile.exists() || printFile.length() == 0) {
            FabConfirm.create(getContext())
                    .setDescription(R.string.print_warning_open_unable)
                    .setConfirm(R.string.all_confirm, (dialog, which) -> {
                        dialog.dismiss();
                        ServiceContainer.getInstance().getService(IRouter.class).backHome().start(requireContext());
                    })
                    .show();
        } else {
            AndroidSchedulers.mainThread().scheduleDirect(this::startPrint, 300, Constants.TIME_UNIT);
        }
    }

    private Single<Boolean> checkFastCalibration() {
        SingleSubject<Boolean> resultSubject = SingleSubject.create();

        final int headType = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId();
        final int calibrationMode = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().get3DPCalibrationMode();
        boolean fastCalibrationOn = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().get3DPFastCalibrationOn();
        boolean powerOutageFlag = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getRecoveryFlag();

        // do not fast calibration if resuming print via power panic
        if (headType == Module.ModuleType.HEAD_3DP && !powerOutageFlag && calibrationMode == 0 && fastCalibrationOn) {
            // Fast Calibration may take a few minutes, we can heated up the bed simultaneously.
            // preHeatHeatedBed();

            Logger.d("Start Fast Calibration...");

            mWaitingSubject.onNext(true);

            ServiceContainer.getInstance().getService(IMachine.class).getFDMController().queryBedCalibrationStatus()
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(responseStructure -> {
//                        mWaitingSubject.onNext(false);
//                        if (retCode == 0) { // success
//                            resultSubject.onSuccess(true);
//                        } else if (retCode == 1) { // failed
//                            coolDownHeatedBed();
//
//                            Logger.w("Fast Calibration failed, ret code %d", retCode);
//
//                            FabFullScreenDialog dialog = FabFullScreenDialog.create(getContext());
//                            dialog.setMessage(R.string.calibration_failed);
//                            dialog.setPositive(R.string.all_ok, (v, which) -> {
//                                v.dismiss();
//                                back();
//                            });
//                            dialog.show();
//                        } else if (retCode == 2) { // not calibrated yet
//                            coolDownHeatedBed();
//
//                            Logger.w("Machine is not calibrated yet, ret code %d", retCode);
//
//                            FabFullScreenDialog dialog = FabFullScreenDialog.create(getContext());
//                            dialog.setMessage(R.string.calibration_missing_notice);
//                            dialog.setPositive(R.string.all_yes, (v, which) -> {
//                                v.dismiss();
//                                resultSubject.onSuccess(true);
//                            });
//                            dialog.setNegative(R.string.all_no, (v, which) -> {
//                                v.dismiss();
//                                back();
//                            });
//                            dialog.show();
//                        }
                    }, e -> {
                        coolDownHeatedBed();
                        mWaitingSubject.onNext(false);
                        LogHelper.log(e);
                    });
        } else {
            resultSubject.onSuccess(true);
        }

        return resultSubject.hide();
    }

    private void preHeatHeatedBed() {
        float temperature = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getOverrideInitialHeatedBedTemperature();

        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().requestAdjustSettingHeatedBedTemp(temperature)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(retCode -> {
                    // do nothing
                }, LogHelper::log);
    }

    private void coolDownHeatedBed() {
        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().requestAdjustSettingHeatedBedTemp(0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(retCode -> {
                    // do nothing
                }, LogHelper::log);
    }

    private void startPrint() {
        mCompositeDisposable.clear();

        PrintController printController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();
        printController.setFile(ServiceContainer.getInstance().getService(IAppService.class).getWorkspace().getPrintFile());
        printController.setTotalLines(ServiceContainer.getInstance().getService(IAppService.class).getWorkspace().getFileTotalLineCount());
        printController.setListener(new PrintListener() {
            @Override
            public void onStartSuccess() {
                // oh we started
                Logger.i("Print started.");
                mWaitingSubject.onNext(false);
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().reset();
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().start();
            }

            @Override
            public void onStartFailed(int retCode) {
                switch (retCode) {
                    case 202: {
                        Logger.w("Filament used out, unable to start printing.");
                        handleFilamentRunOut(result -> {
                            if (result == HandleFilamentRunOutCallback.RESULT_CANCEL) {
                                Logger.d("Load canceled, exiting.");
                                back();
                            }
                        });
                        break;
                    }
                    case 203: {
                        Logger.d("Unable to start printing, enclosure door open detected.");
                        handleEnclosureDoorPaused();
                        break;
                    }
                    default: {
                        Logger.w("Unable to start printing, ret code %d", retCode);
                        FabConfirm.create(getContext())
                                .setDescription(R.string.print_warning_start_unable)
                                .setConfirm(R.string.all_confirm, (dialog, which) -> {
                                    dialog.dismiss();
                                    ServiceContainer.getInstance().getService(IRouter.class).backHome().start(requireContext());
                                })
                                .show();
                        break;
                    }
                }
            }

            @Override
            public void onPauseSuccess() {
                Logger.i("Print paused.");
                mWaitingSubject.onNext(false);
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().stop();
            }

            @Override
            public void onPauseFailed(int retCode) {
                Logger.w("Unable to pause printing.");
                mWaitingSubject.onNext(false);
                // Just a confirm
                FabConfirm.create(getContext())
                        .setDescription(R.string.print_warning_pause_unable)
                        .setConfirm(R.string.all_confirm, (dialog, which) -> dialog.dismiss())
                        .show();
            }

            @Override
            public void onResumeSuccess() {
                Logger.i("Print resumed.");
                mWaitingSubject.onNext(false);
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().start();
            }

            @Override
            public void onResumeFailed(int retCode) {
                mWaitingSubject.onNext(false);
                switch (retCode) {
                    case 202: {
                        Logger.d("Filament used out, unable to resume printing.");
                        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().pauseOnFilamentUsedOut();
                        handleFilamentRunOut(null);
                        break;
                    }
                    case 203: {
                        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().pauseOnEnclosureDoorDetected();
                        handleEnclosureDoorPaused();
                        break;
                    }
                    default: {
                        Logger.w("Unable to resume printing.");
                        FabConfirm.create(getContext())
                                .setDescription(R.string.print_warning_resume_unable)
                                .setConfirm(R.string.all_confirm, (dialog, which) -> {
                                    dialog.dismiss();
                                })
                                .show();
                        break;
                    }
                }
            }

            @Override
            public void onResumeFromPowerOutageSuccess() {
                mWaitingSubject.onNext(false);
                // we resumed from power outage
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setPowerOutageFlag(false);
                Logger.i("Print recovered.");

                // clear flag when resume success
                Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().resetErrorFlag()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(success -> {
                            Logger.d("Error flag removed.");
                            ServiceContainer.getInstance().getService(IMachine.class).getPrintController().clearPowerOutageFlag();
                        }, LogHelper::log);
                mCompositeDisposable.add(sub);

                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().load();
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().start();
            }

            @Override
            public void onResumeFromPowerOutageFailed(int retCode) {

                mWaitingSubject.onNext(false);
                switch (retCode) {
                    case 202: {
                        Logger.d("Filament used out, failed to recover from power loss.");
                        handleFilamentRunOut(result -> {
                            if (result == HandleFilamentRunOutCallback.RESULT_CANCEL) {
                                Logger.d("Load canceled, exiting.");
                                // Clear power outage flag before exiting.
                                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setPowerOutageFlag(false);
                                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().resetErrorFlag()
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .as(bindToLifecycle())
                                        .subscribe(success -> {
                                            Logger.d("Error flag removed.");

                                            ServiceContainer.getInstance().getService(IMachine.class).getPrintController().clearPowerOutageFlag();
                                            back();
                                        }, e -> {
                                            LogHelper.log(e);
                                            back();
                                        });
                            }
                        });
                        break;
                    }
                    case 203: {
                        handleEnclosureDoorPaused();
                        break;
                    }
                    default: {
                        Logger.w("Failed to recover from power loss.");
                        FabConfirm.create(getContext())
                                .setDescription(R.string.print_warning_resume_unable)
                                .setConfirm(R.string.all_confirm, (dialog, which) -> {
                                    dialog.dismiss();
                                    ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setPowerOutageFlag(false);
                                    ServiceContainer.getInstance().getService(IRouter.class).backHome().start(requireContext());
                                })
                                .show();
                        break;
                    }
                }
            }

            @Override
            public void onStopSuccess() {
                mWaitingSubject.onNext(false);
                Logger.i("print stopped.");
                ServiceContainer.getInstance().getService(IRouter.class).backHome().start(requireContext());
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().stop();
            }

            @Override
            public void onStopFailed(int retCode) {
                Logger.w("Unable to stop printing, ret code %d", retCode);
                mWaitingSubject.onNext(false);
                FabConfirm.create(getContext())
                        .setDescription(R.string.print_warning_stop_unable)
                        .setConfirm(R.string.all_confirm, (dialog, which) -> {
                            dialog.dismiss();
                            ServiceContainer.getInstance().getService(IRouter.class).backHome().start(requireContext());
                        })
                        .show();
            }

            @Override
            public void onFinishSuccess() {
                Logger.i("Print Finished.");
                mWaitingSubject.onNext(false);

                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().stop();
                Logger.d("Print job costs %s.", BaseApplication.formatTime(ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().getCount()));
            }

            @Override
            public void onFinishFailed(int retCode) {
                Logger.w("Unable to finish printing, ret code %d", retCode);
                mWaitingSubject.onNext(false);
                FabConfirm.create(getContext())
                        .setDescription(R.string.print_warning_finish_unable)
                        .setConfirm(R.string.all_confirm, (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            }
        });

        // Power Panic
        mWaitingSubject.onNext(true);
        boolean powerOutageFlag = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getRecoveryFlag();
        // Set the master monitor to pause
        Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getPauseState()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(paceuseState -> {
                    if (paceuseState == 0) {
                        printController.masterPause();
                    }
                }, LogHelper::log);
        mCompositeDisposable.add(sub);

        if (powerOutageFlag) {
            printController.recover();
        } else {
            printController.start();
        }

        // reset filament flag before print start.
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().clearFilamentOutFlag();

        // Watch errors from slave computer
        sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineController()
                .getFilamentObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isFilamentOut -> {
                    if (isFilamentOut) {
                        Logger.d("Filament out detected.");
                        mWaitingSubject.onNext(true);
                        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().pauseOnFilamentUsedOut();
                        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().stop();
                        handleFilamentRunOut(result -> {
                            if (result == HandleFilamentRunOutCallback.RESULT_CANCEL) {
                                Logger.d("Load canceled, ready to resume.");
                                mWaitingSubject.onNext(false);
                            }
                        });
                    }
                }, LogHelper::log);
        mCompositeDisposable.add(sub);
        // reset door detection flag before print start.
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure().clearEnclosureDoorFlag();

        // watch enclosure door detection
        sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(machineInfo -> {
                    if (ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure().getEnclosureStatusValue().isDoorDetectionEnabled() && machineInfo.isEnclosureAvailable) {
                        mWaitingSubject.onNext(true);
                        printController.pauseOnEnclosureDoorDetected();
                        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().stop();
                        handleEnclosureDoorPaused();
                    }
                });
        mCompositeDisposable.add(sub);

        // check air purifier flag
        if (ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isAirPurifierAvailable) {
            boolean airPurifierAutoTurnOnFlag;
            switch (mHeadType) {
                case Module.ModuleType.HEAD_3DP:
                    airPurifierAutoTurnOnFlag = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getAirPurifier3DPAutoFlag();
                    break;
                case Module.ModuleType.HEAD_LASER:
                case Module.ModuleType.HEAD_LASER_10W:
                    airPurifierAutoTurnOnFlag = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getAirPurifierLaserAutoFlag();
                    break;
                case Module.ModuleType.HEAD_CNC:
                    airPurifierAutoTurnOnFlag = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getAirPurifierCNCAutoTurnOnFlag();
                    break;
                default:
                    airPurifierAutoTurnOnFlag = false;
                    break;
            }

            if (airPurifierAutoTurnOnFlag) {
                ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().setAirPurifierEnabled(true)
                        .observeOn(AndroidSchedulers.mainThread())
                        .as(bindToLifecycle())
                        .subscribe(success -> {
                            if (!success) {
                                Logger.w("Open Air Purifier failed!");
                            }
                        }, LogHelper::log);
            }
        }

        sub = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getResumeObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(resume -> resumeFromChangeFilament());
        mCompositeDisposable.add(sub);

        // Update
        sub = Observable.interval(5, TimeUnit.SECONDS)
                .takeUntil(tick -> ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getPrintState() == STATUS_COMPLETED)
                .filter(tick -> ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getPrintState() == STATUS_PRINTING)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(tick -> {
                    updateProgress();
                }, Throwable::printStackTrace);
        mCompositeDisposable.add(sub);
    }

    private void updateProgress() {
        float p = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getProgress();

        // formula: remaining = (1 - p) * p * elapsed / p + (1 - p) * (1 - p) * ETA
        int elapsed = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().getCount();
        int remaining = (int) ((1 - p) * elapsed + (1 - p) * (1 - p) * mEstimatedTime);
        mTvRemainingTime.setText(BaseApplication.formatTime(remaining));

        final int percentage = (int) (100 * p);
        mCpvProgress.setPercentage(percentage);
        mTvProgress.setText(String.valueOf(percentage));
    }

    private void handleFilamentRunOut(@Nullable HandleFilamentRunOutCallback handleFilamentRunOutCallback) {
        Logger.i("Filament has run out.");

        FabFullScreenDialog.create(getContext())
                .setIcon(R.drawable.pic_warning_filament_runout_120x120)
                .setTitle(R.string.print_warning_filament_run_out_title)
                .setMessage(getString(R.string.print_warning_filament_run_out_content))
                .setPositive(R.string.print_ready_to_load, (dialog, which) -> {
                    Logger.d("Ready to load filament.");
                    dialog.dismiss();
                    if (handleFilamentRunOutCallback != null) {
                        handleFilamentRunOutCallback.onCallbackResult(HandleFilamentRunOutCallback.RESULT_LOAD);
                    }
                    PrintActivity activity = (PrintActivity) getContext();
                    if (activity != null) {
                        activity.gotoChangeFilamentFragment();
                    }
                })
                .setNegative(R.string.all_cancel, (dialog, which) -> {
                    dialog.dismiss();
                    if (handleFilamentRunOutCallback != null) {
                        handleFilamentRunOutCallback.onCallbackResult(HandleFilamentRunOutCallback.RESULT_CANCEL);
                    }
                })
                .show();
    }

    private void resumeFromChangeFilament() {
        int printState = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getPrintState();

        mWaitingSubject.onNext(true);
        if (printState == STATUS_IDLE) { // IDLE
            // Since we didn't start work yet, set the nozzle temperature back to 0°C.
            ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("M104 S0")
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(response -> { /**/ });

            ServiceContainer.getInstance().getService(IMachine.class).getMachineController().clearFilamentOutFlag();

            boolean powerOutageFlag = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getRecoveryFlag();
            if (powerOutageFlag) {
                Logger.i("Complete change filament, recover printing.");
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().recover();
            } else {
                Logger.i("Complete change filament, re-start previous job.");
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().start();
            }
        } else if (printState == STATUS_PAUSED) { // PAUSE
            Logger.i("Complete change filament, resume previous job.");
            ServiceContainer.getInstance().getService(IMachine.class).getMachineController().clearFilamentOutFlag();
            ServiceContainer.getInstance().getService(IMachine.class).getPrintController().resume();
        }
    }

    private void handleEnclosureDoorPaused() {
        FabFullScreenDialog.create(getContext())
                .setIcon(R.drawable.pic_warning_enclosure_120x120)
                .setTitle(R.string.print_warning_enclosure_door_detection_title)
                .setMessage(getString(R.string.print_warning_enclosure_door_detection_content))
                .setPositive(R.string.all_confirm, (dialog, which) -> {
                    dialog.dismiss();
                    mWaitingSubject.onNext(false);
                })
                .show();
    }

    @OnClick(R2.id.btn_print_start)
    void onClickStart() {
        playNormalClickSound();
        if (mHeadType == Module.ModuleType.HEAD_LASER_10W) {
            // check Header Security Status before startPrint
            ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getHeaderSecurityStatus()
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(headerSecurity -> {
                        if (headerSecurity.status == 0) {
                            startPrint();
                        }
                    });
        } else {
            startPrint();
        }
    }

    @OnClick(R2.id.btn_print_pause)
    void onClickPause() {
        playNormalClickSound();
        PrintController printController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();

        switch (printController.getPrintState()) {
            case STATUS_PRINTING: {
                FabFullScreenDialog.create(getContext())
                        .setIcon(R.drawable.pic_dialog_warning_72x72)
                        .setTitle(R.string.all_warning)
                        .setMessage(R.string.print_warning_pause)
                        .setPositive(R.string.all_pause, (dialog, which) -> {
                            dialog.dismiss();
                            mWaitingSubject.onNext(true);
                            printController.pause();
                        })
                        .setNegative(R.string.all_cancel, (dialog, which) -> dialog.dismiss())
                        .show();
                break;
            }
            case STATUS_PAUSED: {
                mWaitingSubject.onNext(true);
                if (mHeadType == Module.ModuleType.HEAD_LASER_10W) {
                    // check Header Security Status before resume
                    ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getHeaderSecurityStatus()
                            .observeOn(AndroidSchedulers.mainThread())
                            .as(bindToLifecycle())
                            .subscribe(headerSecurity -> {
                                if (headerSecurity.status == 0) {
                                    printController.resume();
                                }
                            }, e -> {
                                mWaitingSubject.onNext(false);
                                Logger.d(e);
                            });
                } else {
                    printController.resume();
                }
                break;
            }
            case STATUS_IDLE: {
                break;
            }
        }
    }

    @OnClick(R2.id.btn_print_stop)
    void onClickStop() {
        playNormalClickSound();
        FabFullScreenDialog.create(getContext())
                .setIcon(R.drawable.pic_dialog_warning_72x72)
                .setTitle(R.string.all_warning)
                .setMessage(R.string.print_warning_stop)
                .setPositive(R.string.all_yes, (dialog, which) -> {
                    dialog.dismiss();

                    mWaitingSubject.onNext(true);

                    PrintController printController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();
                    printController.stop();
                })
                .setNegative(R.string.all_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    @OnClick(R2.id.btn_print_complete)
    void onClickComplete() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class).backHome().start(requireContext());
    }

    private enum TextStatus {
        NOT_CHANGE,
        CHANGE_TO_RESUME,
        CHANGE_TO_PAUSE
    }

    interface HandleFilamentRunOutCallback {
        int RESULT_CANCEL = 0;
        int RESULT_LOAD = 1;

        void onCallbackResult(int result);
    }
}
