package fabscreen.features.machinetools.setup.singlesingle.bedleveling;

import androidx.fragment.app.Fragment;

import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.view.BaseFragment;

public class SingleSingleBedLevelingIntroFragment extends BaseFragment {
    public static Fragment newInstance() {
        return new SingleSingleBedLevelingIntroFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_setup_intro;
    }

    @OnClick(R2.id.btn_start)
    void onStartClicked() {
        playNormalClickSound();
        ((SingleSingleBedLevelingActivity) requireActivity()).goToBedLeveling();
    }
}
