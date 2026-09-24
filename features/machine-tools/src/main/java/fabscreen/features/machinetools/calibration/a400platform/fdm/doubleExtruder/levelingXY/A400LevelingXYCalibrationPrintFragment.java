package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingXY;

import static fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingXY.A400LevelingXYCalibrationInfoFragment.A400_LEVELING_XY_CALIBRATION_ABS;
import static fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingXY.A400LevelingXYCalibrationInfoFragment.A400_LEVELING_XY_CALIBRATION_CUSTOM;
import static fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingXY.A400LevelingXYCalibrationInfoFragment.A400_LEVELING_XY_CALIBRATION_PETG;
import static fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingXY.A400LevelingXYCalibrationInfoFragment.A400_LEVELING_XY_CALIBRATION_PLA;
import static fabscreen.platform.base.service.machine.controller.PrintController.STATE_IDLE;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP_DOUBLE_EXTRUDER;
import static fabscreen.platform.base.service.machine.entity.parts.Extruder.EXTRUDER_DIAMETER_0_2;
import static fabscreen.platform.base.service.machine.entity.parts.Extruder.EXTRUDER_DIAMETER_0_4;
import static fabscreen.platform.base.service.machine.entity.parts.Extruder.EXTRUDER_DIAMETER_0_6;
import static fabscreen.platform.base.service.machine.entity.parts.Extruder.EXTRUDER_DIAMETER_0_8;
import static fabscreen.platform.base.service.machine.entity.parts.Extruder.EXTRUDER_LEFT;
import static fabscreen.platform.base.service.machine.entity.parts.Extruder.EXTRUDER_RIGHT;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
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
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.controller.PrintController;
import fabscreen.platform.base.service.machine.entity.module.HeatedBed;
import fabscreen.platform.base.service.machine.entity.parts.Extruder;
import fabscreen.platform.base.service.machine.entity.toolhead.FdmToolhead;
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

public class A400LevelingXYCalibrationPrintFragment extends A400CalibrationBaseFragment {
    private static final int STATUS_IDLE = 0;
    private static final int STATUS_PRINTING = 1;
    private static final int STATUS_PAUSED = 2;
    private static final int STATUS_COMPLETED = 3;

    private static final int STATUS_ERROR = 2;
    private static final int STATUS_STOP_SUCCESS = 1;

    @BindView(R2.id.iv_print_file_diagram)
    ImageView mIvPrintFileDiagram;
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
    private final BehaviorSubject<Integer> mPrintStatusSubject = BehaviorSubject.createDefault(STATUS_IDLE);
    Disposable mDisCheckoutExtruderTemperature;
    Disposable mDisCheckFilamentProcess;
    StepIntroductionDialog checkFilamentProcessTipDialog;
    IPreferences mPreference;
    private DecisionDialog mQuitDialog;

    private final BehaviorSubject<Boolean> mStartSubject = BehaviorSubject.createDefault(false);
    private final PublishSubject<Boolean> mHeatingSubject = PublishSubject.create();
    int mLeftPrintingTemperature;
    int mRightPrintingTemperature;
    int mBedPrintingTemperature;
    private boolean mHeatReady = false;
    private boolean mFileReady = false;
    public PublishSubject<Boolean> mFinishSubject = PublishSubject.create();
    private boolean isHaveCheck;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mA400Machine = ServiceContainer.getInstance().getService(IMachine.class);
        mRouter = ServiceContainer.getInstance().getService(IRouter.class);
        mWorkspace = ServiceContainer.getInstance().getService(IPrintWorkspace.class);
        mPrintController = mA400Machine.getPrintController();
        mParser = ServiceContainer.getInstance().getService(IGcodeParser.class);
        mPreference = ServiceContainer.getInstance().getService(IPreferences.class);

        initView();
        int a400BevelingXYMaterialSelection = mPreference.getHelper().getA400BevelingXYMaterialSelection();
        mLeftPrintingTemperature = mPreference.getHelper().getA400LevelingXYCalibrationLeftPrintingTemperature();
        mRightPrintingTemperature = mPreference.getHelper().getA400LevelingXYCalibrationRightPrintingTemperature();
        mBedPrintingTemperature = mPreference.getHelper().getA400LevelingXYCalibrationBedPrintingTemperature();
        switch (a400BevelingXYMaterialSelection) {
            case A400_LEVELING_XY_CALIBRATION_PLA:
                mLeftPrintingTemperature = 210;
                mRightPrintingTemperature = 210;
                mBedPrintingTemperature = 60;
                break;
            case A400_LEVELING_XY_CALIBRATION_PETG:
                mLeftPrintingTemperature = 230;
                mRightPrintingTemperature = 230;
                mBedPrintingTemperature = 80;
                break;
            case A400_LEVELING_XY_CALIBRATION_ABS:
                mLeftPrintingTemperature = 235;
                mRightPrintingTemperature = 235;
                mBedPrintingTemperature = 80;
                break;
            case A400_LEVELING_XY_CALIBRATION_CUSTOM:
            default:
                break;
        }
        setPrintControllerListener(mPrintController);
        mQuitDialog = DecisionDialog.create(getActivity())
                .setDialogStatus(DecisionDialog.BTN_TWO, false, false, false, true)
                .setType(DecisionDialog.WARMING_TYPE)
                .setContent(getString(R.string.assistant_back_notice, getString(R.string.calibration_a400_leveling_xy_title)))
                .setFirstTv(getContext().getResources().getString(R.string.all_cancel), R.color.select_dialog_white_txt, ((dialog, which) -> {
                    dialog.dismiss();
                }))
                .setSecondTv(getContext().getResources().getString(R.string.all_yes), R.color.select_dialog_white_txt, (dialog, which) -> {
                    mQuitDialog.mCancelBtn.setEnabled(false);
                    mQuitDialog.mSecondBtn.setEnabled(false);
                    if (mStartSubject.getValue()) {
                        mPrintController.stop();
                        mStartSubject.onNext(false);
                    } else {
                        mQuitDialog.dismiss();
                        mStartSubject.onNext(false);
                        mFinishSubject.onNext(false);
                    }

                });

        mPrintStatusSubject
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(integer -> {
                    switch (integer) {
                        case STATUS_IDLE:
                            break;
                        case STATUS_COMPLETED:
                            ((A400LevelingXYCalibrationActivity) requireActivity()).gotoAdjustX();
                            break;
                        case STATUS_STOP_SUCCESS:
                        default:
                            Logger.d("Now PrintStatus is: %d ", integer);
                            mStartSubject.onNext(false);
                            break;
                    }
                });
        checkHome()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        mHeatingSubject.onNext(aBoolean);
                        initFile();
                    }
                });
        Observable<FdmToolhead.FdmToolheadStatus> fdmToolHeadObservable = mA400Machine.getFDMController()
                .getToolheadStatusSubjectHolder(0)
                .getObservable();
        Observable<HeatedBed.HeatedBedStatus> bedObservable = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getHeatedBed()
                .getHeatedBedStatusSubjectHolder().getObservable();

        Observable.zip(fdmToolHeadObservable, bedObservable, (fdmToolHeadInfo, bedInfo) -> {
            if (mHeatReady) return mHeatReady;
            Extruder leftExtruder = fdmToolHeadInfo.getExtruderList().get(0);
            int leftTemperature = (int) leftExtruder.getTemperature();
            int leftTargetTemperature = (int) leftExtruder.getTargetTemperature();
            Extruder rightExtruder = fdmToolHeadInfo.getExtruderList().get(1);
            int rightTemperature = (int) rightExtruder.getTemperature();
            int rightTargetTemperature = (int) rightExtruder.getTargetTemperature();
            HeatedBed.ZoneInfo zoneInfo = bedInfo.getZoneList().get(0);
            float zoneCurrentTemperature = zoneInfo.getCurrentTemperature();
            int zoneTargetTemperature = zoneInfo.getTargetTemperature();
            if (leftTargetTemperature <= 0 || rightTargetTemperature <= 0) {
                mHeatReady = false;
            } else {
                mHeatReady = (rightTemperature >= rightTargetTemperature - 3) && (leftTemperature >= leftTargetTemperature - 3) && (zoneTargetTemperature >= zoneCurrentTemperature - 3);
            }
            return mHeatReady;
        }).distinctUntilChanged().as(bindToLifecycle()).subscribe(b -> mStartSubject.onNext(mFileReady && mHeatReady), LogHelper::log);

        mStartSubject.observeOn(AndroidSchedulers.mainThread())
                .distinctUntilChanged()
                .as(bindToLifecycle())
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        initPrint();
                    }
                });

        mHeatingSubject
                .flatMap(b -> mA400Machine.getFDMController().setExtruderTemperature(0, 0, mLeftPrintingTemperature))
                .flatMap(b -> mA400Machine.getFDMController().setExtruderTemperature(0, 1, mRightPrintingTemperature))
                .flatMap(b -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getHeatedBed().setAllTargetTemperature(mBedPrintingTemperature))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aBoolean -> {
                }, LogHelper::log);

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
        if (mA400Machine.getFDMController().getHeadType() == HEAD_3DP_DOUBLE_EXTRUDER) {
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
                                .setContent(getString(R.string.print_warning_filament_run_out_extruder_content,
                                        mFilamentStateSubject.getValue().getFailureFilamentIndex() == 0 ?
                                                getString(R.string.all_left_extruder) :
                                                getString(R.string.all_right_extruder)))
                                .setType(DecisionDialog.WARMING_TYPE)
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

    public Observable<Boolean> checkHome() {
        IMachine service = ServiceContainer.getInstance().getService(IMachine.class);
        if (!service.getMachineStatusSubjectHolder().getValue().isHomed) {
            return service.getMachineController().updateCoordinateSystem(0)
                    .flatMap(machineStatus -> service.getMachineController().home(0))
                    .flatMap(integer -> Observable.just(integer == 0));
        } else {
            return Observable.just(true);
        }
    }

    private void initFile() {
        List<Extruder> extruderList = mA400Machine.getFDMController().getToolheadStatusSubjectHolder(0).getValue().getExtruderList();
        float leftDiameter = extruderList.get(EXTRUDER_LEFT).getDiameter();
        float rightDiameter = extruderList.get(EXTRUDER_RIGHT).getDiameter();
        int printFileId = -1;
        String fileName = "calibrationXY";
        // FIXME: Consider matching as a string concatenation?
        if (leftDiameter == EXTRUDER_DIAMETER_0_2) {
            fileName += "_02";
            if (rightDiameter == EXTRUDER_DIAMETER_0_2) {
                fileName += "_02";
                printFileId = R.raw.a400_eveling_xy_calibration_on_print_02_02;
            } else if (rightDiameter == EXTRUDER_DIAMETER_0_4) {
                fileName += "_04";
                printFileId = R.raw.a400_eveling_xy_calibration_on_print_02_04;
            } else if (rightDiameter == EXTRUDER_DIAMETER_0_6) {
                fileName += "_06";
                printFileId = R.raw.a400_eveling_xy_calibration_on_print_02_06;
            } else if (rightDiameter == EXTRUDER_DIAMETER_0_8) {
                fileName += "_08";
                printFileId = R.raw.a400_eveling_xy_calibration_on_print_02_08;
            }
        } else if (leftDiameter == EXTRUDER_DIAMETER_0_4) {
            fileName += "_04";
            if (rightDiameter == EXTRUDER_DIAMETER_0_2) {
                fileName += "_02";
                printFileId = R.raw.a400_eveling_xy_calibration_on_print_04_02;
            } else if (rightDiameter == EXTRUDER_DIAMETER_0_4) {
                fileName += "_04";
                printFileId = R.raw.a400_eveling_xy_calibration_on_print_04_04;
            } else if (rightDiameter == EXTRUDER_DIAMETER_0_6) {
                fileName += "_06";
                printFileId = R.raw.a400_eveling_xy_calibration_on_print_04_06;
            } else if (rightDiameter == EXTRUDER_DIAMETER_0_8) {
                fileName += "_08";
                printFileId = R.raw.a400_eveling_xy_calibration_on_print_04_08;
            }
        } else if (leftDiameter == EXTRUDER_DIAMETER_0_6) {
            fileName += "_06";
            if (rightDiameter == EXTRUDER_DIAMETER_0_2) {
                fileName += "_02";
                printFileId = R.raw.a400_eveling_xy_calibration_on_print_06_02;
            } else if (rightDiameter == EXTRUDER_DIAMETER_0_4) {
                fileName += "_04";
                printFileId = R.raw.a400_eveling_xy_calibration_on_print_06_04;
            } else if (rightDiameter == EXTRUDER_DIAMETER_0_6) {
                fileName += "_06";
                printFileId = R.raw.a400_eveling_xy_calibration_on_print_06_06;
            } else if (rightDiameter == EXTRUDER_DIAMETER_0_8) {
                fileName += "_08";
                printFileId = R.raw.a400_eveling_xy_calibration_on_print_06_08;
            }
        } else if (leftDiameter == EXTRUDER_DIAMETER_0_8) {
            fileName += "_08";
            if (rightDiameter == EXTRUDER_DIAMETER_0_2) {
                fileName += "_02";
                printFileId = R.raw.a400_eveling_xy_calibration_on_print_08_02;
            } else if (rightDiameter == EXTRUDER_DIAMETER_0_4) {
                fileName += "_04";
                printFileId = R.raw.a400_eveling_xy_calibration_on_print_08_04;
            } else if (rightDiameter == EXTRUDER_DIAMETER_0_6) {
                fileName += "_06";
                printFileId = R.raw.a400_eveling_xy_calibration_on_print_08_06;
            } else if (rightDiameter == EXTRUDER_DIAMETER_0_8) {
                fileName += "_08";
                printFileId = R.raw.a400_eveling_xy_calibration_on_print_08_08;
            }
        }
        if (printFileId == -1) {
            Toast.makeText(getContext(), String.format("當前無法識別到可用文件，左噴嘴直徑為：%.2f,右噴子直徑為：%.2f", leftDiameter, rightDiameter), Toast.LENGTH_SHORT).show();
        }
        fileName += ".gcode";
        File printFile = copyPrintFile(printFileId, fileName);
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

    void handleResult(IFile file) {
        mTvFilename.setText(file.getName());
        mIvPrintFileDiagram.setImageBitmap(ServiceContainer.getInstance().getService(IGcodeParser.class).getGcodeThumbnail());
        mWorkspace.setPrintMode(IPrintWorkspace.PRINT_MODE_NORMAL);
        mWorkspace.setPrintSource(0);
        mWorkspace.setFileTotalLineCount(mParser.getTotalLinesCount());
        mWorkspace.setEstimatedTime(mParser.getEstimatedTime());
        mWorkspace.setFileMD5Value("c319528c5c360d46031b69d39e01ceb3");
        mEstimatedTime = mWorkspace.getEstimatedTime();
        if (mParser.getFileType() == IMachine.WorkType.FDM) {
            if (mParser.getHeaderType() == HEAD_3DP) {
                mWorkspace.setWorkTemperature(new float[]{mParser.getNozzleTargetTemperature()});
            } else if (mParser.getHeaderType() == HEAD_3DP_DOUBLE_EXTRUDER) {
                mWorkspace.setWorkTemperature(new float[]{mParser.getNozzleTargetTemperature(), mParser.getNozzleTarget_1_Temperature()});
            }
        }
        mWorkspace.addFileToWorkspace(file)
                .observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe(success -> {
            mFileReady = success;
            mStartSubject.onNext(mFileReady && mHeatReady);
        }, LogHelper::log);
    }

    private File copyPrintFile(int printFileId, String fileName) {
        InputStream is = getResources().openRawResource(printFileId);
        File file = null;
        try {
            file = new File(getContext().getCacheDir().getAbsoluteFile() + "/" + fileName);
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

    private void initView() {
        if (getArguments() != null) {
            isHaveCheck = getArguments().getBoolean("is_have_check", false);
        }
        if (isHaveCheck) {
            mTvTopBarContent.setText(R.string.print_print_calibration_models);
            mGuideProgressBar.setMax(6);
        } else {
            mTvTopBarContent.setText(R.string.print_print_calibration_models_4);
            mGuideProgressBar.setMax(4);
        }
        mGuideProgressBar.setVisibility(View.VISIBLE);
        setTitle(R.string.print_xy_offset_calibration_title);
        mGuideProgressBar.setProgress(1);

        mBtnStop.setVisibility(Button.VISIBLE);
        init3DPPanel();
    }

    void initPrint() {
        boolean isPrinting = (mPrintController.getPrintState() == PrintController.STATE_PRINTING || mPrintController.getPrintState() == PrintController.STATE_PAUSED);
        if (!isPrinting) {
            AndroidSchedulers.mainThread().scheduleDirect(this::startPrint, 300, TimeUnit.MILLISECONDS);
        }

        ServiceContainer.getInstance().getService(IMachine.class)
                .getMachineStatusSubjectHolder()
                .getObservable()
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(machineStatus -> {
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
    }

    private void startPrint() {
        mCompositeDisposable.clear();
//        PrintController printController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();
        IFile file1 = mWorkspace.getPrintFile();
        mPrintController.reset();
        mPrintController.setFile(file1);
        mPrintController.setTotalLines(mWorkspace.getFileTotalLineCount());
//        setPrintControllerListener(printController);

        // Power Panic
        mWaitingSubject.onNext(true);
        boolean powerOutageFlag = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getRecoveryFlag();

        if (powerOutageFlag) {
            Logger.d("Try Power Loss recovering..");
            mPrintController.recover();
        } else {
            mPrintController.start();
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
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, false)
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setFirstTv(R.string.a400_print_got_it, R.color.select_dialog_blue_txt, ((dialog, which) -> {
                            dialog.dismiss();
                            mFinishSubject.onNext(false);
                        }));
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
                    case 227: {
                        decisionDialog.setTitle(getString(R.string.a400_print_enclosure_open, getString(R.string.a400_print_start_print)));
                    }
                    default: {
                        Logger.w("Unable to start printing, ret code %d", retCode);
                        decisionDialog.setTitle(getString(R.string.print_warning_start_unable) + "\nretCode:" + retCode);
                        break;
                    }
                }
                decisionDialog.show();
                FabConfirm.create(getContext())
                        .setDescription(R.string.print_warning_start_unable)
                        .setConfirm(R.string.all_confirm, (dialog, which) -> {
                            dialog.dismiss();

                        })
                        .show();
            }

            @Override
            public void onPauseSuccess() {
                Logger.i("Print paused.");
                if (mQuitDialog != null && mQuitDialog.isShowing()) mQuitDialog.dismiss();
                mWaitingSubject.onNext(false);
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().stop();
            }

            @Override
            public void onPauseFailed(int retCode) {
                Logger.w("Unable to pause printing.");
                mWaitingSubject.onNext(false);
                // Just a confirm
                DecisionDialog decisionDialog = DecisionDialog.create(getContext())
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, false)
                        .setType(DecisionDialog.WARMING_TYPE)
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
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, false)
                        .setType(DecisionDialog.WARMING_TYPE)
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
                if (mQuitDialog != null && mQuitDialog.isShowing()) mQuitDialog.dismiss();
                mWaitingSubject.onNext(false);
                Logger.i("print stopped.");
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().stop();
                mFinishSubject.onNext(false);
            }

            @Override
            public void onStopFailed(int retCode) {
                Logger.w("Unable to stop printing, ret code %d", retCode);
                mWaitingSubject.onNext(false);
                DecisionDialog.create(getContext())
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, true)
                        .setTitle(R.string.print_warning_stop_unable)
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setFirstTv(R.string.a400_print_got_it, R.color.select_dialog_blue_txt, ((dialog, which) -> {
                            dialog.dismiss();
                            mFinishSubject.onNext(false);
                        })).show();
            }

            @Override
            public void onFinishSuccess() {
                if (mQuitDialog != null && mQuitDialog.isShowing()) mQuitDialog.dismiss();
                Logger.i("Print Finished.");
                mWaitingSubject.onNext(false);
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().stop();
                mPrintStatusSubject.onNext(STATUS_COMPLETED);

//                Logger.d("Print job costs %s.", BaseApplication.formatTime(ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().getCount()));
                // Finish Print.
//                ((PrintActivity) requireActivity()).gotoPrintCompleteFragment();
            }

            @Override
            public void onFinishFailed(int retCode) {
                Logger.w("Unable to finish printing, ret code %d", retCode);
                mWaitingSubject.onNext(false);
                DecisionDialog.create(getContext())
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, true)
                        .setTitle(R.string.print_warning_finish_unable)
                        .setFirstTv(R.string.a400_print_got_it, R.color.select_dialog_blue_txt, ((dialog, which) -> {
                            dialog.dismiss();
                            mFinishSubject.onNext(false);
                        })).show();
            }
        });
    }

    private void updateProgress() {
        float p = mPrintController.getProgress();

        // formula: remaining = (1 - p) * p * elapsed / p + (1 - p) * (1 - p) * ETA
        int elapsed = mPrintController.getTickCounter().getCount();
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
        return new A400LevelingXYCalibrationPrintFragment();
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
