package fabscreen.features.machinetools.control.j1;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class J1MotorControlFragment extends BaseFragment {
    public static Fragment newInstance() {
        return new J1MotorControlFragment();
    }

    @BindView(R2.id.sw_motor)
    SwitchMaterial mSwMotor;
    @BindView(R2.id.tv_j1_motor_control_status)
    TextView mTvMotorStatus;
    @BindView(R2.id.lin_j1_motor_control_switch)
    LinearLayout mLinMotorSwitch;
    @BindView(R2.id.iv_j1_motor_control_switch_pic)
    ImageView mIvPower;
    @BindView(R2.id.iv_j1_motor_control_light_pic)
    ImageView mIvLight;

    private J1MotorControlViewModel mViewModel;
    private boolean mIsOpen = false;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
    }

    private void initView() {

        mViewModel.getMotorStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isOn -> {
                    mIsOpen = isOn;
                    touchSwitch();
                }, LogHelper::log);

        mViewModel.getMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isOn -> {
                    mIsOpen = isOn;
                    touchSwitch();
                }, LogHelper::log);


    }

    @OnClick({R2.id.lin_j1_motor_control_switch, R2.id.iv_j1_motor_control_light_pic})
    public void onSwitchClick(View view) {
        if (view.getId() == R.id.lin_j1_motor_control_switch) {
            if (!mIsOpen) {
                return;
            }
            mViewModel.switchMotor(!mIsOpen);
        } else if (view.getId() == R.id.iv_j1_motor_control_light_pic) {
            if (mIsOpen) {
                return;
            }
            mViewModel.switchMotor(!mIsOpen);
        }

    }

    public void touchSwitch() {
        mTvMotorStatus.setText(mIsOpen ? getString(R.string.j1_control_heated_bed_on) : getString(R.string.j1_control_heated_bed_off));
        mLinMotorSwitch.setBackgroundResource(mIsOpen ? R.drawable.pic_tab_horizontal_right_440x104 : R.drawable.pic_tab_bg_horizontal_left_440x104);
        mIvPower.setImageResource(mIsOpen ? R.drawable.icon_off_normal_64x64 : R.drawable.icon_off_checked_64x64);
        mIvLight.setImageResource(mIsOpen ? R.drawable.icon_motor_checked_64x64 : R.drawable.icon_motor_normal_64x64);

    }


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_motor_control;
    }

    @Override
    protected J1MotorControlViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(J1MotorControlViewModel.class);
    }
}
