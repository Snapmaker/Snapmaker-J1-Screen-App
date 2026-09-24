package fabscreen.features.print.s20.preview;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.print.R;
import fabscreen.features.print.s20.prepare.laser.PreviewLaserMeasureSucceedFragment;
import fabscreen.features.print.s20.prepare.laser.PreviewLaserMeasureThicknessFragment;
import fabscreen.features.print.s20.prepare.laser.PreviewLaserPrepareMaterialFragment;
import fabscreen.features.print.s20.prepare.laser.PreviewLaserPrepareModeFragment;
import fabscreen.features.print.s20.prepare.laser.PreviewLaserPrepareModeNoteFragment;
import fabscreen.features.print.s20.prepare.laser.PreviewLaserPrepareSetOriginFragment;
import fabscreen.features.print.s20.prepare.laser.PreviewLaserPrepareTouchMaterialFragment;
import fabscreen.features.print.s20.prepare.rotarycnc.PreviewCNCPrepareRotaryInstallTailstockFragment;
import fabscreen.features.print.s20.prepare.rotarycnc.PreviewCNCPrepareRotaryOriginRemindFragment;
import fabscreen.features.print.s20.prepare.rotarycnc.PreviewCNCPrepareRotarySetOriginFragment;
import fabscreen.features.print.s20.prepare.rotarylaser.PreviewLaserRotaryInstallMaterialFragment;
import fabscreen.features.print.s20.prepare.rotarylaser.PreviewLaserRotaryMeasureHeightFragment;
import fabscreen.features.print.s20.prepare.rotarylaser.PreviewLaserRotaryMeasureHeightIntroFragment;
import fabscreen.features.print.s20.prepare.rotarylaser.PreviewLaserRotaryPrepareModeFragment;
import fabscreen.features.print.s20.prepare.rotarylaser.PreviewLaserRotarySetMaterialFragment;
import fabscreen.features.print.s20.prepare.rotarylaser.PreviewLaserRotarySetOriginFragment;
import fabscreen.features.print.s20.prepare.safety.PreviewCNCPrepareSafetyGogglesFragment;
import fabscreen.features.print.s20.prepare.safety.PreviewLaserPrepareSafetyGogglesFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.PRINT_PREVIEW)
public class PreviewActivity extends BaseActivity {

    private Bundle mBundle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_default);

        gotoPreviewFragment();
    }

    // preview
    public void gotoPreviewFragment() {
        Intent intent = getIntent();
        mBundle = intent.getExtras();

        PreviewFragment fragment = new PreviewFragment();
        fragment.setArguments(mBundle);

        addFragment(R.id.fragment_container, fragment);
    }

    // prepare

    /**
     * Laser prepare, step 0, select mode
     */
    public void gotoLaserPrepareModeFragment() {
        PreviewLaserPrepareModeFragment fragment = new PreviewLaserPrepareModeFragment();
        addFragment(
                PreviewLaserPrepareModeFragment.class.getSimpleName(),
                R.id.fragment_container,
                fragment
                , true);
    }

    /**
     * Laser prepare, set material height
     * Auto mode, step 1
     */
    public void gotoLaserPrepareMaterialFragment() {
        addFragment(R.id.fragment_container, PreviewLaserPrepareMaterialFragment.newInstance());
    }

    /**
     * Laser prepare, safety goggles
     * <p>
     * Auto mode, step 2
     * Manual mode, step 1
     */
    public void gotoLaserPrepareSafetyGogglesFragment(boolean autoMode) {
        PreviewLaserPrepareSafetyGogglesFragment fragment = new PreviewLaserPrepareSafetyGogglesFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("auto_mode", autoMode);
        fragment.setArguments(bundle);
        addFragment(R.id.fragment_container, fragment);
    }

    /**
     * Laser Prepare, Set Origin
     * <p>
     * Auto mode, step 3
     * Manual mode, step 2
     * <p>
     * also used in CNC prepare
     *
     * @param autoMode boolean
     */
    public void gotoLaserPrepareSetOriginFragment(boolean autoMode) {
        PreviewLaserPrepareSetOriginFragment fragment = new PreviewLaserPrepareSetOriginFragment();
        Bundle bundle = (Bundle) mBundle.clone();// so tricky!
        bundle.putBoolean("auto_mode", autoMode);
        fragment.setArguments(bundle);
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoLaserPrepareModeNoteFragment(boolean autoMode) {
        PreviewLaserPrepareModeNoteFragment fragment = new PreviewLaserPrepareModeNoteFragment();
        Bundle bundle = (Bundle) mBundle.clone();
        bundle.putBoolean("auto_mode", autoMode);
        fragment.setArguments(bundle);
        addFragment(
                PreviewLaserPrepareModeNoteFragment.class.getSimpleName(),
                R.id.fragment_container,
                fragment,
                true);
    }

    public void gotoLaserPrepareMeasureThicknessFragment() {
        addFragment(
                PreviewLaserMeasureThicknessFragment.class.getSimpleName(),
                R.id.fragment_container,
                new PreviewLaserMeasureThicknessFragment(),
                true);
    }

    public void gotoLaserPrepareMeasureSucceedFragment() {
        addFragment(R.id.fragment_container, new PreviewLaserMeasureSucceedFragment());
    }

    public void gotoLaserPrepareTouchMaterialFragment() {
        addFragment(R.id.fragment_container, new PreviewLaserPrepareTouchMaterialFragment());
    }

    // Laser with Rotary
    public void gotoLaserRotarySetupModeFragment() {
        PreviewLaserRotaryPrepareModeFragment fragment = new PreviewLaserRotaryPrepareModeFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoLaserRotarySetOriginFragment(boolean autoMode) {
        PreviewLaserRotarySetOriginFragment fragment = new PreviewLaserRotarySetOriginFragment();
        Bundle bundle = (Bundle) mBundle.clone();
        bundle.putBoolean("auto_mode", autoMode);
        fragment.setArguments(bundle);
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoLaserRotarySetMaterial() {
        PreviewLaserRotarySetMaterialFragment fragment = new PreviewLaserRotarySetMaterialFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoLaserRotaryInstallMaterial() {
        PreviewLaserRotaryInstallMaterialFragment fragment = new PreviewLaserRotaryInstallMaterialFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoLaserRotaryMeasureHeightIntro() {
        PreviewLaserRotaryMeasureHeightIntroFragment fragment = new PreviewLaserRotaryMeasureHeightIntroFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoLaserRotaryMeasureHeight() {
        PreviewLaserRotaryMeasureHeightFragment fragment = new PreviewLaserRotaryMeasureHeightFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    /**
     * CNC Safety Goggles
     * step1
     */
    public void gotoCNCPrepareSafetyGogglesFragment() {
        addFragment(R.id.fragment_container, PreviewCNCPrepareSafetyGogglesFragment.newInstance());
    }

    // CNC with Rotary

    /**
     * CNC Rotary Origin Remind
     * step1
     */
    public void gotoCNCPrepareRotaryOriginRemindFragment() {
        PreviewCNCPrepareRotaryOriginRemindFragment fragment = new PreviewCNCPrepareRotaryOriginRemindFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    /**
     * CNC Rotary Set Origin
     * step3
     */
    public void gotoCNCPrepareRotarySetOriginFragment() {
        PreviewCNCPrepareRotarySetOriginFragment fragment = new PreviewCNCPrepareRotarySetOriginFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    /**
     * CNC Rotary Install Tailstock
     * step4
     */
    public void gotoCNCRotaryInstallTailstockFragment() {
        PreviewCNCPrepareRotaryInstallTailstockFragment fragment = new PreviewCNCPrepareRotaryInstallTailstockFragment();
        Bundle bundle = (Bundle) mBundle.clone();
        fragment.setArguments(bundle);
        addFragment(R.id.fragment_container, fragment);
    }
}
