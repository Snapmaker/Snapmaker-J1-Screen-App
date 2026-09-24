package fabscreen.features.print.a400platform;

import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_CNC_200W;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.parser.IGcodeParser;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.lib.print.PrintListener;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.controller.PrintController;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.entity.parts.Extruder;
import fabscreen.platform.base.service.machine.entity.parts.LaserTube;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.prop.UInt16Prop;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.base.view.FabConfirm;
import fabscreen.platform.core.ui.data.FilamentState;
import fabscreen.platform.core.ui.view.FileParsingDialog;
import fabscreen.platform.core.ui.view.PrintDetailCard;
import fabscreen.platform.core.ui.view.StepIntroductionDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class PrintA400Fragment extends BaseFragment {
    private static final int STATUS_IDLE = 0;
    private static final int STATUS_PRINTING = 1;
    private static final int STATUS_PAUSED = 2;
    private static final int STATUS_COMPLETED = 3;

    @BindView(R2.id.iv_print_file_diagram)
    ImageView mIvPrintFileDiagram;
    @BindView(R2.id.iv_print_base_show)
    ImageView mIvPrintBaseShow;
    @BindView(R2.id.tv_a400_print_filename)
    TextView mTvFilename;
    @BindView(R2.id.tv_print_remaining_time)
    TextView mTvRemainingTime;
    @BindView(R2.id.tv_a400_print_progress)
    TextView mTvPrintProgress;
    @BindView(R2.id.pb_print_progress)
    CircularProgressIndicator mPvPrintProgress;

    @BindView(R2.id.btn_a400_print_pause)
    ImageView mBtnPause;
    @BindView(R2.id.btn_a400_print_resume)
    ImageView mBtnResume;
    @BindView(R2.id.btn_a400_print_stop)
    ImageView mBtnStop;

    @BindView(R2.id.li_print_detail)
    LinearLayout mLiPrintDetail;
    boolean mLeftFilamentLastState;
    boolean mRightFilamentLastState;

    private IRouter mRouter;
    private IPrintWorkspace mWorkspace;
    private PrintController mPrintController;
    private IMachine mA400Machine;
    private final int mIsBackUpMode = 0;

    private int mHeadType;
    // Mock data temporary.
    private float mEstimatedTime = 8263;
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private StepIntroductionDialog checkFilamentProcessTipDialog;
    private BehaviorSubject<FilamentState> mFilamentStateSubject;

    private final int mLastExtruderNeedToFill = -1;
    private final PublishSubject<Boolean> mInsertFilamentSubject = PublishSubject.create();
    // indicates the the machine is moving, hence buttons should be disabled temporarily.
    private final BehaviorSubject<Boolean> mWaitingSubject = BehaviorSubject.createDefault(false);
    Disposable mDisCheckoutExtruderTemperature;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mA400Machine = ServiceContainer.getInstance().getService(IMachine.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRouter = ServiceContainer.getInstance().getService(IRouter.class);
        mWorkspace = ServiceContainer.getInstance().getService(IPrintWorkspace.class);
        mPrintController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();
        initPrint();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mA400Machine.getMachineInfoSubjectHolder().getValue().isEnclosureAvailable) {
            mA400Machine.getMachineController().getEnclosure().unsubscribeEnclosureInfo();
        }

        if (mA400Machine.getMachineInfoSubjectHolder().getValue().isAirPurifierAvailable) {
            mA400Machine.getMachineController().getAirPurifier().unsubscribeAirPurifierStatusChange();
        }


        switch (mA400Machine.getMachineInfoSubjectHolder().getValue().workType) {
            case FDM:
                mA400Machine.getFDMController().unSubscribeExtruderChange();
                mA400Machine.getMachineController().getHeatedBed().unsubscribeTemperatureChange();
                break;
            case LASER:
                mA400Machine.getLaserController().unSubscribeLaserTubeStatus().as(bindToLifecycle()).subscribe(responseStructure -> {
                }, LogHelper::log);
                break;
            case CNC:
                mA400Machine.getCNCController().unSubscribeCNCInfo();
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mA400Machine.getMachineInfoSubjectHolder().getValue().isEnclosureAvailable) {
            mA400Machine.getMachineController().getEnclosure().subscribeEnclosureInfo();
        }

        if (mA400Machine.getMachineInfoSubjectHolder().getValue().isAirPurifierAvailable) {
            mA400Machine.getMachineController().getAirPurifier().subscribeAirPurifierStatusChange();
        }
        switch (mA400Machine.getMachineInfoSubjectHolder().getValue().workType) {
            case FDM:
                mA400Machine.getFDMController().subscribeExtruderChange();
                mA400Machine.getMachineController().getHeatedBed().subscribeTemperatureChange();
                break;
            case LASER:
                mA400Machine.getLaserController().subscribeLaserTubeStatus().as(bindToLifecycle()).subscribe(responseStructure -> {
                }, LogHelper::log);
                break;
            case CNC:
                mA400Machine.getCNCController().subscribeCNCInfo();
                break;
            default:
                break;
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_print;
    }

    @OnClick({R2.id.iv_print_back_home, R2.id.tv_print_back_home})
    @Override
    protected void back() {
        playSwitchSound();
        ServiceContainer.getInstance().getService(IRouter.class).routeToHome().start(requireActivity());
        requireActivity().finish();
    }

    private void initView() {
        mIvPrintFileDiagram.setImageBitmap(ServiceContainer.getInstance().getService(IGcodeParser.class).getGcodeThumbnail());
        IMachine.WorkType workType = mA400Machine.getMachineInfoSubjectHolder().getValue().workType;
        GradientDrawable drawable = new GradientDrawable();
        drawable.setSize(12, 1);
        mLiPrintDetail.setDividerDrawable(drawable);
        mLiPrintDetail.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        int width = 24;
        GradientDrawable dra = new GradientDrawable();
        dra.setSize(width, 1);
        mLiPrintDetail.setDividerDrawable(dra);
        mLiPrintDetail.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

        switch (workType) {
            case FDM:
                mIvPrintBaseShow.setBackgroundResource(R.drawable.pic_a400_print_base_show_fdm);
                init3DPPanel();
                break;
            case LASER:
                mIvPrintBaseShow.setBackgroundResource(R.drawable.pic_a400_print_base_show_laser);
                initLaserPanel();
                break;
            case CNC:
                mIvPrintBaseShow.setBackgroundResource(R.drawable.pic_a400_print_base_show_cnc);
                initCNCPanel();
                break;
            default:
                break;
        }

        mTvFilename.setText(mWorkspace.getFileName());
        mTvFilename.setSelected(true);
        mTvRemainingTime.setText(getString(R.string.print_remaining_time) + formatTime(mWorkspace.getEstimatedTime()));

    }

    Disposable mDisCheckFilamentProcess;

    void initLaserPanel() {
        PrintDetailCard laserCard = new PrintDetailCard(getContext())
                .setIcon(R.drawable.icon_laser_gray_128x128)
                .setDetailsName(R.string.print_laser_power);
        laserCard.setClickable(true);
        mLiPrintDetail.addView(laserCard);

        mA400Machine.getLaserController().getLaserToolHeadInfoObservable(0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(laserToolheadInfo -> {
                    LaserTube laserTube = laserToolheadInfo.getLaserTube();
                    laserCard.setDetailsPercentValue((int) laserTube.getCurrentPower());
                    laserCard.setProgressValue((int) (laserTube.getCurrentPower() / 100f * 100));
                }, LogHelper::log);

        PrintDetailCard workSpeedCard = new PrintDetailCard(getContext())
                .setIcon(R.drawable.icon_work_speed_gray_64x64)
                .setDetailsName(R.string.print_work_speed);
        workSpeedCard.setClickable(true);
        mLiPrintDetail.addView(workSpeedCard);

        mPrintController.getExtruderWorkSpeed(IMachine.WorkType.LASER, 0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    BaseStructure baseStructure = (BaseStructure) responseStructure.dataProp;
                    int key = (int) baseStructure.getProp("key").getValue();
                    ArrayList<UInt16Prop> workSpeedList = (ArrayList<UInt16Prop>) baseStructure.getProp("workSpeed").getValue();
                    Logger.d("key %d, arrayList workSpeed %s", key, workSpeedList.toString());
                    workSpeedCard.setDetailsPercentValue(workSpeedList.get(0).getValue());
                    workSpeedCard.setProgressValue((int) (workSpeedList.get(0).getValue() / 500f * 100));
                });
    }

    void initCNCPanel() {
        PrintDetailCard cncCard = new PrintDetailCard(getContext())
                .setIcon(R.drawable.icon_spindle_speed_gray_64x64)
                .setDetailsName(R.string.print_cnc);
        cncCard.setClickable(true);
        mLiPrintDetail.addView(cncCard);
        boolean is200WCNC = mA400Machine.getCNCController().getHeadType() == HEAD_CNC_200W;
        mA400Machine.getCNCController().getCncToolHeadInfoObservable(0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(cncToolheadInfo -> {
                    if (is200WCNC) {
                        cncCard.setDetailsSingleValue((int) cncToolheadInfo.getCurrentSpeed());
                        cncCard.setProgressValue((int) (cncToolheadInfo.getCurrentSpeed() / 18000f * 100));
                    } else {
                        cncCard.setDetailsPercentValue((int) cncToolheadInfo.getCurrentPower());
                        cncCard.setProgressValue((int) (cncToolheadInfo.getCurrentPower() / 100f * 100));
                    }

                }, LogHelper::log);

        PrintDetailCard workSpeedCard = new PrintDetailCard(getContext())
                .setIcon(R.drawable.icon_work_speed_gray_64x64)
                .setDetailsName(R.string.print_work_speed);
        workSpeedCard.setClickable(true);
        mLiPrintDetail.addView(workSpeedCard);
        mPrintController.getExtruderWorkSpeed(IMachine.WorkType.CNC, 0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    BaseStructure baseStructure = (BaseStructure) responseStructure.dataProp;
                    int key = (int) baseStructure.getProp("key").getValue();
                    ArrayList<UInt16Prop> workSpeedList = (ArrayList<UInt16Prop>) baseStructure.getProp("workSpeed").getValue();
                    Logger.d("key %d, arrayList workSpeed %s", key, workSpeedList.toString());
                    workSpeedCard.setDetailsPercentValue(workSpeedList.get(0).getValue());
                    workSpeedCard.setProgressValue((int) (workSpeedList.get(0).getValue() / 500f * 100));
                });
    }

    void initPrint() {
        initView();

        mEstimatedTime = mWorkspace.getEstimatedTime();

        boolean isPrinting = (mPrintController.getPrintState() == PrintController.STATE_PRINTING || mPrintController.getPrintState() == PrintController.STATE_PAUSED);

        if (isPrinting) {
            // Initializing from last printing
            resumePrintFromHome();

        } else {
            // Initialize a new print job.
            AndroidSchedulers.mainThread().scheduleDirect(this::startPrint, 300, TimeUnit.MILLISECONDS);
        }

        ServiceContainer.getInstance().getService(IMachine.class)
                .getMachineStatusSubjectHolder()
                .getObservable()
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(machineStatus -> {
                    Logger.d("Machine work status " + machineStatus.status);
                    switch (machineStatus.status) {
                        case IMachine.WorkStatus.WORK_STATUS_IDLE:
                            break;
                        default:
                            break;
                    }
                }, LogHelper::log);

        mPrintController
                .getPrintStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(status -> {
                    Logger.d("print state " + status);
//                    mBtnStart.setVisibility(status == STATUS_IDLE ? Button.VISIBLE : Button.GONE);
                    mBtnPause.setVisibility(status == STATUS_PRINTING ? Button.VISIBLE : Button.INVISIBLE);
                    mBtnResume.setVisibility(status == STATUS_PAUSED ? Button.VISIBLE : Button.INVISIBLE);
                    mBtnStop.setVisibility((status != STATUS_IDLE && status != STATUS_COMPLETED) ? Button.VISIBLE : Button.INVISIBLE);
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

        mPrintController.getEmergencyStopSubjectObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(triggered -> {
                    if (triggered) {
                        FabConfirm.create(getContext())
                                .setDescription("Emergency Stop Button Trigger!")
                                .setCanceledOnTouchOutSide(false)
                                .setConfirm(R.string.all_confirm, ((dialog, which) -> {
                                    mPrintController.setEmergencyStop(true);
                                    dialog.dismiss();
                                }))
                                .show();
                    }
                });

        mPrintController.getEnclosureSubjectObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(triggered -> {
                    if (triggered) {
                        DecisionDialog.create(getContext())
                                .setCanceledOnTouchOutSide(false)
                                .setType(DecisionDialog.WARMING_TYPE)
                                .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, false)
                                .setContent(getString(R.string.a400_print_enclosure_open, "job"))
                                .setFirstTv(R.string.a400_print_got_it, R.color.select_dialog_blue_txt, ((dialog, which) -> {
                                    mPrintController.setEnclosure(true);
                                    dialog.dismiss();
                                }))
                                .show();
//                        FabConfirm.create(getContext())
//                                .setDescription("Enclosure Panel Interrupted!")
//                                .setCanceledOnTouchOutSide(false)
//                                .setConfirm(R.string.all_confirm, ((dialog, which) -> {
//                                    mPrintController.setEnclosure(true);
//                                    dialog.dismiss();
//                                }))
//                                .show();
                    }
                });

        updateProgress();
    }

    public void resumePrintFromHome() {
        setPrintControllerListener(mPrintController);
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
        PrintController printController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();
        printController.reset();
        IFile file1 = mWorkspace.getPrintFile();
        printController.setFile(file1);
        printController.setTotalLines(mWorkspace.getFileTotalLineCount());
        setPrintControllerListener(printController);

        // Power Panic
        mWaitingSubject.onNext(true);
        boolean powerOutageFlag = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getRecoveryFlag();

        if (powerOutageFlag) {
            Logger.d("Try Power Loss recovering..");
            printController.recover();
        } else {
            printController.start();
        }

        Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getResumeObservable()
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
                DecisionDialog decisionDialog = DecisionDialog.create(getContext())
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, false)
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setFirstTv(R.string.a400_print_got_it, R.color.select_dialog_blue_txt, ((dialog, which) -> {
                            dialog.dismiss();
                            ServiceContainer.getInstance().getService(IRouter.class).routeToHome().startAndClear(requireContext());
                        }));
                switch (retCode) {
                    //FIXME:
//                    case 202: {
////                        Logger.w("Filament used out, unable to start printing.");
////                        handleFilamentRunOut(result -> {
////                            if (result == PrintFragment.HandleFilamentRunOutCallback.RESULT_CANCEL) {
////                                Logger.d("Load canceled, exiting.");
////                                back();
////                            }
////                        });
//                        break;
//                    }
//                    case 203: {
//                        Logger.d("Unable to start printing, enclosure door open detected.");
////                        handleEnclosureDoorPaused();
//                        break;
//                    }
                    case 227: {
//                        The Enclosure door is opened, so the {流程} has been stopped.
                        decisionDialog.setTitle(getString(R.string.a400_print_enclosure_operation_trigger));
                    }
                    break;
                    default: {
                        Logger.w("Unable to start printing, ret code %d", retCode);
                        decisionDialog.setTitle(getString(R.string.print_warning_start_unable) + "\nretCode:" + retCode);
                        break;
                    }
                }
                decisionDialog.show();
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
                DecisionDialog decisionDialog = DecisionDialog.create(getContext())
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, false)
                        .setFirstTv(R.string.a400_print_got_it, R.color.select_dialog_blue_txt, ((dialog, which) -> {
                            dialog.dismiss();
                        }));
                switch (retCode) {
                    case 227:
                        decisionDialog.setTitle(getString(R.string.a400_print_enclosure_operation_trigger));
                        break;
                    default:
                        decisionDialog.setTitle(getString(R.string.print_warning_pause_unable) + "\nretCode:" + retCode);
                        break;
                }
                decisionDialog.show();
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
                DecisionDialog decisionDialog = DecisionDialog.create(getContext())
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, false)
                        .setFirstTv(R.string.a400_print_got_it, R.color.select_dialog_blue_txt, ((dialog, which) -> {
                            dialog.dismiss();
                        }));
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
                    case 227: {
                        decisionDialog.setTitle(getString(R.string.a400_print_enclosure_operation_trigger));
                        break;
                    }
                    default: {
                        Logger.w("Unable to resume printing.");
                        decisionDialog.setTitle(getString(R.string.print_warning_resume_unable) + "\nretCode:" + retCode);

                        break;
                    }
                }
                decisionDialog.show();
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
            }

            @Override
            public void onResumeFromPowerOutageFailed(int retCode) {
                DecisionDialog decisionDialog = DecisionDialog.create(getContext())
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, false)
                        .setFirstTv(R.string.a400_print_got_it, R.color.select_dialog_blue_txt, ((dialog, which) -> {
                            dialog.dismiss();
                            ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setPowerOutageFlag(false);
                            ServiceContainer.getInstance().getService(IRouter.class).routeToHome().startAndClear(requireContext());
                        }));
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
                    case 227: {
                        //ResumeFromPowerOutage
                        decisionDialog.setTitle(getString(R.string.a400_print_enclosure_open, getString(R.string.a400_print_resume_from_power_outage)));
                        break;
                    }
                    default: {
                        Logger.w("Failed to recover from power loss.");
                        decisionDialog.setTitle(R.string.print_warning_resume_unable);

                        break;
                    }
                }
                decisionDialog.show();
            }

            @Override
            public void onStopSuccess() {
                mWaitingSubject.onNext(false);
                Logger.i("print stopped.");
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().stop();
                ServiceContainer.getInstance().getService(IRouter.class).routeToHome().startAndClear(getContext());
            }

            @Override
            public void onStopFailed(int retCode) {
                Logger.w("Unable to stop printing, ret code %d", retCode);
                mWaitingSubject.onNext(false);
                DecisionDialog.create(getContext())
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, true)
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setTitle(R.string.print_warning_stop_unable)
                        .setFirstTv(R.string.a400_print_got_it, R.color.select_dialog_blue_txt, ((dialog, which) -> {
                            dialog.dismiss();
                            ServiceContainer.getInstance().getService(IRouter.class).routeToHome().startAndClear(requireContext());
                        })).show();
            }

            @Override
            public void onFinishSuccess() {
                Logger.i("Print Finished.");
                mWaitingSubject.onNext(false);

                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().stop();
                ((PrintA400Activity) requireActivity()).gotoPrintCompleteFragment();


//                Logger.d("Print job costs %s.", BaseApplication.formatTime(ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().getCount()));
                // Finish Print.
//                ((PrintActivity) requireActivity()).gotoPrintCompleteFragment();
            }

            @Override
            public void onFinishFailed(int retCode) {
                Logger.w("Unable to finish printing, ret code %d", retCode);
                mWaitingSubject.onNext(false);
                DecisionDialog.create(getContext())
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, true)
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setTitle(R.string.print_warning_finish_unable)
                        .setFirstTv(R.string.a400_print_got_it, R.color.select_dialog_blue_txt, ((dialog, which) -> {
                            dialog.dismiss();
                        })).show();
            }
        });
    }

    private void updateProgress() {
        float p = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getProgress();

        // formula: remaining = (1 - p) * p * elapsed / p + (1 - p) * (1 - p) * ETA
        int elapsed = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().getCount();
        int remaining = (int) ((1 - p) * elapsed + (1 - p) * (1 - p) * mEstimatedTime);
        mTvRemainingTime.setText(getString(R.string.print_remaining_time) + formatTime(remaining));

        final int percentage = (int) (100 * p);
        mTvPrintProgress.setText(percentage + "%");
        mPvPrintProgress.setProgress(percentage);
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

    void init3DPPanel() {
        mFilamentStateSubject = BehaviorSubject.createDefault(new FilamentState());
        PrintDetailCard ExtruderLeftCard = new PrintDetailCard(getContext())
                .setIcon(R.drawable.icon_nozzle_left_gray_64x64)
                .setDetailsName(R.string.all_left_nozzle_temp);
        ExtruderLeftCard.setClickable(true);
        mLiPrintDetail.addView(ExtruderLeftCard);

        PrintDetailCard ExtruderRightCard = null;
        if (mA400Machine.getFDMController().getHeadType() == Module.ModuleType.HEAD_3DP_DOUBLE_EXTRUDER) {
            ExtruderRightCard = new PrintDetailCard(getContext())
                    .setIcon(R.drawable.icon_nozzle_right_gray_64x64)
                    .setDetailsName(R.string.all_right_nozzle_temp);
            mFilamentStateSubject.onNext(mFilamentStateSubject.getValue().setExtruderNum(2));
        } else {
            mFilamentStateSubject.onNext(mFilamentStateSubject.getValue().setExtruderNum(1));
        }
        PrintDetailCard finalExtruderRightCard = ExtruderRightCard;
        if (finalExtruderRightCard != null) {
            finalExtruderRightCard.setClickable(true);
            mLiPrintDetail.addView(finalExtruderRightCard);
        }

        mA400Machine.getFDMController().getToolheadStatusSubjectHolder(0)
                .getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(fdmToolheadStatus -> {
                    Extruder leftExtruder = fdmToolheadStatus.getExtruderList().get(0);
                    float leftExtruderTemp = leftExtruder.getTemperature();
                    float leftExtruderTargetTemp = leftExtruder.getTargetTemperature();
                    boolean leftExtruderFilamentStatus = leftExtruder.getFilamentStatus();
                    ExtruderLeftCard.setDetailsCurrentValue((int) leftExtruderTemp);
                    ExtruderLeftCard.setDetailsTargetValue((int) leftExtruderTargetTemp);
                    ExtruderLeftCard.setProgressValue((int) (leftExtruderTemp / 320f * 100));
                    mFilamentStateSubject.onNext(mFilamentStateSubject.getValue().setFilamentState(0, leftExtruderFilamentStatus, leftExtruderTargetTemp, leftExtruderTargetTemp - 5 <= leftExtruderTemp));

                    if (fdmToolheadStatus.getExtruderList().size() > 1) {
                        Extruder rightExtruder = fdmToolheadStatus.getExtruderList().get(1);
                        float rightExtruderTemp = rightExtruder.getTemperature();
                        float rightExtruderTargetTemp = rightExtruder.getTargetTemperature();
                        boolean rightExtruderFilamentStatus = rightExtruder.getFilamentStatus();
                        mFilamentStateSubject.onNext(mFilamentStateSubject.getValue().setFilamentState(1, rightExtruderFilamentStatus, rightExtruderTargetTemp, rightExtruderTargetTemp - 5 <= rightExtruderTemp));
                        finalExtruderRightCard.setDetailsCurrentValue((int) rightExtruderTemp);
                        finalExtruderRightCard.setDetailsTargetValue((int) rightExtruderTargetTemp);
                        finalExtruderRightCard.setProgressValue((int) (rightExtruderTemp / 320f * 100));
                    }

                }, LogHelper::log);

        PrintDetailCard ExtruderBedCard = new PrintDetailCard(getContext())
                .setIcon(R.drawable.icon_heated_bed_gray_64x64)
                .setDetailsName(R.string.print_heated_bed_temp);
        ExtruderBedCard.setClickable(true);
        mLiPrintDetail.addView(ExtruderBedCard);

        mA400Machine.getMachineController()
                .getHeatedBed()
                .getHeatedBedStatusSubjectHolder()
                .getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(heatedBedStatus -> {
                    ExtruderBedCard.setDetailsCurrentValue((int) heatedBedStatus.getZoneList().get(0).getCurrentTemperature());
                    ExtruderBedCard.setDetailsTargetValue(heatedBedStatus.getZoneList().get(0).getTargetTemperature());
                    ExtruderBedCard.setProgressValue((int) ((heatedBedStatus.getZoneList().get(0).getCurrentTemperature() / 110f) * 100));
                }, LogHelper::log);

        mPrintController.getFilamentSubjectObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(triggered -> {
                    if (triggered) {
                        mA400Machine.getFDMController().stopExtruderHeat().observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe();
                    }
                })
                .as(bindToLifecycle())
                .subscribe(triggered -> {
                    if (triggered) {
                        mFilamentStateSubject.onNext(mFilamentStateSubject.getValue().setIsFailureState(true));
                        DecisionDialog.create(getContext())
                                .setTitle(R.string.print_warning_filament_run_out_title)
                                .setType(DecisionDialog.WARMING_TYPE)
                                .setContent(getString(R.string.print_warning_filament_run_out_extruder_content,
                                        mFilamentStateSubject.getValue().getFailureFilamentIndex() == 0 ?
                                                getString(R.string.all_left_extruder) :
                                                getString(R.string.all_right_extruder)))
                                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, true)
                                .setPic(R.drawable.pic_a400_warning_112x112)
                                .setFirstTv(getString(R.string.all_cancel), R.color.select_dialog_white_txt, ((dialog, which) -> {
                                    mPrintController.setFilament(true);
                                    dialog.dismiss();
                                }))
                                .setSecondTv(getString(R.string.print_filament_runout_load_filament), R.color.select_dialog_yellow_txt, (dialog, which) -> {
                                    dialog.dismiss();
                                    enterFilamentRefillProcedure();
                                }).show();
                    }
                });

    }

    // filament refill procedure
    // 1. Checkout which extruder should refill filament;
    // 2. Checkout extruder temperature, heated up if target is 0;
    // 3. Checkout filament sensor is on or off, start filling filament process;
    // 4. extrude a certain length filament and ask user if ready. If not, continue extruding;
    // 5. user click continue printing, leave refill procedure and resume print.
    void enterFilamentRefillProcedure() {
        if (mDisCheckoutExtruderTemperature != null && !mDisCheckoutExtruderTemperature.isDisposed()) {
            mDisCheckoutExtruderTemperature.dispose();
        }
        FileParsingDialog loadingDialog = FileParsingDialog.create(requireContext()).setContent(R.string.print_heating_nozzle);
        loadingDialog.show();
        FilamentState mFilamentState = mFilamentStateSubject.getValue();
        mDisCheckoutExtruderTemperature = mA400Machine.getFDMController().setExtruderTemperature(0, 0, (int) mFilamentState.getLeftTarget())
                .flatMap(structure -> mFilamentState.getExtruderNum() == 2 ?
                        mA400Machine.getFDMController().setExtruderTemperature(0, 1, (int) mFilamentState.getRightTarget())
                        : Observable.just(structure))
                .flatMap(structure -> mFilamentStateSubject)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(filamentState -> {
                    if (filamentState.isTemperatureReached()) {
                        loadingDialog.dismiss();
                        CheckFilamentProcess();
                    }
                }, LogHelper::log);
    }

    private void CheckFilamentProcess() {
        if (mDisCheckoutExtruderTemperature != null && !mDisCheckoutExtruderTemperature.isDisposed()) {
            mDisCheckoutExtruderTemperature.dispose();
        }
        if (mDisCheckFilamentProcess != null && !mDisCheckFilamentProcess.isDisposed()) {
            mDisCheckFilamentProcess.dispose();
        }

        FilamentState mFilamentState = mFilamentStateSubject.getValue();
        if (checkFilamentProcessTipDialog != null) {
            checkFilamentProcessTipDialog.dismiss();
        }
        checkFilamentProcessTipDialog = StepIntroductionDialog.create(requireContext());
        checkFilamentProcessTipDialog.setCanceledOnTouchOutSide(false);
        checkFilamentProcessTipDialog.setOnClickBack(v -> {
            checkFilamentProcessTipDialog.dismiss();
            mPrintController.setFilament(true);
            mDisCheckoutExtruderTemperature.dispose();
        });
        if (!mFilamentState.getNowFilamentState()) {
            checkFilamentProcessTipDialog.setImage(R.drawable.pic_pull_out_filament);
            checkFilamentProcessTipDialog.setTitle(R.string.print_pull_out_filament_title);
            checkFilamentProcessTipDialog.setContent(R.string.print_pull_out_filament_content);
            checkFilamentProcessTipDialog.show();
            mDisCheckFilamentProcess = mFilamentStateSubject
                    .as(bindToLifecycle())
                    .subscribe(filamentState -> {
                        if (mFilamentState.getNowFilamentState()) {
                            checkFilamentProcessTipDialog.dismiss();
                            CheckFilamentProcess();
                        }
                    }, LogHelper::log);
        } else {
            checkFilamentProcessTipDialog.setImage(R.drawable.pic_insert_filament);
            checkFilamentProcessTipDialog.setTitle(R.string.print_insert_filament_title);
            checkFilamentProcessTipDialog.setContent(R.string.print_insert_filament_content);
            checkFilamentProcessTipDialog.show();
            mDisCheckFilamentProcess = mFilamentStateSubject
                    .as(bindToLifecycle())
                    .subscribe(filamentState -> {
                        if (!mFilamentState.getNowFilamentState()) {
                            checkFilamentProcessTipDialog.dismiss();
                            fillingFilamentProcess();
                        }
                    }, LogHelper::log);
        }
    }

    private void fillingFilamentProcess() {
        if (mDisCheckFilamentProcess != null && !mDisCheckFilamentProcess.isDisposed()) {
            mDisCheckFilamentProcess.dispose();
        }
        FileParsingDialog loadingDialog = FileParsingDialog.create(requireContext()).setContent(R.string.print_filament_runout_start_loading);
        loadingDialog.show();
        mA400Machine.getFDMController().requestActivatedExtrusion(0, 100, 240, 0, 0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(structure -> {
                    loadingDialog.dismiss();
                    if (structure.isSuccess()) {
                        Logger.d("Filament loaded.");
                        checkFilamentComplete();
                    } else {
                        Logger.d("Filament load fail.");
                        showFilamentExtruderFailedDialog(structure.resultProp.getValue());
                    }
                }, e -> {
                    loadingDialog.dismiss();
                    mPrintController.setFilament(true);
                    LogHelper.log(e);
                    showFilamentExtruderFailedDialog(10086);
                });
    }

    void checkFilamentComplete() {
        DecisionDialog.create(getContext())
                .setTitle(R.string.control_load_filament_success)
                .setContent(R.string.control_load_filament_success_content)
                .setType(DecisionDialog.TIP_TYPE)
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, false)
                .setPic(R.drawable.pic_a400_success_112x112)
                .setFirstTv(R.string.control_load_filament, R.color.select_dialog_white_txt, ((dialog, which) -> {
                    dialog.dismiss();
                    mPrintController.setFilament(true);
                    fillingFilamentProcess();
                }))
                .setSecondTv(R.string.all_continue_printing, R.color.select_a400_dialog_success_txt, (dialog, which) -> {
                    dialog.dismiss();
                    mWaitingSubject.onNext(true);
                    mPrintController.setFilament(true);
                    mPrintController.resume();
                }).show();
    }

    void showFilamentExtruderFailedDialog(int value) {
        DecisionDialog.create(getContext())
                .setContent(String.format("An unknown exception occurred, error code %d", value))
                .setType(DecisionDialog.WARMING_TYPE)
                .setDialogStatus(DecisionDialog.BTN_ONE, true, false, false, true)
                .setPic(R.drawable.pic_a400_warning_112x112)
                .setFirstTv(getString(R.string.all_cancel), R.color.select_dialog_white_txt, ((dialog, which) -> {
                    mPrintController.setFilament(true);
                    dialog.dismiss();
                })).show();
    }

    @OnClick(R2.id.btn_a400_print_stop)
    void onClickControlStop() {
        playNormalClickSound();
        DecisionDialog.create(getContext())
                .setTitle(R.string.print_stop_printing)
                .setContent(getString(R.string.print_confirmation_content, getString(R.string.print_stop_printing)))
                .setType(DecisionDialog.WARMING_TYPE)
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, true)
                .setPic(R.drawable.pic_a400_warning_112x112)
                .setFirstTv(getString(R.string.all_cancel), R.color.select_dialog_white_txt, ((dialog, which) -> dialog.dismiss()))
                .setSecondTv(R.string.all_stop, R.color.select_dialog_yellow_txt, (dialog, which) -> {
                    dialog.dismiss();
                    mWaitingSubject.onNext(true);
                    mPrintController.stop();
                }).show();
    }

    @OnClick(R2.id.btn_a400_print_pause)
    void onClickControlPause() {
        playNormalClickSound();
        DecisionDialog.create(getContext())
                .setTitle(R.string.print_pause_printing)
                .setContent(getString(R.string.print_confirmation_content, getString(R.string.print_pause_printing)))
                .setType(DecisionDialog.WARMING_TYPE)
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, true)
                .setPic(R.drawable.pic_a400_warning_112x112)
                .setFirstTv(getString(R.string.all_cancel), R.color.select_dialog_white_txt, ((dialog, which) -> dialog.dismiss()))
                .setSecondTv(R.string.all_pause, R.color.select_dialog_yellow_txt, (dialog, which) -> {
                    dialog.dismiss();
                    mWaitingSubject.onNext(true);
                    mPrintController.pause();
                }).show();
    }

    @OnClick(R2.id.btn_a400_print_resume)
    void onClickControlResume() {
        playNormalClickSound();
        PrintController printController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();
        mWaitingSubject.onNext(true);
        printController.resume();
    }

    @OnClick(R2.id.btn_a400_print_setting)
    void onClickSettings() {
        playSwitchSound();
        if (requireActivity() != null) {
            ((PrintA400Activity) requireActivity()).gotoA400AdjustmentContainerFragment();
        }
    }

}

