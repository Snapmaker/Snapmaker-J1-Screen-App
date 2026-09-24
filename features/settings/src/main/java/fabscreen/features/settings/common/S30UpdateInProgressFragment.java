package fabscreen.features.settings.common;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import butterknife.BindView;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public abstract class S30UpdateInProgressFragment extends BaseFragment {
    @BindView(R2.id.tv_update_desc)
    TextView mTvUpdateDesc;
    private S30UpdateInProgressViewModel mViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initData();
        initView();
    }

    private void initData() {
        Bundle arguments = getArguments();
        if (arguments == null) return;

        String filePath = arguments.getString("file_path");
        boolean isLocal = arguments.getBoolean("is_local");

        mViewModel.update(filePath, isLocal);
    }

    private void initView() {
        mViewModel.waitingForUpdateScreen()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::onMachineUpdated, e -> {
                    LogHelper.log(e);
                    back();
                });

//        mViewModel.getUpdateDescObservable()
//                .observeOn(AndroidSchedulers.mainThread())
//                .as(bindToLifecycle())
//                .subscribe(desc -> mTvUpdateDesc.setText(desc), LogHelper::log);

        mViewModel.getProgressObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(progress -> {
                    updateProgress(progress);
                }, e -> {
                    LogHelper.log(e);
                    back();
                });
    }

    private void onMachineUpdated(String screenAPKPath) {
        // check apk PackageName
        PackageManager packageManager = requireContext().getPackageManager();
        PackageInfo archiveInfo = packageManager.getPackageArchiveInfo(screenAPKPath, PackageManager.GET_ACTIVITIES);
        String packageName = archiveInfo.applicationInfo.packageName;
        if (!requireContext().getPackageName().equals(packageName)) {
            Logger.e("Given apk with wrong package name: %s, abort updating!", packageName);
            requireActivity().finish();
            return;
        }
        onReadyToInstallApk(screenAPKPath);
    }

    protected abstract void onReadyToInstallApk(String screenAPKPath);

    protected abstract void updateProgress(int progress);

    @Override
    protected S30UpdateInProgressViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(S30UpdateInProgressViewModel.class);
    }
}
