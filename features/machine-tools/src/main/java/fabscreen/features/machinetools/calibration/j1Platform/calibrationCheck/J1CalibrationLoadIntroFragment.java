package fabscreen.features.machinetools.calibration.j1Platform.calibrationCheck;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.J1CalibrationBaseFragment;
import fabscreen.features.machinetools.calibration.j1Platform.vibrationcalibration.VibrationCalibrationActivity;

public class J1CalibrationLoadIntroFragment extends J1CalibrationBaseFragment {
    @BindView(R2.id.iv_show_gif)
    ImageView mIvShowImage;
    @BindView(R2.id.iv_calibration_high_temperature_warning_normal)
    ImageView mIvCalibrationTemperatureWarningNormal;
    @BindView(R2.id.tv_calibration_title)
    TextView mTvCalibrationTitle;
    @BindView(R2.id.tv_calibration_progress)
    TextView mTvCalibrationProgress;
    @BindView(R2.id.tv_calibration_content_1)
    TextView mTvCalibrationContent1;
    @BindView(R2.id.btn_second)
    Button mBtnSecond;
    @BindView(R2.id.btn_next)
    Button mBtnPrint;

    public static Fragment newInstance() {
        return new J1CalibrationLoadIntroFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {

        if (requireActivity() instanceof VibrationCalibrationActivity) {
            Glide.with(this)
                    .load(R.drawable.pic_calibration_j1_z_offset_load_filament_for_vibration)
                    .into(mIvShowImage);
        } else {
            Glide.with(this)
                    .load(R.drawable.pic_calibration_j1_z_offset_load_filament)
                    .into(mIvShowImage);
        }
        mTvCalibrationTitle.setText(R.string.j1_calibration_calibration_check_load_filament_title);
        mTvCalibrationProgress.setText("3/5");
        mTvCalibrationContent1.setText(R.string.j1_calibration_calibration_check_load_filament_contnent);
        mIvCalibrationTemperatureWarningNormal.setVisibility(View.VISIBLE);
        mBtnPrint.setText(R.string.all_print);
        mBtnSecond.setText(R.string.j1_calibration_calibration_check_go_to_load);
        mBtnSecond.setVisibility(View.VISIBLE);

    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_calibration_currency;
    }

    @OnClick(R2.id.btn_next)
    void onClickNext() {
        playNormalClickSound();
        if (requireActivity() instanceof CalibrationCheckCalibrationActivity) {
            ((CalibrationCheckCalibrationActivity) requireActivity()).gotoPrintFragment();
        } else if (requireActivity() instanceof VibrationCalibrationActivity) {
            ((VibrationCalibrationActivity) requireActivity()).goToCalibrationPrint();
        }
    }

    @OnClick(R2.id.btn_second)
    void onClickSecond() {
        if (requireActivity() instanceof CalibrationCheckCalibrationActivity) {
            ((CalibrationCheckCalibrationActivity) requireActivity()).goToLoadFilament();
        } else if (requireActivity() instanceof VibrationCalibrationActivity) {
            ((VibrationCalibrationActivity) requireActivity()).goToLoadFilament();
        }
    }
}
