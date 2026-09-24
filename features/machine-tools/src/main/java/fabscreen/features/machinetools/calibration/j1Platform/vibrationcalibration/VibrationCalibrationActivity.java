package fabscreen.features.machinetools.calibration.j1Platform.vibrationcalibration;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.calibration.j1Platform.CalibrationSuccessfullyFragment;
import fabscreen.features.machinetools.calibration.j1Platform.J1FilamentFullWidthFragment;
import fabscreen.features.machinetools.calibration.j1Platform.calibrationCheck.CalibrationPrintFragment;
import fabscreen.features.machinetools.calibration.j1Platform.calibrationCheck.J1CalibrationInstallGlassFragment;
import fabscreen.features.machinetools.calibration.j1Platform.calibrationCheck.J1CalibrationLoadIntroFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseActivity;
import fabscreen.platform.base.view.DecisionDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;

@Route(path = RoutePath.TOOLS_CALIBRATION_J1_3DP_CALIBRATION_VIBRATION)
public class VibrationCalibrationActivity extends BaseActivity {

    private boolean mIsGuide;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        init();
    }

    private void init() {
        mIsGuide = getIntent().getBooleanExtra("bool", false);
        if (mIsGuide) {
            goToLoadFilamentIntro();
        } else {
            showGlassPlateCheck();
        }

        VibrationCalibrationViewModel viewModel = getViewModel(VibrationCalibrationViewModel.class);
        viewModel.enableCompensation()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::handleResult);
    }

    private void showGlassPlateCheck() {
        replaceFragment(R.id.fragment_container, J1CalibrationInstallGlassFragment.newInstance());
    }

    public void goToLoadFilamentIntro() {
        replaceFragment(R.id.fragment_container, J1CalibrationLoadIntroFragment.newInstance());
    }

    public void goToLoadFilament() {
        addFragment(R.id.fragment_container, J1FilamentFullWidthFragment.newInstance(0));
    }

    public void goToCalibrationPrint() {
        replaceFragment(R.id.fragment_container, CalibrationPrintFragment.newInstance(1));
    }

    public void goToChooseAreaIntro() {
        replaceFragment(R.id.fragment_container, ChooseAreaIntroFragment.newInstance());
    }

    public void goToChooseXArea() {
        replaceFragment(R.id.fragment_container, ChooseAreaXFragment.newInstance());
    }

    public void goToChooseYArea() {
        replaceFragment(R.id.fragment_container, ChooseAreaYFragment.newInstance());
    }

    public void goToCalibrateSuccess() {
        if (mIsGuide) {
            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setGuideVibrationCompensation(true);
        }
        replaceFragment(R.id.fragment_container, CalibrationSuccessfullyFragment.newInstance(mIsGuide));
    }

    private void handleResult(Boolean enabled) {
        if (!enabled) {
            DecisionDialog.create(this)
                    .setDialogStatus(1, false, false, true, true)
                    .setTitle(R.string.j1_failed_to_enable_feature)
                    .setContent(R.string.j1_failed_to_toggle_function_alert)
                    .setFirstTv(R.string.all_j1_i_know, R.color.select_dialog_orange_txt, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        finish();
                    }).show();
        }
    }
}
