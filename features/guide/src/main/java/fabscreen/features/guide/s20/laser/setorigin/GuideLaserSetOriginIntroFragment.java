package fabscreen.features.guide.s20.laser.setorigin;

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
import fabscreen.features.guide.s20.laser.GuideLaserActivity;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class GuideLaserSetOriginIntroFragment extends BaseFragment {
    @BindView(R2.id.iv_guide_intro_cover)
    ImageView mIvCover;
    @BindView(R2.id.tv_guide_intro_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_guide_intro_content)
    TextView mTvContent;

    public static GuideLaserSetOriginIntroFragment newInstance() {
        return new GuideLaserSetOriginIntroFragment();
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
        mIvCover.setImageResource(R.drawable.pic_guide_laser_set_work_origin_360x320);

        mTvTitle.setText(R.string.control_set_origin);
        mTvContent.setText(R.string.guide_laser_set_origin_content);
    }

    @OnClick(R2.id.btn_guide_intro_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((GuideLaserActivity) getActivity()).startSetOriginFragment();
        }
    }
}
