package fabscreen.features.settings.a400.update;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;

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
import fabscreen.platform.core.ui.view.A400ProgressButton;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class A400SettingsFirmwareUpdateFragment extends BaseFragment {

    @BindView(R2.id.tv_new_version)
    TextView mTvNewVersion;
    @BindView(R2.id.ll_change_log)
    LinearLayout mLlChangeLog;
    @BindView(R2.id.btn_update)
    A400ProgressButton mBtnUpdate;
    @BindView(R2.id.tv_version_size)
    TextView mTvVersionSize;
    @BindView(R2.id.tv_setting_bar_right)
    TextView mTvLocalUpgrade;
    @BindView(R2.id.tv_new_version_tip)
    TextView mTvNewVersionTip;
    @BindView(R2.id.group_new_version_content)
    Group mGroupNewVersionContent;
    @BindView(R2.id.group_checking)
    Group mGroupChecking;
    @BindView(R2.id.group_check_fail)
    Group mGroupCheckFail;
    @BindView(R2.id.group_latest)
    Group mGroupLatest;

    private S30FirmwareUpdateViewModel mViewModel;

    public static Fragment newInstance() {
        return new A400SettingsFirmwareUpdateFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getFragmentScopeViewModel(S30FirmwareUpdateViewModel.class);
        initView();
    }

    private void initView() {
        mGroupLatest.setVisibility(View.INVISIBLE);
        mGroupCheckFail.setVisibility(View.INVISIBLE);
        mGroupLatest.setVisibility(View.INVISIBLE);
        mGroupNewVersionContent.setVisibility(View.INVISIBLE);
        mTvLocalUpgrade.setVisibility(View.VISIBLE);
        mTvLocalUpgrade.setText("Local Update");
        LinearGradient linearGradient = new LinearGradient(0, 0, mTvLocalUpgrade.getPaint().getTextSize() * mTvLocalUpgrade.getText().length(), 0, 0xFF1A41F5, 0xFF1A8CF5, Shader.TileMode.CLAMP);
        mTvLocalUpgrade.getPaint().setShader(linearGradient);
        mTvLocalUpgrade.invalidate();
        setTitle("Firmware Version (" + mViewModel.getCurrentVersion() + ")");

        mViewModel.getNewVersionInfoObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::onGetNewVersionInfo, LogHelper::log);

        mViewModel.getFirmwareStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::refreshStatus, LogHelper::log);

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
        mTvNewVersion.setText("Firmware: " + version.name);
        displayChangelogs(version.changelogs);
        mTvVersionSize.setText(version.fileSize + "M | " + version.releaseTime);
        mBtnUpdate.setMaxSize(version.fileSize);
    }

    private void refreshStatus(S30FirmwareUpdateViewModel.FirmwareDisplayStatus status) {
        switch (status) {
            case CHECKING:
                mGroupChecking.setVisibility(View.VISIBLE);
                break;
            case CHECK_FAIL:
                mGroupChecking.setVisibility(View.INVISIBLE);
                mGroupCheckFail.setVisibility(View.VISIBLE);
                break;
            case LATEST:
                mGroupChecking.setVisibility(View.INVISIBLE);
                mGroupLatest.setVisibility(View.VISIBLE);
                break;
            case TO_BE_DOWNLOADED:
                mGroupChecking.setVisibility(View.INVISIBLE);
                mGroupNewVersionContent.setVisibility(View.VISIBLE);
                mTvNewVersionTip.setText("New version found");
                mTvNewVersionTip.setTextColor(0xffffab00);
                mBtnUpdate.setState(A400ProgressButton.State.IDLE);
                break;
            case DOWNLOADING:
                mGroupChecking.setVisibility(View.INVISIBLE);
                mGroupNewVersionContent.setVisibility(View.VISIBLE);
                mTvNewVersionTip.setText("New version found");
                mTvNewVersionTip.setTextColor(0xffffab00);
                mBtnUpdate.setState(A400ProgressButton.State.DOWNLOADING);
                break;
            case DOWNLOADED:
                mGroupChecking.setVisibility(View.INVISIBLE);
                mGroupNewVersionContent.setVisibility(View.VISIBLE);
                mTvNewVersionTip.setText("New version downloaded");
                mTvNewVersionTip.setTextColor(0xff62c864);
                mBtnUpdate.setState(A400ProgressButton.State.DOWNLOADED);
                break;
            case DOWNLOAD_FAIL:
                mGroupChecking.setVisibility(View.INVISIBLE);
                mTvNewVersionTip.setText("Download fail");
                mTvNewVersionTip.setTextColor(0xffd0021b);
                mBtnUpdate.setState(A400ProgressButton.State.IDLE);
                break;
        }
    }

    private void displayChangelogs(List<ChangelogItem> items) {
        mLlChangeLog.removeAllViews();
        for (int i = 0; i < items.size(); i++) {
            ChangelogItem item = items.get(i);
            TextView textView = new TextView(requireContext());
            textView.setTextColor(Color.WHITE);
            textView.setText(item.words);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
            if (item.type == ChangelogItem.ChangelogType.TITLE) {
                textView.setTextColor(0xffc9c9c9);
                textView.setTypeface(Typeface.DEFAULT_BOLD);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                layoutParams.leftMargin = (int) DimensUtils.dp2px(0f);
                layoutParams.bottomMargin = (int) DimensUtils.dp2px(12f);
                if (i > 0) {
                    layoutParams.topMargin = (int) DimensUtils.dp2px(48f);
                }
            } else {
                textView.setTextColor(0xff848484);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                layoutParams.leftMargin = (int) DimensUtils.dp2px(12f);
                layoutParams.bottomMargin = (int) DimensUtils.dp2px(4f);
            }
            mLlChangeLog.addView(textView, layoutParams);
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_s30_firmware_update;
    }


    @OnClick(R2.id.tv_setting_bar_right)
    void onUpdateLocalClicked() {
        playNormalClickSound();
        mRouter.routeToFilesPage(4).startForResult(this, 1);
    }

    @OnClick(R2.id.btn_update)
    void onUpdateClicked() {
        playNormalClickSound();
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
