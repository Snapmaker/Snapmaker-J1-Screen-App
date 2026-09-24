package fabscreen.features.print.j1platform;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import butterknife.BindView;
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.j1platform.viewmodel.PrintJ1AdjustmentFanSpeedViewModel;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.entity.parts.Fan;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.core.ui.view.CustomArcSeekBar;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class PrintJ1AdjustmentFanSpeedFragment extends BaseFragment {

    @BindView(R2.id.cas_target_left)
    CustomArcSeekBar mCasTargetLeft;
    @BindView(R2.id.cas_target_right)
    CustomArcSeekBar mCasTargetRight;
    @BindView(R2.id.tv_l_current_temp)
    TextView mTvCurrentLTemp;
    @BindView(R2.id.tv_r_current_temp)
    TextView mTvCurrentRTemp;
    @BindView(R2.id.tv_l_t_setting)
    TextView mTvLTSetting;
    @BindView(R2.id.tv_r_t_setting)
    TextView mTvRTSetting;
    @BindView(R2.id.rl_nozzle_right)
    RelativeLayout mRlRTSetting;


    private PrintJ1AdjustmentFanSpeedViewModel mViewModel;
    private static final int FAN_SPEED_MAX_VALUE = 100;
    private static final int FAN_SPEED_MIN_VALUE = 0;

    public static Fragment newInstance() {
        return new PrintJ1AdjustmentFanSpeedFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_print_adjustment_fan_speed;
    }

    private void initView() {
        final int deltaValue = FAN_SPEED_MAX_VALUE - FAN_SPEED_MIN_VALUE;
        mCasTargetLeft.setMax(deltaValue);
        mCasTargetRight.setMax(deltaValue);
        IMachine machine = ServiceContainer.getInstance().getService(IMachine.class);
        machine.getFDMController().getToolheadStatusSubjectHolder(0)
                .getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(fdmToolheadStatus -> {
                    ArrayList<Fan> fanLists = (ArrayList<Fan>) fdmToolheadStatus.getFanList();
                    int extruder0Speed = fanLists.get(0).getSpeedLevel();
                    int e0SpeedPercent = (int) ((extruder0Speed / 255f) * 100);
                    mTvCurrentLTemp.setText(e0SpeedPercent + "");
                    mCasTargetLeft.setProgress(e0SpeedPercent - FAN_SPEED_MIN_VALUE);
                });

        machine.getFDMController().getToolheadStatusSubjectHolder(1)
                .getObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(fdmToolheadStatus -> {
                    ArrayList<Fan> fanLists = (ArrayList<Fan>) fdmToolheadStatus.getFanList();
                    int extruder0Speed = fanLists.get(0).getSpeedLevel();
                    int e0SpeedPercent = (int) ((extruder0Speed / 255f) * 100);
                    mTvCurrentRTemp.setText(e0SpeedPercent + "");
                    mCasTargetRight.setProgress(e0SpeedPercent - FAN_SPEED_MIN_VALUE);
                });

        // listen seekbar
        mCasTargetLeft.setOnSeekArcChangeListener(new CustomArcSeekBar.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(CustomArcSeekBar customArcSeekBar, int progress, boolean fromUser) {
                mTvLTSetting.setText((progress + FAN_SPEED_MIN_VALUE) + "");
            }

            @Override
            public void onStartTrackingTouch(CustomArcSeekBar customArcSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(CustomArcSeekBar customArcSeekBar) {
                int v = (int) ((FAN_SPEED_MIN_VALUE + customArcSeekBar.getProgress()) / 100f * 255);
                mViewModel.setFanSpeed(0, 0, v)
                        .observeOn(AndroidSchedulers.mainThread())
                        .as(bindToLifecycle())
                        .subscribe(success -> {
                        });
            }
        });

        mCasTargetRight.setOnSeekArcChangeListener(new CustomArcSeekBar.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(CustomArcSeekBar customArcSeekBar, int progress, boolean fromUser) {
                mTvRTSetting.setText((progress + FAN_SPEED_MIN_VALUE) + "");
            }

            @Override
            public void onStartTrackingTouch(CustomArcSeekBar customArcSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(CustomArcSeekBar customArcSeekBar) {
                int v = (int) ((FAN_SPEED_MIN_VALUE + customArcSeekBar.getProgress()) / 100f * 255);
                mViewModel.setFanSpeed(1, 0, v)
                        .observeOn(AndroidSchedulers.mainThread())
                        .as(bindToLifecycle())
                        .subscribe(success -> {
                        });
            }
        });
    }

    @Override
    protected PrintJ1AdjustmentFanSpeedViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(PrintJ1AdjustmentFanSpeedViewModel.class);
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.unSubscribeFanChange();
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.subscribeFanChange();
    }
}
