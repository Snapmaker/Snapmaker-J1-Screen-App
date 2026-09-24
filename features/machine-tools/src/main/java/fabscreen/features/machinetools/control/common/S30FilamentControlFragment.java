package fabscreen.features.machinetools.control.common;

import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Guideline;
import androidx.fragment.app.Fragment;

import com.orhanobut.logger.Logger;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import fabscreen.features.machinetools.R;
import fabscreen.features.machinetools.R2;
import fabscreen.platform.base.view.BaseFragment;
import fabscreen.platform.base.view.FabScreenDialog;
import fabscreen.platform.core.ui.view.CustomArcSeekBar;
import fabscreen.platform.core.ui.view.FabProgressDialog;
import fabscreen.platform.core.ui.view.RotateButtonView;
import fabscreen.platform.lib.LogHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class S30FilamentControlFragment extends BaseFragment {
    private static final int J1_EXTRUDER_MIN_VALUE = 180;
    private static final int J1_EXTRUDER_MAX_VALUE = 300;
    private static final int A400_EXTRUDER_MIN_VALUE = 160;
    private static final int A400_EXTRUDER_MAX_VALUE = 320;

    @BindView(R2.id.btn_filament_out_l)
    Button mBtnFilamentOutL;
    @BindView(R2.id.btn_filament_out_r)
    Button mBtnFilamentOutR;
    @BindView(R2.id.btn_filament_in_l)
    Button mBtnFilamentInL;
    @BindView(R2.id.btn_filament_in_r)
    Button mBtnFilamentInR;
    @BindView(R2.id.tv_target_temp_l)
    TextView mTvTargetTempL;
    @BindView(R2.id.tv_target_temp_r)
    TextView mTvTargetTempR;
    @BindView(R2.id.tv_cur_temp_l)
    TextView mTvCurTempL;
    @BindView(R2.id.tv_cur_temp_r)
    TextView mTvCurTempR;
    @BindView(R2.id.gl_vertical)
    Guideline mGlVertical;
    @BindView(R2.id.tv_l_hotend_title)
    TextView mTvLHotendTitle;
    @BindView(R2.id.btn_e0_heat)
    Button mBtnE0Heat;
    @BindView(R2.id.btn_e1_heat)
    Button mBtnE1Heat;
    @BindView(R2.id.btn_switch_left)
    Button mBtnSwitchLeft;
    @BindView(R2.id.btn_switch_right)
    Button mBtnSwitchRight;

    @BindView(R2.id.cas_extruder_target_temp_left)
    CustomArcSeekBar mCasTargetTempLeft;
    @BindView(R2.id.cas_extruder_target_temp_right)
    CustomArcSeekBar mCasTargetTempRight;

    FabProgressDialog mPdSwitchExtruder;
    protected S30FilamentControlViewModel mViewModel;
    private int mTargetSeekBarMinValue = 0;
    private FabProgressDialog mMovingDialog;
    private final int TEMPERATURE_ERROR_RANGE = 3;
    private boolean mLeftExtruderMovement = true;
    private boolean mRightExtruderMovement = true;

    @BindView(R2.id.rbv_a400_test)
    RotateButtonView mRbvTest;

    public static Fragment newInstance() {
        return new S30FilamentControlFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = getViewModel();
        initView();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_s30_control_filament;
    }

    private void initView() {
        mPdSwitchExtruder = new FabProgressDialog(requireContext());
        mPdSwitchExtruder.setMessage(R.string.control_swith_extruder);
        // j1 330, a400 270
        // TODO: Should we distinguish J1 and A400 filament page for different feature?
        if (mViewModel.isJ1()) {
            // FIXME: workaround for seekbar could not set MinValue by calling setMin();
            //  1. set SeekBar MaxValue = delta(MAX, MIN)
            //  2. actualValue = SEEKBAR_MIN_VALUE + progress
            mTargetSeekBarMinValue = J1_EXTRUDER_MIN_VALUE;
            mCasTargetTempRight.setMax(J1_EXTRUDER_MAX_VALUE - J1_EXTRUDER_MIN_VALUE);
            mCasTargetTempLeft.setMax(J1_EXTRUDER_MAX_VALUE - J1_EXTRUDER_MIN_VALUE);

            // don't show switch extruder buttons on J1
            mBtnSwitchLeft.setVisibility(Button.GONE);
            mBtnSwitchRight.setVisibility(Button.GONE);
        } else {
            mTargetSeekBarMinValue = A400_EXTRUDER_MIN_VALUE;
            mCasTargetTempLeft.setMax(A400_EXTRUDER_MAX_VALUE - A400_EXTRUDER_MIN_VALUE);
            mCasTargetTempRight.setMax(A400_EXTRUDER_MAX_VALUE - A400_EXTRUDER_MIN_VALUE);
        }

        // Set a tag to tell view whether it should update with new subscribe values.
        // See method setSeekBarProgress() in this page.
        mCasTargetTempLeft.setTag(true);
        mCasTargetTempRight.setTag(true);

        // FIXME: Test display code, to be deleted
        mRbvTest.setCrollerChangeListener(new RotateButtonView.OnCrollerChangeListener() {
            @Override
            public void onProgressChanged(RotateButtonView croller, float progress) {

            }

            @Override
            public void onStartTrackingTouch(RotateButtonView croller, float progress) {
                // 做个过滤？
                mViewModel.setTargetTemp(0, (int) progress);
            }

            @Override
            public void onStopTrackingTouch(RotateButtonView croller, float progress) {
                mViewModel.setTargetTemp(0, (int) progress);
            }
        });
        mRbvTest.setMax(A400_EXTRUDER_MAX_VALUE);
        mRbvTest.setMin(A400_EXTRUDER_MIN_VALUE);

        mViewModel.getTargetTempObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(temps -> {
                    mRbvTest.setColor1Progress(temps.get(0).intValue());
                    setSeekBarProgress(mCasTargetTempLeft, temps.get(0).intValue());
                    if (temps.size() > 1) {
                        setSeekBarProgress(mCasTargetTempRight, temps.get(1).intValue());
                    }
                    mTvTargetTempL.setText(String.format(Locale.getDefault(), "%.0f", temps.get(0)));
                    mTvTargetTempR.setText(String.format(Locale.getDefault(), "%.0f", temps.get(1)));
                }, LogHelper::log);

        mViewModel.getCurrentTempObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(temps -> {
                    mRbvTest.setColor2Progress(temps.get(0).intValue());
                    mTvCurTempL.setText(String.format(Locale.getDefault(), "%.0f", temps.get(0)));
                    mTvCurTempR.setText(String.format(Locale.getDefault(), "%.0f", temps.get(1)));
                }, LogHelper::log);

        Observable.zip(mViewModel.getTargetTempObservable(), mViewModel.getCurrentTempObservable(), (targetTemps, currentTemps) -> {
                    SparseBooleanArray booleanSparseArray = new SparseBooleanArray();
                    booleanSparseArray.put(0, currentTemps.get(0).intValue() >= targetTemps.get(0).intValue() - TEMPERATURE_ERROR_RANGE);
                    if (targetTemps.size() > 1 && currentTemps.size() > 1) {
                        booleanSparseArray.put(1, currentTemps.get(1).intValue() >= targetTemps.get(1).intValue() - TEMPERATURE_ERROR_RANGE);
                    }
                    return booleanSparseArray;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(booleanSparseArray -> {
                    SparseBooleanArray heatOn = mViewModel.getHeatOn();
                    boolean LStats = booleanSparseArray.get(0) && heatOn.get(0) && mLeftExtruderMovement;
                    mBtnFilamentOutL.setEnabled(LStats);
                    mBtnFilamentInL.setEnabled(LStats);
                    if (booleanSparseArray.size() > 1) {
                        boolean RStats = booleanSparseArray.get(1) && heatOn.get(1) && mRightExtruderMovement;
                        mBtnFilamentOutR.setEnabled(RStats);
                        mBtnFilamentInR.setEnabled(RStats);
                    }
                }, LogHelper::log);

        // display nozzle temperature
        /*mViewModel.getExtruderListObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(extruders -> {
                    if (extruders == null || extruders.size() == 0) return;
                    int extruder0Target = (int) (extruders.get(0).getTargetTemperature());
                    int extruder0Cur = (int) (extruders.get(0).getTemperature());

                    setSeekBarProgress(mSbTargetLeft, extruder0Target);
                    mPbCurrentLeft.setProgress(extruder0Cur);
                    mTvCurTempL.setText(String.valueOf(extruder0Cur));

                    if (extruders.size() > 1) {
                        int extruder1Target = (int) (extruders.get(1).getTargetTemperature());
                        int extruder1Cur = (int) (extruders.get(1).getTemperature());
                        setSeekBarProgress(mSbTargetRight, extruder1Target);
                        mPbCurrentRight.setProgress(extruder1Cur);
                        mTvCurTempR.setText(String.valueOf(extruder1Cur));
                    }
                });*/

        // listen seekbar
        mCasTargetTempLeft.setOnSeekArcChangeListener(new CustomArcSeekBar.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(CustomArcSeekBar customArcSeekBar, int progress, boolean fromUser) {
                mTvTargetTempL.setText(String.format(Locale.getDefault(), "%.0f", (float) progress + mTargetSeekBarMinValue));
            }

            @Override
            public void onStartTrackingTouch(CustomArcSeekBar customArcSeekBar) {
                customArcSeekBar.setTag(false);
            }

            @Override
            public void onStopTrackingTouch(CustomArcSeekBar customArcSeekBar) {
                customArcSeekBar.setTag(true);
                mViewModel.setTargetTemp(0, customArcSeekBar.getProgress() + mTargetSeekBarMinValue);
            }
        });
        mCasTargetTempRight.setOnSeekArcChangeListener(new CustomArcSeekBar.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(CustomArcSeekBar customArcSeekBar, int progress, boolean fromUser) {
                mTvTargetTempR.setText(String.format(Locale.getDefault(), "%.0f", (float) progress + mTargetSeekBarMinValue));
            }

            @Override
            public void onStartTrackingTouch(CustomArcSeekBar customArcSeekBar) {
                customArcSeekBar.setTag(false);
            }

            @Override
            public void onStopTrackingTouch(CustomArcSeekBar customArcSeekBar) {
                customArcSeekBar.setTag(true);
                mViewModel.setTargetTemp(1, customArcSeekBar.getProgress() + mTargetSeekBarMinValue);
            }
        });

        showSecondExtruderView(mViewModel.hasSecondExtruder());

        mViewModel.getHeatOnObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(isOnArray -> {
                    mBtnE0Heat.setText(isOnArray.get(0) ? "停止加热" : "开始加热");
                    mBtnE1Heat.setText(isOnArray.get(1) ? "停止加热" : "开始加热");
                }, LogHelper::log);

        mMovingDialog = new FabProgressDialog(requireContext());
        mMovingDialog.setMessage(R.string.move_show);

        mViewModel.getIsMovingObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(this::handleMovingDialog, LogHelper::log);

        mViewModel.getExtruderPositionErrorObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(trigger -> showPositionCannotExtrudeDialog(), LogHelper::log);


    }

    private void showPositionCannotExtrudeDialog() {
        FabScreenDialog.create(requireContext())
                .setDescription("The extruders will move into position later for you to load the filament.")
                .setConfirm(R.string.guide_got_it, (dialog, which) -> {
                    dialog.dismiss();
                    mViewModel.moveToGoodXPosition();
                })
                .show();
    }

    private void handleMovingDialog(boolean isMoving) {
        if (isMoving) {
            showDialog(mMovingDialog);
        } else {
            dismissDialog(mMovingDialog);
        }
    }

    private void showSecondExtruderView(boolean hasSecondExtruder) {
        if (hasSecondExtruder) {
            mGlVertical.setGuidelinePercent(0.5f);
            mTvLHotendTitle.setText("左喷嘴温度");
        } else {
            mGlVertical.setGuidelinePercent(1f);
            mTvLHotendTitle.setText("喷嘴温度");
        }
    }

    private void setSeekBarProgress(SeekBar seekBar, int progress) {
        if ((Boolean) seekBar.getTag()) {
            seekBar.setProgress(progress - mTargetSeekBarMinValue);
        }
    }

    private void setSeekBarProgress(CustomArcSeekBar customArcSeekBar, int progress) {
        if ((Boolean) customArcSeekBar.getTag()) {
            customArcSeekBar.setProgress(progress - mTargetSeekBarMinValue);
        }
    }

    @Override
    protected S30FilamentControlViewModel getViewModel() {
        return getViewFragmentScopeViewModelProvider().get(S30FilamentControlViewModel.class);
    }

    @OnClick(R2.id.btn_filament_in_l)
    protected void onFilamentLInClicked(View view) {
        playNormalClickSound();
        mLeftExtruderMovement = false;
        mBtnFilamentInL.setEnabled(mLeftExtruderMovement);
        mBtnFilamentOutL.setEnabled(mLeftExtruderMovement);
        mViewModel.loadFilament(0)
                .as(bindToLifecycle())
                .subscribe(structure -> {
                    mLeftExtruderMovement = true;
                    if (structure.isSuccess()) {
                        Logger.d("Filament loaded.");
                    } else {
                        Logger.d("Filament load fail.");
                    }
                }, LogHelper::log);
    }

    @OnClick(R2.id.btn_filament_in_r)
    protected void onFilamentRInClicked(View view) {
        playNormalClickSound();
        mRightExtruderMovement = false;
        mBtnFilamentInR.setEnabled(mRightExtruderMovement);
        mBtnFilamentOutR.setEnabled(mRightExtruderMovement);
        mViewModel.loadFilament(1)
                .as(bindToLifecycle())
                .subscribe(structure -> {
                    mRightExtruderMovement = true;
                    if (structure.isSuccess()) {
                        Logger.d("Filament loaded.");
                    } else {
                        Logger.d("Filament load fail.");
                    }
                }, LogHelper::log);
    }

    @OnClick(R2.id.btn_filament_out_l)
    protected void onFilamentLOutClicked(View view) {
        playNormalClickSound();
        mBtnFilamentInL.setEnabled(false);
        mBtnFilamentOutL.setEnabled(false);
        mViewModel.unloadFilament(0)
                .as(bindToLifecycle())
                .subscribe(structure -> {
                    mBtnFilamentInL.setEnabled(true);
                    mBtnFilamentOutL.setEnabled(true);
                    if (structure.isSuccess()) {
                        Logger.d("Filament loaded.");
                    } else {
                        Logger.d("Filament load fail.");
                    }
                }, LogHelper::log);
    }

    @OnClick(R2.id.btn_filament_out_r)
    protected void onFilamentROutClicked(View view) {
        playNormalClickSound();
        mBtnFilamentInR.setEnabled(false);
        mBtnFilamentOutR.setEnabled(false);
        mViewModel.unloadFilament(1)
                .as(bindToLifecycle())
                .subscribe(structure -> {
                    mBtnFilamentInR.setEnabled(true);
                    mBtnFilamentOutR.setEnabled(true);
                    if (structure.isSuccess()) {
                        Logger.d("Filament loaded.");
                    } else {
                        Logger.d("Filament load fail.");
                    }
                }, LogHelper::log);
    }

    @OnClick({R2.id.btn_e0_heat, R2.id.btn_e1_heat})
    void onHeatClicked(Button button) {
        playNormalClickSound();

        int index = -1;

        if (button.getId() == R.id.btn_e0_heat) {
            index = 0;
        } else if (button.getId() == R.id.btn_e1_heat) {
            index = 1;
        }
        boolean isHeatOn = mViewModel.getIsHeatOn(index);

        Logger.d("flmt: clicked %s", isHeatOn);

        if (!isHeatOn) {
            // not heating, prepare heating
            if (index == 0) {
                mCasTargetTempLeft.setEnabled(true);
                mCasTargetTempLeft.setTag(true);
            } else {
                mCasTargetTempRight.setEnabled(true);
                mCasTargetTempRight.setTag(true);
            }
            mViewModel.startHeat(index);
        } else {
            if (index == 0) {
                mCasTargetTempLeft.setEnabled(false);
                mCasTargetTempLeft.setTag(false);
            } else {
                mCasTargetTempRight.setEnabled(false);
                mCasTargetTempRight.setTag(false);
            }
            mViewModel.setHeatOn(index, false);
        }
    }

    @OnClick(R2.id.btn_switch_left)
    void onSwitchLClicked() {
        playNormalClickSound();
        mPdSwitchExtruder.show();
        mViewModel.switchExtruder(0, 0)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(result -> {
                    mPdSwitchExtruder.dismiss();
                }, e -> {
                    mPdSwitchExtruder.dismiss();
                    LogHelper.log(e);
                });
    }

    @OnClick(R2.id.btn_switch_right)
    void onSwitchRClicked() {
        playNormalClickSound();
        mPdSwitchExtruder.show();
        mViewModel.switchExtruder(0, 1)
                .observeOn(AndroidSchedulers.mainThread())
                .as(bindToLifecycle())
                .subscribe(result -> {
                    mPdSwitchExtruder.dismiss();
                }, e -> {
                    mPdSwitchExtruder.dismiss();
                    LogHelper.log(e);
                });
    }


    protected int getExtruderIndexByViewId(int viewId) {
        if (viewId == R.id.btn_filament_in_l || viewId == R.id.btn_filament_out_l) {
            return 0;
        } else if (viewId == R.id.btn_filament_in_r || viewId == R.id.btn_filament_out_r) {
            return 1;
        } else {
            return 0;
        }
    }
}
