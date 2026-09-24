package fabscreen.features.settings.j1;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.features.settings.common.S30FirmwareUpdateViewModel;
import fabscreen.features.settings.common.S30FirmwareUpdateViewModel.ChangelogItem;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.lib.update.UpdateFileParser;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.view.J1ProgressButton;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class J1SettingsFirmwareUpdateFragment extends BaseFragment {

    @BindView(R2.id.tv_version)
    TextView mTvVersion;
    @BindView(R2.id.tv_new_version)
    TextView mTvNewVersion;
    @BindView(R2.id.tv_current_version)
    TextView mTvCurrentVersion;
    @BindView(R2.id.ll_change_log)
    LinearLayout mLlChangeLog;
    @BindView(R2.id.btn_download_update)
    J1ProgressButton mBtnUpdate;
    @BindView(R2.id.tv_update_state)
    TextView mTvUpdateState;

    @BindView(R2.id.group_checking)
    Group mGroupChecking;
    @BindView(R2.id.group_check_fail)
    Group mGroupCheckFail;
    @BindView(R2.id.group_latest)
    Group mGroupLatest;
    @BindView(R2.id.group_new_version_content)
    Group mGroupNewVersionContent;
    @BindView(R2.id.group_old_version_content)
    Group mGroupOldVersionContent;

    private S30FirmwareUpdateViewModel mViewModel;
    //为了处理点击升级弹窗自动跳转这里且下载
    private boolean mIsDownload = false;

    public static Fragment newInstance(boolean download) {
        Fragment fragment = new J1SettingsFirmwareUpdateFragment();
        Bundle args = new Bundle();
        args.putBoolean("download", download);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getFragmentScopeViewModel(S30FirmwareUpdateViewModel.class);
        mIsDownload = requireArguments().getBoolean("download");
        initView();
    }

    private void initView() {
        mTvCurrentVersion.setText(getString(R.string.all_update_current_version) + mViewModel.getCurrentVersion());
        mTvVersion.setText(mViewModel.getCurrentVersion());

        mViewModel.getNewVersionInfoObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::onGetNewVersionInfo, LogHelper::log);

        mViewModel.getFirmwareStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::refreshFirmwareStatus, LogHelper::log);

        mViewModel.getDownloadProgressObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(progress -> {
                    int totalSizeInMegabyte = mViewModel.getVersionSizeInMegabyte();
                    int downloadedSizeInMegabyte = (int) (totalSizeInMegabyte * progress / 100f);
                    mBtnUpdate.setDownloadedSize(downloadedSizeInMegabyte);
                }, LogHelper::log);
    }

    private void onGetNewVersionInfo(S30FirmwareUpdateViewModel.VersionInfo version) {
        mTvNewVersion.setText(version.name);
        displayChangelogs(version.changelogs);
        mBtnUpdate.setMaxSize(version.fileSize);
    }

    private void displayChangelogs(List<ChangelogItem> items) {
        mLlChangeLog.removeAllViews();
        for (ChangelogItem item : items) {
            TextView textView = new TextView(requireContext());
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.palette_grey_french));
            textView.setText(item.words);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
            if (item.type == ChangelogItem.ChangelogType.TITLE) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                layoutParams.leftMargin = (int) DimensUtils.dp2px(0f);
            } else {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                layoutParams.leftMargin = (int) DimensUtils.dp2px(12f);
            }
            mLlChangeLog.addView(textView, layoutParams);
        }
    }

    public void setsa(boolean is) {
        mIsDownload = is;
    }

    private void refreshFirmwareStatus(S30FirmwareUpdateViewModel.FirmwareDisplayStatus status) {
        Logger.d("firmware status updated: %s", status);
        mGroupCheckFail.setVisibility(View.INVISIBLE);
        mGroupChecking.setVisibility(View.INVISIBLE);
        mGroupLatest.setVisibility(View.INVISIBLE);
        mGroupNewVersionContent.setVisibility(View.INVISIBLE);
        mGroupOldVersionContent.setVisibility(View.INVISIBLE);
        switch (status) {
            case CHECKING:
                mGroupOldVersionContent.setVisibility(View.VISIBLE);
                mGroupChecking.setVisibility(View.VISIBLE);
                break;
            case CHECK_FAIL:
                mGroupOldVersionContent.setVisibility(View.VISIBLE);
                mGroupCheckFail.setVisibility(View.VISIBLE);
                break;
            case LATEST:
                mGroupOldVersionContent.setVisibility(View.VISIBLE);
                mGroupLatest.setVisibility(View.VISIBLE);
                break;
            case TO_BE_DOWNLOADED:
                mGroupNewVersionContent.setVisibility(View.VISIBLE);
                mBtnUpdate.setState(J1ProgressButton.State.IDLE);
                if (mIsDownload) {
                    mViewModel.startDownload();
                }
                break;
            case DOWNLOADING:
                mGroupNewVersionContent.setVisibility(View.VISIBLE);
                mBtnUpdate.setState(J1ProgressButton.State.DOWNLOADING);
                break;
            case DOWNLOADED:
                mGroupNewVersionContent.setVisibility(View.VISIBLE);
                mBtnUpdate.setState(J1ProgressButton.State.DOWNLOADED);
                mTvUpdateState.setVisibility(View.VISIBLE);
                mTvUpdateState.setTextColor(ContextCompat.getColor(getContext(), R.color.palette_green_mountain_meadow));
                mTvUpdateState.setText(R.string.j1_setting_update_firmware_downloaded);
                break;
            case DOWNLOAD_FAIL:
                mGroupNewVersionContent.setVisibility(View.VISIBLE);
                mBtnUpdate.setState(J1ProgressButton.State.IDLE);
                mTvUpdateState.setVisibility(View.VISIBLE);
                mTvUpdateState.setTextColor(ContextCompat.getColor(requireContext(), R.color.palette_red_sunset));
                mTvUpdateState.setText(R.string.j1_setting_update_network_abnormal);
                break;
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_firmware_update;
    }

    @OnClick(R2.id.btn_download_update)
    void onUpdateClicked() {
        switch (mViewModel.getCurrentStatus()) {
            case TO_BE_DOWNLOADED:
            case DOWNLOAD_FAIL:
                mViewModel.startDownload();
                break;
            case DOWNLOADING:
                mViewModel.cancelDownload();
                break;
            case DOWNLOADED:
                goToUpdate(UpdateFileParser.getBigBinPath(requireContext().getApplicationContext()), true);
                break;
        }
    }

    @OnClick(R2.id.tv_local_install)
    void onLocalInstallClicked() {
        playNormalClickSound();
        mRouter.routeToFilesPage(4).startForResult(this, 1);
    }

    private void goToUpdate(String filePath, boolean isLocal) {
        mRouter.routeToUpdateInProgress(filePath, isLocal).start(requireContext());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                // update file got, copy and update
                if (data == null) return;
                String filePath = data.getStringExtra("file_path");
                boolean isLocal = data.getBooleanExtra("is_local", false);
                goToUpdate(filePath, isLocal);
            }
        }
    }
}
