package fabscreen.features.machinetools.calibration.a400platform.laser.w_10.cameraCalibration;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.platform.base.helper.GsonHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.FabLocalFile;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.parser.IGcodeParser;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.lib.print.PrintListener;
import fabscreen.platform.base.model.ILaserCameraController;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.controller.PrintController;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class A400CameraCalibration10w2Fragment extends A400CalibrationBaseFragment {
    @BindView(R2.id.top_bar_back)
    Button mBtnBack;
    public static final int STATUS_IDLE = 0;
    public static final int STATUS_PRINTSUCCESS = 1;
    public static final int STATUS_COMPLETE = 2;
    public static final int STATUS_ERROR = 3;
    private static final int STATUS_COMPLETED = 3;
    private IPrintWorkspace mWorkspace;
    private PrintController mPrintController;
    private IGcodeParser mParser;
    private IMachine mA400Machine;
    private DecisionDialog exitDialog;
    private float bottomZ;
    private ILaserCameraController laserCameraController;
    int mIndex = 0;
    private ArrayList<Point> mCorners = new ArrayList<>();
    private BehaviorSubject<Integer> mCalibrationStatusSubject = BehaviorSubject.createDefault(STATUS_IDLE);


    public static Fragment newInstance() {
        return new A400CameraCalibration10w2Fragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWorkspace = ServiceContainer.getInstance().getService(IPrintWorkspace.class);
        mA400Machine = ServiceContainer.getInstance().getService(IMachine.class);
        mPrintController = mA400Machine.getPrintController();
        laserCameraController = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController();
        mParser = ServiceContainer.getInstance().getService(IGcodeParser.class);

        exitDialog = DecisionDialog.create(getContext())
                .setContent(getString(R.string.assistant_back_notice, getString(R.string.calibration_camera_calibration_10w_2_title)))
                .setType(DecisionDialog.WARMING_TYPE)
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, false, true)
                .setPic(R.drawable.pic_a400_warning_112x112)
                .setFirstTv(getContext().getResources().getString(R.string.all_cancel), R.color.select_dialog_white_txt, ((dialog, which) -> {
                    dialog.dismiss();
                }))
                .setSecondTv(requireContext().getString(R.string.all_yes), R.color.select_dialog_orange_txt, ((dialog, which) -> {
                    fabBackConfirm.mCancelBtn.setEnabled(false);
                    fabBackConfirm.mSecondBtn.setEnabled(false);
                    mPrintController.stop();
                }));
        initView();
        initPoint();
        setAutoWhiteBalance(false);
        initPosition();

    }

    private void initPoint() {
        float x = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getX() / 2;
        float y = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getY() / 2;
        mCorners.add(new Point((int) (x - 50), (int) (y + 50)));
        mCorners.add(new Point((int) (x + 50), (int) (y + 50)));
        mCorners.add(new Point((int) (x + 50), (int) (y - 50)));
        mCorners.add(new Point((int) (x - 50), (int) (y - 50)));
    }

    private void initPosition() {
        bottomZ = mA400Machine.getLaserController().getLaserToolhead().getLaserToolHeadInfoValue().getLaserFocalLength() + mA400Machine.getLaserController().getLaserToolhead().getLaserToolHeadInfoValue().getPlatformHeight();
        Vector vector = new Vector();
        vector.setX(mA400Machine.getMachineInfoSubjectHolder().getValue().size.getX() / 2);
        vector.setY(mA400Machine.getMachineInfoSubjectHolder().getValue().size.getY() / 2);
        vector.setZ(bottomZ);
        mA400Machine.getMachineController()
                .gotoAbsolutePosition(vector)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> initFile());
    }

    private void initView() {
        mBtnBack.setEnabled(false);
        setTitle(R.string.calibration_camera_calibration_10w_2_title);
        mTvTopBarContent.setText(R.string.calibration_camera_calibration_10w_2_content);
        mGuideProgressBar.setMax(2);
        mGuideProgressBar.setProgress(2);

        mCalibrationStatusSubject
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(integer -> {
                    switch (integer) {
                        case STATUS_ERROR:
                            Toast.makeText(getContext(), "校准出错", Toast.LENGTH_LONG).show();
                            getServiceContainer().getService(IMachine.class).getLaserController()
                                    .exitCalibration(true)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .as(bindToLifecycle())
                                    .subscribe(response -> {
                                        if (response.isSuccess()) {
                                            finishActivityWithResultOk();
                                        }
                                    });
                            break;
                        case STATUS_PRINTSUCCESS:
                            toDoTakePhoto();
                            break;
                        case STATUS_COMPLETE:
                            getServiceContainer().getService(IMachine.class).getLaserController()
                                    .exitCalibration(true)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .as(bindToLifecycle())
                                    .subscribe(response -> {
                                        if (response.isSuccess()) {
                                            finishActivityWithResultOk();
                                        }
                                    });
                            break;
                        default:
                            break;
                    }
                });
    }

    private void initFile() {
        File printFile = copyPrintFile(R.raw.a400_laser_camera_calibration);
        IFile selectFile = new FabLocalFile(printFile);
        mParser.startParse(selectFile, IMachine.WorkType.LASER);
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

    private File copyPrintFile(@RawRes int fileRawId) {
        InputStream is = getResources().openRawResource(fileRawId);
        File file = null;
        try {
            file = new File(getContext().getCacheDir().getAbsoluteFile() + "/calibrationXY.gcode");
            if (file.exists()) {
                file.delete();
            }
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                int read;
                byte[] bytes = new byte[1024];
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

    void handleResult(IFile file) {
        mWorkspace.setPrintMode(IPrintWorkspace.PRINT_MODE_NORMAL);
        mWorkspace.setPrintSource(0);
        mWorkspace.setFileTotalLineCount(mParser.getTotalLinesCount());
        mWorkspace.setEstimatedTime(mParser.getEstimatedTime());
        mWorkspace.setFileMD5Value("c319528c5c360d46031b69d39e01ceb3");
        mWorkspace.addFileToWorkspace(file).observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe(success -> {
            if (success) {
                initPrint();
            }
        }, LogHelper::log);
    }

    void initPrint() {
        AndroidSchedulers.mainThread().scheduleDirect(this::startPrint, 300, TimeUnit.MILLISECONDS);

        mA400Machine
                .getMachineStatusSubjectHolder()
                .getObservable()
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(machineStatus -> {
                    Logger.d("---FDT--- Machine work status " + machineStatus.status);
                }, LogHelper::log);

        mPrintController
                .getPrintStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(status -> {
                    Logger.d("screen print state " + status);
                    if (status == STATUS_COMPLETED) {
                    }
                }, LogHelper::log);


    }

    private void startPrint() {
        IFile file1 = mWorkspace.getPrintFile();
        mPrintController.reset();
        mPrintController.setFile(file1);
        mPrintController.setTotalLines(mWorkspace.getFileTotalLineCount());
        setPrintControllerListener(mPrintController);
        mPrintController.start();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_calibration_camrea_doing;
    }

    private void setPrintControllerListener(PrintController mPrintController) {
        mPrintController.setListener(new PrintListener() {
            @Override
            public void onStartSuccess() {
                // oh we started
                Logger.i("Print started.");
                mPrintController.getTickCounter().reset();
                mPrintController.getTickCounter().start();
            }

            @Override
            public void onStartFailed(int retCode) {
                DecisionDialog decisionDialog = DecisionDialog.create(getContext())
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, false)
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setFirstTv(R.string.a400_print_got_it, R.color.select_dialog_blue_txt, ((dialog, which) -> {
                            dialog.dismiss();
                            ServiceContainer.getInstance().getService(IRouter.class).routeToHome().start(requireContext());
                        }));
                switch (retCode) {
                    case 202: {
                        Logger.w("Filament used out, unable to start printing.");
                        break;
                    }
                    case 203: {
                        Logger.d("Unable to start printing, enclosure door open detected.");
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
                mPrintController.getTickCounter().stop();
            }

            @Override
            public void onPauseFailed(int retCode) {
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
            }

            @Override
            public void onResumeFailed(int retCode) {
                DecisionDialog decisionDialog = DecisionDialog.create(getContext())
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, false)
                        .setType(DecisionDialog.WARMING_TYPE)
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
                Logger.i("Print recovered.");
            }

            @Override
            public void onResumeFromPowerOutageFailed(int retCode) {
                DecisionDialog decisionDialog = DecisionDialog.create(getContext())
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, true, false)
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setFirstTv(R.string.a400_print_got_it, R.color.select_dialog_blue_txt, ((dialog, which) -> {
                            dialog.dismiss();
                            ServiceContainer.getInstance().getService(IMachine.class).getPrintController().setPowerOutageFlag(false);
                            ServiceContainer.getInstance().getService(IRouter.class).routeToHome().startAndClear(requireContext());
                        }));
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
                Logger.i("print stopped.");
                mPrintController.getTickCounter().stop();
                ServiceContainer.getInstance().getService(IRouter.class).routeToHome().start(getContext());
                mA400Machine.getLaserController().exitCalibration(false)
                        .observeOn(AndroidSchedulers.mainThread())
                        .as(bindToLifecycle())
                        .subscribe(success -> {
                            if (exitDialog != null && exitDialog.isShowing()) {
                                exitDialog.dismiss();
                            }
                            if (success.isSuccess()) {
                                requireActivity().finish();
                            }
                        });
            }

            @Override
            public void onStopFailed(int retCode) {
                Logger.w("Unable to stop printing, ret code %d", retCode);
                DecisionDialog.create(requireContext())
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
                        .setContent(R.string.print_warning_stop_unable)
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setFirstTv(R.string.all_confirm, R.color.select_dialog_white_txt, (dialog, which) -> {
                            dialog.dismiss();
                            mA400Machine.getLaserController().exitCalibration(false)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .as(bindToLifecycle())
                                    .subscribe(success -> {
                                        if (success.isSuccess()) {
                                            exitDialog.dismiss();
                                            requireActivity().finish();
                                        }
                                    });
                        })
                        .show();
            }

            @Override
            public void onFinishSuccess() {
                Logger.i("Print Finished.");
                mPrintController.getTickCounter().stop();
                mCalibrationStatusSubject.onNext(STATUS_PRINTSUCCESS);
            }

            @Override
            public void onFinishFailed(int retCode) {
                Logger.w("Unable to finish printing, ret code %d", retCode);
                mA400Machine.getLaserController().exitCalibration(false)
                        .observeOn(AndroidSchedulers.mainThread())
                        .as(bindToLifecycle())
                        .subscribe(success -> {
                            if (success.isSuccess()) {
                                exitDialog.dismiss();
                                requireActivity().finish();
                            }
                        });
                DecisionDialog.create(requireContext())
                        .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
                        .setType(DecisionDialog.WARMING_TYPE)
                        .setContent(R.string.print_warning_finish_unable)
                        .setFirstTv(R.string.all_confirm, R.color.select_dialog_white_txt, (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            }
        });
    }

    private void toDoTakePhoto() {
        String cameraCalibrationTakePhotoVector = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getCameraCalibrationTakePhotoVector();
        Vector vectorByModuleID = new GsonHelper().getVectorByModuleID(cameraCalibrationTakePhotoVector,
                mA400Machine.getMachineInfoSubjectHolder().getValue().modelId,
                mA400Machine.getLaserController().getHeadType());

        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().gotoAbsolutePosition(vectorByModuleID, 1500)
                .flatMap(success -> (mA400Machine.getLaserController().getHeadType() == Module.ModuleType.HEAD_LASER_10W) ?
                        ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().setPhotoQuality(10) :
                        Observable.just(success)
                )
                .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().requestCapturePhoto())
                .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().watchPhotoReceive())
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnNext(bitmap -> setAutoWhiteBalance(true))
                .subscribeOn(Schedulers.computation())
                .as(bindToLifecycle())
                .subscribe(bitmap -> {
                    Logger.d("Capture image succeed.");
                    String path = ServiceContainer.getInstance().getService(IAppService.class).getCacheDir() + "/10WLaserCalibration.jpg";

                    Matrix m = new Matrix();
                    m.postRotate(90);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);

                    FileOutputStream out = new FileOutputStream(path);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

                    process(bitmap);
                    mCalibrationStatusSubject.onNext(STATUS_COMPLETE);
                }, e -> {
                    Logger.w("Capture image failed.");
                    mCalibrationStatusSubject.onNext(STATUS_ERROR);
                });
    }

    @Override
    protected void back() {
        if (exitDialog.isShowing()) {
            return;
        }
        exitDialog.show();
    }

    private void setAutoWhiteBalance(boolean enabled) {
        laserCameraController
                .setCameraAutoWhiteBalance(enabled)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    Log.d("DEBUG", "camera auto white balance " + enabled + success);
                }, LogHelper::log);
    }

    private void turnLight(boolean isOpen) {
        laserCameraController.setCameraLighting(isOpen)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    // do nothing
                }, LogHelper::log);
    }

    private Bitmap getGreyscaleImage(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();

        Bitmap greyscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(greyscale);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorFilter);
        canvas.drawBitmap(image, 0, 0, paint);

        return greyscale;
    }

    private void process(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();
        Bitmap greyscale = getGreyscaleImage(image);

        Equation top = getLine(greyscale, width * 2 / 5, height / 3, width / 5, height / 8
        );

        Equation bottom = getLine(greyscale, width * 2 / 5, height * 4 / 7, width / 5, height / 8);

        Equation left = getLine(greyscale, width * 3 / 10, height * 6 / 15, width / 7, height / 5);

        Equation right = getLine(greyscale, width * 3 / 5, height * 6 / 15, width / 7, height / 5);

        for (int x = 0; x < width; x++) {
            int y = Math.round(top.m * x + top.c);
            if (0 <= y && y < height) {
                image.setPixel(x, y, Color.GREEN);
            }
        }
        for (int x = 0; x < width; x++) {
            int y = Math.round(bottom.m * x + bottom.c);
            if (0 <= y && y < height) {
                image.setPixel(x, y, Color.GREEN);
            }
        }
        for (int y = 0; y < height; y++) {
            int x = Math.round((y - left.c) / left.m);
            if (0 <= x && x < width) {
                image.setPixel(x, y, Color.GREEN);
            }
        }
        for (int y = 0; y < height; y++) {
            int x = Math.round((y - right.c) / right.m);
            if (0 <= x && x < width) {
                image.setPixel(x, y, Color.GREEN);
            }
        }

        ArrayList<Point> points = new ArrayList<>();

        points.add(getCross(top, left));
        points.add(getCross(top, right));
        points.add(getCross(bottom, right));
        points.add(getCross(bottom, left));

        Log.d("DEBUG", "points " + points);
        Log.d("DEBUG", "mCorners " + mCorners);

        // save result into a JSONObject
        JSONObject result = new JSONObject();
        JSONArray jsonPoints = new JSONArray();
        JSONArray jsonCorners = new JSONArray();
        try {
            for (Point p : points) {
                JSONObject point = new JSONObject();
                point.put("x", p.x);
                point.put("y", p.y);
                jsonPoints.put(point);
            }

            for (Point c : mCorners) {
                JSONObject point = new JSONObject();
                point.put("x", c.x);
                point.put("y", c.y);
                jsonCorners.put(point);
            }

            result.put("points", jsonPoints);
            result.put("corners", jsonCorners);
        } catch (JSONException e) {
            LogHelper.log(e);
        }

        Log.d("DEBUG", "JSON \n " + result.toString());

        // save result in preferences
        Logger.d("Camera calibration result : %s", result.toString());
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().set10WLaserCameraCalibration(result.toString());
    }

    class Equation {
        float m;
        float c;

        Equation(float gradient, float intercept) {
            this.m = gradient;
            this.c = intercept;
        }
    }

    private Equation getLine(Bitmap image, int x0, int y0, int width, int height) {
        // crop
        Bitmap cropped = Bitmap.createBitmap(image, x0, y0, width, height);
        saveJpg(cropped, "laser_camera_calibration_" + mIndex++ + ".jpg");

        // binarization
        normalize(cropped);
        binarization(cropped);

        boolean useYRegression = (width < height);

        ArrayList<Point> points = new ArrayList<>();

        long[] sum = new long[4];
        Equation eq;
        while (true) {
            points.clear();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int v = cropped.getPixel(x, y) & 0xff;
                    if (v == 0) {
                        if (useYRegression) {
                            points.add(new Point(y, x));
                        } else {
                            points.add(new Point(x, y));
                        }
                    }
                }
            }

            eq = regression(points);

            // calculate distance
            float maxDistance = 0;
            for (Point point : points) {
                float y = point.x * eq.m + eq.c;
                float dist = Math.abs(y - point.y);
                maxDistance = Math.max(maxDistance, dist);
            }

            if (maxDistance < 1) {
                break;
            }

            for (Point point : points) {
                float y = point.x * eq.m + eq.c;
                float dist = Math.abs(y - point.y);
                if (dist > maxDistance * 0.8) {
                    if (useYRegression) {
                        cropped.setPixel(point.y, point.x, Color.WHITE);
                    } else {
                        cropped.setPixel(point.x, point.y, Color.WHITE);
                    }
                }
            }
        }

        if (useYRegression) {
            eq.m = 1 / eq.m;
            eq.c = -(x0 + eq.c) * eq.m + y0;
        } else {
            eq.c = eq.c - eq.m * x0 + y0;
        }

        return eq;
    }

    private void normalize(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();

        long total = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int v = image.getPixel(x, y) & 0xff;
                total += v;
            }
        }
        double avg = total / (width * height);

        // divide image into cells and normalize the cell use image average color.
        final int cellSize = 30;
        for (int i = 0; i < width; i += cellSize) {
            for (int j = 0; j < height; j += cellSize) {
                int maxX = Math.min(i + cellSize, width);
                int maxY = Math.min(j + cellSize, height);

                total = 0;
                for (int x = i; x < maxX; x++) {
                    for (int y = j; y < maxY; y++) {
                        int v = image.getPixel(x, y) & 0xff;
                        total += v;
                    }
                }

                double cellAvg = total / ((maxX - i) * (maxY - j));
                int diff = (int) Math.round((cellAvg - avg) * 0.8);

                for (int x = i; x < maxX; x++) {
                    for (int y = j; y < maxY; y++) {
                        int v = image.getPixel(x, y) & 0xff;
                        int newValue = v - diff;
                        image.setPixel(x, y, 0xff000000 | (0x010101 * newValue));
                    }
                }
            }
        }

        // classic normalize
        int min = 255;
        int max = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int v = image.getPixel(x, y) & 0xff;

                min = Math.min(min, v);
                max = Math.max(max, v);
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int v = image.getPixel(x, y) & 0xff;
                int newValue = (int) Math.round(255.0 * (v - min) / (max - min));
                image.setPixel(x, y, 0xff000000 | (0x010101 * newValue));
            }
        }
    }

    private Point getCross(Equation a, Equation b) {
        float x = -(a.c - b.c) / (a.m - b.m);
        float y = a.m * x + a.c;
        return new Point((int) x, (int) y);
    }

    private void saveJpg(Bitmap cropped, String name) {
        try {
            File file = new File(ServiceContainer.getInstance().getService(IAppService.class).getFilesDir(), name);
            FileOutputStream out = new FileOutputStream(file);
            cropped.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            Logger.d("saveJpg: " + e);
        }
    }

    private void binarization(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int v = image.getPixel(x, y) & 0xff;

                if (v >= 128) {
                    image.setPixel(x, y, Color.WHITE);
                } else {
                    image.setPixel(x, y, Color.BLACK);
                }
            }
        }
    }

    private float round(float number) {
        int factor = 1;
        for (int i = 0; i < 4; i++) {
            factor *= 10;
        }
        return 1.0f * Math.round(number * factor) / factor;
    }

    private Equation regression(ArrayList<Point> points) {
        long[] sum = new long[4];

        for (int i = 0; i < 4; i++) {
            sum[i] = 0;
        }

        for (Point point : points) {
            sum[0] += point.x;
            sum[1] += point.y;
            sum[2] += point.x * point.x;
            sum[3] += point.x * point.y;
        }

        long size = points.size();
        long run = size * sum[2] - sum[0] * sum[0];
        long rise = size * sum[3] - sum[0] * sum[1];
        float gradient = run == 0 ? 0 : round(1.0f * rise / run);
        float intercept = round(1.0f * sum[1] / size - gradient * sum[0] / size);

        return new Equation(gradient, intercept);
    }
}
