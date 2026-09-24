package fabscreen.features.settings.j1.attendance;

import static fabscreen.features.settings.j1.attendance.J1SettingsVibrationCompensationViewModel.*;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.core.ui.view.FabInputDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class J1SettingsVibrationCompensationFragment extends BaseFragment {
    private J1SettingsVibrationCompensationViewModel mViewModel;

    public static Fragment newInstance() {
        return new J1SettingsVibrationCompensationFragment();
    }

    @BindView(R2.id.ll_compensation_value)
    LinearLayout mLlCompensationValue;
    @BindView(R2.id.tv_freq)
    TextView mTvFreq;
    @BindView(R2.id.sw_compensation)
    SwitchCompat mSwCompensation;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_settings_vibration_compensation;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getFragmentScopeViewModel(J1SettingsVibrationCompensationViewModel.class);
        initView();
    }

    private void initView() {
        mSwCompensation.setOnCheckedChangeListener(mCheckedChangeListener);

        mViewModel.getEnableStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::setCheckStateWithoutTriggerOnCheckChanged, LogHelper::log);

        mViewModel.getFreqObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(freq -> mTvFreq.setText(getString(R.string.all_freq_hz_value, freq)), LogHelper::log);
    }

    @OnClick(R2.id.ll_compensation_value)
    void onClickEditValue() {
        editValue();
    }

    /**
     * Use this method instead of directly call onClickEditValue().
     */
    private void editValue() {
        FabInputDialog.create(requireContext())
                .setButton(getString(R.string.all_confirm), (dialogInterface, i) -> {
                    String input = FabInputDialog.getsInstance().getEditTextContent();
                    if (checkValid(input)) {
                        alertChange(input);
                    }
                }).show();
    }

    private boolean checkValid(String input) {
        float value = Float.parseFloat(input);
        boolean valid = value <= FREQ_MAX && value >= FREQ_MIN;
        if (!valid) {
            DecisionDialog.create(requireContext())
                    .setDialogStatus(1, false, false, false, true)
                    .setContent(R.string.j1_settings_vibration_freq_value_invalid_altert)
                    .setFirstTv(R.string.all_j1_i_know, R.color.select_dialog_orange_txt, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        editValue();
                    })
                    .show();
        }
        return valid;
    }

    private void alertChange(String input) {
        DecisionDialog.create(requireContext())
                .setDialogStatus(2, false, false, false, true)
                .setContent(R.string.j1_set_vibration_compensation_value_alert_content)
                .setFirstTv(R.string.all_cancel, R.color.select_dialog_white_txt, (dialog, which) -> dialog.dismiss())
                .setSecondTv(R.string.all_change, R.color.select_dialog_orange_txt, (dialog, which) -> {
                    dialog.dismiss();
                    mViewModel.setCompensationFreq(input);
                })
                .show();
    }

    @OnClick(R2.id.ll_compensation_switch)
    void onClickSwitch() {
        mSwCompensation.toggle();
    }

    private final CompoundButton.OnCheckedChangeListener mCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//            mLlCompensationValue.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            mViewModel.setCompensationEnabled(isChecked)
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(success -> showOperationResult(isChecked, success));
        }
    };

    private void showOperationResult(boolean isChecked, boolean success) {
        // do nothing if success
        if (!success) {
            // revert switch
            setCheckStateWithoutTriggerOnCheckChanged(!isChecked);
            // alert fail
            DecisionDialog.create(requireContext())
                    .setDialogStatus(1, false, false, true, true)
                    .setTitle(isChecked ? R.string.j1_failed_to_enable_feature : R.string.j1_failed_to_disable_feature)
                    .setContent(R.string.j1_failed_to_toggle_function_alert)
                    .setFirstTv(R.string.all_j1_i_know, R.color.select_dialog_orange_txt, (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();
        }
    }

    private void setCheckStateWithoutTriggerOnCheckChanged(Boolean enabled) {
        mSwCompensation.setOnCheckedChangeListener(null);
        mSwCompensation.setChecked(enabled);
//        mLlCompensationValue.setVisibility(enabled ? View.VISIBLE : View.GONE);
        mSwCompensation.setOnCheckedChangeListener(mCheckedChangeListener);
    }
}
