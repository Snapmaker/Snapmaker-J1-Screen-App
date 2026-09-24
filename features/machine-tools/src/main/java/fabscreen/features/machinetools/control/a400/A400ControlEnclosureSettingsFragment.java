package fabscreen.features.machinetools.control.a400;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import java.util.List;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.structure.OpenDoorDetectionState;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.base.view.MenuAdapter;
import fabscreen.platform.core.ui.common.A400SwitchCompat;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class A400ControlEnclosureSettingsFragment extends BaseFragment {

    public static Fragment newInstance() {
        return new A400ControlEnclosureSettingsFragment();
    }

    @BindView(R2.id.switch_3dp)
    A400SwitchCompat mSwitch3dp;
    @BindView(R2.id.switch_laser)
    A400SwitchCompat mSwitchLaser;
    @BindView(R2.id.switch_cnc)
    A400SwitchCompat mSwitchCnc;

    private MenuAdapter mMenuAdapter;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        setTitle(getString(R.string.a400_control_enclosure_setttings));
        List<OpenDoorDetectionState> doorDetectionEnabled = getServiceContainer().getService(IMachine.class).getMachineController().getEnclosure().getEnclosureStatusValue().getDoorDetectionEnabled();

        for (OpenDoorDetectionState o : doorDetectionEnabled) {
            switch (o.getWorkType()) {
                case 1:
                    mSwitch3dp.setChecked(o.getState());
                    break;
                case 2:
                    mSwitchLaser.setChecked(o.getState());
                    break;
                case 3:
                    mSwitchCnc.setChecked(o.getState());
                    break;
                default:
                    Logger.d("---FDT--- Unknown job type:" + o.getWorkType());

            }
        }
    }

    @OnCheckedChanged(R2.id.switch_3dp)
    public void onClickFDM(CompoundButton view, boolean isCheck) {
        playSwitchSound();
        setChangeState(IMachine.WorkType.FDM, isCheck);
    }

    @OnCheckedChanged(R2.id.switch_laser)
    public void onClickLaser(CompoundButton view, boolean isCheck) {
        playSwitchSound();
        setChangeState(IMachine.WorkType.LASER, isCheck);
    }

    @OnCheckedChanged(R2.id.switch_cnc)
    public void onClickCnc(CompoundButton view, boolean isCheck) {
        playSwitchSound();
        setChangeState(IMachine.WorkType.CNC, isCheck);
    }

    private void setChangeState(IMachine.WorkType workType, boolean isCheck) {
        getServiceContainer().getService(IMachine.class).getMachineController().getEnclosure()
                .setEnclosureDoorDetection(workType, isCheck)
                .doOnNext(responseStructure -> getServiceContainer().getService(IMachine.class).getMachineController().getEnclosure().requestInfo())
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    if (!responseStructure.isSuccess()) {
                        showErrorDialog();
                    }
                }, e -> {
                    LogHelper.log(e);
                    showErrorDialog();
                });
    }


    public void showErrorDialog() {
        DecisionDialog.create(requireContext())
                .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
                .setType(DecisionDialog.WARMING_TYPE)
                .setContent(R.string.a400_control_air_purifier_settings_setting_fail_msg)
                .setFirstTv(R.string.all_ok, R.color.select_dialog_white_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_enclosure_settings;
    }
}
