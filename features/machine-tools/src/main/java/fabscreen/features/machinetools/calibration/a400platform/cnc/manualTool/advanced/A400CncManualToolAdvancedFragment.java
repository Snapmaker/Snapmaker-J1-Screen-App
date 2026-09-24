package fabscreen.features.machinetools.calibration.a400platform.cnc.manualTool.advanced;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.features.machinetools.calibration.a400platform.cnc.manualTool.A400CncManualToolViewModel;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.core.ui.view.A400CoordinatesPanel;
import fabscreen.platform.core.ui.view.XYZControlPanelFourRange;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;

public class A400CncManualToolAdvancedFragment extends A400CalibrationBaseFragment {
    @BindView(R2.id.cp_a400_calibration_move)
    XYZControlPanelFourRange mXYZControlPanel;
    @BindView(R2.id.cp_a400_calibration_coordinates)
    A400CoordinatesPanel mCoordinatesPanel;

    PublishSubject<Boolean> mIsMovePopUpSubject = PublishSubject.create();
    private A400CncManualToolViewModel mViewModel;

    public static Fragment newInstance() {
        return new A400CncManualToolAdvancedFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
        checkHome().observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe();
    }

    public Observable<Boolean> checkHome() {
        IMachine service = ServiceContainer.getInstance().getService(IMachine.class);
        if (!service.getMachineStatusSubjectHolder().getValue().isHomed) {
            mIsMovePopUpSubject.onNext(true);
            return service.getMachineController().updateCoordinateSystem(0)
                    .flatMap(machineStatus -> service.getMachineController().home(0))
                    .flatMap(integer -> service.getMachineController().updateCoordinateSystem(1))
                    .doOnNext(machineStatus -> {
                        mIsMovePopUpSubject.onNext(false);
                    })
                    .flatMap(machineStatus -> Observable.just(machineStatus.isHomed));
        } else {
            return Observable.just(true);
        }
    }

    private void initView() {

        List<String> mList = new ArrayList<>();
        mList.add(getString(R.string.work_coordinates));
        mList.add(getString(R.string.machine_coordinates));
        mCoordinatesPanel.setBAxisVisibility(false);
        mCoordinatesPanel.setRunBoundaryVisibility(false);
        mCoordinatesPanel.setCoordinatesList(mList)
                .setOnDirectionClickListener(new A400CoordinatesPanel.OnCoordinatesOnClickListener() {
                    @Override
                    public void onDirectionClicked(int type, int viewId) {
                        playNormalClickSound();
                        switch (type) {
                            case A400CoordinatesPanel.X_TYPE:
                                Vector mXVector = new Vector();
                                mXVector.setX(0);
                                mViewModel.setOrigin(mXVector, viewId);
                                break;

                            case A400CoordinatesPanel.Y_TYPE:
                                Vector mYVector = new Vector();
                                mYVector.setY(0);
                                mViewModel.setOrigin(mYVector, viewId);
                                break;

                            case A400CoordinatesPanel.Z_TYPE:
                                Vector mZVector = new Vector();
                                mZVector.setZ(0);
                                mViewModel.setOrigin(mZVector, viewId);
                                break;
                            case A400CoordinatesPanel.XYZ_TYPE:
                                Vector mXYZVector = new Vector();
                                mXYZVector.setX(0);
                                mXYZVector.setY(0);
                                mXYZVector.setZ(0);
                                mViewModel.setOrigin(mXYZVector, viewId);
                                break;
                        }
                    }

                    @Override
                    public void onPopupOnClicked(int position) {
                        mViewModel.setCoordinateType(position);
                    }

                    @Override
                    public void onClickRunBoundary() {

                    }
                });
        updateCoordinateView();

        setTitle(R.string.manual_tool_title);
        mTvTopBarContent.setText(R.string.manual_tool_subheading);
        mGuideProgressBar.setMax(1);
        mGuideProgressBar.setProgress(1);
        mGuideProgressBar.setVisibility(View.VISIBLE);
        mXYZControlPanel.setStepWidths(0.1f, 1f, 10f, 50f)
                .setOnDirectionClickListener((direction, stepWidth) -> {
                    playNormalClickSound();
                    mViewModel.move(direction, stepWidth);
                });
        mViewModel.getIsMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> {
                    mXYZControlPanel.setEnabled(!isMoving);
                });
        mIsMovePopUpSubject.observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe(aBoolean -> {
            if (aBoolean) {
                fabLoading.show();
            } else {
                fabLoading.dismiss();
            }
        });
        mViewModel.getIsMachineMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> {
                    if (isMoving) {
                        fabLoading.show();
                    } else {
                        fabLoading.dismiss();
                    }
                });
    }

    private void updateCoordinateView() {
        mViewModel.getCoordinateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(vector -> {
                    mCoordinatesPanel.setCoordinatesValue(
                            String.format(Locale.US, "%.2f", vector.getX()),
                            String.format(Locale.US, "%.2f", vector.getY()),
                            String.format(Locale.US, "%.2f", vector.getZ())
                    );
                }, LogHelper::log);
    }

    @OnClick(R2.id.bt_a400_calibration_submit)
    public void onClickSubmit() {
        playNormalClickSound();
        mViewModel.setWorkOrigin()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(response -> {
                    if (response.isSuccess()) {
                        ((A400CncManualToolAdvancedActivity) requireActivity()).gotoCncManualToolComplete();
                    }
                }, LogHelper::log);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_cnc_manual_tool_advanced;
    }

    @Override
    protected A400CncManualToolViewModel getViewModel() {
        return getViewModelProvider().get(A400CncManualToolViewModel.class);
    }
}
