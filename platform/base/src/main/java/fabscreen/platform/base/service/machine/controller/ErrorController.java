package fabscreen.platform.base.service.machine.controller;

import android.util.SparseArray;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.R;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.MachineFault;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.prop.ArrayProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.lib.SubjectHolder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class ErrorController {
    private final IMachine mMachine;
    private final IAppService mAppService;
    private final IRouter mRouter;
    private final MachineConnectionController mConnectionController;
    private final FDMController mFDMController;
    private final LaserController mLaserController;
    private final CNCController mCNCController;
    private final SparseArray<DecisionDialog> mErrorSparseArray = new SparseArray<>();

    private final CompositeDisposable mDisposables = new CompositeDisposable();

    private boolean lastButtonState = false;
    private final BehaviorSubject<Boolean> ButtonStateBehaviorSubject = BehaviorSubject.create();
    private final SubjectHolder<Boolean> mButtonStateSubjectHolder = new SubjectHolder<>(ButtonStateBehaviorSubject);
    private PublishSubject<AbnormalState> mAbnormalTriggerShow = PublishSubject.create();
    private PublishSubject<AbnormalState> mAbnormalReturnShow = PublishSubject.create();
    private Disposable subscribe;
    private Disposable sub;

    public ErrorController(IMachine iMachine, IAppService appService, MachineConnectionController cc, IRouter iRouter, FDMController fdmController, LaserController laserController, CNCController cncController) {
        mMachine = iMachine;
        mConnectionController = cc;
        mAppService = appService;
        mRouter = iRouter;
        mFDMController = fdmController;
        mLaserController = laserController;
        mCNCController = cncController;

        mDisposables.add(mAbnormalTriggerShow
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(abnormalState -> {
                    Logger.w("Machine error called, state " + abnormalState);
                    if (mAppService.getNowViewContext() == null) {
                        Logger.w("Could not get current view context, went silent.");
                        return;
                    }
                    if (mErrorSparseArray.get(abnormalState.getIndex()) != null && mErrorSparseArray.get(abnormalState.getIndex()).isShowing()) {
                        return;
                    }
                    DecisionDialog decisionDialog;
                    if (iMachine.getMachineInfoSubjectHolder().getValue().seriesId == IMachine.MachineSeries.J) {
                        decisionDialog = DecisionDialog.create(mAppService.getNowViewContext())
                                .setType(DecisionDialog.WARMING_TYPE)
                                .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true);

                        if (!abnormalState.check.isEmpty()) {
                            decisionDialog.setFirstTv(abnormalState.check, R.color.select_dialog_orange_txt, ((dialog, which) -> {
                                dialog.dismiss();
                                mConnectionController.request(0x04, 0x03, new ArrayProp<>(abnormalState.machineFault), new ResponseStructure())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(t -> {
                                        }, LogHelper::log);
                            }));
                        } else {
                            decisionDialog.setFirstTv(R.string.all_j1_i_know, R.color.select_dialog_orange_txt, ((dialog, which) -> {
                                dialog.dismiss();
                                mConnectionController.request(0x04, 0x03, new ArrayProp<>(abnormalState.machineFault), new ResponseStructure())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(t -> {
                                        }, LogHelper::log);
                            }));
                        }

                        if (!abnormalState.title.isEmpty()) {
                            decisionDialog.setTitle(abnormalState.title);
                        }

                        if (!abnormalState.content.isEmpty()) {
                            decisionDialog.setContent(abnormalState.content);
                        }

                        if (abnormalState.contentId == -1) {
                            decisionDialog.setContent(abnormalState.content);
                        } else {
                            decisionDialog.setContent(abnormalState.contentId);
                        }
                    } else {
                        decisionDialog = DecisionDialog.create(mAppService.getNowViewContext())
                                .setType(DecisionDialog.WARMING_TYPE)
                                .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
                                .setFirstTv(R.string.all_close, R.color.select_dialog_white_txt, ((dialog, which) -> {
                                    dialog.dismiss();
                                }));
                    }
                    decisionDialog.show();
                    mErrorSparseArray.put(abnormalState.getIndex(), decisionDialog);

                }));

        mDisposables.add(mAbnormalReturnShow
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(abnormalState -> {
                    DecisionDialog fabConfirm = mErrorSparseArray.get(abnormalState.getIndex());
                    if (fabConfirm != null && fabConfirm.isShowing()) {
                        fabConfirm.dismiss();
                    }
                    mErrorSparseArray.remove(abnormalState.getIndex());
                }));
    }


    public void onEmergencyStop(boolean onStart) {
    }

    public void onAbnormalTrigger(MachineFault machineFault, List<Integer> machineBehavior) {
        if (machineFault.isEmergencyStop()) {
            mRouter.routeToEmergencyStopPage(false).start(mAppService.getNowViewContext());
            return;
        }
        StringBuilder content = new StringBuilder();
        String title = "";
        String check = "";
        int contentId = -1;
        Logger.e("Machine error triggered, %d", machineFault.getValue());
        switch (machineFault.getOwner()) {
            case Module.ModuleType.J1_CONTROL: {
                switch (machineFault.getValue()) {
                    case 1:
                        contentId = R.string.error_motor_drive_is_abnormal;
                        break;
                    case 2:
                        contentId = R.string.error_hot_bed_not_connected;
                        break;
                    case 3:
                        contentId = R.string.error_bed_self_detection_failed_msg;
                        title = mAppService.getAppContext().getString(R.string.error_bed_self_detection_failed_title);
                        break;
                    case 4:
                        contentId = R.string.error_unable_identify_left_extruder;
                        break;
                    case 5:
                        contentId = R.string.error_unable_identify_right_extruder;
                        break;
                    case 6:
                        contentId = R.string.error_unable_identify_double_extruder;
                        break;
                    case 7:
                        contentId = R.string.error_left_extruder_abnormal_temperature;
                        break;
                    case 8:
                        contentId = R.string.error_right_extruder_abnormal_temperature;
                        break;
                    case 9:
                        contentId = R.string.error_double_extruder_abnormal_temperature;
                        break;
                    case 10:
                        title = mAppService.getAppContext().getString(R.string.error_hot_bed_abnormal_temperature_title);
                        contentId = R.string.error_hot_bed_abnormal_temperature;
                        break;
                    case 11:
                        contentId = R.string.error_motor_lose_step;
                        title = mAppService.getAppContext().getString(R.string.error_motor_lose_step_title);
                        break;
                    case 12:
                        contentId = R.string.error_no_feedback_during_heating_left_extruder;
                        title = mAppService.getAppContext().getString(R.string.error_no_feedback_during_heating_left_extruder_title);
                        check = mAppService.getAppContext().getString(R.string.error_all_aware_danger);
                        break;
                    case 13:
                        contentId = R.string.error_no_feedback_during_heating_right_extruder;
                        title = mAppService.getAppContext().getString(R.string.error_no_feedback_during_heating_right_extruder_title);
                        check = mAppService.getAppContext().getString(R.string.error_all_aware_danger);
                        break;
                    case 14:
                        contentId = R.string.error_no_feedback_during_heating_hot_bed;
                        title = mAppService.getAppContext().getString(R.string.error_no_feedback_during_heating_hot_bed_title);
                        check = mAppService.getAppContext().getString(R.string.error_all_aware_danger);
                        break;
                    case 15:
                        contentId = R.string.error_no_feedback_during_heating_double_extruder;
                        title = mAppService.getAppContext().getString(R.string.error_no_feedback_during_heating_double_extruder_title);
                        check = mAppService.getAppContext().getString(R.string.error_all_aware_danger);
                        break;
                    case 16:
                        contentId = R.string.error_abnormal_x_motor_drive;
                        break;
                    case 17:
                        contentId = R.string.error_abnormal_y_motor_drive;
                        break;
                    case 18:
                        contentId = R.string.error_abnormal_z_motor_drive;
                        break;
                    case 19:
                        contentId = R.string.error_abnormal_left_motor_drive;
                        break;
                    case 20:
                        contentId = R.string.error_abnormal_right_motor_drive;
                        break;
                    case 21:
                        contentId = R.string.error_abnormal_double_motor_drive;
                        break;
                    case 22:
                        contentId = R.string.error_left_hot_end_detection_abnormal;
                        break;
                    case 23:
                        contentId = R.string.error_right_hot_end_detection_abnormal;
                        break;
                    default:
                        content.append("Undefined error：" + machineFault.getValue());
                        break;
                }
            }
            break;
            default:
                content.append("Error:\t");
                content.append(machineFault).append("\nError State:");
                for (int i = 0; i < machineBehavior.size(); i++) {
                    if (machineBehavior.get(i) == -1) continue;
                    content.append(machineBehavior.get(i)).append(" ");
                }
                break;
        }
        if (contentId == -1) {
            mAbnormalTriggerShow.onNext(new AbnormalState(machineFault, title, content.toString(), check));
        } else {
            mAbnormalTriggerShow.onNext(new AbnormalState(machineFault, title, contentId, check));
        }
    }

    public void onAbnormalReturn(MachineFault machineFault, List<Integer> machineBehavior) {
        if (machineFault.isEmergencyStop()) {
            if (sub != null && !sub.isDisposed()) sub.dispose();
            sub = Observable.timer(10, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(t -> {
                        mAppService.restart();
                    }, LogHelper::log);
            return;
        }
        mAbnormalReturnShow.onNext(new AbnormalState(machineFault, "", "", ""));
    }

    public Observable<ResponseStructure> queryException() {
        ResponseStructure iStructureResponseStructure = new ResponseStructure();
        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("exceptionInfos", new ArrayProp<>(new MachineFault()));
                addProp("machineBehaviorStates", new ArrayProp<>(new UInt8Prop(-1)));
            }
        };
        iStructureResponseStructure.dataProp = baseStructure;
        return mConnectionController.request(0x04, 0x02, null, iStructureResponseStructure)
                .doOnNext(responseStructure -> {
                    BaseStructure baseStructure1 = (BaseStructure) responseStructure.dataProp;
                    List<MachineFault> exceptionInfos = ((ArrayProp<MachineFault>) baseStructure1.getProp("exceptionInfos")).getValue();
                    List<UInt8Prop> machineBehaviorStates = ((ArrayProp<UInt8Prop>) baseStructure1.getProp("machineBehaviorStates")).getValue();
                    List<Integer> machineBehaviors = new ArrayList<>();
                    if (exceptionInfos.size() == 1 && exceptionInfos.get(0).getOwner() == -1) {
                        return;
                    }
                    for (int i = 0; i < machineBehaviorStates.size(); i++) {
                        machineBehaviors.add(machineBehaviorStates.get(i).getValue());
                    }
                    for (int i = 0; i < exceptionInfos.size(); i++) {
                        onAbnormalTrigger(exceptionInfos.get(i), machineBehaviors);
                    }
                });
    }


    public Observable<ResponseStructure> TestException() {
        return mConnectionController.request(0x04, 0x0a, null, new ResponseStructure());
    }

    static class AbnormalState {
        MachineFault machineFault;
        String title = "";
        String content = "";
        int contentId = -1;
        String check = "";

//        public AbnormalState(MachineFault machineFault, String content) {
//            this.machineFault = machineFault;
//            this.content = content;
//        }

        public AbnormalState(MachineFault machineFault, String title, String content, String check) {
            this.machineFault = machineFault;
            this.title = title;
            this.content = content;
            this.check = check;
        }

        public AbnormalState(MachineFault machineFault, String title, int contentId, String check) {
            this.machineFault = machineFault;
            this.title = title;
            this.contentId = contentId;
            this.check = check;
        }

        public int getIndex() {
            return machineFault.getOwner() << 24 | machineFault.getLevel() << 8 | machineFault.getValue();
        }

        @Override
        public String toString() {
            return "AbnormalState{" +
                    "machineFault=" + machineFault +
                    ", title='" + title + '\'' +
                    ", content='" + content + '\'' +
                    ", contentId=" + contentId +
                    ", check='" + check + '\'' +
                    '}';
        }
    }
}
