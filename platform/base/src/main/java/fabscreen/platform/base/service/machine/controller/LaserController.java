package fabscreen.platform.base.service.machine.controller;

import android.util.SparseArray;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.legacy.connection.LaserCameraController;
import fabscreen.platform.base.model.ILaserCameraController;
import fabscreen.platform.base.model.LaserFineTuneExecutor;
import fabscreen.platform.base.model.LaserPattern;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.MachineConnectionController;
import fabscreen.platform.base.service.machine.entity.toolhead.LaserToolhead;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.BluetoothMacStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.service.machine.structure.SubscribeStructure;
import fabscreen.platform.base.service.machine.structure.prop.BoolProp;
import fabscreen.platform.base.service.machine.structure.prop.FloatProp;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.SingleSubject;

/**
 * Control laser and laser camera
 */
public class LaserController {
    private static final String TAG = "LaserController";
    // Mapped by moduleIndex.
    SparseArray<LaserToolhead> mToolheads = new SparseArray<>();
    private final IMachine mMachine;
    private final MachineConnectionController mConnectionController;
    private MachineConnectionController mCameraController;
    private final CompositeDisposable mDisposables = new CompositeDisposable();
    private final ILaserCameraController mLaserCameraController;
    private int mHeadType = -1;

    private final BehaviorSubject<String> mBluetoothBehaviorSubject = BehaviorSubject.create();
    private SingleSubject<Integer> mStartLaserFocalCalibrationSubject;
    private SingleSubject<Integer> mStartFineTuneSubject;

    public LaserController(IMachine iMachine, MachineConnectionController connectionController, IAppService appService, IPreferences preferences) {
        mMachine = iMachine;
        mConnectionController = connectionController;

        mLaserCameraController = LaserCameraController.getInstance();

        // After get BT MAC, wait 10 seconds for BT to init, then connect.
        mDisposables.add(mBluetoothBehaviorSubject
                .distinctUntilChanged()
                .doOnNext(address -> Logger.d("Rcv BT addr: %s", address))
                .flatMap(mLaserCameraController::connect)
                .subscribe(success -> Logger.d("Connect to BT %s", success ? "success" : "fail"), Throwable::printStackTrace));

        // 订阅激光安全状态

    }

    public void onGetBluetoothMacAddress(BluetoothMacStructure bluetooth) {
        if (bluetooth.getStatus() != 0) return;
        // TODO: Possible connection to multiple Bluetooth?
        if (mBluetoothBehaviorSubject.getValue() == null || !mBluetoothBehaviorSubject.getValue().equals(bluetooth.getMac()))
            mBluetoothBehaviorSubject.onNext(bluetooth.getMac());
    }


    public void addToolHead(LaserToolhead module) {
        mMachine.getMachineInfoSubjectHolder().getValue().headType = mHeadType = module.getModuleInfo().getModuleId();
        mToolheads.put(module.getModuleInfo().getModuleIndex(), module);
        mDisposables.add(subscribeLaserSafetyState().subscribe(success -> {
        }, LogHelper::log));

    }

    public Observable<String> getBluetoothAddressObservable() {
        return mBluetoothBehaviorSubject.hide();
    }

    public int getHeadType() {
        return mHeadType;
    }

    public ILaserCameraController getLaserCameraController() {
        return mLaserCameraController;
    }

    public Observable<LaserToolhead.LaserToolheadInfo> getLaserToolHeadInfoObservable() {
        return getLaserToolHeadInfoObservable(0);
    }

    public Observable<LaserToolhead.LaserToolheadInfo> getLaserToolHeadInfoObservable(int index) {
        return mToolheads.get(index).getLaserToolHeadInfoObservable();
    }

    public LaserToolhead.LaserToolheadInfo getLaserToolHeadInfoValue() {
        return getLaserToolHeadInfoValue(0);
    }

    public LaserToolhead.LaserToolheadInfo getLaserToolHeadInfoValue(int index) {
        return mToolheads.get(index).getLaserToolHeadInfoValue();
    }

    public LaserToolhead getLaserToolhead() {
        return mToolheads.get(0);
    }

    public Observable<ResponseStructure<IStructure>> setLaserPower(int index, float targetPower) {
        LaserToolhead laserToolhead = mToolheads.get(index);

        int key = laserToolhead.getModuleInfo().getKey();
        BaseStructure laserRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("targetPower", new FloatProp());
            }
        };
        laserRequest.getProp("key").setValue(key);
        laserRequest.getProp("targetPower").setValue(targetPower);
        return mConnectionController.request(0x12, 0x02, laserRequest, new ResponseStructure());
    }

    public Observable<ResponseStructure> switchFocusAssistLight(int focusAssistLightStatus) {
        return switchFocusAssistLight(0, focusAssistLightStatus);
    }

    public Observable<ResponseStructure> switchFocusAssistLight(int index, int focusAssistLightStatus) {
        LaserToolhead laserToolhead = mToolheads.get(index);
        if (laserToolhead == null)
            // TODO:no module
            return Observable.just(new ResponseStructure());
        int key = laserToolhead.getModuleInfo().getKey();

        BaseStructure laserRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("focusAssistLightStatus", new UInt8Prop());
            }
        };
        laserRequest.getProp("key").setValue(key);
        laserRequest.getProp("focusAssistLightStatus").setValue(focusAssistLightStatus);
        return mConnectionController.request(0x12, 0x03, laserRequest, new ResponseStructure());
    }

    public Observable<ResponseStructure> setFocalLength(float focalLength) {
        return setFocalLength(0, focalLength);
    }

    public Observable<ResponseStructure> setFocalLength(int index, float focalLength) {
        LaserToolhead laserToolhead = mToolheads.get(index);
        if (laserToolhead == null) {
            throw new IllegalStateException("There's no laser toolhead matched!");
        }
        int key = laserToolhead.getModuleInfo().getKey();

        BaseStructure laserRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("focalLength", new FloatProp());
            }
        };
        laserRequest.getProp("key").setValue(key);
        laserRequest.getProp("focalLength").setValue(focalLength);
        return mConnectionController.request(0x12, 0x04, laserRequest, new ResponseStructure())
                .flatMap(response -> laserToolhead.requestInfo());
    }

    public Observable<ResponseStructure> setPlatformHeight(float height) {
        return setPlatformHeight(0, height);
    }

    public Observable<ResponseStructure> setPlatformHeight(int index, float height) {
        LaserToolhead laserToolhead = mToolheads.get(index);
        if (laserToolhead == null) {
            throw new IllegalStateException("There's no laser toolhead matched!");
        }
        int key = laserToolhead.getModuleInfo().getKey();
        BaseStructure structure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("platformHeight", new FloatProp());
            }
        };
        structure.getProp("key").setValue(key);
        structure.getProp("platformHeight").setValue(height);

        return mConnectionController.request(0x12, 0x08, structure, new ResponseStructure<>())
                .flatMap(response -> laserToolhead.requestInfo());
    }

    public Observable<ResponseStructure> setAxisCenterHeight(float height) {
        return setAxisCenterHeight(0, height);
    }

    public Observable<ResponseStructure> setAxisCenterHeight(int index, float height) {
        LaserToolhead laserToolhead = mToolheads.get(index);
        if (laserToolhead == null) {
            throw new IllegalStateException("There's no laser toolhead matched!");
        }
        int key = laserToolhead.getModuleInfo().getKey();
        BaseStructure structure = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("axisCenterHeight", new FloatProp());
            }
        };
        structure.getProp("key").setValue(key);
        structure.getProp("axisCenterHeight").setValue(height);

        return mConnectionController.request(0x12, 0x09, structure, new ResponseStructure<>())
                .flatMap(response -> laserToolhead.requestInfo());
    }

    public Observable<ResponseStructure> setTemperatureThreshold(int recoveryTemperature, int protectTemperature) {
        return setTemperatureThreshold(0, recoveryTemperature, protectTemperature);
    }

    public Observable<ResponseStructure> setTemperatureThreshold(int index, int recoveryTemperature, int protectTemperature) {
        LaserToolhead laserToolhead = mToolheads.get(index);
        if (laserToolhead == null)
            // TODO:no module
            return Observable.just(new ResponseStructure());
        int key = laserToolhead.getModuleInfo().getKey();

        // TODO: Check whether it is 10w

        BaseStructure laserRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("key", new UInt8Prop());
                addProp("recoveryTemperature", new UInt8Prop());
                addProp("protectTemperature", new UInt8Prop());
            }
        };
        laserRequest.getProp("key").setValue(key);
        laserRequest.getProp("recoveryTemperature").setValue(recoveryTemperature);
        laserRequest.getProp("protectTemperature").setValue(protectTemperature);
        return mConnectionController.request(0x12, 0x05, laserRequest, new ResponseStructure());
    }

    public Observable<ResponseStructure> subscribeLaserTubeStatus() {
        Logger.d("Subscribe laser tube...");
        SubscribeStructure subscribeStructure = new SubscribeStructure(0x12, 0xa1, 1000);
        return mConnectionController.request(0x01, 0x00, subscribeStructure, new ResponseStructure());
    }

    public Observable<ResponseStructure> unSubscribeLaserTubeStatus() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0x12, 0xa1, 1000);
        return mConnectionController.request(0x01, 0x00, subscribeStructure, new ResponseStructure());
    }

    public Observable<ResponseStructure> subscribeLaserSafetyState() {
        Logger.d("Subscribe laser safety...");
        SubscribeStructure subscribeStructure = new SubscribeStructure(0x12, 0xa0, 1000);
        return mConnectionController.request(0x01, 0x00, subscribeStructure, new ResponseStructure());
    }

    public Observable<ResponseStructure> unSubscribeLaserSafetyState() {
        SubscribeStructure subscribeStructure = new SubscribeStructure(0x12, 0xa0, 1000);
        return mConnectionController.request(0x01, 0x00, subscribeStructure, new ResponseStructure());
    }

    public Observable<Integer> startLaserFocusCalibration(float x, float y, float z) {
        mStartLaserFocalCalibrationSubject = SingleSubject.create();
        BaseStructure laserRequest = new BaseStructure() {
            @Override
            protected void init() {
                addProp("X", new FloatProp());
                addProp("Y", new FloatProp());
                addProp("Z", new FloatProp());
            }
        };
        laserRequest.getProp("X").setValue(x);
        laserRequest.getProp("Y").setValue(y);
        laserRequest.getProp("Z").setValue(z);
        return mConnectionController.request(0xa8, 0x00, laserRequest, new ResponseStructure())
                .flatMap(response -> {
                    if (response.isSuccess()) {
                        return mStartLaserFocalCalibrationSubject.toObservable();
                    } else {
                        throw new IllegalStateException("Machine start calibration fail!");
                    }
                });
    }

    public void onGetStartLaserFocusCalibrationResult(int cmdSet, int cmdId, int sequence, int result) {
        mStartLaserFocalCalibrationSubject.onSuccess(result);
        mConnectionController.sendResponse(cmdSet, cmdId, sequence, new UInt8Prop(0));
    }

    public Observable<Integer> startLaserFineTune(float laserTestPatternZDiff) {
        mStartFineTuneSubject = SingleSubject.create();
        return mConnectionController.request(0xa8, 0x04, new FloatProp(laserTestPatternZDiff), new ResponseStructure())
                .flatMap(response -> {
                    if (response.isSuccess()) {
                        return mStartFineTuneSubject.toObservable();
                    } else {
                        throw new IllegalStateException("Machine start fine tune fail!");
                    }
                });
    }

    public void onGetStartFineTuneResult(int cmdSet, int cmdId, int sequence, int result) {
        mStartFineTuneSubject.onSuccess(result);
        mConnectionController.sendResponse(cmdSet, cmdId, sequence, new UInt8Prop(0));
    }

    public Observable<ResponseStructure> setCalibrationMode(int laserCalibrationMode) {
        return mConnectionController.request(0xa8, 0x02, new UInt8Prop(laserCalibrationMode), new ResponseStructure());
    }

    public Observable<ResponseStructure> exitCalibration(boolean isSave) {
        return mConnectionController.request(0xa8, 0x03, new BoolProp(isSave), new ResponseStructure());
    }

    public Observable<Integer> startLaserFineTuneLocally(LaserPattern laserPattern) {
        LaserFineTuneExecutor fineTuneExecutor = new LaserFineTuneExecutor(mDisposables);
        fineTuneExecutor.setLaserPattern(laserPattern);
        return fineTuneExecutor.startFineTune().toObservable().flatMap(success -> Observable.just(0));
    }

    public void reset() {
        mToolheads.clear();
    }
}
