package fabscreen.features.guide.s20.laser._10w.cameralibration;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.guide.R;
import fabscreen.features.guide.R2;
import fabscreen.features.guide.s20.laser._10w.Guide10wLaserActivity;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.common.Settings10wCameraCalibrationViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class Guide10wLaserCameraCalibrationStep2Fragment extends BaseFragment {
    @BindView(R2.id.top_bar_back)
    Button mBtnBack;
    @BindView(R2.id.tv_settings_camera_calibration_content)
    TextView mTvContent;
    @BindView(R2.id.btn_settings_camera_calibration_complete)
    Button mBtnNext;
    @BindView(R2.id.iv_settings_camera_calibration)
    ImageView mIvCover;
    private Settings10wCameraCalibrationViewModel mViewModel;

    public static Guide10wLaserCameraCalibrationStep2Fragment newInstance() {
        return new Guide10wLaserCameraCalibrationStep2Fragment();
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
    protected Settings10wCameraCalibrationViewModel getViewModel() {
        return getViewModelProvider().get(Settings10wCameraCalibrationViewModel.class);
    }

    private void initView() {
        mIvCover.setImageResource(R.drawable.pic_10w_laser_camera_calibration_240x160);
        // Start camera calibration procedure
        mViewModel.getCalibrationStatus()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(status -> {
                    switch (status) {
                        case Settings10wCameraCalibrationViewModel.STATUS_IDLE:
                            mBtnBack.setVisibility(ImageButton.VISIBLE);
                            mBtnNext.setEnabled(false);
                            mTvContent.setText(R.string.laser_camera_calibration_step2_desc);
                            break;
                        case Settings10wCameraCalibrationViewModel.STATUS_PROCESSING:
                            mBtnBack.setVisibility(ImageButton.GONE);
                            mBtnNext.setEnabled(false);
                            mBtnNext.setText(R.string.laser_calibration_processing);
                            break;
                        case Settings10wCameraCalibrationViewModel.STATUS_COMPLETE:
                            mBtnBack.setVisibility(ImageButton.VISIBLE);
                            mBtnNext.setEnabled(true);
                            mBtnNext.setText(R.string.all_complete);
                            break;
                        case Settings10wCameraCalibrationViewModel.STATUS_ERROR:
                            mBtnBack.setVisibility(ImageButton.VISIBLE);
                            mBtnNext.setEnabled(true);
                            mBtnNext.setText(R.string.all_failed);
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
                        Logger.d("Processing photo %d/4", index);
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
        ((Guide10wLaserActivity) getActivity()).startCompleteFragment();
    }
}
