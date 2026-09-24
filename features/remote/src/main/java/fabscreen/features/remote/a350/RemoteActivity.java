package fabscreen.features.remote.a350;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.remote.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.REMOTE_INDEX)
public class RemoteActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_default);

        Fragment fragment = RemoteHomeFragment.getInstance();
        replaceFragment(R.id.fragment_container, fragment);
    }
}
