package fabscreen.platform.core.ui.presenter;

import android.view.View;
import android.widget.Button;

import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.core.R2;
import fabscreen.platform.core.ui.view.ActionButton;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

public class SetOrigin4AxisWidgetPresenter extends BasePresenter {
    @BindView(R2.id.btn_control_laser_page_set_origin_set_origin)
    ActionButton mBtnPageSetOriginSetOrigin;
    @BindView(R2.id.btn_control_laser_page_set_origin_set_origin_x)
    ActionButton mBtnPageSetOriginSetOriginX;
    @BindView(R2.id.btn_control_laser_page_set_origin_set_origin_y)
    ActionButton mBtnPageSetOriginSetOriginY;
    @BindView(R2.id.btn_control_laser_page_set_origin_set_origin_z)
    ActionButton mBtnPageSetOriginSetOriginZ;
    @BindView(R2.id.btn_control_laser_page_set_origin_set_origin_b)
    ActionButton mBtnPageSetOriginSetOriginB;
    @BindView(R2.id.btn_control_laser_page_set_origin_goto_origin)
    ActionButton mBtnPageSetOriginGotoOrigin;
    @BindView(R2.id.btn_control_laser_page_set_origin_home)
    ActionButton mBtnHome;

    private BehaviorSubject<Boolean> mMovingEventSubject = BehaviorSubject.createDefault(false);
    private boolean mDisableSetZOrigin = false;

    public SetOrigin4AxisWidgetPresenter(CompositeDisposable compositeDisposable) {
        super(compositeDisposable);
    }

    public void bind(View view) {
        ButterKnife.bind(this, view);

        Disposable sub = mMovingEventSubject
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(movingEvent -> setEnabled(!movingEvent));
        addDisposable(sub);
    }

    public void connect() {
        // automatically connected via OnClick binding
    }

    public void setEnabled(boolean enabled) {
        mBtnPageSetOriginSetOrigin.setEnabled(enabled);
        mBtnPageSetOriginSetOriginX.setEnabled(enabled);
        mBtnPageSetOriginSetOriginY.setEnabled(enabled);
        mBtnPageSetOriginSetOriginZ.setEnabled(enabled && !mDisableSetZOrigin);
        mBtnPageSetOriginSetOriginB.setEnabled(enabled);
        mBtnPageSetOriginGotoOrigin.setEnabled(enabled);
        mBtnHome.setEnabled(enabled);
    }

    public Observable<Boolean> getMovingEventObservable() {
        return mMovingEventSubject.hide();
    }

    private Observable<MachineStatus> updateCoordinateSystem(Object response) {
        return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem();
    }

    public void disableSetZOrigin() {
        mDisableSetZOrigin = true;
        mBtnPageSetOriginSetOriginZ.setEnabled(false);
        mBtnPageSetOriginSetOriginZ.setVisibility(Button.GONE);
    }

    @OnClick(R2.id.btn_control_laser_page_set_origin_set_origin_x)
    void onClickSetOriginX() {
        mMovingEventSubject.onNext(true);
        mBtnPageSetOriginSetOriginX.setActivated(true);

        Logger.i("Requesting set origin x...");

        Vector vector = new Vector();
        vector.setX(0);
        Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector)
                .flatMap(this::updateCoordinateSystem)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    mMovingEventSubject.onNext(false);
                    mBtnPageSetOriginSetOriginX.setActivated(false);
                }, e -> {
                    LogHelper.log(e);
                    mMovingEventSubject.onNext(false);
                    mBtnPageSetOriginSetOriginX.setActivated(false);
                });
        addDisposable(sub);
    }

    @OnClick(R2.id.btn_control_laser_page_set_origin_set_origin_y)
    void onClickSetOriginY() {
        mMovingEventSubject.onNext(true);
        mBtnPageSetOriginSetOriginY.setActivated(true);

        Logger.i("Requesting set origin y...");
        Vector vector = new Vector();
        vector.setY(0);
        Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector)
                .flatMap(this::updateCoordinateSystem)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    mMovingEventSubject.onNext(false);
                    mBtnPageSetOriginSetOriginY.setActivated(false);
                }, e -> {
                    LogHelper.log(e);
                    mMovingEventSubject.onNext(false);
                    mBtnPageSetOriginSetOriginY.setActivated(false);
                });
        addDisposable(sub);
    }

    @OnClick(R2.id.btn_control_laser_page_set_origin_set_origin_z)
    void onClickSetOriginZ() {
        mMovingEventSubject.onNext(true);
        mBtnPageSetOriginSetOriginZ.setActivated(true);

        Logger.i("Requesting set origin z...");
        Vector vector = new Vector();
        vector.setZ(0);
        Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector)
                .flatMap(this::updateCoordinateSystem)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    mMovingEventSubject.onNext(false);
                    mBtnPageSetOriginSetOriginZ.setActivated(false);
                }, e -> {
                    LogHelper.log(e);
                    mMovingEventSubject.onNext(false);
                    mBtnPageSetOriginSetOriginZ.setActivated(false);
                });
        addDisposable(sub);
    }

    @OnClick(R2.id.btn_control_laser_page_set_origin_set_origin_b)
    void onClickSetOriginB() {
        mMovingEventSubject.onNext(true);
        mBtnPageSetOriginSetOriginB.setActivated(true);

        Logger.i("Requesting set origin b...");
        Vector vector = new Vector();
        vector.setB(0);
        Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector)
                .flatMap(this::updateCoordinateSystem)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    mMovingEventSubject.onNext(false);
                    mBtnPageSetOriginSetOriginB.setActivated(false);
                }, e -> {
                    LogHelper.log(e);
                    mMovingEventSubject.onNext(false);
                    mBtnPageSetOriginSetOriginB.setActivated(false);
                });
        addDisposable(sub);
    }

    @OnClick(R2.id.btn_control_laser_page_set_origin_set_origin)
    void onClickSetOrigin() {
        mMovingEventSubject.onNext(true);
        mBtnPageSetOriginSetOrigin.setActivated(true);

        Logger.i("Requesting set origin...");
        Vector vector = new Vector();
        vector.setX(0);
        vector.setY(0);
        vector.setZ(0);
        vector.setB(0);
        Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector)
                .flatMap(this::updateCoordinateSystem)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    mMovingEventSubject.onNext(false);
                    mBtnPageSetOriginSetOrigin.setActivated(false);
                }, e -> {
                    LogHelper.log(e);
                    mMovingEventSubject.onNext(false);
                    mBtnPageSetOriginSetOrigin.setActivated(false);
                });
        addDisposable(sub);
    }

    @OnClick(R2.id.btn_control_laser_page_set_origin_goto_origin)
    void onClickGotoOrigin() {
        final float currentZ = (float) ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().currentPosition.getZ();
        mMovingEventSubject.onNext(true);
        mBtnPageSetOriginGotoOrigin.setActivated(true);

        Logger.i("Requesting go to origin...");

        if (currentZ > 0) {
            // Engage direction, move X Y linear module and B rotary module first, then Z.
            Disposable sub = ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 X0 Y0 F3000")
                    .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 B0 F3000"))
                    .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 Z0 F1800"))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(success -> {
                        mMovingEventSubject.onNext(false);
                        mBtnPageSetOriginGotoOrigin.setActivated(false);
                    }, e -> {
                        LogHelper.log(e);
                        mMovingEventSubject.onNext(false);
                        mBtnPageSetOriginGotoOrigin.setActivated(false);
                    });
            addDisposable(sub);
        } else {
            // Retract direction, move Z linear module first, then X Y and B.
            Disposable sub = ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 Z0 F1800")
                    .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 X0 Y0 F3000"))
                    .flatMap(res -> ServiceContainer.getInstance().getService(IAppService.class).getSlaveComputer().sendGcode("G0 B0 F3000"))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(success -> {
                        mMovingEventSubject.onNext(false);
                        mBtnPageSetOriginGotoOrigin.setActivated(false);
                    }, e -> {
                        LogHelper.log(e);
                        mMovingEventSubject.onNext(false);
                        mBtnPageSetOriginGotoOrigin.setActivated(false);
                    });
            addDisposable(sub);
        }
    }

    @OnClick(R2.id.btn_control_laser_page_set_origin_home)
    void onClickHome() {
        mMovingEventSubject.onNext(true);
        mBtnHome.setActivated(true);

        Logger.i("Requesting G28...");

        Disposable sub = ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .flatMap(response -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().home(0))
                .flatMap(response -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    mMovingEventSubject.onNext(false);
                    mBtnHome.setActivated(false);
                }, e -> {
                    LogHelper.log(e);
                    mMovingEventSubject.onNext(false);
                    mBtnHome.setActivated(false);
                });
        addDisposable(sub);
    }
}
