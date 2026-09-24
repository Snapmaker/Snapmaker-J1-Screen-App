package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingXY;

import static fabscreen.platform.base.service.machine.controller.PrintController.STATE_IDLE;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP_DOUBLE_EXTRUDER;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.FabLocalFile;
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
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.base.view.FabToast;
import fabscreen.platform.core.ui.data.FilamentState;
import fabscreen.platform.core.ui.view.PrintDetailCard;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class A400LevelingXYCalibrationCheckPrintFragment extends A400CalibrationBaseFragment {
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
    @BindView(R2.id.btn_a400_print_stop)
    ImageView mBtnStop;
    @BindView(R2.id.li_print_detail)
    LinearLayout mLiPrintDetail;
    @BindView(R2.id.top_bar_title)
    TextView mTvTitle;
    @BindView(R2.id.top_bar_content)
    TextView mTvSubTitle;

    private IRouter mRouter;
    private IPrintWorkspace mWorkspace;
    private PrintController mPrintController;
    private IMachine mA400Machine;
    private final int mIsBackUpMode = 0;

    private int mHeadType;
    // Mock data temporary.
    private float mEstimatedTime = 8263;
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private IGcodeParser mParser;
    // indicates the the machine is moving, hence buttons should be disabled temporarily.
    private final BehaviorSubject<Boolean> mWaitingSubject = BehaviorSubject.createDefault(false);
    Disposable mDisCheckoutExtruderTemperature;
    Disposable mDisCheckFilamentProcess;
    FabToast checkFilamentProcessTipDialog;
    private DecisionDialog mQuitDialog;
    public PublishSubject<Boolean> mFinishSubject = PublishSubject.create();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mA400Machine = ServiceContainer.getInstance().getService(IMachine.class);
        mRouter = ServiceContainer.getInstance().getService(IRouter.class);
        mWorkspace = ServiceContainer.getInstance().getService(IPrintWorkspace.class);
        mPrintController = mA400Machine.getPrintController();
        mParser = ServiceContainer.getInstance().getService(IGcodeParser.class);

        mTvTitle.setText(R.string.print_xy_offset_calibration_title);
        mTvSubTitle.setText(R.string.print_xy_offset_calibration_subtitle);

        mQuitDialog = DecisionDialog.create(getContext())
                .setTitle(R.string.print_stop_printing)
                .setContent(getString(R.string.print_confirmation_content, getString(R.string.print_stop_printing)))
                .setType(DecisionDialog.WARMING_TYPE)
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, true)
                .setPic(R.drawable.pic_a400_warning_112x112)
                .setFirstTv(getContext().getResources().getString(R.string.all_cancel), R.color.select_dialog_white_txt, ((dialog, which) -> {
                    dialog.dismiss();
                }))
                .setSecondTv(getContext().getResources().getString(R.string.all_stop), R.color.select_dialog_white_txt, (dialog, which) -> {
                    mQuitDialog.mCancelBtn.setEnabled(false);
                    mQuitDialog.mSecondBtn.setEnabled(false);
                    mPrintController.stop();
                });
        initView();
        init3DPPanel();
        setPrintControllerListener(mPrintController);
        checkHome()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        initFile();
                    }
                });
        mFinishSubject
                .flatMap(aBoolean -> ServiceContainer.getInstance().getService(IMachine.class).getFDMController().exitCalibration(aBoolean))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    if (success.isSuccess()) {
                        if (mQuitDialog != null && mQuitDialog.isShowing()) {
                            mQuitDialog.dismiss();
                        }
                        mPrintController.setPrintState(STATE_IDLE);
                        coolDownBedIfHave();
                        coolDownToolHead();
                        requireActivity().finish();
                    }
                });
    }

    private void coolDownToolHead() {
        mA400Machine.getFDMController().setAllExtruderTemperature(0)
                .flatMap(b -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getHeatedBed().setAllTargetTemperature(0))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aBoolean -> {
                }, LogHelper::log);

    }

    private BehaviorSubject<FilamentState> mFilamentStateSubject;

    void init3DPPanel() {
        int width = 24;
        GradientDrawable drawable = new GradientDrawable();
        drawable.setSize(width, 1);
        mLiPrintDetail.setDividerDrawable(drawable);
        mLiPrintDetail.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

        mFilamentStateSubject = BehaviorSubject.createDefault(new FilamentState());
        PrintDetailCard ExtruderLeftCard = new PrintDetailCard(getContext())
                .setIcon(R.drawable.icon_nozzle_left_gray_64x64)
                .setDetailsName(R.string.all_left_nozzle_temp);
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
    }

    public Observable<Boolean> checkHome() {
        IMachine service = ServiceContainer.getInstance().getService(IMachine.class);
        if (!service.getMachineStatusSubjectHolder().getValue().isHomed) {
            return service.getMachineController().updateCoordinateSystem(0)
                    .flatMap(machineStatus -> service.getMachineController().home(0))
                    .flatMap(integer -> service.getMachineController().updateCoordinateSystem(1))
                    .flatMap(machineStatus -> Observable.just(machineStatus.isHomed));
        } else {
            return Observable.just(true);
        }
    }

    private void initFile() {
        File printFile = copyPrintFile();
        IFile selectFile = new FabLocalFile(printFile);
        mParser.startParse(selectFile, IMachine.WorkType.FDM);
        mParser.getParseProgressObservable()
                .throttleLast(100, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(progress -> {
                    if (progress == 100) {
                        handleResult(selectFile);
                    }
                });
    }

    private File copyPrintFile() {
        InputStream is = getResources().openRawResource(R.raw.g1647071720062);
        File file = null;
        try {
            file = new File(getContext().getCacheDir().getAbsoluteFile() + "/calibrationXY.gcode");
            if (file.exists()) {
                file.delete();
            }
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                int read;
                byte[] bytes = new byte[20480];
                while ((read = is.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            }
        } catch (Exception e) {
            file = null;
            Logger.d("---FDT--- file Error: " + e);
        } finally {
            try {
                is.close();
            } catch (Exception ignored) {

            }
        }
        return file;
    }

    void handleResult(IFile file) {
        mIvPrintFileDiagram.setImageBitmap(mParser.getGcodeThumbnail());
        mWorkspace.setPrintMode(IPrintWorkspace.PRINT_MODE_NORMAL);
        mWorkspace.setPrintSource(0);
        mWorkspace.setFileTotalLineCount(mParser.getTotalLinesCount());
        mWorkspace.setEstimatedTime(mParser.getEstimatedTime());
        mWorkspace.setFileMD5Value("c319528c5c360d46031b69d39e01ceb3");
        if (mParser.getFileType() == IMachine.WorkType.FDM) {
            if (mParser.getHeaderType() == HEAD_3DP) {
                mWorkspace.setWorkTemperature(new float[]{mParser.getNozzleTargetTemperature()});
            } else if (mParser.getHeaderType() == HEAD_3DP_DOUBLE_EXTRUDER) {
                mWorkspace.setWorkTemperature(new float[]{mParser.getNozzleTargetTemperature(), mParser.getNozzleTarget_1_Temperature()});
            }
        }
        mWorkspace.addFileToWorkspace(file).observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe(success -> {
            if (success) {
                initPrint();
            }
        }, LogHelper::log);
    }

    private void initView() {
        mGuideProgressBar.setMax(6);
        mGuideProgressBar.setVisibility(View.VISIBLE);
        mGuideProgressBar.setProgress(5);
        setTitle(R.string.calibration_a400_leveling_xy_title);
        mTvTopBarContent.setText(R.string.calibration_a400_leveling_xy_print_check_mode);
        mBtnStop.setVisibility(Button.VISIBLE);
    }

    void initPrint() {
        mEstimatedTime = mWorkspace.getEstimatedTime();
        AndroidSchedulers.mainThread().scheduleDirect(this::startPrint, 300, TimeUnit.MILLISECONDS);

        ServiceContainer.getInstance().getService(IMachine.class)
                .getMachineStatusSubjectHolder()
                .getObservable()
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(machineStatus -> {
                    Logger.d("---FDT--- Machine work status " + machineStatus.status);
                }, LogHelper::log);

        mPrintController
                .getPrintStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(status -> {
                    Logger.d("screen print state " + status);
                    if (status == STATUS_COMPLETED) {
                        updateProgress();
                    }
                }, LogHelper::log);

        // enable or disable buttons
        mWaitingSubject
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(waiting -> {
                    mBtnStop.setEnabled(!waiting);
                });

        mA400Machine = ServiceContainer.getInstance().getService(IMachine.class);
        updateProgress();
    }

    private void startPrint() {
        mCompositeDisposable.clear();
        PrintController printController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();
        IFile file1 = mWorkspace.getPrintFile();
        mTvFilename.setText(file1.getName());
        printController.reset();
        printController.setFile(file1);
        printController.setTotalLines(mWorkspace.getFileTotalLineCount());

        // Power Panic
        mWaitingSubject.onNext(true);
        printController.start();

        Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getResumeObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(resume -> {
//                    resumeFromChangeFilament()
                });
        mCompositeDisposable.add(sub);

        // Update
        sub = Observable.interval(2, TimeUnit.SECONDS)
                .takeUntil(tick -> mPrintController.getPrintState() == STATUS_COMPLETED)
                .filter(tick -> mPrintController.getPrintState() == STATUS_PRINTING)
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
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, false)
                        .setFirstTv(R.string.a400_print_got_it, R.color.select_dialog_blue_txt, ((dialog, which) -> {
                            dialog.dismiss();
                            mFinishSubject.onNext(false);
                        }));
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().stop();
                switch (retCode) {
                    case 202: {
                        Logger.w("Filament used out, unable to start printing.");
                        break;
                    }
                    case 203: {
                        Logger.d("Unable to start printing, enclosure door open detected.");
                        break;
                    }
                    case 227: {
//
                        decisionDialog.setTitle(getString(R.string.a400_print_enclosure_open, getString(R.string.a400_print_start_print)));
                    }
                    default: {
                        Logger.w("Unable to start printing, ret code %d", retCode);
                        decisionDialog.setTitle("getString(R.string.print_warning_start_unable) + \"\\nretCode:\" + retCode");
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
                        decisionDialog.setTitle(getString(R.string.a400_print_enclosure_open, getString(R.string.a400_print_pause_print)));
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
                            mFinishSubject.onNext(false);
                        }));
                switch (retCode) {
                    case 202: {
                        Logger.d("Filament used out, unable to resume printing.");
//                        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().pauseOnFilamentUsedOut();
//                        handleFilamentRunOut(null);
                        break;
                    }
                    case 203: {
//                        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().pauseOnEnclosureDoorDetected();
//                        handleEnclosureDoorPaused();
                        break;
                    }
                    case 227: {
                        decisionDialog.setTitle(getString(R.string.a400_print_enclosure_open, getString(R.string.a400_print_resume_print)));
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
                            mFinishSubject.onNext(false);
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
                mQuitDialog.dismiss();
                getServiceContainer().getService(IMachine.class).getFDMController().exitCalibration(false).as(bindToLifecycle()).subscribe();
                mFinishSubject.onNext(false);
            }

            @Override
            public void onStopFailed(int retCode) {
                Logger.w("Unable to stop printing, ret code %d", retCode);
                mWaitingSubject.onNext(false);
                mQuitDialog.mCancelBtn.setEnabled(true);
                mQuitDialog.mSecondBtn.setEnabled(true);
                getServiceContainer().getService(IMachine.class).getFDMController().exitCalibration(false).as(bindToLifecycle()).subscribe();
                DecisionDialog.create(getContext())
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, true)
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setTitle(R.string.print_warning_stop_unable)
                        .setFirstTv(R.string.a400_print_got_it, R.color.select_dialog_blue_txt, ((dialog, which) -> {
                            dialog.dismiss();
                            mFinishSubject.onNext(false);
                        })).show();

            }

            @Override
            public void onFinishSuccess() {
                Logger.i("Print Finished.");
                mWaitingSubject.onNext(false);
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().stop();
                ((A400LevelingXYCalibrationActivity) requireActivity()).gotoVerifyResults();
            }

            @Override
            public void onFinishFailed(int retCode) {
                Logger.w("Unable to finish printing, ret code %d", retCode);
                mWaitingSubject.onNext(false);
                mFinishSubject.onNext(false);
                DecisionDialog.create(getContext())
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, true)
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
        mTvPrintProgress.setText("" + percentage);
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

    @OnClick(R2.id.btn_a400_print_stop)
    void onClickControlStop() {
        playNormalClickSound();
//        PrintController printController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();
        if (mQuitDialog.isShowing()) {
            return;
        } else {
            mQuitDialog.show();
        }
//        mWaitingSubject.onNext(true);
//        back();
    }

    public static Fragment newInstance() {
        return new A400LevelingXYCalibrationCheckPrintFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_leveling_xy_calibration_print;
    }


    @Override
    protected void back() {
        if (mQuitDialog.isShowing()) return;
        mQuitDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        mA400Machine.getFDMController().subscribeExtruderChange();
        mA400Machine.getMachineController().getHeatedBed().subscribeTemperatureChange();
    }

    @Override
    public void onPause() {
        super.onPause();
        mA400Machine.getFDMController().unSubscribeExtruderChange();
        mA400Machine.getMachineController().getHeatedBed().unsubscribeTemperatureChange();
    }

    @OnClick(R2.id.btn_a400_print_setting)
    public void goToSetting() {
        playNormalClickSound();
        mRouter.routeToPrintSetting().start(requireContext());
    }

}
