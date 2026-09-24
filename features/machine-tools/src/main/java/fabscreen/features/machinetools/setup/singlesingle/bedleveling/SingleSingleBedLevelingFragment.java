package fabscreen.features.machinetools.setup.singlesingle.bedleveling;

import androidx.fragment.app.Fragment;

import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.view.BaseFragment;

public class SingleSingleBedLevelingFragment extends BaseFragment {
    public static Fragment newInstance() {
        return new SingleSingleBedLevelingFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_single_single_leveling;
    }

    @OnClick(R2.id.btn_next_submit)
    void onNextSubmitClicked() {
        playNormalClickSound();
        ((SingleSingleBedLevelingActivity) requireActivity()).setResultAndFinish();
    }
}
