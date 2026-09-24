package fabscreen.platform.base.service.remote;

import static fabscreen.platform.base.helper.GsonHelper.A150_10W;
import static fabscreen.platform.base.helper.GsonHelper.A150_1_6W;
import static fabscreen.platform.base.helper.GsonHelper.A250_10W;
import static fabscreen.platform.base.helper.GsonHelper.A250_1_6W;
import static fabscreen.platform.base.helper.GsonHelper.A350_10W;
import static fabscreen.platform.base.helper.GsonHelper.A350_1_6W;
import static fabscreen.platform.base.helper.GsonHelper.A400_10W;
import static fabscreen.platform.base.helper.GsonHelper.A400_1_6W;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import fabscreen.platform.base.helper.GsonHelper;
import fabscreen.platform.base.helper.SendFileHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.prop.ArrayProp;
import fabscreen.platform.base.service.machine.structure.prop.FloatProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt16Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import okio.Buffer;

public class RemoteLaserController {
    private RemoteConnectionController mConnectionController;
    private RemoteFileController mRemoteFileController;
    private SendFileHelper mSendFileHelper;
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private IAppService mAppService;
    private IPreferences mPreferences;

    private String mFileName;
    private long mFileLength;
    private long mPackageCount;
    private String mMd5;

    PublishSubject<Integer> mRequestPackageSubject = PublishSubject.create();
    PublishSubject<Integer> mSendPackageSubject = PublishSubject.create();
    PublishSubject<IFile> mFilePackageSubject = PublishSubject.create();
    private ArrayList<byte[]> mDataList;
    int mReceivedFileLength;
    private int mSendDataSequence;

    public RemoteLaserController(IMachine iMachine, RemoteConnectionController connectionController, RemoteFileController remoteFileController, IAppService appService, IPreferences preferences) {
        mConnectionController = connectionController;
        mRemoteFileController = remoteFileController;
        mSendFileHelper = new SendFileHelper(remoteFileController);
        mAppService = appService;
        mPreferences = preferences;
        Logger.d("debug RemoteLaserController instance.");
    }

    // 0xb0 0x03
    public void requestGet10WCameraCalibrationData(int commandSet, int commandId, int sequence, IStructure requestStructure) {
        int result = 0;
        String calibrationData = mPreferences.getHelper().get10WLaserCameraCalibration();
        // TODO: add new Structure for this response.
        ResponseStructure responseStructure = new ResponseStructure();
        if (calibrationData == null) {
            // Temporary fail code;
            Logger.d("debug RemoteLaserController calibration data is null");
            result = 200;
        } else {
            // Fill data
            Logger.d("debug RemoteLaserController fill data.");
            BeanCameraCalibrationData data = new Gson().fromJson(calibrationData, BeanCameraCalibrationData.class);
            if (data == null) {
                // Parse json failed.
                result = 200;
            } else {
                BaseStructure baseStructure = new BaseStructure() {
                    @Override
                    protected void init() {
                        addProp("point", new ArrayProp<>(data.points));
                        addProp("corner", new ArrayProp<>(data.corners));
                    }
                };
                responseStructure.dataProp = baseStructure;
                Logger.d("debug RemoteLaserController data is " + baseStructure.toString());
            }
        }
        responseStructure.resultProp = new UInt8Prop(result);
        mConnectionController.sendResponse(commandSet, commandId, sequence, responseStructure);
    }

    Disposable subscription;

    // 0xb0 0x04
    public void requestCapturePhotoByMove(int commandSet, int commandId, int sequence, byte[] requestPayload) throws IOException {
        int result = 0;
        if (requestPayload == null) {
            result = 6;
            Logger.d("debug RemoteLaserController payload is null");
            ResponseStructure responseStructure = new ResponseStructure();
            responseStructure.resultProp = new UInt8Prop(result);
            mConnectionController.sendResponse(commandSet, commandId, sequence, responseStructure);
        } else {
            BaseStructure structure = new BaseStructure() {
                @Override
                protected void init() {
                    addProp("photoIndex", new UInt8Prop());
                    addProp("positionX", new FloatProp());
                    addProp("positionY", new FloatProp());
                    addProp("positionZ", new FloatProp());
                    addProp("feedRate", new UInt16Prop());
                    addProp("photoQuality", new UInt8Prop());
                }
            };
            structure.readBuffer(new Buffer().write(requestPayload));

            int index = (int) structure.getProp("photoIndex").getValue();
            float x = (float) structure.getProp("positionX").getValue();
            float y = (float) structure.getProp("positionY").getValue();
            float z = (float) structure.getProp("positionZ").getValue();
            int f = (int) structure.getProp("feedRate").getValue();
            int quality = (int) structure.getProp("photoQuality").getValue();

            Vector vector = new Vector();
            vector.setX(x);
            vector.setY(y);
            vector.setZ(z);
            if (subscription != null && !subscription.isDisposed()) subscription.dispose();
            subscription =
                    ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                            .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().gotoAbsolutePosition(vector, f))
                            .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().setPhotoQuality(quality))
                            .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().requestCapturePhoto())
                            .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().watchPhotoReceive())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(bitmap -> {

                                Matrix m = new Matrix();
                                m.postRotate(ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserToolhead().getModuleInfo().getModuleId() == Module.ModuleType.HEAD_LASER_10W ? 90 : 270);
                                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);

                                File file = new File(ServiceContainer.getInstance().getService(IAppService.class).getCacheDir(), index + "_temp.jpg");
                                FileOutputStream fos = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                fos.close();
                                file.renameTo(new File(ServiceContainer.getInstance().getService(IAppService.class).getCacheDir(), index + ".jpg"));

                                ResponseStructure responseStructure = new ResponseStructure();
                                responseStructure.resultProp = new UInt8Prop(0);
                                mConnectionController.sendResponse(commandSet, commandId, sequence, responseStructure);
                                subscription.dispose();
                                // Start send file and wait result for response.
//                        Disposable sub = mSendFileHelper.sendFile(file)
//                                .subscribe(finalResult -> {
//                                    Logger.d("Send file result " + finalResult);
//
//                                    ResponseStructure responseStructure = new ResponseStructure();
//                                    responseStructure.resultProp = new UInt8Prop(finalResult);
//
//                                });
//                        mDisposables.add(sub);

                            }, e -> {
                                e.printStackTrace();
                                // FIXME: 2022/4/11 exception should return 1
                                final int errorResult = 200;
                                // Return result.
                                Logger.d("Exception catch ! e :\n " + e.toString());
                                ResponseStructure responseStructure = new ResponseStructure();
                                responseStructure.resultProp = new UInt8Prop(errorResult);
                                mConnectionController.sendResponse(commandSet, commandId, sequence, responseStructure);
                                subscription.dispose();
                            });
        }
    }

    // 0xb0 0x05
    // response after file transfer finished.
    public void requestGetPhotoByIndex(int commandSet, int commandId, int sequence, byte[] requestPayload) throws IOException {
        int result;
        if (requestPayload == null) {
            Logger.d("debug RemoteLaserController payload is null");
            // Return result.
            mConnectionController.sendResponse(commandSet, commandId, sequence, new ResponseStructure<>(6));
        } else {
            BaseStructure structure = new BaseStructure() {
                @Override
                protected void init() {
                    addProp("photoIndex", new UInt8Prop());
                }
            };
            structure.readBuffer(new Buffer().write(requestPayload));

            // Get Index
            int index = (int) structure.getProp("photoIndex").getValue();

            File file = new File(ServiceContainer.getInstance().getService(IAppService.class).getCacheDir(), index + ".jpg");
            if (file.exists()) {
                mDisposables.add(mSendFileHelper.sendFileDesc(file)
                        .flatMap(descSendResult -> mRemoteFileController.getSendFileResultObservable())
                        .subscribe(baseStructure -> mConnectionController.sendResponse(commandSet, commandId, sequence, new ResponseStructure<>(baseStructure)),
                                e -> {
                                    LogHelper.log(e);
                                    mConnectionController.sendResponse(commandSet, commandId, sequence, new ResponseStructure<>(201));
                                }));
            } else {
                // File wasn't exist, return result.
                mConnectionController.sendResponse(commandSet, commandId, sequence, new ResponseStructure<>(200));
            }
        }
    }

    // 0xb0 0x06
    public void requestCameraCalibrationPhoto(int commandSet, int commandId, int sequence, byte[] requestPayload) {
        int result;
        if (requestPayload == null) {
            Logger.d("debug RemoteLaserController payload is null");
            // Return result.
            mConnectionController.sendResponse(commandSet, commandId, sequence, new ResponseStructure<>(6));
        } else {
            BaseStructure structure = new BaseStructure() {
                @Override
                protected void init() {
                    addProp("toolHeadType", new UInt8Prop());
                }
            };

            File file = new File(ServiceContainer.getInstance().getService(IAppService.class).getCacheDir(), "10WLaserCalibration.jpg");
            if (file.exists()) {
                Logger.d("debug RemoteLaserController file exists, start sending...");
                mDisposables.add(mSendFileHelper.sendFileDesc(file)
                        .flatMap(descSendResult -> mRemoteFileController.getSendFileResultObservable())
                        .subscribe(baseStructure -> mConnectionController.sendResponse(commandSet, commandId, sequence, new ResponseStructure<>(baseStructure)),
                                e -> {
                                    LogHelper.log(e);
                                    mConnectionController.sendResponse(commandSet, commandId, sequence, new ResponseStructure<>(201));
                                }
                        ));

            } else {
                // File wasn't exist, return result.
                Logger.d("debug RemoteLaserController not exists.");
                mConnectionController.sendResponse(commandSet, commandId, sequence, new ResponseStructure<>(200));
            }
        }
    }

    // 0xb0 0x07
    public void requestSet10WCameraCalibrationData(int commandSet, int commandId, int sequence, byte[] requestPayload) throws IOException {
        int result = 0;
        if (requestPayload == null) {
            result = 6;
            Logger.d("debug RemoteLaserController payload is null");
        } else {
            BaseStructure structure = new BaseStructure() {
                @Override
                protected void init() {
                    addProp("headType", new UInt8Prop());
                    addProp("points", new ArrayProp<>(new BeanPoint()));
                    addProp("corners", new ArrayProp<>(new BeanPoint()));
                }
            };

            structure.readBuffer(new Buffer().write(requestPayload));
            ArrayList<BeanPoint> points = (ArrayList) structure.getProp("points").getValue();
            ArrayList<BeanPoint> corners = (ArrayList) structure.getProp("corners").getValue();
            Logger.d("points %s corners %s", points, corners);

            BeanCameraCalibrationData beanCameraCalibrationData = new BeanCameraCalibrationData(points, corners);
            // TODO: Distinguish between 10W and 1.6W
            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().set10WLaserCameraCalibration(new Gson().toJson(beanCameraCalibrationData));
        }

        ResponseStructure responseStructure = new ResponseStructure();
        responseStructure.resultProp = new UInt8Prop(result);
        Logger.d("debug RemoteLaserController response is " + responseStructure);

        mConnectionController.sendResponse(commandSet, commandId, sequence, responseStructure);
    }

    // 0xb0 0x90
    public void requestSetTestCameraCalibrationTakePhotoVector(int commandSet, int commandId, int sequence, byte[] requestPayload) throws IOException {
        ResponseStructure responseStructure = new ResponseStructure();
        BaseStructure structure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("modelId", new UInt8Prop());
                addProp("headType", new UInt8Prop());
                addProp("positionX", new FloatProp());
                addProp("positionY", new FloatProp());
                addProp("positionZ", new FloatProp());
            }
        };
        structure.readBuffer(new Buffer().write(requestPayload));
        int model = (int) structure.getProp("modelId").getValue();
        int headType = (int) structure.getProp("headType").getValue();
        float x = (float) structure.getProp("positionX").getValue();
        float y = (float) structure.getProp("positionY").getValue();
        float z = (float) structure.getProp("positionZ").getValue();
        IPreferences.Helper helper = ServiceContainer.getInstance().getService(IPreferences.class).getHelper();

        String cameraCalibrationTakePhotoVector = helper.getCameraCalibrationTakePhotoVector();
        Map<String, Vector> map = new GsonHelper().StringToCameraCalibrationTakePhotoVector(cameraCalibrationTakePhotoVector);
        Vector vector = new Vector();
        vector.setX(x);
        vector.setY(y);
        vector.setZ(z);
        if (headType == Module.ModuleType.HEAD_LASER) {
            switch (model) {
                case IMachine.MachineModel.A150:
                    map.put(A150_1_6W, vector);
                    break;
                case IMachine.MachineModel.A250:
                    map.put(A250_1_6W, vector);
                    break;
                case IMachine.MachineModel.A350:
                    map.put(A350_1_6W, vector);
                    break;
                case IMachine.MachineModel.A400:
                    map.put(A400_1_6W, vector);
                    break;
                default:
                    responseStructure.resultProp.setValue(200);
                    break;
            }
        } else if (headType == Module.ModuleType.HEAD_LASER_10W) {
            switch (model) {
                case IMachine.MachineModel.A150:
                    map.put(A150_10W, vector);
                    break;
                case IMachine.MachineModel.A250:
                    map.put(A250_10W, vector);
                    break;
                case IMachine.MachineModel.A350:
                    map.put(A350_10W, vector);
                    break;
                case IMachine.MachineModel.A400:
                    map.put(A400_10W, vector);
                    break;
                default:
                    responseStructure.resultProp.setValue(200);
                    break;
            }
        } else {
            responseStructure.resultProp.setValue(200);
        }
        if (responseStructure.resultProp.getValue().equals(0)) {
            helper.setCameraCalibrationTakePhotoVector(new GsonHelper().CameraCalibrationTakePhotoVectorToString(map));
        }
        mConnectionController.sendResponse(commandSet, commandId, sequence, responseStructure);
    }

    // 0xb0 0x91
    public void requestGetTestCameraCalibrationTakePhotoVector(int commandSet, int commandId, int sequence, byte[] requestPayload) throws IOException {
        ResponseStructure responseStructure = new ResponseStructure();
        Buffer buffer = new Buffer().write(requestPayload);
        int model = new UInt8Prop().readBufferToValue(buffer);
        int headType = new UInt8Prop().readBufferToValue(buffer);
        IPreferences.Helper helper = ServiceContainer.getInstance().getService(IPreferences.class).getHelper();
        String cameraCalibrationTakePhotoVector = helper.getCameraCalibrationTakePhotoVector();
        Vector vectorByModuleID = new GsonHelper().getVectorByModuleID(cameraCalibrationTakePhotoVector, model, headType);
        if (vectorByModuleID == null) {
            responseStructure.resultProp.setValue(200);
        } else {
            BaseStructure baseStructure = new BaseStructure() {
                @Override
                protected void init() {
                    addProp("positionX", new FloatProp());
                    addProp("positionY", new FloatProp());
                    addProp("positionZ", new FloatProp());
                }
            };
            baseStructure.getProp("positionX").setValue(vectorByModuleID.getX());
            baseStructure.getProp("positionY").setValue(vectorByModuleID.getY());
            baseStructure.getProp("positionZ").setValue(vectorByModuleID.getZ());
            responseStructure.dataProp = baseStructure;
        }
        mConnectionController.sendResponse(commandSet, commandId, sequence, responseStructure);
    }

    Disposable subscribe;

    public void requestAutoMeasureMaterialThickness(int commandSet, int commandId, int sequence, byte[] payload) throws IOException {
        float x = 0;
        float y = 0;
        int speed = 0;
        float initZ = 0;
        ResponseStructure<IStructure> iStructureResponseStructure = new ResponseStructure<>();
        try {
            Buffer buffer = new Buffer().write(payload);
            x = new FloatProp().readBufferToValue(buffer);
            y = new FloatProp().readBufferToValue(buffer);
            speed = new UInt16Prop().readBufferToValue(buffer);
            initZ = 170f;
        } catch (Exception e) {
            LogHelper.log(e);
            iStructureResponseStructure.resultProp.setValue(6);
            mConnectionController.sendResponse(commandSet, commandId, sequence, iStructureResponseStructure);
            return;
        }
        if (subscribe != null && !subscribe.isDisposed()) subscribe.dispose();
        subscribe = ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getAutothickness(x, y, initZ, speed)
                .subscribe(autothickness -> {
                    Logger.d("---FDT--- " + autothickness);
                    if (autothickness != -200) {
                        iStructureResponseStructure.dataProp = new FloatProp(autothickness);
                        mConnectionController.sendResponse(commandSet, commandId, sequence, iStructureResponseStructure);
                    } else {
                        iStructureResponseStructure.resultProp.setValue(201);
                        mConnectionController.sendResponse(commandSet, commandId, sequence, iStructureResponseStructure);
                    }
                    subscribe.dispose();
                }, e -> {
                    LogHelper.log(e);
                    iStructureResponseStructure.resultProp.setValue(202);
                    mConnectionController.sendResponse(commandSet, commandId, sequence, iStructureResponseStructure);
                    subscribe.dispose();
                });
    }


    // Temporary Bean class.
    public static class BeanCameraCalibrationData {
        private ArrayList<BeanPoint> points;
        private ArrayList<BeanPoint> corners;

        BeanCameraCalibrationData(ArrayList<BeanPoint> a, ArrayList<BeanPoint> b) {
            points = a;
            corners = b;
        }

        @Override
        public String toString() {
            return "BeanCameraCalibrationData{" +
                    "points=" + points.toString() +
                    ", corners=" + corners.toString() +
                    '}';
        }
    }

    public static class BeanPoint implements IStructure {
        float x;
        float y;

        @Override
        public byte[] toByteArray() {
            Buffer buffer = new Buffer();
            buffer.write(new FloatProp(x).toByteArray());
            buffer.write(new FloatProp(y).toByteArray());
            return buffer.readByteArray();
        }

        @Override
        public Buffer readBuffer(Buffer buffer) throws IOException {
            x = new FloatProp().readBufferToValue(buffer);
            y = new FloatProp().readBufferToValue(buffer);
            return buffer;
        }

        @Override
        public String toString() {
            return "BeanPoint{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }
}
