package fabscreen.features.settings.a400.update;

import android.os.Bundle;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import butterknife.BindView;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.features.settings.common.S30UpdateInProgressFragment;

public class A400UpdateInProgressFragment extends S30UpdateInProgressFragment {
    public static Fragment newInstance(String filePath, boolean isLocal) {
        Fragment fragment = new A400UpdateInProgressFragment();
        Bundle bundle = new Bundle();
        bundle.putString("file_path", filePath);
        bundle.putBoolean("is_local", isLocal);
        fragment.setArguments(bundle);
        return fragment;
    }

    @BindView(R2.id.progress)
    CircularProgressIndicator mProgress;
    @BindView(R2.id.tv_percent)
    TextView mTvPercent;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_update_in_progress;
    }

    @Override
    protected void updateProgress(int progress) {
        mProgress.setProgressCompat(progress, true);
        mTvPercent.setText(progress + "%");
    }

    @Override
    protected void onReadyToInstallApk(String screenAPKPath) {
        mRouter.routeToUpdateSuccess(0, screenAPKPath).start(requireContext());
    }
}
