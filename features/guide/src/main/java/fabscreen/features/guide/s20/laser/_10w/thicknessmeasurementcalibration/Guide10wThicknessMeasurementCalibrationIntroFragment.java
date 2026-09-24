package fabscreen.features.guide.s20.laser._10w.thicknessmeasurementcalibration;

import static fabscreen.platform.core.ui.common.Laser10wThicknessCalibrationViewModel.FIRST_CAPTURE;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.features.guide.s20.laser._10w.Guide10wLaserActivity;
import fabscreen.features.guide.s20.laser._10w.touchplatform.Guide10wLaserTouchPlatformIntroFragment;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.common.Laser10wThicknessCalibrationViewModel;
import fabscreen.platform.core.ui.presenter.CoordinateSystemPresenter;
import fabscreen.platform.core.ui.view.GuideProgressBar;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class Guide10wThicknessMeasurementCalibrationIntroFragment extends BaseFragment {
    @BindView(R2.id.iv_guide_intro_cover)
    ImageView mIvCover;
    @BindView(R2.id.tv_guide_intro_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_guide_intro_content)
    TextView mTvContent;
    @BindView(R2.id.view_guide_progress_bar)
    GuideProgressBar mGuideProgressBar;
    @BindView(R2.id.btn_guide_intro_next)
    Button mBtNext;
    @BindView(R2.id.top_bar_back)
    Button mBtnBack;
    private Laser10wThicknessCalibrationViewModel mViewModel;

    public static Guide10wThicknessMeasurementCalibrationIntroFragment newInstance() {
        return new Guide10wThicknessMeasurementCalibrationIntroFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
        // We can only go ahead after finishing switching coordinate.
        CoordinateSystemPresenter coordinateSystemPresenter = new CoordinateSystemPresenter(disposables);
        coordinateSystemPresenter.setOnCoordinateSwitchListener(this::prepareHead);
        coordinateSystemPresenter.ensureCoordinate(1);
        setButtonsEnabled(false);
    }


    /**
     * Set proper exposeTime(now we set 1) and init camera position for distance measuring.
     * Laser point won't be on the material surface if we don't init the camera position.
     */
    private void prepareHead() {
        mViewModel.initCameraPosition(FIRST_CAPTURE)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    if (success) {
                        setButtonsEnabled(true);
                    } else {
                        // If fail, we can't start but should have ability to back.
                        mBtnBack.setEnabled(true);
                    }
                }, LogHelper::log);
    }


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_guide_intro;
    }

    @Override
    protected Laser10wThicknessCalibrationViewModel getViewModel() {
        return getViewModelProvider().get(Laser10wThicknessCalibrationViewModel.class);
    }

    private void initView() {
//        mIvCover.setImageResource(R.drawable.pic_10w_maesure_material_thickness_360x320);
        mBtNext.setText(R.string.all_start);
        mTvTitle.setText(R.string.settings_laser_thickness_measure_calibration_title);
        mTvContent.setText(R.string.settings_laser_thickness_measure_calibration_page_desc);
        mGuideProgressBar.setmStepNum(3);
        mGuideProgressBar.setmStepIndex(2);
        mGuideProgressBar.setVisibility(View.VISIBLE);
    }

    @OnClick(R2.id.btn_guide_intro_next)
    void onClickNext() {
        playNormalClickSound();
        mViewModel.switchAFAssistLight(true);
        if (getActivity() == null) return;
        ((Guide10wLaserActivity) getActivity()).Guide10wThicknessMeasureCalibrationPointsFragment();
    }

    private void setButtonsEnabled(boolean enabled) {
        mBtnBack.setEnabled(enabled);
        mBtNext.setEnabled(enabled);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            prepareHead();
        }
    }

    @Override
    protected void back() {
        getActivity().getSupportFragmentManager().popBackStack(Guide10wLaserTouchPlatformIntroFragment.class.getSimpleName(), 0);
    }

}
