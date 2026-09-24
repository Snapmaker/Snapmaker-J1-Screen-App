package fabscreen.features.machinetools.calibration.a400platform.cnc.manualTool;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.a400platform.A400CalibrationBaseInfoFragment;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;


public class A400ManualToolFragment extends A400CalibrationBaseInfoFragment {
    private static final int A400_MANUAL_TOOL_BASIC = 0;
    private static final int A400_MANUAL_TOOL_ADVANCED = 1;
    protected IPreferences.Helper helper;
    int CalibrationMode;

    public static Fragment newInstance() {
        return new A400ManualToolFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        helper = getServiceContainer().getService(IPreferences.class).getHelper();

    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
        updateView();
    }

    private void updateView() {
        mIvInfoShow.setImageResource(R.drawable.pic_a400_manual_tool_setting_info);
        mTvInfoShowTitle.setText(R.string.calibration_manual_tool_setting_title);

        String checkMode;
        if (CalibrationMode == A400_MANUAL_TOOL_BASIC) {
            checkMode = getString(R.string.calibration_base_mode);
            mTvInfoShowContent.setText(R.string.calibration_manual_tool_setting_content);
        } else {
            checkMode = getString(R.string.calibration_advanced_mode);
            mTvInfoShowContent.setText(R.string.calibration_manual_tool_setting_advanced_mode_content);
        }
        mTvCheckMode.setText(checkMode);


    }

    private void getData() {
        CalibrationMode = helper.getA400ManualToolCalibrationMode();
    }

    @OnClick(R2.id.btn_calibration_info_start)
    public void onClickStart() {
        playNormalClickSound();

        DecisionDialog.create(requireContext())
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, false, true)
                .setType(DecisionDialog.TIP_TYPE)
                .setPic(R.drawable.ic_laser_glass)
                .setContent(getResources().getString(R.string.all_manual_tool_tip))
                .setFirstTv(getResources().getString(R.string.all_cancel), R.color.select_dialog_grey_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setSecondTv(getResources().getString(R.string.all_confirm), R.color.select_dialog_yellow_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DecisionDialog.getsInstance().mCancelBtn.setEnabled(false);
                        DecisionDialog.getsInstance().mSecondBtn.setEnabled(false);
                        ServiceContainer.getInstance().getService(IMachine.class)
                                .getCNCController()
                                .setCalibrationMode(3)
                                .observeOn(AndroidSchedulers.mainThread())
                                .as(bindToLifecycle())
                                .subscribe(responseStructure -> {
                                    if (responseStructure.isSuccess()) {
                                        if (CalibrationMode == A400_MANUAL_TOOL_BASIC) {
                                            mRouter.routeWithClassPath(RoutePath.TOOLS_CALIBRATION_A400_CNC_MANUAL_TOOL_BASIC).start(getContext());
                                        } else {
                                            mRouter.routeWithClassPath(RoutePath.TOOLS_CALIBRATION_A400_CNC_MANUAL_TOOL_ADVANCED).start(getContext());
                                        }
                                    }
                                    dialog.dismiss();

                                }, LogHelper::log);
                    }
                }).show();

    }

    @OnClick(R2.id.cl_calibration_check_mode)
    public void onClickCheckMode() {
        playNormalClickSound();
        ServiceContainer.getInstance().getService(IRouter.class)
                .routeWithClassPath(RoutePath.TOOLS_CALIBRATION_A400_CNC_MANUAL_TOOL_CHECK_MODE)
                .start(getContext());
    }

}
