package fabscreen.features.settings.j1;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.base.view.FileLoadingDialog;
import fabscreen.platform.base.view.SuperToastHelper;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public abstract class BaseSettingsAboutFragment extends BaseFragment {

    @BindView(R2.id.tv_machine_name)
    public TextView mTvMachineName;
    @BindView(R2.id.tv_machine_model)
    TextView mTvMachineModel;
    @BindView(R2.id.tv_work_area)
    TextView mTvWorkArea;
    @BindView(R2.id.tv_ip_address)
    TextView mTvIpAddress;
    @BindView(R2.id.tv_mac_address)
    TextView mTvMacAddress;
    @BindView(R2.id.tv_storage)
    TextView mTvStorage;
    @BindView(R2.id.iv_export)
    public ImageView mIvExport;
    @BindView(R2.id.ll_export_logs)
    public LinearLayout mLlExportLogs;

    private long mTime = 0;
    private int mTouchCount = 0;
    private boolean isDeveloper;

    protected S30SettingsAboutViewModel mViewModel;
    protected PopupWindow mExportWindow;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getFragmentScopeViewModel(S30SettingsAboutViewModel.class);
        initView();
    }

    private void initView() {
        mTvMachineName.setText(mViewModel.getUserMachineName());
        mTvMachineModel.setText(mViewModel.getMachineModelName());
        mTvWorkArea.setText(mViewModel.getWorkArea());
        mTvIpAddress.setText(mViewModel.getIPAddress());
        mTvStorage.setText(mViewModel.getStorageUsage());
        if (!TextUtils.isEmpty(mViewModel.getMacAddr())) {
            mTvMacAddress.setText(mViewModel.getMacAddr());
        }
        initExportLogsPopup();
        FileLoadingDialog loading = FileLoadingDialog.create(requireContext(), true).setContent(getString(R.string.j1_usb_exporting));
        mViewModel.getExportStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(state -> handleExportResult(loading, state), LogHelper::log);
    }

    @OnClick({R2.id.ll_edit_name, R2.id.ll_export_logs, R2.id.ll_certification, R2.id.tv_clear_cache})
    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if (id == R.id.ll_edit_name) {
            onClickEditName();
        } else if (id == R.id.ll_export_logs) {
            showPopup();
        } else if (id == R.id.ll_certification) {
            goToCertification();
        } else if (id == R.id.tv_clear_cache) {
            clearCache();
        }
    }

    protected abstract void clearCache();

    private void showPopup() {
        if (mExportWindow.isShowing()) {
            mExportWindow.dismiss();
        } else {
            showAsDropDownWithOffset();
            mExportWindow.setFocusable(true);
            mExportWindow.setTouchable(true);
            mExportWindow.setOutsideTouchable(true);
        }
    }

    protected abstract void showAsDropDownWithOffset();

    protected abstract void onClickEditName();

    protected abstract void initExportLogsPopup();

    protected abstract void goToCertification();

    @OnClick(R2.id.ll_settings_about_model_name)
    public void onclickMachineName() {
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - mTime < 500) {
            mTouchCount += 1;
        } else {
            mTouchCount = 1;
        }
        mTime = currentTime;
        if (mTouchCount >= 5) {
            isDeveloper = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineDeveloper();

            DecisionDialog.create(requireContext())
                    .setDialogStatus(DecisionDialog.BTN_TWO, false, false, false, true)
                    .setType(DecisionDialog.TIP_TYPE)
                    .setContent(isDeveloper ? R.string.all_close_developer_mode_msg : R.string.all_open_developer_mode_msg)
                    .needMoreHeight()
                    .setCanceledOnTouchOutSide(true)
                    .setFirstTv(R.string.all_cancel, R.color.select_dialog_grey_txt, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setSecondTv(isDeveloper ? R.string.all_quit : R.string.all_Enter, R.color.select_dialog_orange_txt, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMachineDeveloper(!isDeveloper);
                            if (requireActivity() instanceof J1SettingsActivity) {
                                ((J1SettingsActivity) requireActivity()).setDeveloperState(!isDeveloper);
                            }
                        }
                    }).show();
        }
    }

    private void handleExportResult(FileLoadingDialog loading, S30SettingsAboutViewModel.ExportState state) {
        switch (state) {
            case ON_START:
                loading.show();
                break;
            case ON_SUCCESS:
                loading.dismiss();
                new SuperToastHelper.Builder()
                        .setDrawable(R.drawable.ic_toast_success)
                        .setMessage(getString(R.string.j1_log_exported))
                        .build()
                        .showToast(requireContext());
                break;
            case ON_FAIL_NO_REMOTE:
                loading.dismiss();
                new SuperToastHelper.Builder()
                        .setMessage(getString(R.string.j1_about_lava_can_not_connect_msg))
                        .build()
                        .showToast(requireContext());
                break;
            case ON_FAIL_NO_U_DISK:
                loading.dismiss();
                new SuperToastHelper.Builder()
                        .setMessage(getString(R.string.j1_usb_flash_drive_first_msg))
                        .build()
                        .showToast(requireContext());
                break;
            case ON_FAIL_OTHER:
                loading.dismiss();
                new SuperToastHelper.Builder()
                        .setMessage(getString(R.string.all_failed))
                        .build()
                        .showToast(requireContext());
                break;
        }
    }
}
