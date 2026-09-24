package fabscreen.features.addons.airpurifier;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.addons.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.ADDONS_AIR_PURIFIER)
public class AirPurifierActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_default);

        startAirPurifierHome();
    }

    public void startAirPurifierHome() {
        addFragment(R.id.fragment_container, AirPurifierHomeFragment.newInstance());
    }
}


