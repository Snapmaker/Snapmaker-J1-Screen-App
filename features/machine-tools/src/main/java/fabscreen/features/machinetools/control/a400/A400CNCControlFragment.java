package fabscreen.features.machinetools.control.a400;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.control.common.S30CNCControlViewModel;
import fabscreen.platform.base.service.machine.IStructure;
import fabscreen.platform.base.service.machine.structure.ResponseStructure;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.core.ui.view.RotateButtonView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class A400CNCControlFragment extends BaseFragment {

    @BindView(R2.id.tv_cnc_control_rpm)
    TextView mTvRpm;
    @BindView(R2.id.tv_cnc_control_company)
    TextView mTvCompany;
    @BindView(R2.id.btn_cnc_control_switch)
    Button mBtnSwitch;
    @BindView(R2.id.rbv_cnc_control)
    RotateButtonView mRbvCncControl;

    private S30CNCControlViewModel mViewModel;

    public static Fragment newInstance() {
        return new A400CNCControlFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
    }

    private void initView() {
        mViewModel.getCncToolHeadInfoObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(cncToolheadInfo -> {
                    boolean isOn = cncToolheadInfo.getRunningState() == 1;
                    mRbvCncControl.setUseColor2(isOn);
                    mBtnSwitch.setText(isOn ? R.string.all_cnc_turn_off : R.string.all_cnc_turn_on);
                    mBtnSwitch.setTextColor(ContextCompat.getColor(requireContext(), isOn ?
                            R.color.palette_white_pure : R.color.palette_white_silver));
                    mBtnSwitch.setBackgroundResource(isOn ? R.drawable.pic_a400_cnc_on_bg : R.drawable.pic_a400_cnc_off_bg);
                    if (mViewModel.getMode() == S30CNCControlViewModel.POWER_MODE) {
                        mTvRpm.setText(cncToolheadInfo.getTargetPower() + "");
                        mRbvCncControl.setColor1Progress(cncToolheadInfo.getTargetPower());
                        mRbvCncControl.setColor2Progress(cncToolheadInfo.getCurrentPower());
                    } else if (mViewModel.getMode() == S30CNCControlViewModel.RPM_MODE) {
                        mTvRpm.setText(cncToolheadInfo.getTargetSpeed() + "");
                        mRbvCncControl.setColor1Progress(cncToolheadInfo.getTargetSpeed());
                        mRbvCncControl.setColor2Progress(cncToolheadInfo.getCurrentSpeed());
                    }
                });

        if (mViewModel.getMode() == S30CNCControlViewModel.POWER_MODE) {
            mRbvCncControl.setMin(0);
            mRbvCncControl.setMax(100);
            mTvCompany.setText("%");
        } else if (mViewModel.getMode() == S30CNCControlViewModel.RPM_MODE) {
            mRbvCncControl.setMin(0);
            mRbvCncControl.setMax(18000);
            mTvCompany.setText("rpm");
        }

        mRbvCncControl.setCrollerChangeListener(new RotateButtonView.OnCrollerChangeListener() {
            @Override
            public void onProgressChanged(RotateButtonView croller, float progress) {
                mViewModel.setSpindleSpeed((int) progress);
            }

            @Override
            public void onStartTrackingTouch(RotateButtonView croller, float progress) {
                mViewModel.setSpindleSpeed((int) progress);
            }

            @Override
            public void onStopTrackingTouch(RotateButtonView croller, float progress) {
                mViewModel.setSpindleSpeed((int) progress);
            }
        });

    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_control_cnc;
    }

    @Override
    protected S30CNCControlViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(S30CNCControlViewModel.class);
    }

    @OnClick(R2.id.btn_cnc_control_switch)
    void onSpindlePowerClicked() {
        playNormalClickSound();
        int currentPower = mViewModel.getCurrentPower();
        Observable<ResponseStructure<IStructure>> switchCNCObservable;
        if (currentPower <= 0) {
            DecisionDialog.create(requireContext())
                    .setDialogStatus(DecisionDialog.BTN_TWO, true, false, true, false)
                    .setType(DecisionDialog.TIP_TYPE)
                    .setPic(R.drawable.pic_a400_control_cnc_open)
                    .setTitle(R.string.control_cnc_open_title)
                    .setContent(R.string.control_cnc_open)
                    .setFirstTv(requireContext().getResources().getString(R.string.all_cancel), R.color.select_dialog_white_txt, ((dialog, which) -> {
                        dialog.dismiss();
                    }))
                    .setSecondTv(requireContext().getResources().getString(R.string.all_confirm), R.color.select_dialog_orange_txt, ((dialog, which) -> {
                        dialog.dismiss();
                        mViewModel.switchSpindlePower();
                    })).show();

        } else {
            mViewModel.switchSpindlePower();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.subscribeCNCInfo();
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.unSubscribeCNCInfo();
    }
}
