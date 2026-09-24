package fabscreen.features.machinetools.calibration.j1Platform;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_CALIBRATION_J1)
public class J1CalibrationActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        gotoJ1Calibration();
    }

    public void gotoJ1Calibration() {
        Intent intent = getIntent();
        Fragment fragment = J1CalibrationFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putBoolean("is_guide", intent.getBooleanExtra("is_guide", false));
        fragment.setArguments(bundle);
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoGuideSuccess() {
        IPreferences.Helper helper = ServiceContainer.getInstance().getService(IPreferences.class).getHelper();
        helper.setGuideLevelingBed(false);
        helper.setGuideLevelingZ(false);
        helper.setGuideLevelingXY(false);
        helper.setGuideCheckPrint(false);
        helper.setGuideVibrationCompensation(false);
        helper.setGuideCalibration(true);
        setResult(RESULT_OK);
        finish();
    }
}
