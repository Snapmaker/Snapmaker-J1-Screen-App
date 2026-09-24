package fabscreen.features.print.j1platform;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.j1platform.viewmodel.PrintJ1AdjustmentHeatedBedViewModel;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.view.CustomArcSeekBar;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class PrintJ1AdjustmentHeatedBedFragment extends BaseFragment {
    private static final int BED_MIN_VALUE = 1;
    private static final int BED_MAX_VALUE = 100;
    @BindView(R2.id.tv_zone_0_target_temp)
    TextView mTvZone0TargetTemp;
    @BindView(R2.id.tv_zone_0_cur_temp)
    TextView mTvZone0CurTemp;
    @BindView(R2.id.cas_heated_bed_temp)
    CustomArcSeekBar mCasHeatedBed;
    @BindView(R2.id.btn_heated_bed_heat)
    Button mHeatedBedHeating;
    private int mTargetSeekBarMinValue;

    private PrintJ1AdjustmentHeatedBedViewModel mViewModel;
    private boolean mIsOpen = true;


    public static Fragment newInstance() {
        return new PrintJ1AdjustmentHeatedBedFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getFragmentScopeViewModel(PrintJ1AdjustmentHeatedBedViewModel.class);
        initView();
        mTargetSeekBarMinValue = BED_MIN_VALUE;
    }

    private void initView() {
        mCasHeatedBed.setMax(BED_MAX_VALUE - BED_MIN_VALUE);
        mCasHeatedBed.setTag(true);
        mViewModel.geZone0StateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(filamentExtruderState -> {
                    mCasHeatedBed.setHeating(filamentExtruderState.isHeatingStats());
                    mTvZone0CurTemp.setText(String.format("%d", (int) (filamentExtruderState.getTemperature())));
                    if (filamentExtruderState.isHeatingStats()) {
                        setSeekBarProgress(mCasHeatedBed, (int) filamentExtruderState.getTargetTemperature());
                        if (!((int) filamentExtruderState.getTargetTemperature() == 0)) {
                            mTvZone0TargetTemp.setText(String.format("%d", (int) filamentExtruderState.getTargetTemperature()));
                        }
                    } else {
                        setSeekBarProgress(mCasHeatedBed, filamentExtruderState.getStopTemperature());
                        mTvZone0TargetTemp.setText(String.format("%d", filamentExtruderState.getStopTemperature()));
                    }
                    mHeatedBedHeating.setText(filamentExtruderState.isHeatingStats() ? R.string.all_stop : R.string.all_heat);
                });

        // listen seekbar
        mCasHeatedBed.setOnSeekArcChangeListener(new CustomArcSeekBar.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(CustomArcSeekBar customArcSeekBar, int progress, boolean fromUser) {
                mViewModel.setTargetChange(0, true);
                mViewModel.changeStopTemperature(0, progress + mTargetSeekBarMinValue);
                mTvZone0TargetTemp.setText(String.format("%d", progress + mTargetSeekBarMinValue));
            }

            @Override
            public void onStartTrackingTouch(CustomArcSeekBar customArcSeekBar) {
                customArcSeekBar.setTag(false);
            }

            @Override
            public void onStopTrackingTouch(CustomArcSeekBar customArcSeekBar) {
                customArcSeekBar.setTag(true);
                mViewModel.setTargetChange(0, false);
            }
        });

//        mViewModel.getHeatedBedStatusObservable()
//                .observeOn(AndroidSchedulers.mainThread())
//                .as(bindToLifecycle())
//                .subscribe(status -> {
//                    List<HeatedBed.ZoneInfo> zoneList = status.getZoneList();
//                    int zone0TargetTemp = zoneList.get(0).getTargetTemperature();
//                    int zone0CurTemp = (int) (zoneList.get(0).getCurrentTemperature());
//                    int zone1TargetTemp = 0;
//                    int zone1CurTemp = 0;
//                    if (zoneList.size() > 1) {
//                        zone1TargetTemp = zoneList.get(1).getTargetTemperature();
//                        zone1CurTemp = (int) zoneList.get(1).getCurrentTemperature();
//                    }
////                    setSeekbarProgress(mSbZone0Target, zone0TargetTemp);
//                    setSeekbarProgress(mCasHeatedBed, zone0TargetTemp);
//                    mTvZone0CurTemp.setText(String.valueOf(zone0CurTemp));
////                    mPbZone0CurTemp.setProgress(zone0CurTemp);
////                    setSeekbarProgress(mSbZone1Target, zone1TargetTemp);
////                    mTvZone1CurTemp.setText(String.valueOf(zone1CurTemp));
////                    mPbZone1CurTemp.setProgress(zone1CurTemp);
//                });
//        if (mViewModel.getMachineSeriesId() == IMachine.MachineSeries.J) {
////            mTvAllTargetTemp.setVisibility(View.GONE);
////            mSbAllTarget.setVisibility(View.GONE);
////            mSbZone1Target.setVisibility(View.GONE);
////            mTvZone1CurTemp.setVisibility(View.GONE);
////            mTvZone1TargetTemp.setVisibility(View.GONE);
////            mPbZone1CurTemp.setVisibility(View.GONE);
////            mTvAllZoneTitle.setVisibility(View.GONE);
////            mTvZone1TargetTitle.setVisibility(View.GONE);
////            mTvZone1CurTitle.setVisibility(View.GONE);
//        }
////        mSbAllTarget.setTag(true);
////        mSbZone0Target.setTag(true);
////        mSbZone1Target.setTag(true);
    }

    @OnClick(R2.id.btn_heated_bed_heat)
    void onSwitch0Heating() {
        playSwitchSound();
        int index = 0;
        int target = mViewModel.getTemperature(index, mCasHeatedBed.getProgress() + mTargetSeekBarMinValue);
        mViewModel.changeHeating(index, target);
    }

    private void setSeekBarProgress(CustomArcSeekBar customArcSeekBar, int progress) {
        if (((Boolean) customArcSeekBar.getTag()) && (progress >= mTargetSeekBarMinValue)) {
            customArcSeekBar.setProgress(progress - mTargetSeekBarMinValue);
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_print_adjustment_heated_bed;
    }

    @Override
    protected PrintJ1AdjustmentHeatedBedViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(PrintJ1AdjustmentHeatedBedViewModel.class);
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.unSubscribeTemperatureChange();
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.subscribeTemperatureChange();
    }
}
