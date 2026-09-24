package fabscreen.features.settings.a350.advanced.laser;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.settings.R;
import fabscreen.features.settings.R2;
import fabscreen.features.settings.a350.index.SettingsActivity;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.common.SettingsCameraCalibrationViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class SettingsCameraCalibrationStep2Fragment extends BaseFragment {
    @BindView(R2.id.top_bar_back)
    Button mBtnBack;
    @BindView(R2.id.tv_settings_camera_calibration_content)
    TextView mTvContent;
    @BindView(R2.id.btn_settings_camera_calibration_complete)
    Button mBtnComplete;
    private SettingsCameraCalibrationViewModel mViewModel;

    public static SettingsCameraCalibrationStep2Fragment newInstance() {
        return new SettingsCameraCalibrationStep2Fragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = getViewModel();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_settings_camera_calibration_step2;
    }

    @Override
    protected SettingsCameraCalibrationViewModel getViewModel() {
        return getViewModelProvider().get(SettingsCameraCalibrationViewModel.class);
    }

    private void initView() {
        // Start camera calibration procedure
        mViewModel.getCalibrationStatus()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(status -> {
                    switch (status) {
                        case SettingsCameraCalibrationViewModel.STATUS_IDLE:
                            mBtnBack.setVisibility(ImageButton.VISIBLE);
                            mBtnComplete.setEnabled(false);
                            mTvContent.setText(R.string.laser_camera_calibration_step2_desc);
                            break;
                        case SettingsCameraCalibrationViewModel.STATUS_PROCESSING:
                            mBtnBack.setVisibility(ImageButton.GONE);
                            mBtnComplete.setEnabled(false);
                            mBtnComplete.setText(R.string.laser_calibration_processing);
                            break;
                        case SettingsCameraCalibrationViewModel.STATUS_COMPLETE:
                            mBtnBack.setVisibility(ImageButton.VISIBLE);
                            mBtnComplete.setEnabled(true);
                            mBtnComplete.setText(R.string.all_complete);
                            break;
                        case SettingsCameraCalibrationViewModel.STATUS_ERROR:
                            mBtnBack.setVisibility(ImageButton.VISIBLE);
                            mBtnComplete.setEnabled(true);
                            mBtnComplete.setText(R.string.all_failed);
                            break;
                        default:
                            break;
                    }
                });

        mViewModel.getProcessProgressObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(index -> {
                    if (index != 0) {
                        mTvContent.setText(getString(R.string.laser_camera_calibration_step2_desc2, index));
                    } else {
                        mTvContent.setText(R.string.laser_camera_calibration_step2_desc);
                    }
                });

        mViewModel.start();
    }

    @OnClick(R2.id.btn_settings_camera_calibration_complete)
    void onClickNext() {
        playNormalClickSound();
        if (getActivity() == null) return;
        ((SettingsActivity) getActivity()).popSettingsPage();
    }
}
