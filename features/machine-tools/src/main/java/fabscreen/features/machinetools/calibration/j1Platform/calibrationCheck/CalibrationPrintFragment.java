package fabscreen.features.machinetools.calibration.j1Platform.calibrationCheck;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.CalibrationPrintViewModel;
import fabscreen.features.machinetools.calibration.J1CalibrationBaseFragment;
import fabscreen.features.machinetools.calibration.j1Platform.vibrationcalibration.VibrationCalibrationActivity;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.lib.print.PrintListener;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.controller.PrintEvent;
import fabscreen.platform.base.service.machine.entity.module.HeatedBed;
import fabscreen.platform.base.service.machine.entity.parts.Extruder;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.base.view.FabConfirm;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static fabscreen.platform.base.RoutePath.PRINT_PRINT_J1_AJUSTMENT_MENT_CONTAINER;
import static fabscreen.platform.base.service.machine.controller.MachineOperationStatus.SYSTEM_STATUS_PAUSED;
import static fabscreen.platform.base.service.machine.controller.MachineOperationStatus.SYSTEM_STATUS_PRINTING;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CalibrationPrintFragment extends J1CalibrationBaseFragment {
    @BindView(R2.id.tv_calibration_instructions_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_calibration_instructions_content)
    TextView mTvContent;
    @BindView(R2.id.tv_j1_leveling_xy_calibration_progress)
    TextView mTvProgress;
    @BindView(R2.id.tv_j1_leveling_xy_calibration_time)
    TextView mTvRemainingTime;
    @BindView(R2.id.btn_resume)
    Button mBtResume;
    @BindView(R2.id.btn_pause)
    Button mBtPause;
    @BindView(R2.id.top_bar_back)
    Button mBtBack;
    @BindView(R2.id.tv_nozzle_state_left)
    TextView mShowStateLeft;
    @BindView(R2.id.iv_nozzle_state_left)
    ImageView mIvShowStateLeft;
    @BindView(R2.id.tv_nozzle_state_right)
    TextView mShowStateRight;
    @BindView(R2.id.iv_nozzle_state_right)
    ImageView mIvShowStateRight;
    @BindView(R2.id.tv_bed_state)
    TextView mShowStateBed;
    @BindView(R2.id.iv_bed_state)
    ImageView mIvShowStateBed;
    @BindView(R2.id.rectangle_2)
    ImageView mIvRectangle;
    @BindView(R2.id.btn_skip)
    Button mBtnAdjust;

    private CalibrationPrintViewModel mViewModel;
    boolean mIsFirstLeftExtruderTemp = false;
    boolean mIsFirstRightExtruderTemp = false;
    boolean mIsFirstHeatedBedTemp = false;
    private boolean mVisible;
    private boolean mFinishSuccess;
    private int mPrintScene;
    private IMachine mJ1Machine;
    private long mPrintCostTime = 0;

    Disposable mPrintControllerCallbackSub;

    /**
     * @param printScene 0: calibration check; 1: vibration calibration;
     */
    public static Fragment newInstance(int printScene) {
        Fragment fragment = new CalibrationPrintFragment();
        Bundle args = new Bundle();
        args.putInt("scene", printScene);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mJ1Machine = ServiceContainer.getInstance().getService(IMachine.class);
        mViewModel = getViewModel();
        mPrintScene = requireArguments().getInt("scene", 0);
        initView();
//        setPrintControllerListener();

        mViewModel.startPrint(getGcode());
    }

    private int getGcode() {
        if (mPrintScene == 0) {
            return R.raw.scp_p4;
        } else if (mPrintScene == 1) {
            return R.raw.gcode_vibration_compensation_print;
        } else {
            return 0;
        }
    }

    private void initView() {
        initViewByScene();

        mBtPause.setBackgroundResource(R.drawable.j1_btn_round_secondary);
        mBtPause.setTextColor(requireContext().getColorStateList(R.color.j1_btn_second_txt));

        mViewModel.getProgress()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(integer -> {
                    mTvProgress.setText(String.format(Locale.ENGLISH, "%d%%", integer));
                });
        mTvRemainingTime.setText("15 min");

        mViewModel.getWaitingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(waiting -> {
                    mBtBack.setEnabled(!waiting);
                    mBtPause.setEnabled(!waiting);
                    mBtResume.setEnabled(!waiting);
                    if (!waiting) {
                        mBtResume.setText(R.string.all_resume);
                        mBtPause.setText(R.string.all_pause);
                        mBtnAdjust.setEnabled(true);
                    }
                });

        mViewModel.getToolheadStatusObservable(0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(fdmToolHeadInfo -> {
                            Extruder extruder = fdmToolHeadInfo.getExtruderList().get(0);
                            float currentTemperature = extruder.getTemperature();
                            float targetTemperature = extruder.getTargetTemperature();
                            mShowStateLeft.setText(String.format("%.0f/%.0f℃", currentTemperature, targetTemperature));
                            if (currentTemperature <= targetTemperature && targetTemperature != 0 && !mIsFirstLeftExtruderTemp) {
                                mIvShowStateLeft.setImageResource(R.drawable.icon_nozzle_left_yellow_64x64);
                            } else {
                                if (targetTemperature != 0) {
                                    mIsFirstLeftExtruderTemp = true;
                                }
                                mIvShowStateLeft.setImageResource(R.drawable.icon_nozzle_left_normal_64x64);
                            }
                        }
                );

        mViewModel.getToolheadStatusObservable(1)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(fdmToolHeadInfo -> {
                            Extruder extruder = fdmToolHeadInfo.getExtruderList().get(0);
                            float currentTemperature = extruder.getTemperature();
                            float targetTemperature = extruder.getTargetTemperature();
                            mShowStateRight.setText(currentTemperature + "/" + targetTemperature + "°C");
                            if (currentTemperature <= targetTemperature && targetTemperature != 0 && !mIsFirstRightExtruderTemp) {
                                mIvShowStateRight.setImageResource(R.drawable.icon_nozzle_right_yellow_64x64);
                            } else {
                                if (targetTemperature != 0) {
                                    mIsFirstRightExtruderTemp = true;
                                }
                                mIvShowStateRight.setImageResource(R.drawable.icon_nozzle_right_normal_64x64);
                            }
                            mShowStateRight.setText(String.format("%.0f/%.0f℃", currentTemperature, targetTemperature));
                        }
                );


        mViewModel.getHeatedBedObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(bedStatus -> {
                            HeatedBed.ZoneInfo zoneInfo = bedStatus.getZoneList().get(0);
                            float targetTemperature = zoneInfo.getTargetTemperature();
                            float currentTemperature = zoneInfo.getCurrentTemperature();
                            if (currentTemperature <= targetTemperature && targetTemperature != 0 && !mIsFirstHeatedBedTemp) {
                                mIvShowStateBed.setImageResource(R.drawable.icon_heated_bed_yellow_64x64);
                            } else {
                                if (targetTemperature != 0) {
                                    mIsFirstHeatedBedTemp = true;
                                }
                                mIvShowStateBed.setImageResource(R.drawable.icon_heated_bed_normal_64x64);
                            }
                            mShowStateBed.setText(String.format("%.0f/%.0f℃", currentTemperature, targetTemperature));

                        }
                );


        mViewModel.getPrintControllerStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(integer -> {
                    mBtPause.setVisibility(SYSTEM_STATUS_PRINTING.valueEquals(integer) ? Button.VISIBLE : Button.GONE);
                    mBtResume.setVisibility(SYSTEM_STATUS_PAUSED.valueEquals(integer) ? Button.VISIBLE : Button.GONE);
                });

        mViewModel.getFilamentSubjectObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(triggered -> {
                    if (triggered) {
                        int sumFilamentStatus = 0;

                        if (ServiceContainer.getInstance().getService(IPrintWorkspace.class).isApplyMultiExtruder()) {
                            sumFilamentStatus += mViewModel.getToolheadFilamentStatus(0) ? 1 : 0;
                            sumFilamentStatus += mViewModel.getToolheadFilamentStatus(1) ? 2 : 0;
                        } else {
                            sumFilamentStatus += mViewModel.getToolheadFilamentStatus(0) ? 1 : 0;
                            sumFilamentStatus += mViewModel.getToolheadFilamentStatus(1) ? 2 : 0;
                        }

                        Logger.d("Filament Runout detected, status %d, L: %b R: %b",
                                sumFilamentStatus,
                                mViewModel.getToolheadFilamentStatus(0),
                                mViewModel.getToolheadFilamentStatus(1));

                        DecisionDialog decisionDialog = DecisionDialog.create(requireContext())
                                .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, true)
                                .setTitle(R.string.control_load_filament_failed)
                                .setType(DecisionDialog.WARMING_TYPE)
                                .setFirstTv(R.string.all_ok, R.color.select_dialog_orange_txt, (dialog, which) -> {
                                    mViewModel.setFilament(true);
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

        mJ1Machine.getNewPrintController().getPrintCostTimeObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(timeCost -> {
                    mPrintCostTime = timeCost > 0 ? timeCost : mPrintCostTime;
                    mViewModel.mNewPrintController.setPrintTime(mPrintCostTime);
                }, LogHelper::log);

    }

    private void initViewByScene() {
        // 0 - Print Check Section, used for print a pattern model to check if calibration was successful.
        // 1 - Vibration Calibration Print Section,
        // used for print a G-code file with several frequency reduction, and let user to choose the most smooth surface.
        if (mPrintScene == 0) {
            mTvTitle.setText(R.string.j1_calibration_calibration_check_print_check_model);
            Glide.with(this).asGif().load(R.drawable.gif_j1_calibration_check_printing).into(mIvRectangle);
            mShowStateRight.setVisibility(View.VISIBLE);
            mIvShowStateRight.setVisibility(View.VISIBLE);
        } else {
            mTvTitle.setText(R.string.j1_calibration_vibration_print_model_title);
            Glide.with(this).asGif().load(R.drawable.gif_j1_calibration_print_calibration_model).into(mIvRectangle);
            mShowStateRight.setVisibility(View.GONE);
            mIvShowStateRight.setVisibility(View.GONE);
        }
    }

    private void listenPrintEvent() {
        Disposable subscribe = mViewModel.getPrintEventObservable()
                .doOnSubscribe(p -> Logger.d("Start watching print event from controller..."))
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
        Logger.d("print event " + printEvent.toString());
        switch (printEvent.getPrintEventState()) {
            case START_SUCCESS:
                Logger.i("Print started.");
                break;
            case START_FAIL:
                Logger.w("Unable to start printing, ret code %d", retCode);
                String str = "";
                switch (retCode) {
                    //FIXME:
                    case 256:
                        str = "初始化文件出问题";
                        break;
                    default: {
                        str = getString(R.string.print_warning_start_unable) + retCode;
                        break;
                    }
                }
                DecisionDialog.create(requireContext())
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
                        .setContent(str)
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setFirstTv(getString(R.string.all_confirm), R.color.select_dialog_orange_txt, (dialog, which) -> {
                            dialog.dismiss();
                            exitAndFinish();
                        }).show();
                break;
            case PAUSE_SUCCESS:
                Logger.i("Print paused.");
                break;
            case PAUSE_FAIL:
                Logger.w("Unable to pause printing, ret code %d", retCode);
                DecisionDialog.create(requireContext())
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
                        .setContent(getString(R.string.print_warning_pause_unable) + retCode)
                        .setFirstTv(getString(R.string.all_confirm), R.color.select_dialog_orange_txt, (dialog, which) -> {
                            dialog.dismiss();
                        }).show();
                break;
            case RESUME_SUCCESS:
                Logger.i("Print resumed.");
                break;
            case RESUME_FAIL:
                Logger.w("Unable to resume printing, ret code %d", retCode);
                break;
            case POWER_LOSS_RESUME_SUCCESS:
                Logger.i("Print recovered.");
                break;
            case POWER_LOSS_RESUME_FAIL:
                Logger.w("Failed to recover from power loss, ret code %d", retCode);
                DecisionDialog.create(requireContext())
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
                        .setContent(getString(R.string.print_warning_resume_unable) + retCode)
                        .setFirstTv(getString(R.string.all_confirm), R.color.select_dialog_orange_txt, (dialog, which) -> {
                            dialog.dismiss();
                            mViewModel.stop();
                        }).show();
                break;
            case STOP_SUCCESS:
                Logger.i("print stopped.");
                fabMoving.dismiss();
                exitAndFinish();
                break;
            case STOP_FAIL:
                fabMoving.dismiss();
                Logger.w("Unable to stop printing, ret code %d", retCode);
                DecisionDialog.create(requireContext())
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
                        .setContent(getString(R.string.print_warning_stop_unable) + retCode)
                        .setFirstTv(getString(R.string.all_confirm), R.color.select_dialog_orange_txt, (dialog, which) -> {
                            dialog.dismiss();
                            exitAndFinish();
                        }).show();
                break;
            case FINISH_SUCCESS:
                Logger.d("FINISH SUCCESS");
                if (mVisible) {
                    goNext();
                } else {
                    mFinishSuccess = true;
                }
                break;
            case FINISH_FAIL:
                Logger.w("Unable to finish printing, ret code %d", retCode);
                DecisionDialog.create(requireContext())
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
                        .setContent(getString(R.string.print_warning_finish_unable) + retCode)
                        .setFirstTv(getString(R.string.all_confirm), R.color.select_dialog_orange_txt, (dialog, which) -> {
                            dialog.dismiss();
                            mViewModel.stop();
                        }).show();
                break;
            default:
                break;
        }
    }

    private void setPrintControllerListener() {
        mViewModel.setListener(new PrintListener() {
            @Override
            public void onStartSuccess() {
//                mWaitingSubject.onNext(false);
            }

            @Override
            public void onStartFailed(int retCode) {
//                mWaitingSubject.onNext(false);
                Logger.w("Unable to start printing, ret code %d", retCode);
                String str = "";
                switch (retCode) {
                    //FIXME:
                    case 256:
                        str = "初始化文件出问题";
                        break;
                    default: {
                        str = getString(R.string.print_warning_start_unable) + retCode;
                        break;
                    }
                }
                DecisionDialog.create(requireContext())
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
                        .setContent(str)
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setFirstTv(getString(R.string.all_confirm), R.color.select_dialog_orange_txt, (dialog, which) -> {
                            dialog.dismiss();
                            exitAndFinish();
                        }).show();
            }

            @Override
            public void onPauseSuccess() {
//                mWaitingSubject.onNext(false);
            }

            @Override
            public void onPauseFailed(int retCode) {
//                mWaitingSubject.onNext(false);
                DecisionDialog.create(requireContext())
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
                        .setContent(getString(R.string.print_warning_pause_unable) + retCode)
                        .setFirstTv(getString(R.string.all_confirm), R.color.select_dialog_orange_txt, (dialog, which) -> {
                            dialog.dismiss();
                        }).show();
            }

            @Override
            public void onResumeSuccess() {
//                mWaitingSubject.onNext(false);
            }

            @Override
            public void onResumeFailed(int retCode) {
//                mWaitingSubject.onNext(false);
            }

            @Override
            public void onResumeFromPowerOutageSuccess() {
//                mWaitingSubject.onNext(false);
            }

            @Override
            public void onResumeFromPowerOutageFailed(int retCode) {
//                mWaitingSubject.onNext(false);
                DecisionDialog.create(requireContext())
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
                        .setContent(getString(R.string.print_warning_resume_unable) + retCode)
                        .setFirstTv(getString(R.string.all_confirm), R.color.select_dialog_orange_txt, (dialog, which) -> {
                            dialog.dismiss();
                            mViewModel.stop();
                        }).show();
            }

            @Override
            public void onStopSuccess() {
                fabMoving.dismiss();
//                mWaitingSubject.onNext(false);
                exitAndFinish();
            }

            @Override
            public void onStopFailed(int retCode) {
                fabMoving.dismiss();
//                mWaitingSubject.onNext(false);
                DecisionDialog.create(requireContext())
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
                        .setContent(getString(R.string.print_warning_stop_unable) + retCode)
                        .setFirstTv(getString(R.string.all_confirm), R.color.select_dialog_orange_txt, (dialog, which) -> {
                            dialog.dismiss();
                            exitAndFinish();
                        }).show();
            }

            @Override
            public void onFinishSuccess() {
//                mWaitingSubject.onNext(false);
                if (mVisible) {
                    goNext();
                } else {
                    mFinishSuccess = true;
                }
            }

            @Override
            public void onFinishFailed(int retCode) {
//                mWaitingSubject.onNext(false);
                DecisionDialog.create(requireContext())
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
                        .setContent(getString(R.string.print_warning_finish_unable) + retCode)
                        .setFirstTv(getString(R.string.all_confirm), R.color.select_dialog_orange_txt, (dialog, which) -> {
                            dialog.dismiss();
                            mViewModel.stop();
                        }).show();
            }
        });
    }

    private void goNext() {
        mViewModel.printComplete();
        if (requireActivity() instanceof CalibrationCheckCalibrationActivity) {
            ((CalibrationCheckCalibrationActivity) requireActivity()).gotoCheckZOffsetCalibration();
        } else if (requireActivity() instanceof VibrationCalibrationActivity) {
            ((VibrationCalibrationActivity) requireActivity()).goToChooseAreaIntro();
        }
    }

    @Override
    protected CalibrationPrintViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(CalibrationPrintViewModel.class);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_leveling_xy_calibration_print;
    }

    boolean skip = false;


    @Override
    protected void back() {
        onClickStop();
    }

    @OnClick({R2.id.top_bar_back})
    public void onClickStop() {
        playNormalClickSound();
        DecisionDialog.create(getContext())
                .setDialogStatus(DecisionDialog.BTN_TWO, false, false, false, true)
                .setType(DecisionDialog.WARMING_TYPE)
                .setContent(R.string.j1_calibration_quit_msg)
                .setContentColor(R.color.palette_grey_french)
                .setFirstTv(getString(R.string.all_cancel), R.color.select_dialog_left_text_color, ((dialog, which) -> {
                    dialog.dismiss();
                }))
                .setSecondTv(getString(R.string.all_quit), R.color.palette_red_sunset, ((dialog, which) -> {
//                    mWaitingSubject.onNext(true);
                    dialog.dismiss();
                    fabMoving.show();
                    mViewModel.stop();
//                    ServiceContainer.getInstance().getService(IMachine.class).getFDMController()
//                            .exitCalibration(false)
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .as(bindToLifecycle())
//                            .subscribe(success -> {
//                                if (success.isSuccess()) {
//
//                                }else {
//                                    Logger.d("Stop Error");
//                                }
//                            });
                }))
                .show();
    }

    @OnClick(R2.id.btn_pause)
    public void onChickPause() {
//        mWaitingSubject.onNext(true);
        mBtPause.setText(R.string.all_print_pausing);
        mViewModel.pause();
        mBtnAdjust.setEnabled(false);
    }

    @OnClick(R2.id.btn_resume)
    public void onChickResume() {
//        mWaitingSubject.onNext(true);
        mBtResume.setText(R.string.all_print_resuming);
        mViewModel.resume();
        mBtnAdjust.setEnabled(false);
    }


    private void exitAndFinish() {
        if (skip == true) {
            goNext();
            return;
        }
//        requireActivity().finish();
        ServiceContainer.getInstance().getService(IMachine.class).getFDMController()
                .exitCalibration(false)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    if (success.isSuccess()) {
                        requireActivity().finish();
                    }
                }, LogHelper::log);
    }

    @OnClick(R2.id.btn_skip)
    void onClickAdjustment() {
        ServiceContainer.getInstance().getService(IRouter.class)
                .routeWithClassPath(PRINT_PRINT_J1_AJUSTMENT_MENT_CONTAINER)
                .start(getContext());
    }

    @OnClick(R2.id.rl_j1_print_details_adjustment)
    public void onChickSkip() {
        FabConfirm.create(getContext())
                .setDescription("这只是一个临时跳过按钮")
                .setConfirm(R.string.all_yes, (dialog, which) -> {
                    dialog.dismiss();
                    skip = true;
                    mViewModel.stop();
                })
                .setCancel(R.string.all_cancel, ((dialog, which) -> dialog.dismiss()))
                .show();

    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.unSubscribeTemperature();
        mVisible = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidSchedulers.mainThread().scheduleDirect(this::listenPrintEvent, 500, TimeUnit.MILLISECONDS);
        mViewModel.subscribeTemperature();
        mVisible = true;
        if (mFinishSuccess) {
            goNext();
        }
    }
}
