package fabscreen.platform.core.ui.presenter;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.MachineController;
import fabscreen.platform.core.R2;
import fabscreen.platform.core.ui.data.MoveController;
import fabscreen.platform.core.ui.view.CustomSteeringView;
import fabscreen.platform.core.ui.view.SteeringView;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;

import static fabscreen.platform.core.ui.data.MoveController.Direction.BACKWARD;
import static fabscreen.platform.core.ui.data.MoveController.Direction.DOWN;
import static fabscreen.platform.core.ui.data.MoveController.Direction.FORWARD;
import static fabscreen.platform.core.ui.data.MoveController.Direction.LEFT;
import static fabscreen.platform.core.ui.data.MoveController.Direction.RIGHT;
import static fabscreen.platform.core.ui.data.MoveController.Direction.UP;

public class ControlXYZPanelWidgetPresenter {
    @BindView(R2.id.sbg_control_steps)
    SegmentedButtonGroup mSbgControlSteps;
    @BindView(R2.id.sv_control_panel_xy)
    CustomSteeringView mSvControlXY;
    @BindView(R2.id.btn_control_panel_z_plus)
    Button mBtnControlZPlus;
    @BindView(R2.id.btn_control_panel_z_minus)
    Button mBtnControlZMinus;
    private Context mContext;
    private CompositeDisposable mCompositeDisposable;
    private double mMoveStep = 0.1;
    private boolean mDisableXY = false;
    private boolean mDisableZ = false;
    private BehaviorSubject<Boolean> mMovingEventSubject = BehaviorSubject.createDefault(false);
    private MachineController machineController;

    public ControlXYZPanelWidgetPresenter(CompositeDisposable compositeDisposable) {
        machineController = ServiceContainer.getInstance().getService(IMachine.class).getMachineController();
        mCompositeDisposable = compositeDisposable;
    }

    private Context getContext() {
        return mContext;
    }


    public Observable<Boolean> getMovingEventObservable() {
        return mMovingEventSubject.hide();
    }

    public void bind(View view, float xStep, float yStep, float zStep) {
        ButterKnife.bind(this, view);

        mSvControlXY.setSkin(CustomSteeringView.SteeringViewSkin.STEERING_VIEW_SKIN_A400);
        mSbgControlSteps.setOnPositionChangedListener(position -> {
            switch (position) {
                case 0:
                    mMoveStep = xStep;
                    break;
                case 1:
                    mMoveStep = yStep;
                    break;
                case 2:
                    mMoveStep = zStep;
                    break;
            }
        });
        mSbgControlSteps.setPosition(1, false);
        mMoveStep = 1;

        Disposable sub = mMovingEventSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(movingEvent -> setEnabled(!movingEvent));
        mCompositeDisposable.add(sub);
    }

    public void bind(View view, float xStep, float yStep, float zStep, float step) {
        ButterKnife.bind(this, view);
        mSvControlXY.setSkin(CustomSteeringView.SteeringViewSkin.STEERING_VIEW_SKIN_A400);
        mSbgControlSteps.setOnPositionChangedListener(position -> {
            switch (position) {
                case 0:
                    mMoveStep = xStep;
                    break;
                case 1:
                    mMoveStep = yStep;
                    break;
                case 2:
                    mMoveStep = zStep;
                    break;
                case 3:
                    mMoveStep = step;
                    break;
            }
        });
        mSbgControlSteps.setPosition(1, false);
        mMoveStep = 1;

        Disposable sub = mMovingEventSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(movingEvent -> setEnabled(!movingEvent));
        mCompositeDisposable.add(sub);
    }

    public void connect() {
        // x y panel
        mSvControlXY.setOnDirectionClickedListener(direction -> {

            switch (direction) {
                case SteeringView.DIRECTION_UP: {
                    mMovingEventSubject.onNext(true);
                    Disposable sub = MoveController.getInstance().stepToPosition(FORWARD, (float) mMoveStep)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(res -> {
                                mMovingEventSubject.onNext(false);
                            }, e -> {
                                LogHelper.log(e);
                                mMovingEventSubject.onNext(false);
                            });
                    mCompositeDisposable.add(sub);
                    break;
                }
                case SteeringView.DIRECTION_DOWN: {
                    mMovingEventSubject.onNext(true);
                    Disposable sub = MoveController.getInstance().stepToPosition(BACKWARD, (float) mMoveStep)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(res -> {
                                mMovingEventSubject.onNext(false);
                            }, e -> {
                                LogHelper.log(e);
                                mMovingEventSubject.onNext(false);
                            });
                    mCompositeDisposable.add(sub);
                    break;
                }
                case SteeringView.DIRECTION_LEFT: {
                    mMovingEventSubject.onNext(true);
                    Disposable sub = MoveController.getInstance().stepToPosition(LEFT, (float) mMoveStep)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(res -> {
                                mMovingEventSubject.onNext(false);
                            }, e -> {
                                LogHelper.log(e);
                                mMovingEventSubject.onNext(false);
                            });
                    mCompositeDisposable.add(sub);
                    break;
                }
                case SteeringView.DIRECTION_RIGHT: {
                    mMovingEventSubject.onNext(true);
                    Disposable sub = MoveController.getInstance().stepToPosition(RIGHT, (float) mMoveStep)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(res -> {
                                mMovingEventSubject.onNext(false);
                            }, e -> {
                                LogHelper.log(e);
                                mMovingEventSubject.onNext(false);
                            });
                    mCompositeDisposable.add(sub);
                    break;
                }
                default:
                    break;
            }
        });

        // z height button
        mBtnControlZMinus.setOnClickListener(v -> {
            mMovingEventSubject.onNext(true);
            Disposable sub = MoveController.getInstance().stepToPosition(DOWN, (float) mMoveStep)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(res -> {
                        mMovingEventSubject.onNext(false);
                    }, e -> {
                        LogHelper.log(e);
                        mMovingEventSubject.onNext(false);
                    });
            mCompositeDisposable.add(sub);
        });
        mBtnControlZPlus.setOnClickListener(v -> {
            mMovingEventSubject.onNext(true);
            Disposable sub = MoveController.getInstance().stepToPosition(UP, (float) mMoveStep)
                    .observeOn(AndroidSchedulers.mainThread())
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
        mBtnControlZPlus.setEnabled(enabled && !mDisableZ);
        mBtnControlZMinus.setEnabled(enabled && !mDisableZ);
        mSvControlXY.setEnabled(enabled && !mDisableXY);
    }

    public void disabledXY() {
        mDisableXY = true;
        mSvControlXY.setEnabled(false);
    }

    public void disabledZ() {
        mDisableZ = true;
        mBtnControlZMinus.setEnabled(false);
        mBtnControlZPlus.setEnabled(false);
    }
}
