package fabscreen.features.print.print;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.Constants;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.FabLocalFile;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.print.PrintListener;
import fabscreen.platform.base.model.system.MachineStatusManager;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.PrintController;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.FabConfirm;
import fabscreen.platform.core.ui.view.FabAlert;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

public class PrintLandFragment extends BaseFragment {
    private static final int STATUS_IDLE = 0;
    private static final int STATUS_PRINTING = 1;
    private static final int STATUS_PAUSED = 2;
    private static final int STATUS_COMPLETED = 3;

    @BindView(R2.id.tv_print_file_name)
    TextView mTvFilename;
    @BindView(R2.id.tv_print_remaining_time)
    TextView mTvRemainingTime;
    @BindView(R2.id.tv_print_progress)
    TextView mTvProgress;
    @BindView(R2.id.btn_print_control_pause)
    Button mBtnPause;
    @BindView(R2.id.btn_print_control_resume)
    Button mBtnResume;
    @BindView(R2.id.btn_print_control_stop)
    Button mBtnStop;

    @BindView(R2.id.btn_right_temperature)
    Button mBtnRightTemp;
    @BindView(R2.id.btn_left_temperature)
    Button mBtnLeftTemp;
    @BindView(R2.id.btn_print_progress)
    Button mBtnProgress;
    @BindView(R2.id.btn_bed_temperature)
    Button mBtnBedTemp;

    // temporary treat ImageView as button
    @BindView(R2.id.iv_print_bottom_control_enclosure)
    ImageView mBtnEnclosure;
    @BindView(R2.id.iv_print_bottom_control_fan1)
    ImageView mBtnFan1;
    @BindView(R2.id.iv_print_bottom_control_speed)
    ImageView mBtnSpeed;
    @BindView(R2.id.iv_print_bottom_control_fan2)
    ImageView mBtnFan2;
    @BindView(R2.id.iv_print_bottom_control_air_purifier)
    ImageView mBtnAirPurifier;

    private int mHeadType;
    // Mock data temporary.
    private float mEstimatedTime = 8263;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    // indicates the the machine is moving, hence buttons should be disabled temporarily.
    private BehaviorSubject<Boolean> mWaitingSubject = BehaviorSubject.createDefault(false);

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle("返回主页(功能开发中，非完成状态）");

        initPrint();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_print;
    }

    @Override
    protected void back() {
//        ServiceContainer.getInstance().getService(IRouter.class).routeToS30Home().start(requireContext());
        super.back();
    }

    void initPrint() {
        PrintController printController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();

        boolean isPrinting = (printController.getPrintState() == PrintController.STATE_PRINTING || printController.getPrintState() == PrintController.STATE_PAUSED);

        if (isPrinting) {
            // Initializing from last printing
            resumePrintFromHome();

        } else {
            // Initialize a new print job.
//            final IFile printFile = ServiceContainer.getInstance().getService(IAppService.class).getWorkspace().getPrintFile();
//            if (printFile == null || !printFile.exists() || printFile.length() == 0) {
//                FabConfirm.create(getContext())
//                        .setDescription(R.string.print_warning_open_unable)
//                        .setConfirm(R.string.all_confirm, (dialog, which) -> {
//                            dialog.dismiss();
//                            ServiceContainer.getInstance().getService(IRouter.class).backHome().start(requireContext());
//                        })
//                        .show();
//            } else {
            AndroidSchedulers.mainThread().scheduleDirect(this::startPrint, 300, Constants.TIME_UNIT);
//            }
        }

        ServiceContainer.getInstance().getService(IMachine.class).getPrintController()
                .getPrintStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(status -> {
                    Logger.d("print state " + status);
//                    mBtnStart.setVisibility(status == STATUS_IDLE ? Button.VISIBLE : Button.GONE);
                    mBtnPause.setVisibility(status == STATUS_PRINTING ? Button.VISIBLE : Button.GONE);
                    mBtnResume.setVisibility(status == STATUS_PAUSED ? Button.VISIBLE : Button.GONE);
                    mBtnStop.setVisibility((status != STATUS_IDLE && status != STATUS_COMPLETED) ? Button.VISIBLE : Button.GONE);
//                    mBtnComplete.setVisibility(status == STATUS_COMPLETED ? Button.VISIBLE : Button.GONE);

                    if (status == STATUS_COMPLETED) {
                        updateProgress();
                    }
                }, LogHelper::log);

        // enable or disable buttons
        mWaitingSubject
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(waiting -> {
//                    mBtnStart.setEnabled(!waiting);
                    mBtnPause.setEnabled(!waiting);
                    mBtnResume.setEnabled(!waiting);
                    mBtnStop.setEnabled(!waiting);
                });

        MachineStatusManager.getMachineInfoHolder().getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(status -> {
                    mBtnBedTemp.setText(String.valueOf(status.bedTemperature));
                    mBtnRightTemp.setText(String.valueOf(status.rightNozzleTemperature));
                    mBtnLeftTemp.setText(String.valueOf(status.leftNozzleTemperature));
                });

        updateProgress();
    }

    public void resumePrintFromHome() {
//        PrintController printController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();
//        setPrintControllerListener(printController);

        Disposable sub;
        // Update
        sub = Observable.interval(2, TimeUnit.SECONDS)
                .takeUntil(tick -> ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getPrintState() == STATUS_COMPLETED)
                .filter(tick -> ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getPrintState() == STATUS_PRINTING)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(tick -> {
                    updateProgress();
                }, Throwable::printStackTrace);
        mCompositeDisposable.add(sub);
    }

    private void startPrint() {
        mCompositeDisposable.clear();

//        PrintController printController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();
//        printController.setFile(ServiceContainer.getInstance().getService(IAppService.class).getWorkspace().getPrintFile());
//        printController.setTotalLines(ServiceContainer.getInstance().getService(IAppService.class).getWorkspace().getFileTotalLineCount());

        // Mock for debug
        PrintController printController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();
        File file = new File(ServiceContainer.getInstance().getService(IAppService.class).getFilesDir().getAbsolutePath() + "/alan_original_3dp_assortment_box_1x1_v4_20200806.gcode");
        IFile file1 = new FabLocalFile(file);
        printController.reset();
        printController.setFile(file1);
        printController.setTotalLines(166822);
        setPrintControllerListener(printController);

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

        // Temporary comment add-on logic
        /*
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
//                        handleFilamentRunOut(result -> {
//                            if (result == PrintFragment.HandleFilamentRunOutCallback.RESULT_CANCEL) {
//                                Logger.d("Load canceled, ready to resume.");
//                                mWaitingSubject.onNext(false);
//                            }
//                        });
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
                    if (ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure().getEnclosureStatusStatusValue().isDoorDetectionEnabled && machineInfo.isEnclosureAvailable) {
                        mWaitingSubject.onNext(true);
                        printController.pauseOnEnclosureDoorDetected();
                        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().stop();
//                        handleEnclosureDoorPaused();
                    }
                });
        mCompositeDisposable.add(sub);

        // check air purifier flag
        if (ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isAirPurifierAvailable) {
            boolean airPurifierAutoTurnOnFlag;
            switch (mHeadType) {
                case Toolhead.ToolheadType.HEAD_3DP:
                    airPurifierAutoTurnOnFlag = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getAirPurifier3DPAutoFlag();
                    break;
                case Toolhead.ToolheadType.HEAD_LASER:
                case Toolhead.ToolheadType.HEAD_LASER_10W:
                    airPurifierAutoTurnOnFlag = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getAirPurifierLaserAutoFlag();
                    break;
                case Toolhead.ToolheadType.HEAD_CNC:
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
        */

        sub = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getResumeObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(resume -> {
//                    resumeFromChangeFilament()
                });
        mCompositeDisposable.add(sub);

        // Update
        sub = Observable.interval(2, TimeUnit.SECONDS)
                .takeUntil(tick -> ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getPrintState() == STATUS_COMPLETED)
                .filter(tick -> ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getPrintState() == STATUS_PRINTING)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(tick -> {
                    updateProgress();
                }, Throwable::printStackTrace);
        mCompositeDisposable.add(sub);
    }

    private void setPrintControllerListener(PrintController printController) {
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
//                        handleFilamentRunOut(result -> {
//                            if (result == PrintFragment.HandleFilamentRunOutCallback.RESULT_CANCEL) {
//                                Logger.d("Load canceled, exiting.");
//                                back();
//                            }
//                        });
                        break;
                    }
                    case 203: {
                        Logger.d("Unable to start printing, enclosure door open detected.");
//                        handleEnclosureDoorPaused();
                        break;
                    }
                    default: {
                        Logger.w("Unable to start printing, ret code %d", retCode);
                        FabConfirm.create(getContext())
                                .setDescription(R.string.print_warning_start_unable)
                                .setConfirm(R.string.all_confirm, (dialog, which) -> {
                                    dialog.dismiss();
//                                    ServiceContainer.getInstance().getService(IRouter.class).routeToS30Home().start(requireContext());
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
//                        handleFilamentRunOut(null);
                        break;
                    }
                    case 203: {
                        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().pauseOnEnclosureDoorDetected();
//                        handleEnclosureDoorPaused();
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
//                        handleFilamentRunOut(result -> {
//                            if (result == PrintFragment.HandleFilamentRunOutCallback.RESULT_CANCEL) {
//                                Logger.d("Load canceled, exiting.");
//                                // Clear power outage flag before exiting.
//                                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setPowerOutageFlag(false);
//                                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().resetErrorFlag()
//                                        .observeOn(AndroidSchedulers.mainThread())
//                                        .as(bindToLifecycle())
//                                        .subscribe(success -> {
//                                            Logger.d("Error flag removed.");
//                                            ServiceContainer.getInstance().getService(IMachine.class).getPrintController().clearPowerOutageFlag();
//                                            back();
//                                        }, e -> {
//                                            LogHelper.log(e);
//                                            back();
//                                        });
//                            }
//                        });
                        break;
                    }
                    case 203: {
//                        handleEnclosureDoorPaused();
                        break;
                    }
                    default: {
                        Logger.w("Failed to recover from power loss.");
                        FabConfirm.create(getContext())
                                .setDescription(R.string.print_warning_resume_unable)
                                .setConfirm(R.string.all_confirm, (dialog, which) -> {
                                    dialog.dismiss();
                                    ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setPowerOutageFlag(false);
//                                    ServiceContainer.getInstance().getService(IRouter.class).routeToS30Home().start(requireContext());
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
//                ServiceContainer.getInstance().getService(IRouter.class).routeToS30Home().start(getContext());
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
//                            ServiceContainer.getInstance().getService(IRouter.class).routeToS30Home().start(requireContext());
                        })
                        .show();
            }

            @Override
            public void onFinishSuccess() {
                Logger.i("Print Finished.");
                mWaitingSubject.onNext(false);

                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().stop();
//                Logger.d("Print job costs %s.", BaseApplication.formatTime(ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().getCount()));
                // Finish Print.
                ((PrintActivity) requireActivity()).gotoPrintCompleteFragment();
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
    }

    private void updateProgress() {
        float p = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getProgress();

        // formula: remaining = (1 - p) * p * elapsed / p + (1 - p) * (1 - p) * ETA
        int elapsed = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().getCount();
        int remaining = (int) ((1 - p) * elapsed + (1 - p) * (1 - p) * mEstimatedTime);
        mTvRemainingTime.setText(String.format("剩余时间： %s", formatTime(remaining)));

        final int percentage = (int) (100 * p);
        mTvProgress.setText("进度：" + percentage);
        mBtnProgress.setText(String.valueOf(percentage));
    }

    public static String formatTime(double time) {
        int hour = (int) (time) / 3600;
        int minute = ((int) (time) % 3600) / 60;
        int second = ((int) (time) % 60);

        if (hour < 1) {
            return ServiceContainer.getInstance().getService(IAppService.class).getApp().getString(fabscreen.platform.base.R.string.date_helper_format_time_minute_second, minute, second);
        } else {
            return ServiceContainer.getInstance().getService(IAppService.class).getApp().getString(fabscreen.platform.base.R.string.date_helper_format_time_hour_minute, hour, minute);
        }
    }

    @OnClick(R2.id.btn_print_control_stop)
    void onClickControlStop() {
        playNormalClickSound();
        PrintController printController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();

        mWaitingSubject.onNext(true);
        printController.stop();
    }

    @OnClick(R2.id.btn_print_control_pause)
    void onClickControlPause() {
        playNormalClickSound();
        PrintController printController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();
        mWaitingSubject.onNext(true);
        printController.pause();

    }

    @OnClick(R2.id.btn_print_control_resume)
    void onClickControlResume() {
        playNormalClickSound();
        PrintController printController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();
        mWaitingSubject.onNext(true);
        printController.resume();
    }

    @OnClick(R2.id.btn_print_trigger_filament)
    void onTriggerFilament() {
        playNormalClickSound();
        ((PrintActivity) requireActivity()).gotoPrintFilamentChangedFragment();
    }

    @OnClick(R2.id.btn_right_temperature)
    void onClickRightTemp() {
        playNormalClickSound();
        FabAlert.alert(getContext(), "接口暂未实现");
    }

    @OnClick(R2.id.btn_left_temperature)
    void onClickLeftTemp() {
        playNormalClickSound();
        FabAlert.alert(getContext(), "接口暂未实现");
    }

    @OnClick(R2.id.btn_bed_temperature)
    void onClickBedTemp() {
        playNormalClickSound();
        FabAlert.alert(getContext(), "接口暂未实现");
    }

    @OnClick(R2.id.iv_print_bottom_control_enclosure)
    void onClickEnclosure() {
        playNormalClickSound();
        FabAlert.alert(getContext(), "接口暂未实现");
    }

    @OnClick(R2.id.iv_print_bottom_control_fan1)
    void onClickFan1() {
        playNormalClickSound();
        FabAlert.alert(getContext(), "接口暂未实现");
    }

    @OnClick(R2.id.iv_print_bottom_control_speed)
    void onClickSpeed() {
        playNormalClickSound();
        FabAlert.alert(getContext(), "接口暂未实现");
    }

    @OnClick(R2.id.iv_print_bottom_control_fan2)
    void onClickFan2() {
        playNormalClickSound();
        FabAlert.alert(getContext(), "接口暂未实现");
    }

    @OnClick(R2.id.iv_print_bottom_control_air_purifier)
    void onClickAirPurifier() {
        playNormalClickSound();
        FabAlert.alert(getContext(), "接口暂未实现");
    }
}

