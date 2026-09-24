package fabscreen.features.machinetools.calibration.a400platform.cnc.originAssistant.new_ui;

import android.os.Bundle;

import androidx.annotation.Nullable;

import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.calibration.a400platform.cnc.originAssistant.A400CnOriginAssistantCompleteFragment;
import fabscreen.features.machinetools.calibration.a400platform.cnc.originAssistant.CNCOriginAssistantSetCarvingToolLandFragment;
import fabscreen.features.machinetools.calibration.a400platform.cnc.originAssistant.CNCOriginAssistantSetOriginIntroLandFragment;
import fabscreen.features.machinetools.calibration.a400platform.cnc.originAssistant.CNCOriginAssistantSetOriginLandFragment;
import fabscreen.platform.base.view.BaseActivity;

//@Route(path = RoutePath.TOOLS_CALIBRATION_A400_CNC_ORIGIN_ASSISTANT)
public class A400OriginAssistantActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            setContentView(R.layout.activity_default);
        }

        gotoCNCOriginAssistantSetMaterialFragment();
    }

    /*
     *  CNC Origin Assistant
     *
     *  1. Origin Assistant Get Started
     *  2. Set Material(workpiece)
     *  3. Install Material
     *  4. Set Carving Tool (carving bit, including custom bit)
     *  5. Origin Assistant Set Origin Intro
     *  6. Start Origin Assistant
     *  7. Complete
     */

    /**
     * Set Material(workpiece)
     * <p>
     * Origin Assistant Step1
     */
    public void gotoCNCOriginAssistantSetMaterialFragment() {
        addFragment(R.id.fragment_container, A400CncOriginAssistantSetMaterialFragment.newInstance());
    }

    /**
     * Install Material(workpiece)
     * <p>
     * Origin Assistant Step2
     */
    public void gotoCNCOriginAssistantInstallMaterialFragment() {
        addFragment(R.id.fragment_container, A400CncOriginAssistantInstallMaterialFragment.newInstance());
    }

    /**
     * Set Carving Tool
     * <p>
     * Origin Assistant Step3
     */
    public void gotoCNCOriginAssistantSetCarvingToolFragment() {
        addFragment(R.id.fragment_container, CNCOriginAssistantSetCarvingToolLandFragment.newInstance());
    }

    /**
     * Set Origin Intro
     * <p>
     * Origin Assistant Step4
     */
    public void gotoCNCOriginAssistantSetOriginIntroFragment() {
        addFragment(R.id.fragment_container, CNCOriginAssistantSetOriginIntroLandFragment.newInstance());
    }

    /**
     * Set Origin
     * <p>
     * Origin Assistant Step6
     */
    public void gotoCNCOriginAssistantSetOriginFragment() {
        addFragment(R.id.fragment_container, CNCOriginAssistantSetOriginLandFragment.newInstance());
    }

    /**
     * Origin Assistant Complete
     * <p>
     * Origin Assistant Step7
     */
    public void gotoCNCOriginAssistantCompleteFragment() {
        addFragment(R.id.fragment_container, A400CnOriginAssistantCompleteFragment.newInstance());
    }
}
