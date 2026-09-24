package fabscreen.features.guide.s20._3dp;

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
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.common.CalibrationViewModel;

public class Guide3DPCalibrationIntroFragment extends BaseFragment {

    @BindView(R2.id.iv_guide_intro_cover)
    ImageView mIvCover;
    @BindView(R2.id.tv_guide_intro_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_guide_intro_content)
    TextView mTvContent;
    @BindView(R2.id.btn_guide_intro_next)
    Button mBtnIntroNext;
    private CalibrationViewModel mViewModel;

    public static Guide3DPCalibrationIntroFragment newInstance() {
        return new Guide3DPCalibrationIntroFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = getViewModel();
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
    protected CalibrationViewModel getViewModel() {
        return getViewModelProvider().get(CalibrationViewModel.class);
    }

    private void initView() {
//        mIvCover.setImageResource(R.drawable.pic_guide_3dp_calibration_360x320);
        mTvTitle.setText(R.string.calibration_intro_calibrate_the_bed);
        mTvContent.setText(R.string.guide_3dp_calibration_content);
        mBtnIntroNext.setText(R.string.all_start);
    }

    @OnClick(R2.id.btn_guide_intro_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() == null) return;
        ((Guide3DPActivity) getActivity()).startCalibrationFragment();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            // Triggered when back from other fragments.
            // This ensures that, every time we click "Start" at this page, it is a fresh new "Start".
            mViewModel.resetBehaviors();
            mViewModel.clearCalibrationPointData(false);
        }
    }
}
