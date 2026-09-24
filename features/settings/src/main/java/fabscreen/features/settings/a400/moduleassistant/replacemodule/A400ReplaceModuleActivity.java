package fabscreen.features.settings.a400.moduleassistant.replacemodule;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.settings.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.A400_SETTINGS_REPLACE_MODULE)
public class A400ReplaceModuleActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        addFragment(R.id.fragment_container, ReplaceModuleIntroFragment.newInstance());
    }

    public void goToReplaceModuleInstruction(boolean checked) {
        replaceFragment(R.id.fragment_container, ReplaceModuleInstructionFragment.newInstance(checked));
    }

    public void goToReplaceModuleRestart() {
        replaceFragment(R.id.fragment_container, ReplaceModuleRestartFragment.newInstance());
    }

    public void goToConfirmation() {
        replaceFragment(R.id.fragment_container, ReplaceModuleConfirmationFragment.newInstance());
    }

    public void goToComplete() {
        replaceFragment(R.id.fragment_container, ReplaceModuleCompleteFragment.newInstance());
    }
}
