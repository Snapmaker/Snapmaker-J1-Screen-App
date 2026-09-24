package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingXY;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.a400platform.A400CalibrationActivity;
import fabscreen.features.machinetools.calibration.a400platform.A400CalibrationBaseInfoFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;


public class A400LevelingXYCalibrationInfoFragment extends A400CalibrationBaseInfoFragment {
    public static final int A400_LEVELING_XY_CALIBRATION_PLA = 0;
    public static final int A400_LEVELING_XY_CALIBRATION_PETG = 1;
    public static final int A400_LEVELING_XY_CALIBRATION_ABS = 2;
    public static final int A400_LEVELING_XY_CALIBRATION_CUSTOM = 3;
    protected IPreferences.Helper helper;
    int CalibrationMode;

    public static Fragment newInstance() {
        return new A400LevelingXYCalibrationInfoFragment();
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
        CalibrationMode = helper.getA400BevelingXYMaterialSelection();
        mIvInfoShow.setImageResource(R.drawable.pic_a400_leveling_xy_calibration_info);
        mTvInfoShowTitle.setText(R.string.calibration_XY_offset_calibration_title);
        mTvInfoShowContent.setText(R.string.calibration_A400_XY_manual_mode_content);
        if (CalibrationMode == A400_LEVELING_XY_CALIBRATION_PLA) {
            mTvCheckMode.setText(String.format(Locale.getDefault(), getString(R.string.calibration_A400_XY_material), getString(R.string.all_material_PLA)));
        } else if (CalibrationMode == A400_LEVELING_XY_CALIBRATION_PETG) {
            mTvCheckMode.setText(String.format(Locale.getDefault(), getString(R.string.calibration_A400_XY_material), getString(R.string.all_material_PETG)));
        } else if (CalibrationMode == A400_LEVELING_XY_CALIBRATION_ABS) {
            mTvCheckMode.setText(String.format(Locale.getDefault(), getString(R.string.calibration_A400_XY_material), getString(R.string.all_material_ABS)));
        } else if (CalibrationMode == A400_LEVELING_XY_CALIBRATION_CUSTOM) {
            mTvCheckMode.setText(String.format(Locale.getDefault(), getString(R.string.calibration_A400_XY_material), getString(R.string.all_material_CUSTOM)));
        }

    }


    @OnClick(R2.id.btn_calibration_info_start)
    public void onClickStart() {
        playNormalClickSound();
//        ServiceContainer.getInstance().getService(IRouter.class)
//                .routeWithClassPath(RoutePath.TOOLS_CALIBRATION_A400_3DP_LEVELING_XY)
//                .start(getContext());
        DecisionDialog.create(getContext())
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, false)
                .setType(DecisionDialog.TIP_TYPE)
                .setPic(R.drawable.ic_a400_clean_up_112x112)
                .setTitle(getString(R.string.calibraiton_a400_procedure_start_confirm_dialog_title,
                        getString(R.string.calibration_XY_offset_calibration_title)))
                .setContent(R.string.calibraiton_a400_procedure_start_confirm_dialog_content_3dp)
                .setFirstTv(R.string.all_cancel, R.color.select_dialog_white_txt, (dialog, which) -> {
                    playNormalClickSound();
                    dialog.dismiss();
                    ServiceContainer.getInstance().getService(IMachine.class)
                            .getFDMController()
                            .setCalibrationMode(101)
                            .observeOn(AndroidSchedulers.mainThread())
                            .as(bindToLifecycle())
                            .subscribe(responseStructure -> {
                                if (responseStructure.isSuccess()) {
                                    ServiceContainer.getInstance().getService(IRouter.class)
                                            .routeWithClassPath(RoutePath.TOOLS_CALIBRATION_A400_3DP_LEVELING_XY)
                                            .startForResult(requireActivity(), A400CalibrationActivity.CalibrationType.DUAL_EXTRUDER_XY);
                                }
                            }, LogHelper::log);
                })
                .setSecondTv(R.string.all_next, R.color.select_dialog_yellow_txt, (dialog, which) -> {
                    playNormalClickSound();
                    dialog.dismiss();
                })
                .show();



    }

    @OnClick(R2.id.cl_calibration_check_mode)
    public void onClickCheckMode() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class)
                .routeWithClassPath(RoutePath.TOOLS_CALIBRATION_A400_3DP_LEVELING_XY_CHECK_MODE)
                .start(getContext());
    }

}
