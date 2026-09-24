package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingBed;

import android.annotation.SuppressLint;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.machine.controller.FDMController;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.entity.module.HeatedBed;
import fabscreen.platform.base.service.machine.entity.toolhead.FdmToolhead;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.data.MoveController;
import fabscreen.platform.core.ui.data.calibration.CalibrationPoint;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

/**
 * We manage the lifecycle of this ViewModel by ourself, instead of AutoDispose.
 */
@SuppressLint("AutoDispose")
public class A400LevelingBedViewModel extends BaseViewModel {

    private final static int[][] mDirection = {{0, 1}, {-1, 0}, {0, -1}, {1, 0}}; // right, up, left, down, rotate with counterclockwise
    private int mGrid = 0;
    private boolean mIsAutoMode = false;
    private ArrayList<CalibrationPoint> mPoints;
    private BehaviorSubject<Boolean> mIsMovingSubject = BehaviorSubject.createDefault(false);
    private BehaviorSubject<Boolean> mIsMovePopUpSubject = BehaviorSubject.createDefault(false);
    public int HEATING_SPEED = 5;
    public int LEVELING_POINT_TIME = 5;
    private int mBedCalibrationBedTemperature;
    private int calibrationMode;
    private FDMController fdmController;
    private MachineController machineController;
    private BehaviorSubject<Integer> mHeatTimeSubject = BehaviorSubject.createDefault(0);
    private PublishSubject<Integer> mCalibrationPointSubject = PublishSubject.create();
    private int wholeCalculateTime = 0;
    private BehaviorSubject<Boolean> mTodoNext = BehaviorSubject.createDefault(false);
    private BehaviorSubject<Boolean> mToolheadStatusState = BehaviorSubject.createDefault(false);
    private BehaviorSubject<Boolean> mBedStatusState = BehaviorSubject.createDefault(false);

    public A400LevelingBedViewModel() {
        super();
        IPreferences.Helper helper = ServiceContainer.getInstance().getService(IPreferences.class).getHelper();
        fdmController = ServiceContainer.getInstance().getService(IMachine.class).getFDMController();
        machineController = ServiceContainer.getInstance().getService(IMachine.class).getMachineController();

        calibrationMode = helper.getA400LevelingBedCalibrationMode();
        mBedCalibrationBedTemperature = helper.getA400LevelingBedCalibrationBedTemperature();

        mIsAutoMode = (calibrationMode == 0);

        mGrid = helper.getA400LevelingBedCalibrationGrid();

        fdmController.unWatchGridCalibrationStatus();

        fdmController.watchGridCalibrationStatus()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(heatedBedStatus -> {
                    if (!heatedBedStatus.isSuccess()) {
                        return;
                    }
                    BaseStructure returnStruct = (BaseStructure) heatedBedStatus.dataProp;
                    int index = (int) returnStruct.getProp("index").getValue();
                    int status = (int) returnStruct.getProp("status").getValue();
                    if (status != 0) {
                        // Error
                        Logger.d("---FDT--- watchGridCalibrationStatus StatusError " + heatedBedStatus);
                    } else {
                        if (mPoints != null) {
                            mCalibrationPointSubject.onNext(index);
                        }
                    }
                }, LogHelper::log);
        Observable.zip(mToolheadStatusState, mBedStatusState, (toolState, bedState) -> toolState && bedState)
                .as(bindToLifecycle())
                .subscribe(aBoolean -> mTodoNext.onNext(aBoolean), LogHelper::log);
        float currentTemperature = machineController.getHeatedBed().getHeatedBedStatusSubjectHolder().getValue().getZoneList().get(0).getCurrentTemperature();
        wholeCalculateTime = (int) Math.abs(currentTemperature - mBedCalibrationBedTemperature) * HEATING_SPEED + mGrid * mGrid * LEVELING_POINT_TIME;

    }

    public int getWholeCalculateTime() {
        return wholeCalculateTime;
    }


    public int CalculateHeatingTime() {
        // FIXME:
        float currentTemperature = machineController.getHeatedBed().getHeatedBedStatusSubjectHolder().getValue().getZoneList().get(0).getCurrentTemperature();
        return (int) Math.abs(currentTemperature - mBedCalibrationBedTemperature) * HEATING_SPEED;
    }

    public Observable<ResponseStructure> startCalibration() {
        initPoints();
        return fdmController.startGridCalibration(mGrid);
    }

    public Observable<Boolean> getIsMovingObservable() {
        return mIsMovingSubject.hide();
    }

    public Observable<Boolean> getIsMovePopUpSubject() {
        return mIsMovePopUpSubject.hide();
    }

    public int getGridCount() {
        return mGrid;
    }

    public boolean isAutoMode() {
        return mIsAutoMode;
    }

    public ArrayList<CalibrationPoint> initPoints() {
        final int[][] orders = generateViewOrders();
        mPoints = new ArrayList<>();
        for (int i = 1; i <= mGrid; i++) {
            for (int j = 1; j <= mGrid; j++) {
                int coordinateOrder = j + (mGrid * (mGrid - i));
                mPoints.add(new CalibrationPoint(i, j, orders[i - 1][j - 1], coordinateOrder));
            }
        }

        return mPoints;
    }

    private int[][] generateViewOrders() {
        int[][] orders = new int[mGrid][mGrid];
        int order = 1;
        int direction = 0;

        // init first element
        int currentRow = mGrid - 1;
        int currentColumn = 0;
        orders[currentRow][currentColumn] = order;
        order++;

        while (order <= mGrid * mGrid) {
            int nextRow = currentRow + mDirection[direction][0];
            int nextColumn = currentColumn + mDirection[direction][1];

            if (nextRow > mGrid - 1 || nextRow < 0 || nextColumn > mGrid - 1 || nextColumn < 0 || orders[nextRow][nextColumn] != 0) {
                // change direction
                direction = (direction + 1) % 4;
            } else {
                orders[nextRow][nextColumn] = order;
                currentRow = nextRow;
                currentColumn = nextColumn;
                order++;
            }
        }
        return orders;
    }

    private CalibrationPoint getPoint(int coordinateOrder) {
        if (coordinateOrder > mPoints.size() || coordinateOrder < 1) {
            return null;
        }

        for (CalibrationPoint point : mPoints) {
            if (point.getCoordinateOrder() == coordinateOrder) {
                return point;
            }
        }

        return null;
    }

    private CalibrationPoint getPointByViewOrder(int viewOrder) {
        for (int i = 0; i < mPoints.size(); i++) {
            if (mPoints.get(i).getViewOrder() == viewOrder) {
                return mPoints.get(i);
            }
        }
        return null;
    }

    private CalibrationPoint getPoint(int row, int column) {
        if (row > mGrid || row < 1 || column > mGrid || column < 1) {
            return null;
        }

        int position = column + mGrid * (row - 1);
        if (position > mPoints.size() || position < 1) {
            return null;
        }

        return mPoints.get(position - 1);
    }

    public void move(MoveController.Direction direction, float stepWidth) {
        mIsMovingSubject.onNext(true);
        MoveController.getInstance()
                .stepToPosition(direction, stepWidth)
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        mIsMovingSubject.onNext(false);
                    } else {
                        mIsMovingSubject.onNext(false);
                    }
                }, e -> {
                    LogHelper.log(e);
                    mIsMovingSubject.onNext(false);
                });
    }

    public void startHeating() {
        machineController.getHeatedBed().setAllTargetTemperature(mBedCalibrationBedTemperature)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                }, LogHelper::log);
        fdmController.setExtruderTemperature(0, 0, 150)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                }, LogHelper::log);
    }

    public int getViewOrder(int index) {
        return getPoint(index).getViewOrder();
    }

    public int getCoordinateOrder(int index) {
        return getPoint(index).getCoordinateOrder();
    }

    public int getCoordinateOrderByViewOrder(int index) {
        return getPointByViewOrder(index).getCoordinateOrder();
    }

    public int getBedCalibrationBedTemperature() {
        return mBedCalibrationBedTemperature;
    }

    public Observable<Integer> updateHeatedTime() {
        return mHeatTimeSubject.hide();
    }

    public Observable<Boolean> todoNext() {
        return mTodoNext.hide();
    }

    public Observable<Integer> getCalibrationObservable() {
        return mCalibrationPointSubject.hide();
    }

    public void subscribeTemperatureChange() {
        machineController.getHeatedBed().subscribeTemperatureChange();
        fdmController.subscribeExtruderChange();
    }

    public void unsubscribeTemperatureChange() {
        machineController.getHeatedBed().unsubscribeTemperatureChange();
        fdmController.unSubscribeExtruderChange();
    }

    public void subscribeGridCalibrationStatus() {
        fdmController.subscribeGridCalibrationStatus();
    }

    public void unsubscribeGridCalibrationStatus() {
        fdmController.unSubscribeGridCalibrationStatus();
    }

    public Observable<Boolean> checkHome() {
        IMachine service = ServiceContainer.getInstance().getService(IMachine.class);
        if (!service.getMachineStatusSubjectHolder().getValue().isHomed) {
            mIsMovePopUpSubject.onNext(true);
            return service.getMachineController().updateCoordinateSystem(0)
                    .flatMap(machineStatus -> service.getMachineController().home(0))
                    .doOnNext(machineStatus -> {
                        mIsMovePopUpSubject.onNext(false);
                    })
                    .flatMap(integer -> Observable.just(integer == 0));
        } else {
            return Observable.just(true);
        }
    }

    public void setCalibrationPoint(int i) {
        mIsMovePopUpSubject.onNext(true);
        fdmController.GridCalibration(getPointByViewOrder(i).getCoordinateOrder())
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        mIsMovePopUpSubject.onNext(false);
                        //因为外面是从1开始算，自定义的棋盘是index是从0开始算的，所以这边减1
                        mCalibrationPointSubject.onNext(getPointByViewOrder(i - 1).getCoordinateOrder());
                    }
                }, LogHelper::log);
    }

    public Observable<FdmToolhead.FdmToolheadStatus> getExtruderChangeObservable() {
        return fdmController.getToolheadStatusSubjectHolder(0).getObservable()
                .doOnNext(toolheadStatus -> {
                    float currentTemperature = toolheadStatus.getExtruderList().get(0).getTemperature();
                    if (currentTemperature >= 150) {
                        mToolheadStatusState.onNext(true);
                    }
                });
    }

    public Observable<HeatedBed.HeatedBedStatus> getBedChangeObservable() {
        return machineController.getHeatedBed().getHeatedBedStatusSubjectHolder().getObservable()
                .doOnNext(heatedBedStatus -> {
                    int targetTemperature = heatedBedStatus.getZoneList().get(0).getTargetTemperature();
                    float currentTemperature = heatedBedStatus.getZoneList().get(0).getCurrentTemperature();
                    int time = (int) (Math.abs(targetTemperature - currentTemperature) * HEATING_SPEED) + mGrid * mGrid * LEVELING_POINT_TIME;
                    mHeatTimeSubject.onNext(time);
                    if (currentTemperature >= mBedCalibrationBedTemperature && mBedCalibrationBedTemperature != 0) {
                        mBedStatusState.onNext(true);
                    }
                });

    }

    public Observable<ResponseStructure> getInterruptAutoLevelingObservable() {
        return machineController.getInterruptAutoLevelingObservable();
    }
}
