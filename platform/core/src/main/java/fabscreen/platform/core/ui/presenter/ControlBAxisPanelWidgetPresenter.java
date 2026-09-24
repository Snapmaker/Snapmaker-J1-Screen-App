package fabscreen.platform.core.ui.presenter;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.base.service.machine.structure.CoordinateSystemInfo;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.core.R2;
import fabscreen.platform.core.ui.view.ActionButton;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.subjects.BehaviorSubject;

public class ControlBAxisPanelWidgetPresenter {
    @BindView(R2.id.sbg_control_b_axis_steps)
    SegmentedButtonGroup mSbgBAxisSteps;
    @BindView(R2.id.btn_widget_b_axis_clockwise)
    Button mBtnBAxisClockwise;
    @BindView(R2.id.btn_widget_b_axis_counterclockwise)
    Button mBtnBAxisCounterClockwise;
    private Context mContext;
    private CompositeDisposable mCompositeDisposable;
    private double mMoveStep = 1;
    private BehaviorSubject<Boolean> mMovingEventSubject = BehaviorSubject.createDefault(false);
    private final MachineController mMachineController;
    private final IMachine mMachine;
    private int mCoordinateType = 0;
    private final MachineInfo mMachineInfo;

    public ControlBAxisPanelWidgetPresenter(CompositeDisposable compositeDisposable) {

        mCompositeDisposable = compositeDisposable;
        mMachine = ServiceContainer.getInstance().getService(IMachine.class);
        mMachineInfo = mMachine.getMachineInfoSubjectHolder().getValue();
        mMachineController = mMachine.getMachineController();
        int coordinateSystemIndex = mMachineInfo.workType == IMachine.WorkType.FDM ? 0 : 1;
        mMachineController.updateCoordinateSystem(coordinateSystemIndex);
    }

    private Context getContext() {
        return mContext;
    }

    public Observable<Boolean> getMovingEventObservable() {
        return mMovingEventSubject.hide();
    }

    public void bind(View view) {
        ButterKnife.bind(this, view);

        mSbgBAxisSteps.setOnPositionChangedListener(position -> {
            switch (position) {
                case 0:
                    mMoveStep = 0.2;
                    break;
                case 1:
                    mMoveStep = 1;
                    break;
                case 2:
                    mMoveStep = 5;
                    break;
                case 3:
                    mMoveStep = 90;
                    break;
            }
        });
        mSbgBAxisSteps.setPosition(1, false);
        mMoveStep = 1;

        Disposable sub = mMovingEventSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(movingEvent -> setEnabled(!movingEvent));
        mCompositeDisposable.add(sub);
    }

    public void connect() {
        mBtnBAxisCounterClockwise.setOnClickListener(v -> {
            mMovingEventSubject.onNext(true);

            Disposable sub = mMachineController.pullCoordinate()
                    .concatMap((Function<ResponseStructure<CoordinateSystemInfo>, ObservableSource<ResponseStructure>>) coordinateSystemInfoResponseStructure -> {
                        Vector coordinate = mMachineController.getCachedCoordinate();
                        Vector b = new Vector();
                        b.setB((float) (coordinate.getB() - mMoveStep));
                        return mMachineController.gotoAbsolutePosition(b, 1800);
                    }).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(res -> {
                        mMovingEventSubject.onNext(false);
                    }, e -> {
                        LogHelper.log(e);
                        mMovingEventSubject.onNext(false);
                    });
            mCompositeDisposable.add(sub);
        });

        mBtnBAxisClockwise.setOnClickListener(v -> {
            mMovingEventSubject.onNext(true);

            Disposable sub = mMachineController.pullCoordinate()
                    .concatMap((Function<ResponseStructure<CoordinateSystemInfo>, ObservableSource<ResponseStructure>>) coordinateSystemInfoResponseStructure -> {
                        Vector coordinate = mMachineController.getCachedCoordinate();
                        Vector b = new Vector();
                        b.setB((float) (coordinate.getB() + mMoveStep));
                        return mMachineController.gotoAbsolutePosition(b, 1800);
                    }).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(res -> {
                        mMovingEventSubject.onNext(false);
                    }, e -> {
                        LogHelper.log(e);
                        mMovingEventSubject.onNext(false);
                    });
            mCompositeDisposable.add(sub);
        });
    }

    public void setEnabled(boolean enabled) {
        mBtnBAxisClockwise.setEnabled(enabled);
        mBtnBAxisCounterClockwise.setEnabled(enabled);
    }
}
