package fabscreen.features.settings.j1.attendance;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.controller.FDMController;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.base.view.FileLoadingDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class J1SettingsHotEndPIDAutoTuneFragment extends BaseFragment {
    private final int GOOD_DISTANCE = 7;

    @BindView(R2.id.top_bar_back)
    Button mBtnBack;
    @BindView(R2.id.iv_left_hot_end_tune)
    ImageView mBtnLeft;
    @BindView(R2.id.iv_right_hot_end_tune)
    ImageView mBtnRight;

    FileLoadingDialog mFabWaitingDialog;
    DecisionDialog mResultDialog;

    private int mCurrentTuningExtruder = -1;
    private FDMController mFDMController;
    private MachineController mMachineController;

    private BehaviorSubject<Boolean> mWaitingSubject = BehaviorSubject.createDefault(false);

    public static Fragment newInstance() {
        return new J1SettingsHotEndPIDAutoTuneFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_settings_hot_end_pid_auto_tune;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();

        mFDMController = ServiceContainer.getInstance().getService(IMachine.class).getFDMController();
        mMachineController = ServiceContainer.getInstance().getService(IMachine.class).getMachineController();

        ServiceContainer.getInstance().getService(IMachine.class)
                .getMachineStatusSubjectHolder()
                .getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(machineStatus -> {
//                    MachineOperationStatus statusName = MachineOperationStatus.valueOf(machineStatus.status);
//                    if (statusName == null) return;
//                    Logger.d("Current machineStatus %1$d, %2$s" + machineStatus.status, statusName.name());
                }, LogHelper::log);

        mWaitingSubject.observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(waiting -> {
                    mBtnBack.setEnabled(!waiting);
                    mBtnLeft.setEnabled(!waiting);
                    mBtnRight.setEnabled(!waiting);
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        unSubscribeDataChange();
    }

    @Override
    public void onResume() {
        super.onResume();
        subscribeDataChange();
    }

    private void initView() {
        mFabWaitingDialog = FileLoadingDialog.create(requireContext(), true);
        mFabWaitingDialog.setContent(getString(R.string.j1_dialog_settings_hot_end_pid_auto_tuning));

        mResultDialog = DecisionDialog.create(requireContext())
                .setDialogStatus(1, false, false, false, true)
                .setFirstTv(R.string.all_ok, R.color.select_dialog_white_txt, ((dialog, which) -> {
                    dialog.dismiss();
                    back();
                }));
    }

    private void startCheck(int index) {
        mCurrentTuningExtruder = index;
        checkMove(index)
                .flatMap(needed -> {
                    if (needed) {
                        return moveToGoodXPosition(index);
                    } else {
                        return Observable.just(true);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(result -> {
                    mWaitingSubject.onNext(false);
                    if (result) {
                        startExtruderPIDAutoTune(index);
                    } else {
                        Logger.e("Move to position failed");
                    }
                }, LogHelper::log);

    }

    public Observable<Boolean> checkMove(int index) {
        return mMachineController.getMotorStateObservable()
                .flatMap(enabled -> {
                    if (enabled) {
                        Vector vector = mMachineController.getRelativeHomeLocationSubject().getValue();
                        if (index == 0) {
                            return Observable.just(vector.getX() < GOOD_DISTANCE);
                        } else {
                            return Observable.just(vector.getX2() > -GOOD_DISTANCE);
                        }
                    } else {
                        // motor not enabled, ignore position
                        return Observable.just(false);
                    }
                });
    }

    public Observable<Boolean> moveToGoodXPosition(int index) {
        mWaitingSubject.onNext(true);
        AndroidSchedulers.mainThread().scheduleDirect(this::showWaitingDialog, 100, TimeUnit.MILLISECONDS);
        return mMachineController.homeIfNotYet(0)
                .flatMap(integer -> {
                    Vector vector = new Vector();
                    vector.setX(GOOD_DISTANCE);
                    return mMachineController.MoveRelativeHome(vector, 0);
                })
                .flatMap(responseStructure -> Observable.just(responseStructure.isSuccess()));
    }

    public void subscribeDataChange() {
        mFDMController.subscribeExtruderChange();
        mMachineController.subscribeRelativeHomeLocation().observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe(responseStructure -> {
        }, LogHelper::log);
    }

    public void unSubscribeDataChange() {
        mFDMController.unSubscribeExtruderChange();
        mMachineController.unSubscribeRelativeHomeLocation().observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe(responseStructure -> {
        }, LogHelper::log);
    }

    private void showWaitingDialog() {
        if (!mFabWaitingDialog.isShowing()) {
            if (mCurrentTuningExtruder != -1) {
                mFabWaitingDialog.setContent(mCurrentTuningExtruder == 0 ?
                        getString(R.string.j1_dialog_settings_left_hot_end_pid_auto_tuning)
                        : getString(R.string.j1_dialog_settings_right_hot_end_pid_auto_tuning));
            }
            mFabWaitingDialog.show();
        }
    }

    private void startExtruderPIDAutoTune(int id) {
        mWaitingSubject.onNext(true);
        showWaitingDialog();
        mFDMController.setCalibrationMode(150)
                .doOnNext(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        Logger.d("set PID Auto Tune calibration mode success");
                    } else {
                        Logger.d("set PID Auto Tune calibration mode failed, ret %d", responseStructure.resultProp.getValue());
                    }
                })
                .flatMap(response -> response.isSuccess() ? mFDMController.startHotEndPIDAutoTune(id, 0) : Observable.just(response))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    mWaitingSubject.onNext(false);
                    if (mFabWaitingDialog.isShowing()) {
                        mFabWaitingDialog.dismiss();
                    }

                    if (responseStructure.isSuccess()) {
                        Logger.d("%s extruder PID auto tune success", id == 0 ? "Left" : "Right");
                    } else {
                        Logger.d("%s extruder PID auto tune failed, ret %d", id == 0 ? "Left" : "Right", responseStructure.resultProp.getValue());
                    }

                    String content = getString(responseStructure.isSuccess()
                            ? R.string.j1_dialog_settings_hot_end_pid_auto_tune_success
                            : R.string.j1_dialog_settings_hot_end_pid_auto_tune_failed);
                    mResultDialog.setContent(content);
                    mResultDialog.show();
                }, e -> {
                    mWaitingSubject.onNext(false);
                    LogHelper.log(e);
                });
    }

    @OnClick(R2.id.iv_left_hot_end_tune)
    void onClickLeftExtruderTune() {
        DecisionDialog.create(requireContext())
                .setDialogStatus(2, false, false, false, true)
                .setContent(getString(R.string.j1_dialog_settings_hot_end_pid_auto_tune_confirm))
                .setFirstTv(R.string.all_cancel, R.color.select_dialog_white_txt, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setSecondTv(R.string.all_start, R.color.select_dialog_orange_txt, (dialog, which) -> {
                    dialog.dismiss();
                    startCheck(0);

                })
                .show();
    }

    @OnClick(R2.id.iv_right_hot_end_tune)
    void onClickRightExtruderTune() {
        DecisionDialog.create(requireContext())
                .setDialogStatus(2, false, false, false, true)
                .setContent(getString(R.string.j1_dialog_settings_hot_end_pid_auto_tune_confirm))
                .setFirstTv(R.string.all_cancel, R.color.select_dialog_white_txt, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setSecondTv(R.string.all_start, R.color.select_dialog_orange_txt, (dialog, which) -> {
                    dialog.dismiss();
                    startCheck(1);
                })
                .show();
    }
}
