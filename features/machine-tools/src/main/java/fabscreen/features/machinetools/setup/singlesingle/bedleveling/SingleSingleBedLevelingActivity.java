package fabscreen.features.machinetools.setup.singlesingle.bedleveling;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;

import fabscreen.features.machinetools.R;
import fabscreen.platform.base.RoutePath;
import fabscreen.platform.base.view.BaseActivity;

@Route(path = RoutePath.TOOLS_SETUP_SINGLE_SINGLE_BED_LEVELING)
public class SingleSingleBedLevelingActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        addFragment(R.id.fcv_setup_content, SingleSingleBedLevelingIntroFragment.newInstance());
    }

    public void goToBedLeveling() {
        replaceFragment(R.id.fcv_setup_content,SingleSingleBedLevelingFragment.newInstance());
    }

    public void setResultAndFinish() {
        setResult(RESULT_OK);
        finish();
    }
}
