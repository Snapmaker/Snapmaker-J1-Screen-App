package fabscreen.features.print.j1platform;

import static fabscreen.platform.base.service.machine.controller.MachineOperationStatus.SYSTEM_STATUS_PAUSED;

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
import fabscreen.features.print.R;
import fabscreen.features.print.R2;
import fabscreen.features.print.j1platform.viewmodel.PrintJ1AdjustmentExtruderViewModel;
import fabscreen.platform.base.instantiation.ServiceContainer;
import fabscreen.platform.base.service.IMachine;
import fabscreen.platform.base.service.machine.controller.NewPrintController;
import fabscreen.platform.base.service.machine.controller.PrintController;
import fabscreen.platform.base.service.machine.structure.BaseStructure;
import fabscreen.platform.base.service.machine.structure.prop.UInt8Prop;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.DecisionDialog;
import fabscreen.platform.base.view.FileLoadingDialog;
import fabscreen.platform.core.ui.view.CustomArcSeekBar;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class PrintJ1AdjustmentExtruderFragment extends BaseFragment {
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

    private PrintController mPrintController;
    private NewPrintController mNewPrintController;

    private PrintJ1AdjustmentExtruderViewModel mViewModel;
    private int mTargetSeekBarMinValue;
    private FileLoadingDialog mMovingDialog;
    int mBodyColor;
    int mUncheckColor;


    public static Fragment newInstance() {
        return new PrintJ1AdjustmentExtruderFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrintController = ServiceContainer.getInstance().getService(IMachine.class).getPrintController();
        mNewPrintController = ServiceContainer.getInstance().getService(IMachine.class).getNewPrintController();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getFragmentScopeViewModel(PrintJ1AdjustmentExtruderViewModel.class);
        getColor();
        initView();
        mMovingDialog = FileLoadingDialog.create(requireContext(), true);
        mMovingDialog.setContent(getString(R.string.move_show));
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
        return R.layout.fragment_j1_print_adjustment_extruder;
    }

    private void initView() {
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
                        mLeftMovement.setText(filamentExtruderState.getMovementStats() == 1 ? getString(R.string.j1_print_setting_stop_unloading) : getString(R.string.j1_print_setting_stop_loading));
                        mLeftMovement.setVisibility(View.VISIBLE);
                        mLeftOut.setEnabled(false);
                        mLeftIn.setEnabled(false);
                    } else {
                        mLeftMovement.setVisibility(View.GONE);
                        mLeftOut.setEnabled(filamentExtruderState.isCanMovement() && SYSTEM_STATUS_PAUSED.valueEquals(mViewModel.getMachineStatus()));
                        mLeftIn.setEnabled(filamentExtruderState.isCanMovement() && SYSTEM_STATUS_PAUSED.valueEquals(mViewModel.getMachineStatus()));
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
                        mRightMovement.setText(filamentExtruderState.getMovementStats() == 1 ? getString(R.string.j1_print_setting_stop_unloading) : getString(R.string.j1_print_setting_stop_loading));
                        mRightMovement.setVisibility(View.VISIBLE);
                        mRightOut.setEnabled(false);
                        mRightIn.setEnabled(false);
                    } else {
                        mRightMovement.setVisibility(View.GONE);
                        mRightOut.setEnabled(filamentExtruderState.isCanMovement() && SYSTEM_STATUS_PAUSED.valueEquals(mViewModel.getMachineStatus()));
                        mRightIn.setEnabled(filamentExtruderState.isCanMovement() && SYSTEM_STATUS_PAUSED.valueEquals(mViewModel.getMachineStatus()));
                    }
                    mRightHeating.setText(filamentExtruderState.isHeatingStats() ? R.string.all_stop : R.string.all_heat);
                });
        mViewModel.getMachineStatusObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(state -> {
                    mRightHeating.setEnabled(SYSTEM_STATUS_PAUSED.valueEquals(state));
                    mLeftHeating.setEnabled(SYSTEM_STATUS_PAUSED.valueEquals(state));
                });

//        Observable.interval(1, TimeUnit.SECONDS)
//                .observeOn(AndroidSchedulers.mainThread())
//                .as(bindToLifecycle())
//                .subscribe(tick -> refreshHeatUI(), LogHelper::log);

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

    // TODO: 2022/8/3 need to be refactored to subscribe
    private void refreshHeatUI() {
        mNewPrintController.getPrintHeadState()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(responseStructure -> {
                    BaseStructure baseStructure = (BaseStructure) responseStructure.dataProp;
                    int leftEnabled = ((UInt8Prop) baseStructure.getProp("left_enable")).getValue();
                    int rightEnabled = ((UInt8Prop) baseStructure.getProp("right_enable")).getValue();
                    mLeftHeating.setEnabled(leftEnabled == 1);
                    mLeftCustomArcSeekBar.setEnabled(leftEnabled == 1);
                    if (leftEnabled != 1) mViewModel.changeHeating(0, 0);
                    mRightHeating.setEnabled(rightEnabled == 1);
                    mRightCustomArcSeekBar.setEnabled(rightEnabled == 1);
                    if (rightEnabled != 1) mViewModel.changeHeating(1, 0);
                });
    }

    @Override
    protected PrintJ1AdjustmentExtruderViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(PrintJ1AdjustmentExtruderViewModel.class);
    }

    private void setSeekBarProgress(CustomArcSeekBar customArcSeekBar, int progress) {
        if (((Boolean) customArcSeekBar.getTag()) && (progress >= mTargetSeekBarMinValue)) {
            customArcSeekBar.setProgress(progress - mTargetSeekBarMinValue);
        }
    }

    @OnClick(R2.id.btn_l_heat)
    void onSwitchLHeating() {
        playSwitchSound();
        int index = 0;
        int target = mViewModel.getTemperature(index, mLeftCustomArcSeekBar.getProgress() + mTargetSeekBarMinValue);
        mViewModel.changeHeating(index, target);
//        if (target == 0) {
//            mViewModel.changeHeating(index, target);
//        } else {
//            mViewModel.checkMove(index)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .as(bindToLifecycle())
//                    .subscribe(needMove -> {
//                        if (needMove) {
//                            showNeedMoveDialog(index, target);
//                        } else {
//                            mViewModel.changeHeating(index, target);
//                        }
//                    });
//        }
    }

    @OnClick(R2.id.btn_r_heat)
    void onSwitchRHeating() {
        playSwitchSound();
        int index = 1;
        int target = mViewModel.getTemperature(index, mRightCustomArcSeekBar.getProgress() + mTargetSeekBarMinValue);
        mViewModel.changeHeating(index, target);
//        if (target == 0) {
//            mViewModel.changeHeating(index, target);
//        } else {
//            mViewModel.checkMove(index)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .as(bindToLifecycle())
//                    .subscribe(needMove -> {
//                        if (needMove) {
//                            showNeedMoveDialog(index, target);
//                        } else {
//                            mViewModel.changeHeating(index, target);
//                        }
//                    });
//        }
    }

    public void showNeedMoveDialog(int index, int moveType) {
        DecisionDialog.create(getContext())
                .setDialogStatus(DecisionDialog.BTN_ONE, false, false, false, true)
                .setType(DecisionDialog.TIP_TYPE)
                .setContent(R.string.print_adjustment_move_machine_msg)
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
        playSwitchSound();
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
        playSwitchSound();
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
        playSwitchSound();
        mViewModel.movementExtruder(0, -1);
    }

    @OnClick(R2.id.tv_filament_out_r)
    void onCheckROut() {
        playSwitchSound();
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
        playSwitchSound();
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
        playSwitchSound();
        mViewModel.movementExtruder(1, -1);
    }


    @Override
    public void onResume() {
        super.onResume();
        refreshHeatUI();
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
    }

}
