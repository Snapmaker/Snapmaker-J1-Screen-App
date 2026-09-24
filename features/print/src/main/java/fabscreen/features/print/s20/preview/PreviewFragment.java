package fabscreen.features.print.s20.preview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.platform.base.Constants;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.print.DeprecatedBatchPrintController;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.file.IPartition;
import fabscreen.platform.base.lib.parser.GcodeParser;
import fabscreen.platform.base.lib.parser.IGcodeParser;
import fabscreen.platform.base.lib.parser.SnapmakerParser;
import fabscreen.platform.base.model.ModelBoundary;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IFileManagerService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.presenter.PrintDetailPanelWidgetPresenter;
import fabscreen.platform.core.ui.view.FabAlert;
import fabscreen.platform.base.view.FabConfirm;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class PreviewFragment extends BaseFragment {
    public static final int REQUEST_CALIBRATION_RESULT = 1;

    // preview pic
    @BindView(R2.id.iv_preview_picture_icon)
    ImageView mIvPreviewPictureDefault;
    @BindView(R2.id.iv_preview_picture)
    ImageView mIvPreviewPicture;

    // basic info
    @BindView(R2.id.tv_preview_file_name)
    TextView mTvFilename;
    @BindView(R2.id.tv_preview_bounding_box)
    TextView mTvBoundingBox;

    // detail info
    @BindView(R2.id.tv_preview_detail_info)
    TextView mTvDetailInfo;
    @BindView(R2.id.widget_detail_panel_3dp)
    View mViewDetailPanel3DP;
    @BindView(R2.id.widget_detail_panel_laser)
    View mViewDetailPanelLaser;
    @BindView(R2.id.widget_detail_panel_cnc)
    View mViewDetailPanelCNC;
    // buttons
    @BindView(R2.id.btn_preview_start_print)
    Button mBtnStartPrint;
    @BindView(R2.id.btn_preview_change_settings)
    Button mBtnChangeSettings;
    private PrintDetailPanelWidgetPresenter mDetailPanelWidgetPresenter;
    private String mFilePath;
    private boolean mIsLocal;

    private int mFileType = Constants.FILE_TYPE_UNKNOWN;

    private IFileManagerService mFileManager;
    private IPartition mDevice;
    private IFile mParseFile;

    private int mHeadType = Module.ModuleType.HEAD_UNPLUGGED;

    private IGcodeParser mGcodeParser;
    private ModelBoundary mModelBoundary = new ModelBoundary();
    private boolean mIsRotaryAvailable = false;
    private Disposable sub;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.all_preview);

        // get head type
        mHeadType = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId();

        // deal with arguments
        if (getArguments() == null) {
            return;
        }

        Bundle arguments = getArguments();
        mFilePath = arguments.getString("file_path");
        mIsLocal = arguments.getBoolean("is_local");

        initView();

        mDetailPanelWidgetPresenter = new PrintDetailPanelWidgetPresenter();
        mDetailPanelWidgetPresenter.bind(view);

        Logger.d("");
        // if file is not match with toolHead
        if (!fileMatchesHead(mFileType, mHeadType) && mFileType != Constants.FILE_TYPE_UNKNOWN) {
            FabAlert.alert(getContext(), R.string.preview_alert_file_type_not_match);
            AndroidSchedulers.mainThread().scheduleDirect(this::back, 2000, Constants.TIME_UNIT);
        } else {
            initFile();
        }

        mIsRotaryAvailable = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isRotaryAvailable;
    }

    private boolean fileMatchesHead(int fileType, int headType) {
        if (headType == Module.ModuleType.HEAD_LASER_10W) {
            return fileType == Constants.FILE_TYPE_LASER;
        } else {
            return fileType == mHeadType;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGcodeParser.destroy();
        // TODO: TBD, device status should not affected by UI changing
//        if (mDevice != null) {
//            if (mDevice instanceof FabUsbFileDevice) {
//                mDevice = null;
//            } else {
//                mDevice.close();
//            }
//        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_preview;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        int index = mFilePath.lastIndexOf('/');
        String filename = mFilePath.substring(index + 1);

        index = filename.lastIndexOf('.');
        String extension = filename.substring(index + 1).toLowerCase();

        // basic
        mTvFilename.setSelected(true);
        mTvFilename.setText(filename);
        mTvBoundingBox.setVisibility(View.INVISIBLE);

        // detail
        switch (extension) {
            case "gcode":
                mFileType = Constants.FILE_TYPE_3DP;
                mIvPreviewPictureDefault.setImageResource(R.drawable.ic_file_preview_3d_120x120);
                mViewDetailPanel3DP.setVisibility(View.INVISIBLE);
                mViewDetailPanelLaser.setVisibility(View.GONE);
                mViewDetailPanelCNC.setVisibility(View.GONE);
                break;
            case "nc":
                mFileType = Constants.FILE_TYPE_LASER;
                mIvPreviewPictureDefault.setImageResource(R.drawable.ic_file_preview_laser_120x120);
                mViewDetailPanel3DP.setVisibility(View.GONE);
                mViewDetailPanelLaser.setVisibility(View.INVISIBLE);
                mViewDetailPanelCNC.setVisibility(View.GONE);
                break;
            case "cnc":
                mFileType = Constants.FILE_TYPE_CNC;
                mIvPreviewPictureDefault.setImageResource(R.drawable.ic_file_preview_cnc_120x120);
                mViewDetailPanel3DP.setVisibility(View.GONE);
                mViewDetailPanelLaser.setVisibility(View.GONE);
                mViewDetailPanelCNC.setVisibility(View.INVISIBLE);
                break;
            case "sm":
            default:
                mFileType = Constants.FILE_TYPE_UNKNOWN;
                mViewDetailPanel3DP.setVisibility(View.GONE);
                mViewDetailPanelLaser.setVisibility(View.GONE);
                mViewDetailPanelCNC.setVisibility(View.GONE);
                break;
        }

        // buttons
        mBtnStartPrint.setEnabled(false);
        mBtnStartPrint.setText(mHeadType == Module.ModuleType.HEAD_3DP ? R.string.all_start : R.string.all_ready);

        mBtnChangeSettings.setEnabled(false);
    }

    private void initFile() {
        // get file
        mFileManager = ServiceContainer.getInstance().getService(IFileManagerService.class);
        mDevice = mFileManager.getDevice(mIsLocal);
        sub = mFileManager.getFileManagerStateSubjHolder()
                .getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isAttach -> {
                    if (!isAttach) {
                        back();
                    }
                }, LogHelper::log);

        if (mFileType == Constants.FILE_TYPE_UNKNOWN) {
            mGcodeParser = new SnapmakerParser();
        } else {
            mGcodeParser = new GcodeParser();
        }

        try {
            mParseFile = mDevice.search(mFilePath);
            ServiceContainer.getInstance().getService(IMachine.class).getPrintController().reset();
            IMachine.WorkType workType = IMachine.WorkType.NONE;
            switch (mFileType) {
                case Constants.FILE_TYPE_3DP:
                    workType = IMachine.WorkType.FDM;
                    break;
                case Constants.FILE_TYPE_LASER:
                    workType = IMachine.WorkType.LASER;
                    break;
                case Constants.FILE_TYPE_CNC:
                    workType = IMachine.WorkType.CNC;
                    break;
                default:
                    break;
            }
            mGcodeParser.startParse(mParseFile, workType);
        } catch (Exception e) {
            LogHelper.log(e);
            FabAlert.alert(getContext(), R.string.preview_alert_failed_to_open_file);
        }

        // TODO: watch changes on show parameters
        // Listen to parsing progress
        mGcodeParser.getParseProgressObservable()
                .throttleLast(100, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(progress -> {
                    // Return -1 when parsing file failed.
                    if (progress == -1) {
                        FabAlert.alert(getContext(), R.string.preview_alert_failed_to_parse_file);
                        Logger.w("Failed to parse file.");
                        AndroidSchedulers.mainThread().scheduleDirect(this::back, 3000, TimeUnit.MILLISECONDS);
                        return;
                    }

                    if (progress != 100) {
                        mTvDetailInfo.setText(getString(R.string.preview_parsing_file_desc, progress));
                        mViewDetailPanel3DP.setVisibility(View.INVISIBLE);
                    } else {
                        mTvBoundingBox.setVisibility(View.VISIBLE);
                        mTvDetailInfo.setVisibility(View.INVISIBLE);

                        // update fileType
                        IMachine.WorkType workType = mGcodeParser.getFileType();
                        switch (workType) {
                            case FDM:
                                mFileType = Constants.FILE_TYPE_3DP;
                                break;
                            case CNC:
                                mFileType = Constants.FILE_TYPE_CNC;
                                break;
                            case LASER:
                                mFileType = Constants.FILE_TYPE_LASER;
                                break;
                            default:
                                mFileType = Constants.FILE_TYPE_UNKNOWN;
                                break;
                        }
                        if (mFileType == Constants.FILE_TYPE_3DP) {
                            mViewDetailPanel3DP.setVisibility(View.VISIBLE);
                        } else if (mFileType == Constants.FILE_TYPE_LASER) {
                            mViewDetailPanelLaser.setVisibility(View.VISIBLE);
                        } else if (mFileType == Constants.FILE_TYPE_CNC) {
                            mViewDetailPanelCNC.setVisibility(View.VISIBLE);
                        }

                        // check again when sm file is parsed.
                        if (!fileMatchesHead(mFileType, mHeadType)) {
                            FabAlert.alert(getContext(), R.string.preview_alert_file_type_not_match);
                            AndroidSchedulers.mainThread().scheduleDirect(this::back, 2000, Constants.TIME_UNIT);
                        }

                        mBtnStartPrint.setEnabled(true);
                        mBtnChangeSettings.setEnabled(true);

                        handleParsedResult();
                        updateView();
                    }
                }, e -> {
                    LogHelper.log(e);
                    FabAlert.alert(getContext(), R.string.preview_alert_failed_to_parse_file);
                });
    }

    private void updateView() {
        switch (mFileType) {
            case Constants.FILE_TYPE_3DP: {
                double nozzleTemperature = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getOverrideInitialNozzleTemperature();
                double heatedBedTemperature = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getOverrideInitialHeatedBedTemperature();
                double workSpeed = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getOverrideFeedRate();

                mTvBoundingBox.setText(getString(R.string.preview_bounding_box_format_x_y_z,
                        mModelBoundary.getMaxX() - mModelBoundary.getMinX(),
                        mModelBoundary.getMaxY() - mModelBoundary.getMinY(),
                        mModelBoundary.getMaxZ() - mModelBoundary.getMinZ()));

                mDetailPanelWidgetPresenter.setNozzleTemp(nozzleTemperature);
                mDetailPanelWidgetPresenter.setHeatedBedTemp(heatedBedTemperature);
                mDetailPanelWidgetPresenter.setWorkSpeedPercentage(workSpeed);
                mDetailPanelWidgetPresenter.setEstimatedTime(mGcodeParser.getEstimatedTime());
                Logger.d("Update result: nozzle = %.1f, heated bead = %.1f, work speed = %.1f, estimated time =%fs",
                        nozzleTemperature,
                        heatedBedTemperature,
                        workSpeed,
                        mGcodeParser.getEstimatedTime());
                break;
            }
            case Constants.FILE_TYPE_LASER: {
                final double laserPower = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getOverrideLaserPower();
                final double workSpeed = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getOverrideFeedRate();

                if (mIsRotaryAvailable) {
                    mTvBoundingBox.setText(getString(R.string.preview_bounding_box_format_d_l,
                            mGcodeParser.getDiameter(),
                            mModelBoundary.getMaxY() - mModelBoundary.getMinY()));

                } else {
                    mTvBoundingBox.setText(getString(R.string.preview_bounding_box_format_x_y,
                            mModelBoundary.getMaxX() - mModelBoundary.getMinX(),
                            mModelBoundary.getMaxY() - mModelBoundary.getMinY()));
                }

                mDetailPanelWidgetPresenter.setLaserPower(laserPower);
                mDetailPanelWidgetPresenter.setWorkSpeedPercentage(workSpeed);
                mDetailPanelWidgetPresenter.setEstimatedTime(mGcodeParser.getEstimatedTime());// FIXME: 2021/9/7 NPE?
                Logger.d("Update result: laser power = %.1f, work speed = %.1f, estimated time = %fs",
                        laserPower,
                        workSpeed,
                        mGcodeParser.getEstimatedTime());
                break;
            }
            case Constants.FILE_TYPE_CNC: {
                final double workSpeed = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getOverrideFeedRate();

                if (mIsRotaryAvailable) {
                    mTvBoundingBox.setText(getString(R.string.preview_bounding_box_format_d_l,
                            mGcodeParser.getDiameter(),
                            mModelBoundary.getMaxY() - mModelBoundary.getMinY()));
                } else {
                    mTvBoundingBox.setText(getString(R.string.preview_bounding_box_format_x_y,
                            mModelBoundary.getMaxX() - mModelBoundary.getMinX(),
                            mModelBoundary.getMaxY() - mModelBoundary.getMinY()));
                }

                mDetailPanelWidgetPresenter.setSpindleSpeed(12000); // fixed
                mDetailPanelWidgetPresenter.setWorkSpeedPercentage(workSpeed);
                mDetailPanelWidgetPresenter.setEstimatedTime(mGcodeParser.getEstimatedTime());
                Logger.d("Update result: work speed = %.1f, estimated time = %fs",
                        workSpeed,
                        mGcodeParser.getEstimatedTime());
                break;
            }
        }
    }

    private void handleParsedResult() {
        mModelBoundary = mGcodeParser.getBoundary();
        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setModelBoundary(mModelBoundary);
        // FIXME: what if G-code needs to run x y b, what dimensions of boundary should touchscreen executed?
        if (mIsRotaryAvailable) {
            mModelBoundary.setDimension(ModelBoundary.DIMENSION_BY);
        } else {
            mModelBoundary.setDimension(ModelBoundary.DIMENSION_XY);
        }
        Logger.d(mModelBoundary.toString());

        // show gcode thumbnail
        Bitmap thumbnail = mGcodeParser.getGcodeThumbnail();
        if (thumbnail != null) {
            mIvPreviewPicture.setImageBitmap(thumbnail);
            mIvPreviewPictureDefault.setVisibility(ImageView.GONE);
            mIvPreviewPicture.setVisibility(ImageView.VISIBLE);
        }

        switch (mFileType) {
            case Constants.FILE_TYPE_3DP: {
                final float heatedBedTemperature = mGcodeParser.getBedTargetTemperature();
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setOverrideInitialHeatedBedTemperature(heatedBedTemperature);

                final float nozzleTemperature = mGcodeParser.getNozzleTargetTemperature();
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setOverrideInitialNozzleTemperature(nozzleTemperature);

                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setOverrideFeedRate(100);
                break;
            }
            case Constants.FILE_TYPE_LASER: {
                final float laserPower = mGcodeParser.getPower();
                if (laserPower == 0) {
                    ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setOverrideLaserPower(100);
                } else {
                    ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setOverrideLaserPower(laserPower);
                }
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setOverrideFeedRate(100);
                break;
            }
            case Constants.FILE_TYPE_CNC: {
                ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setOverrideFeedRate(100);
                break;
            }
            default:
                break;
        }
    }

    private AlertDialog showLoadingWorkspaceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.color.default_theme_dark_mask);
            dialog.getWindow().setLayout(280 * 2, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_preview_copy_to_workspace_loading, null);
        dialog.setView(view);
        dialog.show();
        return dialog;
    }

    @OnClick(R2.id.btn_preview_start_print)
    void onStartPrint() {
        playNormalClickSound();
        if (mHeadType != Module.ModuleType.HEAD_3DP) {
            realStartPrint();
            return;
        }
        // FIXME: need to remove DeprecatedBatchPrintController
        if (ServiceContainer.getInstance().getService(IAppService.class).getPrintController() instanceof DeprecatedBatchPrintController) {
//            // Check if ever calibrated for 3dp
//            ServiceContainer.getInstance().getService(IMachine.class).getFDMController().checkCalibrationEverSucceeded()
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .as(bindToLifecycle())
//                    .subscribe(resultStructure -> {
//                        boolean calibrated = resultStructure.isSuccess();
//                        if (calibrated) {
//                            realStartPrint();
//                        } else {
//                            warnNotCalibrated();
//                        }
//                    });
        } else {
            realStartPrint();
        }
    }

    private void warnNotCalibrated() {
        FabConfirm.create(getContext())
                .setIcon(R.drawable.pic_dialog_warning_72x72)
                .setDescription(R.string.preview_warning_not_calibrated)
                .setCanceledOnTouchOutSide(false)
                .setConfirm(R.string.all_calibrate, (dialog, which) -> {
                    dialog.dismiss();
                    // Calibrating and wait for a result.
                    ServiceContainer.getInstance().getService(IRouter.class)
                            .routeTo3DPCalibrationPage(false)
                            .startForResult(this, REQUEST_CALIBRATION_RESULT);
                })
                .setCancel(R.string.all_continue, (dialog, which) -> {
                    dialog.dismiss();
                    // Ignore calibration absent.
                    realStartPrint();
                })
                .show();
    }

    private void realStartPrint() {
        PreviewActivity activity = (PreviewActivity) getActivity();
        if (activity == null) return;
        if (sub != null && !sub.isDisposed()) {
            sub.dispose();
            sub = null;
        }

        // Save information of the file we gonna print
        ServiceContainer.getInstance().getService(IAppService.class).getWorkspace().setPrintSource(Constants.PRINT_SOURCE_SCREEN);
        ServiceContainer.getInstance().getService(IAppService.class).getWorkspace().setEstimatedTime(mGcodeParser.getEstimatedTime());
        ServiceContainer.getInstance().getService(IAppService.class).getWorkspace().setFileTotalLineCount(mGcodeParser.getTotalLinesCount());

        // Show loading dialog while copying.
        AlertDialog dialog = showLoadingWorkspaceDialog();

        // Start copying file into workspace, dismiss dialog when finished.
        ServiceContainer.getInstance().getService(IAppService.class).getWorkspace().addFileToWorkspace(mParseFile)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    if (success) {
                        dialog.dismiss();
                        switch (mHeadType) {
                            case Module.ModuleType.HEAD_3DP: {
                                ServiceContainer.getInstance().getService(IRouter.class)
                                        .routeToPrintPage()
                                        .start(getContext());
                                break;
                            }
                            case Module.ModuleType.HEAD_LASER:
                            case Module.ModuleType.HEAD_LASER_10W: {
                                if (ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isRotaryAvailable) {
//                                    activity.gotoLaserRotarySetupModeFragment();
                                    activity.gotoLaserRotarySetMaterial();
                                } else {
                                    activity.gotoLaserPrepareModeFragment();
                                }
                                break;
                            }
                            case Module.ModuleType.HEAD_CNC: {
                                if (ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isRotaryAvailable) {
                                    activity.gotoCNCPrepareRotaryOriginRemindFragment();
                                } else {
                                    activity.gotoCNCPrepareSafetyGogglesFragment();
                                }
                                break;
                            }
                        }
                    }
                }, e -> {
                    LogHelper.log(e);
                    dialog.dismiss();
                });
    }

    @OnClick(R2.id.btn_preview_change_settings)
    void onClickChangeSettings() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class).routeToPrintSettingsPage().start(getContext());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CALIBRATION_RESULT && resultCode == Activity.RESULT_OK) {
            realStartPrint();
        }
    }
}
