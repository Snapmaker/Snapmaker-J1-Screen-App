package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingZ.manual;

import android.os.Bundle;
import android.view.View;
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

public class A400LevelingZCalibrationManualFragment extends A400CalibrationBaseFragment {
    @BindView(R2.id.fragment_calibration_image)
    ImageView mIvImage;
    @BindView(R2.id.fragment_calibration_z_content)
    TextView mTvContent;

    @BindView(R2.id.cp_a400_leveling_z_calibration_move)
    A400DirectionControlPanelTemp mCpMove;

    private A400LevelingZViewModel mViewModel;

    public static Fragment newInstance() {
        return new A400LevelingZCalibrationManualFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
        mViewModel.A400LevelingZCalibration(0);
    }

    private void initView() {
        setTitle(R.string.guide_a400_double_extruder_step_1_2_title);
        mTvTopBarContent.setText(R.string.guide_a400_double_extruder_step_1_for_1_3);
        mGuideProgressBar.setMax(4);
        mGuideProgressBar.setVisibility(View.VISIBLE);
        mIvImage.setImageResource(R.drawable.pic_leveling_z_left);
        mViewModel.getIsMovePopUpObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMove -> {
                    if (isMove) {
                        fabLoading.show();
                        int extruderIndex = mViewModel.getExtruderIndex();
                        switch (extruderIndex) {
                            case 0:
                                mTvTopBarContent.setText(R.string.guide_a400_double_extruder_step_1_for_1_3);
                                mIvImage.setImageResource(R.drawable.pic_leveling_z_left);
                                mTvContent.setText(R.string.a400_levenling_z_manual_content);
                                mGuideProgressBar.setProgress(1);
                                mGuideProgressBar.invalidate();
                                break;
                            case 1:
                                mIvImage.setImageResource(R.drawable.pic_leveling_z_right);
                                mTvContent.setText(R.string.a400_levenling_z_manual_content);
                                mTvTopBarContent.setText(R.string.guide_a400_double_extruder_step_1_for_2_3_subtitle);
                                mGuideProgressBar.setProgress(2);
                                mGuideProgressBar.invalidate();
                                break;
                            default:
                                break;
                        }
                    } else {
                        fabLoading.dismiss();
                    }
                });
        mCpMove.setStepWidths(0.02f, 0.1f, 1f, 5).setOnDirectionClickListener(new A400DirectionControlPanelTemp.OnDirectionClickListener() {
            @Override
            public void onDirectionClicked(MoveController.Direction direction, float stepWidth) {
                mViewModel.move(direction, stepWidth);
            }
        });

        mViewModel.getIsMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aBoolean -> {
                    mCpMove.setEnabled(!aBoolean);
                });
    }

    @OnClick(R2.id.bt_a400_leveling_z_calibration_submit)
    public void onClickSubmit() {
        playNormalClickSound();
        int extruderIndex = mViewModel.getExtruderIndex();
        switch (extruderIndex) {
            case 0:
                DecisionDialog.create(getContext())
                        .setTitle(R.string.a400_leveling_z_calibration_manual_save_l)
                        .setContent(R.string.a400_leveling_z_calibration_manual_save_l_content)
                        .setType(DecisionDialog.TIP_TYPE)
                        .setDialogStatus(DecisionDialog.BTN_TWO, false, false, true, true)
                        .setFirstTv(R.string.all_cancel, R.color.select_dialog_white_txt, ((dialog, which) -> {
                            dialog.dismiss();
                        }))
                        .setSecondTv(R.string.all_save, R.color.select_dialog_blue_txt, ((dialog, which) -> {
                            dialog.dismiss();
                            mViewModel.A400LevelingZCalibration(mViewModel.getExtruderIndex() + 1);
                        })).show();
                break;
            case 1:
                DecisionDialog.create(getContext())
                        .setTitle(R.string.a400_leveling_z_calibration_manual_save_r)
                        .setContent(R.string.a400_leveling_z_calibration_manual_save_r_content)
                        .setType(DecisionDialog.TIP_TYPE)
                        .setDialogStatus(DecisionDialog.BTN_TWO, false, false, true, true)
                        .setFirstTv(R.string.all_cancel, R.color.select_dialog_white_txt, ((dialog, which) -> {
                            dialog.dismiss();
                        }))
                        .setSecondTv(R.string.all_save, R.color.select_dialog_blue_txt, ((dialog, which) -> {
                            dialog.dismiss();
                            ((A400LevelingZCalibrationManualActivity) requireActivity()).gotoLevelingZCalibrationComplete();
                        })).show();
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
