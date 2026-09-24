package fabscreen.features.print.a400platform;

import androidx.fragment.app.Fragment;

import fabscreen.features.print.R;
import fabscreen.platform.base.view.BaseFragment;

public class TouchMaterialFragment extends BaseFragment {
    public static Fragment newInstance() {
        return new TouchMaterialFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_touch_platform;
    }
}
