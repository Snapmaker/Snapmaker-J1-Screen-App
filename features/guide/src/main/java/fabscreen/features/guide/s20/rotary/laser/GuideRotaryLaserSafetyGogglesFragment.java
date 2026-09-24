package fabscreen.features.guide.s20.rotary.laser;

import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class GuideRotaryLaserSafetyGogglesFragment extends BaseFragment {
    public static GuideRotaryLaserSafetyGogglesFragment newInstance() {
        return new GuideRotaryLaserSafetyGogglesFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_preview_laser_prepare_safety_goggles;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_preview_laser_prepare_safety_goggles_next)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() == null) return;
        ((GuideRotaryLaserActivity) getActivity()).startSetOriginIntroFragment();
    }
}
