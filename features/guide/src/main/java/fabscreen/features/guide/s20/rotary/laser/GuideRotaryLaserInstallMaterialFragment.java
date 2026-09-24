package fabscreen.features.guide.s20.rotary.laser;

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
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class GuideRotaryLaserInstallMaterialFragment extends BaseFragment {
    @BindView(R2.id.iv_guide_intro_cover)
    ImageView mIvCover;
    @BindView(R2.id.tv_guide_intro_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_guide_intro_content)
    TextView mTvContent;

    public static GuideRotaryLaserInstallMaterialFragment newInstance() {
        return new GuideRotaryLaserInstallMaterialFragment();
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
        mIvCover.setImageResource(R.drawable.pic_guide_laser_rotary_install_material_360x320);

        mTvTitle.setText(R.string.cnc_origin_assistant_install_material_title);
        mTvContent.setText(R.string.laser_calibration_4axis_install_material_content);
    }

    @OnClick(R2.id.btn_guide_intro_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((GuideRotaryLaserActivity) getActivity()).startMeasureHeightIntroFragment();
        }
    }
}
