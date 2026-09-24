package fabscreen.features.settings.j1;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import butterknife.ButterKnife;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.J1_SETTINGS_UPDATE_SUCCESS)
public class J1UpdateSuccessActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_j1_update_success);
        ButterKnife.bind(this);
    }

    @OnClick(R2.id.btn_complete)
    void onCompleteClicked() {
        ServiceContainer.getInstance().getService(IPreferences.class).getHelper().setShowUpdateResultWhenHome(false);
        mRouter.backHome().start(this);
    }
}
