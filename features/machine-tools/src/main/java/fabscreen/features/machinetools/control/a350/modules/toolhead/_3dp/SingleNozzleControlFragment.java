package fabscreen.features.machinetools.control.a350.modules.toolhead._3dp;

import androidx.fragment.app.Fragment;

import fabscreen.features.machinetools.R;

public class SingleNozzleControlFragment extends NozzleControlFragment {
    public static Fragment newInstance() {
        return new SingleNozzleControlFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_control_3dp_page_nozzle;
    }

    @Override
    protected void lazyLoadPageData() {
        // TODO: 2021/11/25  set nozzle target temp.
    }
}
