package fabscreen.features.machinetools.calibration.a400platform.laser.w_1_6;

import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import fabscreen.platform.core.ui.view.ViewUtils;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * 1.6W laser 3 or 4 axis Touch Material to get material surface "height"
 */
public class CalibrationTouchMaterialFragment extends CalibrationJogFragment {
    public static Fragment newInstance() {
        return new CalibrationTouchMaterialFragment();
    }

    @Override
    protected void initView() {
        super.initView();
        mViewModel.startCalibration();
        boolean rotaryAvailable = mViewModel.isRotaryAvailable();
        setMainTitle("Touch Material (1/4)");
        setSubTitle("1-1 Manual Focus Calibration");
        setProgress(1, 4);

        // Show steering view for rotary surface touching.
        if (rotaryAvailable && mSvControlPanelXy != null) {
            mSvControlPanelXy.setVisibility(View.VISIBLE);
        }

        mViewModel.getIsMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isMoving -> ViewUtils.enableButtons((ViewGroup) requireView(), !isMoving), LogHelper::log);
    }

    @Override
    protected void goNext() {
        mViewModel.saveMaterialSurfaceZ()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    // Material surface z saved.
                    ((A400LaserManualFocusCalibrationActivity) requireActivity()).goToSetOrigin();
                }, LogHelper::log);
    }
}
