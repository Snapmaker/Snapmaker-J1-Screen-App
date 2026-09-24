package fabscreen.features.print.a400platform.viewmodel;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.Constants;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.print.IPrintWorkspace;
import fabscreen.platform.base.model.ModelBoundary;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.entity.Module;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class PrintReadyViewModel extends BaseViewModel {

    public final IMachine mMachine;
    private final BehaviorSubject<Boolean> mIsMovingSubject = BehaviorSubject.create();
    private final PublishSubject<Float> mThicknessMeasureSubject = PublishSubject.create();
    private final ModelBoundary mBoundary;
    private int mPrepareMode = 0;
    private float mMaterialThickness = 0.5f;
    private float mAutothickness = 0.5f;
    private Vector mMachineVector = new Vector();

    public PrintReadyViewModel() {
        mMachine = getServiceContainer().getService(IMachine.class);
        mBoundary = getServiceContainer().getService(IPrintWorkspace.class).getModelBoundary();
        mPrepareMode = getServiceContainer().getService(IPreferences.class).getHelper().getLaserPrintZOriginModel();
        if (mMachine.getMachineInfoSubjectHolder().getValue().workType == IMachine.WorkType.LASER) {
            float laserFocalLength = mMachine.getLaserController().getLaserToolhead().getLaserToolHeadInfoValue().getLaserFocalLength();
            Logger.d("print vm, focal len is: %f", laserFocalLength);
        }
    }

    public Observable<Boolean> runBoundary(boolean currentAsOrigin) {
        Observable<Boolean> observable;
        // If need to set current position as xy origin, set it first.
        if (currentAsOrigin) {
            observable = setXYZOrigin();
        } else {
            observable = Observable.just(true);
        }
        return observable.flatMap(success -> mMachine.getMachineController().goToBoundaryVertex(0, mBoundary, 1800))
                .flatMap(response -> Observable.just(response.isSuccess()));
    }

    public Observable<Boolean> setXYOrigin() {
        Vector vector = new Vector();
        vector.setX(0);
        vector.setY(0);
        return mMachine.getMachineController().setWorkOrigin(vector)
                .flatMap(structure -> Observable.just(structure.isSuccess()));
    }

    public Observable<Boolean> setXYZOrigin() {
        Vector vector = new Vector();
        vector.setX(0);
        vector.setY(0);
        vector.setZ(0);
        return mMachine.getMachineController().setWorkOrigin(vector)
                .flatMap(structure -> Observable.just(structure.isSuccess()));
    }

    public int getPrepareMode() {
        return mPrepareMode;
    }

    public void setPrepareMode(int prepareMode) {
        mPrepareMode = prepareMode;
    }

    public void saveMaterialThickness(float thickness) {
        mMaterialThickness = thickness;
        Logger.d("material thickness is: %s", mMaterialThickness);
    }

    public IMachine.WorkType getWorkType() {
        return mMachine.getMachineInfoSubjectHolder().getValue().workType;
    }


    public Observable<Boolean> getMovingObservable() {
        return mIsMovingSubject.hide();
    }

    public Observable<Boolean> moveToZ() {
        Observable<Boolean> returnObservable = Observable.just(false);
        mIsMovingSubject.onNext(true);
        switch (mPrepareMode) {
            case 0:
                returnObservable = autoThicknessMeasureToZ();
                break;
            case 1:
                returnObservable = mMachine.getMachineController().updateCoordinateSystem(0)
                        .flatMap(status -> mMachine.getMachineController().getCurrentCoordinateObservable())
                        .flatMap(vector1 -> {
                            mMachine.getMachineController().updateCoordinateSystem(1);
                            return Observable.just(vector1);
                        })
                        .flatMap(vector2 -> {
                            float laserFocalLength = mMachine.getLaserController().getLaserToolhead().getLaserToolHeadInfoValue().getLaserFocalLength();
                            float platformHeight = mMachine.getLaserController().getLaserToolhead().getLaserToolHeadInfoValue().getPlatformHeight();
                            Vector vector = new Vector();
                            vector.setZ(vector2.getZ() - laserFocalLength - platformHeight - mMaterialThickness);
                            return mMachine.getMachineController().setWorkOrigin(vector);
                        })
                        .flatMap(structure -> {
                            Vector vector1 = new Vector();
                            vector1.setZ(0);
                            return mMachine.getMachineController().gotoAbsolutePosition(vector1);
                        })
                        .flatMap(structure -> Observable.just(structure.isSuccess()));
                break;
            case 2:
                Vector vector = new Vector();
                vector.setZ(-10);
                returnObservable = mMachine.getMachineController().setWorkOrigin(vector)
                        .flatMap(structure -> {
                            Vector vector1 = new Vector();
                            vector1.setZ(0);
                            return mMachine.getMachineController().gotoAbsolutePosition(vector1);
                        })
                        .flatMap(structure -> Observable.just(structure.isSuccess()));
                break;
            case 3:
                Vector vector1 = new Vector();
                vector1.setZ(0);
                returnObservable = mMachine.getMachineController().setWorkOrigin(vector1)
                        .flatMap(response -> Observable.just(response.isSuccess()));
                break;
            default:
        }
        return returnObservable
                .doOnNext(aBoolean -> mIsMovingSubject.onNext(false))
                .doOnError(throwable -> mIsMovingSubject.onNext(false));
    }

    public Observable<Boolean> autoThicknessMeasureToZ() {
        mAutothickness = -200;
        mIsMovingSubject.onNext(true);
        float initX = mMachine.getMachineInfoSubjectHolder().getValue().size.getX() * 0.5f - Constants.LASER_CAMERA_OFFSET_X + Constants.LASER_MEASURE_OFFSET_X;
        float initY = mMachine.getMachineInfoSubjectHolder().getValue().size.getY() * 0.5f - Constants.LASER_CAMERA_OFFSET_Y;
        //Measure height.
        float initZ = 170f;
        return ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getAutothickness(initX, initY, initZ, 0)
                .flatMap(autothickness -> {
                    mAutothickness = autothickness;
                    mThicknessMeasureSubject.onNext(mAutothickness);
                    return mAutothickness != -200 ? mMachine.getMachineController().updateCoordinateSystem(0).flatMap(machineStatus -> Observable.just(true)) : Observable.just(false);
                })
                .flatMap(success -> success ? mMachine.getMachineController().getCurrentCoordinateObservable().flatMap(vector1 -> {
                    mMachineVector = vector1;
                    return Observable.just(true);
                }) : Observable.just(success))
                .flatMap(success -> success ? mMachine.getMachineController().updateCoordinateSystem(1).flatMap(machineStatus -> Observable.just(true)) : Observable.just(success))
                .flatMap(success -> {
                    if (success) {
                        if (mAutothickness == -200) return Observable.just(false);
                        float laserFocalLength = mMachine.getLaserController().getLaserToolhead().getLaserToolHeadInfoValue().getLaserFocalLength();
                        float platformHeight = mMachine.getLaserController().getLaserToolhead().getLaserToolHeadInfoValue().getPlatformHeight();
                        Vector vector3 = new Vector();
                        vector3.setZ(mMachineVector.getZ() - laserFocalLength - platformHeight - mAutothickness);
                        return mMachine.getMachineController().setWorkOrigin(vector3).flatMap(machineStatus -> Observable.just(true));
                    } else {
                        return Observable.just(false);
                    }
                })
                .flatMap(success -> {
                    if (success) {
                        Vector vector1 = new Vector();
                        vector1.setZ(0);
                        return mMachine.getMachineController().gotoAbsolutePosition(vector1).flatMap(machineStatus -> Observable.just(true));
                    } else {
                        return Observable.just(success);
                    }
                });
    }

    public void updateMode() {
        mPrepareMode = getServiceContainer().getService(IPreferences.class).getHelper().getLaserPrintZOriginModel();
    }

    public Observable<Boolean> checkHome() {
        IMachine service = ServiceContainer.getInstance().getService(IMachine.class);
        if (!service.getMachineStatusSubjectHolder().getValue().isHomed) {
            mIsMovingSubject.onNext(true);
            return service.getMachineController().updateCoordinateSystem(0)
                    .flatMap(machineStatus -> service.getMachineController().home(0))
                    .flatMap(machineStatus -> moveLaserReadyPosition())
                    .flatMap(integer -> service.getMachineController().updateCoordinateSystem(1))
                    .flatMap(machineStatus -> Observable.just(machineStatus.isHomed))
                    .doOnNext(aBoolean -> mIsMovingSubject.onNext(false));
        } else if (mPrepareMode == 0) {
            mIsMovingSubject.onNext(true);
            Vector vector = new Vector();
            vector.setX(mMachine.getMachineInfoSubjectHolder().getValue().size.getX() * 0.5f - Constants.LASER_CAMERA_OFFSET_X + Constants.LASER_MEASURE_OFFSET_X);
            vector.setY(mMachine.getMachineInfoSubjectHolder().getValue().size.getY() * 0.5f - Constants.LASER_CAMERA_OFFSET_Y);
            vector.setZ(170f);
            return service.getMachineController().updateCoordinateSystem(0)
                    .flatMap(machineStatus -> service.getMachineController().gotoAbsolutePosition(vector, 6000))
                    .flatMap(integer -> service.getMachineController().updateCoordinateSystem(1))
                    .flatMap(machineStatus -> Observable.just(machineStatus.isHomed))
                    .doOnNext(aBoolean -> mIsMovingSubject.onNext(false));
        } else {
            return Observable.just(true);
        }
    }

    public Observable<Float> getThicknessMeasure() {
        return mThicknessMeasureSubject.hide();
    }

    public boolean getIsRotaryAvailable() {
        return mMachine.getMachineInfoSubjectHolder().getValue().isRotaryAvailable;
    }

    public void setOriginIndicatorState(boolean b) {
        int headType = mMachine.getMachineInfoSubjectHolder().getValue().headType;
        float power = headType == Module.ModuleType.HEAD_LASER_10W ? 0.5f : 1f;
        mMachine.getLaserController().setLaserPower(0, b ? power : 0f).as(bindToLifecycle()).subscribe(responseStructure -> {
            if (!responseStructure.isSuccess()) Logger.d(responseStructure);
        }, LogHelper::log);
    }

    public Observable<ResponseStructure> moveLaserReadyPosition() {
        float initX = mMachine.getMachineInfoSubjectHolder().getValue().size.getX() * 0.5f;
        float initY = mMachine.getMachineInfoSubjectHolder().getValue().size.getY() * 0.5f;
        float initZ = mMachine.getMachineInfoSubjectHolder().getValue().size.getZ();
        if (mPrepareMode == 0) {
            initX = mMachine.getMachineInfoSubjectHolder().getValue().size.getX() * 0.5f - Constants.LASER_CAMERA_OFFSET_X + Constants.LASER_MEASURE_OFFSET_X;
            initY = mMachine.getMachineInfoSubjectHolder().getValue().size.getY() * 0.5f - Constants.LASER_CAMERA_OFFSET_Y;
            initZ = 170f;
        }
        Vector vector = new Vector();
        vector.setX(initX);
        vector.setY(initY);
        vector.setZ(initZ);
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().gotoAbsolutePosition(vector, 6000);
    }
}
