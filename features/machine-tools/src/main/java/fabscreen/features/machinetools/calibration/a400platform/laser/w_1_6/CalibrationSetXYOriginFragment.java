package fabscreen.features.machinetools.calibration.a400platform.laser.w_1_6;

import android.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import fabscreen.platform.core.ui.view.ViewUtils;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class CalibrationSetXYOriginFragment extends CalibrationJogFragment {
    public static Fragment newInstance() {
        return new CalibrationSetXYOriginFragment();
    }

    @Override
    protected void initView() {
        super.initView();
        setMainTitle("2-1 Manual Focus Calibration");
        setSubTitle("Set Work Origin (2/4) ");
        setProgress(2, 4);

        mTvCalibrationDescTitle.setText("Set XY Origin");
        mBtnRunBoundary.setVisibility(View.VISIBLE);
        if (mSvControlPanelXy != null) {
            mSvControlPanelXy.setVisibility(View.VISIBLE);
        }

        mBtnRunBoundary.setEnabled(false);
        mViewModel.getLoadBoundaryObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> mBtnRunBoundary.setEnabled(true), LogHelper::log);

        mViewModel.getIsMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> ViewUtils.enableButtons((ViewGroup) requireView(), !isMoving), LogHelper::log);
    }

    @Override
    protected void goNext() {
        new AlertDialog.Builder(requireContext())
                .setTitle("佩戴护目镜")
                .setPositiveButton("继续", (dialog, which) -> setOriginAndGo())
                .setNegativeButton("算了，我没有护目镜", (dialog, which) -> {
                })
                .setCancelable(false)
                .create()
                .show();
    }

    private void setOriginAndGo() {
        mViewModel.setXYOrigin()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> ((A400LaserManualFocusCalibrationActivity) requireActivity()).goToEngravingWork(), LogHelper::log);
    }
}
