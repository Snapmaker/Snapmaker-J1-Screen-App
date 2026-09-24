package fabscreen.features.machinetools.calibration.a400platform.laser.w_1_6.rotary;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class CentralAxisInputDiameterFragment extends A400CalibrationBaseFragment {
    public static Fragment newInstance() {
        return new CentralAxisInputDiameterFragment();
    }

    @BindView(R2.id.et_diameter)
    EditText mEtDiameter;
    CalibrationCentralAxisViewModel mViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getActivityScopeViewModel(CalibrationCentralAxisViewModel.class);
        initView();
    }

    private void initView() {
        setTitle(R.string.a400_central_axis_calibration);
        mTvTopBarContent.setText(R.string.a400_set_material_diameter_1_2);
        mGuideProgressBar.setMax(2);
        mGuideProgressBar.setProgress(1);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_calibration_central_axis;
    }

    @OnClick(R2.id.btn_next)
    void onNextClicked() {
        playNormalClickSound();
        mViewModel.setMaterialDiameter(Float.parseFloat(mEtDiameter.getText().toString().trim()));
        ((CalibrationCentralAxisActivity) requireActivity()).goToTouchMaterial();
    }

    @OnTextChanged(value = R2.id.et_diameter, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onTemperatureChange(CharSequence sequence) {
        try {
            float mMaterial = Float.parseFloat(sequence.toString());
            if (mMaterial > 300f) {
                mMaterial = 300f;
                mEtDiameter.setText(String.valueOf(mMaterial));
            } else if (mMaterial < 1f) {
                mMaterial = 1f;
                mEtDiameter.setText(String.valueOf(mMaterial));
            }
        } catch (Exception ignored) {
        }
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
////                                if (ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().modelId ==IMachine.Product.A400){
//                                    MoveController.getInstance().stepToPosition(MoveController.Direction.UP, 100);
////                                }
                                requireActivity().setResult(Activity.RESULT_CANCELED);
                                requireActivity().finish();
                            }, LogHelper::log);
                }));
        fabBackConfirm.show();
    }

}
