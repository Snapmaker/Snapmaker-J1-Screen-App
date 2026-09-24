package fabscreen.platform.base.legacy.server.http.handlers;

import static fabscreen.platform.base.legacy.connection.MockConst.CAMERA_HEIGHT_OFFSET;
import static fabscreen.platform.base.legacy.connection.MockConst.H1_Z_POSITION;
import static fabscreen.platform.base.legacy.connection.MockConst.H2_Z_POSITION;

import android.graphics.Bitmap;

import com.orhanobut.logger.Logger;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.framework.body.FileBody;
import com.yanzhenjie.andserver.framework.body.JsonBody;
import com.yanzhenjie.andserver.framework.body.StringBody;
import com.yanzhenjie.andserver.http.HttpRequest;
import com.yanzhenjie.andserver.http.HttpResponse;
import com.yanzhenjie.andserver.http.ResponseBody;
import com.yanzhenjie.andserver.http.multipart.MultipartFile;
import com.yanzhenjie.andserver.util.StatusCode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import fabscreen.platform.base.Constants;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.MockConst;
import fabscreen.platform.base.legacy.connection.print.DeprecatedPrintController;
import fabscreen.platform.base.lib.file.FabLocalFile;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.parser.GcodeParser;
import fabscreen.platform.base.lib.parser.IGcodeParser;
import fabscreen.platform.base.model.HTTPEventBus;
import fabscreen.platform.base.model.system.DeprecatedMachineInfo;
import fabscreen.platform.base.model.system.MachineStatusManager;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

@Deprecated
@RestController
class MainRequestHandler extends BaseRequestHandler {
    private static final String URI_STATUS = "/api/v1/status";

    private static final String URI_EXECUTE_CODE = "/api/v1/execute_code";

    // file
    private static final String URI_UPLOAD_FILE = "/api/v1/upload";
    private static final String URI_PRINT_FILE = "/api/v1/print_file";

    // print
    private static final String URI_PREPARE_PRINT = "/api/v1/prepare_print";
    private static final String URI_START_PRINT = "/api/v1/start_print";
    private static final String URI_PAUSE_PRINT = "/api/v1/pause_print";
    private static final String URI_RESUME_PRINT = "/api/v1/resume_print";
    private static final String URI_STOP_PRINT = "/api/v1/stop_print";

    // filament load/unload
    private static final String URI_FILAMENT_UNLOAD = "/api/v1/filament_unload";
    private static final String URI_FILAMENT_LOAD = "/api/v1/filament_load";

    // override parameters
    private static final String URI_OVERRIDE_NOZZLE = "/api/v1/override_nozzle_temperature";
    private static final String URI_OVERRIDE_HEATED_BED = "/api/v1/override_bed_temperature";
    private static final String URI_OVERRIDE_Z_OFFSET = "/api/v1/override_z_offset";
    private static final String URI_OVERRIDE_WORK_SPEED = "/api/v1/override_work_speed";
    private static final String URI_OVERRIDE_LASER_POWER = "/api/v1/override_laser_power";

    // enclosure
    private static final String URI_ENCLOSURE = "/api/v1/enclosure";

    // air purifier
    private static final String URI_AIR_PURIFIER_SWITCH = "/api/v1/air_purifier_switch";
    private static final String URI_AIR_PURIFIER_FAN_SPEED = "/api/v1/air_purifier_fan_speed";

    // IQC test api
    private static final String URI_IQC_CHECK_ALIVE = "/api/v1/iqc_check_alive";
    private static final String URI_IQC_UPLOAD_TEST = "/api/v1/iqc_upload_test";

    private static final String URI_LASER_MATERIAL_THICKNESS = "/api/request_Laser_Material_Thickness";

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    /**
     * API for getting status of the machine. This API is designed to be called every second.
     */
    @GetMapping(path = URI_STATUS)
    void getStatus(HttpRequest request, HttpResponse response) {
        if (!ensureConnection(request, response)) return;

        final DeprecatedMachineInfo status = MachineStatusManager.getMachineInfoHolder().getValue();

        JSONObject data = new JSONObject();
        try {
            if (status.printerStatus == 0) {
                data.put("status", "IDLE");
            } else if (status.printerStatus == 1 || status.printerStatus == 3) {
                data.put("status", "RUNNING");
            } else {
                data.put("status", "PAUSED");
            }

            data.put("x", status.x);
            data.put("y", status.y);
            data.put("z", status.z);
            if (ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isRotaryAvailable) {
                data.put("b", status.b);
            }

            data.put("homed", ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().isHomed);
            data.put("offsetX", ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getX());
            data.put("offsetY", ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getY());
            data.put("offsetZ", ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getZ());

            // toolHead
            // TODO: need refactor
            int headType = 0; // ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType;
            String toolHeadKey;
            switch (headType) {
                case Module.ModuleType.HEAD_3DP:
                    toolHeadKey = ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().KEY_TOOL_HEAD_3DP_1;
                    break;
                case Module.ModuleType.HEAD_CNC:
                    toolHeadKey = ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().KEY_TOOL_HEAD_CNC_1;
                    break;
                case Module.ModuleType.HEAD_LASER:
                    toolHeadKey = ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().KEY_TOOL_HEAD_LASER_1;
                    break;
                case Module.ModuleType.HEAD_LASER_10W:
                    toolHeadKey = ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().KEY_TOOL_HEAD_LASER_2;
                    break;
                case Module.ModuleType.HEAD_UNPLUGGED:
                default:
                    toolHeadKey = "unplugged";
                    break;
            }
            data.put("toolHead", toolHeadKey);

            switch (headType) {
                case Module.ModuleType.HEAD_3DP:
                    data.put("nozzleTemperature", status.leftNozzleTemperature);
                    data.put("nozzleTargetTemperature", status.leftNozzleTargetTemperature);
                    data.put("heatedBedTemperature", status.bedTemperature);
                    data.put("heatedBedTargetTemperature", status.bedTargetTemperature);

                    boolean isFilamentOut = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().isFilamentOut();
                    data.put("isFilamentOut", isFilamentOut);
                    break;
                case Module.ModuleType.HEAD_LASER:
                case Module.ModuleType.HEAD_LASER_10W:
                    // FIXME: 2021/9/8 Laser 10w may not match this!
                    float laserFocus = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolHeadInfoValue().getLaserFocalLength();
                    data.put("laserFocalLength", laserFocus);
                    data.put("laserPower", status.laserPower);
                    data.put("laserCamera", ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().isConnected());
                    data.put("laser10WErrorState", ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolHeadInfoValue().getHeadStatus());
                    break;
                case Module.ModuleType.HEAD_CNC:
                    data.put("spindleSpeed", status.spindleSpeed);
                    break;
            }

            data.put("workSpeed", status.feedRate);

            // print status
            int printState = ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().getRemotePrintState();

            String printStatus = "Idle";
            switch (printState) {
                case DeprecatedPrintController.STATE_PAUSED:
                    printStatus = "Paused";
                    break;
                case DeprecatedPrintController.STATE_PRINTING:
                    printStatus = "Printing";
                    break;
                case DeprecatedPrintController.STATE_COMPLETED:
                    printStatus = "Complete";
                    break;
                case DeprecatedPrintController.STATE_IDLE:
                default:
                    printStatus = "Idle";
                    break;
            }
            data.put("printStatus", printStatus);

            if (printState != DeprecatedPrintController.STATE_IDLE) {
                boolean isPrintFromRemote = ServiceContainer.getInstance().getService(IAppService.class).getWorkspace().getPrintSource() == Constants.PRINT_SOURCE_LUBAN;
                String printFileName = "";
                if (ServiceContainer.getInstance().getService(IAppService.class).getWorkspace().getPrintFile() == null && ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().getFile() == null) {
                    printFileName = "";
                } else {
                    if (isPrintFromRemote) {
                        printFileName = ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().getFileName();

                    } else {
                        printFileName = ServiceContainer.getInstance().getService(IAppService.class).getWorkspace().getFileName();
                    }
                }

                data.put("fileName", printFileName);
                data.put("totalLines", ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().getTotalLines());
                data.put("estimatedTime", isPrintFromRemote ? ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().getEstimatedTime() : ServiceContainer.getInstance().getService(IAppService.class).getWorkspace().getEstimatedTime());
                data.put("currentLine", ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().getProgressCount());
                data.put("progress", ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().getProgress());
                data.put("elapsedTime", ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().getElapsedTIme());
                data.put("remainingTime", ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().getRemainingTime());
            }

            // collect add-on module list
            JSONObject addOnModules = new JSONObject();
            addOnModules.put("enclosure", ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isEnclosureAvailable);
            addOnModules.put("rotaryModule", ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isRotaryAvailable);
            addOnModules.put("emergencyStopButton", ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isEnclosureAvailable);
            addOnModules.put("airPurifier", ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isAirPurifierAvailable);

            data.put("moduleList", addOnModules);

            if (ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isEnclosureAvailable) {
                data.put("isEnclosureDoorOpen", ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isEnclosureAvailable);
                data.put("doorSwitchCount", ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().getEnclosureDoorCount());
            }

            if (ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isEmergencyStopAvailable) {
                data.put("isEmergencyStopped", ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isEmergencyStopAvailable);
            }

            if (ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isAirPurifierAvailable) {
                data.put("airPurifierSwitch", ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getAirPurifier().getAirPurifierStatusValue().isFanOn());
                data.put("airPurifierFanSpeed", ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getAirPurifier().getAirPurifierStatusValue().getFanSpeedLevel());
                data.put("airPurifierFilterHealth", ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getAirPurifier().getAirPurifierStatusValue().getFilterLife());
            }

            response.setBody(new JsonBody(data));
        } catch (JSONException e) {
            LogHelper.log(e);
        }
    }

    @GetMapping(URI_LASER_MATERIAL_THICKNESS)
    void laserMaterialThckness(HttpRequest request, HttpResponse response,
                               @RequestParam("x") float x,
                               @RequestParam("y") float y,
                               @RequestParam("feedRate") int f) {
        JSONObject json = new JSONObject();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        //Measure height.
        float initZ = 170f;
        Disposable subscription = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .flatMap(success -> {
                    Vector vector = new Vector();
                    vector.setX(x);
                    vector.setY(y);
                    vector.setZ(initZ);
                    return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().gotoAbsolutePosition(vector, f);
                })
                .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1))
                .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getLaserController().switchFocusAssistLight(1))
                .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().setExposeTime(1))
                .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().requestCapturePhoto())
                .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().watchPhotoReceive())
                .subscribe(bitmap -> {
                    json.put("status", true);
                    FileOutputStream out = new FileOutputStream(ServiceContainer.getInstance().getService(IAppService.class).getCacheDir() + "/distance.jpg");
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    float distance = 10; //LaserDistanceMeasureProcess.process(bitmap);
                    if (distance < -200) {
                        json.put("status", false);
                        countDownLatch.countDown();
                        return;
                    }
                    Logger.i(">>> distance is %s <<<", distance);
                    float mS1plus = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserThicknessS1Plus();
                    float mS2plus = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserThicknessS2Plus();
                    float h1 = H1_Z_POSITION + CAMERA_HEIGHT_OFFSET;
                    float h2 = H2_Z_POSITION + CAMERA_HEIGHT_OFFSET;
                    float h3 = h1 - h2;
                    float thickness = h1 - (h1 * ((h3 * mS1plus) + ((mS2plus * h2) - (mS1plus * h1))) / (h3 * distance + ((mS2plus * h2) - (mS1plus * h1)))) + MockConst.LASER_MATERIAL_MEASURE_CALIBRATION_OBJECT_HEIGHT;
                    json.put("thickness", thickness);
                    countDownLatch.countDown();
                }, e -> {
                    Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().switchFocusAssistLight(0).subscribe();
                    sub.dispose();
                    e.printStackTrace();
                    json.put("status", false);
                    countDownLatch.countDown();
                });
        compositeDisposable.add(subscription);
        try {
            countDownLatch.await();
            final ResponseBody body = new JsonBody(json);
            response.setBody(body);
        } catch (InterruptedException e) {
            try {
                json.put("status", false);
                final ResponseBody body = new JsonBody(json);
                response.setBody(body);
            } catch (JSONException e2) {
                e2.printStackTrace();
            }
        } finally {
            Disposable subscribe = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().switchFocusAssistLight(0)
                    .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().setExposeTime(0))
                    .subscribe(success -> {
                    }, LogHelper::log);
            compositeDisposable.add(subscribe);
        }
    }

    // file
    @PostMapping(path = URI_UPLOAD_FILE)
    void uploadFile(HttpRequest request, HttpResponse response,
                    @RequestParam(name = "file") MultipartFile file) {
        if (!ensureConnection(request, response)) return;

        if (file.isEmpty()) {
            response.setBody(new StringBody("Empty file body"));
            response.setStatus(HttpResponse.SC_BAD_REQUEST);
            return;
        }

        if (file.getFilename() == null) {
            response.setBody(new StringBody("Empty file name"));
            response.setStatus(HttpResponse.SC_BAD_REQUEST);
            return;
        }

        File targetFile = new File(ServiceContainer.getInstance().getService(IAppService.class).getFilesDir(), file.getFilename());
        try {
            file.transferTo(targetFile);
            HTTPEventBus.getInstance().onReceiveFile(targetFile);
        } catch (IOException e) {
            response.setStatus(HttpResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        response.setBody(new StringBody("Upload successfully."));
        response.setStatus(StatusCode.SC_OK);
    }

    @GetMapping(path = URI_PRINT_FILE)
    void downloadPrintFile(HttpRequest request, HttpResponse response) {
        if (!ensureConnection(request, response)) return;

        // TODO: Did we define "print file" in this API?
        //  What if local print is running, and remote access request "print file" ?
        File targetFile = new File(ServiceContainer.getInstance().getService(IAppService.class).getWorkspace().getWorkspaceDir().getPath(), "remotePrint.gcode");

        if (!targetFile.exists()) {
            response.setBody(new StringBody("File not Exist."));
            response.setStatus(StatusCode.SC_FORBIDDEN);
        } else {
            response.setBody(new FileBody(targetFile));
            response.setStatus(StatusCode.SC_OK);
        }
    }

    // - Movement

    @PostMapping(path = URI_EXECUTE_CODE)
    void executeCode(HttpRequest request, HttpResponse response,
                     @RequestParam(name = "code") String code) {
        if (!ensureConnection(request, response)) return;

        DeprecatedMachineInfo status = MachineStatusManager.getMachineInfoHolder().getValue();
        if (status.printerStatus != 0) {
            response.setBody(new StringBody("Machine is printing now, movement rejected."));
            response.setStatus(HttpResponse.SC_FORBIDDEN);
            return;
        }

        try {
            code = URLDecoder.decode(code, "GBK");
        } catch (UnsupportedEncodingException e) {
            response.setStatus(HttpResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        String[] lines = code.split("\n");
        String line = lines[0];

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Disposable sub = ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(line, true)
                .subscribe(res -> {
                    String content = res.getContent();
                    if (content.isEmpty()) {
                        content = "ok";
                    }
                    response.setBody(new StringBody(content));
                    countDownLatch.countDown();
                }, e -> {
                    LogHelper.log(e);
                    response.setStatus(StatusCode.SC_INTERNAL_SERVER_ERROR);
                });
        compositeDisposable.add(sub);

        try {
            countDownLatch.await();
            if (line.startsWith("G28") || line.startsWith("G53")
                    || line.startsWith("G54")
                    || line.startsWith("G92")) {
                Disposable subscription = ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().getMachineController()
                        .updateCoordinateSystem().subscribe(coordinateSystem -> {/**/});
                compositeDisposable.add(subscription);
            }

            response.setStatus(HttpResponse.SC_OK);
        } catch (InterruptedException e) {
            response.setBody(new StringBody("Failed to parse G-code file."));
            response.setStatus(StatusCode.SC_BAD_REQUEST);
        }
    }

    // - Print

    @PostMapping(path = URI_PREPARE_PRINT)
    void preparePrint(HttpRequest request, HttpResponse response,
                      @RequestParam(name = "type") String type,
                      @RequestParam(name = "file") MultipartFile file) {
        if (!ensureConnection(request, response)) return;

        if (file.isEmpty()) {
            response.setBody(new StringBody("Empty file body"));
            response.setStatus(HttpResponse.SC_BAD_REQUEST);
            return;
        }

        int fileType = Constants.FILE_TYPE_UNKNOWN;
        switch (type) {
            case "3DP":
                fileType = Constants.FILE_TYPE_3DP;
                break;
            case "CNC":
                fileType = Constants.FILE_TYPE_CNC;
                break;
            case "Laser":
                fileType = Constants.FILE_TYPE_LASER;
                break;
            default:
                break;
        }

        int headType = 0;//ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType;
        if (!(headType == Module.ModuleType.HEAD_LASER_10W && fileType == Constants.FILE_TYPE_LASER)
                && headType != fileType) {
            response.setBody(new StringBody("Wrong file type."));
            response.setStatus(HttpResponse.SC_CONFLICT);
            return;
        }

        File targetFile = new File(ServiceContainer.getInstance().getService(IAppService.class).getWorkspace().getWorkspaceDir().getPath(), "remotePrint.gcode");
        try {
            file.transferTo(targetFile);
        } catch (IOException e) {
            response.setStatus(HttpResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        IFile iFile = new FabLocalFile(targetFile);
        ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().setFile(iFile);
        ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().setFileType(fileType);
        ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().setFileName(file.getFilename());
        // Workspace
        ServiceContainer.getInstance().getService(IAppService.class).getWorkspace().setPrintSource(Constants.PRINT_SOURCE_LUBAN);

        final IGcodeParser parser = new GcodeParser();
        IMachine.WorkType workType = IMachine.WorkType.NONE;
        switch (fileType) {
            case Constants.FILE_TYPE_3DP:
                workType = IMachine.WorkType.FDM;
                break;
            case Constants.FILE_TYPE_LASER:
                workType = IMachine.WorkType.LASER;
                break;
            case Constants.FILE_TYPE_CNC:
                workType = IMachine.WorkType.CNC;
                break;
        }
        parser.startParse(iFile, workType);

        // Wait file parsing to finish
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Disposable sub = parser.getParseProgressObservable()
                .throttleLast(100, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribe(progress -> {
                    Logger.d("progress = " + progress);
                    if (progress == 100) {
                        ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().reset();
                        ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().setTotalLines(parser.getTotalLinesCount());
                        ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().setEstimatedTime(parser.getEstimatedTime());

                        countDownLatch.countDown();
                    }
                });
        compositeDisposable.add(sub);

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            response.setBody(new StringBody("Failed to parse G-code file."));
            response.setStatus(StatusCode.SC_BAD_REQUEST);
            return;
        }
        response.setBody(new StringBody("Prepare successfully."));
    }

    @PostMapping(path = URI_START_PRINT)
    void startPrint(HttpRequest request, HttpResponse response) {
        if (!ensureConnection(request, response)) return;

        JSONObject result = new JSONObject();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Disposable sub = ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().start()
                .subscribe(retCode -> {
                    if (retCode == 0) {
                        response.setStatus(HttpResponse.SC_OK);
                    } else {
                        response.setStatus(HttpResponse.SC_CONFLICT);
                    }
                    result.put("code", retCode);
                    countDownLatch.countDown();
                }, LogHelper::log);
        compositeDisposable.add(sub);

        try {
            countDownLatch.await();
            response.setBody(new JsonBody(result));
        } catch (InterruptedException e) {
            response.setBody(new StringBody("Interrupted."));
            response.setStatus(HttpResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = URI_PAUSE_PRINT)
    void pausePrint(HttpRequest request, HttpResponse response) {
        if (!ensureConnection(request, response)) return;

        JSONObject result = new JSONObject();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Disposable sub = ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().pause()
                .subscribe(retCode -> {
                    if (retCode == 0) {
                        response.setStatus(HttpResponse.SC_OK);
                    } else {
                        response.setStatus(HttpResponse.SC_CONFLICT);
                    }
                    result.put("code", retCode);
                    countDownLatch.countDown();
                }, LogHelper::log);
        compositeDisposable.add(sub);

        try {
            countDownLatch.await();
            response.setBody(new JsonBody(result));
        } catch (InterruptedException e) {
            response.setBody(new StringBody("Interrupted."));
            response.setStatus(HttpResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = URI_RESUME_PRINT)
    void resumePrint(HttpRequest request, HttpResponse response) {
        if (!ensureConnection(request, response)) return;

        // Fixme: re-thick about filament out situation if requesting start printing
        if (ServiceContainer.getInstance().getService(IMachine.class).getMachineController().isFilamentOut()) {
            ServiceContainer.getInstance().getService(IMachine.class).getMachineController().clearFilamentOutFlag();
        }

        JSONObject result = new JSONObject();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Disposable sub = ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().resume()
                .subscribe(retCode -> {
                    if (retCode == 0) {
                        if (ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isEnclosureAvailable) {
                            ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure().clearEnclosureDoorFlag();
                        }
                        response.setStatus(HttpResponse.SC_OK);
                    } else {
                        response.setStatus(HttpResponse.SC_CONFLICT);
                    }
                    result.put("code", retCode);
                    countDownLatch.countDown();
                }, LogHelper::log);
        compositeDisposable.add(sub);

        try {
            countDownLatch.await();
            response.setBody(new JsonBody(result));
        } catch (InterruptedException e) {
            response.setBody(new StringBody("Interrupted."));
            response.setStatus(HttpResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = URI_STOP_PRINT)
    void stopPrint(HttpRequest request, HttpResponse response) {
        if (!ensureConnection(request, response)) return;

        JSONObject result = new JSONObject();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Disposable sub = ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().stop()
                .subscribe(retCode -> {
                    if (retCode == 0) {
                        response.setStatus(HttpResponse.SC_OK);
                    } else {
                        response.setStatus(HttpResponse.SC_CONFLICT);
                    }
                    result.put("code", retCode);
                    countDownLatch.countDown();
                }, LogHelper::log);
        compositeDisposable.add(sub);

        try {
            countDownLatch.await();
            response.setBody(new JsonBody(result));
        } catch (InterruptedException e) {
            response.setBody(new StringBody("Interrupted."));
            response.setStatus(HttpResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = URI_FILAMENT_UNLOAD)
    void filamentUnload(HttpRequest request, HttpResponse response) {
        if (!ensureConnection(request, response)) return;

        CountDownLatch countDownLatch = new CountDownLatch(1);
        Disposable subscription = ServiceContainer.getInstance().getService(IMachine.class).getFDMController().requestActivatedExtrusion(0, 6, 200, 60, 150)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseStructure -> {
                    boolean success = responseStructure.isSuccess();
                    if (success) {
                        response.setStatus(StatusCode.SC_OK);
                    } else {
                        response.setBody(new StringBody("code = " + 0));
                        response.setStatus(HttpResponse.SC_CONFLICT);
                    }
                    countDownLatch.countDown();
                });
        compositeDisposable.add(subscription);

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            response.setBody(new StringBody("Interrupted."));
            response.setStatus(HttpResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = URI_FILAMENT_LOAD)
    void filamentLoad(HttpRequest request, HttpResponse response) {
        if (!ensureConnection(request, response)) return;

        CountDownLatch countDownLatch = new CountDownLatch(1);
        Disposable subscription = ServiceContainer.getInstance().getService(IMachine.class).getFDMController().requestActivatedExtrusion(0, 60, 200, 0, 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resultStructure -> {
                    boolean success = resultStructure.isSuccess();
                    if (success) {
                        response.setStatus(StatusCode.SC_OK);
                    } else {
                        response.setBody(new StringBody("code = " + 0));
                        response.setStatus(HttpResponse.SC_CONFLICT);
                    }
                    countDownLatch.countDown();
                });
        compositeDisposable.add(subscription);

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            response.setBody(new StringBody("Interrupted."));
            response.setStatus(HttpResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = URI_OVERRIDE_NOZZLE)
    void overrideNozzleTemperature(HttpRequest request, HttpResponse response) {
        if (!ensureConnection(request, response)) return;

        if (ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId() == Module.ModuleType.HEAD_3DP) {
            response.setBody(new StringBody("Wrong head type."));
            response.setStatus(StatusCode.SC_CONFLICT);
            return;
        }

        // get parameters
        String nozzleTemp = request.getParameter("nozzleTemp");

        if (nozzleTemp == null) {
            response.setStatus(StatusCode.SC_BAD_REQUEST);
        } else {
            float temp = Float.valueOf(nozzleTemp);
            ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().overrideNozzleTemperature(temp);

            response.setStatus(StatusCode.SC_OK);
        }
    }

    @PostMapping(path = URI_OVERRIDE_HEATED_BED)
    void overrideHeatedBedTemperature(HttpRequest request, HttpResponse response) {
        if (!ensureConnection(request, response)) return;

        if (ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId() == Module.ModuleType.HEAD_3DP) {
            response.setBody(new StringBody("Wrong head type."));
            response.setStatus(StatusCode.SC_CONFLICT);
            return;
        }

        // get parameters
        String headBedTemp = request.getParameter("heatedBedTemp");

        if (headBedTemp == null) {
            response.setStatus(StatusCode.SC_BAD_REQUEST);
        } else {
            float temp = Float.valueOf(headBedTemp);
            ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().overrideHeatedBedTemperature(temp);

            response.setStatus(StatusCode.SC_OK);
        }
    }

    @PostMapping(path = URI_OVERRIDE_Z_OFFSET)
    void overrideZOffset(HttpRequest request, HttpResponse response) {
        if (!ensureConnection(request, response)) return;

        if (ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId() == Module.ModuleType.HEAD_3DP) {
            response.setBody(new StringBody("Wrong head type."));
            response.setStatus(StatusCode.SC_CONFLICT);
            return;
        }

        // get parameters
        String zOffset = request.getParameter("zOffset");

        if (zOffset == null) {
            response.setStatus(StatusCode.SC_BAD_REQUEST);
        } else {
            float offset = Float.valueOf(zOffset);
            ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().overrideZOffset(offset);

            response.setStatus(StatusCode.SC_OK);
        }
    }

    @PostMapping(path = URI_OVERRIDE_WORK_SPEED)
    void overrideWorkSpeed(HttpRequest request, HttpResponse response) {
        if (!ensureConnection(request, response)) return;

        // get parameters
        String workSpeed = request.getParameter("workSpeed");

        if (workSpeed == null) {
            response.setStatus(StatusCode.SC_BAD_REQUEST);
        } else {
            float speed = Float.valueOf(workSpeed);
            ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().overrideWorkSpeed(speed);

            response.setStatus(StatusCode.SC_OK);
        }
    }

    @PostMapping(path = URI_OVERRIDE_LASER_POWER)
    void overrideLaserPower(HttpRequest request, HttpResponse response) {
        if (!ensureConnection(request, response)) return;

        if (!isLaser()) {
            response.setBody(new StringBody("Wrong head type."));
            response.setStatus(StatusCode.SC_CONFLICT);
            return;
        }

        // get parameters
        String laserPower = request.getParameter("laserPower");

        if (laserPower == null) {
            response.setStatus(StatusCode.SC_BAD_REQUEST);
        } else {
            float power = Float.valueOf(laserPower);
            ServiceContainer.getInstance().getService(IAppService.class).getRemoteController().overrideLaserPower(power);

            response.setStatus(StatusCode.SC_OK);
        }
    }

    private boolean isLaser() {
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType == IMachine.WorkType.LASER;
    }

    // enclosure
    @GetMapping(path = URI_ENCLOSURE)
    void getEnclosure(HttpRequest request, HttpResponse response) {
        if (!ensureConnection(request, response)) return;

        JSONObject data = new JSONObject();
        try {
            data.put("isReady", ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isEnclosureAvailable);
            data.put("isDoorEnabled", ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure().getEnclosureStatusValue().isDoorDetectionEnabled());
            data.put("led", ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure().getEnclosureStatusValue().getLedValue());
            data.put("fan", ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure().getEnclosureStatusValue().getFanSpeed());

            response.setBody(new JsonBody(data));
        } catch (JSONException e) {
            LogHelper.log(e);
        }
    }

    @PostMapping(path = URI_ENCLOSURE)
    void setEnclosure(HttpRequest request, HttpResponse response) {
        if (!ensureConnection(request, response)) return;

        if (!ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isEnclosureAvailable) {
            response.setBody(new StringBody("Enclosure is not available."));
            response.setStatus(StatusCode.SC_CONFLICT);
            return;
        }
        CountDownLatch countDownLatch = new CountDownLatch(3);
        JSONObject data = new JSONObject();

        String led = request.getParameter("led");
        if (led == null) {
            countDownLatch.countDown();
        } else {
            int value = Integer.parseInt(led);

            Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure()
                    .setEnclosureLedLevel(value)
                    .doOnNext(success -> response.setStatus(success.isSuccess() ? StatusCode.SC_OK : StatusCode.SC_CONFLICT))
                    .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure().getEnclosureStatusObservable())
                    .subscribe(status -> {
                        data.put("led", status.getLedValue());
                        countDownLatch.countDown();
                    }, e -> {
                        LogHelper.log(e);
                        response.setStatus(StatusCode.SC_INTERNAL_SERVER_ERROR);
                        countDownLatch.countDown();
                    });
            compositeDisposable.add(sub);
        }

        String fan = request.getParameter("fan");
        if (fan == null) {
            countDownLatch.countDown();
        } else {
            int value = Integer.parseInt(fan);
            Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure()
                    .setEnclosureFanLevel(value)
                    .doOnNext(success -> response.setStatus(success.isSuccess() ? StatusCode.SC_OK : StatusCode.SC_CONFLICT))
                    .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure().getEnclosureStatusObservable())
                    .subscribe(status -> {
                        data.put("fan", status.getFanSpeed());
                        countDownLatch.countDown();
                    }, e -> {
                        LogHelper.log(e);
                        response.setStatus(StatusCode.SC_INTERNAL_SERVER_ERROR);
                        countDownLatch.countDown();
                    });
            compositeDisposable.add(sub);
        }

        String doorEnabled = request.getParameter("isDoorEnabled");
        if (doorEnabled == null) {
            countDownLatch.countDown();
        } else {
            boolean isDoorEnabled = Boolean.parseBoolean(doorEnabled);
            Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure()
                    .setEnclosureDoorDetection(isDoorEnabled)
                    .doOnNext(success -> response.setStatus(success.isSuccess() ? StatusCode.SC_OK : StatusCode.SC_CONFLICT))
                    .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getEnclosure().getEnclosureStatusObservable())
                    .subscribe(status -> {
                        data.put("isDoorEnabled", status.isDoorDetectionEnabled());
                        countDownLatch.countDown();
                    }, e -> {
                        LogHelper.log(e);
                        response.setStatus(StatusCode.SC_INTERNAL_SERVER_ERROR);
                        countDownLatch.countDown();
                    });
            compositeDisposable.add(sub);
        }

        try {
            countDownLatch.await();
            if (led == null && fan == null && doorEnabled == null) {
                response.setStatus(StatusCode.SC_BAD_REQUEST);
            }
            response.setBody(new JsonBody(data));
        } catch (InterruptedException e) {
            response.setBody(new StringBody("Interrupted."));
            response.setStatus(HttpResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = URI_AIR_PURIFIER_SWITCH)
    void setAirPurifierSwitch(HttpRequest request, HttpResponse response) {
        if (!ensureConnection(request, response)) return;

        if (!ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isAirPurifierAvailable) {
            response.setBody(new StringBody("Air Purifier is not available."));
            response.setStatus(StatusCode.SC_CONFLICT);
            return;
        }
        CountDownLatch countDownLatch = new CountDownLatch(1);
        JSONObject data = new JSONObject();

        // get parameter
        String stringSwitch = request.getParameter("switch");
        if (stringSwitch == null) {
            countDownLatch.countDown();
        } else {
            boolean enabled = Boolean.parseBoolean(stringSwitch);
            Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().setAirPurifierEnabled(enabled)
                    .doOnNext(success -> response.setStatus(success ? StatusCode.SC_OK : StatusCode.SC_CONFLICT))
                    .flatMap(ret -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getAirPurifier().getAirPurifierStatusObservable())
                    .subscribe(airPurifierFan -> {
                        data.put("airPurifierSwitch", airPurifierFan.isFanOn());
                        countDownLatch.countDown();
                    }, e -> {
                        LogHelper.log(e);
                        response.setStatus(StatusCode.SC_INTERNAL_SERVER_ERROR);
                        countDownLatch.countDown();
                    });
            compositeDisposable.add(sub);
        }

        try {
            countDownLatch.await();
            if (stringSwitch == null) {
                response.setStatus(StatusCode.SC_BAD_REQUEST);
            }
            response.setBody(new JsonBody(data));
        } catch (InterruptedException e) {
            response.setBody(new StringBody("Interrupted."));
            response.setStatus(HttpResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = URI_AIR_PURIFIER_FAN_SPEED)
    void setAirPurifierFanSpeed(HttpRequest request, HttpResponse response) {
        if (!ensureConnection(request, response)) return;

        if (!ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().isAirPurifierAvailable) {
            response.setBody(new StringBody("Air Purifier is not available."));
            response.setStatus(StatusCode.SC_CONFLICT);
            return;
        }
        CountDownLatch countDownLatch = new CountDownLatch(1);
        JSONObject data = new JSONObject();

        // get parameter
        String stringFanSpeed = request.getParameter("fan_speed");
        if (stringFanSpeed == null) {
            countDownLatch.countDown();
        } else {
            int level = Integer.parseInt(stringFanSpeed);
            Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getAirPurifier().setFanSpeedLevel(0, level)
                    .doOnNext(success -> response.setStatus(success.isSuccess() ? StatusCode.SC_OK : StatusCode.SC_CONFLICT))
                    .flatMap(ret -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().getAirPurifier().getAirPurifierStatusObservable())
                    .subscribe(airPurifierFan -> {
                        data.put("airPurifierFanSpeed", airPurifierFan.getFanSpeedLevel());
                        countDownLatch.countDown();
                    }, e -> {
                        LogHelper.log(e);
                        response.setStatus(StatusCode.SC_INTERNAL_SERVER_ERROR);
                        countDownLatch.countDown();
                    });
            compositeDisposable.add(sub);
        }

        try {
            countDownLatch.await();
            if (stringFanSpeed == null) {
                response.setStatus(StatusCode.SC_BAD_REQUEST);
            }
            response.setBody(new JsonBody(data));
        } catch (InterruptedException e) {
            response.setBody(new StringBody("Interrupted."));
            response.setStatus(HttpResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // test api for incoming quality control

    @GetMapping(URI_IQC_CHECK_ALIVE)
    void iqcCheckAlive(HttpResponse response) {
        response.setStatus(StatusCode.SC_OK);
    }

    @PostMapping(URI_IQC_UPLOAD_TEST)
    void iqcUploadTest(HttpResponse response, @RequestParam("file") MultipartFile file) {
        response.setStatus(StatusCode.SC_OK);
    }
}
