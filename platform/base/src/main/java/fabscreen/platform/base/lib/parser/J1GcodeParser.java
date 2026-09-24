package fabscreen.platform.base.lib.parser;

import static fabscreen.platform.base.service.IMachine.WorkType.CNC;
import static fabscreen.platform.base.service.IMachine.WorkType.FDM;
import static fabscreen.platform.base.service.IMachine.WorkType.LASER;
import static fabscreen.platform.base.service.IMachine.WorkType.NONE;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_3DP_DOUBLE_EXTRUDER;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_CNC;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_CNC_200W;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_LASER;
import static fabscreen.platform.base.service.machine.entity.Module.ModuleType.HEAD_LASER_10W;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.annotation.Keep;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import fabscreen.platform.base.R;
import fabscreen.platform.base.helper.StringHelper;
import fabscreen.platform.base.instantiation.IServiceIdentifier;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.FabFileInputStream;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.model.ModelBoundary;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IFileManagerService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import okio.BufferedSource;
import okio.Okio;

/**
 * J1CodeParser
 * <p>
 * Forked from GcodeParser.
 * <p>
 * A Temporary workaround for J1 G-code file header,
 * which sliced by Cura J1 Plugin or future version of Snapmaker Luban.
 */
public class J1GcodeParser implements IGcodeParser, IServiceIdentifier {
    private static final String BOUND_HEADER_START_MARK_CURA = ";START_OF_HEADER";
    private static final String BOUND_HEADER_END_MARK_CURA = ";END_OF_HEADER";
    private static final String BOUND_HEADER_START_MARK_SNAPMAKER = ";Header Start";
    private static final String BOUND_HEADER_END_MARK_SNAPMAKER = ";Header End";
    private static String TAG = "GcodeParser";
    // comment marker
    private static final String BOUNDS_START_MARK = ";Start GCode end";
    private static final String BOUNDS_END_MARK = ";End GCode begin";
    private static final String BOUNDS_END_MARK_V1 = ";--- End G-code Begin ---";
    private boolean mIsParsingHeader = false;

    // Reader
    private long totalBytes;
    private long readBytes;
    private BufferedSource source;
    private FabFileInputStream mFabFileInputStream;
    private String[] lineArgs = new String[20];
    private int lineArgCount;

    // G-code state
    private boolean mIsCurrentGcodeStateWorking = false;
    private Position mCurrentPosition;

    private boolean mShouldUpdateAttribute = true;
    private boolean isAbsoluteCoordinate = true;

    private float mBedTargetTemperature = 0;
    private float mNozzleTarget_0_Temperature = 0;
    private float mNozzleTarget_1_Temperature = 0;

    private float mCurrentLineFeedRate = 0;
    private double mFeedRateAmount = 0;
    private int mFeedRateCount = 0;

    private float extrusionAmount = 0;

    private float mPower = 0;

    private float mSpindleSpeed = 0;

    private IMachine.WorkType mFileType = NONE;
    private int mParseProgress = 0;

    private float mEstimateTime = 0;
    private int mTotalLinesCount = 0;

    private float mWorkSpeed = 0;
    private float mJogSpeed = 0;

    private float mDiameter = 0;

    private int mPrintMode = 0;
    private boolean mIsDefineT0 = false;
    private boolean mIsDefineT1 = false;

    private Bitmap mGcodeThumbnail;
    private byte[] mGcodeThumbnailBytes;
    private ModelBoundary mModelBoundary;
    private ArrayList<Position> mToolPath = new ArrayList<>();

    private BehaviorSubject<Integer> mParseProgressSubject = BehaviorSubject.createDefault(0);
    private Scheduler.Worker mParseWorker;

    private int mToolHead = -1;
    private float mNozzle_0_Diameter = -1;
    private float mNozzle_1_Diameter = -1;
    private int mLayerNumber = -1;
    private float mLayerHeight = -1;
    private float mMaterialWeight = -1;
    private float mMaterialLength = -1;
    private String mNozzle_0_Material = null;
    private String mNozzle_1_Material = null;
    private String mRenderMethod = null;
    private int mIsRotate = -1;
    private float mWorkSizeX = -1;
    private float mWorkSizeY = -1;
    private String mOrigin = null;
    private String mMachine = null;
    private int mToolHeadNameID = -1;
    private int mExtrudersUsed = 1;

    private String mHeaderVersion = null;
    private String mSlicerEngine = null;

    private int mCurrentExtruder = 0;
    private int mT0RetractionCount = 0;
    private int mT1RetractionCount = 0;
    private float mLastEAxisPosition = 0;
    private boolean mFDMRetractionDetected = false;
    private float mExtruder0RetractionDistance = 0;
    private float mExtruder0SwitchRetractionDistance = 0;
    private float mExtruder1RetractionDistance = 0;
    private float mExtruder1SwitchRetractionDistance = 0;

    private HeaderParamsChecker mHeaderChecker;

    @Keep
    public J1GcodeParser() {
        mModelBoundary = new ModelBoundary();
    }

    @Override
    public void startParse(String filePath, boolean isLocal, IMachine.WorkType fileType) {
        IFile search = ServiceContainer.getInstance().getService(IFileManagerService.class).getDevice(isLocal).search(filePath);
        startParse(search, fileType);
    }

    @Override
    public void startParse(IFile file, IMachine.WorkType fileType) {
        synchronized (J1GcodeParser.this) {
            mFileType = fileType;

            try {
                totalBytes = file.length();
                readBytes = 0;

                mFabFileInputStream = file.getInputStream();
                source = Okio.buffer(Okio.source(mFabFileInputStream.getInputStream()));
            } catch (IOException e) {
                LogHelper.log(e);
                if (mFabFileInputStream != null) {
                    try {
                        mFabFileInputStream.close();
                    } catch (IOException e1) {
                        LogHelper.log(e1);
                    }

                }
            }
            // Create parse worker if not exist
            if (mParseWorker == null) {
                mParseWorker = Schedulers.computation().createWorker();
            }

            // Start parse here in computation scheduler
            mParseWorker.schedule(this::parse);
        }
    }

    @Override
    public void startParse(InputStream io, IMachine.WorkType fileType) {
        synchronized (J1GcodeParser.this) {
            mFileType = fileType;
            try {
                totalBytes = io.available();
                readBytes = 0;
                source = Okio.buffer(Okio.source(io));
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Create parse worker if not exist
            if (mParseWorker == null) {
                mParseWorker = Schedulers.computation().createWorker();
            }

            // Start parse here in computation scheduler
            mParseWorker.schedule(this::parse);
        }
    }

    private void parse() {
        String line;
        int linesCount = 0;
        int newLineBytes = 1;

        // Peek to count total lines
        try {
            BufferedSource peek = source.peek();
            // https://github.com/square/okio/blob/master/okio/jvm/src/main/java/okio/Buffer.kt#L648
            // Check if the file uses "\n" or "\r\n", which will affect our byte calculation
            long carriage = peek.indexOf((byte) ('\r'), 0, 64);
            if (carriage != -1) {
                newLineBytes = 2;
            }
            peek.close();
        } catch (IOException e) {
            e.printStackTrace();
            mParseProgressSubject.onNext(-1);
            return;
        }

        // Reset progress
        mParseProgressSubject.onNext(0);
        mTotalLinesCount = 0;
        mIsParsingHeader = false;
        mHeaderChecker = HeaderParamsChecker.getInstance();
        mHeaderChecker.reset();

        resetResult();

        while (true) {
            try {
                line = source.readUtf8Line();

                if (line == null) break;

                // stop parsing if header exists and get totalLinesCount
                if (mTotalLinesCount != 0 && mFDMRetractionDetected && !mIsParsingHeader) {
                    Logger.w("parsing break!");
                    break;
                }

                // Update progress
                readBytes += line.length() + newLineBytes;
                mParseProgress = (int) (100 * readBytes / totalBytes);
                if (mParseProgress <= 100 && mParseProgress > mParseProgressSubject.getValue()) {
                    mParseProgressSubject.onNext(mParseProgress);
                }

                // fixme: Need to refactor format number for parsing args instead of throwing exception
                parseLine(line);
                linesCount++;
            } catch (Exception e) {
                e.printStackTrace();
                LogHelper.log(e);
                mParseProgressSubject.onNext(-1);
                return;
            }
        }
        try {
            if (source != null) {
                source.close();
            }
            if (mFabFileInputStream != null) {
                mFabFileInputStream.close();
            }
        } catch (IOException e) {
            LogHelper.log(e);
        }

        if (!mHeaderChecker.isTotalLinesCheck()) {
            mTotalLinesCount = (mTotalLinesCount == 0) ? linesCount : mTotalLinesCount;
        }
        checkPathClose();

        // Set progress to 100
        mParseProgressSubject.onNext(100);
    }

    private void resetResult() {
        if (mModelBoundary != null) {
            mModelBoundary = new ModelBoundary();
        }
        mCurrentPosition = null;
        mFileType = NONE;
        mToolHead = -1;
        mGcodeThumbnail = null;
        mTotalLinesCount = 0;
        mEstimateTime = 0;
        mNozzleTarget_0_Temperature = 0;
        mNozzleTarget_1_Temperature = 0;
        mBedTargetTemperature = 0;
        mPower = 0;
        mWorkSpeed = 0;
        mJogSpeed = 0;
        mDiameter = 0;
        mNozzle_0_Diameter = -1;
        mNozzle_1_Diameter = -1;
        mLayerNumber = -1;
        mLayerHeight = -1;
        mMaterialWeight = -1;
        mMaterialLength = -1;
        mNozzle_0_Material = null;
        mNozzle_1_Material = null;
        mRenderMethod = null;
        mIsRotate = -1;
        mWorkSizeX = -1;
        mWorkSizeY = -1;
        mOrigin = null;
        mMachine = null;
        mPrintMode = 0;
        mIsDefineT0 = false;
        mIsDefineT1 = false;
        mToolHeadNameID = -1;
        mHeaderVersion = null;
        mSlicerEngine = null;
        mExtrudersUsed = 1;
        mFDMRetractionDetected = false;
        mLastEAxisPosition = 0;
        mT0RetractionCount = 0;
        mT1RetractionCount = 0;
        mExtruder0RetractionDistance = 0;
        mExtruder0SwitchRetractionDistance = 0;
        mExtruder1RetractionDistance = 0;
        mExtruder1SwitchRetractionDistance = 0;
        mCurrentExtruder = 0;
        mShouldUpdateAttribute = true;
    }

    private void parseLine(final String line) throws NumberFormatException {
        if (line.isEmpty()) return;

        // Straight comment
        if (line.charAt(0) == ';') {
            if (containsEndGcodeMark(line)) {
                mShouldUpdateAttribute = false;
            }

            //  parse header markers
            if (mIsParsingHeader) {
                if (line.equals(BOUND_HEADER_END_MARK_SNAPMAKER) || line.equals(BOUND_HEADER_END_MARK_CURA)) {
                    if (line.equals(BOUND_HEADER_END_MARK_CURA)) {
                        mFileType = FDM;
                    }
                    mIsParsingHeader = false;
                } else {
                    parseHeader(line);
                }
                return;
            } else {
                if (line.equals(BOUND_HEADER_START_MARK_SNAPMAKER) || line.equals(BOUND_HEADER_START_MARK_CURA)) {
                    mIsParsingHeader = true;
                }
                return;
            }
        }

        // Parse line to separate arguments
        final int length = line.length();

        lineArgCount = 0;
        int pos = 0;
        while (pos < length) {
            while (pos < length && line.charAt(pos) == ' ') pos++;

            if (pos == length) break;
            if (line.charAt(pos) == ';') break;

            int start = pos;
            pos++;
            while (pos < length) {
                char c = line.charAt(pos);
                if (c == ' ' || c == ';' || StringHelper.isAlphabetic(c)) break;
                pos++;
            }

            lineArgs[lineArgCount++] = line.substring(start, pos);
            if (lineArgCount >= 20) break;
        }

        if (lineArgCount == 0) return;

        // Parse arguments based on G-code command
        switch (lineArgs[0]) {
            case "G0":
            case "G1": {
                parseG0G1();
                break;
            }
            case "G4": {
                parseG4();
                break;
            }
            case "G20":
            case "G21": {
                break;
            }
            case "G28": {
//                parseG28();
                break;
            }
            case "G90": {
                // absolute position
                isAbsoluteCoordinate = true;
                break;
            }
            case "G91": {
                // relative position
                isAbsoluteCoordinate = false;
                break;
            }
            case "G92": {
                parseG92();
                break;
            }
            case "M3":
            case "M5": {
                if (mHeaderChecker.isLaserPowerCheck()) break;

                parseM3M5();
                break;
            }
            case "M83": {
                isAbsoluteCoordinate = false;
            }
            case "M140":
            case "M190": {
                if (mHeaderChecker.isHeatedBedTempCheck()) break;
                // heated bed
                parseHeatedBedTemperature();
                break;
            }
            case "M104":
            case "M109": {
                if (mHeaderChecker.isNozzleTempCheck()) break;
                // nozzle
                parseNozzleTemperature();
                break;
            }
            case "T0":
                mIsDefineT0 = true;
                mCurrentExtruder = 0;
                break;
            case "T1":
                mIsDefineT1 = true;
                mCurrentExtruder = 1;
                break;
            case "M605": {
                if (mHeaderChecker.isPrintModeCheck()) break;

                parseM605();
                break;
            }
            default:
                break;
        }
    }

    private void checkPathStart() {
        if (!mIsCurrentGcodeStateWorking && mCurrentPosition != null) {
            mIsCurrentGcodeStateWorking = true;

            mCurrentPosition.setAsStartPoint();
//            mToolPath.add(currentPosition);
        }
    }

    private void checkPathClose() {
        if (mIsCurrentGcodeStateWorking && mCurrentPosition != null) {
            mIsCurrentGcodeStateWorking = false;

            mCurrentPosition.setAsEndPoint();
        }
    }

    private void parseHeader(String line) throws NumberFormatException {
        final int length = line.length();

        lineArgCount = 0;
        // skip comment mark
        int pos = 1;

        while (pos < length) {
            while (pos < length && line.charAt(pos) == ' ') pos++;

            if (pos == length) break;

            int start = pos;
            while (pos < length && line.charAt(pos) != ':') pos++;

            lineArgs[lineArgCount++] = line.substring(start, pos);

            pos++;
        }

        if (lineArgCount == 0) return;
        if ("null".equals(lineArgs[1])) return;
        // Parse arguments based on G-code command
        switch (lineArgs[0]) {
            /* Version */
            case "Version":
                mHeaderVersion = lineArgs[1];
                break;
            /* Slicer */
            case "Slicer":
                mSlicerEngine = lineArgs[1];
                break;
            /* Printer */
            case "Printer":
                mMachine = lineArgs[1];
                break;
            /* Estimated Print Time */
            case "Estimated Print Time":
            case "estimated_time(s)": // SM2
            case "PRINT.TIME":  // Deprecated
                mEstimateTime = Float.parseFloat(lineArgs[1]);
                mHeaderChecker.setEstimatedTimeCheck(true);
                break;
            /* Lines */
            case "Lines":
            case "file_total_lines": // SM2
                mTotalLinesCount = Integer.parseInt(lineArgs[1]);
                mHeaderChecker.setTotalLinesCheck(true);
                break;
            /* Extruder Mode */
            case "Extruder Mode":
                switch (lineArgs[1]) {
                    case "IDEX Auto Park":
                        mPrintMode = 1;
                        break;
                    case "IDEX Duplication":
                        mPrintMode = 2;
                        break;
                    case "IDEX Mirror":
                        mPrintMode = 3;
                        break;
                    case "IDEX Full Control":
                    default:
                        mPrintMode = 0;
                        break;
                }
                mHeaderChecker.setPrintModeCheck(true);
                break;
            /* Extruder 0 Nozzle Size */
            case "Extruder 0 Nozzle Size":
            case "nozzle_0_diameter":
            case "nozzle_0_diameter(mm)":
                mNozzle_0_Diameter = Float.parseFloat(lineArgs[1]);
                break;
            /* Extruder 0 Material */
            case "Extruder 0 Material":
            case "nozzle_0_material":
                mNozzle_0_Material = lineArgs[1];
                break;
            /* Extruder 0 Print Temperature */
            case "Extruder 0 Print Temperature":
            case "nozzle_temperature(°C)":
            case "nozzle_0_temperature(°C)":
            case "EXTRUDER_TRAIN.0.INITIAL_TEMPERATURE":
                mNozzleTarget_0_Temperature = Float.parseFloat(lineArgs[1]);
                mHeaderChecker.setNozzleTempCheck(true);
                // Workaround
                mToolHead = HEAD_3DP;
                Logger.d("debug Extruder 0 Print Temperature " + mToolHead);
                break;
            /* Extruder 1 Nozzle Size */
            case "Extruder 1 Nozzle Size":
            case "nozzle_1_diameter":
            case "nozzle_1_diameter(mm)":
                mNozzle_1_Diameter = Float.parseFloat(lineArgs[1]);
                break;
            /* Extruder 1 Material */
            case "Extruder 1 Material":
            case "nozzle_1_material":
                mNozzle_1_Material = lineArgs[1];
                break;
            /* Extruder 1 Print Temperature */
            case "Extruder 1 Print Temperature":
            case "nozzle_1_temperature(°C)":
            case "EXTRUDER_TRAIN.1.INITIAL_TEMPERATURE":
                mNozzleTarget_1_Temperature = Float.parseFloat(lineArgs[1]);
                break;
            /* Bed Temperature */
            case "Bed Temperature":
            case "build_plate_temperature(°C)":
            case "BUILD_PLATE.INITIAL_TEMPERATURE":
                mBedTargetTemperature = Float.parseFloat(lineArgs[1]);
                mHeaderChecker.setHeatedBedTempCheck(true);
                break;
            /* Extruder(s) Used */
            case "Extruder(s) Used":
                mExtrudersUsed = Integer.parseInt(lineArgs[1]);
                break;
            /* Work Range - Min X */
            case "Work Range - Min X":
            case "min_x(mm)": // SM2
            case "PRINT.SIZE.MIN.X":
                mModelBoundary.setMinX(Float.parseFloat(lineArgs[1]));
                mHeaderChecker.setBoundaryCheck(true);
                break;
            /* Work Range - Min Y */
            case "Work Range - Min Y":
            case "min_y(mm)":
            case "PRINT.SIZE.MIN.Y":
                mModelBoundary.setMinY(Float.parseFloat(lineArgs[1]));
                mHeaderChecker.setBoundaryCheck(true);
                break;
            /* Work Range - Min Z */
            case "Work Range - Min Z":
            case "min_z(mm)":
            case "PRINT.SIZE.MIN.Z":
                mModelBoundary.setMinZ(Float.parseFloat(lineArgs[1]));
                mHeaderChecker.setBoundaryCheck(true);
                break;
            /* Work Range - Max X */
            case "Work Range - Max X":
            case "max_x(mm)":
            case "PRINT.SIZE.MAX.X":
                mModelBoundary.setMaxX(Float.parseFloat(lineArgs[1]));
                mHeaderChecker.setBoundaryCheck(true);
                break;
            /* Work Range - Max Y */
            case "Work Range - Max Y":
            case "max_y(mm)":
            case "PRINT.SIZE.MAX.Y":
                mModelBoundary.setMaxY(Float.parseFloat(lineArgs[1]));
                mHeaderChecker.setBoundaryCheck(true);
                break;
            /* Work Range - Max Z */
            case "Work Range - Max Z":
            case "max_z(mm)":
            case "PRINT.SIZE.MAX.Z":
                mModelBoundary.setMaxZ(Float.parseFloat(lineArgs[1]));
                mHeaderChecker.setBoundaryCheck(true);
                break;
            /* Thumbnail */
            case "Thumbnail":
            case "thumbnail": // SM2
                try {
                    byte[] bitmapArray = Base64.decode(lineArgs[2].split(",")[1], Base64.DEFAULT);
                    mGcodeThumbnailBytes = bitmapArray.clone();
                    mGcodeThumbnail = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
                } catch (Exception e) {
                    LogHelper.log(e);
                }
                break;
                // legacy header keys from SM2.0 definition.
            case "header_type": {
                switch (lineArgs[1]) {
                    case "3dp":
                        mFileType = FDM;
                        break;
                    case "laser":
                        mFileType = LASER;
                        break;
                    case "cnc":
                        mFileType = CNC;
                        break;
                    default:
                        break;
                }
                break;
            }
            case "tool_head": {
                switch (lineArgs[1]) {
                    case "singleExtruderToolheadForOriginal":
                        break;
                    case "singleExtruderToolheadForSM2":
                        mToolHead = HEAD_3DP;
                        mToolHeadNameID = R.string.all_head_tool_3dp;
                        break;
                    case "dualExtruderToolheadForSM2":
                        mToolHead = HEAD_3DP_DOUBLE_EXTRUDER;
                        mToolHeadNameID = R.string.all_head_tool_double_extruder;
                        break;
                    case "levelOneLaserToolheadForOriginal":
                        break;
                    case "levelTwoLaserToolheadForOriginal":
                        break;
                    case "levelOneLaserToolheadForSM2":
                        mToolHead = HEAD_LASER;
                        mToolHeadNameID = R.string.all_head_tool_laser;
                        break;
                    case "levelTwoLaserToolheadForSM2":
                        mToolHead = HEAD_LASER_10W;
                        mToolHeadNameID = R.string.all_head_tool_laser_10w;
                        break;
                    case "standardCNCToolheadForOriginal":
                        break;
                    case "standardCNCToolheadForSM2":
                        mToolHead = HEAD_CNC;
                        mToolHeadNameID = R.string.all_head_tool_head_cnc;
                        break;
                    case "levelTwoCNCToolheadForSM2":
                        mToolHead = HEAD_CNC_200W;
                        mToolHeadNameID = R.string.all_head_tool_head_cnc_200w;
                        break;
                    default:
                        break;
                }
                break;
            }
            case "min_b(mm)": {
                mModelBoundary.setMinB(Float.parseFloat(lineArgs[1]));
                mHeaderChecker.setBoundaryCheck(true);
                break;
            }
            case "max_b(mm)": {
                mModelBoundary.setMaxB(Float.parseFloat(lineArgs[1]));
                mHeaderChecker.setBoundaryCheck(true);
                break;
            }
            case "spindle_speed(mm/minute)": {
                mSpindleSpeed = Float.parseFloat(lineArgs[1]);
                break;
            }
            case "power(%)": {
                mPower = Float.parseFloat(lineArgs[1]);
                break;
            }
            case "work_speed(mm/minute)": {
                mWorkSpeed = Float.parseFloat(lineArgs[1]);
                break;
            }
            case "jog_speed(mm/minute)": {
                mJogSpeed = Float.parseFloat(lineArgs[1]);
                break;
            }
            case "diameter": {
                mDiameter = Float.parseFloat(lineArgs[1]);
                break;
            }
            case "layer_number": {
                mLayerNumber = Integer.parseInt(lineArgs[1]);
                break;
            }
            case "layer_height": {
                mLayerHeight = Float.parseFloat(lineArgs[1]);
                break;
            }
            case "renderMethod": {
                mRenderMethod = lineArgs[1];
                break;
            }
            case "is_rotate": {
                mIsRotate = lineArgs[1].equals("true") ? 1 : 0;
                break;
            }
            case "work_size_x": {
                mWorkSizeX = Float.parseFloat(lineArgs[1]);
                break;
            }
            case "work_size_y": {
                mWorkSizeY = Float.parseFloat(lineArgs[1]);
                break;
            }
            case "origin": {
                mOrigin = lineArgs[1];
                Context context = ServiceContainer.getInstance().getService(IAppService.class).getAppContext();
                switch (mOrigin) {
                    case "center":
                        mOrigin = context.getString(R.string.work_origin_center);
                        break;
                    case "top-left":
                        mOrigin = context.getString(R.string.work_origin_top_left);
                        break;
                    case "top-right":
                        mOrigin = context.getString(R.string.work_origin_top_right);
                        break;
                    case "bottom-left":
                        mOrigin = context.getString(R.string.work_origin_bottom_left);
                        break;
                    case "bottom-right":
                        mOrigin = context.getString(R.string.work_origin_bottom_right);
                        break;
                    default:
                        break;
                }
                break;
            }
            case "machine": {
                mMachine = lineArgs[1];
                break;
            }
            case "Extruder 0 Retraction Distance": {
                mExtruder0RetractionDistance = Float.parseFloat(lineArgs[1]);
                mFDMRetractionDetected = true;
                mHeaderChecker.setExtruder0RetractionCheck(true);
                break;
            }
            case "Extruder 0 Switch Retraction Distance": {
                mExtruder0SwitchRetractionDistance = Float.parseFloat(lineArgs[1]);
                break;
            }
            case "Extruder 1 Retraction Distance": {
                mExtruder1RetractionDistance = Float.parseFloat(lineArgs[1]);
                mFDMRetractionDetected = true;
                mHeaderChecker.setExtruder1RetractionCheck(true);
                break;
            }
            case "Extruder 1 Switch Retraction Distance": {
                mExtruder1SwitchRetractionDistance = Float.parseFloat(lineArgs[1]);
                break;
            }
            default:
                break;
        }
    }

    /**
     * Parse G0 and G1 command from lineArgs.
     * <p>
     * Examples:
     * G1 F1500
     * G1 X90.6 Y13.8 E22.4 F3000
     * G1 X80 Y20 E36 F1500
     * G0 F2400 X49.071 Y22.466 E0.43903
     */
    private void parseG0G1() throws NumberFormatException {
        float x = Position.INITIAL_X, y = Position.INITIAL_Y, z = Position.INITIAL_Z, e, feedRate;
        if (mCurrentPosition != null) {
            x = mCurrentPosition.x;
            y = mCurrentPosition.y;
            z = mCurrentPosition.z;
        }

        for (int i = 1; i < lineArgCount; i++) {
            switch (lineArgs[i].charAt(0)) {
                case 'X': {
                    x = Float.parseFloat(lineArgs[i].substring(1));
                    break;
                }
                case 'Y': {
                    y = Float.parseFloat(lineArgs[i].substring(1));
                    break;
                }
                case 'Z': {
                    z = Float.parseFloat(lineArgs[i].substring(1));
                    break;
                }
                case 'E': {
                    if (mShouldUpdateAttribute) {
                        e = Float.parseFloat(lineArgs[i].substring(1));
                        extrusionAmount = isAbsoluteCoordinate ? (e) : (extrusionAmount + e);
                        if (!mHeaderChecker.isExtruder0RetractionCheck() || !mHeaderChecker.isExtruder1RetractionCheck()) {
                            calculateRetraction(e);
                        }
                    }
                    break;
                }
                case 'F': {
                    if (mShouldUpdateAttribute) {
                        feedRate = Float.parseFloat(lineArgs[i].substring(1));
                        if (feedRate != 0 && feedRate != mCurrentLineFeedRate) {
                            mCurrentLineFeedRate = feedRate;
                        }
                    }
                    break;
                }
            }
        }

        boolean pointMoved = (mCurrentPosition == null || (x != mCurrentPosition.x || y != mCurrentPosition.y || z != mCurrentPosition.z));
        if (!pointMoved) {
            return;
        }

        Position position = new Position(x, y, z);

        if (lineArgs[0].equals("G0")) {
            checkPathClose();

            // Calculate ETA
            if (mCurrentLineFeedRate != 0 && mCurrentPosition != null && !mHeaderChecker.isEstimatedTimeCheck()) {
                final float segmentTime = mCurrentPosition.distanceTo(position) / mCurrentLineFeedRate * 60;
                // FIXME: Workaround here. The estimated time will multiply 1.6 constant as tricks.
                mEstimateTime += (segmentTime * 100 / 60f);
            }

            // Update current position
            mCurrentPosition = position;

//            mModelBoundary.updateBoundary(position);
        } else if (lineArgs[0].equals("G1")) {
            checkPathStart();

            if (mCurrentLineFeedRate != 0 && mCurrentPosition != null && !mHeaderChecker.isEstimatedTimeCheck()) {
                final float segmentTime = mCurrentPosition.distanceTo(position) / mCurrentLineFeedRate * 60;
                // FIXME: Workaround here. The estimated time will multiply 1.6 constant as tricks.
                mEstimateTime += (segmentTime * 100 / 60f);

                // Calculate average work speed FeedRate
                mFeedRateAmount += mCurrentLineFeedRate;
                mFeedRateCount++;
            }

            mCurrentPosition = position;

//            mToolPath.add(position);
            if (!mHeaderChecker.isBoundaryCheck()) {
                mModelBoundary.updateBoundary(position);
            }
        }
    }

    private void calculateRetraction(float ePosition) {
        // Absolute mode
        float delta = isAbsoluteCoordinate ? ePosition - mLastEAxisPosition : ePosition;
        if (delta < 0 && delta > -8.0f) {
//            Logger.d("retraction detected T%d %.4f", mCurrentExtruder, delta);
//            Logger.d("gcode %s", Arrays.toString(lineArgs));
            if (mCurrentExtruder == 0) {
                if (mT0RetractionCount == 0) {
                    mExtruder0RetractionDistance = -delta;
                }

                if (mExtruder0RetractionDistance != -delta) {
                    mT0RetractionCount = 1;
                }

                if (mT0RetractionCount > 3 && mExtruder0RetractionDistance > -delta) {
                    mExtruder0RetractionDistance = -delta;
                }
                mT0RetractionCount++;
            } else {
                if (mT1RetractionCount == 0) {
                    mExtruder1RetractionDistance = -delta;
                }

                if (mExtruder1RetractionDistance != -delta) {
                    mT1RetractionCount = 1;
                }

                if (mT1RetractionCount > 3 && mExtruder1RetractionDistance > -delta) {
                    mExtruder1RetractionDistance = -delta;
                }
                mT1RetractionCount++;
            }
//            Logger.d("T0 retracted %.2f at %d times, T1 retracted %.2f at %d times",
//                    mExtruder0RetractionDistance, mT0RetractionCount,
//                    mExtruder1RetractionDistance, mT1RetractionCount);
        }
        mLastEAxisPosition = ePosition;
    }

    /**
     * G4
     */
    private void parseG4() throws NumberFormatException {
        for (int i = 1; i < lineArgCount; i++) {
            switch (lineArgs[i].charAt(0)) {
                case 'S': {
                    final float dwellTime = Float.parseFloat(lineArgs[i].substring(1));
                    mEstimateTime += dwellTime;
                    return;
                }
                case 'P': {
                    final float dwellTime = Float.parseFloat(lineArgs[i].substring(1));
                    mEstimateTime += dwellTime * 0.001;
                    return;
                }
            }
        }
    }

    private void parseG28() {
        // TODO: implement an offset for absolute position
        if (lineArgCount < 2) {
            checkPathClose();
            mCurrentPosition = new Position(0, 0, 0);
        } else {
            float x = mCurrentPosition.x;
            float y = mCurrentPosition.y;
            float z = mCurrentPosition.z;

            for (int i = 1; i < lineArgCount; i++) {
                switch (lineArgs[i].charAt(0)) {
                    case 'X': {
                        x = 0;
                        break;
                    }
                    case 'Y': {
                        y = 0;
                        break;
                    }
                    case 'Z': {
                        z = 0;
                        break;
                    }
                }
            }

            checkPathClose();
            mCurrentPosition = new Position(x, y, z);
        }
    }

    /**
     * G92
     */
    private void parseG92() {
        // TODO: implement an offset for absolute position
        for (int i = 1; i < lineArgCount; i++) {
            if (lineArgs[i].charAt(0) == 'E') {
                mLastEAxisPosition = Float.parseFloat(lineArgs[i].substring(1));
            }
        }
    }

    private void parseM3M5() throws NumberFormatException {
        for (int i = 1; i < lineArgCount; i++) {
            switch (lineArgs[i].charAt(0)) {
                case 'S': {
                    mPower = Float.valueOf(lineArgs[i].substring(1)) / 255 * 100;
                    break;
                }
                case 'P': {
                    mPower = Float.valueOf(lineArgs[i].substring(1));
                    break;
                }
            }
        }
    }

    private void parseHeatedBedTemperature() throws NumberFormatException {
        Logger.d("parsing bed temp...");
        for (int i = 1; i < lineArgCount; i++) {
            if (lineArgs[i].charAt(0) == 'S') {
                float temperature = Float.valueOf(lineArgs[i].substring(1));
                Logger.d("mTarget: %1$f, parsed: %2$f", mBedTargetTemperature, temperature);
                if (mBedTargetTemperature < temperature) {
                    mBedTargetTemperature = temperature;
                }
            }
        }
    }

    private void parseNozzleTemperature() throws NumberFormatException {
        for (int i = 1; i < lineArgCount; i++) {
            if (lineArgs[i].charAt(0) == 'S') {
                float temperature = Float.valueOf(lineArgs[i].substring(1));
                if (mNozzleTarget_0_Temperature < temperature) {
                    mNozzleTarget_0_Temperature = temperature;
                }
            }
        }
    }

    private void parseM605() throws NumberFormatException {
        for (int i = 1; i < lineArgCount; i++) {
            switch (lineArgs[i].charAt(0)) {
                case 'S': {
                    mPrintMode = Integer.parseInt(lineArgs[i].substring(1));
                }
            }
        }
    }

    private boolean containsEndGcodeMark(String line) {
        return line.contains(BOUNDS_END_MARK) || line.contains(BOUNDS_END_MARK_V1);
    }

    public ArrayList<Position> getToolPath() {
        return mToolPath;
    }

    @Override
    public IMachine.WorkType getFileType() {
        return mFileType;
    }

    @Override
    public Bitmap getGcodeThumbnail() {
        return mGcodeThumbnail;
    }

    public byte[] getGcodeThumbnailBytes() {
        return mGcodeThumbnailBytes;
    }

    @Override
    public int getTotalLinesCount() {
        return mTotalLinesCount;
    }

    @Override
    public float getBedTargetTemperature() {
        return mBedTargetTemperature;
    }

    @Override
    public float getNozzleTargetTemperature() {
        return mNozzleTarget_0_Temperature;
    }

    @Override
    public float getPower() {
        return mPower;
    }

//    @Override
//    public float getCNCPower() {
//        return mCNCPower;
//    }

    @Override
    public float getWorkSpeed() {
        if (mWorkSpeed != 0) {
            return mWorkSpeed;
        } else {
            if (mFeedRateCount == 0) return 0;

            return (float) mFeedRateAmount / mFeedRateCount;
        }
    }

    @Override
    public float getJogSpeed() {
        return mJogSpeed;
    }

    @Override
    public float getDiameter() {
        return mDiameter;
    }

    @Override
    public float getSpindleSpeed() {
        return mSpindleSpeed;
    }

    public float getAverageFeedRate() {
        if (mFeedRateCount == 0) return 0;

        return (float) mFeedRateAmount / mFeedRateCount;
    }

    @Override
    public float getEstimatedTime() {
        return mEstimateTime;
    }

    @Override
    public ModelBoundary getBoundary() {
        return mModelBoundary;
    }

    @Override
    public Observable<Integer> getParseProgressObservable() {
        return mParseProgressSubject;
    }

    @Override
    public void destroy() {
        if (mParseWorker != null) {
            mParseWorker.dispose();
            mParseWorker = null;
        }

        if (mToolPath != null) {
            mToolPath.clear();
        }
    }

    public int getHeaderType() {
        return mToolHead;
    }

    @Override
    public int getHeaderNameID() {
        return mToolHeadNameID;
    }

    public float getNozzle_0_Diameter() {
        return mNozzle_0_Diameter;
    }

    public float getNozzle_1_Diameter() {
        return mNozzle_1_Diameter;
    }

    public int getLayerNumber() {
        return mLayerNumber;
    }

    public float getLayerHeight() {
        return mLayerHeight;
    }

    public float getMaterialWeight() {
        return mMaterialWeight;
    }

    public float getMaterialLength() {
        return mMaterialLength;
    }

    public String getMaterial_0() {
        return mNozzle_0_Material;
    }

    public String getMaterial_1() {
        return mNozzle_1_Material;
    }

    public String getRenderMethod() {
        return mRenderMethod;
    }

    public int isIsRotate() {
        return mIsRotate;
    }

    public float getWorkSizeX() {
        return mWorkSizeX;
    }

    public float getWorkSizeY() {
        return mWorkSizeY;
    }

    public String getOrigin() {
        return mOrigin;
    }

    public float getNozzleTarget_1_Temperature() {
        return mNozzleTarget_1_Temperature;
    }

    @Override
    public float getExtruder0RetractionDistance() {
        return mExtruder0RetractionDistance;
    }

    @Override
    public float getExtruder1RetractionDistance() {
        return mExtruder1RetractionDistance;
    }

    @Override
    public float getExtruder0SwitchRetractionDistance() {
        return mExtruder0SwitchRetractionDistance;
    }

    @Override
    public float getExtruder1SwitchRetractionDistance() {
        return mExtruder1SwitchRetractionDistance;
    }

    @Override
    public int getPrintMode() {
        return mPrintMode;
    }

    @Override
    public boolean isApplyMultiExtruder() {
        return mExtrudersUsed > 1 || (mIsDefineT0 && mIsDefineT1);
    }
}
