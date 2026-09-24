package fabscreen.features.settings.a350.firmware.update;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.IFile;
import fabscreen.platform.base.lib.file.IPartition;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IFileManagerService;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.base.view.CircularProgressView;
import fabscreen.platform.base.view.FabConfirm;
import fabscreen.platform.core.ui.view.FabAlert;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class UpdateFragment extends BaseFragment {
    private static final String TAG = UpdateFragment.class.getSimpleName();

    @BindView(R2.id.cpv_update_progress)
    CircularProgressView mCpvProgress;
    @BindView(R2.id.tv_update_progress)
    TextView mTvProgress;

    @BindView(R2.id.tv_update_title)
    TextView mTvTitle;
    @BindView(R2.id.tv_update_desc)
    TextView mTvDesc;

    private IPartition mFileManager;
    private UpdateViewModel mViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.all_update);

        initView();
        initData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mViewModel.dispose();
//        if (mFileManager != null) {
//            if (mFileManager instanceof FabUsbFileDevice) {
//                mFileManager = null;
//            } else {
//                mFileManager.close();
//            }
//        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_update;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    private void initView() {
        mViewModel = new UpdateViewModel(getContext());

        mViewModel.getUpdateMessageObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(message -> mTvTitle.setText(message));

        mViewModel.getUpdateProgressTextObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(message -> mTvDesc.setText(message));

        mViewModel.getUpdateProgressObservable()
                .throttleLast(50, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(progress -> {
                    mCpvProgress.setPercentage(100f * progress, true);
                    mTvProgress.setText(String.format(Locale.US, "%.0f", 100f * progress));
                });
    }

    private void initData() {
        if (getArguments() == null) return;

        String filePath = getArguments().getString("file_path");
        boolean isLocal = getArguments().getBoolean("is_local");
        mFileManager = ServiceContainer.getInstance().getService(IFileManagerService.class).getDevice(isLocal);

//        Logger.i(String.format("Opening file %s for upgrade.", filePath));
//        mViewModel.startSearchingFile(mFileManager, filePath)
//                .observeOn(AndroidSchedulers.mainThread())
//                .as(bindToLifecycle())
//                .subscribe(file -> {
//                    String updateCacheFolder = ServiceContainer.getInstance().getService(IAppService.class).getCacheDir().getAbsolutePath() + File.separatorChar + "update";
//                    mViewModel.saveUpdateFile(updateCacheFolder, file);
//                    update(file);
//                }, e -> {
//                    LogHelper.log(e);
//                    FabConfirm.create(getContext())
//                            .setCanceledOnTouchOutSide(false)
//                            .setDescription(R.string.settings_update_failed_file_not_found)
//                            .setConfirm(R.string.all_ok, (dialog, which) -> {
//                                dialog.dismiss();
//                                back();
//                            })
//                            .show();
//                });
    }

    private void update(IFile file) {
        if (!mViewModel.parse(file)) {
            Logger.w("Failed to parse update file.");
            FabConfirm.create(getContext())
                    .setDescription(R.string.settings_update_failed_file_parse_error)
                    .setConfirm(R.string.all_ok, (dialog, which) -> {
                        dialog.dismiss();
                        back();
                    })
                    .show();
            return;
        }

        mViewModel.update()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    if (success) {
                        Logger.i("Upgrade successfully");

                        // save update file.
                        String updateDataFolder = ServiceContainer.getInstance().getService(IAppService.class).getDataDir().getAbsolutePath() + File.separatorChar + "update";
                        mViewModel.saveUpdateFile(updateDataFolder, mViewModel.getUpdateFile());

                        FabConfirm.create(getContext())
                                .setDescription(R.string.settings_update_success)
                                .setConfirm(R.string.all_ok, (dialog, which) -> {
                                    dialog.dismiss();
                                    ServiceContainer.getInstance().getService(IRouter.class).backHome().start(requireContext());
                                })
                                .show();
                    } else {
                        FabAlert.alert(getContext(), R.string.settings_update_failed);
                        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMachineUpdatedFlag(false);
                    }
                });
    }
}
