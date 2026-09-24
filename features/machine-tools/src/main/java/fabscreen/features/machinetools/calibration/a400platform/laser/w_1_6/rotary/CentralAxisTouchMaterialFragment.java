package fabscreen.features.machinetools.calibration.a400platform.laser.w_1_6.rotary;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.core.ui.data.MoveController;
import fabscreen.platform.core.ui.view.A400XYZBControlPanel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class CentralAxisTouchMaterialFragment extends A400CalibrationBaseFragment {

    @BindView(R2.id.cp_a400_leveling_z_calibration_move)
    A400XYZBControlPanel mXYZBCalibrationControl;
    CalibrationCentralAxisViewModel mViewModel;
    private DecisionDialog mDecisionDialog;

    public static Fragment newInstance() {
        return new CentralAxisTouchMaterialFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getActivityScopeViewModel(CalibrationCentralAxisViewModel.class);
        initView();
        mViewModel.checkHome()
                .flatMap(aBoolean -> aBoolean ? mViewModel.goToRotaryTouchInitPosition() : Observable.just(aBoolean))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aBoolean -> {

                }, LogHelper::log);

    }

    private void initView() {
        setTitle(R.string.a400_central_axis_calibration);
        mTvTopBarContent.setText(R.string.a400_touch_material_2_2);
        mGuideProgressBar.setMax(2);
        mGuideProgressBar.setProgress(2);
        mDecisionDialog = DecisionDialog.create(requireContext()).setDialogStatus(DecisionDialog.BTN_ONE,
                false, false, false, true);

        mXYZBCalibrationControl.setRotaryStuffVisibility(true);
        mXYZBCalibrationControl.setOnDirectionClickListener(new A400XYZBControlPanel.OnDirectionClickListener() {
            @Override
            public void onDirectionClicked(MoveController.Direction direction, float stepWidth) {
                playNormalClickSound();
                mViewModel.moveToPosition(direction)
                        .observeOn(AndroidSchedulers.mainThread())
                        .as(bindToLifecycle())
                        .subscribe(responseStructure -> {
                            if (responseStructure.isGeneralError()) {
                                mDecisionDialog.setContent("机器运动受限")
                                        .setFirstTv(requireContext().getString(R.string.all_confirm),
                                                R.color.select_dialog_blue_txt, (dialog, which) -> {
                                                    dialog.dismiss();
                                                }).show();
                            }
                        }, LogHelper::log);
            }

            @Override
            public void onPositionChange(int position) {
                mViewModel.changeStepWidth(position);
            }
        });

        mViewModel.getMovingSubject()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aBoolean -> {
                    mXYZBCalibrationControl.setEnabled(!aBoolean);
                }, LogHelper::log);
        mViewModel.getIsMovingSubject()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        if (!fabLoading.isShowing()) {
                            fabLoading.show();
                        }
                    } else {
                        fabLoading.dismiss();
                    }
                }, LogHelper::log);
    }

    @OnClick(R2.id.btn_next)
    void goNext() {
        playNormalClickSound();
        // save axis position
        mViewModel.saveRotaryAxisZ()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> finishActivityWithResultOk(), LogHelper::log);
    }

    @Override
    protected void back() {
        fabBackConfirm = DecisionDialog.create(getContext())
                .setTitle(getTitle())
                .setContent(getString(R.string.assistant_back_notice, getTitle()))
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, true)
                .setPic(R.drawable.pic_a400_warning_112x112)
                .setFirstTv(getContext().getResources().getString(R.string.all_cancel), R.color.select_dialog_white_txt, ((dialog, which) -> {
                    dialog.dismiss();
                }))
                .setSecondTv(getContext().getResources().getString(R.string.all_stop), R.color.select_dialog_yellow_txt, ((dialog, which) -> {
                    fabBackConfirm.mCancelBtn.setEnabled(false);
                    fabBackConfirm.mSecondBtn.setEnabled(false);
                    ServiceContainer.getInstance().getService(IMachine.class).getLaserController().exitCalibration(false)
                            .observeOn(AndroidSchedulers.mainThread())
                            .as(bindToLifecycle())
                            .subscribe(success -> {
                                mViewModel.Lift100Z();
                                if (!success.isSuccess()) {
                                    Logger.d("Exit Calibration: " + success);
                                }
                                dialog.dismiss();
                                requireActivity().setResult(Activity.RESULT_CANCELED);
                                requireActivity().finish();
                            }, LogHelper::log);
                }));
        fabBackConfirm.show();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_calibration_axis_touch_material;
    }
}
