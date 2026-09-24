package fabscreen.features.print.s20.prepare.cnc;


import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.print.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.PREPARE_CNC)
public class PrepareCNCActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_default);

        switch (getApplication().getPackageName()) {
            case "com.snapmaker.fabscreen": {
                break;
            }
            case "com.snapmaker.fabscreenj1":
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                gotoCNCXYZOriginBasicModeFragment();
                break;
            default:
                break;
        }

    }

    public void gotoCNCXYZOriginBasicModeFragment() {
        PrepareCNCXYZOriginBasicModeFragment fragment = new PrepareCNCXYZOriginBasicModeFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoCNCXYZOriginAdvanceModeFragment() {
        PrepareCNCXYZOriginAdvanceModeFragment fragment = new PrepareCNCXYZOriginAdvanceModeFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoCNCXYZOriginSelectModeFragment() {
        PrepareCNCXYZOriginSelectModeFragment fragment = new PrepareCNCXYZOriginSelectModeFragment();
        addFragment(R.id.fragment_container, fragment);
    }

}
