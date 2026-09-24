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
import fabscreen.features.machinetools.calibration.j1Platform.viewmodel.NozzleBedHeatingViewModel;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.Vector;
import fabscreen.platform.base.service.machine.controller.FDMController;
import fabscreen.platform.base.service.machine.entity.module.HeatedBed;
import fabscreen.platform.base.service.machine.entity.parts.Extruder;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class J1CalibrationNozzleBedHeatingFragment extends J1CalibrationBaseFragment {
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
    @BindView(R2.id.btn_next)
    Button mBtnNext;

    @BindView(R2.id.rl_calibration_heating_icon)
    RelativeLayout mRlHeating;
    @BindView(R2.id.tv_nozzle_state_left)
    TextView mShowStateLeft;
    @BindView(R2.id.iv_nozzle_state_left)
    ImageView mIvShowStateLeft;
    @BindView(R2.id.tv_nozzle_state_right)
    TextView mShowStateRight;
    @BindView(R2.id.iv_nozzle_state_right)
    ImageView mIvShowStateRight;
    @BindView(R2.id.tv_bed_state)
    TextView mShowStateBed;
    @BindView(R2.id.iv_bed_state)
    ImageView mIvShowStateBed;


    NozzleBedHeatingViewModel mViewModel;
    FDMController fdmController;

    public static Fragment newInstance() {
        return new J1CalibrationNozzleBedHeatingFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fdmController = ServiceContainer.getInstance().getService(IMachine.class).getFDMController();
        mViewModel = getFragmentScopeViewModel(NozzleBedHeatingViewModel.class);
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
        mBtnNext.setEnabled(false);
        mRlHeating.setVisibility(View.VISIBLE);
        Glide.with(this)
                .asGif()
                .load(R.drawable.gif_calibration_j1_z_offset_heat_noaales_and_bed)
                .into(mIvShowImage);
        mTvCalibrationTitle.setText(R.string.j1_calibration_nozzle_bed_heating_title);
        mTvCalibrationProgress.setText("2/6");
        mTvCalibrationContent1.setText(R.string.j1_calibration_nozzle_bed_heating_content);

        mViewModel.getLFdmToolheadStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(fdmToolHeadInfo -> {
                            Extruder extruder = fdmToolHeadInfo.getExtruderList().get(0);
                            int leftTemperature = (int) extruder.getTemperature();
                            int targetTemperature = (int) extruder.getTargetTemperature();
                            mShowStateLeft.setText(getString(R.string.all_temperature_heating, leftTemperature, targetTemperature));
                            boolean b = targetTemperature != 0 && leftTemperature >= targetTemperature - 3;
                            if (b) {
                                mIvShowStateLeft.setImageResource(R.drawable.icon_nozzle_left_orange_64x64);
                            } else {
                                mIvShowStateLeft.setImageResource(R.drawable.icon_nozzle_left_yellow_64x64);
                            }
                        }
                );

        mViewModel.getRFdmToolheadStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(fdmToolHeadInfo -> {
                            Extruder extruder = fdmToolHeadInfo.getExtruderList().get(0);
                            int leftTemperature = (int) extruder.getTemperature();
                            int targetTemperature = (int) extruder.getTargetTemperature();
                            mShowStateRight.setText(getString(R.string.all_temperature_heating, leftTemperature, targetTemperature));
                            boolean b = targetTemperature != 0 && leftTemperature >= targetTemperature - 3;
                            if (b) {
                                mIvShowStateRight.setImageResource(R.drawable.icon_nozzle_right_orange_64x64);
                            } else {
                                mIvShowStateRight.setImageResource(R.drawable.icon_nozzle_right_yellow_64x64);
                            }
                        }
                );
        mViewModel.getBedStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(bedStatus -> {
                            HeatedBed.ZoneInfo zoneInfo = bedStatus.getZoneList().get(0);
                            int targetTemperature = zoneInfo.getTargetTemperature();
                            int temperature = (int) zoneInfo.getCurrentTemperature();
                            mShowStateBed.setText(getString(R.string.all_temperature_heating, temperature, targetTemperature));
                            boolean b = targetTemperature != 0 && temperature >= targetTemperature - 10;
                            mIvCalibrationTemperatureWarningNormal.setVisibility(temperature >= 40 ? View.VISIBLE : View.GONE);
                            if (b) {
                                mIvShowStateBed.setImageResource(R.drawable.icon_heated_bed_orange_64x64);
                            } else {
                                mIvShowStateBed.setImageResource(R.drawable.icon_heated_bed_yellow_64x64);
                            }
                        }
                );

        mViewModel.getNextObservable()
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(abool -> {
                            if (abool) {
                                mTvCalibrationContent1.setText(R.string.j1_calibration_nozzle_bed_heated_content);
                                mBtnNext.setEnabled(abool);
                            }
                        }
                );
    }


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_calibration_currency;
    }

    @OnClick(R2.id.btn_next)
    void onClickNext() {
        playNormalClickSound();
        fabMoving.show();
        Vector vector = new Vector();
        vector.setX(50);
        fdmController.CalibrationDrawBackZ()
                .flatMap(responseStructure -> ServiceContainer.getInstance().getService(IMachine.class).getMachineController().MoveRelativeHome(vector, 0))
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    fabMoving.dismiss();
                    if (responseStructure.isSuccess()) {
                        FragmentActivity fragmentActivity = requireActivity();
                        if (fragmentActivity instanceof LevelingBedAuxiliaryCalibrationActivity) {
                            ((LevelingBedAuxiliaryCalibrationActivity) fragmentActivity).gotoCleanNozzle();
                        } else if (fragmentActivity instanceof LevelingXYAuxiliaryCalibrationActivity) {
                            ((LevelingXYAuxiliaryCalibrationActivity) fragmentActivity).gotoCleanNozzle();
                        } else if (fragmentActivity instanceof LevelingZAuxiliaryCalibrationActivity) {
                            ((LevelingZAuxiliaryCalibrationActivity) fragmentActivity).gotoCleanNozzle();
                        }
                    } else {
                        errorBack("CalibrationDrawBackZ", responseStructure.resultProp.getValue());
                    }
                }, LogHelper::log);

    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.subscribeTemperatureChange();
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.unsubscribeTemperatureChange();
    }
}
