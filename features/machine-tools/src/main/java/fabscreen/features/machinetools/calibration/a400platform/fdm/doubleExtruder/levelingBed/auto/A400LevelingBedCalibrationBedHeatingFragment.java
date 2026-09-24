package fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingBed.auto;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.calibration.A400CalibrationBaseFragment;
import fabscreen.features.machinetools.calibration.a400platform.fdm.doubleExtruder.levelingBed.A400LevelingBedViewModel;
import fabscreen.platform.base.service.machine.entity.module.HeatedBed;
import fabscreen.platform.base.service.machine.entity.parts.Extruder;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class A400LevelingBedCalibrationBedHeatingFragment extends A400CalibrationBaseFragment {

    @BindView(R2.id.iv_a400_leveling_bed_calibration_data)
    TextView mTvHeatingBedTime;
    @BindView(R2.id.tv_nozzle_temp_current_temperature)
    TextView mTvNozzleTempCurrentTemp;
    @BindView(R2.id.tv_nozzle_temp_target_temperature)
    TextView mTvNozzleTempTargetTemp;
    @BindView(R2.id.tv_heated_temp_current_temperature)
    TextView mTvHeatedTempCurrentTemp;
    @BindView(R2.id.tv_heated_temp_target_temperature)
    TextView mTvHeatedTempTargetTemp;
    @BindView(R2.id.top_bar_ico)
    ImageView mIvProProblemIcon;

    Disposable subscribe;
    private A400LevelingBedViewModel mViewModel;
    private int mCalculateHeatingTime;

    public static Fragment newInstance() {
        return new A400LevelingBedCalibrationBedHeatingFragment();
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
        if (mViewModel.getBedCalibrationBedTemperature() != 0) {
            mViewModel.startHeating();
        } else {
            ((A400LevelingBedCalibrationAutoActivity) requireActivity()).gotoLevelingBedCalibrationAuto();
        }
    }

    private void initView() {
        mIvProProblemIcon.setVisibility(View.GONE);
        setTitle(R.string.calibration_heated_bed_leveling_title);
        setContent(R.string.calibration_heated_bed_leveling_content_1);
        mGuideProgressBar.setMax(2);
        mGuideProgressBar.setProgress(1);
        mGuideProgressBar.invalidate();
        mGuideProgressBar.setVisibility(View.VISIBLE);
        mCalculateHeatingTime = mViewModel.CalculateHeatingTime();
        mViewModel.getExtruderChangeObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(fdmToolheadStatus -> {
                    Extruder extruder = fdmToolheadStatus.getExtruderList().get(0);
                    mTvNozzleTempCurrentTemp.setText(String.valueOf((int) extruder.getTemperature()));
                    mTvNozzleTempTargetTemp.setText(getString(R.string.calibration_leveling_bed_heating_temperature, (int) extruder.getTargetTemperature()));
                }, LogHelper::log);
        mViewModel.getBedChangeObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(bedStatus -> {
                    HeatedBed.ZoneInfo zoneInfo = bedStatus.getZoneList().get(0);
                    mTvHeatedTempCurrentTemp.setText(String.valueOf((int) zoneInfo.getCurrentTemperature()));
                    mTvHeatedTempTargetTemp.setText(getString(R.string.calibration_leveling_bed_heating_temperature, (int) zoneInfo.getTargetTemperature()));
                }, LogHelper::log);
        mViewModel.updateHeatedTime()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(times -> mTvHeatingBedTime.setText(getString(R.string.a400_leveling_bed_calibration_heat_bed, mViewModel.getWholeCalculateTime(), times)), LogHelper::log);

        subscribe = mViewModel.todoNext()
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(bool -> {
                    if (bool) {
                        ((A400LevelingBedCalibrationAutoActivity) requireActivity()).gotoLevelingBedCalibrationAuto();
                    }
                }, LogHelper::log);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_leveling_bed_calibration_heating;
    }

    @Override
    protected A400LevelingBedViewModel getViewModel() {
        return getViewModelProvider().get(A400LevelingBedViewModel.class);
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
        if (subscribe != null) subscribe.dispose();
    }
}
