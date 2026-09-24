package fabscreen.features.guide.s20.laser._10w.cameralibration;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.features.guide.s20.laser._10w.Guide10wLaserActivity;
import fabscreen.features.guide.s20.laser._10w.thicknessmeasurementcalibration.Guide10wThicknessMeasurementCalibrationIntroFragment;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.view.GuideProgressBar;

public class Guide10wLaserCameraCalibrationIntroFragment extends BaseFragment {
    @BindView(R2.id.iv_guide_intro_cover)
    ImageView mIvCover;
    @BindView(R2.id.tv_guide_intro_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_guide_intro_content)
    TextView mTvContent;
    @BindView(R2.id.view_guide_progress_bar)
    GuideProgressBar mGuideProgressBar;

    public static Guide10wLaserCameraCalibrationIntroFragment newInstance() {
        return new Guide10wLaserCameraCalibrationIntroFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_guide_intro;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
//        mIvCover.setImageResource(R.drawable.pic_guide_10w_laser_camera_calibration_360x320);

        mTvTitle.setText(R.string.laser_camera_calibration);
        mTvContent.setText(R.string.laser_camera_calibration_intro);
        mGuideProgressBar.setmStepNum(3);
        mGuideProgressBar.setmStepIndex(3);
        mGuideProgressBar.invalidate();
        mGuideProgressBar.setVisibility(View.VISIBLE);
    }

    @OnClick(R2.id.btn_guide_intro_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() == null) return;
        ((Guide10wLaserActivity) getActivity()).start10wCameraCalibrationStep1Fragment();
    }

    @Override
    protected void back() {
        getFragmentManager().popBackStack(Guide10wThicknessMeasurementCalibrationIntroFragment.class.getSimpleName(), 0);
    }
}
