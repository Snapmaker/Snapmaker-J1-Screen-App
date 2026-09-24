package fabscreen.features.machinetools.calibration.a400platform.laser.w_10.thicknessMeasurement;

import static fabscreen.platform.core.ui.common.Laser10wThicknessCalibrationViewModel.FIRST_CAPTURE;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.platform.core.ui.common.Laser10wThicknessCalibrationViewModel;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;

public class ThicknessMeasureCalibration21Fragment extends A400CalibrationBaseFragment {
    @BindView(R2.id.tv_a400_calibration_camera_info)
    TextView mTvContentInfo;
    @BindView(R2.id.tv_a400_calibration_camera_info_title)
    TextView mTvContentTitleInfo;

    private BehaviorSubject<Boolean> mIsMovePopUpSubject = BehaviorSubject.create();

    public static Fragment newInstance() {
        return new ThicknessMeasureCalibration21Fragment();
    }

    Laser10wThicknessCalibrationViewModel mViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
        mIsMovePopUpSubject.onNext(true);
        mViewModel.initCameraPosition(FIRST_CAPTURE)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aBoolean -> {
                    mViewModel.switchAFAssistLight(true);
                    mIsMovePopUpSubject.onNext(false);
                }, e -> {
                    LogHelper.log(e);
                    Toast.makeText(getContext(), "初始化移动出问题", Toast.LENGTH_LONG).show();
                    mIsMovePopUpSubject.onNext(false);
                });
    }

    private void initView() {
        setTitle(R.string.calibration_thickness_measure_3_1_title);
        mTvTopBarContent.setText(R.string.calibration_thickness_measure_3_1_content);
        mTvContentTitleInfo.setText(R.string.calibration_thickness_measure_3_1_second_title);
        mTvContentInfo.setText(R.string.calibration_thickness_measure_3_1_second_content);
        mGuideProgressBar.setMax(3);
        mGuideProgressBar.setProgress(2);
        mIsMovePopUpSubject.
                observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        if (requireActivity() instanceof ThicknessMeasureCalibrationActivity) {
                            ((ThicknessMeasureCalibrationActivity) requireActivity()).showDialog();
                        }
                    } else {
                        if (requireActivity() instanceof ThicknessMeasureCalibrationActivity) {
                            ((ThicknessMeasureCalibrationActivity) requireActivity()).dismissDialog();
                        }
                    }
                });
    }


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_calibration_camrea_info;
    }

    @OnClick(R2.id.bt_a400_calibration_camera_info_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() == null) return;
        ((ThicknessMeasureCalibrationActivity) getActivity()).gotToThicknessMeasureCalibration22();
    }

    @Override
    protected Laser10wThicknessCalibrationViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(Laser10wThicknessCalibrationViewModel.class);
    }
}
