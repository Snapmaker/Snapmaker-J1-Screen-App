package fabscreen.features.machinetools.control.a400;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.control.common.S30LaserControlViewModel;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class A400LaserControlFragment extends BaseFragment {

    @BindView(R2.id.iv_laser_contronl_light)
    ImageView mIvLaserLight;
    @BindView(R2.id.btn_layer_contronl_swich)
    Button mBtnSwitch;
    @BindView(R2.id.tv_layer_contronl_swich_title)
    TextView mTVSwitchTitle;

    private S30LaserControlViewModel mViewModel;

    public static Fragment newInstance() {
        return new A400LaserControlFragment();
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
    }

    private void initView() {
        mViewModel.getLaserPowerObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isOn -> {
                    changeSwitch(isOn);
                }, LogHelper::log);

    }

    public void changeSwitch(boolean isOpen) {
        mIvLaserLight.setImageResource(isOpen ? R.drawable.pic_laser_light_on : R.drawable.pic_laser_light_off);
        mBtnSwitch.setBackgroundResource(isOpen ? R.drawable.pic_laser_light_swich_on : R.drawable.pic_laser_light_swich_off);
        mTVSwitchTitle.setText(isOpen ? "Turn Off" : "Turn On ");
        mTVSwitchTitle.setTextColor(ContextCompat.getColor(requireContext(), isOpen ? R.color.palette_white_pure : R.color.palette_grey_dim));
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_control_laser;
    }

    @Override
    protected S30LaserControlViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(S30LaserControlViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.subscribeLaserStatus();
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.unsubscribeLaserStatus();
    }

    @OnClick(R2.id.btn_layer_contronl_swich)
    void onLaserPowerClicked() {
        playNormalClickSound();
        float power = mViewModel.getCurrentPower();
        if (power <= 0) {
            DecisionDialog.create(requireContext())
                    .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, false)
                    .setPic(R.drawable.ic_laser_glass)
                    .setType(DecisionDialog.TIP_TYPE)
                    .setTitle(R.string.laser_safety_goggles_open)
                    .setContent(R.string.control_laser_open)
                    .setFirstTv(requireContext().getResources().getString(R.string.all_cancel), R.color.select_dialog_white_txt, ((dialog, which) -> {
                        dialog.dismiss();
                    }))
                    .setSecondTv(requireContext().getResources().getString(R.string.all_confirm), R.color.select_dialog_orange_txt, ((dialog, which) -> {
                        dialog.dismiss();
                        mViewModel.switchLaserPower();

                    })).show();
        } else {
            mViewModel.switchLaserPower();

        }
    }
}
