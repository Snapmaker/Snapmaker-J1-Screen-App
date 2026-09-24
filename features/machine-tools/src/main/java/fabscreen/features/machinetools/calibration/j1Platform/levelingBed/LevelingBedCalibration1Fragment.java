package fabscreen.features.machinetools.calibration.j1Platform.levelingBed;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.J1CalibrationBaseFragment;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.FabConfirm;
import fabscreen.platform.core.ui.data.MoveController;
import fabscreen.platform.core.ui.view.XYZControlPanel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

public class LevelingBedCalibration1Fragment extends J1CalibrationBaseFragment {
    @BindView(R2.id.xyz_panel_touch_platform)
    XYZControlPanel mControlPanel;
    @BindView(R2.id.top_bar_back)
    Button mBtBack;
    @BindView(R2.id.btn_next)
    Button mBtNext;
    private Subject<Boolean> mIsMovingSubject = BehaviorSubject.create();

    public static Fragment newInstance() {
        return new LevelingBedCalibration1Fragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        mControlPanel.setXYEnabled(false);
        mControlPanel.setStepWidths(0.1f, 1f, 10f)
                .setOnDirectionClickListener((direction, stepWidth) -> {
                    mIsMovingSubject.onNext(true);
                    MoveController.getInstance()
                            .moveByStep(direction, stepWidth)
                            .as(bindToLifecycle())
                            .subscribe(responseStructure -> {
                                mIsMovingSubject.onNext(false);
                                if (responseStructure.isSuccess()) {

                                } else if (responseStructure.isGeneralError()) {
                                    FabConfirm.create(getContext())
                                            .setDescription(getString(R.string.all_moving_limitation))
                                            .setConfirm(R.string.all_confirm, (dialog, which) -> {
                                                dialog.dismiss();
                                            });
                                }
                            }, LogHelper::log);
                });

        mIsMovingSubject.observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMove -> {
                    mControlPanel.setZEnabled(!isMove);
                    mBtBack.setEnabled(!isMove);
                    mBtNext.setEnabled(!isMove);
                    mControlPanel.setEnabled(!isMove);
                });
    }

    @OnClick(R2.id.btn_next)
    public void onClickNext() {
        playNormalClickSound();
        fabMoving.show();
        ServiceContainer.getInstance().getService(IMachine.class)
                .getFDMController()
                .calibratePointByIndex(2, false)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    fabMoving.dismiss();
                    if (success.isSuccess()) {
                        if (getActivity() == null) return;
                        ((LevelingBedCalibrationActivity) getActivity()).gotoLevelingBedCalibration2();
                    }
                });

    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_calibration_adjust_height;
    }

}
