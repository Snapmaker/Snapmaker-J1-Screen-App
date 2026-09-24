package fabscreen.features.machinetools.calibration.s20._3dp;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseActivity;
import fabscreen.platform.core.ui.common.CalibrationViewModel;

@Route(path = RoutePath.TOOLS_CALIBRATION_S20_3DP)
public class Calibration3DPActivity extends BaseActivity {

    private CalibrationViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            setContentView(R.layout.activity_default);
        }

        mViewModel = getViewModel(CalibrationViewModel.class);

        initRootFragment();
    }

    private void initRootFragment() {
        boolean isHeatedLevelingOn = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().get3DPCalibrationHeatedLevelingOn();

        if (isHeatedLevelingOn) {
            startCalibrationHeatedLevelingIntroFragment();
        } else {
            startCalibrationIntroFragment();
        }
    }

    private void startCalibrationIntroFragment() {
        addFragment(R.id.fragment_container, CalibrationIntroFragment.newInstance());
    }

    private void startCalibrationHeatedLevelingIntroFragment() {
        addFragment(R.id.fragment_container, CalibrationHeatedLevelingIntroFragment.newInstance());
    }

    public void startCalibrationPreHeatedFragment() {
        addFragment(R.id.fragment_container, CalibrationPreHeatedBedFragment.newInstance());
    }

    public void startCalibrationFragment() {
        addFragment(R.id.fragment_container, CalibrationFragment.newInstance());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewModel.dispose();
    }
}
