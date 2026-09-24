package fabscreen.features.remote.a350;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.orhanobut.logger.Logger;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.remote.R;
import fabscreen.features.remote.R2;
import fabscreen.platform.base.Constants;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.print.DeprecatedPrintController;
import fabscreen.platform.base.legacy.remote.SessionManager;
import fabscreen.platform.base.lib.network.AccessPoint;
import fabscreen.platform.base.model.system.DeprecatedMachineInfo;
import fabscreen.platform.base.model.system.MachineStatusManager;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.INetwork;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.view.FabAlert;
import fabscreen.platform.base.view.FabConfirm;
import fabscreen.platform.core.ui.view.FabFullScreenDialog;
import fabscreen.platform.core.ui.view.FabLoading;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class RemoteHomeFragment extends BaseFragment {
    @BindView(R2.id.view_home_wifi)
    View mViewWifi;
    @BindView(R2.id.tv_home_wifi_ssid)
    TextView mTvWifiSSID;
    @BindView(R2.id.view_home_3dp_status)
    View mView3DPStatus;
    @BindView(R2.id.view_home_laser_status)
    View mViewLaserStatus;
    @BindView(R2.id.view_home_cnc_status)
    View mViewCNCStatus;
    @BindView(R2.id.tv_laser_type)
    TextView mTvLaserType;
    // Addon
    @BindView(R2.id.view_home_add_on_enclosure)
    View mViewEnclosureStatus;
    @BindView(R2.id.view_home_add_on_rotary)
    View mViewRotaryStatus;
    @BindView(R2.id.view_home_add_on_emergency_stop)
    View mViewEmergencyStopStatus;
    @BindView(R2.id.view_home_add_on_air_purifier)
    View mViewAirPurifierStatus;
    @BindView(R2.id.view_home_3dp_add_on)
    View mViewAddOn3DP;
    @BindView(R2.id.view_home_laser_add_on)
    View mViewAddOnLaser;
    @BindView(R2.id.view_home_cnc_add_on)
    View mViewAddOnCNC;
    @BindView(R2.id.tv_home_status_machine_name)
    TextView mTvMachineName;
    @BindView(R2.id.iv_home_3dp_status_heated_bed)
    ImageView mIvStatusHeatedBed;
    @BindView(R2.id.tv_widget_nozzle_temp_value)
    TextView mTvNozzleTemp;
    @BindView(R2.id.tv_widget_heated_bed_temp_value)
    TextView mTvHeatedBedTemp;
    @BindView(R2.id.iv_home_laser_status_camera)
    ImageView mIvStatusCamera;
    @BindView(R2.id.tv_widget_laser_focus_value)
    TextView mTvLaserFocus;
    @BindView(R2.id.iv_home_enclosure_status)
    ImageView mIvEnclosureStatus;
    @BindView(R2.id.iv_home_rotary_status)
    ImageView mIvRotaryStatus;
    @BindView(R2.id.iv_home_emergency_stop_status)
    ImageView mIvStatusEmergencyStop;
    @BindView(R2.id.iv_home_air_purifier_status)
    ImageView mIvStatusAirPurifier;
    @BindView(R2.id.btn_remote_home_disconnect)
    Button mBtnDisconnect;
    private boolean mIsRemotePrintNotFinish = false;
    private FabConfirm mPrintNotFinishDialog = null;
    private FabConfirm mFileTransferCompletedDialog = null;
    private FabLoading mFabLoading;

    public static RemoteHomeFragment getInstance() {
        return new RemoteHomeFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
        initialize();
        ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().setRemotePageFlag(true);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_remote_home;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @Override
    public void onDestroy() {
        ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().setRemotePageFlag(false);
        super.onDestroy();
    }

    private void initView() {
        // Network
        ServiceContainer.getInstance().getService(INetwork.class).getActiveNetworkObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(accessPoint -> {
                    if (accessPoint != AccessPoint.NULL_ACCESS_POINT) {
                        mViewWifi.setVisibility(View.VISIBLE);
                        mTvWifiSSID.setText(accessPoint.getSSID());
                    } else {
                        mViewWifi.setVisibility(View.INVISIBLE);
                    }
                });

        // Machine Name
        String machineName = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineName();
        mTvMachineName.setText(machineName);

        // Head type and status
        int headType = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId();

        mView3DPStatus.setVisibility(headType == Module.ModuleType.HEAD_3DP ? View.VISIBLE : View.GONE);

        int laserVisibility = headType == Module.ModuleType.HEAD_LASER || headType == Module.ModuleType.HEAD_LASER_10W ? View.VISIBLE : View.GONE;

        mViewLaserStatus.setVisibility(laserVisibility);
        mViewCNCStatus.setVisibility(headType == Module.ModuleType.HEAD_CNC ? View.VISIBLE : View.GONE);
        mTvLaserType.setText(headType == Module.ModuleType.HEAD_LASER_10W ? R.string.all_10w_laser : R.string.all_laser);

        mViewAddOn3DP.setVisibility(headType == Module.ModuleType.HEAD_3DP ? View.VISIBLE : View.GONE);
        mViewAddOnLaser.setVisibility(laserVisibility);
        mViewAddOnCNC.setVisibility(headType == Module.ModuleType.HEAD_CNC ? View.VISIBLE : View.GONE);

        switch (headType) {
            case Module.ModuleType.HEAD_3DP: {
                mViewEnclosureStatus = mViewAddOn3DP.findViewById(R.id.view_home_add_on_enclosure);
                mViewRotaryStatus = mViewAddOn3DP.findViewById(R.id.view_home_add_on_rotary);
                mViewEmergencyStopStatus = mViewAddOn3DP.findViewById(R.id.view_home_add_on_emergency_stop);
                mViewAirPurifierStatus = mViewAddOn3DP.findViewById(R.id.view_home_add_on_air_purifier);
                init3DPStatus();
                break;
            }
            case Module.ModuleType.HEAD_LASER:
            case Module.ModuleType.HEAD_LASER_10W: {
                mViewEnclosureStatus = mViewAddOnLaser.findViewById(R.id.view_home_add_on_enclosure);
                mViewRotaryStatus = mViewAddOnLaser.findViewById(R.id.view_home_add_on_rotary);
                mViewEmergencyStopStatus = mViewAddOnLaser.findViewById(R.id.view_home_add_on_emergency_stop);
                mViewAirPurifierStatus = mViewAddOnLaser.findViewById(R.id.view_home_add_on_air_purifier);
                initLaserStatus();
                updateLaserStatus();
                break;
            }
            case Module.ModuleType.HEAD_CNC: {
                mViewEnclosureStatus = mViewAddOnCNC.findViewById(R.id.view_home_add_on_enclosure);
                mViewRotaryStatus = mViewAddOnCNC.findViewById(R.id.view_home_add_on_rotary);
                mViewEmergencyStopStatus = mViewAddOnCNC.findViewById(R.id.view_home_add_on_emergency_stop);
                mViewAirPurifierStatus = mViewAddOnCNC.findViewById(R.id.view_home_add_on_air_purifier);
                break;
            }
        }

        initAddOnStatus();

        // Confirmation
        ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().getCurrentSessionObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(auth -> checkCurrentSession());

        // Workaround here.
        // Cause we don't have pages to manage remote printing that remote were already disconnected,
        // remote home page will pop up confirm dialog when disconnecting from remote printing(either active disconnect or timed out from interval heartbeat).
        Observable.combineLatest(ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().getCurrentSessionObservable(),
                ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().getRemotePrintStateObservable(), (auth, state) -> {
                    boolean isShowDialog = false;
                    if (auth == SessionManager.NULL_SESSION) {
                        switch (state) {
                            case DeprecatedPrintController.STATE_IDLE:
                            case DeprecatedPrintController.STATE_COMPLETED: {
                                isShowDialog = false;
                                break;
                            }
                            case DeprecatedPrintController.STATE_PRINTING:
                            case DeprecatedPrintController.STATE_PAUSED: {
                                isShowDialog = true;
                                break;
                            }
                            default:
                                isShowDialog = false;
                        }
                    }
                    return isShowDialog;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(showDialog -> {
                    if (showDialog) {
                        showUpRemotePrintNotFinishDialog();
                    } else {
                        dismissRemotePrintNotFinishDialog();
                        // If no connection is ever maintain, then exit page.
                        if (ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().getCurrentSession() == SessionManager.NULL_SESSION) {
                            back();
                        }
                    }
                });

        // Watch file transit events
        ServiceContainer.getInstance().getService(IAppService.class).getHTTPEventBus().watchReceiveFileEvent()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(file -> {
                    Logger.d("File transfer via Wi-Fi is done.");
                    // If last confirm dialog exists, then dismiss before new dialog created.
                    if (mFileTransferCompletedDialog != null) {
                        dismissFileTransferCompletedDialog();
                    }
                    // Show up file transfer complete dialog.
                    mFileTransferCompletedDialog = FabConfirm.create(getContext())
                            .setIcon(R.drawable.pic_dialog_success_72x72)
                            .setDescription(R.string.dialog_file_transfer_successful)
                            .setCanceledOnTouchOutSide(false)
                            .setConfirm(R.string.guide_got_it, (dialog, which) -> {
                                dismissFileTransferCompletedDialog();
                            });
                    mFileTransferCompletedDialog.show();
                });

        ServiceContainer.getInstance().getService(IAppService.class).getHTTPEventBus().watchReceiveProgressEvent()
                .filter(progress -> progress == 0)
                .flatMap(result -> ServiceContainer.getInstance().getService(IAppService.class).getHTTPEventBus().watchReceiveProgressEvent()
                        .timeout(10, TimeUnit.SECONDS)
                        .onExceptionResumeNext(Observable.just(-1))
                        .takeUntil(progress -> progress == 100)
                )
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(progress -> {
                    if (progress == -1) {
                        Logger.d("Failed to receive the file.");
                        FabAlert.alert(getContext(), getResources().getText(R.string.exception_receiving_file));
                        if (mFabLoading != null) {
                            mFabLoading.dismiss();
                            mFabLoading = null;
                        }
                        return;
                    }
                    if (progress == 100) {
                        if (mFabLoading != null) {
                            mFabLoading.dismiss();
                            mFabLoading = null;
                        }
                        return;
                    }
                    if (mFabLoading != null) {
                        mFabLoading.setProgress(String.valueOf(progress));
                    } else {
                        Logger.d("Start receiving file...");
                        mFabLoading = FabLoading.create(getContext())
                                .setTitle(R.string.all_receiving_file_title)
                                .setDescription(R.string.desc_receiving_file)
                                .setProgress(String.valueOf(progress));
                        mFabLoading.show();
                    }
                }, LogHelper::log);

        checkCurrentSession();
    }

    private void initialize() {
        // reset door detection flag if connected
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure().clearEnclosureDoorFlag();

        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure().getEnclosureStatusObservable()
                .skip(1)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(enclosureStatus -> {
                    if (enclosureStatus.isDoorDetectionEnabled()) {
                        ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().setEnclosureDoorPause();

                        // Calculate door open count, used for notify remote client enclosure door status has changed.
                        int doorCount = ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().getEnclosureDoorCount();
                        doorCount = (doorCount >= 32) ? 0 : (doorCount + 1);
                        ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().setEnclosureDoorCount(doorCount);
                    }
                });

        ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().connectPrintController();
    }

    private void checkCurrentSession() {
        SessionManager.Session session = ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().getCurrentSession();

        if (session != SessionManager.NULL_SESSION) {
            // If not granted
            if (!session.isGranted()) {
                FabFullScreenDialog dialog = FabFullScreenDialog.create(getContext());
                dialog.setIcon(R.drawable.pic_warning_remote_connect_120x120)
                        .setTitle(R.string.remote_connect_request)
                        .setMessage(R.string.remote_connect_request_desc);
                dialog.setPositive(R.string.all_yes, (v, which) -> {
                    v.dismiss();
                    ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().grantCurrentSession();
                    Logger.i("Remote access request approved, session #%s", session.getToken());
                });
                dialog.setNegative(R.string.all_no, (v, which) -> {
                    v.dismiss();
                    Logger.i("Remote access request denied. %s", session.getToken());
                    ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().denyCurrentSession();
                });
                dialog.show();
            }
        }
    }

    private void dismissFileTransferCompletedDialog() {
        if (mFileTransferCompletedDialog != null && mFileTransferCompletedDialog.isShowing()) {
            mFileTransferCompletedDialog.dismiss();
            mFileTransferCompletedDialog = null;
        }
    }

    private void showUpRemotePrintNotFinishDialog() {
        if (mPrintNotFinishDialog != null) return;

        // TODO: Description needs to be check and update.
        mPrintNotFinishDialog = FabConfirm.create(getContext())
                .setDescription(R.string.remote_print_job_not_finish_confirm)
                .setCanceledOnTouchOutSide(false)
                .setConfirm(R.string.all_confirm, (dialog, which) -> {
                    dialog.dismiss();
                    stopRemotePrint(false);
                });
        mPrintNotFinishDialog.show();
    }

    private void dismissRemotePrintNotFinishDialog() {
        if (mPrintNotFinishDialog != null && mPrintNotFinishDialog.isShowing()) {
            mPrintNotFinishDialog.dismiss();
            mPrintNotFinishDialog = null;
        }
    }

    private void initAddOnStatus() {

        final boolean isEmergencyAvailable = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isEnclosureAvailable;

        mIvEnclosureStatus = mViewEnclosureStatus.findViewById(R.id.iv_home_enclosure_status);
        mIvRotaryStatus = mViewRotaryStatus.findViewById(R.id.iv_home_rotary_status);

        // Enclosure status
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure().getEnclosureStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(enclosureStatus -> {
                    mViewEnclosureStatus.setVisibility(enclosureStatus.getStatus() == 0 ? View.VISIBLE : View.GONE);
                    mIvEnclosureStatus.setBackgroundResource(enclosureStatus.getStatus() == 0 ? R.drawable.ic_home_status_normal_10x10 : R.drawable.ic_home_status_abnormal_10x10);
                });


//        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getRotaryModuleSubject(1).getValue().getRotaryModuleStatusSubjectHolder().getObservable()
//                .observeOn(AndroidSchedulers.mainThread())
//                .as(bindToLifecycle())
//                .subscribe(rotaryModule -> {
//                    mViewRotaryStatus.setVisibility((rotaryModule.status == (byte) 0) ? View.VISIBLE : View.GONE);
//                    mIvRotaryStatus.setBackgroundResource((rotaryModule.status == (byte) 0) ? R.drawable.ic_home_status_normal_10x10 : R.drawable.ic_home_status_abnormal_10x10);
//                }, LogHelper::log);
        int moduleState = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getRotaryModule().getModuleInfo().getModuleState();
        mViewRotaryStatus.setVisibility((moduleState == 0) ? View.VISIBLE : View.GONE);
        mIvRotaryStatus.setBackgroundResource(moduleState == 0 ? R.drawable.ic_home_status_normal_10x10 : R.drawable.ic_home_status_abnormal_10x10);


        mViewEmergencyStopStatus.setVisibility(isEmergencyAvailable ? View.VISIBLE : View.GONE);
        mIvStatusEmergencyStop = mViewEmergencyStopStatus.findViewById(R.id.iv_home_emergency_stop_status);
        mIvStatusEmergencyStop.setBackgroundResource(isEmergencyAvailable ? R.drawable.ic_home_status_normal_10x10 : R.drawable.ic_home_status_abnormal_10x10);

        // Air Purifier Status
        mIvStatusAirPurifier = mViewAirPurifierStatus.findViewById(R.id.iv_home_air_purifier_status);
        final boolean isAirPurifierPlugged = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isAirPurifierAvailable;
        mViewAirPurifierStatus.setVisibility(isAirPurifierPlugged ? View.VISIBLE : View.GONE);
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure().getEnclosureStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(airPurifierStatus -> {
                    switch (airPurifierStatus.getStatus()) {
                        case (byte) 0:
                            mIvStatusAirPurifier.setBackgroundResource(R.drawable.ic_home_status_normal_10x10);
                            break;
                        case (byte) 1:
                        case (byte) 2:
                        case (byte) 3:
                        default: {
                            mIvStatusAirPurifier.setBackgroundResource(R.drawable.ic_home_status_abnormal_10x10);
                            break;
                        }
                    }
                });
    }

    private void init3DPStatus() {
        MachineStatusManager.getMachineInfoHolder().getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(machineStatus -> {
                    double nozzleTemp = machineStatus.leftNozzleTemperature;
                    mTvNozzleTemp.setText(String.format(Locale.getDefault(), "%.0f °C", nozzleTemp));

                    double heatedBedTemp = machineStatus.bedTemperature;
                    mTvHeatedBedTemp.setText(String.format(Locale.getDefault(), "%.0f °C", heatedBedTemp));

                    mIvStatusHeatedBed.setBackgroundResource(heatedBedTemp > 0 ? R.drawable.ic_home_status_normal_10x10 : R.drawable.ic_home_status_abnormal_10x10);
                });

        // watch filament status, auto pause if filament is out while printing

        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getFilamentObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isFilamentOut -> {
                    final DeprecatedMachineInfo status = MachineStatusManager.getMachineInfoHolder().getValue();
                    if (isFilamentOut && status.printerStatus != 0) {
                        ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().setFilamentOutPause();
                    }
                });
    }

    private void initLaserStatus() {
//        ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().getBluetoothConnectedObservable()
//                .observeOn(AndroidSchedulers.mainThread())
//                .as(bindToLifecycle())
//                .subscribe(connected -> {
//                    mIvStatusCamera.setBackgroundResource(connected ? R.drawable.ic_home_status_normal_10x10 : R.drawable.ic_home_status_abnormal_10x10);
//                });
    }

    private void updateLaserStatus() {
        ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolHeadInfoObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(laserToolheadInfo -> {
                    mTvLaserFocus.setText(String.format(Locale.getDefault(), "%.1f mm", laserToolheadInfo.getLaserFocalLength()));
                });
    }

    @OnClick(R2.id.btn_remote_home_disconnect)
    void onClickDisconnect() {
        playNormalClickSound();
        final int remotePrintState = ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().getRemotePrintState();

        if (remotePrintState == DeprecatedPrintController.STATE_PRINTING || remotePrintState == DeprecatedPrintController.STATE_PAUSED) {
            showDisconnectWarningDialog();
        } else {
            ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().setCurrentSession(SessionManager.NULL_SESSION);
        }
    }

    private void showDisconnectWarningDialog() {
        FabConfirm.create(getContext())
                .setDescription(R.string.remote_disconnect_confirm)
                .setConfirm(R.string.all_confirm, (dialog, which) -> {
                    // Fixme: Workaround here, there's no page to manage remote printing,
                    //  so we need to stop the print to avoid machine working out of control.
                    dialog.dismiss();
                    stopRemotePrint(true);
                })
                .setCancel(R.string.all_cancel, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void stopRemotePrint(boolean selfDisconnect) {
        // Show loading dialog while requesting stop print.
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.color.default_theme_dark_mask);
            dialog.getWindow().setLayout(280 * 2, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_remote_stop_print_loading, null);
        dialog.setView(view);
        dialog.show();

        ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().stop()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    if (selfDisconnect) {
                        ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().setCurrentSession(SessionManager.NULL_SESSION);
                    }
                    dialog.dismiss();
                }, e -> {
                    LogHelper.log(e);
                    dialog.dismiss();
                    showStopPrintErrorDialog();
                });
    }

    private void showStopPrintErrorDialog() {
        // Show loading dialog while requesting stop print.
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.color.default_theme_dark_mask);
            dialog.getWindow().setLayout(280 * 2, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_remote_stop_print_failed, null);
        dialog.setView(view);
        dialog.show();

        AndroidSchedulers.mainThread().scheduleDirect(dialog::dismiss, 3000, Constants.TIME_UNIT);
    }
}
