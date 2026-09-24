package fabscreen.features.machinetools.control;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.control.j1.J1ControlContainerFragment;
import fabscreen.features.machinetools.control.j1.J1ControlViewModel;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_CONTROL_J1)
public class J1ControlActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getViewModel(J1ControlViewModel.class);
        setContentView(R.layout.activity_default);

        addFragment(R.id.fragment_container, J1ControlContainerFragment.newInstance());
    }
}
