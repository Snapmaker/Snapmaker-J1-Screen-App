package fabscreen.features.settings.j1.attendance;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.features.settings.j1.J1SettingsActivity;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class J1SettingsAttendanceFragment extends BaseFragment {
    public static Fragment newInstance() {
        return new J1SettingsAttendanceFragment();
    }

    @BindView(R2.id.sw_filament_l)
    SwitchCompat mSwFilamentL;
    @BindView(R2.id.sw_filament_r)
    SwitchCompat mSwFilamentR;
    @BindView(R2.id.sw_home_stall_detection)
    SwitchCompat mSwHomeStallDetection;

    // Not implemented yet.
    @BindView(R2.id.sw_lighting)
    SwitchCompat mSwLighting;

    @BindView(R2.id.tv_vibration_compensation_enabled)
    TextView mTvCompensationEnabled;

    @BindView(R2.id.tv_platform_height_fine_tune_value)
    TextView mTvPlatformHeight;

    private J1SettingsAttendanceViewModel mViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getFragmentScopeViewModel(J1SettingsAttendanceViewModel.class);
        initView();
    }

    private void initView() {
        mSwFilamentL.setChecked(mViewModel.isRunoutRecoveryEnabled(0));
        mSwFilamentR.setChecked(mViewModel.isRunoutRecoveryEnabled(1));
        mSwLighting.setChecked(mViewModel.isLightOn());
//        mSwHomeStallDetection.setChecked(mViewModel.isHomeStallDetectionEnabled());

        mSwFilamentL.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mViewModel.setRunoutRecoveryEnabled(0, isChecked);
        });

        mSwFilamentR.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mViewModel.setRunoutRecoveryEnabled(1, isChecked);
        });

        mSwHomeStallDetection.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            mViewModel.setHomeStallDetection(isChecked);
        }));

        mSwLighting.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mViewModel.setLightingEnabled(isChecked);
        });

        mViewModel.getVibrationCompensationEnabledObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(enabled -> mTvCompensationEnabled.setText(enabled ? getString(R.string.j1_vibration_compensation_enabled) : getString(R.string.j1_vibration_compensation_disabled)), LogHelper::log);

        mViewModel.getHomeStallDetectionObservable()
                .skip(1)
                .take(1)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(enabled -> {
                    mSwHomeStallDetection.setChecked(enabled);
                });

//        mViewModel.getPlatformHeightObservable()
//                .observeOn(AndroidSchedulers.mainThread())
//                .as(bindToLifecycle())
//                .subscribe(platformHeight -> {
//                    mTvPlatformHeight.setText(platformHeight < 0 ?
//                            "N/A" :
//                            String.format(Locale.getDefault(), "%.1f", platformHeight)
//                                    + getString(R.string.all_unit_mm));
//                });
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_settings_attendance;
    }

    @OnClick({R2.id.ll_wizard,
            R2.id.ll_filament_runout_l,
            R2.id.ll_filament_runout_r,
            R2.id.ll_lighting,
            R2.id.ll_vibration_compensation,
            R2.id.tv_factory_reset,
            R2.id.ll_home_stall_detection,
            R2.id.ll_platform_height_fine_tune,
            R2.id.ll_hot_end_pid_auto_tune
    })
    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if (id == R.id.ll_wizard) {
            mRouter.routeToWelcome().start(requireContext());
        } else if (id == R.id.ll_filament_runout_l) {
            mSwFilamentL.toggle();
        } else if (id == R.id.ll_filament_runout_r) {
            mSwFilamentR.toggle();
        } else if (id == R.id.ll_lighting) {
            mSwLighting.toggle();
        } else if (id == R.id.ll_vibration_compensation) {
            ((J1SettingsActivity) requireActivity()).goToVibrationSettings();
        } else if (id == R.id.ll_home_stall_detection) {
            mSwHomeStallDetection.toggle();
        } else if (id == R.id.ll_platform_height_fine_tune) {
            ((J1SettingsActivity) requireActivity()).goToPlatformHeightFineTuneSettings();
        } else if (id == R.id.ll_hot_end_pid_auto_tune) {
            ((J1SettingsActivity) requireActivity()).goToHotEndPIDAutoTune();
        } else if (id == R.id.tv_factory_reset) {
            DecisionDialog.create(getContext())
                    .setDialogStatus(DecisionDialog.BTN_TWO, false, false, false, true)
                    .setType(DecisionDialog.WARMING_TYPE)
                    .setContent(R.string.j1_setting_general_factory_factory_reset_content)
                    .setContentColor(R.color.palette_grey_french)
                    .setFirstTv(getString(R.string.all_cancel), R.color.select_dialog_left_text_color, ((dialog, which) -> {
                        dialog.dismiss();
                    }))
                    .setSecondTv(getString(R.string.j1_setting_general_factory_reset), R.color.palette_red_sunset, ((dialog, which) -> {
                        DecisionDialog.getsInstance().mCancelBtn.setEnabled(false);
                        DecisionDialog.getsInstance().mSecondBtn.setEnabled(false);
                        factoryReset(dialog);
                    }))
                    .show();
        }
    }

    private void factoryReset(DialogInterface dialog) {
        mViewModel.J1FactoryReset()
                .delay(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    dialog.dismiss();
                    if (!success) {
                        Logger.e("Factory reset fail: cannot reset machine.");
                        showResetErrorDialog("Factory reset fail: cannot reset machine.");
                        return;
                    }
                    requireActivity().finishAffinity();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }, e -> {
                    dialog.dismiss();
                    LogHelper.log(e);
                    showResetErrorDialog("Fail to reset: " + e.getMessage());
                });
    }

    private void showResetErrorDialog(String msg) {
        DecisionDialog.create(requireContext())
                .setContent(msg)
                .setFirstTv(R.string.all_j1_i_know, R.color.select_dialog_orange_txt, (dialog, which) -> dialog.dismiss())
                .show();
    }
}
