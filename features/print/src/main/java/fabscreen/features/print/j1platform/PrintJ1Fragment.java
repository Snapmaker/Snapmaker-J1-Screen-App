package fabscreen.features.print.j1platform;

import static fabscreen.platform.base.RoutePath.PRINT_PRINT_J1_AJUSTMENT_MENT_CONTAINER;
import static fabscreen.platform.base.service.machine.controller.MachineOperationStatus.SYSTEM_STATUS_COMPLETED;
import static fabscreen.platform.base.service.machine.controller.MachineOperationStatus.SYSTEM_STATUS_PAUSED;
import static fabscreen.platform.base.service.machine.controller.MachineOperationStatus.SYSTEM_STATUS_PRINTING;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.j1platform.viewmodel.J1PrintViewModel;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.lib.print.PrintListener;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IRemote;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.controller.NewPrintController;
import fabscreen.platform.base.service.machine.controller.PrintController;
import fabscreen.platform.base.service.machine.controller.PrintEvent;
import fabscreen.platform.base.service.machine.entity.parts.Extruder;
import fabscreen.platform.base.service.machine.entity.parts.Fan;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.base.service.remote.RemoteClient;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.CircularProgressView;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

public class PrintJ1Fragment extends BaseFragment {
    private static final int STATUS_IDLE = 0;
    private static final int STATUS_PRINTING = 1;
    private static final int STATUS_PAUSED = 2;
    private static final int STATUS_COMPLETED = 3;

    @BindView(R2.id.cpv_j1_print_progress)
    CircularProgressView mCpvProgress;
    @BindView(R2.id.tv_j1_print_current_mode)
    TextView mTvPrintMode;
    @BindView(R2.id.tv_j1_print_filename)
    TextView mTvFilename;
    @BindView(R2.id.tv_j1_print_remaining_time_label)
    TextView mTvRemainTimeLabel;
    @BindView(R2.id.tv_j1_print_remaining_time_value)
    TextView mTvRemainingTime;
    @BindView(R2.id.tv_j1_print_progress)
    TextView mTvProgress;
    @BindView(R2.id.rl_j1_print_notice_temperature)
    RelativeLayout mViewNotice;
    @BindView(R2.id.btn_j1_print_pause)
    TextView mBtnPause;
    @BindView(R2.id.btn_j1_print_resume)
    TextView mBtnResume;
    @BindView(R2.id.btn_j1_print_stop)
    TextView mBtnStop;
    @BindView(R2.id.btn_j1_print_complete)
    TextView mBtnComplete;
    @BindView(R2.id.btn_j1_print_stop_one_extruder)
    TextView mBtnStopOneExtruder;
    @BindView(R2.id.btn_j1_print_print_again)
    TextView mBtnPrintAgain;

    @BindView(R2.id.iv_j1_print_left_extruder_temp)
    ImageView mIvLeftExtruderTemp;
    @BindView(R2.id.iv_j1_print_right_extruder_temp)
    ImageView mIvRightExtruderTemp;
    @BindView(R2.id.iv_j1_print_heat_bed_temp)
    ImageView mIvHeatedBedTemp;
    @BindView(R2.id.tv_j1_print_left_extruder_temp)
    TextView mTvLeftExtruderTemp;
    @BindView(R2.id.tv_j1_print_right_extruder_temp)
    TextView mTvRightExtruderTemp;
    @BindView(R2.id.tv_j1_print_heated_bed_temp)
    TextView mTvHeatedBedTemp;
    @BindView(R2.id.tv_j1_print_fan_speed_l)
    TextView mTvFanSpeedL;
    @BindView(R2.id.tv_j1_print_fan_speed_r)
    TextView mTvFanSpeedR;
    @BindView(R2.id.tv_j1_print_work_speed)
    TextView mTvWorkSpeed;
    @BindView(R2.id.rl_j1_print_details_adjustment)
    RelativeLayout mRlAdjustment;
    @BindView(R2.id.iv_j1_print_notice_temperature_logo)
    ImageView mIvHeightTemperatureLogo;
    @BindView(R2.id.tv_j1_print_notice_temperature_msg)
    TextView mIvHeightTemperatureMsg;

    boolean mIsFirstLeftExtruderTemp = false;
    boolean mIsFirstRightExtruderTemp = false;
    boolean mIsFirstHeatedBedTemp = false;

    private IRouter mRouter;
    private IPrintWorkspace mWorkspace;
    private PrintController mPrintController;
    private NewPrintController mNewPrintController;
    private IMachine mJ1Machine;

    private int mHeadType;
    // Mock data temporary.
    private float mEstimatedTime = 0;

    // indicates the the machine is moving, hence buttons should be disabled temporarily.
    private final BehaviorSubject<Boolean> mWaitingSubject = BehaviorSubject.createDefault(true);
    private int mPrintStatus;
    private boolean mIsRecover = false;
    private int mPrintModeStatusValue = -1;
    private long mPrintCostTime = 0;
    private boolean isStopOneExtruder = false;
    Disposable mSubscribe;
    Disposable mPrintControllerCallbackSub;

    private J1PrintViewModel mViewModel;
    private boolean isStateFail = false;
    private boolean isStop;
    private boolean mIsPrintFinished = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mJ1Machine = ServiceContainer.getInstance().getService(IMachine.class);
        mViewModel = getFragmentScopeViewModel(J1PrintViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRouter = ServiceContainer.getInstance().getService(IRouter.class);
        mWorkspace = ServiceContainer.getInstance().getService(IPrintWorkspace.class);
        mPrintController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();
        mNewPrintController = ServiceContainer.getInstance().getService(IMachine.class).getNewPrintController();
        initView();
        initPrint();
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.onPause();

        if (mSubscribe != null && !mSubscribe.isDisposed()) {
            mSubscribe.dispose();
            mSubscribe = null;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getPrintControllerCallback();
        mSubscribe = Observable.timer(400, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(times -> {
                    subscribeTemperature();
                });
        mNewPrintController.subscribeExtruderWorkSpeedObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                }, LogHelper::log);
        mNewPrintController.subscribePrintCostTime();
        bindMachineStatusToView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_print;
    }

    void subscribeTemperature() {
        mJ1Machine.getFDMController().subscribeExtruderChange();
        mJ1Machine.getFDMController().subscribeFanChange();
        mJ1Machine.getMachineController().getHeatedBed().subscribeTemperatureChange();
        mNewPrintController.subscribePrintModeStatus()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                }, LogHelper::log);
    }

    void unSubscribeTemperature() {
        mJ1Machine.getFDMController().unSubscribeExtruderChange();
        mJ1Machine.getFDMController().unSubscribeFanChange();
        mJ1Machine.getMachineController().getHeatedBed().unsubscribeTemperatureChange();
        mNewPrintController.unsubscribePrintModeStatus()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                }, LogHelper::log);
    }

    void bindMachineStatusToView() {
        mJ1Machine.getFDMController().getToolheadStatusSubjectHolder(0)
                .getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(fdmToolheadStatus -> {
                    Fan leftFan = fdmToolheadStatus.getFanList().get(0);
                    Extruder toolHeadExtruder = fdmToolheadStatus.getExtruderList().get(0);
                    float t0temp = toolHeadExtruder.getTemperature();
                    float t0TargetTemp = toolHeadExtruder.getTargetTemperature();
                    int fanSpeed = (int) (leftFan.getSpeedLevel() / 255f * 100);
                    // Product requirements: Before the first time the current temperature is higher than
                    // the target temperature, it needs to turn yellow to remind the user that the temperature is being heated
                    if (t0temp <= t0TargetTemp && t0TargetTemp != 0 && !mIsFirstLeftExtruderTemp) {
                        mIvLeftExtruderTemp.setImageResource(R.drawable.icon_nozzle_left_yellow_64x64);
                    } else {
                        if (t0TargetTemp != 0) {
                            mIsFirstLeftExtruderTemp = true;
                        }
                        mIvLeftExtruderTemp.setImageResource(R.drawable.icon_nozzle_left_normal_64x64);
                    }
                    mTvLeftExtruderTemp.setText(String.format(Locale.ENGLISH, "%.0f/%.0f℃", t0temp, t0TargetTemp));
                    // TODO: Percentage display, different execution headers
                    mTvFanSpeedL.setText(getString(R.string.print_j1_fan_speed_l, fanSpeed));
                }, LogHelper::log);

        mJ1Machine.getFDMController().getToolheadStatusSubjectHolder(1)
                .getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(fdmToolheadStatus -> {
                    Fan fan = fdmToolheadStatus.getFanList().get(0);
                    int fanSpeed = (int) (fan.getSpeedLevel() / 255f * 100);
                    Extruder toolHeadStatus = fdmToolheadStatus.getExtruderList().get(0);
                    float t1temp = toolHeadStatus.getTemperature();
                    float t1TargetTemp = toolHeadStatus.getTargetTemperature();
                    if (t1temp <= t1TargetTemp && t1TargetTemp != 0 && !mIsFirstRightExtruderTemp) {
                        mIvRightExtruderTemp.setImageResource(R.drawable.icon_nozzle_right_yellow_64x64);
                    } else {
                        if (t1TargetTemp != 0) {
                            mIsFirstRightExtruderTemp = true;
                        }
                        mIvRightExtruderTemp.setImageResource(R.drawable.icon_nozzle_right_normal_64x64);
                    }
                    mTvRightExtruderTemp.setText(String.format(Locale.ENGLISH, "%.0f/%.0f℃", t1temp, t1TargetTemp));
                    mTvFanSpeedR.setText(getString(R.string.print_j1_fan_speed_r, fanSpeed));
                }, LogHelper::log);

        mJ1Machine.getMachineController()
                .getHeatedBed()
                .getHeatedBedStatusSubjectHolder()
                .getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(heatedBedStatus -> {
                    float heatedBedCurrentTemperature = heatedBedStatus.getZoneList().get(0).getCurrentTemperature();
                    int heatedBedTargetTemperature = heatedBedStatus.getZoneList().get(0).getTargetTemperature();
                    if (heatedBedCurrentTemperature <= heatedBedTargetTemperature && heatedBedTargetTemperature != 0 && !mIsFirstHeatedBedTemp) {
                        mIvHeatedBedTemp.setImageResource(R.drawable.icon_heated_bed_yellow_64x64);
                    } else {
                        if (heatedBedTargetTemperature != 0) {
                            mIsFirstHeatedBedTemp = true;
                        }
                        mIvHeatedBedTemp.setImageResource(R.drawable.icon_heated_bed_normal_64x64);
                    }
                    mTvHeatedBedTemp.setText(String.format(Locale.ENGLISH, "%.0f/%d", heatedBedCurrentTemperature, heatedBedTargetTemperature)
                            + getString(R.string.all_unit_temperature));
                }, LogHelper::log);

        ServiceContainer.getInstance().getService(IMachine.class).getNewPrintController()
                .getExtruderWorkSpeedObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(speed -> {
                    mTvWorkSpeed.setText(getString(R.string.all_unit_mm_s, speed + ""));
                }, LogHelper::log);

        mNewPrintController
                .getPrintModeStatusObservable()
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(integer -> {
                    int newPrintModeStatusValue = integer == -1 ? ServiceContainer.getInstance().getService(IPrintWorkspace.class).getPrintMode() : integer;
                    if (newPrintModeStatusValue != mPrintModeStatusValue && !mNewPrintController.getPrintModeLocked()) {
                        mPrintModeStatusValue = newPrintModeStatusValue;
                        mTvPrintMode.setText(getPrintModeStr(mPrintModeStatusValue));
                        updateOneExtruder();
                    }

                }, LogHelper::log);
    }

    private void initView() {
        mTvFilename.setText(mWorkspace.getFileName());

        int printMode = mWorkspace.getPrintMode();
        Logger.d("print mode in workspace %d", printMode);
        if (printMode == IPrintWorkspace.PRINT_MODE_CLONE || printMode == IPrintWorkspace.PRINT_MODE_MIRROR) {
            mBtnStopOneExtruder.setVisibility(View.VISIBLE);
        }
        mTvPrintMode.setText(getPrintModeStr(printMode));

        IRemote remoteService = ServiceContainer.getInstance().getService(IRemote.class);
        RemoteClient remoteClient = remoteService.getNowClient();
        if (remoteClient != null) {
            String device = remoteClient.getDeviceName().isEmpty() ? "Unknown Device" : remoteClient.getDeviceName();
            String client = remoteClient.getDeviceName().isEmpty() ? "Unknown Client" : remoteClient.getConnectingClients();
            DecisionDialog decisionDialog = DecisionDialog.create(requireActivity())
                    .setCanceledOnTouchOutSide(false)
                    .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, true)
                    .setTitle(fabscreen.platform.base.R.string.j1_remote_state_title)
                    .setContent(String.format("%s (%s)", device, client))
                    .setFirstTv(fabscreen.platform.base.R.string.all_disconnect, fabscreen.platform.base.R.color.select_dialog_red_txt, ((dialog, which) -> {
                        ServiceContainer.getInstance().getService(IRemote.class).ClearConnection();
                        dialog.dismiss();
                    }));
            decisionDialog.show();

            remoteService.getRemoteConnectedObservable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(state -> {
                        if (state == 0 && decisionDialog.isShowing()) {
                            decisionDialog.dismiss();
                        }
                    });
        }
    }

    void initPrint() {
        mViewModel.initPrint();
        showButtonByPrintState(mViewModel.getPrintStateValue());
//        mPrintController.startWatchPrintIssueRequest();

        mViewModel.getPrintStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(status -> {
                    mPrintStatus = status;
                    showButtonByPrintState(status);
                }, LogHelper::log);


        // Get update progress and refresh view.
        mViewModel.getUpdateProgressObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(printProgress -> {
                    if (SYSTEM_STATUS_COMPLETED.valueEquals(mPrintStatus) || mIsPrintFinished) {
                        mTvRemainTimeLabel.setText(R.string.j1_print_time_cost);
                        mTvRemainingTime.setText(formatTime(mPrintCostTime));
                    } else {
                        mTvRemainingTime.setText(printProgress.formatTime(requireContext()));
                    }

                    mTvProgress.setText(SYSTEM_STATUS_COMPLETED.valueEquals(mPrintStatus) || mIsPrintFinished ? "100" : "" + printProgress.percentage);
                    mCpvProgress.setPercentage(SYSTEM_STATUS_COMPLETED.valueEquals(mPrintStatus) || mIsPrintFinished ? 100 : printProgress.percentage);
                });

        // enable or disable buttons
        mViewModel.getWaitingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(waiting -> {
                    mBtnPause.setEnabled(!waiting);
                    mBtnResume.setEnabled(!waiting);
                    mBtnStop.setEnabled(!waiting);
                    if (isStopOneExtruder) {
                        mBtnStopOneExtruder.setEnabled(false);
                    } else {
                        mBtnStopOneExtruder.setEnabled(!waiting);
                    }
                    mRlAdjustment.setVisibility(waiting ? View.GONE : View.VISIBLE);
                    if (!waiting) {
                        mBtnPause.setText(R.string.all_pause);
                        mBtnResume.setText(R.string.all_resume);
                    }
                });

        mViewModel.getFilamentSubjectObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(triggered -> {
                    if (triggered) {
                        int sumFilamentStatus = 0;

                        //混色模式
                        if (mPrintModeStatusValue == IPrintWorkspace.PRINT_MODE_NORMAL && mWorkspace.isApplyMultiExtruder()) {
                            sumFilamentStatus += mJ1Machine.getFDMController().getToolheadStatusSubjectHolder(0)
                                    .getValue().getExtruderList().get(0).getFilamentStatus() ? 1 : 0;
                            sumFilamentStatus += mJ1Machine.getFDMController().getToolheadStatusSubjectHolder(1)
                                    .getValue().getExtruderList().get(0).getFilamentStatus() ? 2 : 0;
                        } else {
                            sumFilamentStatus += mJ1Machine.getFDMController().getToolheadStatusSubjectHolder(0)
                                    .getValue().getExtruderList().get(0).getFilamentStatus() ? 1 : 0;
                            sumFilamentStatus += mJ1Machine.getFDMController().getToolheadStatusSubjectHolder(1)
                                    .getValue().getExtruderList().get(0).getFilamentStatus() ? 2 : 0;
                        }

                        Logger.d("Filament Runout detected, status %d, L: %b R: %b",
                                sumFilamentStatus,
                                mJ1Machine.getFDMController().getToolheadStatusSubjectHolder(0)
                                        .getValue().getExtruderList().get(0).getFilamentStatus(),
                                mJ1Machine.getFDMController().getToolheadStatusSubjectHolder(1)
                                .getValue().getExtruderList().get(0).getFilamentStatus()
                        );

                        DecisionDialog decisionDialog = DecisionDialog.create(requireContext())
                                .setType(DecisionDialog.WARMING_TYPE)
                                .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, true)
                                .setTitle(R.string.control_load_filament_failed)
                                .setFirstTv(R.string.all_j1_i_know, R.color.select_dialog_orange_txt, (dialog, which) -> {
                                    mNewPrintController.setFilament(true);
                                    dialog.dismiss();
                                });
                        switch (sumFilamentStatus) {
                            case 1:
                                decisionDialog.setContent(R.string.error_left_extruder_unable_discharge);
                                break;
                            case 2:
                                decisionDialog.setContent(R.string.error_right_extruder_unable_discharge);
                                break;
                            case 3:
                            default:
                                decisionDialog.setContent(R.string.error_double_extruder_unable_discharge);
                                break;
                        }
                        decisionDialog.show();

                    }
                });

        mJ1Machine.getMachineController().getHeatedBed().getHeatedBedStatusSubjectHolder().getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(heatedBedStatus -> {
                    if (mViewModel.getPrintStateValue() == SYSTEM_STATUS_COMPLETED.value() || mIsPrintFinished) {
                        mViewNotice.setVisibility(View.VISIBLE);
                        if (heatedBedStatus.getZoneList().get(0).getCurrentTemperature() > 40) {
                            mIvHeightTemperatureLogo.setBackgroundResource(R.drawable.icon_tips_warning_32x32);
                            mIvHeightTemperatureMsg.setTextColor(requireContext().getColor(R.color.palette_orange_safety_deep));
                            mIvHeightTemperatureMsg.setText(R.string.j1_print_remove_print_tip);
                        } else {
                            mIvHeightTemperatureLogo.setBackgroundResource(R.drawable.ic_toast_success);
                            mIvHeightTemperatureMsg.setTextColor(requireContext().getColor(R.color.palette_green));
                            mIvHeightTemperatureMsg.setText(R.string.j1_print_remove_print_low_temperature_tip);
                        }
                    } else {
                        mViewNotice.setVisibility(View.GONE);
                    }
                });

        // clone from print controller
        mJ1Machine.getNewPrintController().getPrintCostTimeObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(timeCost -> {
                    mPrintCostTime = timeCost > 0 ? timeCost : mPrintCostTime;
                    mNewPrintController.setPrintTime(mPrintCostTime);
                }, LogHelper::log);
    }

    @Deprecated
    private void setPrintControllerListener(PrintController printController) {
        printController.setListener(new PrintListener() {
            @Override
            public void onStartSuccess() {
                // oh we started
                Logger.i("Print started.");
                mWaitingSubject.onNext(false);
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().reset();
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().start();
                updateOneExtruder();
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
//                        DecisionDialog.create(requireContext())
//                                .setType(DecisionDialog.WARMING_TYPE)
//                                .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
//                                .setContent(getString(R.string.print_warning_start_unable) + retCode)
//                                .setFirstTv(getString(R.string.all_confirm), R.color.select_dialog_orange_txt, (dialog, which) -> {
//                                    dialog.dismiss();
//                        ServiceContainer.getInstance().getService(IRouter.class).routeToHome().startAndClear(requireContext());
//                                }).show();
                        ServiceContainer.getInstance().getService(IRouter.class).routeToHome().startAndClear(requireContext());
                        break;
                    }
                }
            }

            @Override
            public void onPauseSuccess() {
                Logger.i("Print paused.");
                mWaitingSubject.onNext(false);
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().stop();
                updateOneExtruder();
            }

            @Override
            public void onPauseFailed(int retCode) {
                Logger.w("Unable to pause printing.");
                mWaitingSubject.onNext(false);
//                // Just a confirm
//                DecisionDialog.create(requireContext())
//                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
//                        .setType(DecisionDialog.WARMING_TYPE)
//                        .setContent(getString(R.string.print_warning_pause_unable) + retCode)
//                        .setFirstTv(getString(R.string.all_confirm), R.color.select_dialog_orange_txt, (dialog, which) -> {
//                            dialog.dismiss();
//                        }).show();
            }

            @Override
            public void onResumeSuccess() {
                Logger.i("Print resumed.");
                mWaitingSubject.onNext(false);
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().start();
                updateOneExtruder();
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
//                        DecisionDialog.create(requireContext())
//                                .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
//                                .setType(DecisionDialog.WARMING_TYPE)
//                                .setContent(getString(R.string.print_warning_resume_unable) + retCode)
//                                .setFirstTv(getString(R.string.all_confirm), R.color.select_dialog_orange_txt, (dialog, which) -> {
//                                    dialog.dismiss();
//                                }).show();
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

//                // clear flag when resume success
//                Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().resetErrorFlag()
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(success -> {
//                            Logger.d("Error flag removed.");
//                            ServiceContainer.getInstance().getService(IMachine.class).getPrintController().clearPowerOutageFlag();
//                        }, LogHelper::log);
//                mCompositeDisposable.add(sub);

                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().load();
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().start();
                updateOneExtruder();
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
//                        DecisionDialog.create(requireContext())
//                                .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
//                                .setType(DecisionDialog.WARMING_TYPE)
//                                .setContent(getString(R.string.print_warning_resume_unable) + retCode)
//                                .setFirstTv(getString(R.string.all_confirm), R.color.select_dialog_orange_txt, (dialog, which) -> {
//                                    dialog.dismiss();
//                                    ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setPowerOutageFlag(false);
//                                    ServiceContainer.getInstance().getService(IRouter.class).routeToHome().startAndClear(requireContext());
//                                }).show();
                        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setPowerOutageFlag(false);
                        ServiceContainer.getInstance().getService(IRouter.class).routeToHome().startAndClear(requireContext());

                        break;
                    }
                }
            }

            @Override
            public void onStopSuccess() {
                Logger.i("print stopped.");
                mPrintController.getTickCounter().stop();
                mRouter.routeToHome().startAndClear(getContext());
            }

            @Override
            public void onStopFailed(int retCode) {
                Logger.w("Unable to stop printing, ret code %d", retCode);
                mWaitingSubject.onNext(false);
//                DecisionDialog.create(requireContext())
//                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
//                        .setType(DecisionDialog.WARMING_TYPE)
//                        .setContent(getString(R.string.print_warning_stop_unable) + retCode)
//                        .setFirstTv(getString(R.string.all_confirm), R.color.select_dialog_orange_txt, (dialog, which) -> {
//                            dialog.dismiss();
//                        }).show();
                ServiceContainer.getInstance().getService(IRouter.class).routeToHome().startAndClear(requireContext());
            }

            @Override
            public void onFinishSuccess() {
                Logger.i("Print Finished.");
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().stop();


//                Logger.d("Print job costs %s.", BaseApplication.formatTime(ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().getCount()));
                // Finish Print.
//                ((PrintActivity) requireActivity()).gotoPrintCompleteFragment();
            }

            @Override
            public void onFinishFailed(int retCode) {
                Logger.w("Unable to finish printing, ret code %d", retCode);
                mWaitingSubject.onNext(false);
//                DecisionDialog.create(requireContext())
//                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
//                        .setType(DecisionDialog.WARMING_TYPE)
//                        .setContent(getString(R.string.print_warning_finish_unable) + retCode)
//                        .setFirstTv(getString(R.string.all_confirm), R.color.select_dialog_orange_txt, (dialog, which) -> {
//                            dialog.dismiss();
//                        }).show();
            }
        });
    }

    private void showButtonByPrintState(int status) {
        Logger.d("status %d isPrintFinished %b", status, mIsPrintFinished);
        mBtnPause.setVisibility(SYSTEM_STATUS_PRINTING.valueEquals(status) ? Button.VISIBLE : Button.GONE);
        mBtnResume.setVisibility(SYSTEM_STATUS_PAUSED.valueEquals(status) ? Button.VISIBLE : Button.GONE);

        mBtnStop.setVisibility(!SYSTEM_STATUS_COMPLETED.valueEquals(status) && !mIsPrintFinished ? Button.VISIBLE : Button.GONE);
        mBtnComplete.setVisibility(SYSTEM_STATUS_COMPLETED.valueEquals(status) || mIsPrintFinished ? Button.VISIBLE : Button.GONE);
        mBtnPrintAgain.setVisibility(SYSTEM_STATUS_COMPLETED.valueEquals(status) || mIsPrintFinished ? Button.VISIBLE : Button.GONE);
        mRlAdjustment.setVisibility(SYSTEM_STATUS_COMPLETED.valueEquals(status) || mIsPrintFinished ? View.GONE : View.VISIBLE);

        if (SYSTEM_STATUS_COMPLETED.valueEquals(status) || mIsPrintFinished) {
            mViewModel.updateProgress();
            mBtnStopOneExtruder.setVisibility(View.GONE);
        }
        if (mIsRecover && !SYSTEM_STATUS_PRINTING.valueEquals(status)) {
            mBtnPause.setVisibility(Button.VISIBLE);
            mBtnStop.setVisibility(Button.VISIBLE);
        } else if (mIsRecover) {
            mIsRecover = false;
        }
    }

    private void updateProgress() {
        float p = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getProgress();
        if (mPrintStatus == STATUS_COMPLETED) {
            mTvRemainTimeLabel.setText(R.string.j1_print_time_cost);
            mTvRemainingTime.setText(formatTime(mPrintCostTime));
        } else {
            // formula: remaining = (1 - p) * p * elapsed / p + (1 - p) * (1 - p) * ETA
            int elapsed = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().getCount();
            int remaining = (int) ((1 - p) * elapsed + (1 - p) * (1 - p) * mEstimatedTime);
//            Logger.d("---FDT--- updateProgress\tremaining:%d,elapsed:%d,mEstimatedTime:%f,p:%f", remaining, elapsed, mEstimatedTime, p);
            mTvRemainingTime.setText(formatTime(remaining));
        }

        final int percentage = (int) (100 * p);
        mTvProgress.setText(mPrintStatus == STATUS_COMPLETED ? "100" : "" + percentage);
        mCpvProgress.setPercentage(mPrintStatus == STATUS_COMPLETED ? 100 : percentage);
    }

    public static String formatTime(long timeInSecond) {
        int hour = (int) (timeInSecond) / 3600;
        int minute = ((int) (timeInSecond) % 3600) / 60;
        int second = ((int) (timeInSecond) % 60);

        if (hour < 1) {
            return ServiceContainer.getInstance().getService(IAppService.class).getApp().getString(fabscreen.platform.base.R.string.date_helper_format_time_minute_second, minute, second);
        } else {
            return ServiceContainer.getInstance().getService(IAppService.class).getApp().getString(fabscreen.platform.base.R.string.date_helper_format_time_hour_minute, hour, minute);
        }
    }

    private void getPrintControllerCallback() {
        Logger.d("Subscribe print event.");
        Disposable subscribe = mViewModel.getPrintEventObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::onPrintControllerCallback, LogHelper::log);

        if (!subscribe.isDisposed()) {
            if (mPrintControllerCallbackSub != null && !mPrintControllerCallbackSub.isDisposed()) {
                mPrintControllerCallbackSub.dispose();
            }
            mPrintControllerCallbackSub = subscribe;
        }
    }


    private void onPrintControllerCallback(PrintEvent printEvent) {
        int retCode = printEvent.getErrorCode();
        // TODO: Split out into method.
        switch (printEvent.getPrintEventState()) {
            case START_SUCCESS:
                // oh we started
                Logger.i("Print started.");
                updateOneExtruder();
                isStateFail = false;
                break;
            case START_FAIL:
                Logger.w("Unable to start printing, ret code %d", retCode);
                isStateFail = true;
                if (retCode == 17) return;
                if (retCode == 222) {
                    mViewModel.setFilament(false);
                } else {
                    ServiceContainer.getInstance().getService(IRouter.class).routeToHome().startAndClear(requireContext());
                }
                break;
            case PAUSE_SUCCESS:
                Logger.i("Print paused.");
                updateOneExtruder();
                break;
            case PAUSE_FAIL:
                Logger.w("Unable to pause printing, ret code %d", retCode);
                if (retCode == 17) return;
                switch (retCode) {
                    case 222: {
                        mViewModel.setFilament(false);
                        return;
                    }
                    case 227:
//                        decisionDialog.setContent(getString(R.string.a400_print_enclosure_operation_trigger));
                        break;
                    default:
//                        decisionDialog.setContent(getString(R.string.print_warning_pause_unable) + "\nretCode:" + retCode);
                        break;
                }
//                decisionDialog.show();
                break;
            case RESUME_SUCCESS:
                Logger.i("Print resumed.");
                updateOneExtruder();
                break;
            case RESUME_FAIL:
                Logger.w("Unable to resume printing, ret code %d", retCode);
                if (retCode == 17) return;
                switch (retCode) {
                    case 222: {
                        mViewModel.setFilament(false);
                        return;
                    }
                    case 227: {
//                        decisionDialog.setContent(getString(R.string.a400_print_enclosure_operation_trigger));
                        break;
                    }
                    default: {
//                        decisionDialog.setContent(getString(R.string.print_warning_resume_unable) + "\nretCode:" + retCode);
                        break;
                    }
                }
//                decisionDialog.show();
                break;
            case POWER_LOSS_RESUME_SUCCESS:
                Logger.i("Print recovered.");
                isStateFail = false;
                // we resumed from power outage
                mViewModel.setPowerOutageFlag(false);
                updateOneExtruder();
                break;
            case POWER_LOSS_RESUME_FAIL:
                Logger.w("Failed to recover from power loss, ret code %d", retCode);
                isStateFail = true;
                if (retCode == 17) return;
                switch (retCode) {
                    case 222: {
                        mViewModel.setFilament(false);
                        return;
                    }
                    case 227: {
                        //ResumeFromPowerOutage
//                        decisionDialog.setContent(getString(R.string.a400_print_enclosure_operation_trigger));
                        break;
                    }
                    default: {
//                        decisionDialog.setContent(R.string.print_warning_resume_unable);
                        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setPowerOutageFlag(false);
                        ServiceContainer.getInstance().getService(IRouter.class).routeToHome().startAndClear(requireContext());

                        break;
                    }
                }
//                decisionDialog.show();
                break;
            case STOP_SUCCESS:
                if (isStop) return;
                isStop = true;
                Logger.i("print stopped.");
                mRouter.routeToHome().startAndClear(getContext());
                break;
            case STOP_FAIL:
                Logger.w("Unable to stop printing, ret code %d", retCode);
                if (retCode == 17) return;
                ServiceContainer.getInstance().getService(IRouter.class).routeToHome().startAndClear(requireContext());
                break;
            case FINISH_SUCCESS:
                Logger.i("Print Finished.");
                // FINISH Here
                Logger.d("print finish?");
                mIsPrintFinished = true;
                // Sometimes status changed quicker than print event.To prevent buttons not refreshing, update the print status.
                mViewModel.updatePrintState();
                break;
            case FINISH_FAIL:
                Logger.w("Unable to finish printing, ret code %d", retCode);
                if (retCode == 17) return;
                break;
            default:
                break;
        }
    }

    @OnClick(R2.id.rl_j1_print_details_adjustment)
    void onClickAdjustment() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class)
                .routeWithClassPath(PRINT_PRINT_J1_AJUSTMENT_MENT_CONTAINER)
                .start(getContext());
    }

    @OnClick(R2.id.btn_j1_print_stop)
    void onClickControlStop() {
        playNormalClickSound();
        if (isStateFail) {
            mRouter.routeToHome().start(requireActivity());
        } else {
            DecisionDialog.create(getActivity())
                    .setDialogStatus(DecisionDialog.BTN_TWO, false, false, false, true)
                    .setType(DecisionDialog.WARMING_TYPE)
                    .setContent(R.string.j1_print_stop_job_msg)
                    .setContentColor(R.color.palette_grey_french)
                    .needMoreHeight()
                    .setFirstTv(R.string.all_cancel, R.color.select_dialog_grey_txt, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setSecondTv(R.string.all_stop, R.color.select_dialog_orange_txt, (dialog, which) -> {
                        dialog.dismiss();
                        mViewModel.requestMachineStop();
                    }).show();
        }
    }

    @OnClick(R2.id.btn_j1_print_pause)
    void onClickControlPause() {
        playNormalClickSound();
        DecisionDialog.create(getActivity())
                .setDialogStatus(DecisionDialog.BTN_TWO, false, false, false, true)
                .setContent(R.string.j1_print_pause_job_msg)
                .setContentColor(R.color.palette_grey_french)
                .setType(DecisionDialog.WARMING_TYPE)
                .needMoreHeight()
                .setFirstTv(R.string.all_cancel, R.color.select_dialog_grey_txt, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setSecondTv(R.string.all_pause, R.color.select_dialog_orange_txt, (dialog, which) -> {
                    dialog.dismiss();
                    mViewModel.requestMachinePause();
                    mBtnPause.setText(R.string.j1_print_pausing);
                }).show();


    }

    @OnClick(R2.id.btn_j1_print_resume)
    void onClickControlResume() {
        playNormalClickSound();
        if (isStateFail) {
            mViewModel.startPrint();
        } else {
            mViewModel.requestMachineResume();
        }
        mBtnResume.setText(R.string.j1_print_resuming);
    }

    @OnClick(R2.id.btn_j1_print_complete)
    void onClickComplete() {
        playNormalClickSound();
        mRouter.routeToHome().startAndClear(getContext());
    }

    @OnClick(R2.id.btn_j1_print_print_again)
    void onClickPrintAgain() {
        playNormalClickSound();
        mIsPrintFinished = false;
        isStopOneExtruder = false;
        showButtonByPrintState(mViewModel.getPrintStateValue());
        mViewModel.initPrint();
        getPrintControllerCallback();
    }


    @OnClick(R2.id.btn_j1_print_stop_one_extruder)
    void onClickStopOneExtruder() {
        playNormalClickSound();

        DecisionDialog.create(getActivity())
                .setDialogStatus(DecisionDialog.BTN_THREE, false, false, false, true)
                .setContent(R.string.j1_print_stop_extruder)
                .setContentColor(R.color.palette_grey_french)
                .setType(DecisionDialog.TIP_TYPE)
                .needMoreHeight()
                .setFirstTv(R.string.all_cancel, R.color.select_dialog_grey_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setSecondTv(R.string.print_extruder_left, R.color.select_dialog_orange_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mBtnStopOneExtruder.setText(R.string.j1_print_left_extruder_stop);
                        stopOneExtruder(0);
                    }
                })
                .setThirdTv(R.string.print_extruder_right, R.color.select_dialog_orange_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mBtnStopOneExtruder.setText(R.string.j1_print_right_extruder_stop);
                        stopOneExtruder(1);
                    }
                }).show();

    }

    public void stopOneExtruder(int index) {
        mWaitingSubject.onNext(true);
        isStopOneExtruder = true;
        PrintController printController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();
        printController.requestStopOneExtruder(index)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(resultStructure -> {
                    mWaitingSubject.onNext(false);
                    if (resultStructure.isSuccess()) {
                        Logger.d("request stop index %d success", index);
                        updateOneExtruder();
                    } else {
                        Logger.d("request stop index %d failed", index);
                    }
                }, e -> {
                    mWaitingSubject.onNext(false);
                    LogHelper.log(e);
                });
    }

    private void updateOneExtruder() {
        if (mPrintModeStatusValue == IPrintWorkspace.PRINT_MODE_CLONE || mPrintModeStatusValue == IPrintWorkspace.PRINT_MODE_MIRROR) {
            mViewModel.getPrintHeadState()
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(responseStructure -> {
                        BaseStructure baseStructure = (BaseStructure) responseStructure.dataProp;
                        int left_enable = ((UInt8Prop) baseStructure.getProp("left_enable")).getValue();
                        int right_enable = ((UInt8Prop) baseStructure.getProp("right_enable")).getValue();
                        mBtnStopOneExtruder.setVisibility(View.VISIBLE);
                        if (left_enable + right_enable == 2) {
                            mBtnStopOneExtruder.setEnabled(true);
                            mBtnStopOneExtruder.setText(R.string.print_stop_one_extruder);
                        } else if (left_enable == 0) {
                            isStopOneExtruder = true;
                            mBtnStopOneExtruder.setEnabled(false);
                            mBtnStopOneExtruder.setText(R.string.j1_print_left_extruder_stop);
                        } else if (right_enable == 0) {
                            isStopOneExtruder = true;
                            mBtnStopOneExtruder.setEnabled(false);
                            mBtnStopOneExtruder.setText(R.string.j1_print_right_extruder_stop);
                        }
                    });
        } else {
            mBtnStopOneExtruder.setVisibility(View.GONE);
        }
    }

    public String getPrintModeStr(int printMode) {
        String printModeName;
        switch (printMode) {
            case IPrintWorkspace.PRINT_MODE_NORMAL:
                printModeName = getString(R.string.print_print_mode_standard);
                break;
            case IPrintWorkspace.PRINT_MODE_DUAL_EXTRUDER_BACK_UP:
                printModeName = getString(R.string.print_print_mode_back_up);
                break;
            case IPrintWorkspace.PRINT_MODE_CLONE:
                printModeName = getString(R.string.print_print_mode_clone);
                break;
            case IPrintWorkspace.PRINT_MODE_MIRROR:
                printModeName = getString(R.string.print_print_mode_mirror);
                break;
            default:
                printModeName = "UNKNOWN";
                break;
        }
        return printModeName;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mNewPrintController.reset();
    }

}

