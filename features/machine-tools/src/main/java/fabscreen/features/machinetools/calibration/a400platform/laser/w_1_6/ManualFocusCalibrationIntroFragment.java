package fabscreen.features.machinetools.calibration.a400platform.laser.w_1_6;

import androidx.fragment.app.Fragment;

import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.a400platform.A400CalibrationActivity;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;

public class ManualFocusCalibrationIntroFragment extends BaseFragment {
    public static Fragment newInstance() {
        return new ManualFocusCalibrationIntroFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_manual_focus_calibration_intro;
    }

    @OnClick(R2.id.btn_start)
    void onStartClicked() {
        playNormalClickSound();
        DecisionDialog.create(getContext())
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, false)
                .setType(DecisionDialog.TIP_TYPE)
                .setPic(R.drawable.ic_a400_clean_up_112x112)
                .setTitle(getString(R.string.calibraiton_a400_procedure_start_confirm_dialog_title,
                        getString(R.string.calibration_manual_focus_calibration_title)))
                .setContent(R.string.calibraiton_a400_procedure_start_confirm_dialog_content_laser)
                .setFirstTv(R.string.all_cancel, R.color.select_dialog_white_txt, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setSecondTv(R.string.all_next, R.color.select_dialog_yellow_txt, (dialog, which) -> {
                    dialog.dismiss();
                    mRouter.routeToManualFocusCalibration().start(requireContext());
                })
                .show();
    }
}
