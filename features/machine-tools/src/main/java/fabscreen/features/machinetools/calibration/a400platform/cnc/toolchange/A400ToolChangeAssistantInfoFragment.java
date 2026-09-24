package fabscreen.features.machinetools.calibration.a400platform.cnc.toolchange;

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


public class A400ToolChangeAssistantInfoFragment extends A400CalibrationBaseInfoFragment {
    protected IPreferences.Helper helper;

    public static Fragment newInstance() {
        return new A400ToolChangeAssistantInfoFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        helper = getServiceContainer().getService(IPreferences.class).getHelper();
        updateView();
    }

    private void updateView() {
        mIvInfoShow.setImageResource(R.drawable.pic_a400_bit_assistant_info);
        mTvInfoShowTitle.setText(R.string.calibration_cnc_tool_change);
        mTvInfoShowContent.setText(R.string.cnc_tool_change_assistant_content_);
        mClCheckMode.setVisibility(View.GONE);

    }


    @OnClick(R2.id.btn_calibration_info_start)
    public void onClickStart() {
        playNormalClickSound();
        DecisionDialog.create(requireContext())
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, false, true)
                .setPic(R.drawable.ic_laser_glass)
                .setType(DecisionDialog.TIP_TYPE)
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
                                .setCalibrationMode(1)
                                .observeOn(AndroidSchedulers.mainThread())
                                .as(bindToLifecycle())
                                .subscribe(responseStructure -> {
                                    if (responseStructure.isSuccess()) {
                                        ServiceContainer.getInstance().getService(IRouter.class)
                                                .routeWithClassPath(RoutePath.TOOLS_CALIBRATION_A400_CNC_CHANGE_ASSISTANT)
                                                .start(getContext());
                                    }
                                    dialog.dismiss();
                                }, LogHelper::log);
                    }
                }).show();
    }

}
