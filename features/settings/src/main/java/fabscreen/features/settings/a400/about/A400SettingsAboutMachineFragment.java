package fabscreen.features.settings.a400.about;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import fabscreen.features.settings.R;
import fabscreen.features.settings.a400.A400SettingsActivity;
import fabscreen.features.settings.j1.BaseSettingsAboutFragment;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.SuperToastHelper;
import fabscreen.platform.core.ui.view.FabInputDialog;

public class A400SettingsAboutMachineFragment extends BaseSettingsAboutFragment {
    public static A400SettingsAboutMachineFragment newInstance() {
        return new A400SettingsAboutMachineFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_settings_about;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle("About Machine");
    }

    @Override
    protected void onClickEditName() {
        FabInputDialog.create(requireContext())
                .setEditText(mViewModel.getUserMachineName())
                .setButton("Done", (dialog, which) -> {
                    dialog.dismiss();
                    String input = FabInputDialog.getsInstance().getEditTextContent();
                    mTvMachineName.setText(input);
                    getServiceContainer().getService(IPreferences.class).getHelper().setMachineName(input);
                })
                .show();
    }

    @Override
    protected void initExportLogsPopup() {
        View exportView = getLayoutInflater().inflate(R.layout.popup_a400_export_logs, (ViewGroup) requireView(), false);
        TextView tvUsb = exportView.findViewById(R.id.tv_to_usb);
        TextView tvLuban = exportView.findViewById(R.id.tv_to_luban);

        tvUsb.setOnClickListener(v -> {
            Logger.d("Exporting logs to usb disk...");
            playNormalClickSound();
            mExportWindow.dismiss();
            mViewModel.exportLogsToUDisk();
        });

        tvLuban.setOnClickListener(v -> {
            playNormalClickSound();
            Logger.d("Exporting logs to Luban...");
            mExportWindow.dismiss();
            if (mViewModel.isRemoteAvailable()) {
                mViewModel.exportLogsToRemote();
            } else {
                new SuperToastHelper.Builder()
                        .setMessage("Please connect Luban to the machine first.")
                        .build()
                        .showToast(requireContext());
            }
        });

        mExportWindow = new PopupWindow(exportView, (int) DimensUtils.dp2px(360), (int) DimensUtils.dp2px(218));
        mExportWindow.setElevation(8);
        mExportWindow.setOnDismissListener(() -> playArrowAnimation(true));
    }

    @Override
    protected void showAsDropDownWithOffset() {
        playArrowAnimation(false);
        mExportWindow.showAsDropDown(mLlExportLogs, (int) DimensUtils.dp2px(522), (int) DimensUtils.dp2px(-7));
    }

    private void playArrowAnimation(boolean isDismiss) {
        ValueAnimator animator = ValueAnimator.ofFloat(isDismiss ? -90f : 90f, isDismiss ? 90f : -90f);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> mIvExport.setRotation((Float) animation.getAnimatedValue()));
        animator.setDuration(100);
        animator.start();
    }

    @Override
    protected void goToCertification() {
        if (requireActivity() instanceof A400SettingsActivity) {
            ((A400SettingsActivity) requireActivity()).goToLongTextDisplay(
                    R.string.settings_about_certification_page_title,
                    R.string.settings_about_certification_page_content
            );
        }
    }

    @Override
    protected void clearCache() {
        mViewModel.clearCache();
    }
}
