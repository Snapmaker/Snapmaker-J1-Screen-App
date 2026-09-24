package fabscreen.features.settings.j1.attendance;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.base.view.SuperToastHelper;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class J1SettingsPlatformHeightFineTuneFragment extends BaseFragment {
    @BindView(R2.id.et_j1_settings_platform_height)
    EditText mEtPlatformHeight;

    @BindView(R2.id.btn_j1_settings_platform_height_save)
    Button mBtnSave;

    private J1SettingsPlatformHeightFineTuneViewModel mViewModel;

    public static Fragment newInstance() {
        return new J1SettingsPlatformHeightFineTuneFragment();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_settings_platform_height_fine_tune;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getFragmentScopeViewModel(J1SettingsPlatformHeightFineTuneViewModel.class);
        initView();
    }

    private void initView() {
        mEtPlatformHeight.setCursorVisible(false);

        mEtPlatformHeight.setOnEditorActionListener((v, actionId, event) -> {
            switch (actionId) {
                case EditorInfo.IME_ACTION_DONE:
                    String result = v.getText().toString();
                    if (result.isEmpty()) {
                        result = String.valueOf(5.00f);
                        DecisionDialog.create(requireContext()).setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, false)
                                .setContent(R.string.j1_dialog_settings_glass_platform_thickness_input_invalid_range)
                                .setFirstTv(R.string.all_ok, R.color.select_dialog_orange_txt, ((dialog, which) -> {
                                    dialog.dismiss();
                                }))
                                .show();
                    }
                    mViewModel.handleInputPlatformHeight(result);
                    break;
            }
            return false;
        });

        mViewModel.getPlatformHeightObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(height -> {
                    mBtnSave.setEnabled(height != -1f);
                    if (height != -1f) {
                        mEtPlatformHeight.setText(String.format(Locale.getDefault(), "%.2f", height));
                    }
                });

        mViewModel.getResultInvalidHintSubject()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(value -> {
                    DecisionDialog.create(requireContext()).setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, false)
                            .setContent(R.string.j1_dialog_settings_glass_platform_thickness_input_invalid_range)
                            .setFirstTv(R.string.all_ok, R.color.select_dialog_orange_txt, ((dialog, which) -> {
                                dialog.dismiss();
                            }))
                            .show();
                });
    }

    @OnClick(R2.id.btn_j1_settings_platform_height_save)
    void onClickSave() {
        mViewModel.savePlatformHeight()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                   if (responseStructure.isSuccess()) {
                       back();
                   } else {
                       new SuperToastHelper.Builder()
                               .setMessage("Set glass thickness failed！")
                               .build()
                               .showToast(requireContext());
                   }
                }, LogHelper::log);
    }

}
