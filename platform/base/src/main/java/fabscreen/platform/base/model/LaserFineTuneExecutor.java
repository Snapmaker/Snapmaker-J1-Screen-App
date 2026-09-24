package fabscreen.platform.base.model;

import java.util.ArrayList;

import fabscreen.platform.base.FabException;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.parser.Position;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.structure.CoordinateSystemInfo;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.SingleSubject;

/**
 * FineTune Executor: execute laser pattern engrave in laser calibration.
 */
public class LaserFineTuneExecutor {
    private LaserPattern mPattern;

    private CompositeDisposable mDisposables;

    private ArrayList<Position> mStartPositions;
    private ArrayList<Position> mEndPositions;

    private BehaviorSubject<Integer> mEngraveIndexSubject = BehaviorSubject.createDefault(0);

    public LaserFineTuneExecutor(CompositeDisposable disposables) {
        mDisposables = disposables;
    }

    public void setLaserPattern(LaserPattern pattern) {
        mPattern = pattern;
    }

    public Single<Boolean> startFineTune() {
        if (mPattern == null) {
            return SingleSubject.error(new FabException("Laser Pattern can not be null!"));
        }

        SingleSubject<Boolean> resultSubject = SingleSubject.create();

        // calculate all point
        Disposable sub = calculatePoints()
                .flatMap(success -> {
                    if (!success) {
                        return Observable.just(-1);
                    }
                    return mEngraveIndexSubject.hide();
                })
                .subscribe(index -> {
                    if (index == -1) {
                        resultSubject.onSuccess(false);
                        return;
                    }
                    if (index < mPattern.getTotalLines()) {
                        nextLine(index);
                    } else {
                        // goto work origin
                        mDisposables.add(ServiceContainer.getInstance().getService(IMachine.class).getMachineController().goToOrigin()
                                .subscribe(success -> resultSubject.onSuccess(true), LogHelper::log));
                    }
                });
        mDisposables.add(sub);

        return resultSubject.hide();
    }

    public Observable<Boolean> calculatePoints() {
        if (mPattern == null) return Observable.just(false);

        int alignment = mPattern.getAlignment();
        int direction = mPattern.getEngraveDirection();

        mStartPositions = new ArrayList<>();
        mEndPositions = new ArrayList<>();

        // get current z position
        final float initialZPos = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().currentPosition.getZ();

        for (int i = 0; i < mPattern.getTotalLines(); i++) {
            float startX, startY;
            float endX, endY;
            int lineLength = 0;

            lineLength = i % mPattern.getUnitCountPerDivision() == 0 ? mPattern.getLongEngraveLineLength()
                    : mPattern.getShortEngraveLineLength();

            // Calculate start and end point with pattern direction and alignment.
            if (direction == LaserPattern.DIRECTION_X) {
                startX = mPattern.getSpacingPerLine() * (-10 + i);
                endX = startX;
                switch (alignment) {
                    case LaserPattern.ALIGNMENT_ENGRAVE_CENTER:
                        startY = (-1) * lineLength * 0.5f;
                        endY = lineLength * 0.5f;
                        break;
                    case LaserPattern.ALIGNMENT_ENGRAVE_END:
                        startY = mPattern.getLongEngraveLineLength() * 0.5f;
                        endY = startY - lineLength;
                        break;
                    case LaserPattern.ALIGNMENT_ENGRAVE_START:
                    default:
                        startY = (-1) * mPattern.getLongEngraveLineLength() * 0.5f;
                        endY = startY + lineLength;
                        break;
                }
            } else if (direction == LaserPattern.DIRECTION_Y) {
                startY = mPattern.getSpacingPerLine() * (-10 + i);
                endY = startY;
                switch (alignment) {
                    case LaserPattern.ALIGNMENT_ENGRAVE_CENTER:
                        startX = (-1) * lineLength * 0.5f;
                        endX = lineLength * 0.5f;
                        break;
                    case LaserPattern.ALIGNMENT_ENGRAVE_END:
                        startX = mPattern.getLongEngraveLineLength() * 0.5f;
                        endX = startX - lineLength;
                        break;
                    case LaserPattern.ALIGNMENT_ENGRAVE_START:
                    default:
                        startX = (-1) * mPattern.getLongEngraveLineLength() * 0.5f;
                        endX = startX + lineLength;
                        break;
                }
            } else {
                // TODO: If have any other direction?
                return Observable.just(false);
            }
            float lineZ = initialZPos + ((-10 + i) * mPattern.getZOffset());

            // save point
            mStartPositions.add(new Position(startX, startY, lineZ));
            mEndPositions.add(new Position(endX, endY, lineZ));
        }

        return Observable.just(true);
    }

    private void nextLine(int index) {
        Disposable sub = moveToNextStartPosition(mStartPositions.get(index))
                .flatMap(success -> engraveLine(mEndPositions.get(index)))
                .subscribe(success -> {
                    mEngraveIndexSubject.onNext(mEngraveIndexSubject.getValue() + 1);
                }, e -> {
                    LogHelper.log(e);
                    mEngraveIndexSubject.onNext(-1);
                });
        mDisposables.add(sub);
    }

    private Observable<Boolean> engraveLine(Position endPos) {
        return turnOnLaser()
                .flatMap(ret -> {
                    Vector vector = new Vector();
                    vector.setX(endPos.x);
                    vector.setY(endPos.y);
                    vector.setZ(endPos.z);
                    return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().gotoAbsolutePosition(vector, 300);
                })
                .flatMap(success -> turnOffLaser())
                .flatMap(response -> Observable.just(true));
    }

    private Observable<ResponseStructure> moveToNextStartPosition(Position position) {
        Vector vector = new Vector();
        vector.setX(position.x);
        vector.setY(position.y);
        vector.setZ(position.z);
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().gotoAbsolutePosition(vector, 3000);
    }

    private Observable<ResponseStructure<IStructure>> turnOnLaser() {
        return ServiceContainer.getInstance().getService(IMachine.class).getLaserController().setLaserPower(0, 70);
//        return ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("M3 P70");
    }

    private Observable<ResponseStructure<IStructure>> turnOffLaser() {
        return ServiceContainer.getInstance().getService(IMachine.class).getLaserController().setLaserPower(0, 0);
//        return ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("M5");
    }
}
