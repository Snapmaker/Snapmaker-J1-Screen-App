package fabscreen.features.settings.j1;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.settings.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.J1_SETTINGS_UPDATE_IN_PROGRESS)
public class J1UpdateInProgressActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        String filePath = getIntent().getStringExtra("filePath");
        boolean isLocal = getIntent().getBooleanExtra("isLocal", false);
        replaceFragment(R.id.fragment_container, J1UpdateInProgressFragment.newInstance(filePath, isLocal));
    }
}
