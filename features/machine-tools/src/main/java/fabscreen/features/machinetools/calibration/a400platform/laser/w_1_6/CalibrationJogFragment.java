package fabscreen.features.machinetools.calibration.a400platform.laser.w_1_6;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.BaseCalibrationProgressFragment;
import fabscreen.platform.base.view.FabConfirm;
import fabscreen.platform.core.ui.data.MoveController;
import fabscreen.platform.core.ui.view.FileParsingDialog;
import fabscreen.platform.core.ui.view.SteeringView;

public abstract class CalibrationJogFragment extends BaseCalibrationProgressFragment {
    @BindView(R2.id.iv_calibration_desc)
    ImageView mIvCalibrationDesc;
    @BindView(R2.id.tv_calibration_desc_title)
    TextView mTvCalibrationDescTitle;
    @BindView(R2.id.rg_step)
    RadioGroup mRgStep;
    @Nullable
    @BindView(R2.id.sv_control_panel_xy)
    protected SteeringView mSvControlPanelXy;
    @BindView(R2.id.btn_control_panel_z_plus)
    Button mBtnZPlus;
    @BindView(R2.id.btn_control_panel_z_minus)
    Button mBtnZMinus;
    @BindView(R2.id.btn_run_boundary)
    Button mBtnRunBoundary;
    @BindView(R2.id.btn_next)
    protected Button mBtnNext;
    public FileParsingDialog fabLoading;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    protected void initView() {
        fabLoading = FileParsingDialog.create(getActivity())
                .setContent(R.string.move_show);
        initStepWidthChecker();
        initSteeringView();
    }

    private void initStepWidthChecker() {
        ((RadioButton) mRgStep.getChildAt(mViewModel.getStepWidthPos())).setChecked(true);
        mRgStep.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_0_1) {
                mViewModel.changeStepWidth(0);
            } else if (checkedId == R.id.rb_1_0) {
                mViewModel.changeStepWidth(1);
            } else if (checkedId == R.id.rb_10) {
                mViewModel.changeStepWidth(2);
            } else if (checkedId == R.id.rb_100) {
                mViewModel.changeStepWidth(3);
            }
        });
    }

    private void initSteeringView() {
        if (mSvControlPanelXy == null) return;
        mSvControlPanelXy.setOnDirectionClickedListener(state -> {
            playNormalClickSound();
            switch (state) {
                case SteeringView.DIRECTION_UP:
                    mViewModel.moveByStep(MoveController.Direction.FORWARD);
                    break;
                case SteeringView.DIRECTION_DOWN:
                    mViewModel.moveByStep(MoveController.Direction.BACKWARD);
                    break;
                case SteeringView.DIRECTION_LEFT:
                    mViewModel.moveByStep(MoveController.Direction.LEFT);
                    break;
                case SteeringView.DIRECTION_RIGHT:
                    mViewModel.moveByStep(MoveController.Direction.RIGHT);
                    break;
            }
        });
    }


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_manual_focus_calibration;
    }

    @OnClick(R2.id.btn_next)
    void onNextClicked() {
        playNormalClickSound();
        goNext();
    }

    @OnClick(R2.id.btn_control_panel_z_plus)
    void onZPlusClicked() {
        playNormalClickSound();
        mViewModel.moveByStep(MoveController.Direction.UP);
    }

    @OnClick(R2.id.btn_control_panel_z_minus)
    void onZMinusClicked() {
        playNormalClickSound();
        mViewModel.moveByStep(MoveController.Direction.DOWN);
    }

    @Optional
    @OnClick(R2.id.btn_run_boundary)
    void onRunBoundaryClicked() {
        playNormalClickSound();
        FabConfirm.create(requireContext())
                .setIcon(R.drawable.pic_toolhead_run_boundary)
                .setTitle(R.string.toolhead_run_boundary)
                .setDescription(R.string.toolhead_run_boundary_message)
                .setConfirm(R.string.all_confirm, (dialog, which) -> {
                    dialog.dismiss();
                    mViewModel.runBoundary();
                })
                .setCancel(R.string.all_cancel, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();

    }

    protected abstract void goNext();
}
