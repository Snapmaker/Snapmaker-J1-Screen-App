package fabscreen.features.machinetools.setup.singledual.blocksetup;

import androidx.annotation.IntDef;

import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class BlockSetupViewModel extends BaseViewModel {
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    private final Vector mMachineSize;
    private final IMachine mMachine;
    private final BehaviorSubject<Boolean> mIsMovingSubject = BehaviorSubject.create();

    @IntDef({LEFT, RIGHT})
    public @interface LeftOrRight {
    }

    private final MachineController mMachineController;

    public BlockSetupViewModel() {
        mMachine = getServiceContainer().getService(IMachine.class);
        mMachineController = mMachine.getMachineController();
        mMachineSize = mMachine.getMachineInfoSubjectHolder().getValue().size;

        mMachineController.home(0)
                .flatMap(result -> {
                    if (result == 0) {
                        return mMachineController.updateCoordinateSystem(0);
                    } else {
                        throw new IllegalStateException("Home fail!");
                    }
                })
                .doOnSubscribe(disposable -> mIsMovingSubject.onNext(true))
                .doOnError(error -> mIsMovingSubject.onNext(false))
                .doOnNext(result -> mIsMovingSubject.onNext(false))
                .as(bindToLifecycle())
                .subscribe(status -> {
                }, LogHelper::log);
    }

    public Observable<Boolean> goToInstallPosition(@LeftOrRight int position) {
        Vector vector = new Vector();
        vector.setX(position == LEFT ? -17.5f : 392.5f);
        // FDM has no concerns on g53 or g54.
        return mMachineController.gotoAbsolutePosition(vector)
                .flatMap(response -> Observable.just(response.isSuccess()))
                .doOnSubscribe(disposable -> mIsMovingSubject.onNext(true))
                .doOnNext(success -> mIsMovingSubject.onNext(false))
                .doOnError(error -> mIsMovingSubject.onNext(false));
    }

    /**
     * go to mid -> check left -> check right -> result
     */
    public Observable<Boolean> checkBlockInstall() {
        float mid = mMachineSize.getX();
        Vector vector = new Vector();
        vector.setX(mid);
        // FIXME: 2022/5/20 invalid check!
        return mMachineController.gotoAbsolutePosition(vector)
                .flatMap(response -> {
                    if (response.isSuccess()) {
                        return Observable.just(true);
                    } else {
                        throw new IllegalStateException("Error go to mid!");
                    }
                })
                .flatMap(success -> {
                    // Go to very left to trigger extruder switch
                    vector.setX(-17.5f);
                    return mMachineController.gotoAbsolutePosition(vector);
                })
                .flatMap(response -> {
                    if (response.isSuccess()) {
                        return checkExtruderActive(LEFT);
                    } else {
                        throw new IllegalStateException("Error go to very left!");
                    }
                })
                .flatMap(success -> {
                    // Go to very right to trigger extruder switch
                    vector.setX(392.5f);
                    return mMachineController.gotoAbsolutePosition(vector);
                })
                .flatMap(response -> {
                    if (response.isSuccess()) {
                        return checkExtruderActive(RIGHT);
                    } else {
                        throw new IllegalStateException("Error go to very right!");
                    }
                })
                .doOnSubscribe(disposable -> mIsMovingSubject.onNext(true))
                .doOnNext(success -> mIsMovingSubject.onNext(false))
                .doOnError(error -> mIsMovingSubject.onNext(false));
    }

    /**
     * Request toolhead info -> get extruder info -> check extruder state.
     *
     * @param leftOrRight to check the left or right extruder.
     * @return check result in Observable.
     */
    private Observable<Boolean> checkExtruderActive(@LeftOrRight int leftOrRight) {
        return mMachine.getFDMController().getToolheadInfoObservable(0)
                .flatMap(status -> {
                    if (status.isSuccess()) {
                        return Observable.just(status.dataProp.getExtruderList().get(leftOrRight).getState() == 1);
                    } else {
                        throw new IllegalStateException("Cannot fetch toolhead info.");
                    }
                });
    }

    public Observable<Boolean> getIsMovingObservable() {
        return mIsMovingSubject.hide();
    }
}
