package fabscreen.features.machinetools.calibration.a400platform.laser.w_10.thicknessMeasurement;

import static fabscreen.platform.core.ui.common.Laser10wThicknessCalibrationViewModel.FIRST_CAPTURE;
import static fabscreen.platform.core.ui.common.Laser10wThicknessCalibrationViewModel.MEASURE_CAPTURE;
import static fabscreen.platform.core.ui.common.Laser10wThicknessCalibrationViewModel.SECOND_CAPTURE;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import butterknife.BindView;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.core.ui.common.CalibrationCaptureResult;
import fabscreen.platform.core.ui.common.Laser10wThicknessCalibrationViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class ThicknessMeasureCalibration22Fragment extends A400CalibrationBaseFragment {

    @BindView(R2.id.auto_thickness_measure_calibration_22_content)
    TextView mTvPointCount;
    private Laser10wThicknessCalibrationViewModel mViewModel;
    @Laser10wThicknessCalibrationViewModel.CaptureCount
    private int mCaptureCount = FIRST_CAPTURE;

    public static Fragment newInstance() {
        return new ThicknessMeasureCalibration22Fragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
        mViewModel.captureAndCalculate(FIRST_CAPTURE);

    }

    private void initView() {
        setTitle(R.string.calibration_thickness_measure_4_1_title);
        mTvTopBarContent.setText(R.string.calibration_thickness_measure_4_1_content);
        mTvPointCount.setText(R.string.calibration_thickness_measure_calibrating);
        mGuideProgressBar.setMax(3);
        mGuideProgressBar.setProgress(3);
        mViewModel.getCaptureResultObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::onGetCaptureResult);
    }

    private void onGetCaptureResult(CalibrationCaptureResult result) {
        Logger.d("result >>" + result);
        if (result.which == SECOND_CAPTURE) {
            mTvPointCount.setText(R.string.calibration_thickness_measure_verifying);
        }
        if (result.isSuccess) {
            switch (result.which) {
                case FIRST_CAPTURE:
                case SECOND_CAPTURE:
                    mViewModel.initCameraPosition(mCaptureCount + 1)
                            .observeOn(AndroidSchedulers.mainThread())
                            .as(bindToLifecycle())
                            .subscribe(success -> {
                                if (!success) return;
                                mCaptureCount += 1;
                                mViewModel.captureAndCalculate(mCaptureCount);
                            });
                    break;
                case MEASURE_CAPTURE:
                    mViewModel.switchAFAssistLight(false);
                    mViewModel.exitCalibration();
                    finishActivityWithResultOk();
                    break;
                default:
                    break;
            }
        } else {
            showFailDialog();
        }

    }

    private void showFailDialog() {
        DecisionDialog.create(requireContext())
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, false)
                .setType(DecisionDialog.ERROR_TYPE)
                .setPic(R.drawable.ic_tips_error_dark)
                .setTitle(R.string.calibration_thickness_measure_failed)
                .setContent(R.string.calibration_thickness_measure_failed_content)
                .setFirstTv(getActivity().getResources().getString(R.string.all_quit), R.color.select_dialog_white_txt, (dialog, which) -> {
                    dialog.dismiss();
                    mViewModel.switchAFAssistLight(false);
                    mViewModel.exitCalibration();
                    requireActivity().finish();
                })
                .setSecondTv(getActivity().getResources().getString(R.string.all_retry), R.color.select_dialog_red_txt, (dialog, which) -> {
                    dialog.dismiss();
                    mViewModel.switchAFAssistLight(true);
                    mViewModel.initCameraPosition(FIRST_CAPTURE)
                            .observeOn(AndroidSchedulers.mainThread())
                            .as(bindToLifecycle())
                            .subscribe(aBoolean -> {
                                mViewModel.captureAndCalculate(mCaptureCount);
                            });
                })
                .show();
    }

    @Override
    protected Laser10wThicknessCalibrationViewModel getViewModel() {
        return getViewModelProvider().get(Laser10wThicknessCalibrationViewModel.class);
    }


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_thickness_measure_calibration_41;
    }

}
