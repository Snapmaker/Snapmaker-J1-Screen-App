package fabscreen.features.machinetools.calibration.j1Platform;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import fabscreen.features.machinetools.calibration.j1Platform.viewmodel.RemoveGlassViewModel;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.FDMController;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class J1CalibrationRemoveGlassFragment extends J1CalibrationBaseFragment {
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
    @BindView(R2.id.rl_calibration_remove_glass)
    RelativeLayout mRlCalibrationRemoveGlass;
    @BindView(R2.id.tv_calibration_remove_glass)
    TextView mTvCalibrationRemoveGlass;
    @BindView(R2.id.btn_next)
    Button mBtnNext;

    RemoveGlassViewModel mViewModel;
    FDMController fdmController;

    public static Fragment newInstance() {
        return new J1CalibrationRemoveGlassFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fdmController = ServiceContainer.getInstance().getService(IMachine.class).getFDMController();
        mViewModel = getFragmentScopeViewModel(RemoveGlassViewModel.class);
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
                .load(R.drawable.gif_leveling_bed_auxiliary_calibration_info)
                .into(mIvShowImage);
        mTvCalibrationTitle.setText(R.string.j1_calibration_remove_glass_plate_title);
        mTvCalibrationProgress.setText("1/6");
        mTvCalibrationContent1.setText(R.string.j1_calibration_remove_glass_plate_content);
        mIvCalibrationGlassPlateNormal.setVisibility(View.VISIBLE);
        mBtnNext.setText(R.string.all_next);
        mViewModel.getBedTempObservable()
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(integer -> {
                    if (integer >= 40) {
                        mIvCalibrationTemperatureWarningNormal.setVisibility(View.VISIBLE);
                        mTvCalibrationRemoveGlass.setText(getString(R.string.j1_calibration_remove_glass_plate_high_temperature_content, integer));
                        mRlCalibrationRemoveGlass.setVisibility(View.VISIBLE);
                    } else {
                        mIvCalibrationTemperatureWarningNormal.setVisibility(View.GONE);
                        mRlCalibrationRemoveGlass.setVisibility(View.GONE);
                    }
                }, LogHelper::log);
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
            ((LevelingBedAuxiliaryCalibrationActivity) fragmentActivity).gotoNozzleBedHeating();
        } else if (fragmentActivity instanceof LevelingXYAuxiliaryCalibrationActivity) {
            ((LevelingXYAuxiliaryCalibrationActivity) fragmentActivity).gotoNozzleBedHeating();
        } else if (fragmentActivity instanceof LevelingZAuxiliaryCalibrationActivity) {
            ((LevelingZAuxiliaryCalibrationActivity) fragmentActivity).gotoLoosenScrews();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.subscribeHeatedBedChange();
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.unsubscribeHeatedBedChange();
    }
}
