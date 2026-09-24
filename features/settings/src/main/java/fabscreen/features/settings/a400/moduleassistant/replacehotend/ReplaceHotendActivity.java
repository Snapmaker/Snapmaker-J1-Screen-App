package fabscreen.features.settings.a400.moduleassistant.replacehotend;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.settings.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.A400_SETTINGS_REPLACE_HOTEND)
public class ReplaceHotendActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        replaceFragment(R.id.fragment_container, ReplaceHotendIntroFragment.newInstance());
    }

    public void goToReplaceHotendProcess() {
        replaceFragment(R.id.fragment_container, ReplaceHotendProcessFragment.newInstance());
    }

    public void goToComplete() {
        replaceFragment(R.id.fragment_container, ReplaceHotendCompleteFragment.newInstance());
    }
}
