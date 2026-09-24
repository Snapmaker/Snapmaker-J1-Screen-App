package fabscreen.features.machinetools.calibration.j1Platform.LevelingXY;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.J1CalibrationBaseFragment;
import fabscreen.platform.base.BaseApplication;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.parser.GcodeParser;
import fabscreen.platform.base.lib.parser.IGcodeParser;
import fabscreen.platform.base.lib.print.PrintListener;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.controller.PrintController;
import fabscreen.platform.base.view.DecisionDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class LevelingXYCalibrationPrintFragment extends J1CalibrationBaseFragment {
    private static final int STATUS_IDLE = 0;
    private static final int STATUS_PRINTING = 1;
    private static final int STATUS_PAUSED = 2;
    private static final int STATUS_COMPLETED = 3;

    @BindView(R2.id.btn_next)
    Button mBtNext;
    @BindView(R2.id.tv_j1_leveling_xy_calibration_progress)
    TextView mTvProgress;
    @BindView(R2.id.tv_j1_leveling_xy_calibration_time)
    TextView mTvRemainingTime;

    private int mState = 0;
    private int mMockPrint = 0;
    private PrintController mPrintController;
    private BehaviorSubject<Integer> mUpdateViewSubject = BehaviorSubject.createDefault(0);
    private BehaviorSubject<Boolean> mWaitingSubject = BehaviorSubject.createDefault(false);

    public static Fragment newInstance() {
        return new LevelingXYCalibrationPrintFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPrintController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();

        mUpdateViewSubject
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(integer -> {
                    mState = integer;
                    switch (integer) {
                        case STATUS_PAUSED:
                            mBtNext.setText(R.string.all_resume);
                            break;
                        case STATUS_IDLE:
                        case STATUS_PRINTING:
                            mBtNext.setText(R.string.all_stop);
                            break;
                    }
                });
        initPrintFile();

        initView();
    }

    private void initPrintFile() {
        // Read the built-in gcode file in the form of inputStream
        // need to correctly declare the print type and use it as a built-in print job
        InputStream is = getResources().openRawResource(R.raw.a400_xy);
        IGcodeParser parser = new GcodeParser();
        parser.startParse(is, IMachine.WorkType.FDM);
        mPrintController.setInputStream(is);
        mPrintController.setTotalLines(parser.getTotalLinesCount());
        mPrintController.setListener(new PrintListener() {
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
                            ServiceContainer.getInstance().getService(IRouter.class).backHome().start(requireContext());
                        }));
                switch (retCode) {
                    case 202: {
                        Logger.w("Filament used out, unable to start printing.");
                        stopPrint();
                        break;
                    }
                    case 227: {
//                        The Enclosure door is opened, so the {流程} has been stopped.
                        decisionDialog.setTitle(getString(R.string.a400_print_enclosure_open, getString(R.string.a400_print_start_print)));
                    }
                    break;
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
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, false)
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setFirstTv(R.string.a400_print_got_it, R.color.select_dialog_blue_txt, ((dialog, which) -> {
                            stopPrint();
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
                stopPrint();
            }

            @Override
            public void onResumeFromPowerOutageFailed(int retCode) {
                DecisionDialog decisionDialog = DecisionDialog.create(getContext())
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, false)
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setFirstTv(R.string.a400_print_got_it, R.color.select_dialog_blue_txt, ((dialog, which) -> {
                            stopPrint();
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
                stopPrint();
            }

            @Override
            public void onStopFailed(int retCode) {
                mWaitingSubject.onNext(false);
                stopPrint();
            }

            @Override
            public void onFinishSuccess() {
                Logger.i("Print Finished.");
                mWaitingSubject.onNext(false);
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().stop();
                Logger.d("Print job costs %s.", BaseApplication.formatTime(ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().getCount()));
                if (getActivity() == null) return;
                ((LevelingXYCalibrationActivity) getActivity()).gotoLevelingXYCalibration2();
            }

            @Override
            public void onFinishFailed(int retCode) {
                mWaitingSubject.onNext(false);
                stopPrint();
            }
        });

        mWaitingSubject.onNext(true);
        mPrintController.start();
        mPrintController.getFilamentSubjectObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(triggered -> {
                    if (triggered) {
                        int sumFilamentStatus = 0;
                        sumFilamentStatus += ServiceContainer.getInstance().getService(IMachine.class).getFDMController().getToolheadStatusSubjectHolder(0)
                                .getValue().getExtruderList().get(0).getFilamentStatus() ? 1 : 0;
                        sumFilamentStatus += ServiceContainer.getInstance().getService(IMachine.class).getFDMController().getToolheadStatusSubjectHolder(1)
                                .getValue().getExtruderList().get(0).getFilamentStatus() ? 2 : 0;

                        DecisionDialog decisionDialog = DecisionDialog.create(requireContext())
                                .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, true)
                                .setTitle(R.string.control_load_filament_failed)
                                .setType(DecisionDialog.WARMING_TYPE)
                                .setFirstTv(R.string.all_ok, R.color.select_dialog_orange_txt, (dialog, which) -> {
                                    mPrintController.setFilament(true);
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
    }


    @OnClick(R2.id.btn_next)
    void onClickNext() {
        playNormalClickSound();
        switch (mState) {
            case STATUS_PRINTING:
                mPrintController.pause();
                break;
            case STATUS_IDLE:
            case STATUS_PAUSED:
                mPrintController.stop();
                break;
        }
    }

    private void initView() {
        // 假设开始打印
        Observable.interval(2, TimeUnit.SECONDS)
                .flatMap(time -> ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getPrintStateObservable())
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(status -> {
                    mUpdateViewSubject.onNext(status);
                    updateProgress();
                });

    }

    private void stopPrint() {
        if (getActivity() != null) {
            Logger.d("Route: Back from " + getClass().getSimpleName());
            getActivity().onBackPressed();
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_leveling_xy_calibration_print;
    }


    private void updateProgress() {
        float p = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getProgress();

        // formula: remaining = (1 - p) * p * elapsed / p + (1 - p) * (1 - p) * ETA
        int elapsed = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getTickCounter().getCount();
        int remaining = (int) ((1 - p) * elapsed);
        mTvRemainingTime.setText(BaseApplication.formatTime(remaining));

        final int percentage = (int) (100 * p);
        mTvProgress.setText(String.valueOf(percentage));
    }

    //    @OnClick(R2.id.btn_temp)
    public void onCheckTemp() {
        mPrintController.finish();
    }
}
