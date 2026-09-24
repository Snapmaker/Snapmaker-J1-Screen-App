package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingZ;

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
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;


public class A400LevelingZCalibrationInfoFragment extends A400CalibrationBaseInfoFragment {
    private static final int A400_LEVELING_Z_CALIBRATION_AUTO = 0;
    private static final int A400_LEVELING_Z_CALIBRATION_MANUAL = 1;
    private static final int A400_LEVELING_Z_CALIBRATION_SENSOR = 2;
    protected IPreferences.Helper helper;
    String CalibrationModePath = "";
    int CalibrationMode;
    private int mCaliType;

    public static Fragment newInstance() {
        return new A400LevelingZCalibrationInfoFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        helper = getServiceContainer().getService(IPreferences.class).getHelper();

    }

    private void updateView() {
        CalibrationMode = helper.getA400LevelingZCalibrationMode();
        mIvInfoShow.setImageResource(R.drawable.pic_a400_leveling_z_calibration_info);
        mTvInfoShowTitle.setText(R.string.calibration_Z_offset_calibration_title);
        if (CalibrationMode == A400_LEVELING_Z_CALIBRATION_AUTO) {
            mTvCheckMode.setText(R.string.calibration_auto_mode);
            mTvInfoShowContent.setText(R.string.calibration_A400_Z_zuto_mode_content);
        } else if (CalibrationMode == A400_LEVELING_Z_CALIBRATION_MANUAL) {
            mTvCheckMode.setText(R.string.calibration_manual_mode);
            mTvInfoShowContent.setText(R.string.calibration_A400_Z_manual_mode_content);
        } else if (CalibrationMode == A400_LEVELING_Z_CALIBRATION_SENSOR) {
            mTvCheckMode.setText(R.string.calibration_Z_sensor_calibration);
            mTvInfoShowContent.setText(R.string.calibration_A400_Z_sensor_mode_content);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    private void startZCalibrationMode() {
        int CalibrationModeId = 0;
        mCaliType = 0;

        switch (CalibrationMode) {
            case A400_LEVELING_Z_CALIBRATION_AUTO:
                CalibrationModeId = 52;
                CalibrationModePath = RoutePath.TOOLS_CALIBRATION_A400_3DP_LEVELING_Z_AUTOMATIC;
                mCaliType = A400CalibrationActivity.CalibrationType.Z_CALI_AUTO;
                break;
            case A400_LEVELING_Z_CALIBRATION_MANUAL:
                CalibrationModeId = 53;
                CalibrationModePath = RoutePath.TOOLS_CALIBRATION_A400_3DP_LEVELING_Z_MANUAL;
                break;
            case A400_LEVELING_Z_CALIBRATION_SENSOR:
                CalibrationModeId = 54;
                CalibrationModePath = RoutePath.TOOLS_CALIBRATION_A400_3DP_LEVELING_Z_SENSOR;
                break;
            default:
                break;
        }
        if (CalibrationModePath.isEmpty() || CalibrationModeId == 0) return;
        ServiceContainer.getInstance().getService(IMachine.class)
                .getFDMController()
                .setCalibrationMode(CalibrationModeId)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        ServiceContainer.getInstance().getService(IRouter.class)
                                .routeWithClassPath(CalibrationModePath)
                                .startForResult(requireActivity(), mCaliType);
                    }
                }, LogHelper::log);
    }

    @OnClick(R2.id.btn_calibration_info_start)
    public void onClickStart() {
        playNormalClickSound();
        DecisionDialog.create(getContext())
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, false)
                .setType(DecisionDialog.TIP_TYPE)
                .setPic(R.drawable.ic_a400_clean_up_112x112)
                .setTitle(getString(R.string.calibraiton_a400_procedure_start_confirm_dialog_title,
                        getString(R.string.calibration_Z_offset_calibration_title)))
                .setContent(R.string.calibraiton_a400_procedure_start_confirm_dialog_content_3dp)
                .setFirstTv(R.string.all_cancel, R.color.select_dialog_white_txt, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setSecondTv(R.string.all_next, R.color.select_dialog_yellow_txt, (dialog, which) -> {
                    dialog.dismiss();
                    startZCalibrationMode();
                })
                .show();
    }

    @OnClick(R2.id.cl_calibration_check_mode)
    public void onClickCheckMode() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class)
                .routeWithClassPath(RoutePath.TOOLS_CALIBRATION_A400_3DP_LEVELING_Z_CHECK_MODE)
                .start(getContext());
    }

}
