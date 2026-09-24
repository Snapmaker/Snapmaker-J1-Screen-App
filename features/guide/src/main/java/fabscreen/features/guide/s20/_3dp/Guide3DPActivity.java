package fabscreen.features.guide.s20._3dp;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.guide.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;
import fabscreen.platform.core.ui.common.CalibrationViewModel;

@Route(path = RoutePath.GUIDE_3DP)
public class Guide3DPActivity extends BaseActivity {

    private CalibrationViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            setContentView(R.layout.activity_default);

            startGetStartedFragment();
        }

        mViewModel = getViewModel(CalibrationViewModel.class);
    }

    public void startGetStartedFragment() {
        addFragment(R.id.fragment_container, Guide3DPGetStartedFragment.newInstance());
    }

    public void startCalibrationIntroFragment() {
        addFragment(Guide3DPCalibrationIntroFragment.class.getSimpleName(),
                R.id.fragment_container,
                Guide3DPCalibrationIntroFragment.newInstance(),true);
    }

    public void startCalibrationFragment() {
        addFragment(R.id.fragment_container, Guide3DPCalibrationFragment.newInstance());
    }

    public void startPrepareFilamentIntroFragment() {
        addFragment(R.id.fragment_container, Guide3DPPrepareFilamentIntroFragment.newInstance());
    }

    public void startPrepareFilamentFragment() {
        addFragment(R.id.fragment_container, Guide3DPPrepareFilamentFragment.newInstance());
    }

    public void startCompleteFragment() {
        addFragment(R.id.fragment_container, Guide3DPCompleteFragment.newInstance());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewModel.dispose();
    }
}
