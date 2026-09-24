package fabscreen.features.settings.a350.advanced.laser._10w;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.features.settings.a350.index.SettingsActivity;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.BaseViewModel;

public class SettingsLaser10wToolheadFocusCalibrationFragment extends BaseFragment {
    @BindView(R2.id.btn_start_toolhead_focus_calibration)
    Button mBtnStart;
    @BindView(R2.id.btn_start_toolhead_focus_calibration_content)
    TextView mTvContent;

    public static Fragment getInstance() {
        return new SettingsLaser10wToolheadFocusCalibrationFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(R.string.settings_laser_toolhead_focus_calibration_title);
        mBtnStart.setText(R.string.all_start);
        mTvContent.setText(R.string.settings_laser_toolhead_focus_calibration_page_desc);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_laser_10w_toolhead_focus_calibration;
    }

    @Override
    protected BaseViewModel getViewModel() {
        return null;
    }

    @OnClick(R2.id.btn_start_toolhead_focus_calibration)
    void onStartClicked() {
        playNormalClickSound();
        SettingsActivity activity = (SettingsActivity) requireActivity();
        activity.gotoLaser10wTouchPlatformFragment();
    }
}
