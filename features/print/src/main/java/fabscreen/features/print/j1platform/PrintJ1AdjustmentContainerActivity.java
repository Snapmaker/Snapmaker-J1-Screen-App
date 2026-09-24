package fabscreen.features.print.j1platform;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.print.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.PRINT_PRINT_J1_AJUSTMENT_MENT_CONTAINER)
public class PrintJ1AdjustmentContainerActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_print);
        gotoJ1AdjustmentContainerFragment();
    }

    public void gotoJ1AdjustmentContainerFragment() {
        PrintJ1AdjustmentContainerFragment fragment = new PrintJ1AdjustmentContainerFragment();
        addFragment(R.id.print_master_container, fragment);
    }

}
