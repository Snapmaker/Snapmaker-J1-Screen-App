package fabscreen.features.guide.s20.laser._10w.touchplatform;

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
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.view.GuideProgressBar;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class Guide10wLaserTouchPlatformIntroFragment extends BaseFragment {
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

    public static Guide10wLaserTouchPlatformIntroFragment newInstance() {
        return new Guide10wLaserTouchPlatformIntroFragment();
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
        mIvCover.setImageResource(R.drawable.pic_laser_10w_settings_touch_platform_360x305);

        mTvTitle.setText(R.string.settings_laser_toolhead_focus_calibration_title);
        mTvContent.setText(R.string.guide_10w_laser_focus_calibration_content);
        mBtNext.setText(R.string.all_start);
        mGuideProgressBar.setmStepNum(3);
        mGuideProgressBar.setmStepIndex(1);
        mGuideProgressBar.invalidate();
        mGuideProgressBar.setVisibility(View.VISIBLE);

    }

    @OnClick(R2.id.btn_guide_intro_next)
    void onClickNext() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IMachine.class).getPrintController().getHeaderSecurityStatus().observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(headerSecurity -> {
                    if (headerSecurity.status == 0) {
                        if (getActivity() == null) return;
                        ((Guide10wLaserActivity) getActivity()).startTouchPlatformFragment();
                    }
                });
    }
}
