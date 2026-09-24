package fabscreen.features.machinetools.setup.singledual.calibration;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_SETUP_XY_CALIBRATION)
public class SetupXYCalibrationActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
        addFragment(R.id.fragment_container, SetupXYCalibrationFragment.newInstance());
    }

    public void setResultAndFinish() {
        setResult(RESULT_OK);
        finish();
    }

    public void goToXYCalibration() {
        mRouter.routeToXYCalibration().startForResult(this, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(resultCode);
        finish();
    }
}
