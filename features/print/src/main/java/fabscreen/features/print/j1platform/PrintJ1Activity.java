package fabscreen.features.print.j1platform;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.print.PrintChangeFilamentLandFragment;
import fabscreen.features.print.print.PrintCompleteLandFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.service.machine.MachineInfo;
import fabscreen.platform.base.view.BaseActivity;
import fabscreen.platform.core.ui.view.AreaScrollDrawerLayout;

@Route(path = RoutePath.PRINT_PRINT_J1)
public class PrintJ1Activity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_print);
                PrintJ1Fragment fragment = new PrintJ1Fragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .add(R.id.print_master_container, fragment)
                        .commit();
    }
}
