package fabscreen.features.machinetools.calibration.j1Platform;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.J1CalibrationBaseFragment;
import fabscreen.features.machinetools.calibration.j1Platform.LevelingXY.LevelingXYAuxiliaryCalibrationActivity;
import fabscreen.features.machinetools.calibration.j1Platform.levelingBed.LevelingBedAuxiliaryCalibrationActivity;
import fabscreen.features.machinetools.calibration.j1Platform.levelingZ.LevelingZAuxiliaryCalibrationActivity;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.FDMController;

public class J1CalibrationCleanNozzleFragment extends J1CalibrationBaseFragment {
    @BindView(R2.id.iv_show_gif)
    ImageView mIvShowImage;
    @BindView(R2.id.iv_calibration_high_temperature_warning_normal)
    ImageView mIvCalibrationTemperatureWarningNormal;
    @BindView(R2.id.iv_calibration_glass_plate_normal)
    ImageView mIvCalibrationGlassPlateNormal;
    @BindView(R2.id.tv_calibration_title)
    TextView mTvCalibrationTitle;
    @BindView(R2.id.tv_calibration_progress)
    TextView mTvCalibrationProgress;
    @BindView(R2.id.tv_calibration_content_1)
    TextView mTvCalibrationContent1;
    FDMController fdmController;

    public static Fragment newInstance() {
        return new J1CalibrationCleanNozzleFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fdmController = ServiceContainer.getInstance().getService(IMachine.class).getFDMController();
        initView();
    }

    private void initView() {
        mIvCalibrationTemperatureWarningNormal.setVisibility(View.GONE);
        mIvCalibrationGlassPlateNormal.setVisibility(View.GONE);
        Glide.with(this)
                .asGif()
                .load(R.drawable.gif_calibration_j1_z_offset_clean_nozzies)
                .into(mIvShowImage);
        mTvCalibrationTitle.setText(R.string.j1_calibration_clean_nozzles_title);
        mTvCalibrationProgress.setText("3/6");
        mTvCalibrationContent1.setText(R.string.j1_calibration_clean_nozzles_msg);
        mIvCalibrationTemperatureWarningNormal.setVisibility(View.VISIBLE);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_calibration_currency;
    }

    @OnClick(R2.id.btn_next)
    void onClickNext() {
        playNormalClickSound();
        FragmentActivity fragmentActivity = requireActivity();
        if (fragmentActivity instanceof LevelingBedAuxiliaryCalibrationActivity) {
            ((LevelingBedAuxiliaryCalibrationActivity) fragmentActivity).gotoLevelingBedAuxiliaryCalibration();
        } else if (fragmentActivity instanceof LevelingXYAuxiliaryCalibrationActivity) {
            ((LevelingXYAuxiliaryCalibrationActivity) fragmentActivity).gotoLevelingXYAuxiliaryCalibration();
        } else if (fragmentActivity instanceof LevelingZAuxiliaryCalibrationActivity) {
            ((LevelingZAuxiliaryCalibrationActivity) fragmentActivity).gotoLevelingZAuxiliaryCalibration();
        }
    }


}
