package fabscreen.features.settings.a400.maintenance.index;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.features.settings.a400.A400SettingsActivity;
import fabscreen.platform.base.receiver.InstallProcessReceiver;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;

public class A400SettingsMaintenanceFragment extends BaseFragment {

    @BindView(R2.id.cl_config_params)
    ConstraintLayout mClConfigParams;
    @BindView(R2.id.cl_module_info)
    ConstraintLayout mClModuleInfo;
    @BindView(R2.id.cl_factory_reset)
    ConstraintLayout mClFactoryReset;
    private SettingsMaintenanceViewModel mViewModel;

    public static Fragment newInstance() {
        return new A400SettingsMaintenanceFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getFragmentScopeViewModel(SettingsMaintenanceViewModel.class);
        initView();
    }

    private void initView() {
        if (mViewModel.getWorkType() == IMachine.WorkType.CNC) {
            mClConfigParams.setVisibility(View.GONE);
        } else {
            ((TextView) mClConfigParams.findViewById(R.id.tv_title)).setText("配置参数");
            mClConfigParams.setVisibility(View.VISIBLE);
        }
        ((TextView) mClModuleInfo.findViewById(R.id.tv_title)).setText(R.string.setting_maintenance_machine_information);
        ((TextView) mClFactoryReset.findViewById(R.id.tv_title)).setText(R.string.setting_restore_to_factory_settings);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_settings_maintenance;
    }

    @OnClick({R2.id.cl_config_params, R2.id.cl_module_info, R2.id.cl_factory_reset})
    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if (id == R.id.cl_config_params) {
            goToConfigParams();
        } else if (id == R.id.cl_module_info) {
            goToModuleInfo();
        } else if (id == R.id.cl_factory_reset) {
            warnFactoryReset();
        }
    }

    private void warnFactoryReset() {
        DecisionDialog.create(requireContext())
                .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, true)
                .setType(DecisionDialog.WARMING_TYPE)
                .setPic(R.drawable.ic_yellow_warn)
                .setTitle(R.string.all_warning)
                .setContent(R.string.a400_reset_msg)
                .setFirstTv(R.string.all_cancel, R.color.select_dialog_white_txt, (dialog, which) -> dialog.dismiss())
                .setSecondTv(R.string.a400_reset, R.color.select_dialog_yellow_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        doFactoryReset();
                    }
                })
                .show();
    }

    private void doFactoryReset() {
//        FabProgressDialog dialog = new FabProgressDialog(requireContext());
//        dialog.setMessage(R.string.settings_firmware_factory_reset);
//        dialog.show();

//        mViewModel.resetMachine()
//                .observeOn(AndroidSchedulers.mainThread())
//                .as(bindToLifecycle())
//                .subscribe(success -> doScreenFactoryReset(), e -> {
//                    dialog.dismiss();
//                    LogHelper.log(e);
//                });

        getServiceContainer().getService(IPreferences.class).getHelper().reset();
        mRouter.routeToWelcome().start(requireContext());
    }

    private void doScreenFactoryReset() {
        Intent resetIntent = new Intent(requireContext(), InstallProcessReceiver.class);
        resetIntent.putExtra("OPERATION", "factory_reset");
        resetIntent.putExtra("PACKAGE_NAME", requireContext().getPackageName());
        requireContext().sendBroadcast(resetIntent);
    }

    private void goToConfigParams() {
        if (requireActivity() instanceof A400SettingsActivity) {
            ((A400SettingsActivity) requireActivity()).goToMaintainConfigParams();
        }
    }

    private void goToModuleInfo() {
        if (requireActivity() instanceof A400SettingsActivity) {
            ((A400SettingsActivity) requireActivity()).goToMachineInfo();
        }
    }
}
