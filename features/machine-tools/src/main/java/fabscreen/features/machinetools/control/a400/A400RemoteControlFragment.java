package fabscreen.features.machinetools.control.a400;

import androidx.fragment.app.Fragment;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.view.BaseFragment;

public class A400RemoteControlFragment extends BaseFragment {
    public static Fragment newInstance() {
        return new A400RemoteControlFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_control_remote;
    }
}
