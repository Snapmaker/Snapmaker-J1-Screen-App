package fabscreen.platform.core.ui.common;

import android.graphics.Bitmap;

import androidx.annotation.IntDef;

import com.orhanobut.logger.Logger;

import java.io.FileOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import fabscreen.platform.base.Constants;
import fabscreen.platform.base.data.imgprocess.LaserDistanceMeasureProcess;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.MockConst;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class Laser10wThicknessCalibrationViewModel extends BaseViewModel {

    public static final int FIRST_CAPTURE = 1;
    public static final int SECOND_CAPTURE = 2;
    public static final int MEASURE_CAPTURE = 3;
    final private static float CAMERA_HEIGHT_OFFSET = 3.9f;
    private final float mH1ZPosition = 170;
    private final float mH2ZPosition = 150;
    private final float mH1;
    private final float mH2;
    private final float mPermissibleError = 0.2f;
    private final String[] mPhotoPaths = {
            ServiceContainer.getInstance().getService(IAppService.class).getCacheDir() + "/h1.png",
            ServiceContainer.getInstance().getService(IAppService.class).getCacheDir() + "/h2.png",
            ServiceContainer.getInstance().getService(IAppService.class).getCacheDir() + "/distance.png"
    };
    private final Subject<CalibrationCaptureResult> mCaptureResultSubject = PublishSubject.create();
    private float mS1plus;
    private float mS2plus;
    private float mSxplus;
    private Disposable subscribe;

    public Laser10wThicknessCalibrationViewModel() {
        super();

        mH1 = mH1ZPosition + CAMERA_HEIGHT_OFFSET;
        mH2 = mH2ZPosition + CAMERA_HEIGHT_OFFSET;
    }

    public Observable<Boolean> initCameraPosition(@CaptureCount int which) {
        float initX = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getX() * 0.5f - Constants.LASER_CAMERA_OFFSET_X + Constants.LASER_MEASURE_OFFSET_X;
        float initY = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getY() * 0.5f - Constants.LASER_CAMERA_OFFSET_Y;
        float initZ = 0f;
        switch (which) {
            case FIRST_CAPTURE:
                initZ = mH1ZPosition;
                break;
            case SECOND_CAPTURE:
                initZ = mH2ZPosition;
                break;
            case MEASURE_CAPTURE:
                initZ = 170f; //Measure height.
                break;
        }
        float finalInitZ = initZ;
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .flatMap(result -> {
                    Logger.d("gotoAbsolutePosition: x:%s,y:%s,z:%s", initX, initY, finalInitZ);
                    Vector vector = new Vector();
                    vector.setX(initX);
                    vector.setY(initY);
                    vector.setZ(finalInitZ);
                    return result.coordinateID == 0 ? ServiceContainer.getInstance().getService(IMachine.class).getMachineController().gotoAbsolutePosition(vector) : Observable.just((new ResponseStructure()));
                })
                .flatMap(resultStructure -> resultStructure.isSuccess() ?
                        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1).flatMap(result ->
                                // FIXME: Temp chang Data
                                Observable.just(true))
                        : Observable.just(false));
    }

    /**
     * Capture photo and calculate the result based on the photo.
     * <p>
     * 1. Set camera expose time to 1;
     * 2. Request capture and receive photo;
     * 3. Process photo, save params, calculate thickness;
     * 4. Restore expose time to default(0).
     *
     * @param which which time we capture(1st time, 2nd time, etc.), count from 1.
     */
    public void captureAndCalculate(@CaptureCount int which) {
        if (subscribe != null) subscribe.dispose();
        subscribe = ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().setExposeTime(2)
                .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().requestCapturePhoto())
                .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().watchPhotoReceive())
                .doOnNext(bitmap -> {
                    FileOutputStream out = new FileOutputStream(mPhotoPaths[which - 1]);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    float distance = LaserDistanceMeasureProcess.process(bitmap);
                    Logger.i(">>> distance is %s <<<", distance);

                    CalibrationCaptureResult result = new CalibrationCaptureResult();
                    result.which = which;
                    if (distance < -200) {
                        result.isSuccess = false;
                        mCaptureResultSubject.onNext(result);
                        return;
                    }
                    Logger.i(">>> which = %d distance = %s", which, distance);
                    if (which == FIRST_CAPTURE) {
                        mS1plus = distance;
                        result.isSuccess = true;
                    } else if (which == SECOND_CAPTURE) {
                        mS2plus = distance;
                        result.isSuccess = true;
                    } else if (which == MEASURE_CAPTURE) {
                        mSxplus = distance;
                        final float calculateResult = calculateResult();
                        Logger.i(">>> calculateResult = %s", calculateResult);
                        result.isSuccess = Math.abs(calculateResult - MockConst.LASER_MATERIAL_MEASURE_CALIBRATION_OBJECT_HEIGHT) < mPermissibleError;
                        // Save calibrated params(s1', s2').
                        saveCalibratedParams();
                    }
                    mCaptureResultSubject.onNext(result);
                    subscribe.dispose();
                })
                .doOnError(e -> {
                    CalibrationCaptureResult result = new CalibrationCaptureResult();
                    result.which = which;
                    result.isSuccess = false;
                    mCaptureResultSubject.onNext(result);
                })
                .flatMap(bitmap -> ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().setExposeTime(0))
                .subscribeOn(Schedulers.computation())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    subscribe.dispose();
                }, LogHelper::log);
    }

    public Observable<CalibrationCaptureResult> getCaptureResultObservable() {
        return mCaptureResultSubject.hide();
    }

    public void saveCalibratedParams() {
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLaserParamS1Plus(mS1plus);
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLaserParamS2Plus(mS2plus);
    }

    public void switchAFAssistLight(boolean on) {
        ServiceContainer.getInstance().getService(IMachine.class).getLaserController().switchFocusAssistLight(on ? 1 : 0).as(bindToLifecycle()).subscribe();
    }

    public void exitCalibration() {
        ServiceContainer.getInstance().getService(IMachine.class).getLaserController()
                .exitCalibration(false)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    if (!success.isSuccess()) {
                        Logger.d("Exit Calibration: " + success);
                    }
                });
    }

    private float calculateResult() {
        float h3 = mH1 - mH2;
        return mH1 - (mH1 * ((h3 * mS1plus) + ((mS2plus * mH2) - (mS1plus * mH1))) / (h3 * mSxplus + ((mS2plus * mH2) - (mS1plus * mH1)))) + MockConst.LASER_MATERIAL_MEASURE_CALIBRATION_OBJECT_HEIGHT;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FIRST_CAPTURE, SECOND_CAPTURE, MEASURE_CAPTURE})
    public @interface CaptureCount {
    }
}
