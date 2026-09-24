package fabscreen.features.machinetools.calibration.a400platform.laser.w_10.cameraCalibration;

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


public class A400CameraCalibration10wInfoFragment extends A400CalibrationBaseInfoFragment {
    protected IPreferences.Helper helper;

    public static Fragment newInstance() {
        return new A400CameraCalibration10wInfoFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        helper = getServiceContainer().getService(IPreferences.class).getHelper();
        updateView();
    }

    private void updateView() {
        mIvInfoShow.setImageResource(R.drawable.pic_a400_camera_calibration_info);
        mTvInfoShowTitle.setText(R.string.calibration_camera_calibration_title);
        mTvInfoShowContent.setText(R.string.calibration_camera_calibration_content);
        mClCheckMode.setVisibility(View.GONE);
    }

    @OnClick(R2.id.btn_calibration_info_start)
    public void onClickStart() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IMachine.class)
                .getLaserController()
                .setCalibrationMode(2)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    if (responseStructure.isSuccess()) {
                        DecisionDialog.create(getContext())
                                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, false)
                                .setType(DecisionDialog.TIP_TYPE)
                                .setPic(R.drawable.ic_a400_clean_up_112x112)
                                .setTitle(getString(R.string.calibraiton_a400_procedure_start_confirm_dialog_title,
                                        getString(R.string.calibration_camera_calibration_title)))
                                .setContent(R.string.calibraiton_a400_procedure_start_confirm_dialog_content_laser)
                                .setFirstTv(R.string.all_cancel, R.color.select_dialog_white_txt, (dialog, which) -> {
                                    playNormalClickSound();
                                    dialog.dismiss();
                                })
                                .setSecondTv(R.string.all_next, R.color.select_dialog_yellow_txt, (dialog, which) -> {
                                    playNormalClickSound();
                                    dialog.dismiss();
                                    ServiceContainer.getInstance().getService(IRouter.class)
                                            .routeWithClassPath(RoutePath.TOOLS_CALIBRATION_A400_LASER_10W_CAMERA_CALIBRATION)
                                            .startForResult(requireActivity(), A400CalibrationActivity.CalibrationType.CAMERA_CALI);
                                })
                                .show();

                    }
                }, LogHelper::log);
    }

}
