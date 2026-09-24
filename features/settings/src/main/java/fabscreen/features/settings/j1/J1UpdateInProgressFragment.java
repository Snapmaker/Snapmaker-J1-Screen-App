package fabscreen.features.settings.j1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import butterknife.BindView;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.features.settings.common.S30UpdateInProgressFragment;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.receiver.InstallProcessReceiver;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.core.ui.view.GradientCircularProgressBar;

public class J1UpdateInProgressFragment extends S30UpdateInProgressFragment {
    public static Fragment newInstance(String filePath, boolean isLocal) {
        Fragment fragment = new J1UpdateInProgressFragment();
        Bundle bundle = new Bundle();
        bundle.putString("file_path", filePath);
        bundle.putBoolean("is_local", isLocal);
        fragment.setArguments(bundle);
        return fragment;
    }

    @BindView(R2.id.tv_percent)
    TextView mTvPercent;
    @BindView(R2.id.gcpb_progress)
    GradientCircularProgressBar mProgressBar;

    @Override
    protected void updateProgress(int progress) {
        // Progress will never reach 100 in this page.
        progress = progress == 100 ? 99 : progress;
        mProgressBar.setProgress(progress);
        mTvPercent.setText(progress + "%");
    }

    @Override
    protected void onReadyToInstallApk(String screenAPKPath) {
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setShowUpdateResultWhenHome(true);
        // Send broadcast to update FabScreen itself.
        Intent updateIntent = new Intent(requireContext(), InstallProcessReceiver.class);
        updateIntent.putExtra("URL", screenAPKPath);
        updateIntent.putExtra("OPERATION", "local_file");
        updateIntent.putExtra("PACKAGE_NAME", requireContext().getPackageName());
        requireContext().sendBroadcast(updateIntent);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_s30_update_progress;
    }
}
