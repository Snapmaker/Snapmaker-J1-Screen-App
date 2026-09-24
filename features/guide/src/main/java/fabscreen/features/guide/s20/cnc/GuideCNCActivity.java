package fabscreen.features.guide.s20.cnc;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.guide.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.GUIDE_CNC)
public class GuideCNCActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            setContentView(R.layout.activity_default);

            startGetStartedFragment();
        }
    }

    /**
     * Guide CNC page 1, Get Started / Safety Instructions
     */
    public void startGetStartedFragment() {
        addFragment(R.id.fragment_container, GuideCNCGetStartedFragment.newInstance());
    }

    /**
     * Guide CNC page 2
     */
    public void startSafetyGogglesFragment() {
        addFragment(R.id.fragment_container, GuideCNCSafetyGogglesFragment.newInstance());
    }

    /**
     * Guide CNC page 3
     */
    public void startPrepareMaterialFragment() {
        addFragment(R.id.fragment_container, GuideCNCPrepareMaterialFragment.newInstance());
    }

    /**
     * Guide CNC page 4
     */
    public void startPrepareToolHeadFragment() {
        addFragment(R.id.fragment_container, GuideCNCPrepareToolHeadFragment.newInstance());
    }

    /**
     * Guide CNC page 5
     */
    public void startCompleteFragment() {
        addFragment(R.id.fragment_container, GuideCNCCompleteFragment.newInstance());
    }
}
