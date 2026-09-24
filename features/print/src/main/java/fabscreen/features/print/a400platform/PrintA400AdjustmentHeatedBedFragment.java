package fabscreen.features.print.a400platform;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.fragment.app.Fragment;

import java.util.List;

import butterknife.BindView;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.a400platform.viewmodel.A400HeatedBedControlViewModel;
import fabscreen.features.print.j1platform.viewmodel.PrintJ1AdjustmentHeatedBedViewModel;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.entity.module.HeatedBed;
import fabscreen.platform.base.view.BaseFragment;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class PrintA400AdjustmentHeatedBedFragment extends BaseFragment {

    @BindView(R2.id.tv_heat_status)
    TextView mTvHeatStatus;
    @BindView(R2.id.tv_all_target_temp)
    TextView mTvAllTargetTemp;
    @BindView(R2.id.sb_all_target)
    AppCompatSeekBar mSbAllTarget;
    @BindView(R2.id.tv_zone_0_target_temp)
    TextView mTvZone0TargetTemp;
    @BindView(R2.id.sb_zone_0_target)
    AppCompatSeekBar mSbZone0Target;
    @BindView(R2.id.tv_zone_1_target_temp)
    TextView mTvZone1TargetTemp;
    @BindView(R2.id.sb_zone_1_target)
    AppCompatSeekBar mSbZone1Target;
    @BindView(R2.id.tv_zone_0_cur_temp)
    TextView mTvZone0CurTemp;
    @BindView(R2.id.pb_zone_0_current)
    ProgressBar mPbZone0CurTemp;
    @BindView(R2.id.tv_zone_1_cur_temp)
    TextView mTvZone1CurTemp;
    @BindView(R2.id.pb_zone_1_current)
    ProgressBar mPbZone1CurTemp;

    @BindView(R2.id.tv_all_zone_title)
    TextView mTvAllZoneTitle;
    @BindView(R2.id.tv_zone1_target_title)
    TextView mTvZone1TargetTitle;
    @BindView(R2.id.tv_zone_1_cur_title)
    TextView mTvZone1CurTitle;

    private A400HeatedBedControlViewModel mViewModel;

    public static Fragment newInstance() {
        return new PrintA400AdjustmentHeatedBedFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = getViewModel();
        mViewModel.subscribeTemperatureChange();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        if (mViewModel.getMachineSeriesId() == IMachine.MachineSeries.J) {
            mTvAllTargetTemp.setVisibility(View.GONE);
            mSbAllTarget.setVisibility(View.GONE);
            mSbZone1Target.setVisibility(View.GONE);
            mTvZone1CurTemp.setVisibility(View.GONE);
            mTvZone1TargetTemp.setVisibility(View.GONE);
            mPbZone1CurTemp.setVisibility(View.GONE);
            mTvAllZoneTitle.setVisibility(View.GONE);
            mTvZone1TargetTitle.setVisibility(View.GONE);
            mTvZone1CurTitle.setVisibility(View.GONE);
        }

        mSbAllTarget.setTag(true);
        mSbZone0Target.setTag(true);
        mSbZone1Target.setTag(true);

        mViewModel.getHeatedBedStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(status -> {
                    List<HeatedBed.ZoneInfo> zoneList = status.getZoneList();
                    int zone0TargetTemp = zoneList.get(0).getTargetTemperature();
                    int zone0CurTemp = (int) (zoneList.get(0).getCurrentTemperature());
                    int zone1TargetTemp = 0;
                    int zone1CurTemp = 0;
                    if (zoneList.size() > 1) {
                        zone1TargetTemp = zoneList.get(1).getTargetTemperature();
                        zone1CurTemp = (int) zoneList.get(1).getCurrentTemperature();
                    }
                    setSeekbarProgress(mSbZone0Target, zone0TargetTemp);
                    mTvZone0CurTemp.setText(String.valueOf(zone0CurTemp));
                    mPbZone0CurTemp.setProgress(zone0CurTemp);
                    setSeekbarProgress(mSbZone1Target, zone1TargetTemp);
                    mTvZone1CurTemp.setText(String.valueOf(zone1CurTemp));
                    mPbZone1CurTemp.setProgress(zone1CurTemp);
                });

        mSbAllTarget.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTvAllTargetTemp.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mSbAllTarget.setTag(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSbAllTarget.setTag(true);
                mViewModel.setAllZonesTemp(seekBar.getProgress());
            }
        });
        mSbZone0Target.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTvZone0TargetTemp.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mSbZone0Target.setTag(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSbZone0Target.setTag(true);
                mViewModel.setZoneTemp(0, seekBar.getProgress());
            }
        });
        mSbZone1Target.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTvZone1TargetTemp.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mSbZone1Target.setTag(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSbZone1Target.setTag(true);
                mViewModel.setZoneTemp(1, seekBar.getProgress());
            }
        });
    }

    private void setSeekbarProgress(AppCompatSeekBar seekBar, int progress) {
        if ((Boolean) seekBar.getTag()) {
            seekBar.setProgress(progress);
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_print_adjustment_heated_bed;
    }

    @Override
    protected A400HeatedBedControlViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(A400HeatedBedControlViewModel.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mViewModel.unSubscribeTemperatureChange();
    }
}
