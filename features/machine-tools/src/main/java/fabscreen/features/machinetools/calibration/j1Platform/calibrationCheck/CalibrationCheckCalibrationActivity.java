package fabscreen.features.machinetools.calibration.j1Platform.calibrationCheck;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.calibration.j1Platform.J1FilamentFullWidthFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_CALIBRATION_J1_3DP_CALIBRATION_CHECK)
public class CalibrationCheckCalibrationActivity extends BaseActivity {
    private boolean isGuide = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            setContentView(R.layout.activity_default);
        }
        Intent intent = getIntent();
        isGuide = intent.getBooleanExtra("bool", false);
        gotoInstallGlassPlate();
    }

    public void gotoInstallGlassPlate() {
        addFragment(R.id.fragment_container, J1CalibrationInstallGlassFragment.newInstance());
    }

    public void gotoLoadInfo() {
        addFragment(R.id.fragment_container, J1CalibrationLoadIntroFragment.newInstance());
    }

    public void goToLoadFilament() {
        addFragment(R.id.fragment_container, J1FilamentFullWidthFragment.newInstance(0));
    }

    public void gotoPrintFragment() {
        addFragment(R.id.fragment_container, CalibrationPrintFragment.newInstance(0));
    }

    public void gotoCheckZOffsetCalibration() {
        addFragment(R.id.fragment_container, J1CalibrationCheckZOffsetFragment.newInstance());
    }

    public void gotoCheckXYOffsetCalibration() {
        addFragment(R.id.fragment_container, J1CalibrationCheckXYOffsetFragment.newInstance());
    }

//    public void gotoCalibrationSuccess() {
//        if (isGuide) {
//            Fragment fragment = CalibrationCheckCheckingFragment.newInstance();
//            addFragment(R.id.fragment_container, fragment);
//        } else {
//            ServiceContainer.getInstance().getService(IRouter.class).routeToCalibrationPage().startAndClear(this);
//            finish();
//        }
//    }

//    public void gotoCalibrationSuccess() {
//        if(isGuide){
//            ServiceContainer.getInstance().getService(IPreferences.class).getHel per().setGuideCheckPrint(true);
//            finish();
//        }else {
//            addFragment(R.id.fragment_container, CalibrationSuccessfullyFragment.newInstance());
//        }
//    }

    @Override
    public void addFragment(int containerId, @NonNull Fragment fragment) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("is_guide", isGuide);
        fragment.setArguments(bundle);
        addFragment(null, containerId, fragment, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // TODO: 9/13/22 why?
    @Override
    public void onBackPressed() {
        finish();
    }
}
