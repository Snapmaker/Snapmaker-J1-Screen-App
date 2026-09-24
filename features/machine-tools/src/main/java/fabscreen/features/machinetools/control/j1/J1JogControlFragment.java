package fabscreen.features.machinetools.control.j1;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.control.common.S30JogControlViewModel;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.FabConfirm;
import fabscreen.platform.base.view.FileLoadingDialog;
import fabscreen.platform.core.ui.data.MoveController;
import fabscreen.platform.core.ui.view.CustomSteeringView;
import fabscreen.platform.core.ui.view.SteeringView;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class J1JogControlFragment extends BaseFragment {
    @BindView(R2.id.csv_control_panel_xy)
    CustomSteeringView mCsvXY;
    @BindView(R2.id.tv_0_1)
    TextView mTvMovingRange01;
    @BindView(R2.id.tv_1_0)
    TextView mTvMovingRange1;
    @BindView(R2.id.tv_10)
    TextView mTvMovingRange10;
    @BindView(R2.id.btn_control_panel_z_plus)
    Button mBtnZPlus;
    @BindView(R2.id.btn_control_panel_z_minus)
    Button mBtnZMinus;
    @BindView(R2.id.btn_home)
    ImageView mBtnHome;

    @BindView(R2.id.lin_j1_motor_control_switch)
    LinearLayout mLinSwitch;
    @BindView(R2.id.tv_j1_control_l)
    TextView mTvControlL;
    @BindView(R2.id.tv_j1_control_r)
    TextView mTvControlR;
    private S30JogControlViewModel mViewModel;
    int mPrimaryColor;
    int mUncheckColor;
    int mDisabledColor;
    FileLoadingDialog fabHoming;
    private int mActive = -1;

    public static Fragment newInstance() {
        return new J1JogControlFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        fabHoming = FileLoadingDialog.create(requireContext(), true);
        fabHoming.setContent(getString(R.string.j1_calibration_moving));
        getColor();
        initView();
    }

    private void getColor() {
        mPrimaryColor = getValueOfColorAttr(R.attr.theme_color_primary);
        mUncheckColor = getValueOfColorAttr(R.attr.theme_color_tab_uncheck);
        mDisabledColor = getValueOfColorAttr(R.attr.theme_color_disabled);
    }

    private int getValueOfColorAttr(@AttrRes int attrId) {
        TypedValue typedValue = new TypedValue();
        if (requireContext().getTheme().resolveAttribute(attrId, typedValue, true)) {
            return typedValue.data;
        } else {
            return Color.TRANSPARENT;
        }
    }

    public void refreshSwitchButton(int activeIndex) {
        if (activeIndex == -1) return;
        mLinSwitch.setBackgroundResource(activeIndex == 1 ? R.drawable.pic_tab_horizontal_botton_440x104 : R.drawable.pic_tab_horizontal_top_440x104);
        mTvControlL.setTextColor(activeIndex != 1 ? mPrimaryColor : mUncheckColor);
        mTvControlR.setTextColor(activeIndex == 1 ? mPrimaryColor : mUncheckColor);
    }

    @OnClick(R2.id.tv_j1_control_l)
    public void onChuckL() {
        mViewModel.switchExtruder(0, 0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(result -> {
                }, LogHelper::log);
    }

    @OnClick(R2.id.tv_j1_control_r)
    public void onChuckR() {
        mViewModel.switchExtruder(1, 0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(result -> {
                }, LogHelper::log);
    }


    @OnClick(R2.id.tv_0_1)
    public void onCheckMovingRange01() {
        mViewModel.changeStepWidth(0);
        mTvMovingRange01.setTextColor(mPrimaryColor);
        mTvMovingRange1.setTextColor(mUncheckColor);
        mTvMovingRange10.setTextColor(mUncheckColor);
    }

    @OnClick(R2.id.tv_1_0)
    public void onCheckMovingRange1() {
        mViewModel.changeStepWidth(1);
        mTvMovingRange01.setTextColor(mUncheckColor);
        mTvMovingRange1.setTextColor(mPrimaryColor);
        mTvMovingRange10.setTextColor(mUncheckColor);
    }

    @OnClick(R2.id.tv_10)
    public void onCheckMovingRange10() {
        mViewModel.changeStepWidth(2);
        mTvMovingRange01.setTextColor(mUncheckColor);
        mTvMovingRange1.setTextColor(mUncheckColor);
        mTvMovingRange10.setTextColor(mPrimaryColor);
    }

    private void onDisableCheckMovingRange() {
        mTvMovingRange01.setTextColor(mDisabledColor);
        mTvMovingRange1.setTextColor(mDisabledColor);
        mTvMovingRange10.setTextColor(mDisabledColor);
    }

    private void initView() {
        // steering view
        mCsvXY.setSkin(CustomSteeringView.SteeringViewSkin.STEERING_VIEW_SKIN_J1);
        mCsvXY.setOnDirectionClickedListener(direction -> {
            playNormalClickSound();
            int actualDirection = 0;
            switch (direction) {
                case SteeringView.DIRECTION_UP:
                    actualDirection = SteeringView.DIRECTION_DOWN;
                    break;
                case SteeringView.DIRECTION_DOWN:
                    actualDirection = SteeringView.DIRECTION_UP;
                    break;
                case SteeringView.DIRECTION_LEFT:
                    actualDirection = SteeringView.DIRECTION_LEFT;
                    break;
                case SteeringView.DIRECTION_RIGHT:
                    actualDirection = SteeringView.DIRECTION_RIGHT;
                    break;
            }
            mViewModel.moveXYZByStep(actualDirection)
                    .observeOn(AndroidSchedulers.mainThread())
                    .as(bindToLifecycle())
                    .subscribe(responseStructure -> {
                        if (responseStructure.isGeneralError()) {
                            FabConfirm.create(getContext())
                                    .setDescription(getString(R.string.all_moving_limitation))
                                    .setConfirm(R.string.all_confirm, (dialog, which) -> {
                                        dialog.dismiss();
                                    });
                        }
                    });
        });

        mViewModel.getActiveToolHeadIndexObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(active -> {
                    Logger.d("Current active tool head is " + active);
                    mActive = active;
                    refreshSwitchButton(active);
                });

        mViewModel.getNeedHomeObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(needHome -> refreshButtonState(!needHome), LogHelper::log);

        onGetMotorPotentialState();
    }

    private void refreshButtonState(boolean enable) {
        mCsvXY.setEnabled(enable);
        mTvMovingRange01.setEnabled(enable);
        mTvMovingRange1.setEnabled(enable);
        mTvMovingRange10.setEnabled(enable);
        mLinSwitch.setEnabled(enable);
        mTvControlL.setEnabled(enable);
        mTvControlR.setEnabled(enable);
        mBtnZPlus.setEnabled(enable);
        mBtnZMinus.setEnabled(enable);
        if (enable) {
            onCheckMovingRange10();
            mBtnHome.setBackgroundResource(R.drawable.select_j1_control_home_gray_bg);
        } else {
            onDisableCheckMovingRange();
            if (mActive == -1) {
                mLinSwitch.setBackgroundResource(R.drawable.pic_tab_horizontal_top_440x104);
                mTvControlL.setTextColor(mDisabledColor);
                mTvControlR.setTextColor(mDisabledColor);
            }
            mBtnHome.setBackgroundResource(R.drawable.select_j1_control_home_orange_bg);
        }
    }

    public void onGetMotorPotentialState() {
//        mBtnHome.setBackgroundResource(R.drawable.select_j1_control_home_gray_bg);
        mViewModel.getMotorPotentialStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(aBoolean -> {
                }, LogHelper::log);
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.subscribeCoordinate();
        ServiceContainer.getInstance().getService(IMachine.class).getFDMController().requestInfoAll();
        ServiceContainer.getInstance().getService(IMachine.class).getFDMController().subscribeExtruderChange();
        mViewModel.pullCoordinateInfo();
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.unSubscribeCoordinate();
        ServiceContainer.getInstance().getService(IMachine.class).getFDMController().unSubscribeExtruderChange();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_control_jog;
    }

    @Override
    protected S30JogControlViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(S30JogControlViewModel.class);
    }

    @OnClick(R2.id.rl_btn_home)
    void onHomeClicked() {
        playNormalClickSound();
        fabHoming.show();
        mViewModel.goHome(true)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(success -> {
                    fabHoming.dismiss();
                    if (success) {
                        onGetMotorPotentialState();
                    } else {
                        FabConfirm.create(requireContext())
                                .setDescription("直线轴异常，需联系售后。")
                                .setConfirm(R.string.all_confirm, (dialog, which) -> {
                                    dialog.dismiss();
                                })
                                .show();
                    }
                }, e -> {
                    fabHoming.dismiss();
                    LogHelper.log(e);
                });

    }

    @OnClick(R2.id.btn_control_panel_z_plus)
    void onZPlusClicked() {
        playNormalClickSound();
        mViewModel.moveToPosition(MoveController.Direction.DOWN)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    if (responseStructure.isSuccess()) {

                    } else if (responseStructure.isGeneralError()) {
                        FabConfirm.create(getContext())
                                .setDescription(getString(R.string.all_moving_limitation))
                                .setConfirm(R.string.all_confirm, (dialog, which) -> {
                                    dialog.dismiss();
                                });
                    }
                });
    }

    @OnClick(R2.id.btn_control_panel_z_minus)
    void onZMinusClicked() {
        playNormalClickSound();
        mViewModel.moveToPosition(MoveController.Direction.UP)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    if (responseStructure.isSuccess()) {

                    } else if (responseStructure.isGeneralError()) {
                        FabConfirm.create(getContext())
                                .setDescription(getString(R.string.all_moving_limitation))
                                .setConfirm(R.string.all_confirm, (dialog, which) -> {
                                    dialog.dismiss();
                                });
                    }
                });
    }
}
