package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingZ.sensor;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingZ.A400LevelingZViewModel;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.core.ui.data.MoveController;
import fabscreen.platform.core.ui.view.A400DirectionControlPanelTemp;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class A400LevelingZCalibrationSensor2Fragment extends A400CalibrationBaseFragment {
    @BindView(R2.id.fragment_calibration_image)
    ImageView mIvImage;
    @BindView(R2.id.fragment_calibration_z_content)
    TextView mTvContent;
    @BindView(R2.id.cp_a400_leveling_z_calibration_move)
    A400DirectionControlPanelTemp mCpMove;
    @BindView(R2.id.bt_a400_leveling_z_calibration_submit)
    Button mBtnConfirm;
    @BindView(R2.id.tv_calibration_title)
    TextView mTvTitle;

    private A400LevelingZViewModel mViewModel;

    public static Fragment newInstance() {
        return new A400LevelingZCalibrationSensor2Fragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
        mViewModel.A400LevelingZSensorCalibration(mViewModel.getExtruderIndex() + 1);
    }

    private void initView() {
        setTitle(R.string.calibration_Z_offset_calibration_title);
        mGuideProgressBar.setMax(4);
        mGuideProgressBar.setVisibility(View.VISIBLE);

        mViewModel.getIsMovePopUpObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMove -> {
                    int extruderIndex = mViewModel.getExtruderIndex();
                    switch (extruderIndex) {
                        case 2:
                            mTvTopBarContent.setText(getString(R.string.calibration_headted_z_leveing_z_offset, getString(R.string.all_right), 3, 4));
                            mGuideProgressBar.setProgress(3);
                            mBtnConfirm.setText(R.string.all_next);
                            mGuideProgressBar.invalidate();
                            mTvTitle.setText(R.string.a400_calibration_manual_calibration_of_nozzle_r_title);
                            mTvContent.setText(R.string.a400_calibration_manual_calibration_of_nozzle_r_content);
                            break;
                        case 3:
                            mTvTopBarContent.setText(getString(R.string.calibration_headted_z_leveing_z_offset, getString(R.string.all_heft), 4, 4));
                            mGuideProgressBar.setProgress(4);
                            mGuideProgressBar.invalidate();
                            mBtnConfirm.setText(R.string.all_save);
                            mTvTitle.setText(R.string.a400_calibration_manual_calibration_of_nozzle_l_title);
                            mTvContent.setText(R.string.a400_calibration_manual_calibration_of_nozzle_l_content);
                            break;

                        default:
                            break;
                    }
                });

        mCpMove.setStepWidths(0.02f, 0.1f, 1f, 5)
                .setOnDirectionClickListener(new A400DirectionControlPanelTemp.OnDirectionClickListener() {
                    @Override
                    public void onDirectionClicked(MoveController.Direction direction, float stepWidth) {
                        mViewModel.move(direction, stepWidth);
                    }
                });

        mViewModel.getIsMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aBoolean -> mCpMove.setEnabled(!aBoolean));

    }

    @OnClick(R2.id.bt_a400_leveling_z_calibration_submit)
    public void onClickSubmit() {
        playNormalClickSound();
        int extruderIndex = mViewModel.getExtruderIndex();
        switch (extruderIndex) {
            case 2:

                DecisionDialog.create(requireContext())
                        .setDialogStatus(DecisionDialog.BTN_TWO, false, false, true, true)
                        .setTitle(R.string.calibration_z_leveling_sensor_submit_nozzle_r_title)
                        .setType(DecisionDialog.TIP_TYPE)
                        .setContent(R.string.calibration_z_leveling_sensor_submit_nozzle_r_content)
                        .setFirstTv(R.string.all_cancel, R.color.select_dialog_white_txt, ((dialog, which) -> {
                            dialog.dismiss();
                        }))
                        .setSecondTv(R.string.all_confirm, R.color.select_dialog_blue_txt, (dialog, which) -> {
                            dialog.dismiss();
                            mViewModel.A400LevelingZSensorCalibration(mViewModel.getExtruderIndex() + 1);
                        }).show();
                break;
            case 3:
                DecisionDialog.create(requireContext())
                        .setDialogStatus(DecisionDialog.BTN_TWO, false, false, true, true)
                        .setTitle(R.string.calibration_z_leveling_sensor_submit_nozzle_l_title)
                        .setType(DecisionDialog.TIP_TYPE)
                        .setContent(R.string.calibration_z_leveling_sensor_submit_nozzle_l_content)
                        .setFirstTv(R.string.all_cancel, R.color.select_dialog_white_txt, ((dialog, which) -> {
                            dialog.dismiss();
                        }))
                        .setSecondTv(R.string.all_confirm, R.color.select_dialog_blue_txt, (dialog, which) -> {
                            dialog.dismiss();
                            ((A400LevelingZCalibrationSensorActivity) requireActivity()).gotoLevelingZCalibrationComplete();
                        }).show();

                break;
            default:
                break;
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_leveling_z_calibration_2;
    }

    @Override
    protected A400LevelingZViewModel getViewModel() {
        return getViewModelProvider().get(A400LevelingZViewModel.class);
    }
}
