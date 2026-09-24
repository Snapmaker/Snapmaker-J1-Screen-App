package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingXY;

import static fabscreen.platform.base.service.machine.entity.parts.Extruder.EXTRUDER_DIAMETER_0_4;
import static fabscreen.platform.base.service.machine.entity.parts.Extruder.EXTRUDER_LEFT;
import static fabscreen.platform.base.service.machine.entity.parts.Extruder.EXTRUDER_RIGHT;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;

import java.util.List;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.entity.parts.Extruder;
import fabscreen.platform.base.view.BaseActivity;
import fabscreen.platform.lib.LogHelper;

@Route(path = RoutePath.TOOLS_CALIBRATION_A400_3DP_LEVELING_XY)
public class A400LevelingXYCalibrationActivity extends BaseActivity {
    private boolean isHaveCheck;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            setContentView(R.layout.activity_default);
        }

        List<Extruder> extruderList = ServiceContainer.getInstance().getService(IMachine.class).getFDMController().getToolheadStatusSubjectHolder(0).getValue().getExtruderList();
        float leftDiameter = extruderList.get(EXTRUDER_LEFT).getDiameter();
        float rightDiameter = extruderList.get(EXTRUDER_RIGHT).getDiameter();
        isHaveCheck = (leftDiameter == EXTRUDER_DIAMETER_0_4 && rightDiameter == EXTRUDER_DIAMETER_0_4);
        initRootFragment();
    }

    public void initRootFragment() {
        addFragment(R.id.fragment_container, A400LevelingXYCalibrationPrintFragment.newInstance());
    }

    public void gotoAdjustX() {
        addFragment(R.id.fragment_container, A400LevelingXYCalibrationAdjustXFragment.newInstance());
    }


    public void gotoAdjustY() {
        addFragment(R.id.fragment_container, A400LevelingXYCalibrationAdjustYFragment.newInstance());
    }

    public void gotoCheckInfo() {
        if (isHaveCheck) {
            addFragment(R.id.fragment_container, A400LevelingXYCalibrationCheckInfoFragment.newInstance());
        } else {
            ServiceContainer.getInstance().getService(IMachine.class).getFDMController().exitCalibration(true).as(bindToLifecycle()).subscribe(success -> {
            }, LogHelper::log);
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    public void gotoCheckPrint() {
        addFragment(
                A400LevelingXYCalibrationCheckPrintFragment.class.getSimpleName(),
                R.id.fragment_container,
                A400LevelingXYCalibrationCheckPrintFragment.newInstance(), true);
    }

    public void gotoVerifyResults() {
        addFragment(R.id.fragment_container, A400LevelingXYCalibrationVerifyResultsFragment.newInstance());
    }


    @Override
    public void addFragment(int containerId, @NonNull Fragment fragment) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("is_have_check", isHaveCheck);
        fragment.setArguments(bundle);
        addFragment(null, containerId, fragment, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
    }


}
