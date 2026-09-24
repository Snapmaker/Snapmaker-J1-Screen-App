package fabscreen.features.settings.a400.moduleassistant.replacemodule;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fabscreen.features.settings.R;
import fabscreen.platform.core.ui.base.BaseProgressFragment;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class ReplaceModuleRestartFragment extends BaseProgressFragment {

    private ReplaceModuleViewModel mViewModel;

    public static Fragment newInstance() {
        return new ReplaceModuleRestartFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getActivityScopeViewModel(ReplaceModuleViewModel.class);
        initView();
    }

    private void initView() {
        setMainTitle("Replace Module");
        setSubTitle("Initialize(2/3)");
        setProgress(2, 3);
        setIfShowClose(false);

        mViewModel.getMachineRestartObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> goToConfirmation(), LogHelper::log);
    }

    private void goToConfirmation() {
        if (requireActivity() instanceof A400ReplaceModuleActivity) {
            ((A400ReplaceModuleActivity) requireActivity()).goToConfirmation();
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_replace_module_restart;
    }
}
