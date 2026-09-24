package fabscreen.features.settings.j1;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import fabscreen.features.settings.R;
import fabscreen.features.settings.common.ExperienceProgramDialogFragment;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.base.view.SuperToastHelper;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static fabscreen.features.settings.j1.J1SettingsInputNameFragment.MACHINE_NAME_KEY;
import static fabscreen.features.settings.j1.J1SettingsInputNameFragment.REQUEST_NAME_KEY;

import java.util.concurrent.TimeUnit;

public class J1SettingsAboutFragment extends BaseSettingsAboutFragment {
    public static J1SettingsAboutFragment newInstance() {
        return new J1SettingsAboutFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_settings_about;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the SettingsActivity's FragmentManager
        requireParentFragment().getParentFragmentManager().setFragmentResultListener(REQUEST_NAME_KEY, this, (requestKey, result) -> {
            String machineName = result.getString(MACHINE_NAME_KEY);
            mTvMachineName.setText(machineName);
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTvMachineModel.setText(R.string.j1_settings_about_j1_models);
    }

    @Override
    protected void onClickEditName() {
        // We will get name in the resultListener under onCreate().
        if (requireActivity() instanceof J1SettingsActivity) {
            ((J1SettingsActivity) requireActivity()).goToNameInput();
        }
    }

    @Override
    protected void initExportLogsPopup() {
        View exportView = getLayoutInflater().inflate(R.layout.popup_j1_export_logs, (ViewGroup) requireView(), false);
        View usbDrive = exportView.findViewById(R.id.v_top_area);
        View lava = exportView.findViewById(R.id.v_bottom_area);
        TextView tvLava = exportView.findViewById(R.id.tv_lava);
        ImageView ivHelp = exportView.findViewById(R.id.iv_help);
        tvLava.setTextColor(mViewModel.isRemoteAvailable() ? 0xFFF7F8FA : 0xFF595A66);
        ivHelp.setVisibility(mViewModel.isRemoteAvailable() ? View.INVISIBLE : View.VISIBLE);

        usbDrive.setOnClickListener(v -> {
            Logger.d("Exporting logs to usb disk...");
            mExportWindow.dismiss();
            mViewModel.exportLogsToUDisk();
        });

        lava.setOnClickListener(v -> {
            Logger.d("Exporting logs to Snapmaker Luban...");
            mExportWindow.dismiss();
            if (mViewModel.isRemoteAvailable()) {
                mViewModel.exportLogsToRemote();
            } else {
                new SuperToastHelper.Builder()
                        .setMessage(getString(R.string.j1_about_lava_can_not_connect_msg))
                        .build()
                        .showToast(requireContext());
            }
        });

        ivHelp.setOnClickListener(v -> {
            new SuperToastHelper.Builder()
                    .setMessage(getString(R.string.j1_about_lava_can_not_connect_msg))
                    .build()
                    .showToast(requireContext());
        });

        mExportWindow = new PopupWindow(exportView, (int) DimensUtils.dp2px(240), (int) DimensUtils.dp2px(124));
        mExportWindow.setElevation(8);
    }

    @Override
    protected void showAsDropDownWithOffset() {
        mExportWindow.showAsDropDown(mIvExport);
    }

    @Override
    protected void goToCertification() {
        ExperienceProgramDialogFragment.newInstance(R.string.j1_setting_about_compliance_certification,
                        R.string.j1_setting_about_compliance_certification_content, false)
                .show(getChildFragmentManager(), "Compliance");
    }

    @Override
    protected void clearCache() {
        DecisionDialog.create(getContext())
                .setDialogStatus(DecisionDialog.BTN_TWO, false, false, false, true)
                .setType(DecisionDialog.WARMING_TYPE)
                .setContent(R.string.j1_settting_about_clean_cache)
                .setContentColor(R.color.palette_grey_french)
                .setFirstTv(getString(R.string.all_cancel), R.color.select_dialog_left_text_color, ((dialog, which) -> {
                    dialog.dismiss();
                }))
                .setSecondTv(getString(R.string.j1_delete), R.color.palette_red_sunset, ((dialog, which) -> {
                    dialog.dismiss();
                    doClearCache();
                }))
                .show();
    }

    private void doClearCache() {
        mViewModel.clearCache()
                .delay(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    requireActivity().finishAffinity();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }, e -> {
                    LogHelper.log(e);
                    showResetErrorDialog("Fail: " + e.getMessage());
                });
    }

    private void showResetErrorDialog(String msg) {
        DecisionDialog.create(requireContext())
                .setContent(msg)
                .setFirstTv(R.string.all_j1_i_know, R.color.select_dialog_orange_txt, (dialog, which) -> dialog.dismiss())
                .show();
    }
}
