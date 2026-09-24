package fabscreen.platform.base.view.debugtool;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import fabscreen.platform.base.R;
import fabscreen.platform.base.R2;
import fabscreen.platform.base.service.IPreferences;
import fabscreen.platform.base.view.BaseFragment;

public class ScreenSaverSettingsFragment extends BaseFragment {
    public static Fragment newInstance() {
        return new ScreenSaverSettingsFragment();
    }

    @BindView(R2.id.sw_enable_screen_saver)
    SwitchCompat mSwEnableSaver;
    @BindView(R2.id.rg_screen_saver_time)
    RadioGroup mRgTime;

    private final int[] mTimeDelays = {5, 10, 900};

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_debug_screen_saver_settings;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        IPreferences.Helper helper = getServiceContainer().getService(IPreferences.class).getHelper();
        mSwEnableSaver.setChecked(helper.getScreenSaverEnabled());
        mSwEnableSaver.setOnCheckedChangeListener((buttonView, isChecked) -> helper.setScreenSaverEnabled(isChecked));

        int delayTime = helper.getScreenSaverDelayTime();
        int checkedIndex;
        switch (delayTime) {
            case 5:
                checkedIndex = 0;
                break;
            case 10:
                checkedIndex = 1;
                break;
            default:
                checkedIndex = 2;
                break;
        }

        mRgTime.check(mRgTime.getChildAt(checkedIndex).getId());
        mRgTime.setOnCheckedChangeListener((group, checkedId) -> {
            int index = group.indexOfChild(group.findViewById(checkedId));
            helper.setScreenSaverDelayTime(mTimeDelays[index]);
        });
    }
}
