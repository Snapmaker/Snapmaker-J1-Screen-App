package fabscreen.features.guide.s20.rotary.cnc;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.guide.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.GUIDE_ROTARY_CNC)
public class GuideRotaryCNCActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            setContentView(R.layout.activity_default);

            startGetStartedFragment();
        }
    }

    public void startGetStartedFragment() {
        addFragment(R.id.fragment_container, GuideRotaryCNCGetStartedFragment.newInstance());
    }

    public void startOriginAssistantIntroFragment() {
        addFragment(R.id.fragment_container, GuideRotaryCNCOriginAssistantIntroFragment.newInstance());
    }

    public void startBitAssistantIntroFragment() {
        addFragment(R.id.fragment_container, GuideRotaryCNCBitAssistantIntroFragment.newInstance());
    }

    public void startCompleteFragment() {
        addFragment(R.id.fragment_container, GuideRotaryCNCCompleteFragment.newInstance());
    }
}
