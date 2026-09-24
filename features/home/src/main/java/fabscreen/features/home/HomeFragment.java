package fabscreen.features.home;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.platform.base.Constants;
import fabscreen.platform.base.helper.FirebaseHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.api.ApiClient;
import fabscreen.platform.base.lib.VersionResponse;
import fabscreen.platform.base.lib.network.AccessPoint;
import fabscreen.platform.base.model.system.MachineStatusManager;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.INetwork;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.entity.Toolhead;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.base.view.FabConfirm;
import fabscreen.platform.core.ui.view.FabFullScreenDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class HomeFragment extends BaseFragment {
//    @BindView(R2.id.view_home_wifi)
//    View mViewWifi;
//    @BindView(R2.id.tv_home_wifi_ssid)
//    TextView mTvWifiSSID;
    @BindView(R2.id.view_home_3dp_status)
    View mView3DPStatus;
    @BindView(R2.id.view_home_laser_status)
    View mViewLaserStatus;
    @BindView(R2.id.view_home_cnc_status)
    View mViewCNCStatus;
    @BindView(R2.id.tv_laser_type)
    TextView mTvLaserType;
    // Addon
    @BindView(R2.id.view_home_3dp_add_on)
    View mViewAddOn3DP;
    @BindView(R2.id.view_home_laser_add_on)
    View mViewAddOnLaser;
    @BindView(R2.id.view_home_cnc_add_on)
    View mViewAddOnCNC;
    @BindView(R2.id.view_home_add_on_enclosure)
    View mViewEnclosureStatus;
    @BindView(R2.id.view_home_add_on_rotary)
    View mViewRotaryStatus;
    @BindView(R2.id.view_home_add_on_emergency_stop)
    View mViewEmergencyStopStatus;
    @BindView(R2.id.view_home_add_on_air_purifier)
    View mViewAirPurifierStatus;
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
    private FabConfirm mModuleConfirmDialog;
    private boolean mVersionUpdateNotificationFlag = true;
    private boolean mIsLaserModuleGuideCompleted = true;
    private FabConfirm mFabConfirm;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    public void onResume() {
        super.onResume();

        int headType = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId();

        byte rotaryStatus = (byte) ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getRotaryModule().getModuleInfo().getModuleState();

        // Factory mode
        if (headType >= Toolhead.HeadFactoryId.HEAD_FACTORY_3DP && headType <= Toolhead.HeadFactoryId.HEAD_FACTORY_LASER) {
            ServiceContainer.getInstance().getService(IRouter.class).routeToFactoryActivity().start(getContext());
            return;
        }

        // Emergency stop button triggered.
//        if (ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isEmergencyStopAvailable) {
////            ServiceContainer.getInstance().getService(IMachine.class).getMachineController().onEmergencyStop();
//            ServiceContainer.getInstance().getService(IRouter.class).routeToEmergencyStopPage(true).start(getContext());
//            return;
//        }

        // Setup Machine
        if (headType != Module.ModuleType.HEAD_UNPLUGGED) {
            if (!ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineSetupFlag()) {
                Logger.d("Setting up machine.");
                ServiceContainer.getInstance().getService(IRouter.class).routeToWelcome().start(getContext());
                return;
            }
        }

        // Guide to tool head
        switch (headType) {
            case Module.ModuleType.HEAD_3DP:
                if (!ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineSetup3DP()) {
                    Logger.d("Guide to 3DP.");
                    ServiceContainer.getInstance().getService(IRouter.class).routeToGuide3DP().start(getContext());
                }
                break;
            case Module.ModuleType.HEAD_LASER:
                if (!ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineSetupRotaryLaser() && (rotaryStatus == (byte) 0)) {
                    Logger.d("Guide to Rotary Laser.");
                    ServiceContainer.getInstance().getService(IRouter.class).routeToGuideRotaryLaser().start(getContext());
                } else if (!ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineSetupLaser() && (rotaryStatus == (byte) 1)) {
                    Logger.d("Guide to Laser.");
                    ServiceContainer.getInstance().getService(IRouter.class).routeToGuideLaser().start(getContext());
                }
                break;
            case Module.ModuleType.HEAD_LASER_10W:
                if (!ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineSetupRotaryLaser() && (rotaryStatus == (byte) 0)) {
                    mIsLaserModuleGuideCompleted = false;
                    Logger.d("Guide to Rotary Laser.");
                    ServiceContainer.getInstance().getService(IRouter.class).routeToGuideRotaryLaser().start(getContext());
                } else if (!ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineSetup10WLaser() && (rotaryStatus == (byte) 1)) {
                    mIsLaserModuleGuideCompleted = false;
                    Logger.d("Guide to 10W Laser.");
                    ServiceContainer.getInstance().getService(IRouter.class).routeToGuide10WLaser().start(getContext());
                }
                break;
            case Module.ModuleType.HEAD_CNC:
                if (!ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineSetupRotaryCNC() && (rotaryStatus == (byte) 0)) {
                    Logger.d("Guide to Rotary CNC.");
                    ServiceContainer.getInstance().getService(IRouter.class).routeToGuideRotaryCNC().start(getContext());
                } else if (!ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineSetupCNC() && (rotaryStatus == (byte) 1)) {
                    Logger.d("Guide to CNC.");
                    ServiceContainer.getInstance().getService(IRouter.class).routeToGuideCNC().start(getContext());
                }
                break;
        }

        String machineName = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineName();
        if (!machineName.contentEquals(mTvMachineName.getText())) {
            mTvMachineName.setText(machineName);
        }

        // Check update flag
        if (ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineUpdatedFlag()) {
            Logger.i("Upgrade successfully");
            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMachineUpdatedFlag(false);
//            ServiceContainer.getInstance().getService(IRouter.class).routeToAboutPage().start(getContext());
            String desc = getResources().getString(R.string.home_dialog_update_successful_desc);
            desc += " " + ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLastUpdatePackageVersion();
            FabFullScreenDialog.create(getContext())
                    .setTitle(R.string.home_dialog_update_successful)
                    .setIcon(R.drawable.pic_dialog_success_72x72)
                    .setMessage(desc)
                    .setPositive(R.string.all_complete, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        }
        if (!mIsLaserModuleGuideCompleted && mFabConfirm != null && mFabConfirm.isShowing()) {
            mFabConfirm.dismiss();
            mFabConfirm = null;
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_home_print_default;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        // Check Power outage

        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getPowerOutageObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isPowerOutage -> {
                    if (isPowerOutage) {
                        if (ServiceContainer.getInstance().getService(IAppService.class).getWorkspace().getPrintSource() == Constants.PRINT_SOURCE_SCREEN) {
                            handlePowerOutage();
                        } else {
                            ServiceContainer.getInstance().getService(IMachine.class).getPrintController().resetErrorFlag()
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .as(bindToLifecycle())
                                    .subscribe(retCode -> {
                                        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().clearPowerOutageFlag();
                                        Logger.d("Clear error flags.");
                                    }, LogHelper::log);
                        }
                    }
                }, LogHelper::log);

        // Network
        ServiceContainer.getInstance().getService(INetwork.class).getActiveNetworkObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(accessPoint -> {
                    if (accessPoint != AccessPoint.NULL_ACCESS_POINT) {
                        Logger.d("Wi-Fi connected.");
//                        mViewWifi.setVisibility(View.VISIBLE);
//                        mTvWifiSSID.setText(accessPoint.getSSID());

                        // check new update if machine is on
                        if (ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getCheckUpdateFlag()) {
                            checkNewUpdate();
                            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setCheckUpdateFlag(false);
                        }
                    } else {
//                        mViewWifi.setVisibility(View.INVISIBLE);
                    }
                });

        // Machine Name
        String machineName = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineName();
        mTvMachineName.setText(machineName);

        // Head type and status
        int headType = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId();

        mView3DPStatus.setVisibility(headType == Module.ModuleType.HEAD_3DP ? View.VISIBLE : View.GONE);
        int isLaserHead = headType == Module.ModuleType.HEAD_LASER || headType == Module.ModuleType.HEAD_LASER_10W ? View.VISIBLE : View.GONE;
        mViewLaserStatus.setVisibility(isLaserHead);
        mViewCNCStatus.setVisibility(headType == Module.ModuleType.HEAD_CNC ? View.VISIBLE : View.GONE);
        mTvLaserType.setText(headType == Module.ModuleType.HEAD_LASER_10W ? R.string.all_10w_laser : R.string.all_laser);

        mViewAddOn3DP.setVisibility(headType == Module.ModuleType.HEAD_3DP ? View.VISIBLE : View.GONE);
        mViewAddOnLaser.setVisibility(isLaserHead);
        mViewAddOnCNC.setVisibility(headType == Module.ModuleType.HEAD_CNC ? View.VISIBLE : View.GONE);
        if (headType == Module.ModuleType.HEAD_LASER_10W) {
            ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getHeaderSecurityStatus().as(bindToLifecycle()).subscribe(success -> {
            }, LogHelper::log);
        }
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

        // Log start up event on Firebase
        int machineModel = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().modelId;
        FirebaseHelper.logStartUpEvent(getFirebaseAnalytics(), headType, machineModel);

        ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().getMachineController().getOutdatedVersionModuleListObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(outdatedModules -> {
                    if (outdatedModules.isEmpty()) return;

                    // build outdated module list string for notification.
                    StringBuilder moduleNames = new StringBuilder();
                    for (int i = 0; i < outdatedModules.size(); i++) {
                        moduleNames.append(outdatedModules.get(i));
                        if (i != outdatedModules.size() - 1) {
                            moduleNames.append(", ");
                        }
                    }
                    String updateFileFolder = ServiceContainer.getInstance().getService(IAppService.class).getDataDir().getAbsolutePath() + File.separatorChar + "update";
                    File file = new File(updateFileFolder, "update.bin");
                    boolean isFileExists = file.exists();

                    if (mVersionUpdateNotificationFlag) {
                        if (mModuleConfirmDialog != null && mModuleConfirmDialog.isShowing()) {
                            mModuleConfirmDialog.dismiss();
                            mModuleConfirmDialog = null;
                        }
                        final int dialogDescRes = isFileExists ? R.string.dialog_warning_outdated_module_version_detected_update
                                : R.string.dialog_warning_outdated_module_version_detected_notifiy;

                        mModuleConfirmDialog = FabConfirm.create(getContext())
                                .setCanceledOnTouchOutSide(false)
                                .setIcon(R.drawable.pic_dialog_warning_72x72)
                                .setDescription(getResources().getString(dialogDescRes, moduleNames.toString()))
                                .setConfirm(isFileExists ? R.string.all_update : R.string.guide_got_it, (dialog, position) -> {
                                    mVersionUpdateNotificationFlag = false;
                                    if (isFileExists) {
                                        ServiceContainer.getInstance().getService(IRouter.class).routeToUpdateActivity(file.getAbsolutePath(), true).start(getContext());
                                    }
                                    dialog.dismiss();
                                });
                        mModuleConfirmDialog.show();
                    }
                }, LogHelper::log);

        initAddOnStatus();

        if (headType != Module.ModuleType.HEAD_LASER_10W) {
            // -1 is the default value, indicating that no calibration is performed
            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setHeaderOnlineSyncID(-1);
        } else {
            // If the main control version does not support or has no reply, change the local value to the default value
            ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolHeadInfoObservable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(laserToolheadInfo -> {
                        int i = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getHeaderOnlineSyncID();
                        return Observable.just(laserToolheadInfo.getKey() != i || i == -1);
                    })
                    .as(bindToLifecycle())
                    .subscribe(success -> {
                        if (success) {
                            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setHeaderOnlineSyncID(-1);
                            if (mFabConfirm != null && mFabConfirm.isShowing()) {
                                mFabConfirm.dismiss();
                            }
                            mFabConfirm = FabConfirm.create(getContext())
                                    .setDescription(R.string.laser_10W_dialog_changer_header)
                                    .setCancel(R.string.all_cancel, (dialog, position) -> dialog.dismiss())
                                    .setConfirm(R.string.all_yes, (dialog, which) -> {
                                        ServiceContainer.getInstance().getService(IRouter.class).routeToCameraCalibration().start(getContext());
                                        dialog.dismiss();
                                    });
                            mFabConfirm.show();
                        }
                    }, e -> {
                        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setHeaderOnlineSyncID(-1);
                        LogHelper.log(e);
                    });
        }
    }

    private void initAddOnStatus() {
        final boolean isEnclosureReady = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isEnclosureAvailable;
        final boolean isEmergencyAvailable = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isEnclosureAvailable;

        mViewEnclosureStatus.setVisibility(isEnclosureReady ? View.VISIBLE : View.GONE);
        mIvEnclosureStatus = mViewEnclosureStatus.findViewById(R.id.iv_home_enclosure_status);
        mIvEnclosureStatus.setBackgroundResource(isEnclosureReady ? R.drawable.ic_home_status_normal_10x10 : R.drawable.ic_home_status_abnormal_10x10);

        mIvRotaryStatus = mViewRotaryStatus.findViewById(R.id.iv_home_rotary_status);

//        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getRotaryModuleSubject(1).getValue().getRotaryModuleStatusSubjectHolder().getObservable()
//                .observeOn(AndroidSchedulers.mainThread())
//                .as(bindToLifecycle())
//                .subscribe(rotaryModule -> {
//                    mViewRotaryStatus.setVisibility((rotaryModule.status != (byte) 1) ? View.VISIBLE : View.GONE);
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
    }

    private void initLaserStatus() {
//        ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().getBluetoothConnectedObservable()
//                .observeOn(AndroidSchedulers.mainThread())
//                .as(bindToLifecycle())
//                .subscribe(connected -> {
//                    mIvStatusCamera.setBackgroundResource(connected ? R.drawable.ic_home_status_normal_10x10 : R.drawable.ic_home_status_abnormal_10x10);
//                });

        ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolHeadInfoObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(laserToolheadInfo -> {
                    mTvLaserFocus.setText(String.format(Locale.getDefault(), "%.1f mm", laserToolheadInfo.getLaserFocalLength()));
                });
    }

    /**
     * Handle print power outage situation. This happens every time we connect/re-connect to
     * the machine.
     */
    private void handlePowerOutage() {
        FabFullScreenDialog.create(getContext())
                .setIcon(R.drawable.pic_warning_power_outage_120x120)
                .setTitle(R.string.print_warning_power_outage_title)
                .setMessage(getString(R.string.print_warning_power_outage_content))
                .setPositive(R.string.all_resume, (dialog, which) -> {
                    if (ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId() == Module.ModuleType.HEAD_LASER_10W) {
                        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getHeaderSecurityStatus()
                                .observeOn(AndroidSchedulers.mainThread())
                                .as(bindToLifecycle())
                                .subscribe(headerSecurity -> {
                                    if (headerSecurity.status == 0) {
                                        dialog.dismiss();
                                        // Init last print file.
                                        ServiceContainer.getInstance().getService(IAppService.class).getWorkspace().initLastPrintFile();

                                        // To print page
                                        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setPowerOutageFlag(true);
                                        ServiceContainer.getInstance().getService(IRouter.class)
                                                .routeToPrintPage()
                                                .start(getContext());
                                    }
                                }, LogHelper::log);
                    } else {
                        dialog.dismiss();
                        // Init last print file.
                        ServiceContainer.getInstance().getService(IAppService.class).getWorkspace().initLastPrintFile();

                        // To print page
                        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setPowerOutageFlag(true);
                        ServiceContainer.getInstance().getService(IRouter.class)
                                .routeToPrintPage()
                                .start(getContext());
                    }
                })
                .setNegative(R.string.all_cancel, (dialog, which) -> {
                    Logger.i("Power outage recover canceled.");
                    dialog.dismiss();
                    ServiceContainer.getInstance().getService(IMachine.class).getPrintController().resetErrorFlag()
                            .observeOn(AndroidSchedulers.mainThread())
                            .as(bindToLifecycle())
                            .subscribe(retCode -> {
                                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().clearPowerOutageFlag();
                                Logger.i("Clear error flags.");
                            }, LogHelper::log);
                })
                .show();
    }

    private void checkNewUpdate() {
        Logger.d("Start checking new version…");
        ApiClient apiClient = new ApiClient(ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getApiHost());
        apiClient.getLatestVersion()
                .as(bindToLifecycle())
                .subscribe(versionResponse -> {
                    String newVersion = versionResponse.data.new_version.version;
                    saveVersionResponse(versionResponse.data.new_version);
                    // Check version here. Notification only show once if new version is available.
                    if (!newVersion.equals(ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLastUpdatePackageVersion())
                            && !newVersion.equals(ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLastCheckVersion())) {
                        Logger.d("New firmware available " + versionResponse.data.new_version.version);
                        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setUpdateNotification(true);
                    }
                }, e -> {
                    if (e instanceof UnknownHostException) {
                        Logger.w("Unable to resolve API server, please check your network connectivity.");
                    } else if (e instanceof SocketTimeoutException) {
                        Logger.w("Socket timeout, please check your network is available.");
                        // AndroidSchedulers.mainThread().scheduleDirect(this::showCheckFailDialog, 1000, TimeUnit.MILLISECONDS);
                    } else {
                        LogHelper.log(e);
                    }
                });
    }

    private void saveVersionResponse(VersionResponse.NewVersionData response) {
        File file = new File(ServiceContainer.getInstance().getService(IAppService.class).getCacheDir(), "version_check.json");
        FileOutputStream fos;
        String content = new Gson().toJson(response);

        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    return;
                }
            }

            fos = new FileOutputStream(file);
            fos.write(content.getBytes(), 0, content.getBytes().length);

            fos.flush();
            fos.close();
        } catch (IOException e) {
            LogHelper.log(e);
        }
    }

    @OnClick(R2.id.btn_home_start)
    void onClickStart() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class).routeToFilesPage(1).start(getContext());
    }
}
