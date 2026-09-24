package fabscreen.platform.base.service.machine.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import fabscreen.platform.base.model.ILaserCameraController;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.BytesProp;
import fabscreen.platform.base.service.machine.structure.prop.StringProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt16Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt32Prop;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class SACPLaserCameraController implements ILaserCameraController {
    private final MachineConnectionController mCameraController;
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private final BehaviorSubject<Boolean> mISConnectSubject = BehaviorSubject.create();
    private ArrayList<byte[]> mDataList;
    private int mDataSize;
    private long mRecvTime = 0;
    PublishSubject<Bitmap> mBitmapPackage = PublishSubject.create();
    private static SACPLaserCameraController instance;
    private int mPackageNum;

    public static SACPLaserCameraController getInstance(String address, IPreferences preferences, IAppService appService) {
        if (instance == null) {
            instance = new SACPLaserCameraController(address, preferences, appService);
        } else {
            instance.setAddress(address);
        }
        return instance;
    }

    private void setAddress(String address) {
        try {
            mCameraController.setAddress(address);
        } catch (Exception e) {
            Logger.e(e.toString());
        }

    }


    public SACPLaserCameraController(String address, IPreferences preferences, IAppService appService) {
        mCameraController = new MachineConnectionController(address, preferences, true, appService.getAppContext(), appService, mISConnectSubject);
        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("id", new UInt16Prop());
                addProp("bytes", new BytesProp());
            }
        };
        mDisposable.add(
                mCameraController.watch(0xa9, 0xa0, new ResponseStructure(baseStructure))
                        .subscribe(responseStructure -> {
                                    Integer result = responseStructure.resultProp.getValue();
                                    switch (result) {
                                        case 200:
                                            // ready for receiving photo
                                            mDataList = new ArrayList<>();
                                            mDataSize = 0;
                                            mRecvTime = SystemClock.elapsedRealtime();
                                            mPackageNum = 0;
                                            Logger.d("Start receiving image...");
                                            break;
                                        case 201:
                                            mPackageNum++;
                                            BaseStructure baseStructure1 = (BaseStructure) responseStructure.dataProp;
                                            int id = ((UInt16Prop) baseStructure1.getProp("id")).getValue();
                                            byte[] value = ((BytesProp) baseStructure1.getProp("bytes")).getValue();
                                            mDataList.add(value);
                                            mDataSize += value.length;
//                                            Logger.d("---FDT--- packageIndex: " + id);
                                            break;
                                        case 202:
                                            Logger.d("---FDT--- mPackageNum:" + mPackageNum);
                                            // merge photo data
                                            byte[] pic = mergeData();
                                            if (pic == null) {
                                                // 错误
                                                Logger.d("---FDT--- pic == null");
                                                return;
                                            }
                                            Bitmap bitmap = BitmapFactory.decodeByteArray(pic, 0, pic.length);
                                            if (bitmap != null) {
                                                mBitmapPackage.onNext(bitmap);
                                                Logger.d("---FDT--- CameraHelper END");
                                            } else {
                                                Logger.d("---FDT--- bitmap != null");
                                            }
                                            Logger.d("End receiving image...");
                                            break;
                                        default:
                                            Logger.d("---FDT--- ERROR");
                                    }
                                }
                        ));

    }

    public Observable<Bitmap> getBitmapObservable() {
        return mBitmapPackage.hide();
    }

    private byte[] mergeData() {
        Logger.d("---FDT--- mergeData " + mDataSize);
        byte[] pic = new byte[mDataSize];
        int offset = 0;
        if (mDataList == null) return null;

        for (byte[] d : mDataList) {
            System.arraycopy(d, 0, pic, offset, d.length);
            offset += d.length;
        }
        return pic;
    }

    // Request camera initialization
    public void requestInitialization() {
        mCameraController.request(0xa9, 0x00, null, new ResponseStructure<>());
    }

    public Observable<ResponseStructure> takePhoto(int flashTime, int delayTime) {
        return takePhoto(flashTime, delayTime, true);
    }

    public Observable<ResponseStructure> takePhoto(int flashTime, int delayTime, boolean autoUpdate) {
        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("flashTime", new UInt16Prop());
                addProp("delayTime", new UInt16Prop());
                addProp("autoUpdate", new BoolProp());
            }
        };
        baseStructure.getProp("flashTime").setValue(flashTime);
        baseStructure.getProp("delayTime").setValue(delayTime);
        baseStructure.getProp("autoUpdate").setValue(autoUpdate);

        BaseStructure responseSreucture = new BaseStructure() {
            @Override
            protected void init() {
                addProp("size", new UInt32Prop());
                addProp("packageNum", new UInt16Prop());
                addProp("fileId", new UInt16Prop());
            }
        };
        return mCameraController.request(0xa9, 0x01, baseStructure, new ResponseStructure<>(responseSreucture));
    }

    public Observable<ResponseStructure> getPhoto(int id, int packageIndex) {
        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("id", new UInt16Prop());
                addProp("packageIndex", new UInt16Prop());
            }
        };
        baseStructure.getProp("id").setValue(id);
        baseStructure.getProp("packageIndex").setValue(packageIndex);
        return mCameraController.request(0xa9, 0x02, baseStructure, new ResponseStructure<>(new BytesProp()));
    }

    public Observable<ResponseStructure> RequestUpgrade(String versionInformation, int size, int packageNum) {
        BaseStructure baseStructure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("versionInformation", new StringProp());
                addProp("size", new UInt32Prop());
                addProp("packageNum", new UInt16Prop());
            }
        };
        baseStructure.getProp("versionInformation").setValue(versionInformation);
        baseStructure.getProp("size").setValue(size);
        baseStructure.getProp("packageNum").setValue(packageNum);
        return mCameraController.request(0xa9, 0x03, baseStructure, new ResponseStructure<>());
    }

    public Observable<ResponseStructure> getVersionNumber() {
        return mCameraController.request(0xa9, 0x04, null, new ResponseStructure<>(new StringProp("")));
    }

    public Observable<ResponseStructure> getAutoWhiteBalanceStatus() {
        return mCameraController.request(0xa9, 0x05, null, new ResponseStructure<>(new BoolProp()));
    }

    public Observable<ResponseStructure> setAutoWhiteBalanceStatus(boolean whiteBalanceStatus) {
        return mCameraController.request(0xa9, 0x06, new BoolProp(whiteBalanceStatus), new ResponseStructure<>());
    }

    public Observable<ResponseStructure> getManualExposureTime() {
        return mCameraController.request(0xa9, 0x07, null, new ResponseStructure<>(new UInt16Prop()));
    }

    public Observable<ResponseStructure> setManualExposureTime(int time) {
        return mCameraController.request(0xa9, 0x08, new UInt16Prop(time), new ResponseStructure<>());
    }

    public Observable<ResponseStructure> getImageSize() {
        return mCameraController.request(0xa9, 0xa9, null, new ResponseStructure<>(new UInt8Prop()));
    }

    public Observable<ResponseStructure> setImageSize(int pictureSize) {
        return mCameraController.request(0xa9, 0x0a, new UInt8Prop(pictureSize), new ResponseStructure<>());
    }

    public Observable<ResponseStructure> getImageQuality() {
        return mCameraController.request(0xa9, 0x0b, null, new ResponseStructure<>(new UInt8Prop()));
    }

    public Observable<ResponseStructure> setImageQuality(int quality) {
        return mCameraController.request(0xa9, 0x0c, new UInt8Prop(quality), new ResponseStructure<>());
    }

    public Observable<ResponseStructure> getBluetoothName() {
        return mCameraController.request(0xa9, 0x0d, null, new ResponseStructure<>(new StringProp()));
    }

    public Observable<ResponseStructure> setBluetoothName(String name) {
        return mCameraController.request(0xa9, 0x0e, new StringProp(name), new ResponseStructure<>());
    }

    public Observable<ResponseStructure> getSupplementaryLightStatus() {
        return mCameraController.request(0xa9, 0x0f, null, new ResponseStructure<>(new UInt8Prop()));
    }

    private Observable<ResponseStructure> setSupplementaryLightStatus(int status) {
        return mCameraController.request(0xa9, 0x10, new UInt8Prop(status), new ResponseStructure<>());
    }

    public Observable<ResponseStructure> setSupplementaryLightStatus(boolean status) {
        return setSupplementaryLightStatus(status ? 0 : 1);
    }

    @Override
    public void unregister(Context context) {

    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void setEnabled(boolean enabled) {

    }

    public boolean isConnected() {
        return mISConnectSubject.getValue();
    }

    @Override
    public String getCurrentConnectedName() {
        return null;
    }

    @Override
    public int getDataSize() {
        return 0;
    }

    @Override
    public Observable<Boolean> connect(String macAddress) {
        return null;
    }

    @Override
    public void updateConnectionStatus() {

    }

    @Override
    public Observable<Boolean> getBluetoothConnectedObservable() {
        return null;
    }

    @Override
    public Observable<Boolean> requestCapturePhoto() {
        return null;
    }

    @Override
    public Observable<Boolean> requestCapturePhoto(int flashTime, int flashDelay) {
        return null;
    }

    @Override
    public Observable<Bitmap> watchPhotoReceive() {
        return null;
    }

    @Override
    public Observable<Boolean> setCameraAutoWhiteBalance(boolean activated) {
        return null;
    }

    @Override
    public Observable<Boolean> checkCameraAutoWhiteBalanceActivated() {
        return null;
    }

    @Override
    public Observable<Boolean> setCameraLighting(boolean enabled) {
        return null;
    }

    @Override
    public Observable<Boolean> setPhotoQuality(int value) {
        return null;
    }

    @Override
    public Observable<Boolean> setPhotoResolution(int resolution) {
        return null;
    }

    @Override
    public Observable<Boolean> setExposeTime(int time) {
        return null;
    }

    @Override
    public int getBondedDeviceCount() {
        return 0;
    }

    @Override
    public void removeBondedDeviceRecords() {

    }

    @Override
    public void onEmergencyStop() {

    }
}


