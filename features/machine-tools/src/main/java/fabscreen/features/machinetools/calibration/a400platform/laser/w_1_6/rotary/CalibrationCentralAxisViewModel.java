package fabscreen.features.machinetools.calibration.a400platform.laser.w_1_6.rotary;

import static fabscreen.platform.core.ui.data.MoveController.getInstance;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.MockConst;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.controller.LaserController;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.data.MoveController;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class CalibrationCentralAxisViewModel extends BaseViewModel {
    private final BehaviorSubject<Boolean> mIsMovingSubject = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> mMovingSubject = BehaviorSubject.createDefault(false);
    private final float[] mLinearStepWidths = {0.1f, 1f, 10f, 100f};
    private final float[] mRotaryStepWidths = {0.2f, 1f, 10f, 90f};
    private MachineController mMachineController;
    private float mMaterialSurfaceZ;
    private float mMaterialDiameter;
    private IMachine machine;
    private LaserController mLaserController;
    private int mStepWidthPos;

    public CalibrationCentralAxisViewModel() {
        machine = getServiceContainer().getService(IMachine.class);
        mMachineController = machine.getMachineController();
        mLaserController = machine.getLaserController();
        mStepWidthPos = 1;
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


    public Observable<Boolean> checkHome() {
        IMachine service = ServiceContainer.getInstance().getService(IMachine.class);
        if (!service.getMachineStatusSubjectHolder().getValue().isHomed) {
            mIsMovingSubject.onNext(true);
            return service.getMachineController().updateCoordinateSystem(0)
                    .flatMap(machineStatus -> service.getMachineController().home(0))
                    .flatMap(integer -> Observable.just(integer == 0));
        } else {
            return service.getMachineController().updateCoordinateSystem(0)
                    .flatMap(machineStatus -> Observable.just(machineStatus.isHomed));
        }
    }

    /**
     * x mid
     * y mid + 100mm
     * z 100mm + diameter/2 + rotary axis default height
     */
    public Observable<Boolean> goToRotaryTouchInitPosition() {
        float platformHeight = mLaserController.getLaserToolHeadInfoValue().getPlatformHeight();
        Vector size = machine.getMachineInfoSubjectHolder().getValue().size;
        float x = size.getX() / 2;
        float y = size.getY() / 2 + 100;
        float z = 100 + mMaterialDiameter / 2 + MockConst.LASER_MOCK_ROTARY_HEIGHT + platformHeight;
        Vector vector = new Vector();
        vector.setX(x);
        vector.setY(y);
        vector.setZ(z);
        return mMachineController
                .updateCoordinateSystem(0)
                .doOnSubscribe(disposable -> mIsMovingSubject.onNext(true))
                .flatMap(machineStatus -> mMachineController.homeIfNotYet(0))
                .flatMap(status -> mMachineController.gotoAbsolutePosition(vector))
                .flatMap(response -> mMachineController.updateCoordinateSystem(1))
                .flatMap(machineStatus -> Observable.just(machineStatus.coordinateID == 1))
                .doOnNext(response -> mIsMovingSubject.onNext(false))
                .doOnError(e -> mIsMovingSubject.onNext(false));
    }

    public Observable<ResponseStructure> moveToPosition(MoveController.Direction direction) {
        if (direction == null) {
            ResponseStructure<IStructure> iStructureResponseStructure = new ResponseStructure<>();
            iStructureResponseStructure.resultProp.setValue(1);
            return Observable.just(iStructureResponseStructure);
        }

        float stepWidth = MoveController.Direction.isRotary(direction) ? mRotaryStepWidths[mStepWidthPos] : mLinearStepWidths[mStepWidthPos];
        mMovingSubject.onNext(true);
        return getInstance()
                .stepToPosition(direction, stepWidth)
                .doOnNext(response -> mMovingSubject.onNext(false))
                .doOnError(e -> mMovingSubject.onNext(false));
    }

    public void Lift100Z() {
        getInstance().stepToPosition(MoveController.Direction.UP, 100)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                }, LogHelper::log);
    }


    public void changeStepWidth(int position) {
        mStepWidthPos = position;
    }

    public Observable<Boolean> getIsMovingSubject() {
        return mIsMovingSubject.hide();
    }

    public Observable<Boolean> getMovingSubject() {
        return mMovingSubject.hide();
    }
}
