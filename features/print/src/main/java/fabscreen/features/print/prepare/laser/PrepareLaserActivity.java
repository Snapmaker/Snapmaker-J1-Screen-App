package fabscreen.features.print.prepare.laser;


import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.print.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;


@Route(path = RoutePath.PREPARE_LASER)
public class PrepareLaserActivity extends BaseActivity {
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
                gotoLaserXYOriginBasicModeFragment();
                break;
            default:
                break;
        }

    }

    public void gotoLaserXYOriginBasicModeFragment() {
        PrepareLaserXYOriginBasicModeFragment fragment = new PrepareLaserXYOriginBasicModeFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoLaserXYOriginAdvanceModeFragment() {
        PrepareLaserXYOriginAdvanceModeFragment fragment = new PrepareLaserXYOriginAdvanceModeFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoLaserXYOriginSelectModeFragment() {
        PrepareLaserXYOriginSelectModeFragment fragment = new PrepareLaserXYOriginSelectModeFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoLaserWorkHeightAutoMeasureFragment() {
        PrepareLaserWorkHeightAutoMeasureFragment fragment = new PrepareLaserWorkHeightAutoMeasureFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoLaserWorkHeightInputMaterialFragment() {
        PrepareLaserWorkHeightInputMaterialFragment fragment = new PrepareLaserWorkHeightInputMaterialFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoLaserWorkHeightTouchMaterialFragment() {
        PrepareLaserWorkHeightTouchMaterialFragment fragment = new PrepareLaserWorkHeightTouchMaterialFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoLaserWorkHeightManualAdjustFragment() {
        PrepareLaserWorkHeightManualAdjustFragment fragment = new PrepareLaserWorkHeightManualAdjustFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoLaserWorkHeightSelectModeFragment() {
        PrepareLaserWorkHeightSelectModeFragment fragment = new PrepareLaserWorkHeightSelectModeFragment();
        addFragment(R.id.fragment_container, fragment);

    }
}
