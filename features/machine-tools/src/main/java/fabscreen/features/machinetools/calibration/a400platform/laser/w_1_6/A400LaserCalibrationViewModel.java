package fabscreen.features.machinetools.calibration.a400platform.laser.w_1_6;

import java.util.ArrayList;
import java.util.List;

import fabscreen.platform.base.legacy.connection.MockConst;
import fabscreen.platform.base.lib.parser.Position;
import fabscreen.platform.base.model.LaserPattern;
import fabscreen.platform.base.model.ModelBoundary;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.controller.LaserController;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.data.MoveController;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class A400LaserCalibrationViewModel extends BaseViewModel {

    private MachineInfo mMachineInfo;
    private MachineController mMachineController;
    private final float[] mStepWidths = {0.1f, 1f, 10f, 100f};
    private int mWidthPos;
    private ModelBoundary mBoundary;

    private float mMaterialSurfaceZ;
    private float mFocalLength;
    private float mZWorkOriginAbsolute;
    private LaserController mLaserController;

    private final BehaviorSubject<Boolean> mIsMovingSubject = BehaviorSubject.create();
    private float mMaterialDiameter;
    private LaserPattern mLaserPattern;

    public A400LaserCalibrationViewModel() {
        // Get machine control.
        initData();

        // Init step width.
        changeStepWidth(1);
    }

    private void initData() {
        IMachine machine = getServiceContainer().getService(IMachine.class);
        mMachineInfo = machine.getMachineInfoSubjectHolder().getValue();
        mMachineController = machine.getMachineController();
        mLaserController = machine.getLaserController();
        mFocalLength = mLaserController.getLaserToolhead().getLaserToolHeadInfoValue().getLaserFocalLength();
        // mFocalLength = 6f; // We trust machine for this value.
    }

    private Observable<Integer> ensureCoordinateAndHome() {
        int requiredSystemIndex = mMachineInfo.workType == IMachine.WorkType.FDM ? 0 : 1;
        return mMachineController.updateCoordinateSystemIfNot(requiredSystemIndex)
                .flatMap(status -> mMachineController.homeIfNotYet(0))
                .doOnSubscribe(disposable -> mIsMovingSubject.onNext(true))
                .doOnNext(result -> mIsMovingSubject.onNext(false))
                .doOnError(e -> mIsMovingSubject.onNext(false));
    }

    public void changeStepWidth(int pos) {
        if (pos < 0 || pos > mStepWidths.length - 1) return;
        mWidthPos = pos;
    }

    public void moveByStep(MoveController.Direction direction) {
        MoveController.getInstance().stepToPosition(direction, mStepWidths[mWidthPos])
                .doOnSubscribe(disposable -> mIsMovingSubject.onNext(true))
                .doOnNext(response -> mIsMovingSubject.onNext(false))
                .doOnError(e -> mIsMovingSubject.onNext(false))
                .as(bindToLifecycle())
                .subscribe(response -> {
                }, LogHelper::log);
    }

    public int getStepWidthPos() {
        return mWidthPos;
    }

    public void runBoundary() {
        Vector origin = new Vector();
        origin.setX(0);
        origin.setY(0);
        mMachineController.setWorkOrigin(origin)
                .flatMap(response -> mMachineController.goToBoundaryVertex(0, mBoundary, 1800))
                .doOnSubscribe(disposable -> mIsMovingSubject.onNext(true))
                .doOnNext(response -> mIsMovingSubject.onNext(false))
                .doOnError(e -> mIsMovingSubject.onNext(false))
                .as(bindToLifecycle())
                .subscribe(response -> {
                }, LogHelper::log);
    }

    public void startCalibration() {
        Vector size = mMachineInfo.size;
        ensureCoordinateAndHome()
                .doOnSubscribe(disposable -> mIsMovingSubject.onNext(true))
                .flatMap(result -> mLaserController.startLaserFocusCalibration(size.getX() / 2, size.getY() / 2, size.getZ() / 2))
                .doOnNext(status -> mIsMovingSubject.onNext(false))
                .doOnError(e -> mIsMovingSubject.onNext(false))
                .as(bindToLifecycle())
                .subscribe(status -> {
                }, LogHelper::log);
    }


    /**
     * Save z coordinate when toolhead touch material surface.
     */
    public Observable<Boolean> saveMaterialSurfaceZ() {
        return mMachineController.updateCoordinateSystem(0)
                .flatMap(status -> {
                    mMaterialSurfaceZ = status.currentPosition.getZ();
                    return mMachineController.updateCoordinateSystem(1);
                })
                .flatMap(status -> MoveController.getInstance().stepToPosition(MoveController.Direction.UP, 10))
                .flatMap(status -> Observable.just(true));
    }


    /**
     * 3 axis: Fixed boundary
     * 4 axis: boundary parsed from pattern
     *
     * @return boundary load result Observable
     */
    public Observable<Boolean> getLoadBoundaryObservable() {
        ModelBoundary boundary = new ModelBoundary();

        if (isRotaryAvailable()) {
            mLaserPattern = new LaserPattern(LaserPattern.SHAPE_RULER, LaserPattern.DIRECTION_Y, LaserPattern.ALIGNMENT_ENGRAVE_CENTER, 0.5f);
            boundary = mLaserPattern.getPatternBoundary();
        } else {
            List<Vector> vectors = getVertexVectors();
            //noinspection ConstantConditions
            if (vectors == null || vectors.size() < 4) {
                throw new IllegalArgumentException("Wrong boundary args!");
            }

            boundary.updateBoundary(new Position(vectors.get(0).getX(), vectors.get(0).getY(), 0));
            boundary.updateBoundary(new Position(vectors.get(1).getX(), vectors.get(1).getY(), 0));
            boundary.updateBoundary(new Position(vectors.get(2).getX(), vectors.get(2).getY(), 0));
            boundary.updateBoundary(new Position(vectors.get(3).getX(), vectors.get(3).getY(), 0));
        }

        mBoundary = boundary;
        return Observable.just(true);
    }

    private List<Vector> getVertexVectors() {
        List<Vector> vectors = new ArrayList<>();
        Vector vector0 = new Vector();
        Vector vector1 = new Vector();
        Vector vector2 = new Vector();
        Vector vector3 = new Vector();

        vector0.setX(-20);
        vector0.setY(-5);
        vector1.setX(-20);
        vector1.setY(5);
        vector2.setX(20);
        vector2.setY(-5);
        vector3.setX(20);
        vector3.setY(5);

        vectors.add(vector0);
        vectors.add(vector1);
        vectors.add(vector2);
        vectors.add(vector3);
        return vectors;
    }

    public Observable<Boolean> doEngraving() {
        return setZWorkOrigin()
                .flatMap(success -> mMachineController.goToOrigin())
                .flatMap(response -> {
                    if (isRotaryAvailable()) {
                        return mLaserController.startLaserFineTuneLocally(mLaserPattern);
                    } else {
                        return mLaserController.startLaserFineTune(0.5f);
                    }
                })
                .flatMap(response -> Observable.just(response == 0));
    }

    private Observable<Boolean> setZWorkOrigin() {
        return getZWorkOriginObservable()
                .flatMap(zValue -> {
                    Vector vector = new Vector();
                    vector.setZ(zValue);
                    return mMachineController.setWorkOrigin(vector);
                })
                .flatMap(response -> Observable.just(response.isSuccess()));
    }

    private Observable<Float> getZWorkOriginObservable() {
        mZWorkOriginAbsolute = mMaterialSurfaceZ + mFocalLength;

        if (mZWorkOriginAbsolute < mMaterialSurfaceZ + 5) {
            // To avoid toolhead crash(even if focal length is 0)
            mZWorkOriginAbsolute = mMaterialSurfaceZ + 5;
        }
        mZWorkOriginAbsolute += 1;

        return mMachineController
                .updateCoordinateSystem(0)
                .flatMap(status -> mMachineController.getCurrentCoordinateObservable())
                .flatMap(vector -> {
                    float zOrigin = vector.getZ() - mZWorkOriginAbsolute;
                    mMachineController.updateCoordinateSystem(1);
                    return Observable.just(zOrigin);
                });
    }

    public Observable<Boolean> setXYOrigin() {
        Vector vector = new Vector();
        vector.setX(0);
        vector.setY(0);
        return mMachineController.setWorkOrigin(vector)
                .flatMap(response -> Observable.just(response.isSuccess()));
    }

    public Observable<Boolean> saveFocalLenAndQuit(float offset) {
        return mLaserController.setFocalLength(mFocalLength + offset)
                .flatMap(response -> quitCalibration(true));
    }

    public Observable<Boolean> quitCalibration(boolean saveCalibration) {
        return mLaserController.exitCalibration(saveCalibration)
                .flatMap(response -> MoveController.getInstance().stepToPosition(MoveController.Direction.UP, 100))
                .flatMap(response -> Observable.just(response.isSuccess()));
    }

    public Observable<Boolean> getIsMovingObservable() {
        return mIsMovingSubject.hide();
    }

    public boolean isRotaryAvailable() {
        return mMachineInfo.isRotaryAvailable;
    }

    /**
     * Axis z = surfaceZ - diameter/2.
     */
    public Observable<Boolean> saveRotaryAxisZ() {
//        IPreferences.Helper prefHelper = getServiceContainer().getService(IPreferences.class).getHelper();
        return mMachineController.updateCoordinateSystem(0)
                .flatMap(status -> {
                    mMaterialSurfaceZ = status.currentPosition.getZ();
                    float z = mMaterialSurfaceZ - mMaterialDiameter / 2;
                    return mLaserController.setAxisCenterHeight(z);
                })
                .flatMap(response -> mMachineController.updateCoordinateSystem(1))
                .flatMap(status -> MoveController.getInstance().stepToPosition(MoveController.Direction.UP, 10))
                .flatMap(response -> Observable.just(response.isSuccess()));
    }

    /**
     * Set material diameter from user input.
     */
    public void setMaterialDiameter(float diameter) {
        mMaterialDiameter = diameter;
    }

    /**
     * x mid
     * y mid + 100mm
     * z 100mm + diameter/2 + rotary axis default height
     */
    public void goToRotaryTouchInitPosition() {
        float platformHeight = mLaserController.getLaserToolHeadInfoValue().getPlatformHeight();
        Vector size = mMachineInfo.size;
        float x = size.getX() / 2;
        float y = size.getY() / 2 + 100;
        float z = 100 + mMaterialDiameter / 2 + MockConst.LASER_MOCK_ROTARY_HEIGHT + platformHeight;
        Vector vector = new Vector();
        vector.setX(x);
        vector.setY(y);
        vector.setZ(z);
        mMachineController
                .updateCoordinateSystem(0)
                .doOnSubscribe(disposable -> mIsMovingSubject.onNext(true))
                .flatMap(machineStatus -> mMachineController.homeIfNotYet(0))
                .flatMap(status -> mMachineController.gotoAbsolutePosition(vector))
                .flatMap(response -> mMachineController.updateCoordinateSystem(1))
                .doOnNext(response -> mIsMovingSubject.onNext(false))
                .doOnError(e -> mIsMovingSubject.onNext(false))
                .as(bindToLifecycle())
                .subscribe(response -> {
                }, LogHelper::log);
    }
}
