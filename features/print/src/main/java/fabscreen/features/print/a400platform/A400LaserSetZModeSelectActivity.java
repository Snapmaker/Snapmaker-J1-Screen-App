package fabscreen.features.print.a400platform;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.print.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.PRINT_LASER_SET_Z_SELECT)
public class A400LaserSetZModeSelectActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        Fragment fragment = A400LaserSetZModeSelectFragment.newInstance();
        addFragment(R.id.fragment_container, fragment);
    }
}
