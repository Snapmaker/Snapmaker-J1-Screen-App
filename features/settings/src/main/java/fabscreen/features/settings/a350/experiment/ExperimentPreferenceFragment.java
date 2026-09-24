package fabscreen.features.settings.a350.experiment;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.Constants;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.lib.file.IPartition;
import fabscreen.platform.base.service.IAppService;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.service.IRouter;
import fabscreen.platform.base.service.Preferences;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;
import fabscreen.platform.core.ui.view.FabAlert;
import fabscreen.platform.base.view.FabConfirm;

public class ExperimentPreferenceFragment extends BaseFragment {
    @BindView(R2.id.btn_experiment_setup_flag)
    Button mBtnUpdateFlag;
    @BindView(R2.id.btn_experiment_reset)
    Button mBtnReset;
    @BindView(R2.id.btn_experiment_clear_update_files)
    Button mBtnClearUpdates;

    private IPartition mFileManager;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        boolean setupFlag = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineSetupFlag();
        mBtnUpdateFlag.setText(setupFlag ? "Setup State: YES" : "Setup State: No");

        mBtnReset.setText("Reset All");

        mBtnClearUpdates.setText("Clear Update Files");
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_experiment_preference;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_experiment_setup_flag)
    void onClickSetupFlag() {
        playNormalClickSound();
        boolean setupFlag = ServiceContainer.getInstance().getService(IPreferences.class).getHelper().getMachineSetupFlag();
        setupFlag = !setupFlag;

        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMachineSetupFlag(setupFlag);
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMachineSetup3DP(setupFlag);
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMachineSetupLaser(setupFlag);
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMachineSetupCNC(setupFlag);
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMachineSetup10WLaser(setupFlag);
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMachineSetupRotaryLaser(setupFlag);
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMachineSetupRotaryCNC(setupFlag);
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMachineSetupRotary10WLaser(setupFlag);
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setMachineSetupLanguage(setupFlag);
        mBtnUpdateFlag.setText(setupFlag ? "Setup State: YES" : "Setup State: No");
    }

    @OnClick(R2.id.btn_experiment_reset)
    void onClickReset() {
        playNormalClickSound();
        Preferences preferences = (Preferences) ServiceContainer.getInstance().getService(IPreferences.class);
        // Package versions should not be cleared for backup
        String packageVersion = preferences.getHelper().getLastUpdatePackageVersion();
        preferences.getHelper().reset();

        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setLastUpdatePackageVersion(packageVersion);
        // Machine not restarted. Need to obtain the machine model again
        switch (ServiceContainer.getInstance().getService(IMachine.class).getMachineInfoSubjectHolder().getValue().modelId) {
            case Constants.MACHINE_MODEL_SNAPMAKER_A150: {
                preferences.getHelper().setMachineModel(Constants.MACHINE_TYPE_A150);
                break;
            }
            case Constants.MACHINE_MODEL_SNAPMAKER_A250: {
                preferences.getHelper().setMachineModel(Constants.MACHINE_TYPE_A250);
                break;
            }
            case Constants.MACHINE_MODEL_SNAPMAKER_A350: {
                preferences.getHelper().setMachineModel(Constants.MACHINE_TYPE_A350);
                break;
            }
            case Constants.MACHINE_MODEL_UNKNOWN: {
                // todo user modified model
                break;
            }
            default:
                break;
        }

        ServiceContainer.getInstance().getService(IRouter.class).backHome().start(requireContext());
    }

    @OnClick(R2.id.btn_experiment_clear_update_files)
    void onClickClearUpdates() {
        playNormalClickSound();
        String updateFolderPath = ServiceContainer.getInstance().getService(IAppService.class).getDataDir().getAbsolutePath() + File.separatorChar + "update";
        File updateFolder = new File(updateFolderPath);
        if (updateFolder.exists()) {
            File[] files = updateFolder.listFiles();
            for (File file : files) {
                file.delete();
            }
            FabConfirm.create(getContext())
                    .setDescription(R.string.all_done)
                    .setConfirm(R.string.all_confirm, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        } else {
            FabAlert.alert(getContext(), "Folder not existed!");
        }
    }
}
