package fabscreen.features.addons.enclosure;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.addons.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.ADDONS_ENCLOSURE)
public class EnclosureActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_default);

        startEnclosureHome();
    }

    public void startEnclosureHome() {
        addFragment(R.id.fragment_container, EnclosureHomeFragment.getInstance());
    }

    public void startEnclosureSettings() {
        addFragment(R.id.fragment_container, EnclosureSettingsFragment.getInstance());
    }
}


