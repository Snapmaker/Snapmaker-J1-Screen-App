package fabscreen.features.settings.a350.firmware.update;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.settings.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.SETTINGS_UPDATE)
public class UpdateActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_default);

        Intent intent = getIntent();
        Fragment fragment = new UpdateFragment();

        Bundle bundle = new Bundle();
        bundle.putBoolean("is_local", intent.getBooleanExtra("is_local", false));
        bundle.putString("file_path", intent.getStringExtra("file_path"));
        fragment.setArguments(bundle);

        addFragment(R.id.fragment_container, fragment);
    }
}
