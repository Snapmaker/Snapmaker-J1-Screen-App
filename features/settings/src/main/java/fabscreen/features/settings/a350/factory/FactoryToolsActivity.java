package fabscreen.features.settings.a350.factory;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import butterknife.ButterKnife;
import fabscreen.features.settings.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.SETTINGS_FACTORY)
public class FactoryToolsActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_default);
        ButterKnife.bind(this);

        FactoryToolsFragment fragment = new FactoryToolsFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoColorFraudFragment() {
        FactoryColorFraudFragment fragment = new FactoryColorFraudFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoCpuBenchmarkFragment() {
        FactoryCpuBenchmarkFragment fragment = new FactoryCpuBenchmarkFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoWifiSignalFragment() {
        FactoryWifiSignalFragment fragment = new FactoryWifiSignalFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoBrightnessTestFragment() {
        FactoryBrightnessTestFragment fragment = new FactoryBrightnessTestFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoTouchPanelTestFragment() {
        FactoryTouchPanelTestFragment fragment = new FactoryTouchPanelTestFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoBluetoothFragment() {
        FactoryBluetoothFragment fragment = new FactoryBluetoothFragment();
        addFragment(R.id.fragment_container, fragment);
    }

    public void gotoColorDifferenceFragment() {
        FactoryColorDifferenceFragment fragment = new FactoryColorDifferenceFragment();
        addFragment(R.id.fragment_container, fragment);
    }
}
