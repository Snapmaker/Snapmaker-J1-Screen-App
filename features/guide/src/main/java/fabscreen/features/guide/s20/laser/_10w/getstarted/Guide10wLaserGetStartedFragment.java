package fabscreen.features.guide.s20.laser._10w.getstarted;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.features.guide.s20.laser._10w.Guide10wLaserActivity;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;


public class Guide10wLaserGetStartedFragment extends BaseFragment {

    @BindView(R2.id.tv_guide_a400_10w_laser_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_guide_a400_10w_laser_content)
    TextView mTvContent;
    @BindView(R2.id.view_guide_a400_10w_laser_thickness)
    View mViewThickness;
    @BindView(R2.id.tv_guide_a400_10w_laser_thickness)
    TextView mTvThickness;
    @BindView(R2.id.view_guide_a400_10w_laser_camera)
    View mViewCamera;
    @BindView(R2.id.tv_guide_a400_10w_laser_camera)
    TextView mTvCamera;

    public static Guide10wLaserGetStartedFragment newInstance() {
        return new Guide10wLaserGetStartedFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_guide_10w_laser_get_started;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick({R2.id.lin_guide_a400_10w_laser_thickness})
    public void onClickThickness() {
        playNormalClickSound();
        selectTv(false);
    }

    @OnClick({R2.id.lin_guide_a400_10w_laser_camera})
    public void onClickCamera() {
        playNormalClickSound();
        selectTv(true);
    }

    public void selectTv(boolean isSelectCamera) {
        mViewThickness.setActivated(isSelectCamera ? false : true);
        mTvThickness.setActivated(isSelectCamera ? false : true);

        mViewCamera.setActivated(isSelectCamera ? true : false);
        mTvCamera.setActivated(isSelectCamera ? true : false);


    }

    @OnClick(R2.id.btn_guide_10w_laser_get_started_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() != null) {
            ((Guide10wLaserActivity) getActivity()).startTouchPlatformFragmentIntroFragment();
        }
    }
}
