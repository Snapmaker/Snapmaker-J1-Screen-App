package fabscreen.features.machinetools.calibration.j1Platform.levelingZ;

import android.os.Bundle;
import android.view.View;
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
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class LevelingZCalibrationLoosenScrewsFragment extends J1CalibrationBaseFragment {
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

    public static Fragment newInstance() {
        return new LevelingZCalibrationLoosenScrewsFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        checkHome();
    }

    public void checkHome() {
        IMachine service = ServiceContainer.getInstance().getService(IMachine.class);
        if (!service.getMachineStatusSubjectHolder().getValue().isHomed) {
            goHome();
        }
    }

    private void goHome() {
        fabHoming.show();
        IMachine service = ServiceContainer.getInstance().getService(IMachine.class);
        service.getMachineController().home(0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(homeState -> {
                    fabHoming.dismiss();
                }, LogHelper::log);
    }

    private void initView() {
        mIvCalibrationTemperatureWarningNormal.setVisibility(View.GONE);
        mIvCalibrationGlassPlateNormal.setVisibility(View.GONE);
        Glide.with(this)
                .asGif()
                .load(R.drawable.gif_calibration_j1_z_offset_lossen_screws)
                .into(mIvShowImage);
        mTvCalibrationTitle.setText(R.string.j1_calibration_Z_offset_calibration_loosen_screws_title);
        mTvCalibrationProgress.setText("2/6");
        mTvCalibrationContent1.setText(R.string.j1_calibration_Z_offset_calibration_loosen_screws_content);
        mIvCalibrationGlassPlateNormal.setVisibility(View.VISIBLE);
    }


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_calibration_currency;
    }

    @OnClick(R2.id.btn_next)
    void onClickNext() {
        playNormalClickSound();
        ((LevelingZAuxiliaryCalibrationActivity) requireActivity()).gotoNozzleBedHeating();

    }
}
