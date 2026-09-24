package fabscreen.features.machinetools.setup.singledual.loadfilament;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_LOAD_FILAMENT)
public class LoadFilamentActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        addFragment(R.id.fragment_container, LoadFilamentFragment.newInstance());
    }

    public void setResultAndFinish() {
        setResult(RESULT_OK);
        finish();
    }
}
