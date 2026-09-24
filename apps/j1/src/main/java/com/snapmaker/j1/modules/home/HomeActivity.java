package com.snapmaker.j1.modules.home;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.orhanobut.logger.Logger;
import com.snapmaker.j1.R;
import com.snapmaker.j1.R2;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fabscreen.features.settings.j1.J1SettingsContainerFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.network.AccessPoint;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.INetwork;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.controller.FDMController;
import fabscreen.platform.base.service.machine.controller.NewPrintController;
import fabscreen.platform.base.service.machine.entity.parts.Extruder;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseActivity;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.core.ui.view.UpdateDialog;
import fabscreen.platform.core.ui.view.VideoPlayerIJK;
import fabscreen.platform.core.ui.view.VideoPlayerListener;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static fabscreen.platform.base.service.machine.entity.parts.Extruder.EXTRUDER_LEFT;

@Route(path = RoutePath.J1_INDEX)
public class HomeActivity extends BaseActivity {
    @BindView(R2.id.tv_j1_home_left_extruder_diameter)
    TextView mTvLeftExtruderDiameter;
    @BindView(R2.id.tv_j1_home_right_extruder_diameter)
    TextView mTvRightExtruderDiameter;
    @BindView(R2.id.tv_j1_home_left_extruder_temp)
    TextView mTvLeftExtruderTemp;
    @BindView(R2.id.tv_j1_home_right_extruder_temp)
    TextView mTvRightExtruderTemp;
    @BindView(R2.id.tv_j1_home_heated_bed_temp)
    TextView mTvHeatedBedTemp;
    @BindView(R2.id.tv_j1_home_jade_one)
    TextView mTvJadeOne;
    @BindView(R2.id.iv_j1_home_wifi)
    ImageView mIvHomeWifi;
    @BindView(R2.id.iv_home_back)
    VideoPlayerIJK mVpHomeBack;
    @BindView(R2.id.rl_j1_home_desc)
    RelativeLayout mRlHomeDesc;
    private HomeViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        mViewModel = getViewModel(HomeViewModel.class);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        initView();
        initVideo();
        mViewModel.copyInternalFiles();
        Logger.d("Requesting Print Power Outage...");
        IMachine machine = ServiceContainer.getInstance().getService(IMachine.class);
        if (!machine.getMachineStatusSubjectHolder().getValue().connected) return;
        NewPrintController printController = machine.getNewPrintController();
        printController.requestPowerOutageStatus()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        Logger.d("Power loss outage detected.");
//                        printController.handlePowerLossFile(responseStructure);
                        handlePrintPowerLoss(responseStructure);
                    } else {
                        Logger.d("Power loss issues return " + responseStructure.resultProp.getValue());
                    }
                }, LogHelper::log);

        if (ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getNeedQueryMachineError()) {
            ServiceContainer.getInstance().getService(IMachine.class).getErrorController().queryException().as(bindToLifecycle()).subscribe();
            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setNeedQueryMachineError(false);
        }
    }

    private void initVideo() {
        // Load so file
        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        } catch (Exception e) {
            LogHelper.log(e);
        }

        mVpHomeBack.setListener(new VideoPlayerListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer mp, int percent) {

            }

            @Override
            public void onCompletion(IMediaPlayer mp) {
            }

            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                Logger.e("IMediaPlayer error %d\t%d", what, extra);
                return false;
            }

            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                return false;
            }

            @Override
            public void onPrepared(IMediaPlayer mp) {
            }

            @Override
            public void onSeekComplete(IMediaPlayer mp) {
//                mRlHomeDesc.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {

            }
        });
        mVpHomeBack.setVideoPath(ServiceContainer.getInstance().getService(IAppService.class).getVideDir() + "/home.webm");
        mVpHomeBack.setLooping(true);
    }

    // TODO: Should J1 home page presented by Activity? Consider refactor into fragment.
    //  Needs to show extruder and heated module info here.

    private void initView() {
        observeUpdate();

        // Set gradient color for home text.
        // TODO: We can implement GradientTextView extends TextView instead of setting shader temporary.
        Shader textShader = new LinearGradient(0, 0, mTvJadeOne.getPaint().measureText(mTvJadeOne.getText().toString()), mTvJadeOne.getTextSize(),
                new int[]{Color.parseColor("#F56A00"), Color.parseColor("#FFAB00")},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        mTvJadeOne.getPaint().setShader(textShader);

        // Network
        ServiceContainer.getInstance().getService(INetwork.class).getActiveNetworkObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(accessPoint -> {
                    if (accessPoint != AccessPoint.NULL_ACCESS_POINT) {
                        Logger.d("Wi-Fi connected.");
                        mIvHomeWifi.setBackgroundResource(R.drawable.icon_wifi_normal_64x64);

                    } else {
                        mIvHomeWifi.setBackgroundResource(R.drawable.icon_wifi_disconnect);
                    }
                });

    }

    private void observeUpdate() {
        mViewModel.getUpdateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(hasUpdate -> {
                    if (hasUpdate) {
                        Logger.d("J1 Update available.");
                        // FIXME: Temporary comment this for beta user not popping up update notification.
//                        showUpdateDialog();
                    }
                }, LogHelper::log);
    }

    private void showUpdateDialog() {
        HomeViewModel.VersionInfo versionInfo = mViewModel.getNewVersionInfo();
        UpdateDialog.create(this)
                .setTitle(versionInfo.name + " " + getString(R.string.settings_firmware_update))
                .setContent(versionInfo.changelogs, this)
                .setOnClickUpdate(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mRouter.routeToSettingsPage(J1SettingsContainerFragment.SETTINGS_FIRMWARE_AND_DOWN)
                                .start(HomeActivity.this);
                    }
                }).show(560);
    }

    private void bindMachineTemperature() {
        FDMController fdmController = mMachine.getFDMController();
        try {
            fdmController
                    .getToolheadStatusSubjectHolder(0)
                    .getObservable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(fdmToolHeadInfo -> {
                                float leftTemperature = 0;
                                float leftTargetTemperature = 0;
                                float leftDiameter = 0;
                                for (Extruder e : fdmToolHeadInfo.getExtruderList()) {
                                    switch (e.getId()) {
                                        case EXTRUDER_LEFT:
                                            leftDiameter = e.getDiameter();
                                            leftTemperature = e.getTemperature();
                                            leftTargetTemperature = e.getTargetTemperature();
                                            break;
                                        default:
                                            break;
                                    }
                                }
                                mTvLeftExtruderDiameter.setText(String.format(Locale.ENGLISH, "%.1f", leftDiameter));
                                mTvLeftExtruderTemp.setText(String.format(Locale.ENGLISH, "%3d", (int) leftTemperature));
                            }, LogHelper::log
                    );

            fdmController
                    .getToolheadStatusSubjectHolder(1)
                    .getObservable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(fdmToolHeadInfo -> {
                        double rightTemperature = 0;
                        double rightTargetTemperature = 0;
                        float rightDiameter = 0;
                        for (Extruder e : fdmToolHeadInfo.getExtruderList()) {
                            switch (e.getId()) {
                                case EXTRUDER_LEFT:
                                    rightTemperature = e.getTemperature();
                                    rightTargetTemperature = e.getTargetTemperature();
                                    rightDiameter = e.getDiameter();
                                    break;
                                default:
                                    break;
                            }
                        }
                        mTvRightExtruderDiameter.setText(String.format(Locale.ENGLISH, "%.1f", rightDiameter));
                        mTvRightExtruderTemp.setText(String.format(Locale.ENGLISH, "%3d", (int) rightTemperature));
                    }, LogHelper::log);

            mMachine.getMachineController()
                    .getHeatedBed()
                    .getHeatedBedStatusSubjectHolder()
                    .getObservable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(heatedBedStatus -> {
                        mTvHeatedBedTemp.setText(String.format(Locale.ENGLISH, "%.0f", heatedBedStatus.getZoneList().get(0).getCurrentTemperature()));
                    });
        } catch (Exception e) {
            LogHelper.log(e);
            Toast.makeText(this, String.format("获取信息的时候出现了问题：%s", e), Toast.LENGTH_SHORT).show();
        }

    }


    @OnClick(R2.id.view_j1_home_quick_start)
    void onClickStartView() {
        IMachine.WorkType workType = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType;
        int fileType = 0;
        switch (workType) {
            case CNC:
                fileType = 3;
                break;
            case FDM:
                fileType = 1;
                break;
            case LASER:
                fileType = 2;
                break;
            default:
                fileType = 0;
        }
        mRouter.routeToFilesPage(fileType).start(this);
    }

    @OnClick(R2.id.ll_j1_home_bottom_navigation_control)
    void onClickControl() {
        mRouter.routeToControlPage().start(this);
    }

    @OnClick(R2.id.ll_j1_home_bottom_navigation_calibration)
    void onClickCalibration() {
        mRouter.routeToCalibrationPage().start(this);
    }

    @OnClick(R2.id.ll_j1_home_bottom_navigation_settings)
    void onClickSettings() {
        mRouter.routeToSettingsPage().start(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unSubscribeMachine();
//        mRlHomeDesc.setVisibility(View.VISIBLE);
        mVpHomeBack.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVpHomeBack.start();
        subscribeMachine();
        bindMachineTemperature();
        mMachine.getErrorController().queryException()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                }, LogHelper::log);

    }

    @Override
    protected void onStop() {
        super.onStop();
        IjkMediaPlayer.native_profileEnd();
    }

    void subscribeMachine() {
        if (mMachine.getMachineController().getHeatedBed() == null) return;
        mMachine.getMachineController().getHeatedBed().subscribeTemperatureChange();
        mMachine.getFDMController().subscribeExtruderChange();
    }

    void unSubscribeMachine() {
        IMachine.WorkType workType = mMachine.getMachineInfoSubjectHolder().getValue().workType;
        if (workType == IMachine.WorkType.FDM) {
            mMachine.getMachineController().getHeatedBed().unsubscribeTemperatureChange();
            mMachine.getFDMController().unSubscribeExtruderChange();
        }

    }

    void handlePrintPowerLoss(ResponseStructure response) {
        BaseStructure gcodeFileInfo = (BaseStructure) response.dataProp;
        String md5 = (String) gcodeFileInfo.getProp("md5").getValue();
        String filename = (String) gcodeFileInfo.getProp("filename").getValue();

        IPrintWorkspace workspace = ServiceContainer.getInstance().getService(IPrintWorkspace.class);
        workspace.initLastPrintFile();
        if (workspace.getPrintFile() == null) {
            Logger.w("Could not find file in workspace!");
            return;
        }
//        if (filename.equals(workspace.getFileName())) {
        DecisionDialog.create(this)
                .setDialogStatus(DecisionDialog.BTN_TWO, false, false, true, true)
                .setType(DecisionDialog.WARMING_TYPE)
                .setTitle(getString(R.string.print_warning_power_outage_title))
                .setContent(getString(R.string.j1_warning_power_outage_content))
                .setFirstTv(getString(R.string.all_cancel), R.color.select_dialog_left_text_color, (dialog, which) -> {
                    ServiceContainer.getInstance().getService(IMachine.class).getNewPrintController().requestPrintPowerLossClearMarker()
                            .observeOn(AndroidSchedulers.mainThread())
                            .as(bindToLifecycle())
                            .subscribe(responseStructure -> dialog.dismiss(), LogHelper::log);
                })
                .setSecondTv(getString(R.string.all_resume), R.color.select_dialog_yellow_txt, (dialog, which) -> {
                    ServiceContainer.getInstance().getService(IMachine.class).getNewPrintController().setPowerOutageFlag(true);
                    workspace.setFileMD5Value(md5);
                    mRouter.routeToPrintPage().start(this);
                    dialog.dismiss();
                }).show();
//        } else {

//        }
    }
}
