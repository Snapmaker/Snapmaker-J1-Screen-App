package fabscreen.features.machinetools.calibration.a400platform.cnc.originAssistant;

import com.orhanobut.logger.Logger;

import fabscreen.platform.base.FabException;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.MockConst;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.data.MoveController;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.SingleSubject;

public class A400CNCOriginAssistantViewModel extends BaseViewModel {

    private float x1;
    private float x2;
    private float mOriginX;
    private float mOriginY;
    private float mOriginZ;
    private BehaviorSubject<AssistPhase> mAssistPhaseSubject = BehaviorSubject.createDefault(AssistPhase.NOT_START);
    // EditText input subject
    private PublishSubject<String> mBitDiameterInputSubject = PublishSubject.create();
    private PublishSubject<String> mBitLengthInputSubject = PublishSubject.create();
    private PublishSubject<String> mWorkpieceDiameterInputSubject = PublishSubject.create();
    private PublishSubject<String> mWorkpieceLengthInputSubject = PublishSubject.create();
    // value subject
    private BehaviorSubject<Float> mBitDiameterSubject = BehaviorSubject.createDefault(-1.0f);
    private BehaviorSubject<Float> mBitLengthSubject = BehaviorSubject.createDefault(-1.0f);
    private BehaviorSubject<Float> mWorkpieceDiameterSubject = BehaviorSubject.createDefault(-1.0f);
    private BehaviorSubject<Float> mWorkpieceLengthSubject = BehaviorSubject.createDefault(-1.0f);
    // tips subject
    private BehaviorSubject<OriginAssistEditTextTips> mBitDiameterTipsSubject = BehaviorSubject.createDefault(OriginAssistEditTextTips.TIP_EMPTY);
    private BehaviorSubject<OriginAssistEditTextTips> mBitLengthTipsSubject = BehaviorSubject.createDefault(OriginAssistEditTextTips.TIP_EMPTY);
    private BehaviorSubject<OriginAssistEditTextTips> mWorkpieceDiameterTipsSubject = BehaviorSubject.createDefault(OriginAssistEditTextTips.TIP_EMPTY);
    private BehaviorSubject<OriginAssistEditTextTips> mWorkpieceLengthTipsSubject = BehaviorSubject.createDefault(OriginAssistEditTextTips.TIP_EMPTY);
    private BehaviorSubject<Integer> mIsMovePopUpSubject = BehaviorSubject.createDefault(0);
    private MachineController machineController;

    public A400CNCOriginAssistantViewModel() {
        super();
        machineController = ServiceContainer.getInstance().getService(IMachine.class).getMachineController();
        bindEvents();
    }

    private Observable<MachineStatus> updateCoordinateSystem(Object response) {
        return machineController.updateCoordinateSystem();
    }

    public void setBitDiameterInput(String input) {
        mBitDiameterInputSubject.onNext(input);
    }

    public void setBitLengthInput(String input) {
        mBitLengthInputSubject.onNext(input);
    }

    public float getBitDiameter() {
        return mBitDiameterSubject.getValue();
    }

    public float getBitLength() {
        return mBitLengthSubject.getValue();
    }

    public void setWorkpieceDiameterInput(String input) {
        mWorkpieceDiameterInputSubject.onNext(input);
    }

    public float getWorkpieceDiameter() {
        return mWorkpieceDiameterSubject.getValue();
    }

    public void setWorkpieceLengthInput(String input) {
        mWorkpieceLengthInputSubject.onNext(input);
    }

    public float getWorkpieceLength() {
        return mWorkpieceLengthSubject.getValue();
    }

    public Observable<AssistPhase> getAssistPhaseObservable() {
        return mAssistPhaseSubject.hide();
    }

    public AssistPhase getAssistPhase() {
        return mAssistPhaseSubject.getValue();
    }

    public Observable<OriginAssistEditTextTips> getWorkpieceDiameterTipObservable() {
        return mWorkpieceDiameterTipsSubject.hide();
    }

    public Observable<OriginAssistEditTextTips> getWorkpieceLengthTipObservable() {
        return mWorkpieceLengthTipsSubject.hide();
    }

    public Observable<Boolean> getMaterialInputReady() {
        // Both diameter and length input must be positive value.
        return Observable.combineLatest(mWorkpieceDiameterInputSubject, mWorkpieceLengthInputSubject, mWorkpieceDiameterSubject, mWorkpieceLengthSubject,
                (inputD, inputL, d, l) -> !inputD.isEmpty() && !inputL.isEmpty() && (d > 0) && (l > 0));
    }

    public Observable<Boolean> getCustomBitInputReady() {
        // Both diameter and length input must be positive value.
        return Observable.combineLatest(mBitDiameterInputSubject, mBitLengthInputSubject, mBitDiameterSubject, mBitLengthSubject,
                (inputD, inputL, d, l) -> !inputD.isEmpty() && !inputL.isEmpty() && (d > 0) && (l > 0));
    }

    public Observable<OriginAssistEditTextTips> getBitDiameterTipObservable() {
        return mBitDiameterTipsSubject.hide();
    }

    public Observable<OriginAssistEditTextTips> getBitLengthTipObservable() {
        return mBitLengthTipsSubject.hide();
    }

    public Observable<Integer> getMovingObservable() {
        return mIsMovePopUpSubject.hide();
    }

    private void bindEvents() {
        mBitLengthInputSubject
                .as(bindToLifecycle())
                .subscribe(s -> {
                    if (s.isEmpty()) {
                        mBitLengthTipsSubject.onNext(OriginAssistEditTextTips.TIP_EMPTY);
                    } else {
                        final float length;
                        try {
                            length = Float.parseFloat(s);
                            mBitLengthSubject.onNext(length);
                        } catch (NumberFormatException e) {
                            LogHelper.log(e);
                        }
                    }
                });

        mBitDiameterInputSubject
                .as(bindToLifecycle())
                .subscribe(s -> {
                    if (s.isEmpty()) {
                        mBitDiameterTipsSubject.onNext(OriginAssistEditTextTips.TIP_EMPTY);
                    } else {
                        final float diameter;
                        try {
                            diameter = Float.parseFloat(s);
                            mBitDiameterSubject.onNext(diameter);
                        } catch (NumberFormatException e) {
                            LogHelper.log(e);
                        }
                    }
                });

        mWorkpieceLengthInputSubject
                .as(bindToLifecycle())
                .subscribe(s -> {
                    if (s.isEmpty()) {
                        mWorkpieceLengthTipsSubject.onNext(OriginAssistEditTextTips.TIP_EMPTY);
                    } else {
                        final float length;
                        try {
                            length = Float.parseFloat(s);
                            mWorkpieceLengthSubject.onNext(length);
                        } catch (NumberFormatException e) {
                            LogHelper.log(e);
                        }
                    }
                });

        mWorkpieceDiameterInputSubject
                .as(bindToLifecycle())
                .subscribe(s -> {
                    if (s.isEmpty()) {
                        mWorkpieceDiameterTipsSubject.onNext(OriginAssistEditTextTips.TIP_EMPTY);
                    } else {
                        final float diameter;
                        try {
                            diameter = Float.parseFloat(s);
                            mWorkpieceDiameterSubject.onNext(diameter);
                        } catch (NumberFormatException e) {
                            LogHelper.log(e);
                        }
                    }
                });

        mBitDiameterSubject
                .skip(1)
                .map(diameter -> {
                    if (diameter <= 0) {
                        return OriginAssistEditTextTips.TIP_NOT_POSITIVE_NUMBER;
                    } else {
                        return OriginAssistEditTextTips.TIP_OK;
                    }
                })
                .as(bindToLifecycle())
                .subscribe(tip -> mBitDiameterTipsSubject.onNext(tip));

        mBitLengthSubject
                .skip(1)
                .map(diameter -> {
                    if (diameter <= 0) {
                        return OriginAssistEditTextTips.TIP_NOT_POSITIVE_NUMBER;
                    } else {
                        return OriginAssistEditTextTips.TIP_OK;
                    }
                })
                .as(bindToLifecycle())
                .subscribe(tip -> mBitLengthTipsSubject.onNext(tip));

        mWorkpieceDiameterSubject
                .skip(1)
                .map(diameter -> {
                    if (diameter <= 0) {
                        return OriginAssistEditTextTips.TIP_NOT_POSITIVE_NUMBER;
                    } else {
                        return OriginAssistEditTextTips.TIP_OK;
                    }
                })
                .as(bindToLifecycle())
                .subscribe(tip -> {
                    mWorkpieceDiameterTipsSubject.onNext(tip);
                });

        mWorkpieceLengthSubject
                .skip(1)
                .map(length -> {
                    if (length <= 0) {
                        return OriginAssistEditTextTips.TIP_NOT_POSITIVE_NUMBER;
                    } else {
                        return OriginAssistEditTextTips.TIP_OK;
                    }
                })
                .as(bindToLifecycle())
                .subscribe(tip -> {
                    mWorkpieceLengthTipsSubject.onNext(tip);
                });
    }

    public void startOriginAssist() {
        // Reset status before start.
        mAssistPhaseSubject.onNext(AssistPhase.NOT_START);
        mIsMovePopUpSubject.onNext(0);

        // Start Origin Assist procedure
        Logger.d("Origin Assistant started.");
        gotoX1Position();
    }

    private void gotoX1Position() {
        mIsMovePopUpSubject.onNext(1);

        float sizeX = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getX();
        float sizeY = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getY();
        float sizeZ = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getZ();

        Logger.d("size x is: %1$f, workpiece d is %2$f, bit diameter is %3$f", sizeX, getWorkpieceDiameter(), getBitDiameter());

        float initialX = sizeX * 0.5f - getWorkpieceDiameter() - getBitDiameter() - 5; // safety first
        float initialY = Math.min(sizeY - MockConst.ROTARY_MOCK_CHUCK_LENGTH, Math.max(0, sizeY * 0.5f - getWorkpieceLength() * 0.5f));
        float initialZ = Math.min(sizeZ, MockConst.ROTARY_MOCK_CHUCK_HEIGHT + getBitLength());
        // We work on G53 because the position was calculated in absolute coordinate.
        Vector moveXY = new Vector();
        moveXY.setX(initialX);
        moveXY.setY(initialY);
        Vector moveZ = new Vector();
        moveZ.setZ(initialZ);
        machineController.updateCoordinateSystem(0)
                .flatMap(ret -> machineController.gotoAbsolutePosition(moveXY, 3000))
                .flatMap(ret -> machineController.gotoAbsolutePosition(moveZ, 1800))
                .flatMap(ret -> machineController.updateCoordinateSystem(1))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(ret -> {
                    mAssistPhaseSubject.onNext(AssistPhase.X1);
                    mIsMovePopUpSubject.onNext(0);
                }, e -> {
                    LogHelper.log(e);
                    mIsMovePopUpSubject.onNext(0);
                    mAssistPhaseSubject.onNext(AssistPhase.ERROR);
                });
    }

    private void gotoX2Position() {
        mIsMovePopUpSubject.onNext(2);
        float sizeX = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getX();
        float sizeZ = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getZ();
        float statusZ = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().currentPosition.getZ();
        float currentZ = statusZ - ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getZ();

        float nextX = sizeX * 0.5f + getWorkpieceDiameter() + getBitDiameter() + 5;
        float pullUpZ = sizeZ - 10;
        Vector movePullUpZ = new Vector();
        movePullUpZ.setZ(pullUpZ);
        Vector moveXB = new Vector();
        moveXB.setX(nextX);
        moveXB.setB(180);
        Vector moveCurrentZ = new Vector();
        moveCurrentZ.setZ(currentZ);
        machineController.updateCoordinateSystem(0)
                .flatMap(ret -> machineController.gotoAbsolutePosition(movePullUpZ, 1800))
                .flatMap(ret -> machineController.gotoAbsolutePosition(moveXB, 3000))
                .flatMap(ret -> machineController.gotoAbsolutePosition(moveCurrentZ, 1800))
                .flatMap(ret -> machineController.updateCoordinateSystem(1))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(ret -> {
                    mAssistPhaseSubject.onNext(AssistPhase.X2);
                    mIsMovePopUpSubject.onNext(0);
                }, e -> {
                    LogHelper.log(e);
                    mIsMovePopUpSubject.onNext(0);
                    mAssistPhaseSubject.onNext(AssistPhase.ERROR);
                });
    }

    private void gotoYPosition() {
        mIsMovePopUpSubject.onNext(3);
        float sizeY = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getY();
        float sizeZ = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getZ();

        float nextX = mOriginX;
        float nextY = sizeY - MockConst.ROTARY_MOCK_CHUCK_LENGTH - getWorkpieceLength() - 10;
        float pullUpZ = sizeZ - 10;
        float nextZ = Math.min(sizeZ, MockConst.ROTARY_MOCK_CHUCK_HEIGHT + getWorkpieceDiameter() * 0.5f + getBitLength() * 0.5f);

        Vector movePullUpZ = new Vector();
        movePullUpZ.setZ(pullUpZ);
        Vector moveXY = new Vector();
        moveXY.setX(nextX);
        moveXY.setY(nextY);
        Vector moveNextZ = new Vector();
        moveNextZ.setZ(nextZ);

        machineController.updateCoordinateSystem(0)
                .flatMap(ret -> machineController.gotoAbsolutePosition(movePullUpZ, 1800))
                .flatMap(ret -> machineController.gotoAbsolutePosition(moveXY, 3000))
                .flatMap(ret -> machineController.gotoAbsolutePosition(moveNextZ, 1800))
                .flatMap(ret -> machineController.updateCoordinateSystem(1))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(ret -> {
                    mAssistPhaseSubject.onNext(AssistPhase.Y);
                    mIsMovePopUpSubject.onNext(0);
                }, e -> {
                    LogHelper.log(e);
                    mIsMovePopUpSubject.onNext(0);
                    mAssistPhaseSubject.onNext(AssistPhase.ERROR);
                });
    }

    private void gotoZPosition() {
        mIsMovePopUpSubject.onNext(4);
        float sizeZ = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getZ();

        float nextX = mOriginX;
        float nextY = mOriginY + getBitDiameter();
        float pullUpZ = sizeZ - 10;
        float nextZ = Math.min(sizeZ, MockConst.ROTARY_MOCK_CHUCK_HEIGHT + getWorkpieceDiameter() * 0.5f + getBitLength() + 10);

        Vector movePullUpZ = new Vector();
        movePullUpZ.setZ(pullUpZ);
        Vector moveXY = new Vector();
        moveXY.setX(nextX);
        moveXY.setY(nextY);
        Vector moveNextZ = new Vector();
        moveNextZ.setZ(nextZ);

        machineController.updateCoordinateSystem(0)
                .flatMap(ret -> machineController.gotoAbsolutePosition(movePullUpZ, 1800))
                .flatMap(ret -> machineController.gotoAbsolutePosition(moveXY, 3000))
                .flatMap(ret -> machineController.gotoAbsolutePosition(moveNextZ, 1800))
                .flatMap(ret -> machineController.updateCoordinateSystem(1))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(ret -> {
                    mAssistPhaseSubject.onNext(AssistPhase.Z);
                    mIsMovePopUpSubject.onNext(0);
                }, e -> {
                    LogHelper.log(e);
                    mIsMovePopUpSubject.onNext(0);
                    mAssistPhaseSubject.onNext(AssistPhase.ERROR);
                });
    }

    private void gotoBPosition() {
        mIsMovePopUpSubject.onNext(5);
        mAssistPhaseSubject.onNext(AssistPhase.B);
        mIsMovePopUpSubject.onNext(0);
//        Vector moveB = new Vector();
//        moveB.setB(5.0f);
//        machineController.updateCoordinateSystem(0)
//                .flatMap(ret -> machineController.gotoAbsolutePosition(moveB, 1800))
//                .flatMap(ret -> machineController.updateCoordinateSystem(1))
//                .observeOn(AndroidSchedulers.mainThread())
//                .as(bindToLifecycle())
//                .subscribe(ret -> {
//                    mAssistPhaseSubject.onNext(AssistPhase.B);
//                    mIsMovePopUpSubject.onNext(0);
//                }, e -> {
//                    LogHelper.log(e);
//                    mIsMovePopUpSubject.onNext(0);
//                    mAssistPhaseSubject.onNext(AssistPhase.ERROR);
//                });
    }

    public Single<Boolean> setOriginX1() {
        SingleSubject<Boolean> resultSubject = SingleSubject.create();
        MachineStatus status = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue();

        // x1 current position
        x1 = status.currentPosition.getX() - ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getX();

        resultSubject.onSuccess(true);
        // goto x2 position
        gotoX2Position();
        return resultSubject.hide();
    }

    public Single<Boolean> setOriginX2() {
        // Get current x2 position and calculate origin x.
        SingleSubject<Boolean> resultSubject = SingleSubject.create();
        MachineStatus status = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue();
        x2 = status.currentPosition.getX() - ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getX();
        mOriginX = (x1 + x2) * 0.5f;

        Logger.d("Requesting set origin x...");

        // Tricks here.
        // The origin x is (x1 + x2) * 0.5f, and we need to set origin without moving to origin position.
        // We assume that current position is "x2", so x2's origin offset is (x2 - originX),
        // so we set the "offset" as current origin. it's equal to goto origin position and set 0(offset).
        Vector vector = new Vector();
        vector.setX((x2 - mOriginX));
        machineController.setWorkOrigin(vector)
                .flatMap(this::updateCoordinateSystem)
                .as(bindToLifecycle())
                .subscribe(ret -> {
                    resultSubject.onSuccess(true);
                    gotoYPosition();
                }, e -> {
                    LogHelper.log(e);
                    mAssistPhaseSubject.onNext(AssistPhase.ERROR);
                    resultSubject.onError(new FabException("Set Origin X2 Failed."));
                });

        return resultSubject.hide();
    }

    public Single<Boolean> setOriginY() {
        SingleSubject<Boolean> resultSubject = SingleSubject.create();
        MachineStatus status = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue();
        float absoluteY = status.currentPosition.getY() - ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getY();
        mOriginY = absoluteY + getBitDiameter() / 2;

        Logger.d("Requesting set origin y...");
        // OffsetY = y - originY
        Vector vector = new Vector();
        vector.setY(-getBitDiameter() / 2);
        machineController.setWorkOrigin(vector)
                .flatMap(this::updateCoordinateSystem)
                .as(bindToLifecycle())
                .subscribe(success -> {
                    resultSubject.onSuccess(true);
                    gotoZPosition();
                }, e -> {
                    LogHelper.log(e);
                    mAssistPhaseSubject.onNext(AssistPhase.ERROR);
                    resultSubject.onError(new FabException("Set Origin Y Failed."));
                });

        return resultSubject.hide();
    }

    public Single<Boolean> setOriginZ() {
        SingleSubject<Boolean> resultSubject = SingleSubject.create();

        Logger.d("Requesting set origin z...");
        // OffsetZ = z - originZ
        Vector vector = new Vector();
        vector.setZ(getWorkpieceDiameter() / 2);
        machineController.setWorkOrigin(vector)
                .flatMap(this::updateCoordinateSystem)
                .flatMap(status -> MoveController.getInstance().stepToPosition(MoveController.Direction.UP, 50))
                .as(bindToLifecycle())
                .subscribe(success -> {
                    resultSubject.onSuccess(true);
                    gotoBPosition();
                }, e -> {
                    LogHelper.log(e);
                    mAssistPhaseSubject.onNext(AssistPhase.ERROR);
                    resultSubject.onError(new FabException("Set Origin Z Failed."));
                });

        return resultSubject.hide();
    }

    public Single<Boolean> setOriginB() {
        SingleSubject<Boolean> resultSubject = SingleSubject.create();

        Logger.d("Requesting set origin b...");
        Vector vector = new Vector();
        vector.setB(0);
        machineController.setWorkOrigin(vector)
                .flatMap(this::updateCoordinateSystem)
                .flatMap(status -> MoveController.getInstance().stepToPosition(MoveController.Direction.UP, 50))
                .as(bindToLifecycle())
                .subscribe(success -> {
                    resultSubject.onSuccess(true);
                    mAssistPhaseSubject.onNext(AssistPhase.COMPLETE);
                }, e -> {
                    LogHelper.log(e);
                    mAssistPhaseSubject.onNext(AssistPhase.ERROR);
                    resultSubject.onError(new FabException("Set Origin B Failed."));
                });

        return resultSubject.hide();
    }

    public enum OriginAssistEditTextTips {
        TIP_NOT_POSITIVE_NUMBER,
        TIP_EMPTY,
        TIP_OK
    }

    public enum AssistPhase {
        NOT_START,
        ERROR,
        X1,
        X2,
        Y,
        Z,
        B,
        COMPLETE
    }
}
