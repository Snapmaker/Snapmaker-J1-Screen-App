package fabscreen.features.machinetools.calibration.a400platform.cnc.toolchange;

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
import fabscreen.features.machinetools.calibration.a400platform.cnc.manualTool.A400CncManualToolViewModel;
import fabscreen.platform.core.ui.view.A400DirectionControlPanel;
import fabscreen.platform.core.ui.view.XYZControlPanelFourRange;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class A400CncToolChange2Fragment extends A400CalibrationBaseFragment {
    @BindView(R2.id.fragment_calibration_image)
    ImageView mIvImage;
    @BindView(R2.id.fragment_calibration_title)
    TextView mTvTitle;
    @BindView(R2.id.fragment_calibration_content)
    TextView mTvContent;
    @BindView(R2.id.cp_a400_calibration_move)
    XYZControlPanelFourRange mCpMove;
    @BindView(R2.id.cp_a400_calibration_move_z)
    A400DirectionControlPanel mCpZMove;


    private A400CncManualToolViewModel mViewModel;

    public static Fragment newInstance() {
        return new A400CncToolChange2Fragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
    }

    private void initView() {

        setTitle(R.string.calibration_cnc_tool_change);
        mTvTopBarContent.setText(R.string.calibration_cnc_tool_change_third_steps);
        mTvTitle.setText(R.string.calibration_cnc_tool_change_first_steps_content_title);
        mTvContent.setText(R.string.calibration_cnc_tool_change_third_steps_content);
        mGuideProgressBar.setMax(3);
        mGuideProgressBar.setProgress(3);
        mGuideProgressBar.setVisibility(View.VISIBLE);
        mCpMove.setVisibility(View.GONE);
        mCpZMove.setVisibility(View.VISIBLE);

        mCpZMove.setStepWidths(0.1f, 1f, 10f, 100f)
                .setOnDirectionClickListener((direction, stepWidth) -> {
                    playNormalClickSound();
                    mViewModel.move(direction, stepWidth);
                });
        mViewModel.getIsMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> {
                    mCpMove.setEnabled(!isMoving);
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

    @OnClick(R2.id.bt_a400_calibration_submit)
    public void onClickSubmit() {
        playNormalClickSound();
        mViewModel.applyZOffset()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    if (success.isSuccess()) {
                        ((A400CncToolChangeAssistantActivity) requireActivity()).gotoComplete();
                    }
                }, LogHelper::log);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_cnc_manual_tool_basic;
    }

    @Override
    protected A400CncManualToolViewModel getViewModel() {
        return getViewModelProvider().get(A400CncManualToolViewModel.class);
    }
}
