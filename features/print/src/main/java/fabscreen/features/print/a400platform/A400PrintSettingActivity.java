package fabscreen.features.print.a400platform;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.print.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.PRINT_SETTING)
public class A400PrintSettingActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a400_print_setting);
        PrintA400AdjustmentContainerFragment fragment = new PrintA400AdjustmentContainerFragment();
        addFragment(R.id.print_master_container, fragment);
    }
}
