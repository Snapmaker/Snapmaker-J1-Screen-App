package fabscreen.features.machinetools.control.j1;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.features.machinetools.control.j1.viewmodel.J1FilamentControlViewModel;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.base.view.FileLoadingDialog;
import fabscreen.platform.core.ui.view.CustomArcSeekBar;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class J1FilamentControlFragment extends BaseFragment {
    private static final int J1_EXTRUDER_MIN_VALUE = 160;
    private static final int J1_EXTRUDER_MAX_VALUE = 300;
    @BindView(R2.id.cas_extruder_target_temp_left)
    CustomArcSeekBar mLeftCustomArcSeekBar;
    @BindView(R2.id.tv_cur_temp_l)
    TextView mLeftCurTemp;
    @BindView(R2.id.tv_target_temp_l)
    TextView mLeftTagTemp;
    @BindView(R2.id.btn_l_heat)
    Button mLeftHeating;
    @BindView(R2.id.tv_filament_out_l)
    TextView mLeftOut;
    @BindView(R2.id.tv_filament_in_l)
    TextView mLeftIn;
    @BindView(R2.id.btn_filament_movement_l)
    Button mLeftMovement;
    @BindView(R2.id.cas_extruder_target_temp_right)
    CustomArcSeekBar mRightCustomArcSeekBar;
    @BindView(R2.id.tv_cur_temp_r)
    TextView mRightCurTemp;
    @BindView(R2.id.tv_target_temp_r)
    TextView mRightTagTemp;
    @BindView(R2.id.btn_r_heat)
    Button mRightHeating;
    @BindView(R2.id.tv_filament_out_r)
    TextView mRightOut;
    @BindView(R2.id.tv_filament_in_r)
    TextView mRightIn;
    @BindView(R2.id.btn_filament_movement_r)
    Button mRightMovement;
    J1FilamentControlViewModel mViewModel;
    private int mTargetSeekBarMinValue;
    private FileLoadingDialog mMovingDialog;
    int mBodyColor;
    int mUncheckColor;

    /**
     * @param mode 0: both enabled; 1: left only; 2: right only.
     */
    public static Fragment newInstance(int mode) {
        Fragment fragment = new J1FilamentControlFragment();
        Bundle args = new Bundle();
        args.putInt("mode", mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getFragmentScopeViewModel(J1FilamentControlViewModel.class);
        getColor();
        initView();
        mMovingDialog = FileLoadingDialog.create(requireContext(), true);
        mMovingDialog.setContent(getString(R.string.j1_calibration_moving));
        mTargetSeekBarMinValue = J1_EXTRUDER_MIN_VALUE;
    }

    private void getColor() {
        mBodyColor = getValueOfColorAttr(R.attr.theme_color_title_primary);
        mUncheckColor = getValueOfColorAttr(R.attr.theme_color_tab_uncheck);
    }

    private int getValueOfColorAttr(@AttrRes int attrId) {
        TypedValue typedValue = new TypedValue();
        if (requireContext().getTheme().resolveAttribute(attrId, typedValue, true)) {
            return typedValue.data;
        } else {
            return Color.TRANSPARENT;
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_j1_control_filament;
    }

    private void initView() {
        refreshEnableStatus();
        mLeftCustomArcSeekBar.setMax(J1_EXTRUDER_MAX_VALUE - J1_EXTRUDER_MIN_VALUE);
        mRightCustomArcSeekBar.setMax(J1_EXTRUDER_MAX_VALUE - J1_EXTRUDER_MIN_VALUE);
        mLeftCustomArcSeekBar.setTag(true);
        mRightCustomArcSeekBar.setTag(true);
        mViewModel.getLeftExtruderStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(filamentExtruderState -> {
                    mLeftCustomArcSeekBar.setHeating(filamentExtruderState.isHeatingStats());
                    mLeftCurTemp.setText(String.format("%d", (int) (filamentExtruderState.getTemperature())));
                    if (filamentExtruderState.isHeatingStats()) {
                        setSeekBarProgress(mLeftCustomArcSeekBar, (int) filamentExtruderState.getTargetTemperature());
                        if (!((int) filamentExtruderState.getTargetTemperature() == 0)) {
                            mLeftTagTemp.setText(String.format("%d", (int) filamentExtruderState.getTargetTemperature()));
                        }
                    } else {
                        setSeekBarProgress(mLeftCustomArcSeekBar, filamentExtruderState.getStopTemperature());
                        mLeftTagTemp.setText(String.format("%d", filamentExtruderState.getStopTemperature()));
                    }
                    if (filamentExtruderState.isMovement()) {
                        mLeftMovement.setText(filamentExtruderState.getMovementStats() == 1 ? getString(R.string.j1_control_extruder_stop_unloading) : getString(R.string.j1_control_extruder_stop_loading));
                        mLeftMovement.setVisibility(View.VISIBLE);
                        mLeftOut.setEnabled(false);
                        mLeftIn.setEnabled(false);
                    } else {
                        mLeftMovement.setVisibility(View.GONE);
                        mLeftOut.setEnabled(filamentExtruderState.isCanMovement());
                        mLeftIn.setEnabled(filamentExtruderState.isCanMovement());
                    }
                    mLeftHeating.setText(filamentExtruderState.isHeatingStats() ? R.string.all_stop : R.string.all_heat);
                });

        mViewModel.getRightExtruderStateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(filamentExtruderState -> {
                    mRightCustomArcSeekBar.setHeating(filamentExtruderState.isHeatingStats());
                    mRightCurTemp.setText(String.format("%d", (int) filamentExtruderState.getTemperature()));
                    if (filamentExtruderState.isHeatingStats()) {
                        setSeekBarProgress(mRightCustomArcSeekBar, (int) filamentExtruderState.getTargetTemperature());
                        if (!((int) filamentExtruderState.getTargetTemperature() == 0)) {
                            mRightTagTemp.setText(String.format("%d", (int) (filamentExtruderState.getTargetTemperature())));
                        }
                    } else {
                        setSeekBarProgress(mRightCustomArcSeekBar, filamentExtruderState.getStopTemperature());
                        mRightTagTemp.setText(String.format("%d", filamentExtruderState.getStopTemperature()));
                    }
                    if (filamentExtruderState.isMovement()) {
                        mRightMovement.setText(filamentExtruderState.getMovementStats() == 1 ? getString(R.string.j1_control_extruder_stop_unloading) : getString(R.string.j1_control_extruder_stop_loading));
                        mRightMovement.setVisibility(View.VISIBLE);
                        mRightOut.setEnabled(false);
                        mRightIn.setEnabled(false);
                    } else {
                        mRightMovement.setVisibility(View.GONE);
                        mRightOut.setEnabled(filamentExtruderState.isCanMovement());
                        mRightIn.setEnabled(filamentExtruderState.isCanMovement());
                    }
                    mRightHeating.setText(filamentExtruderState.isHeatingStats() ? R.string.all_stop : R.string.all_heat);
                });

        // listen seekbar
        mLeftCustomArcSeekBar.setOnSeekArcChangeListener(new CustomArcSeekBar.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(CustomArcSeekBar customArcSeekBar, int progress, boolean fromUser) {
                mViewModel.setTargetChange(0, true);
                mViewModel.changeStopTemperature(0, progress + mTargetSeekBarMinValue);
                mLeftTagTemp.setText(String.format("%d", progress + mTargetSeekBarMinValue));
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

        mRightCustomArcSeekBar.setOnSeekArcChangeListener(new CustomArcSeekBar.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(CustomArcSeekBar customArcSeekBar, int progress, boolean fromUser) {
                mViewModel.setTargetChange(1, true);
                mViewModel.changeStopTemperature(1, progress + mTargetSeekBarMinValue);
                mRightTagTemp.setText(String.format("%d", progress + mTargetSeekBarMinValue));
            }

            @Override
            public void onStartTrackingTouch(CustomArcSeekBar customArcSeekBar) {
                customArcSeekBar.setTag(false);
            }

            @Override
            public void onStopTrackingTouch(CustomArcSeekBar customArcSeekBar) {
                customArcSeekBar.setTag(true);
                mViewModel.setTargetChange(1, false);
            }
        });
    }

    private void refreshEnableStatus() {
        int mode = requireArguments().getInt("mode", 0);
        if (mode == 0) {
            mLeftCustomArcSeekBar.setEnabled(true);
            mRightCustomArcSeekBar.setEnabled(true);
            mLeftHeating.setEnabled(true);
            mRightHeating.setEnabled(true);
        } else if (mode == 1) {
            mLeftCustomArcSeekBar.setEnabled(true);
            mRightCustomArcSeekBar.setEnabled(false);
            mLeftHeating.setEnabled(true);
            mRightHeating.setEnabled(false);
        } else if (mode == 2) {
            mLeftCustomArcSeekBar.setEnabled(false);
            mRightCustomArcSeekBar.setEnabled(true);
            mLeftHeating.setEnabled(false);
            mRightHeating.setEnabled(true);
        }
    }

    private void setSeekBarProgress(CustomArcSeekBar customArcSeekBar, int progress) {
        if (((Boolean) customArcSeekBar.getTag()) && (progress >= mTargetSeekBarMinValue)) {
            customArcSeekBar.setProgress(progress - mTargetSeekBarMinValue);
        }
    }

    @OnClick(R2.id.btn_l_heat)
    void onSwitchLHeating() {
        int index = 0;
        int target = mViewModel.getTemperature(index, mLeftCustomArcSeekBar.getProgress() + mTargetSeekBarMinValue);
        mViewModel.changeHeating(index, target);
    }

    @OnClick(R2.id.btn_r_heat)
    void onSwitchRHeating() {
        int index = 1;
        int target = mViewModel.getTemperature(index, mRightCustomArcSeekBar.getProgress() + mTargetSeekBarMinValue);
        mViewModel.changeHeating(index, target);
    }

    public void showNeedMoveDialog(int index, int moveType) {
        DecisionDialog.create(getContext())
                .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
                .setType(DecisionDialog.TIP_TYPE)
                .setContent(R.string.j1_control_load_filament_before_msg)
                .setFirstTv(getResources().getString(R.string.guide_got_it), R.color.select_dialog_yellow_txt, (dialog, which) -> {
                    dialog.dismiss();
                    mMovingDialog.show();
                    // the maximum nozzle is 1. Subtract the current operating nozzle (0, 1) to obtain the nozzle to be operated
                    int otherIndex = 1 - index;
                    int movementStats = mViewModel.getExtruderStateValue(otherIndex).movementStats;
                    if (movementStats != -1) {
                        mViewModel.movementExtruder(otherIndex, -1);
                    }
                    mViewModel.moveToGoodXPosition()
                            .observeOn(AndroidSchedulers.mainThread())
                            .as(bindToLifecycle())
                            .subscribe(aBoolean -> {
                                mMovingDialog.dismiss();
                                if (aBoolean) {
                                    mViewModel.movementExtruder(index, moveType);
                                }
                                if (movementStats != -1) {
                                    mViewModel.movementExtruder(otherIndex, movementStats);
                                }
                            }, LogHelper::log);
                })
                .show();
    }

    @OnClick(R2.id.tv_filament_out_l)
    void onCheckLOut() {
        mViewModel.checkMove(0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(needMove -> {
                    if (needMove) {
                        showNeedMoveDialog(0, 0);
                    } else {
                        mViewModel.movementExtruder(0, 0);
                    }
                });
    }

    @OnClick(R2.id.tv_filament_in_l)
    void onCheckLIn() {
        mViewModel.checkMove(0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(needMove -> {
                    if (needMove) {
                        showNeedMoveDialog(0, 1);
                    } else {
                        mViewModel.movementExtruder(0, 1);
                    }
                });
    }

    @OnClick(R2.id.btn_filament_movement_l)
    void onCheckLStop() {
        mViewModel.movementExtruder(0, -1);
    }

    @OnClick(R2.id.tv_filament_out_r)
    void onCheckROut() {
        mViewModel.checkMove(1)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(needMove -> {
                    if (needMove) {
                        showNeedMoveDialog(1, 0);
                    } else {
                        mViewModel.movementExtruder(1, 0);
                    }
                });
    }

    @OnClick(R2.id.tv_filament_in_r)
    void onCheckRIn() {
        mViewModel.checkMove(1)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(needMove -> {
                    if (needMove) {
                        showNeedMoveDialog(1, 1);
                    } else {
                        mViewModel.movementExtruder(1, 1);
                    }
                });
    }

    @OnClick(R2.id.btn_filament_movement_r)
    void onCheckRStop() {
        mViewModel.movementExtruder(1, -1);
    }


    @Override
    public void onResume() {
        super.onResume();
        mViewModel.subscribeDataChange();
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.unSubscribeDataChange();
        if (mViewModel.getLeftExtruderStateValue().isMovement()) {
            mViewModel.movementExtruder(0, -1);
        }
        if (mViewModel.getRightExtruderStateValue().isMovement()) {
            mViewModel.movementExtruder(1, -1);
        }
        ;
    }
}
