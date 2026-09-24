package fabscreen.features.settings.a350.about;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.settings.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.SETTINGS_ABOUT)
public class AboutActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_default);

        Fragment fragment = SettingsAboutFragment.getInstance();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoChangeName() {
        Fragment fragment = SettingsNameFragment.getInstance();
        addFragment(R.id.fragment_container, fragment);
    }
}
