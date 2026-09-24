package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingXY;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.FDMController;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.core.ui.view.SlidingRulerView;

public class A400LevelingXYCalibrationAdjustXFragment extends A400CalibrationBaseFragment {
    FDMController fdmController;
    A400LevelingXYViewModel mViewModel;
    @BindView(R2.id.srv_a400_leveling_xy_adjust)
    SlidingRulerView mSrvBar;
    int mIndex = 0;
    private boolean isHaveCheck;

    public static Fragment newInstance() {
        return new A400LevelingXYCalibrationAdjustXFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        fdmController = ServiceContainer.getInstance().getService(IMachine.class).getFDMController();
        initView();
    }

    private void initView() {
        if (getArguments() != null) {
            isHaveCheck = getArguments().getBoolean("is_have_check", false);
        }
        if (isHaveCheck) {
            mTvTopBarContent.setText(R.string.calibration_a400_leveling_xy_content_x);
            mGuideProgressBar.setMax(6);
        } else {
            mTvTopBarContent.setText(R.string.calibration_a400_leveling_xy_content_x_4);
            mGuideProgressBar.setMax(4);
        }
        setTitle(R.string.calibration_a400_leveling_xy_title);
        mIvIco.setVisibility(View.GONE);
        mGuideProgressBar.setProgress(2);
        mGuideProgressBar.setVisibility(View.VISIBLE);
        mSrvBar.setOnProgressChangeListener(index -> mIndex = index);
    }


    @OnClick(R2.id.btn_next)
    void onClickNext() {
        playNormalClickSound();
        DecisionDialog.create(requireContext())
                .setDialogStatus(DecisionDialog.BTN_TWO, false, false, true, true)
                .setType(DecisionDialog.TIP_TYPE)
                .setTitle(R.string.calibration_a400_leveling_xy_x_value_confirm_dialog_title)
                .setContent(R.string.calibration_a400_leveling_xy_x_value_confirm_dialog_content)
                .setFirstTv(R.string.all_cancel, R.color.select_dialog_white_txt, (dialog, which) -> {
                    playNormalClickSound();
                    dialog.dismiss();
                })
                .setSecondTv(R.string.all_save, R.color.select_dialog_blue_txt, (dialog, which) -> {
                    playNormalClickSound();
                    dialog.dismiss();
                    mViewModel.setAdjustX(mIndex);
                    if (getActivity() != null) {
                        ((A400LevelingXYCalibrationActivity) getActivity()).gotoAdjustY();
                    }
                })
                .show();

    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_leveling_xy_calibration_adjust;
    }

    @Override
    protected A400LevelingXYViewModel getViewModel() {
        return getViewModelProvider().get(A400LevelingXYViewModel.class);
    }
}
