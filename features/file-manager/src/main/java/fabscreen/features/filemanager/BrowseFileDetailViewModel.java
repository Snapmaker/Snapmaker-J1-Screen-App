package fabscreen.features.filemanager;

import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP_DOUBLE_EXTRUDER;

import android.content.Context;
import android.graphics.Bitmap;

import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import fabscreen.features.filemanager.entity.BrowseShowFile;
import fabscreen.platform.base.helper.Md5Util;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.parser.IGcodeParser;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.model.ModelBoundary;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IFileManagerService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class BrowseFileDetailViewModel extends BaseViewModel {
    private IGcodeParser mParser;
    private IPrintWorkspace mPrintWorkspace;
    private int mPrintMode = 0;
    private IFile mFile;
    private final IMachine mMachine;
    private final IMachine.WorkType mWorkType;
    private final MachineInfo mMachineInfo;
    private int mHeadToolType = -1;
    private Context mContext;
    private boolean mIsJ1;

    public static final int MODE_NORMAL = 101;
    public static final int MODE_DISABLE_DUAL_EXTRUSION = 102;
    public static final int MODE_OUT_OF_RANGE = 103;
    private BrowseShowFile mBrowseShowFile;
    private IFileManagerService mFileManagerService;
    private BehaviorSubject<Boolean> mIsHaveUSBState = BehaviorSubject.createDefault(false);

    public BrowseFileDetailViewModel() {
        super();
        mParser = ServiceContainer.getInstance().getService(IGcodeParser.class);
        mPrintWorkspace = ServiceContainer.getInstance().getService(IPrintWorkspace.class);
        mMachine = getServiceContainer().getService(IMachine.class);
        mMachineInfo = mMachine.getMachineInfoSubjectHolder().getValue();
        mContext = getServiceContainer().getService(IAppService.class).getAppContext();
        mWorkType = mMachineInfo.workType;
        mPrintWorkspace.setPrintModeXOffset(0);
        mIsJ1 = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().seriesId == IMachine.MachineSeries.J;
        mFileManagerService = ServiceContainer.getInstance().getService(IFileManagerService.class);
        //init mFileManagerService value
        mIsHaveUSBState.onNext(mFileManagerService.getFileManagerStateSubjHolder()
                .getValue());
        mFileManagerService.getFileManagerStateSubjHolder()
                .getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aBoolean -> {
                    mIsHaveUSBState.onNext(aBoolean);
                }, LogHelper::log);
    }

    public Bitmap getGcodeThumbnail() {
        return mParser.getGcodeThumbnail();
    }

    public ArrayList<DetailDesc> getShowData() {
        ArrayList<DetailDesc> detailDescs = new ArrayList<>();
        String str = "";
        if (!mIsJ1) {
            if (mParser.getHeaderType() != -1) {
                mHeadToolType = mParser.getHeaderType();
                str = mContext.getString(mParser.getHeaderNameID());
                detailDescs.add(new DetailDesc("Toolhead", str));
            }

            if (mParser.isIsRotate() != -1) {
                str = mParser.isIsRotate() == 0 ? "3-axis" : "4-axis";
                detailDescs.add(new DetailDesc("Job Type", str));
            }

            if (mParser.getWorkSizeX() != -1 && mParser.getWorkSizeY() != -1) {
                str = mParser.getWorkSizeX() + " x " + mParser.getWorkSizeY() + "mm";
                detailDescs.add(new DetailDesc("Work Size", str));
            }

            if (mParser.getOrigin() != null) {
                str = mParser.getOrigin();
                detailDescs.add(new DetailDesc("Work Origin", str));
            }
        }

        if (mParser.getMaterial_0() != null) {
            str = "L:" + mParser.getMaterial_0();
            if (mParser.getMaterial_1() != null) {
                str += " R:" + mParser.getMaterial_1();
            }
            detailDescs.add(new DetailDesc(mContext.getString(R.string.j1_file_details_filament), str));
        }

        mHeadToolType = mParser.getHeaderType();
        if (mHeadToolType == HEAD_3DP || mHeadToolType == HEAD_3DP_DOUBLE_EXTRUDER) {
            if (mParser.getNozzleTargetTemperature() != -1) {
                str = "L:" + (int) mParser.getNozzleTargetTemperature() + "℃";
                if (mParser.getNozzleTarget_1_Temperature() != -1) {
                    str += " R:" + (int) mParser.getNozzleTarget_1_Temperature() + "℃";
                }
                detailDescs.add(new DetailDesc(mContext.getString(R.string.j1_file_details_nozzle_temp), str));
            }
        }

        if (mParser.getNozzle_0_Diameter() != -1) {
            str = "L:" + mParser.getNozzle_0_Diameter() + " mm";
            if (mParser.getNozzle_1_Diameter() != -1) {
                str += " R:" + mParser.getNozzle_1_Diameter() + " mm";
            }
            detailDescs.add(new DetailDesc(mContext.getString(R.string.j1_file_details_nozzle_diameter), str));
        }

        if (mParser.getBedTargetTemperature() != 0) {
            str = (int) mParser.getBedTargetTemperature() + "℃";
            detailDescs.add(new DetailDesc(mContext.getString(R.string.j1_file_details_heated_bed_temp), str));
        }

        if (!mIsJ1) {
            if (mParser.getLayerNumber() != -1 || mParser.getLayerHeight() != -1) {
                str = "";
                if (mParser.getLayerNumber() != -1) {
                    str += mParser.getLayerNumber();
                } else {
                    str += " - ";
                }

                if (mParser.getLayerHeight() != -1) {
                    str += "/ " + mParser.getLayerHeight() + " mm";
                } else {
                    str += "/ - ";
                }

                detailDescs.add(new DetailDesc("Layer Number / Layer Ht.", str));
            }
        }

        if (mParser.getEstimatedTime() != 0) {
            str = formatTime(mParser.getEstimatedTime());
            detailDescs.add(new DetailDesc(mContext.getString(R.string.j1_file_details_estimated_time), str));
        }

        if (!mIsJ1) {
            if (mParser.getMaterialWeight() != -1 || mParser.getLayerHeight() != -1) {
                str = "";
                if (mParser.getMaterialLength() != -1) {
                    str += String.format(Locale.ENGLISH, "%.1f", mParser.getMaterialLength()) + " m";
                } else {
                    str += " - m";
                }

                if (mParser.getMaterialWeight() != -1) {
                    str += " /" + String.format(Locale.ENGLISH, "%.1f", mParser.getMaterialWeight()) + " g";
                } else {
                    str += " / - g";
                }

                detailDescs.add(new DetailDesc("Material Required ", str));
            }
        }

        return detailDescs;
    }

    public Observable<Boolean> handleResult() {
        mPrintWorkspace.setPrintMode(mPrintMode);
        mPrintWorkspace.setPrintSource(0);
        mPrintWorkspace.setFileTotalLineCount(mParser.getTotalLinesCount());
        mPrintWorkspace.setEstimatedTime(mParser.getEstimatedTime());
        String md5Value = Md5Util.fileToMD5(mFile.getAbsolutePath());
        // FIXME: M0 version firmware using "c319528c5c360d46031b69d39e01ceb3" directly(which was not what we want),
        //  For now this will treat as a magic number.
        mPrintWorkspace.setFileMD5Value(md5Value == null ? "c319528c5c360d46031b69d39e01ceb3" : md5Value);
        mPrintWorkspace.setModelBoundary(mParser.getBoundary());

        mPrintWorkspace.setApplyMultiExtruder(mParser.isApplyMultiExtruder());
        if (mParser.getFileType() == IMachine.WorkType.FDM) {
            if (mParser.getHeaderType() == HEAD_3DP) {
                mPrintWorkspace.setWorkTemperature(new float[]{mParser.getNozzleTargetTemperature()});
            } else if (mParser.getHeaderType() == HEAD_3DP_DOUBLE_EXTRUDER) {
                mPrintWorkspace.setWorkTemperature(new float[]{mParser.getNozzleTargetTemperature(), mParser.getNozzleTarget_1_Temperature()});
            }
        }
        // FIXME:Temporarily add : the copied file when clicking the file
        return mPrintWorkspace.addFileToWorkspace(mFile);
    }

    public int getPrintMode() {
        return mPrintMode;
    }

    public void setPrintMode(int mPrintMode) {
        this.mPrintMode = mPrintMode;
    }

    public void setFile(String filePath, boolean isLocal) {
        mFile = ServiceContainer.getInstance().getService(IFileManagerService.class).getDevice(isLocal).search(filePath);
    }

    public void setFile(BrowseShowFile browseShowFile) {
        mBrowseShowFile = browseShowFile;
        mFile = mBrowseShowFile.getIFile();
    }

    public String getFileName() {
        if (mFile == null) {
            return "NULL";
        } else {
            return mFile.getName();
        }
    }

    public String getFileInfo() {
        if (mFile == null) {
            return "NULL";
        } else {
            long fileLength = mFile.length();
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy  hh:mm", Locale.getDefault());
            String lastModified = sdf.format(new Date(mFile.lastModified()));
            String fileLengthUnit = "bytes";
            if (fileLength > 1024) {
                fileLength /= 1024;
                fileLengthUnit = "KB";
                if (fileLength > 1024) {
                    fileLength /= 1024;
                    fileLengthUnit = "MB";
                }
            }
            return String.format(Locale.ENGLISH, "%s %s  %s", fileLength, fileLengthUnit, lastModified);
        }
    }

    public Observable<Boolean> checkToolhead() {

        int gcodeHeadType = mParser.getHeaderType();
        int machineHeadType = -1;
        if (mWorkType == IMachine.WorkType.FDM) {
            machineHeadType = mMachine.getFDMController().getHeadType();
        } else if (mWorkType == IMachine.WorkType.LASER) {
            machineHeadType = mMachine.getLaserController().getHeadType();
        } else if (mWorkType == IMachine.WorkType.CNC) {
            machineHeadType = mMachine.getCNCController().getHeadType();
        }

        return Observable.just(machineHeadType != -1 && machineHeadType == gcodeHeadType);
    }

    public Observable<Boolean> checkExtruder() {
        if (mWorkType == IMachine.WorkType.FDM && mMachine.getFDMController().getHeadType() == Module.ModuleType.HEAD_3DP_DOUBLE_EXTRUDER) {
            return Observable.just(extruderMatch());
        } else {
            return Observable.just(true);
        }
    }

    private boolean extruderMatch() {
        try {
            float diameter0 = mMachine.getFDMController().getToolheadStatusSubjectHolder(0).getValue().getExtruderList().get(0).getDiameter();
            float diameter1 = mMachine.getFDMController().getToolheadStatusSubjectHolder(0).getValue().getExtruderList().get(1).getDiameter();
            float gcodeDiameter0 = mParser.getNozzle_0_Diameter();
            float gcodeDiameter1 = mParser.getNozzle_1_Diameter();
            return diameter0 == gcodeDiameter0 && diameter1 == gcodeDiameter1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Observable<Boolean> checkFileExtruderRetractionDistance() {
        final float extruder0Retraction = mParser.getExtruder0RetractionDistance();
        final float extruder1Retraction = mParser.getExtruder1RetractionDistance();
        Logger.d("e0 retraction %.2f, e1 retraction %.2f", extruder0Retraction, extruder1Retraction);
        boolean isRetractionOverLimit = extruder0Retraction > 2f || extruder1Retraction > 2f;

        return Observable.just(!isRetractionOverLimit);

    }

    public Observable<Boolean> getUsbStateObservable() {
        return mIsHaveUSBState.hide();
    }

    public int getFilePrintMode() {
        return mParser.getPrintMode();
    }

    public BrowseShowFile getBrowseShowFile() {
        return mBrowseShowFile;
    }

    public int checkPrintModeAvailable(int selectMode) {
        int modeType = MODE_NORMAL;
        ModelBoundary boundary = mParser.getBoundary();
        float modelXRange = boundary.getMaxX() - boundary.getMinX();
        switch (selectMode) {
            case IPrintWorkspace.PRINT_MODE_NORMAL:
                // Do nothing with normal mode.
                break;
            case IPrintWorkspace.PRINT_MODE_DUAL_EXTRUDER_BACK_UP:
                if (mParser.isApplyMultiExtruder()) {
                    modeType = MODE_DISABLE_DUAL_EXTRUSION;
                }
                break;
            case IPrintWorkspace.PRINT_MODE_CLONE:
                // Not available when multiple extruder was already used in G-code.
                if (mParser.isApplyMultiExtruder()) {
                    modeType = MODE_DISABLE_DUAL_EXTRUSION;
                } else {
                    // Print Model out of range
                    modeType = modelXRange < 160 ? MODE_NORMAL : MODE_OUT_OF_RANGE;
                }
                break;
            case IPrintWorkspace.PRINT_MODE_MIRROR:
                // Not available when multiple extruder is already use in G-code.
                if (mParser.isApplyMultiExtruder()) {
                    modeType = MODE_DISABLE_DUAL_EXTRUSION;
                } else {
                    // Print Model out of range
                    modeType = modelXRange < 150 ? MODE_NORMAL : MODE_OUT_OF_RANGE;
                }
                break;
        }
        return modeType;
    }

    public void setXOffsetWithMode(int selectMode) {
        int filePrintMode = mParser.getPrintMode();
        ModelBoundary boundary = mParser.getBoundary();
        float modelXRange = boundary.getMaxX() - boundary.getMinX();
        // print area x is 300mm as default in J1
        int printAreaCenterX;
        switch (filePrintMode) {
            case IPrintWorkspace.PRINT_MODE_NORMAL:
            case IPrintWorkspace.PRINT_MODE_DUAL_EXTRUDER_BACK_UP:
                if (selectMode == IPrintWorkspace.PRINT_MODE_NORMAL || selectMode == IPrintWorkspace.PRINT_MODE_DUAL_EXTRUDER_BACK_UP) {
                    // No offset is needed.
                    mPrintWorkspace.setPrintModeXOffset(0);
                } else {
                    // print area minus half
                    printAreaCenterX = (selectMode == IPrintWorkspace.PRINT_MODE_CLONE) ? 80 : 75;
                    mPrintWorkspace.setPrintModeXOffset(printAreaCenterX - (modelXRange * 0.5f + boundary.getMinX()));
                }
                break;

            case IPrintWorkspace.PRINT_MODE_CLONE:
            case IPrintWorkspace.PRINT_MODE_MIRROR:
                if (selectMode == IPrintWorkspace.PRINT_MODE_CLONE || selectMode == IPrintWorkspace.PRINT_MODE_MIRROR) {
                    mPrintWorkspace.setPrintModeXOffset(0);
                } else {
                    printAreaCenterX = 150;
                    mPrintWorkspace.setPrintModeXOffset(printAreaCenterX - (modelXRange * 0.5f + boundary.getMinX()));
                }
                break;
        }
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
}
