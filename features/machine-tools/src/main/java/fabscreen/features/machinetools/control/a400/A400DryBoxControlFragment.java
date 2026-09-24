package fabscreen.features.machinetools.control.a400;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.control.common.S30DryBoxControlViewModel;
import fabscreen.platform.base.helper.DecimalUtils;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.base.view.MenuAdapter;
import fabscreen.platform.core.ui.view.PullDownMenu;
import fabscreen.platform.core.ui.view.RotateButtonView;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class A400DryBoxControlFragment extends BaseFragment {

    @BindView(R2.id.tv_current_temperature)
    TextView mTvCurrentTemperature;
    @BindView(R2.id.tv_current_humidity)
    TextView mTvCurrentHumidity;
    @BindView(R2.id.tv_cumulative_heating_time)
    TextView mTvCumulativeHeatingTime;
    @BindView(R2.id.tv_door_status)
    TextView mTvDoorStatus;

    @BindView(R2.id.rbv_dry_box_control)
    RotateButtonView mRbvControl;
    @BindView(R2.id.tv_dry_box_time)
    TextView mTvTime;
    @BindView(R2.id.btn_dry_box_control_switch)
    Button mBTnSwitch;
    @BindView(R2.id.rl_filamanet_dryer_temperature)
    RelativeLayout mRlTemperature;
    @BindView(R2.id.tv_temperature_value)
    TextView mTvTemperatureValue;
    @BindView(R2.id.iv_temperature_ic)
    ImageView mIvTemperatureLogo;
    @BindView(R2.id.tv_no_power_bg)
    TextView mTvNotPower;
    @BindView(R2.id.ll_no_power_tip)
    LinearLayout mLlNotPower;

    private MenuAdapter mMenuAdapter;
    private int mSelectMunPosition;
    private ArrayList<String> menuItems;
    int mCutterTemperature = -1;

    private S30DryBoxControlViewModel mViewModel;
    private long tarTime = -1;

    public static Fragment newInstance() {
        return new A400DryBoxControlFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
    }

    private void initView() {
        initTuner();
        initViewObservers();
    }

    private void initTuner() {

        menuItems = new ArrayList<>();
        menuItems.add("25°C");
        menuItems.add("50°C");
        menuItems.add("75°C");
        mMenuAdapter = new MenuAdapter(getContext(), menuItems);
        mTvTime.setText(mViewModel.getHMFormatTime(1800));
        mMenuAdapter.setOnItemClickListener((view, position) -> {
            playNormalClickSound();
            mSelectMunPosition = position;
            mTvTemperatureValue.setText(menuItems.get(position));

            mCutterTemperature = positionToTemperature(position);
            setTemperature(mCutterTemperature);

            PullDownMenu.dismiss();
        });

        mRbvControl.setMin(0.5f);
        mRbvControl.setMax(24f);
        mRbvControl.setUseColor2(true);
        mRbvControl.setCrollerChangeListener(new RotateButtonView.OnCrollerChangeListener() {
            @Override
            public void onProgressChanged(RotateButtonView croller, float progress) {
                mTvTime.setText(mViewModel.getHMFormatTime(mViewModel.hourToSeconds(progress)));
                setTime(progress);
            }

            @Override
            public void onStartTrackingTouch(RotateButtonView croller, float progress) {
                mTvTime.setText(mViewModel.getHMFormatTime(mViewModel.hourToSeconds(progress)));
                setTime(progress);
            }

            @Override
            public void onStopTrackingTouch(RotateButtonView croller, float progress) {
                mTvTime.setText(mViewModel.getHMFormatTime(mViewModel.hourToSeconds(progress)));
                setTime(progress);
            }
        });

    }

    private void setTime(float time) {
        mViewModel.setTargetTime(time)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                }, LogHelper::log);
    }

    private void setTemperature(int temperature) {
        mViewModel.setTargetTemperature(temperature)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                }, LogHelper::log);
    }

    private void initViewObservers() {

        mViewModel.getDryStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(started -> {

                    mBTnSwitch.setText(!started ? getString(R.string.a400_control_filament_dryer_dry) : getString(R.string.a400_control_filament_dryer_stop_dry));
                    mBTnSwitch.setTextColor(ContextCompat.getColor(requireContext(), started ? R.color.palette_white_pure : R.color.palette_white_silver));
                    mBTnSwitch.setBackgroundResource(started ? R.drawable.pic_a400_cnc_on_bg : R.drawable.pic_a400_cnc_off_bg);

                    mRlTemperature.setEnabled(!started);
                    mTvTemperatureValue.setTextColor(ContextCompat.getColor(requireContext(), started ? R.color.palette_grey_mine_shaft : R.color.palette_white_pure));
                    mIvTemperatureLogo.setBackgroundResource(started ? R.drawable.ic_down_arrow_black_136x136 : R.drawable.ic_down_arrow_136x136);

                    mRbvControl.setEnabled(!started);
                }, LogHelper::log);

        mViewModel.getCurrentTempObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(curTemp -> mTvCurrentTemperature.setText(DecimalUtils.getNoMoreThan1DigitsFloor(curTemp)), LogHelper::log);

//        mViewModel.getTargetTempObservable()
//                .observeOn(AndroidSchedulers.mainThread())
//                .as(bindToLifecycle())
//                .subscribe(targetTemp -> mNpTemperature.setValue(Arrays.asList(mTemperatureArray).indexOf(String.valueOf(targetTemp))), LogHelper::log);

        mViewModel.getCurrentHumidityObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(curHumidity -> mTvCurrentHumidity.setText(String.valueOf(curHumidity)), LogHelper::log);

//        mViewModel.getRemainTimeObservable()
//                .observeOn(AndroidSchedulers.mainThread())
//                .as(bindToLifecycle())
//                .subscribe(remainTime ->
//                        {
//                            mRbvControl.setColor1Progress(mViewModel.secondsToHour(remainTime));
//                            mTvCumulativeHeatingTime.setText(mViewModel.formatTime(remainTime));
//                            mTvTime.setText(mViewModel.getHMFormatTime(remainTime));
//                        }
//                        , LogHelper::log);
        mViewModel.getDoorStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(status -> mTvDoorStatus.setText(String.valueOf(status)), LogHelper::log);

        mViewModel.getPowerStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(status -> {
                    mLlNotPower.setVisibility(status == 0 ? View.VISIBLE : View.GONE);
                    mTvNotPower.setVisibility(status == 0 ? View.VISIBLE : View.GONE);
                }, LogHelper::log);


        mViewModel.getDryBoxInfoObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(dryBoxStatus -> {
                    mRbvControl.setUseColor2(dryBoxStatus.getDryState() == 1);
                    mRbvControl.setTouchable(dryBoxStatus.getDryState() != 1);

                    if (dryBoxStatus.getTargetHeatingTime() != 0) {
                        tarTime = dryBoxStatus.getTargetHeatingTime();
                    }

                    mRbvControl.setColor2Progress(mViewModel.secondsToHour(dryBoxStatus.getResidualHeatingTime()));
                    mTvCumulativeHeatingTime.setText(mViewModel.formatTime(dryBoxStatus.getTargetHeatingTime()));

                    if (dryBoxStatus.getDryState() == 1) {
                        mRbvControl.setColor1Progress(mViewModel.secondsToHour(dryBoxStatus.getTargetHeatingTime()));
                        mTvTime.setText(mViewModel.getHMFormatTime(dryBoxStatus.getResidualHeatingTime()));
                    } else {
                        if (tarTime == -1) {
                            mRbvControl.setColor1Progress(0.5f);
                            mTvTime.setText(mViewModel.getHMFormatTime(1800));
                            setTime(0.5f);
                        } else {
                            mRbvControl.setColor1Progress(mViewModel.secondsToHour(tarTime));
                            mTvTime.setText(mViewModel.getHMFormatTime(tarTime));
                            setTime(mViewModel.secondsToHour(tarTime));
                        }

                    }

                    mSelectMunPosition = temperatureToPosition(dryBoxStatus.getTempTargetChamber());
                    setTemperature(positionToTemperature(mSelectMunPosition));
                    mTvTemperatureValue.setText(menuItems.get(mSelectMunPosition));

                }, LogHelper::log);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_control_dry_box;
    }

    @Override
    protected S30DryBoxControlViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(S30DryBoxControlViewModel.class);
    }

    @OnClick(R2.id.btn_dry_box_control_switch)
    void onDryActionClicked() {
        playNormalClickSound();
        mViewModel.switchDryState()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    if (success) {

                    } else {
                        showErrorDialog();
                    }
                }, LogHelper::log);
    }

    public void showErrorDialog() {
        DecisionDialog.create(requireContext())
                .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
                .setType(DecisionDialog.WARMING_TYPE)
                .setContent(R.string.a400_control_air_purifier_settings_setting_fail_msg)
                .setFirstTv(R.string.all_ok, R.color.select_dialog_white_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @OnClick(R2.id.rl_filamanet_dryer_temperature)
    public void onClickTemperature() {
        playNormalClickSound();
        PullDownMenu.create(getContext(), mMenuAdapter)
                .setElevation(24)
                .showBelowView(mRlTemperature, -(int) DimensUtils.dp2px(200), 10);
        if (mMenuAdapter != null) {
            mMenuAdapter.setSelectPosition(mSelectMunPosition);
        }
    }

    public int positionToTemperature(int position) {
        switch (position) {
            case 0:
                return 25;
            case 1:
                return 50;
            case 2:
                return 75;
            default:
                return 25;
        }
    }

    public int temperatureToPosition(int temperature) {
        switch (temperature) {
            case 25:
                return 0;
            case 50:
                return 1;
            case 75:
                return 2;
            default:
                return 0;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.subscribeDryBoxStatus();
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.unSubscribeDryBoxStatus();
    }
}
