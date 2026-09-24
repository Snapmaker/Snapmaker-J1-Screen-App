package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingXY;

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
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.DecisionDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class A400LevelingXYCalibrationVerifyResultsFragment extends A400CalibrationBaseFragment {

    @BindView(R2.id.iv_a400_leveling_xy_adjust)
    ImageView mIvLevelingXYVerify;
    @BindView(R2.id.tv_a400_leveling_xy_adjust_content)
    TextView mTvLevelingXYVerify;
    @BindView(R2.id.btn_next)
    TextView mBtnNext;
    @BindView(R2.id.btn_a400_leveling_xy_no_check)
    TextView mBtnReturn;

    public static Fragment newInstance() {
        return new A400LevelingXYCalibrationVerifyResultsFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        setTitle(R.string.calibration_a400_leveling_xy_title);
        mTvTopBarContent.setText(R.string.calibration_a400_leveling_xy_observe_check_model);
        mGuideProgressBar.setMax(6);
        mGuideProgressBar.setProgress(6);
        mGuideProgressBar.invalidate();
        mTvLevelingXYVerify.setText(R.string.calibration_a400_leveling_xy_observe_check_model_content);
        mBtnReturn.setText(R.string.calibartion_a400_leveling_xy_recalibrate);
        mBtnNext.setText(R.string.all_complete);
        mGuideProgressBar.setVisibility(View.VISIBLE);
    }

    @OnClick(R2.id.btn_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            ServiceContainer.getInstance().getService(IMachine.class).getFDMController()
                    .exitCalibration(true)
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(response -> {
                        if (response.isSuccess()) {
                            finishActivityWithResultOk();
                        }
                    });
        }
    }

    @OnClick(R2.id.btn_a400_leveling_xy_no_check)
    void onClickReturn() {
        playNormalClickSound();
        DecisionDialog.create(requireContext())
                .setDialogStatus(DecisionDialog.BTN_ONE, true, false, true, false)
                .setType(DecisionDialog.WARMING_TYPE)
                .setPic(R.drawable.ic_yellow_warn)
                .setTitle(R.string.calibartion_a400_leveling_xy_recalibrate)
                .setContent(R.string.guide_a400_recalibrate_msg)
                .setFirstTv(R.string.all_cancel, R.color.select_dialog_white_txt, ((dialog, which) -> {
                    dialog.dismiss();

                }))
                .setSecondTv(R.string.all_ok, R.color.select_dialog_yellow_txt, ((dialog, which) -> {
                    ServiceContainer.getInstance().getService(IMachine.class).getFDMController()
                            .exitCalibration(false)
                            .observeOn(AndroidSchedulers.mainThread())
                            .as(bindToLifecycle())
                            .subscribe(success -> {
                                if (success.isSuccess()) {
                                    dialog.dismiss();
                                    ServiceContainer.getInstance().getService(IRouter.class).routeToCalibrationPage().startAndClear(getContext());
                                    requireActivity().finish();
                                }
                            });
                })).show();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_leveling_xy_calibration_check_info;
    }

}
