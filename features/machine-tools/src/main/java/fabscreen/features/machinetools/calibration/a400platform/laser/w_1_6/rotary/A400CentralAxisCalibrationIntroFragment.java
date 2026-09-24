package fabscreen.features.machinetools.calibration.a400platform.laser.w_1_6.rotary;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.a400platform.A400CalibrationActivity;
import fabscreen.features.machinetools.calibration.a400platform.A400CalibrationBaseInfoFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.DecisionDialog;

public class A400CentralAxisCalibrationIntroFragment extends A400CalibrationBaseInfoFragment {
    protected IPreferences.Helper helper;

    public static Fragment newInstance() {
        return new A400CentralAxisCalibrationIntroFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        helper = getServiceContainer().getService(IPreferences.class).getHelper();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    private void updateView() {
        mIvInfoShow.setImageResource(R.drawable.ic_calibration_central_axis);
        mTvInfoShowTitle.setText(R.string.calibration_central_axis_title);
        mTvInfoShowContent.setText(R.string.calibration_central_axis_content);
        mClCheckMode.setVisibility(View.VISIBLE);
        switch (helper.getA400CentralAxisCalibrationMaterialType()) {
            case 0:
            default:
                mTvCheckMode.setText(R.string.a400_laser_central_axis_calibration_material_cylinder_material);
        }
    }

    @OnClick(R2.id.btn_calibration_info_start)
    void onStartClicked() {
        playNormalClickSound();
        DecisionDialog.create(getContext())
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, false)
                .setType(DecisionDialog.TIP_TYPE)
                .setPic(R.drawable.ic_a400_clean_up_112x112)
                .setTitle(getString(R.string.calibraiton_a400_procedure_start_confirm_dialog_title,
                        getString(R.string.calibration_central_axis_title)))
                .setContent(R.string.calibraiton_a400_procedure_start_confirm_dialog_content_laser)
                .setFirstTv(R.string.all_cancel, R.color.select_dialog_white_txt, (dialog, which) -> {
                    playNormalClickSound();
                    dialog.dismiss();
                })
                .setSecondTv(R.string.all_next, R.color.select_dialog_yellow_txt, (dialog, which) -> {
                    playNormalClickSound();
                    dialog.dismiss();
                    mRouter.routeToCentralAxisCalibration().startForResult(requireActivity(), A400CalibrationActivity.CalibrationType.AXIS_CENTRAL_CALI);
                })
                .show();

    }

    @OnClick(R2.id.cl_calibration_check_mode)
    public void onClickCheckMode() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class)
                .routeWithClassPath(RoutePath.TOOLS_CALIBRATION_A400_LASER_CENTRAL_AXIS_SELECT_MATERIAL)
                .start(getContext());
    }
}
