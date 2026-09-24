package fabscreen.features.print.s20.preview;

import static fabscreen.platform.base.legacy.connection.MockConst.CAMERA_HEIGHT_OFFSET;
import static fabscreen.platform.base.legacy.connection.MockConst.H1_Z_POSITION;
import static fabscreen.platform.base.legacy.connection.MockConst.H2_Z_POSITION;

import android.graphics.Bitmap;

import com.orhanobut.logger.Logger;

import java.io.FileOutputStream;
import java.util.Locale;

import fabscreen.platform.base.Constants;
import fabscreen.platform.base.data.imgprocess.LaserDistanceMeasureProcess;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.MockConst;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.data.MoveController;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class PreviewViewModel extends BaseViewModel {

    private final float mH1;
    private final float mH2;
    private float mS1plus;
    private float mS2plus;
    private float mSxplus;

    private Subject<Boolean> mMeasureResultSubject = PublishSubject.create();
    private Subject<Boolean> mIsMovingSubject = BehaviorSubject.create();
    private Subject<Boolean> mCameraMoveSubject = PublishSubject.create();
    private Subject<Boolean> mResultBackSubject = PublishSubject.create();

    private float mMeasuredThickness = 0f;

    public PreviewViewModel() {
        super();
        mH1 = H1_Z_POSITION + CAMERA_HEIGHT_OFFSET;
        mH2 = H2_Z_POSITION + CAMERA_HEIGHT_OFFSET;
        mS1plus = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserThicknessS1Plus();
        mS2plus = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserThicknessS2Plus();
    }

    /**
     * Capture photo and calculate the result based on the photo.
     * 1. Set camera expose time to 1;
     * 2. Request capture and receive photo;
     * 3. Process photo, save params, calculate thickness;
     * 4. Restore expose time to default(0).
     */
    public void autoMeasureMaterialThickness() {
        ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().setExposeTime(1)
                .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().requestCapturePhoto())
                .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().watchPhotoReceive())
                .doOnNext(bitmap -> {
                    FileOutputStream out = new FileOutputStream(ServiceContainer.getInstance().getService(IAppService.class).getCacheDir() + "/distance.jpg");
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    float distance = LaserDistanceMeasureProcess.process(bitmap);
                    if (distance < -200) {
                        mMeasureResultSubject.onNext(false);
                        return;
                    }
                    mSxplus = distance;
                    Logger.i(">>> distance is %s <<<", mSxplus);
                    float thickness = calculateResult();
                    save10wMeasuredThickness(thickness);
                    mMeasureResultSubject.onNext(thickness > 0);
                })
                .doOnError(e -> mMeasureResultSubject.onNext(false))
                .flatMap(bitmap -> ServiceContainer.getInstance().getService(IMachine.class).getLaserController().getLaserCameraController().setExposeTime(0))
                .subscribeOn(Schedulers.computation())
                .as(bindToLifecycle())
                .subscribe(success -> {
                }, LogHelper::log);
    }

    public int getHeadType() {
        return 0;//ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().workType;
    }

    public Observable<Boolean> getMeasureResultObservable() {
        return mMeasureResultSubject.hide();
    }

    public void moveXYZByStep(MoveController.Direction direction, float stepWidth) {
        mIsMovingSubject.onNext(true);
        MoveController.getInstance()
                .moveByStep(direction, stepWidth)
                .as(bindToLifecycle())
                .subscribe(response -> mIsMovingSubject.onNext(false));
    }

    public String getMeasuredThicknessString() {
        return String.format(Locale.ENGLISH, "%.2f", mMeasuredThickness);
    }

    public float getMeasuredThicknessByTouch() {
        float laserPlatformZ = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserPlatformZ();
        MachineStatus status = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue();
        float materialSurfaceZ = status.currentPosition.getZ();
        return Math.abs(materialSurfaceZ - laserPlatformZ);
    }

    public void save10wMeasuredThickness(float thickness) {
        mMeasuredThickness = thickness;
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLaserMaterialThickness(thickness);
    }

    public Observable<Boolean> getIsMovingObservable() {
        return mIsMovingSubject.hide();
    }

    public void initCameraPosition(boolean isBack) {
        mIsMovingSubject.onNext(true);
        float initX = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getX() * 0.5f - Constants.LASER_CAMERA_OFFSET_X + Constants.LASER_MEASURE_OFFSET_X;
        float initY = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getY() * 0.5f - Constants.LASER_CAMERA_OFFSET_Y;
        //Measure height.
        float initZ = 170f;
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .flatMap(success -> {
                    Vector vector = new Vector();
                    vector.setX(initX);
                    vector.setY(initY);
                    vector.setZ(initZ);
                    return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().gotoAbsolutePosition(vector);
                })
                .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1))
                .as(bindToLifecycle())
                .subscribe(coordinateSystem -> {
                    if (isBack) {
                        mIsMovingSubject.onNext(false);
                        mResultBackSubject.onNext(true);
                    } else {
                        mIsMovingSubject.onNext(false);
                        mCameraMoveSubject.onNext(true);
                    }
                });
    }

    public Observable<Boolean> getCameraMoveObservable() {
        return mCameraMoveSubject.hide();
    }

    public Observable<Boolean> getResultBackObservable() {
        return mResultBackSubject.hide();
    }

    private float calculateResult() {
        float h3 = mH1 - mH2;
        return mH1 - (mH1 * ((h3 * mS1plus) + ((mS2plus * mH2) - (mS1plus * mH1))) / (h3 * mSxplus + ((mS2plus * mH2) - (mS1plus * mH1)))) + MockConst.LASER_MATERIAL_MEASURE_CALIBRATION_OBJECT_HEIGHT;
    }

    /**
     * Up/Down lift toolhead to match the focal length and the material thickness.
     */
    public Observable<Boolean> liftToolhead(boolean isAutoMode) {

        float laserPlatformZ = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getLaserPlatformZ();
        float offsetZ = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getZ();

        MachineStatus status = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue();
        float targetX;
        float targetY;
        float targetZ;
        if (isAutoMode) {
            // Auto mode moved to center position
            targetX = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getX() * 0.5f;
            targetY = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getY() * 0.5f;
            targetZ = mMeasuredThickness + Constants.LASER_10W_CAMERA_FOCAL_LENGTH + laserPlatformZ;
        } else {
            // Using current X Y Position
            targetX = status.currentPosition.getX() - ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getX();
            targetY = status.currentPosition.getY() - ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getY();
            targetZ = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().currentPosition.getZ() - offsetZ + Constants.LASER_10W_CAMERA_FOCAL_LENGTH;
        }

        Logger.d("lift to target z %s", targetZ);

        return ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().getMachineController()
                .updateCoordinateSystem(0)
                .flatMap(coordinateSystem -> {
                    Vector vector = new Vector();
                    vector.setX(targetX);
                    vector.setY(targetY);
                    vector.setZ(targetZ);
                    return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().gotoAbsolutePosition(vector);
                })
                .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1))
                .flatMap(coordinateSystem -> Observable.just(true));
    }

    public void switchAFAssistLight(boolean on) {
        ServiceContainer.getInstance().getService(IMachine.class).getLaserController().switchFocusAssistLight(on ? 1 : 0)
                .as(bindToLifecycle())
                .subscribe(success -> {
                    // do nothing
                }, LogHelper::log);
    }

    public void savePlatformZOffset() {
        MachineStatus status = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue();
        float offsetZ = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getZ();
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLaserPlatformZ(status.currentPosition.getZ() - offsetZ);
    }

    public Observable<Boolean> upLiftToolhead() {
        mIsMovingSubject.onNext(true);
        return MoveController.getInstance()
                .moveByStep(MoveController.Direction.UP, Constants.LASER_10W_CAMERA_FOCAL_LENGTH)
                .flatMap(response -> Observable.just(true))
                .doOnNext(success -> mIsMovingSubject.onNext(false));
    }

    public void initPosition() {
        mIsMovingSubject.onNext(true);
        float initX = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getX() * 0.5f;
        float initY = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getY() * 0.5f;
        float initZ = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getZ();
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .flatMap(success -> {
                    Logger.d("gotoAbsolutePosition: x:%s,y:%s,z:%s", initX, initY, initZ);
                    Vector vector = new Vector();
                    vector.setX(initX);
                    vector.setY(initY);
                    vector.setZ(initZ);
                    return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().gotoAbsolutePosition(vector);
                })
                .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1))
                .as(bindToLifecycle())
                .subscribe(coordinateSystem -> {
                    mIsMovingSubject.onNext(false);
                });
    }
}
