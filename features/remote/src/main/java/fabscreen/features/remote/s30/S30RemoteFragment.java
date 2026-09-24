package fabscreen.features.remote.s30;

import androidx.fragment.app.Fragment;

import fabscreen.features.remote.R;
import fabscreen.platform.base.view.BaseFragment;

public class S30RemoteFragment extends BaseFragment {
    public static Fragment newInstance() {
        return S30RemoteFragment.newInstance();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_s30_remote;
    }
}
