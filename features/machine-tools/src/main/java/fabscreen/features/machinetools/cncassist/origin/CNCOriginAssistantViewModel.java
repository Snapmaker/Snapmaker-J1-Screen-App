package fabscreen.features.machinetools.cncassist.origin;

import com.orhanobut.logger.Logger;

import java.util.Locale;

import fabscreen.platform.base.FabException;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.legacy.connection.MockConst;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.view.BaseViewModel;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.SingleSubject;

public class CNCOriginAssistantViewModel extends BaseViewModel {

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
    private BehaviorSubject<Boolean> mMovingEventSubject = BehaviorSubject.createDefault(false);

    public CNCOriginAssistantViewModel() {
        super();

        bindEvents();
    }

    private Observable<MachineStatus> updateCoordinateSystem(Object response) {
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem();
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

    public Observable<Boolean> getMovingObservable() {
        return mMovingEventSubject.hide();
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
        mMovingEventSubject.onNext(false);

        // Start Origin Assist procedure
        Logger.d("Origin Assistant started.");
        gotoX1Position();
    }

    private void gotoX1Position() {
        mMovingEventSubject.onNext(true);

        float sizeX = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getX();
        float sizeY = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getY();
        float sizeZ = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getZ();

        float initialX = sizeX * 0.5f - getWorkpieceDiameter() - getBitDiameter() - 5; // safety first
        float initialY = Math.min(sizeY - MockConst.ROTARY_MOCK_CHUCK_LENGTH, Math.max(0, sizeY * 0.5f - getWorkpieceLength() * 0.5f));
        float initialZ = Math.min(sizeZ, MockConst.ROTARY_MOCK_CHUCK_HEIGHT + getBitLength());
        // We work on G53 because the position was calculated in absolute coordinate.
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .flatMap(ret -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G90"))
                .flatMap(ret -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 X%.2f Y%.2f F3000", initialX, initialY)))
                .flatMap(ret -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 Z%.2f F1800", initialZ)))
                .flatMap(ret -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(ret -> {
                    mAssistPhaseSubject.onNext(AssistPhase.X1);
                    mMovingEventSubject.onNext(false);
                }, e -> {
                    LogHelper.log(e);
                    mMovingEventSubject.onNext(false);
                    mAssistPhaseSubject.onNext(AssistPhase.ERROR);
                });
    }

    private void gotoX2Position() {
        mMovingEventSubject.onNext(true);
        float sizeX = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getX();
        float sizeZ = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getZ();
        float statusZ = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().currentPosition.getZ();
        float currentZ = statusZ - ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getZ();

        float nextX = sizeX * 0.5f + getWorkpieceDiameter() + getBitDiameter() + 5;
        float pullUpZ = sizeZ - 10;
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .flatMap(ret -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G90"))
                .flatMap(ret -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 Z%.2f F1800", pullUpZ)))
                .flatMap(ret -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 X%.2f B%d F3000", nextX, 180)))
                .flatMap(ret -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 Z%.2f F1800", currentZ)))
                .flatMap(ret -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(ret -> {
                    mAssistPhaseSubject.onNext(AssistPhase.X2);
                    mMovingEventSubject.onNext(false);
                }, e -> {
                    LogHelper.log(e);
                    mMovingEventSubject.onNext(false);
                    mAssistPhaseSubject.onNext(AssistPhase.ERROR);
                });
    }

    private void gotoYPosition() {
        mMovingEventSubject.onNext(true);
        float sizeY = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getY();
        float sizeZ = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getZ();

        float nextX = mOriginX;
        float nextY = sizeY - MockConst.ROTARY_MOCK_CHUCK_LENGTH - getWorkpieceLength() - 10;
        float pullUpZ = sizeZ - 10;
        float nextZ = Math.min(sizeZ, MockConst.ROTARY_MOCK_CHUCK_HEIGHT + getWorkpieceDiameter() * 0.5f + getBitLength() * 0.5f);
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .flatMap(ret -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G90"))
                .flatMap(ret -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 Z%.2f F1800", pullUpZ)))
                .flatMap(ret -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 X%.2f Y%.2f F3000", nextX, nextY)))
                .flatMap(ret -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 Z%.2f F1800", nextZ)))
                .flatMap(ret -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(ret -> {
                    mAssistPhaseSubject.onNext(AssistPhase.Y);
                    mMovingEventSubject.onNext(false);
                }, e -> {
                    LogHelper.log(e);
                    mMovingEventSubject.onNext(false);
                    mAssistPhaseSubject.onNext(AssistPhase.ERROR);
                });
    }

    private void gotoZPosition() {
        mMovingEventSubject.onNext(true);
        float sizeZ = ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().size.getZ();

        float nextX = mOriginX;
        float nextY = mOriginY + getBitDiameter();
        float pullUpZ = sizeZ - 10;
        float nextZ = Math.min(sizeZ, MockConst.ROTARY_MOCK_CHUCK_HEIGHT + getWorkpieceDiameter() * 0.5f + getBitLength() + 10);
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0).flatMap(ret -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G90"))
                .flatMap(ret -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 Z%.2f F1800", pullUpZ)))
                .flatMap(ret -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 X%.2f Y%.2f F3000", nextX, nextY)))
                .flatMap(ret -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 Z%.2f F1800", nextZ)))
                .flatMap(ret -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(ret -> {
                    mAssistPhaseSubject.onNext(AssistPhase.Z);
                    mMovingEventSubject.onNext(false);
                }, e -> {
                    LogHelper.log(e);
                    mMovingEventSubject.onNext(false);
                    mAssistPhaseSubject.onNext(AssistPhase.ERROR);
                });
    }

    private void gotoBPosition() {
        mMovingEventSubject.onNext(true);
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0).flatMap(ret -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G91"))
                .flatMap(ret -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode(String.format(Locale.US, "G0 Z%.2f F1800", 5.0f)))
                .flatMap(ret -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G90"))
                .flatMap(ret -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(ret -> {
                    mAssistPhaseSubject.onNext(AssistPhase.B);
                    mMovingEventSubject.onNext(false);
                }, e -> {
                    LogHelper.log(e);
                    mMovingEventSubject.onNext(false);
                    mAssistPhaseSubject.onNext(AssistPhase.ERROR);
                });
    }

    public Single<Boolean> setOriginX1() {
        SingleSubject<Boolean> resultSubject = SingleSubject.create();
        MachineStatus status = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue();

        // x1 current position
        x1 = status.currentPosition.getX() - ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getX();
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
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector)
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
        vector.setY(absoluteY - mOriginY);
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector)
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
        MachineStatus status = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue();
        float absoluteZ = status.currentPosition.getZ() - ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getZ();
        mOriginZ = absoluteZ - getWorkpieceDiameter() / 2;

        Logger.d("Requesting set origin z...");
        // OffsetZ = z - originZ
        Vector vector = new Vector();
        vector.setZ(absoluteZ - mOriginZ);
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector)
                .flatMap(this::updateCoordinateSystem)
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
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector)
                .flatMap(this::updateCoordinateSystem)
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
