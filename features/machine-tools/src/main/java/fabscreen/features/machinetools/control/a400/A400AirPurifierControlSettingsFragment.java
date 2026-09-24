package fabscreen.features.machinetools.control.a400;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.control.common.S30AirPurifierControlViewModel;
import fabscreen.platform.base.helper.DimensUtils;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.base.view.MenuAdapter;
import fabscreen.platform.base.view.WarmTipDialog;
import fabscreen.platform.core.ui.common.A400SwitchCompat;
import fabscreen.platform.core.ui.view.FabAlert;
import fabscreen.platform.core.ui.view.PullDownMenu;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class A400AirPurifierControlSettingsFragment extends BaseFragment {

    public static Fragment newInstance() {
        return new A400AirPurifierControlSettingsFragment();
    }

    @BindView(R2.id.switch_laser_on)
    A400SwitchCompat mSwitchLaserOn;
    @BindView(R2.id.switch_laser_off)
    A400SwitchCompat mSwitchLaserOff;

    @BindView(R2.id.tv_laser_timer_title)
    TextView mTVLTimerTitle;
    @BindView(R2.id.ll_laser_timer)
    LinearLayout mLlAllLaserTimer;
    @BindView(R2.id.rl_laser_timer)
    RelativeLayout mRlLTimer;
    @BindView(R2.id.tv_timer_value)
    TextView mTvLTimerValue;
    int mLaserCheckId;
    private MenuAdapter mMenuAdapter;

    @BindView(R2.id.switch_3dp_on)
    A400SwitchCompat mSwitch3dpOn;
    @BindView(R2.id.switch_3dp_off)
    A400SwitchCompat mSwitch3dpOff;
    @BindView(R2.id.tv_3dp_timer_title)
    TextView mTVDpTimerTitle;
    @BindView(R2.id.ll_3dp_time)
    LinearLayout mLlAllDpTimer;
    @BindView(R2.id.rl_3dp_timer)
    RelativeLayout mRlDpTimer;
    @BindView(R2.id.tv_3dp_value)
    TextView mTvDpTimerValue;
    int mFdmCheckId;

    @BindView(R2.id.switch_cnc_on)
    A400SwitchCompat mSwitchCncOn;
    @BindView(R2.id.switch_cnc_off)
    A400SwitchCompat mSwitchCncOff;
    @BindView(R2.id.tv_cnc_timer_title)
    TextView mTVCNCTimerTitle;
    @BindView(R2.id.ll_cnc_timer)
    LinearLayout mllAllCNCTimer;
    @BindView(R2.id.rl_cnc_timer)
    RelativeLayout mRlCNCTimer;
    @BindView(R2.id.tv_cnc_value)
    TextView mTvCNCTimerValue;
    int mCncCheckId;

    private int mSelectType;
    private int cutterPosition;
    public static final int LASER_TYPE = 101;
    public static final int DP_TYPE = 102;
    public static final int CNC_TYPE = 103;

    private S30AirPurifierControlViewModel mViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
    }

    private void initView() {
        setTitle(getString(R.string.a400_control_air_purigier_setttings));

        ArrayList<String> menuItems = new ArrayList<>();
        menuItems.add(getString(R.string.a400_control_air_purifier_settings_timer_three_title));
        menuItems.add(getString(R.string.a400_control_air_purifier_settings_timer_five_title));
        menuItems.add(getString(R.string.a400_control_air_purifier_settings_timer_ten_title));

        mMenuAdapter = new MenuAdapter(getContext(), menuItems);
        mMenuAdapter.setOnItemClickListener((view, position) -> {
            playNormalClickSound();
            if (mSelectType == LASER_TYPE) {
                mLaserCheckId = position;
                mTvLTimerValue.setText(menuItems.get(position));
                setDelayStop(mLlAllLaserTimer, IMachine.WorkType.LASER, idToDelayTime(mLaserCheckId));

            } else if (mSelectType == DP_TYPE) {
                mFdmCheckId = position;
                mTvDpTimerValue.setText(menuItems.get(position));
                setDelayStop(mLlAllLaserTimer, IMachine.WorkType.FDM, idToDelayTime(mFdmCheckId));
            } else {
                mCncCheckId = position;
                mTvCNCTimerValue.setText(menuItems.get(position));
                setDelayStop(mllAllCNCTimer, IMachine.WorkType.CNC, idToDelayTime(mCncCheckId));
            }
            PullDownMenu.dismiss();
        });

        mViewModel.getAutoState(IMachine.WorkType.LASER).observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe(aBoolean -> mSwitchLaserOn.setChecked(aBoolean));
        mViewModel.getDelayStop(IMachine.WorkType.LASER).observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe(integer -> {
            if (integer == 65535) {
                mSwitchLaserOff.setChecked(false);
            } else {
                mLaserCheckId = delayTimeToId(integer);
                mSwitchLaserOff.setChecked(true);
            }
        });

        mViewModel.getAutoState(IMachine.WorkType.FDM).observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe(aBoolean -> mSwitch3dpOn.setChecked(aBoolean));
        mViewModel.getDelayStop(IMachine.WorkType.FDM).observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe(integer -> {
            if (integer == 65535) {
                mSwitch3dpOff.setChecked(false);
            } else {
                mFdmCheckId = delayTimeToId(integer);
                mSwitch3dpOff.setChecked(true);
            }
        });

        mViewModel.getAutoState(IMachine.WorkType.CNC).observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe(aBoolean -> mSwitchCncOn.setChecked(aBoolean));
        mViewModel.getDelayStop(IMachine.WorkType.CNC).observeOn(AndroidSchedulers.mainThread()).as(bindToLifecycle()).subscribe(integer -> {
            if (integer == 65535) {
                mSwitchCncOff.setChecked(false);
            } else {
                mCncCheckId = delayTimeToId(integer);
                mSwitchCncOff.setChecked(true);
            }
        });
    }

    @OnCheckedChanged(R2.id.switch_laser_on)
    public void onClickLaserOn(CompoundButton view, boolean isCheck) {
        playSwitchSound();
        mViewModel.setAutoState(IMachine.WorkType.LASER, isCheck)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aBoolean -> {
                    if (!aBoolean) {
                        showErrorDialog();
                    }

                }, e -> {
                    LogHelper.log(e);
                    showErrorDialog();
                });
    }

    @OnCheckedChanged(R2.id.switch_3dp_on)
    public void onClickFDMOn(CompoundButton view, boolean isCheck) {
        playSwitchSound();
        mViewModel.setAutoState(IMachine.WorkType.FDM, isCheck)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aBoolean -> {
                    if (!aBoolean) {
                        showErrorDialog();
                    }
                }, e -> {
                    LogHelper.log(e);
                    showErrorDialog();
                });
    }

    @OnCheckedChanged(R2.id.switch_cnc_on)
    public void onClickCncOn(CompoundButton view, boolean isCheck) {
        playSwitchSound();
        mViewModel.setAutoState(IMachine.WorkType.CNC, isCheck)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aBoolean -> {
                    if (!aBoolean) {
                        showErrorDialog();
                    }
                }, e -> {
                    LogHelper.log(e);
                    showErrorDialog();
                });
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

    @OnCheckedChanged(R2.id.switch_3dp_off)
    public void onClickFdmOff(CompoundButton view, boolean isCheck) {
        playSwitchSound();
        mFdmCheckId = 0;
        setDelayStop(mLlAllDpTimer, IMachine.WorkType.FDM, isCheck ? idToDelayTime(mFdmCheckId) : 65535);
    }

    @OnCheckedChanged(R2.id.switch_cnc_off)
    public void onClickCncOff(CompoundButton view, boolean isCheck) {
        playSwitchSound();
        mCncCheckId = 0;
        setDelayStop(mllAllCNCTimer, IMachine.WorkType.CNC, isCheck ? idToDelayTime(mCncCheckId) : 65535);
    }

    @OnCheckedChanged(R2.id.switch_laser_off)
    public void onClickLaserOff(CompoundButton view, boolean isCheck) {
        playSwitchSound();
        mLaserCheckId = 0;
        setDelayStop(mLlAllLaserTimer, IMachine.WorkType.LASER, isCheck ? idToDelayTime(mLaserCheckId) : 65535);
    }

    @OnClick({R2.id.rl_laser_timer})
    public void onClickLaserTimer() {
        playSwitchSound();
        showMenu(LASER_TYPE, mRlLTimer);
    }

    @OnClick({R2.id.rl_3dp_timer})
    public void onClickThreeDpTimer() {
        playSwitchSound();
        showMenu(DP_TYPE, mRlDpTimer);
    }

    @OnClick({R2.id.rl_cnc_timer})
    public void onClickCNCTimer() {
        playSwitchSound();
        showMenu(CNC_TYPE, mRlCNCTimer);
    }

    public void showMenu(int type, View view) {
        mSelectType = type;
        playNormalClickSound();
        if (mSelectType == LASER_TYPE) {
            cutterPosition = mLaserCheckId;
            PullDownMenu.create(getContext(), mMenuAdapter)
                    .showBelowView(view, -(int) DimensUtils.dp2px(250), 10);

        } else if (mSelectType == DP_TYPE) {
            cutterPosition = mFdmCheckId;
            PullDownMenu.create(getContext(), mMenuAdapter)
                    .showBelowView(view, -(int) DimensUtils.dp2px(250), 10);
        } else {
            cutterPosition = mCncCheckId;
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            PullDownMenu.create(getContext(), mMenuAdapter)
                    .showAtLocation(view, Gravity.NO_GRAVITY, location[0] - (int) DimensUtils.dp2px(260), location[1]);
        }
        if (mMenuAdapter != null) {
            mMenuAdapter.setSelectPosition(cutterPosition);
        }

    }

    public void setDelayStop(View view, IMachine.WorkType workType, int delaytime) {
        mViewModel.setDelayStop(workType, delaytime)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aBoolean -> {
                    if (!aBoolean) {
                        showErrorDialog();
                    } else {
                        view.setVisibility(delaytime == 65535 ? View.GONE : View.VISIBLE);
                    }
                }, e -> {
                    LogHelper.log(e);
                    showErrorDialog();
                });
    }


    private int idToDelayTime(int pointID) {
        switch (pointID) {
            case 0:
                return 180;
            case 1:
                return 300;
            case 2:
                return 480;
            default:
                return 0;
        }
    }

    private int delayTimeToId(int delayTime) {
        switch (delayTime) {
            case 180:
                return 0;
            case 300:
                return 1;
            case 480:
                return 2;
            default:
                return 0;
        }
    }


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_a400_air_purifier_settings;
    }

    @Override
    protected S30AirPurifierControlViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(S30AirPurifierControlViewModel.class);
    }
}
