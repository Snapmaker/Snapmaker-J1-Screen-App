package fabscreen.features.print.s20.prepare.rotarylaser;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.s20.preview.PreviewActivity;
import fabscreen.platform.base.Constants;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.lib.LogHelper;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.MachineStatus;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.presenter.ControlXYZPanelWidgetPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class PreviewLaserRotaryMeasureHeightFragment extends BaseFragment {
    @BindView(R2.id.widget_control_panel_xyz_axes_for_4axis)
    View mControlXYZPanel;
    @BindView(R2.id.btn_preview_laser_4axis_measure_height_next)
    Button mBtnNext;
    @BindView(R2.id.top_bar_back)
    Button mBtnBack;
    private ControlXYZPanelWidgetPresenter mControlPanelPresenter;
    private BehaviorSubject<Boolean> mIsMovingSubject = BehaviorSubject.createDefault(false);

    public static PreviewLaserRotaryMeasureHeightFragment newInstance() {
        return new PreviewLaserRotaryMeasureHeightFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.preview_laser_prepare_4axis_measure_height);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_preview_laser_4axis_measure_height;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        mControlPanelPresenter = new ControlXYZPanelWidgetPresenter(disposables);
        mControlPanelPresenter.bind(mControlXYZPanel, 0.1f, 1f, 5f);
        mControlPanelPresenter.connect();

        // bind events
        mControlPanelPresenter.getMovingEventObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> mIsMovingSubject.onNext(isMoving));

        mIsMovingSubject.observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> {
                    mBtnNext.setEnabled(!isMoving);
                    mBtnBack.setEnabled(!isMoving);
                });
    }

    @OnClick(R2.id.btn_preview_laser_4axis_measure_height_next)
    void onClickNext() {
        playNormalClickSound();
        // Pull up Z and move to target position, set origin for it.
        MachineStatus status = ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue();
        float currentX = (float) (status.currentPosition.getX() - ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getX());
        float currentY = (float) (status.currentPosition.getY() - ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getY());
        float currentZ = (float) (status.currentPosition.getZ() - ServiceContainer.getInstance().getService(IMachine.class).getMachineStatusSubjectHolder().getValue().originOffset.getZ());

        float pullUpZ = currentZ + Constants.LASER_10W_CAMERA_FOCAL_LENGTH;

        mIsMovingSubject.onNext(true);
        ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(0)
                .flatMap(result -> {
                    Vector vector = new Vector();
                    vector.setX(currentX);
                    vector.setY(currentY);
                    vector.setZ(pullUpZ);
                    return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().gotoAbsolutePosition(vector);
                })
                .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem(1))
                .flatMap(result -> {
                    Vector vector = new Vector();
                    vector.setX(0);
                    vector.setY(0);
                    vector.setZ(0);
                    vector.setB(0);
                    return ServiceContainer.getInstance().getService(IMachine.class).getMachineController().setWorkOrigin(vector);
                })
                .flatMap(success -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().updateCoordinateSystem())
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(result -> {
                    mIsMovingSubject.onNext(false);

                    if (getActivity() != null) {
                        ((PreviewActivity) getActivity()).gotoLaserPrepareSafetyGogglesFragment(false);
                    }
                }, e -> {
                    mIsMovingSubject.onNext(false);
                    LogHelper.log(e);
                });
    }
}
